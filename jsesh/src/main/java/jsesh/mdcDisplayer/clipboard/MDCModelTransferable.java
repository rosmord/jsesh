/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Dimension2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import jsesh.graphics.export.rtf.RTFExportPreferences;
import jsesh.graphics.export.rtf.RTFExporter;
import jsesh.graphics.export.pdfExport.PDFDataSaver;
import jsesh.mdc.model.ListOfTopItems;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;
import jsesh.mdcDisplayer.preferences.PageLayout;
import jsesh.utils.DoubleDimensions;

import org.qenherkhopeshef.graphics.emf.EMFGraphics2D;
import org.qenherkhopeshef.graphics.generic.RandomAccessByteArray;
import org.qenherkhopeshef.graphics.pict.MacPictGraphics2D;
import org.qenherkhopeshef.graphics.vectorClipboard.EMFTransferable;

/**
 *
 * Transfert handler for Manuel de Codage texts. Can provide manuel de codage
 * structured data (ListOfTopItems), RTF text, images, plain text. One problem
 * is that some of these can be way too large. For instance, bitmap pictures can
 * consume all your memory. Alas, some softwares (an, it seems, Java software)
 * tend to ask for all flavors when pasting.
 *
 * Thus we have to restrict what is copied.
 *
 * @author rosmord
 
 
 TODO: RtfPreferences should be something like "export preferences".
 TODO: It should be responsible for building the export specifications.
 TODO: The graphical drawers in RTFExporter should be reused.
 *
 */
public class MDCModelTransferable implements Transferable {

    private final TopItemList topItemList;

    private RTFExportPreferences rtfPreferences;

    private DrawingSpecification drawingSpecifications;

    private MDCClipboardPreferences clipboardPreferences = new MDCClipboardPreferences();

    private final int maxBitmapWidth;
    private final int maxBitmapHeight;

    private final DataFlavor dataFlavors[];

    public MDCModelTransferable(DataFlavor[] dataFlavors, TopItemList list) {
        this.topItemList = list;
        maxBitmapWidth = 2000;
        maxBitmapHeight = 2000;
        rtfPreferences = new RTFExportPreferences();
        this.drawingSpecifications = new DrawingSpecificationsImplementation();
        this.dataFlavors = dataFlavors;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return dataFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        DataFlavor[] flavors = getTransferDataFlavors();
        return Arrays.asList(flavors).contains(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        Object result;
        // Temporiser pour tester...

        if (JSeshPasteFlavors.RTFFlavor.equals(flavor)) {
            result = getRtfData();
        } else if (DataFlavor.stringFlavor.equals(flavor)) {
            return getStringData();
        } else if (JSeshPasteFlavors.ListOfTopItemsFlavor.equals(flavor)) {
            return getItemListData();
        } else if (DataFlavor.imageFlavor.equals(flavor)) {
            result = getImageData();
        } else if (JSeshPasteFlavors.MACPictFlavor.equals(flavor)) {
            result = getMacPictData();
        } else if (JSeshPasteFlavors.PDFFlavor.equals(flavor)) {
            result = getPDFData();
        } else if (EMFTransferable.EMF_FLAVOR.equals(flavor)) {
            result = getEMFData();
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
        return result;
    }

    /**
     * build and return binary EMF data for the clipboard.
     *
     * @return a ByteArrayInputStream
     * @throws IOException
     */
    private ByteArrayInputStream getEMFData() throws IOException {
        int cadratHeight= rtfPreferences.getCadratHeight();
        // Remove page margins, which are not useful for embedding.
        // TODO : Unify this with the way EMF is built in RTFExporter.
        DrawingSpecification currentSpecifications = createEmbeddedDrawingSpecifications();
        // the memory space in which to write the EMF content.
        RandomAccessByteArray out = new RandomAccessByteArray();
        MDCView view = new SimpleViewBuilder().buildView(topItemList,
                currentSpecifications);
        Dimension2D dims = new DoubleDimensions(view.getWidth(),
                view.getHeight());
        EMFGraphics2D g = new EMFGraphics2D(out, dims, "JSesh",
                topItemList.toMdC());
        ViewDrawer drawer = new ViewDrawer();
        drawer.setShadeAfter(false);
        drawer.draw(g, view, currentSpecifications);
        g.dispose();
        return new ByteArrayInputStream(out.getByteArray());
    }

    /**
     * Get pdf as binary content for the clipboard.
     *
     * @return the pdf
     * @throws IOException
     */
    private ByteArrayInputStream getPDFData() throws IOException {
        DrawingSpecification drawingSpecification = getDrawingSpecifications()
                .copy();
        // Target Cadrat height, in points.
        float targetHeight = rtfPreferences.getCadratHeight();
        float scale = targetHeight / drawingSpecification.getMaxCadratHeight();
        PDFDataSaver pdfDataSaver = new PDFDataSaver(drawingSpecification);
        pdfDataSaver.setScale(scale);
        return pdfDataSaver.createPDFContent(topItemList);
    }

    /**
     * @return
     */
    private ByteArrayInputStream getMacPictData() {
        DrawingSpecification currentSpecifications = createEmbeddedDrawingSpecifications();
        MacPictGraphics2D g = new MacPictGraphics2D();
        MDCView view = new SimpleViewBuilder().buildView(topItemList,
                currentSpecifications);
        new ViewDrawer().draw(g, view, currentSpecifications);
        g.dispose();
        return new ByteArrayInputStream(g.getAsArray());
    }

    /**
     * @return
     */
    private BufferedImage getImageData() {
        BufferedImage result;
        MDCDrawingFacade facade = new MDCDrawingFacade();
        facade.setDrawingSpecifications(createEmbeddedDrawingSpecifications());
        facade.setMaxSize(maxBitmapWidth, maxBitmapHeight);
        facade.setCadratHeight(rtfPreferences.getCadratHeight());
        result = facade.createImage(topItemList);
        return result;
    }

    /**
     * @return
     */
    private ListOfTopItems getItemListData() {
        ListOfTopItems l = new ListOfTopItems();
        for (int i = 0; i < topItemList.getNumberOfChildren(); i++) {
            l.add(topItemList.getTopItemAt(i).deepCopy());
        }
        return l;
    }

    /**
     * @return
     */
    private String getStringData() {
        return topItemList.toMdC(true);
    }

    /**
     * @return @throws IOException
     * @throws UnsupportedEncodingException
     */
    private ByteArrayInputStream getRtfData() throws IOException,
            UnsupportedEncodingException {
        ByteArrayInputStream result;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        if (topItemList.getNumberOfChildren() < 5000) {
            DrawingSpecification currentSpecifications=
                    createEmbeddedDrawingSpecifications();
            RTFExporter rtfExporter = new RTFExporter();
            rtfExporter.setDrawingSpecifications(currentSpecifications);
            rtfExporter.setViewBuilder(new SimpleViewBuilder());
            rtfExporter.setRtfPreferences(rtfPreferences);
            rtfExporter.ExportModelTo(topItemList, outputStream);
            result = new ByteArrayInputStream(outputStream.toByteArray());
        } else {
            String data = "{\\rtf1\\ansi\\ansicpg1252Jsesh : "
                    + "sorry, data too large for RTF cut and paste."
                    + " Please copy a smaller part of your text.\\par}";
            result = new ByteArrayInputStream(data.getBytes("US-ASCII"));
        }
        return result;
    }

    /**
     * @return Returns the drawingSpecifications.
     */
    public DrawingSpecification getDrawingSpecifications() {
        return drawingSpecifications;
    }

    private DrawingSpecification createEmbeddedDrawingSpecifications() {
        DrawingSpecification currentSpecifications = getDrawingSpecifications();
        PageLayout pageLayout = currentSpecifications.getPageLayout();
        pageLayout.setTopMargin(0);
        pageLayout.setLeftMargin(0);
        currentSpecifications.setPageLayout(pageLayout);
        return currentSpecifications;
    }

    /**
     * @param drawingSpecifications The drawingSpecifications to set.
     */
    public void setDrawingSpecifications(
            DrawingSpecification drawingSpecifications) {
        this.drawingSpecifications = drawingSpecifications.copy();
    }

    /**
     * @return Returns the rtfPreferences.
     */
    public RTFExportPreferences getRtfPreferences() {
        return rtfPreferences;
    }

    /**
     * @param rtfPreferences The rtfPreferences to set.
     */
    public void setRtfPreferences(RTFExportPreferences rtfPreferences) {
        this.rtfPreferences = rtfPreferences;
    }

    public void setClipboardPreferences(
            MDCClipboardPreferences clipboardPreferences) {
        this.clipboardPreferences = clipboardPreferences;
    }

    public MDCClipboardPreferences getClipboardPreferences() {
        return clipboardPreferences;
    }

}

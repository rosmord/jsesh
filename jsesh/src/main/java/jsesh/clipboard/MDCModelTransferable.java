/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.qenherkhopeshef.graphics.pict.MacPictGraphics2D;
import org.qenherkhopeshef.graphics.vectorClipboard.EMFTransferable;

import jsesh.graphics.export.emf.EmbeddableEMFSimpleDrawer;
import jsesh.graphics.export.pdfExport.PDFDataSaver;
import jsesh.graphics.export.rtf.RTFExportPreferences;
import jsesh.graphics.export.rtf.RTFExporter;
import jsesh.mdc.model.ListOfTopItems;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.context.JSeshRenderContext;
import jsesh.mdcDisplayer.context.JSeshTechRenderContext;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;

/**
 * Transfert handler for Manuel de Codage texts. Can provide manuel de codage
 * structured data (ListOfTopItems), RTF text, images, plain text. One problem
 * is that some of these can be way too large. For instance, bitmap pictures can
 * consume all your memory. Alas, some softwares (an, it seems, Java software)
 * tend to ask for all flavors when pasting.
 *
 * Thus we have to restrict what is copied.
 *
 * @author Serge Rosmorduc
 */
public class MDCModelTransferable implements Transferable {

    private final TopItemList topItemList;

    private RTFExportPreferences rtfPreferences;

    private JSeshRenderContext jseshRenderContext;

    private final int maxBitmapWidth;
    private final int maxBitmapHeight;

    private final DataFlavor dataFlavors[];

    /**
     * Create a MDCModelTransferable.
     * Note that there are a lot of parameters.
     * We need
     * 
     * @param dataFlavors
     * @param list
     * @param mdcClipboardPreferences
     * @param rtfExportPreferences
     * @param renderContext
     */
    public MDCModelTransferable(DataFlavor[] dataFlavors, TopItemList list,
            RTFExportPreferences rtfExportPreferences,
            JSeshRenderContext renderContext) {
        this.topItemList = list;
        maxBitmapWidth = 2000;
        maxBitmapHeight = 2000;
        this.rtfPreferences = rtfExportPreferences;
        this.jseshRenderContext = renderContext;
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
        JSeshRenderContext marginLessContext = createEmbeddedRenderContext();
        EmbeddableEMFSimpleDrawer drawer = new EmbeddableEMFSimpleDrawer(marginLessContext,
                rtfPreferences.cadratHeight(),
                topItemList.toMdC());
        drawer.drawTopItemList(topItemList);
        return new ByteArrayInputStream(drawer.getBytes());
    }

    /**
     * Get pdf as binary content for the clipboard.
     *
     * @return the pdf
     * @throws IOException
     */
    private ByteArrayInputStream getPDFData() throws IOException {
       
        // Target Cadrat height, in points.
        float targetHeight = rtfPreferences.cadratHeight();
        float maxCadratHeight = jseshRenderContext.jseshStyle().geometry().maxCadratHeight();        
        float scale = targetHeight / maxCadratHeight;
        PDFDataSaver pdfDataSaver = new PDFDataSaver(jseshRenderContext);
        pdfDataSaver.setScale(scale);
        return pdfDataSaver.createPDFContent(topItemList);
    }

    /**
     * @return
     */
    private ByteArrayInputStream getMacPictData() {
        MacPictGraphics2D g = new MacPictGraphics2D();
        JSeshTechRenderContext tmp = JSeshTechRenderContext.buildSimpleContext(g);
        JSeshRenderContext marginLessRenderContext = createEmbeddedRenderContext();
        MDCView view = new ViewBuilder().buildView(topItemList, marginLessRenderContext, tmp);        
        new ViewDrawer().draw(g, marginLessRenderContext, tmp, view);
        g.dispose();
        return new ByteArrayInputStream(g.getAsArray());
    }

    /**
     * @return
     */
    private BufferedImage getImageData() {
        BufferedImage result;
        MDCDrawingFacade facade = new MDCDrawingFacade(createEmbeddedRenderContext());
        facade.setMaxSize(maxBitmapWidth, maxBitmapHeight);
        facade.setCadratHeight(rtfPreferences.cadratHeight());
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
    private ByteArrayInputStream getRtfData() throws IOException {
        ByteArrayInputStream result;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        if (topItemList.getNumberOfChildren() < 5000) {
            RTFExporter rtfExporter = new RTFExporter(jseshRenderContext, rtfPreferences);            
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
     * Adapt the render context, mainly to remove margins.
     * @return
     */
    private JSeshRenderContext createEmbeddedRenderContext() {
        return this.jseshRenderContext.marginLessContext();
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

}

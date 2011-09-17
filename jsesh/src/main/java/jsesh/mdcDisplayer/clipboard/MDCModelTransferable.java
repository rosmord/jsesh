/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.Dimension2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import jsesh.graphics.export.RTFExportPreferences;
import jsesh.graphics.export.RTFSimpleExporter;
import jsesh.graphics.export.pdfExport.PDFDataSaver;
import jsesh.mdc.model.ListOfTopItems;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.output.MdCModelWriter;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;
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
 * 
 */
public class MDCModelTransferable implements Transferable {

	private TopItemList topItemList;

	private RTFExportPreferences rtfPreferences;

	private DrawingSpecification drawingSpecifications;

	private MDCClipboardPreferences clipboardPreferences = new MDCClipboardPreferences();

	private int maxBitmapWidth;
	private int maxBitmapHeight;

	private DataFlavor dataFlavors[];

	public MDCModelTransferable(DataFlavor[] dataFlavors, TopItemList list) {
		this.topItemList = list;
		maxBitmapWidth = 2000;
		maxBitmapHeight = 2000;
		rtfPreferences = new RTFExportPreferences();
		this.drawingSpecifications = new DrawingSpecificationsImplementation();
		this.dataFlavors = dataFlavors;
	}

	public DataFlavor[] getTransferDataFlavors() {
		return dataFlavors;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.
	 * datatransfer.DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		DataFlavor[] flavors = getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(flavor)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer
	 * .DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		Object result;
		// Temporiser pour tester...

		if (JSeshPasteFlavors.RTFFlavor.equals(flavor))
			result = getRtfData();
		else if (DataFlavor.stringFlavor.equals(flavor))
			return getStringData();
		else if (JSeshPasteFlavors.ListOfTopItemsFlavor.equals(flavor))
			return getItemListData();
		else if (DataFlavor.imageFlavor.equals(flavor))
			result = getImageData();
		else if (JSeshPasteFlavors.MACPictFlavor.equals(flavor))
			result = getMacPictData();
		else if (JSeshPasteFlavors.PDFFlavor.equals(flavor))
			result = getPDFData();
		else if (EMFTransferable.EMF_FLAVOR.equals(flavor))
			result = getEMFData();
		else {
			throw new UnsupportedFlavorException(flavor);
		}
		return result;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private Object getEMFData() throws IOException {
		Object result;
		RandomAccessByteArray out1 = new RandomAccessByteArray();
		ViewBuilder simpleBuilder = new SimpleViewBuilder();
		MDCView view = simpleBuilder.buildView(topItemList,
				getDrawingSpecifications());
		ViewDrawer drawer = new ViewDrawer();
		Dimension2D dims = new DoubleDimensions(view.getWidth(),
				view.getHeight());
		EMFGraphics2D g = new EMFGraphics2D(out1, dims, "JSesh",
				topItemList.toMdC());
		drawer.draw(g, view, getDrawingSpecifications());
		g.dispose();
		result = new ByteArrayInputStream(out1.getByteArray());
		return result;
	}

	/**
	 * @return
	 * @throws IOException
	 */
	private Object getPDFData() throws IOException {
		Object result;
		{
			DrawingSpecification drawingSpecification = getDrawingSpecifications()
					.copy();
			// Cadrat height, in points.
			int cadratHeight = rtfPreferences.getCadratHeight();
			float ratio = drawingSpecification.getMaxCadratWidth()
					/ drawingSpecification.getMaxCadratHeight();
			// Change the pdf data accordingly:
			PageLayout pageLayout = drawingSpecification.getPageLayout();
			pageLayout.setTopMargin(1);
			pageLayout.setLeftMargin(1);
			drawingSpecification.setPageLayout(pageLayout);

			drawingSpecification.setStandardSignHeight(cadratHeight);

			drawingSpecification.setMaxCadratHeight(cadratHeight);
			drawingSpecification.setMaxCadratWidth(cadratHeight * ratio);
			PDFDataSaver pdfDataSaver = new PDFDataSaver(drawingSpecification);
			result = pdfDataSaver.getPDFContent(topItemList);
		}
		return result;
	}

	/**
	 * @return
	 */
	private Object getMacPictData() {
		Object result;
		{
			// A test for Mac Picts (normal system is PDF now)
			MacPictGraphics2D g = new MacPictGraphics2D();
			ViewBuilder simpleBuilder = new SimpleViewBuilder();
			MDCView view = simpleBuilder.buildView(topItemList,
					getDrawingSpecifications());
			ViewDrawer drawer = new ViewDrawer();
			drawer.draw(g, view, getDrawingSpecifications());
			result = new ByteArrayInputStream(g.getAsArray());
		}
		return result;
	}

	/**
	 * @return
	 */
	private Object getImageData() {
		Object result;
		{
			MDCDrawingFacade facade = new MDCDrawingFacade();
			facade.setDrawingSpecifications(getDrawingSpecifications());
			facade.setMaxSize(maxBitmapWidth, maxBitmapHeight);
			facade.setCadratHeight(rtfPreferences.getCadratHeight());
			result = facade.createImage(topItemList);
		}
		return result;
	}

	/**
	 * @return
	 */
	private Object getItemListData() {
		{
			ListOfTopItems l = new ListOfTopItems();
			for (int i = 0; i < topItemList.getNumberOfChildren(); i++) {
				l.add(topItemList.getTopItemAt(i).deepCopy());
			}
			return l;
		}
	}

	/**
	 * @return
	 */
	private Object getStringData() {
		{
			StringWriter writer = new StringWriter();

			MdCModelWriter mdCModelWriter = new MdCModelWriter();
			mdCModelWriter.write(writer, topItemList);
			return writer.toString();
		}
	}

	/**
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private Object getRtfData() throws IOException,
			UnsupportedEncodingException {
		Object result;
		{
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			// FIXME : WHEN text is too large, RTF cut and paste fails because
			// of timing issues.
			if (topItemList.getNumberOfChildren() < 5000) {
				DrawingSpecification drawingSpecifications = getDrawingSpecifications();
				PageLayout pageLayout = drawingSpecifications.getPageLayout();
				pageLayout.setTopMargin(0);
				pageLayout.setLeftMargin(0);
				drawingSpecifications.setPageLayout(pageLayout);
				RTFSimpleExporter rtfExporter = new RTFSimpleExporter();
				rtfExporter.setDrawingSpecifications(drawingSpecifications);
				rtfExporter.setViewBuilder(new SimpleViewBuilder());
				rtfExporter.setRtfPreferences(rtfPreferences);
				rtfExporter.ExportModelTo(topItemList, outputStream);
				result = new ByteArrayInputStream(outputStream.toByteArray());
			} else {
				String data = "{\\rtf1\\ansi\\ansicpg1252Jsesh : sorry, data too large for RTF cut and paste. Please copy a smaller part of your text.\\par}";
				result = new ByteArrayInputStream(data.getBytes("US-ASCII"));
			}
		}
		return result;
	}

	/**
	 * @return Returns the drawingSpecifications.
	 */
	public DrawingSpecification getDrawingSpecifications() {
		return drawingSpecifications;
	}

	/**
	 * @param drawingSpecifications
	 *            The drawingSpecifications to set.
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
	 * @param rtfPreferences
	 *            The rtfPreferences to set.
	 */
	public void setRtfPreferences(RTFExportPreferences rtfPreferences) {
		this.rtfPreferences = rtfPreferences;
	}

	/*
	 * public void setPdfExportPreferences( PDFExportPreferences
	 * pdfExportPreferences) { this.pdfExportPreferences = pdfExportPreferences;
	 * }
	 * 
	 * public PDFExportPreferences getPdfExportPreferences() { return
	 * pdfExportPreferences; }
	 */

	public void setClipboardPreferences(
			MDCClipboardPreferences clipboardPreferences) {
		this.clipboardPreferences = clipboardPreferences;
	}

	public MDCClipboardPreferences getClipboardPreferences() {
		return clipboardPreferences;
	}

}

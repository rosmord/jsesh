/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;

import jsesh.graphicExport.RTFExportPreferences;
import jsesh.graphicExport.RTFExporter;
import jsesh.mdc.model.ListOfTopItems;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.output.ModelWriter;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;

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

	private static final DataFlavor RTFFlavor = new DataFlavor("text/rtf", null);

	public static final DataFlavor ListOfTopItemsFlavor = new DataFlavor(
			ListOfTopItems.class, null);

	// private DataFlavor[] flavors= {TopItemListFlavor, RTFFlavor,
	// DataFlavor.imageFlavor, DataFlavor.stringFlavor};
	// private DataFlavor[] flavors= {TopItemListFlavor, RTFFlavor,
	// DataFlavor.imageFlavor};

	private TopItemList topItemList;

	private RTFExportPreferences rtfPreferences;

	private DrawingSpecifications drawingSpecifications;

	private boolean bitmapWanted;

	private int maxBitmapWidth;

	private int maxBitmapHeight;

	public MDCModelTransferable(TopItemList list) {
		topItemList = (TopItemList) list;
		bitmapWanted = false;
		maxBitmapWidth = 2000;
		maxBitmapHeight = 2000;
		rtfPreferences = new RTFExportPreferences();
		this.drawingSpecifications = new DrawingSpecifications();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] result;

		result = new DataFlavor[] { ListOfTopItemsFlavor, RTFFlavor,
				DataFlavor.stringFlavor, ListOfTopItemsFlavor };

		if (bitmapWanted) {
			result = new DataFlavor[] { ListOfTopItemsFlavor, RTFFlavor,
					DataFlavor.imageFlavor, DataFlavor.stringFlavor };
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		System.err.println("calling isDFS " + flavor);
		DataFlavor[] flavors = getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++)
			if (flavors[i].equals(flavor))
				return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		Object result;
		System.out.println("asking for " + flavor);
		// Temporiser pour tester...

		if (RTFFlavor.equals(flavor)) {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			// FIXME : WHEN text is too large, RTF cut and paste fails because
			// of timing issues.
			if (topItemList.getNumberOfChildren() < 1000) {
				DrawingSpecifications drawingSpecifications = getDrawingSpecifications();
				drawingSpecifications.setTopMargin(0);
				drawingSpecifications.setLeftMargin(0);
				RTFExporter rtfExporter = new RTFExporter();
				rtfExporter.setDrawingSpecifications(drawingSpecifications);
				rtfExporter.setViewBuilder(new SimpleViewBuilder());
				rtfExporter.setRtfPreferences(rtfPreferences);
				rtfExporter.ExportModelTo(topItemList, outputStream);
				result = new ByteArrayInputStream(outputStream.toByteArray());
			} else {
				String data="{\\rtf1\\ansi\\ansicpg1252Jsesh : sorry, data too large for RTF cut and paste. Please copy a smaller part of your text.\\par}";
				result = new ByteArrayInputStream(data.getBytes("US-ASCII"));
			}
		} else if (DataFlavor.stringFlavor.equals(flavor)) {
			StringWriter writer = new StringWriter();
			ModelWriter modelWriter = new ModelWriter();
			modelWriter.write(writer, topItemList);
			return writer.toString();
		} else if (ListOfTopItemsFlavor.equals(flavor)) {
			ListOfTopItems l = new ListOfTopItems();
			for (int i = 0; i < topItemList.getNumberOfChildren(); i++)
				l.add(topItemList.getTopItemAt(i).deepCopy());
			System.err.println("copy made.");
			return l;
		} else if (DataFlavor.imageFlavor.equals(flavor)) {

			MDCDrawingFacade facade = new MDCDrawingFacade();
			facade.setDrawingSpecifications(getDrawingSpecifications());
			facade.setMaxSize(maxBitmapWidth, maxBitmapHeight);
			facade.setCadratHeight(rtfPreferences.getCadratHeight());
			result = facade.createImage(topItemList);

		} else {
			throw new UnsupportedFlavorException(flavor);
		}
		return result;
	}

	/**
	 * @return Returns the drawingSpecifications.
	 */
	public DrawingSpecifications getDrawingSpecifications() {
		return drawingSpecifications;
	}

	/**
	 * @param drawingSpecifications
	 *            The drawingSpecifications to set.
	 */
	public void setDrawingSpecifications(
			DrawingSpecifications drawingSpecifications) {
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

	/**
	 * Do we want bitmap pictures to be available ?
	 * 
	 * @return Returns the bitmapWanted.
	 */
	public boolean isBitmapWanted() {
		return bitmapWanted;
	}

	/**
	 * Chose if we want bitmap pictures.
	 * 
	 * @param bitmapWanted
	 *            The bitmapWanted to set.
	 */
	public void setBitmapWanted(boolean bitmapWanted) {
		this.bitmapWanted = bitmapWanted;
	}

}

package org.qenherkhopeshef.graphics.vectorClipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 
 * PDF export. Needs IText to work.
 *
 * @author rosmord
 *
 */
public class PDFTransferable implements Transferable {
	

	public static final DataFlavor PDF_FLAVOR = new DataFlavor(
			"application/pdf", null);

	private static final DataFlavor[] flavors = { PDF_FLAVOR };

	/**
	 * The picture which will be drawn by this transferable.
	 */
	private PDFPicture pdfPicture;

	public PDFTransferable(PDFPicture pdfPicture) {
		this.pdfPicture= pdfPicture;
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (PDF_FLAVOR.equals(flavor)) {
			return new ByteArrayInputStream(pdfPicture.getByteArray());
		} else {
			throw new UnsupportedFlavorException(flavor);
		}
	}

	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		DataFlavor[] flavors = getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(flavor)) {
				return true;
			}
		}
		return false;
	}

}

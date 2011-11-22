package org.qenherkhopeshef.graphics.vectorClipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class RTFTransferable implements Transferable {

	public static final DataFlavor RTF_FLAVOR = new DataFlavor("text/rtf", null);

	private static final DataFlavor[] flavors = { RTF_FLAVOR };

	private RTFPicture rtfPicture;

	public RTFTransferable(RTFPicture rtfPicture) {
		this.rtfPicture = rtfPicture;
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (RTF_FLAVOR.equals(flavor)) {
			return new ByteArrayInputStream(RTFPictureUtils
					.createRTFByteArray(rtfPicture));
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

package org.qenherkhopeshef.graphics.vectorClipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Experimental attempt at direct pict transfer.
 * If it works, this file will be unified with EMFTransferable.
 * 
 * Nearly works.
 * @author rosmord
 *
 */
public class PICTTransferable implements Transferable {

//	private static final String PICTATOM = "PICT";
	private static final String PICTATOM = "com.apple.pict";

	
	public static final DataFlavor PICT_FLAVOR = new DataFlavor("image/pict",
			"Mac Pict file");

	private static final DataFlavor[] flavors = { PICT_FLAVOR };


	static {
		((SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap())
				.addUnencodedNativeForFlavor(PICT_FLAVOR, PICTATOM);
	}

	private MACPictPicture picture;

	public PICTTransferable(MACPictPicture currentPicture) {
		this.picture = currentPicture;
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (PICT_FLAVOR.equals(flavor)) {
			//return new ByteArrayInputStream(picture.getAsArrayForRTF());
			return new ByteArrayInputStream(picture.getAsByteArray());
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

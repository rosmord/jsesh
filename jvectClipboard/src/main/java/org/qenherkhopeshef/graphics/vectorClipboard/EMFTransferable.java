package org.qenherkhopeshef.graphics.vectorClipboard;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.SystemFlavorMap;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class EMFTransferable implements Transferable {

	private static final String EMFATOM = "ENHMETAFILE";

	public static final DataFlavor EMF_FLAVOR = new DataFlavor("image/emf",
			"Enhanced MetaFile");

	private static final DataFlavor[] flavors = { EMF_FLAVOR };


	static {
		((SystemFlavorMap) SystemFlavorMap.getDefaultFlavorMap())
				.addUnencodedNativeForFlavor(EMF_FLAVOR, EMFATOM);
	}

	private EMFPicture picture;

	public EMFTransferable(EMFPicture picture) {
		this.picture = picture;
	}

	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (EMF_FLAVOR.equals(flavor)) {
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

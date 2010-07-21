package jsesh.swingUtils;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * A base for a simple transfertHandler which will receive only strings.
 * @author rosmord
 *
 */
public abstract class SimpleStringTransfertHandler extends TransferHandler {
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		for (int i = 0; i < transferFlavors.length; i++) {
			if (DataFlavor.stringFlavor.equals(transferFlavors[i]))
				return true;
		}
		return false;
	}

	public int getSourceActions(JComponent c) {
		return TransferHandler.NONE;
	}
	
	public String getString(Transferable t) {
		try {
			String val = (String) t
					.getTransferData(DataFlavor.stringFlavor);
			return val;
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}

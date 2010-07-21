package jsesh.utilitySoftwares.signInfoEditor.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

/**
 * TransferHandler attached to JTable.
 * @author rosmord
 *
 */
public class SignCodeToTableTransfertHandler extends TransferHandler {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9218613618596473011L;
	
	private SignPropertyTableModel signVariantTableModel;

	public SignCodeToTableTransfertHandler(SignPropertyTableModel signVariantTableModel) {
		this.signVariantTableModel= signVariantTableModel;
	}

	public boolean importData(JComponent comp, Transferable t) {
		try {
			String val= (String) t.getTransferData(DataFlavor.stringFlavor);
			JTable jTable= (JTable) comp;
			int row= jTable.getSelectedRow();
			if (row == -1)
				return false;
			signVariantTableModel.setValueAt(val, row, 0);
			return true;
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		for (int i=0; i < transferFlavors.length; i++) {
			if (DataFlavor.stringFlavor.equals(transferFlavors[i]))
				return true;
		}
		return false;
	}
	
	public int getSourceActions(JComponent c) {
		return TransferHandler.NONE;
	}
}

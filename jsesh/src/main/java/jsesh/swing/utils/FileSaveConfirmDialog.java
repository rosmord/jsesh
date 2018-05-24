package jsesh.swing.utils;

import java.awt.Component;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

import jsesh.i18n.I18n;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;

public class FileSaveConfirmDialog {

    /**
     * Asks if a file must really be saved (if there is already an existing one).
     * @param parentComponent
     * @param fileToSave
     * @return a FileOperationResult, OK or CANCEL.
     */
	public static FileOperationResult showDialog(Component parentComponent, File fileToSave) {
		String message = I18n.getString("FileSaveConfirmDialog.text");
		Object[] args = { fileToSave.getName() };
		String formattedMessage = MessageFormat.format(message, args);                
		int res= JOptionPane.showConfirmDialog(parentComponent, formattedMessage,
				I18n.getString("FileSaveConfirm.title"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (res == JOptionPane.OK_OPTION)
                    return FileOperationResult.OK;
                else
                    return FileOperationResult.CANCEL;
	}

	public static void showCantOpenDialog(Component frame) {
		JOptionPane.showMessageDialog(frame,
				I18n.getString("FileSaveConfirmDialog.cantOpen"),
				I18n.getString("FileSaveConfirmDialog.error"),
				JOptionPane.ERROR_MESSAGE);
	}

}

package jsesh.swing.utils;

import java.awt.Component;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

import jsesh.i18n.I18n;

public class FileSaveConfirmDialog {

	public static int showDialog(Component parentComponent, File fileToSave) {
		String message = I18n.getString("FileSaveConfirmDialog.text");
		Object[] args = { fileToSave.getName() };
		String formattedMessage = MessageFormat.format(message, args);
		return JOptionPane.showConfirmDialog(parentComponent, formattedMessage,
				I18n.getString("FileSaveConfirm.title"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

	}

	public static void showCantOpenDialog(Component frame) {
		JOptionPane.showMessageDialog(frame,
				I18n.getString("FileSaveConfirmDialog.cantOpen"),
				I18n.getString("FileSaveConfirmDialog.error"),
				JOptionPane.ERROR_MESSAGE);
	}

}

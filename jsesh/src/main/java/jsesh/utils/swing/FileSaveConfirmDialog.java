/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.utils.swing;

import java.awt.Component;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

import jsesh.resources.JSeshMessages;

import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;

public class FileSaveConfirmDialog {

    /**
     * Asks if a file must really be saved (if there is already an existing one).
     * @param parentComponent
     * @param fileToSave
     * @return a FileOperationResult, OK or CANCEL.
     */
	public static FileOperationResult showDialog(Component parentComponent, File fileToSave) {
		String message = JSeshMessages.getString("FileSaveConfirmDialog.text");
		Object[] args = { fileToSave.getName() };
		String formattedMessage = MessageFormat.format(message, args);                
		int res= JOptionPane.showConfirmDialog(parentComponent, formattedMessage,
				JSeshMessages.getString("FileSaveConfirmDialog.title"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (res == JOptionPane.OK_OPTION)
                    return FileOperationResult.OK;
                else
                    return FileOperationResult.CANCEL;
	}

	public static void showCantOpenDialog(Component frame) {
		JOptionPane.showMessageDialog(frame,
				JSeshMessages.getString("FileSaveConfirmDialog.cantOpen"),
				JSeshMessages.getString("FileSaveConfirmDialog.error"),
				JOptionPane.ERROR_MESSAGE);
	}

}

/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */

package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import org.jhotdraw_7_6.app.Application;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.AbstractCoreApplicationAction;
import jsesh.jhotdraw.utils.WindowsHelper;
import jsesh.jseshtexts.JSeshTextLibraryExporter;
import jsesh.resources.JSeshMessages;

/**
 * Let the user choose a directory, and unzip the text library in this directory.
 * 
 * To avoid accidents, makes sure the folder exists and does not contain a directory called jsesh-text.
 */
public class ExportTextLibraryAction extends AbstractCoreApplicationAction{
	public static final String ID = "file.exportTextLibrary";

	public ExportTextLibraryAction(Application app) {
		super(app);
		BundleHelper.getInstance().configureActionWithID(this, ID);
	}

    @Override
    public void actionPerformed(ActionEvent e) {        
        PortableFileDialog dialog = PortableFileDialogFactory
				.createDirectorySaveDialog(WindowsHelper.getRootFrame(
						getApplication(), getApplication().getActiveView()));
		dialog.setTitle(JSeshMessages.getString("file.exportTextLibrary.title"));
		dialog.setCurrentDirectory(appCore().getCurrentDirectory());
		FileOperationResult op = dialog.show();
		if (op == FileOperationResult.OK) {
			File destinationFolder = dialog.getSelectedFile();
            if (isOk(destinationFolder)) {
                JSeshTextLibraryExporter.exportTexts(destinationFolder.toPath());
                String okMessage = JSeshMessages.format("file.exportTextLibrary.done", destinationFolder.getAbsolutePath());
                JOptionPane.showMessageDialog(null, okMessage, "", javax.swing.JOptionPane.INFORMATION_MESSAGE);

            } else {
                String message = JSeshMessages.getString("file.exportTextLibrary.badDestinationMessage");
                String messageTitle = JSeshMessages.getString("file.exportTextLibrary.badDestinationMessageTitle");
                JOptionPane.showMessageDialog(null, message, messageTitle, javax.swing.JOptionPane.WARNING_MESSAGE);
            }
		}
    }

    private boolean isOk(File destinationFolder) {
        if (!destinationFolder.exists()) {
            return false;
        } else if (!destinationFolder.isDirectory()) {
            return false;
        } else if (!destinationFolder.canWrite()) {
            return false;
        } else {
            File jseshTextFolder = new File(destinationFolder, "jsesh-texts");
            if (jseshTextFolder.exists()) {
                return false;
            } else
                return true;
        }
    }
    
}

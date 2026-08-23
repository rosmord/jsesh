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

import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.WindowsHelper;
import jsesh.resources.JSeshMessages;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.action.AbstractApplicationAction;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialog;
import org.qenherkhopeshef.swingUtils.portableFileDialog.PortableFileDialogFactory;

@SuppressWarnings("serial")
public class QuickPDFSelectExportFolderAction extends AbstractApplicationAction {
	public static final String ID = "file.selectQuickPDFExportFolder";

	public QuickPDFSelectExportFolderAction(Application app) {
		super(app);
		BundleHelper.getInstance().configureActionWithID(this, ID);
	}

	public void actionPerformed(ActionEvent e) {
		BundleHelper bundleHelper= BundleHelper.getInstance();
		JSeshApplicationModel model = (JSeshApplicationModel) getApplication()
				.getModel();

		PortableFileDialog dialog = PortableFileDialogFactory
				.createDirectorySaveDialog(WindowsHelper.getRootFrame(
						getApplication(), getApplication().getActiveView()));
		dialog.setTitle(JSeshMessages.getString("file.quickPDFExportFolder.title"));
		dialog.setCurrentDirectory(model.getQuickPDFExportFolder());
		FileOperationResult op = dialog.show();
		if (op == FileOperationResult.OK) {
			File quickPDFExportDirectory = dialog.getSelectedFile();
			model.setQuickPDFExportFolder(quickPDFExportDirectory);
			model.setMessage(bundleHelper.getFormatedLabel("file.quickPDFExportFolder.ok", quickPDFExportDirectory.getAbsolutePath()));
		}
	}

}

package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;

import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.WindowsHelper;

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
		BundleHelper.getInstance().configure(this);
	}

	public void actionPerformed(ActionEvent e) {
		BundleHelper bundleHelper= BundleHelper.getInstance();
		JSeshApplicationModel model = (JSeshApplicationModel) getApplication()
				.getModel();

		PortableFileDialog dialog = PortableFileDialogFactory
				.createDirectorySaveDialog(WindowsHelper.getRootFrame(
						getApplication(), null));
		dialog.setTitle(Messages.getString("file.quickPDFExportFolder.title"));
		dialog.setCurrentDirectory(model.getQuickPDFExportFolder());
		FileOperationResult op = dialog.show();
		if (op == FileOperationResult.OK) {
			File quickPDFExportDirectory = dialog.getSelectedFile();
			model.setQuickPDFExportFolder(quickPDFExportDirectory);
			model.setMessage(bundleHelper.getFormatedLabel("file.quickPDFExportFolder.ok", quickPDFExportDirectory.getAbsolutePath()));
		}
	}

}

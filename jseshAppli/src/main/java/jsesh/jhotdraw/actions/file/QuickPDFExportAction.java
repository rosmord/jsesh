package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.qenherkhopeshef.swingUtils.errorHandler.UserMessage;

import jsesh.graphics.export.pdfExport.PDFExportPreferences;
import jsesh.graphics.export.pdfExport.PDFExporter;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.documentview.JSeshViewCore;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;

@SuppressWarnings("serial")
public class QuickPDFExportAction extends AbstractCoreViewAction {

	public static final String ID = "file.quickPDFExport";

	public QuickPDFExportAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		viewCore().ifPresent(v -> quickPdfExport(v));
	}

	public void quickPdfExport(JSeshViewCore jSeshView) {
		BundleHelper bundleHelper = BundleHelper.getInstance();
		
		// Ensures the folder exists :
		if (!appCore().getQuickPDFExportFolder().exists()) {
			appCore().getQuickPDFExportFolder().mkdir();
		}
		// Check if it is correct.
		if (!(appCore().getQuickPDFExportFolder().exists() && appCore()
				.getQuickPDFExportFolder().isDirectory())) {
			String msg = bundleHelper.getFormatedLabel(
					"file.quickPDFExport.error", appCore()
							.getQuickPDFExportFolder().getAbsolutePath());
			JOptionPane.showMessageDialog(null, msg,
					bundleHelper.getLabel("file.quickPDFExport.errorTitle"),
					JOptionPane.ERROR_MESSAGE);
			return;

		}
		// Prepare exportation and compute file name.
		PDFExportPreferences quickExportPreferences = new PDFExportPreferences();

		PDFExporter pdfExporter = new PDFExporter();
		quickExportPreferences.setJseshStyle(jSeshView.getJSeshStyle());

		// Find the next file name...
		int maxNum = 0;

		for (File f : appCore().getQuickPDFExportFolder().listFiles()) {
			String fname = f.getName();
			// File names : jsesh + number + .pdf
			if (fname.matches("jsesh[0-9]*\\.pdf")) {
				String numString = fname.substring(5, fname.lastIndexOf('.'));
				try {
					int num = Integer.parseInt(numString);
					if (num > maxNum)
						maxNum = num;
				} catch (NumberFormatException numberFormatException) {
					// DO NOTHING ? DON'T STOP PROCESSING, but WARN JUST IN
					// CASE.
					throw new UserMessage(numberFormatException);
				}
			}
		}
		String numAsString = String.format("%06d", maxNum + 1);

		File pdfFile = new File(appCore().getQuickPDFExportFolder(),
				"jsesh" + numAsString + ".pdf");
		quickExportPreferences.setFile(pdfFile);
		quickExportPreferences.setEncapsulated(true);

		pdfExporter.setPdfExportPreferences(quickExportPreferences);

		// Ensure there is a selection
		if (!jSeshView.hasSelection()) {
			jSeshView.selectCurrentLine();
		}
		try {
			pdfExporter.exportModel(jSeshView.getTopItemList(),
					jSeshView.getCaret(), jSeshView.getRenderContext());
			String okMessage = MessageFormat.format(
					bundleHelper.getLabel("file.quickPDFExport.ok"),
					new Object[] { pdfFile.getAbsolutePath() });
			jSeshView.setMessage(okMessage);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(
					referenceComponent(),
					bundleHelper.getFormatedLabel("exportAsPdf.error",
							e1.getMessage()),
					bundleHelper.getLabel("exportAsPdf.errorTitle"),
					JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}
}

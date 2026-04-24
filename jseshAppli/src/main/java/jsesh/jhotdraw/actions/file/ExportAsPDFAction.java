package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

import jsesh.graphics.export.pdfExport.PDFExportPreferences;
import jsesh.graphics.export.pdfExport.PDFExporter;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.documentview.JSeshViewCore;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;
import jsesh.mdcDisplayer.context.JSeshRenderContext;
import jsesh.resources.JSeshMessages;

@SuppressWarnings("serial")
public class ExportAsPDFAction extends AbstractCoreViewAction {
	public static final String ID = "file.export.pdf";

	public ExportAsPDFAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		viewCore().ifPresent(v -> exportAsPDF(v));
	}

	private void exportAsPDF(JSeshViewCore v) {
		PDFExportPreferences pdfExportPreferences = appCore()
				.getPDFExportPreferences();
		PDFExporter pdfExporter = new PDFExporter();
		pdfExporter.setPdfExportPreferences(pdfExportPreferences);
		pdfExportPreferences.setJseshStyle(v
				.getJSeshStyle());

		// WE SHOULD REMOVE FILE FROM THE PDF PREFERENCES. MEANWHILE...
		// (WHILE WE ARE THERE, WE SHOULD PROBABLY DELEGATE NEW FILES NAMES CREATION TO
		// SOME CLASS).
		pdfExportPreferences.setFile(createDefaultExportFile("pdf"));
		if (pdfExporter.getOptionPanel(referenceComponent(), "Export as PDF")
				.askAndSet() == JOptionPane.OK_OPTION) {
			try {
				pdfExporter.exportModel(v.getTopItemList(),
						v.getCaret(), v.getRenderContext());
				appCore().setCurrentDirectory(pdfExportPreferences
						.getFile().getParentFile());
			} catch (IOException e1) {
				String message = JSeshMessages.getString("exportAsPdf.error");
				MessageFormat.format(message,
						new Object[] { e1.getMessage() });
				String messageTitle = JSeshMessages
						.getString("exportAsPdf.errorTitle");
				JOptionPane.showMessageDialog(referenceComponent(), message,
						messageTitle, JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
}

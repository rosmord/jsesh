package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.MessageFormat;

import javax.swing.JOptionPane;

import jsesh.graphics.export.pdfExport.PDFExportPreferences;
import jsesh.graphics.export.pdfExport.PDFExporter;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.JSeshView;
import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractViewAction;

@SuppressWarnings("serial")
public class ExportAsPDFAction extends AbstractViewAction {
	public static final String ID = "file.export.pdf";

	public ExportAsPDFAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	public void actionPerformed(ActionEvent arg0) {
		JSeshView jSeshView = (JSeshView) getActiveView();
		JSeshApplicationModel applicationModel = (JSeshApplicationModel) getApplication()
				.getModel();
		if (jSeshView != null ) {
			PDFExportPreferences pdfExportPreferences = applicationModel
					.getPDFExportPreferences();			
			PDFExporter pdfExporter = new PDFExporter();			
			pdfExporter.setPdfExportPreferences(pdfExportPreferences);
			pdfExportPreferences.setDrawingSpecifications(jSeshView
					.getDrawingSpecifications());

			// WE SHOULD REMOVE FILE FROM THE PDF PREFERENCES. MEANWHILE...
			// (WHILE WE ARE THERE, WE SHOULD PROBABLY DELEGATE NEW FILES NAMES CREATION TO SOME CLASS). 
			pdfExportPreferences.setFile(jSeshView.buildDefaultExportFile("pdf"));
			if (pdfExporter.getOptionPanel(jSeshView, "Export as PDF")
					.askAndSet() == JOptionPane.OK_OPTION) {
				try {
					pdfExporter.exportModel(jSeshView.getTopItemList(),
							jSeshView.getCaret());
					applicationModel.setCurrentDirectory(pdfExportPreferences
							.getFile().getParentFile());
				} catch (IOException e1) {
					String message = Messages.getString("exportAsPdf.error");
					MessageFormat.format(message,
							new Object[] { e1.getMessage() });
					String messageTitle = Messages
							.getString("exportAsPdf.errorTitle");
					JOptionPane.showMessageDialog(jSeshView, message,
							messageTitle, JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		}
	}
}

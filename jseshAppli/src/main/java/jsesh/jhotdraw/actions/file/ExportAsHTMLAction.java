package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import jsesh.graphics.export.html.HTMLExporter;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.viewClass.JSeshView;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

@SuppressWarnings("serial")
public class ExportAsHTMLAction extends AbstractViewAction {

	public static final String ID = "file.exportHTML";

	public ExportAsHTMLAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	public void actionPerformed(ActionEvent arg0) {
		JSeshView jSeshView = (JSeshView) getActiveView();
		JSeshApplicationModel applicationModel = (JSeshApplicationModel) getApplication()
				.getModel();
		if (jSeshView != null) {
			HTMLExporter htmlExporter = applicationModel.getHTMLExporter();

			htmlExporter.setDirectory(new File(applicationModel
					.getCurrentDirectory(), jSeshView.getBaseFileName()));
			htmlExporter.setBaseName(jSeshView.getBaseFileName());
			if (htmlExporter.getOptionPanel(jSeshView, "Export as HTML")
					.askAndSet() == JOptionPane.OK_OPTION) {
				htmlExporter.setDrawingSpecifications(jSeshView
						.getDrawingSpecifications());
				htmlExporter.exportModel(jSeshView.getTopItemList());
			}
		}
	}
}

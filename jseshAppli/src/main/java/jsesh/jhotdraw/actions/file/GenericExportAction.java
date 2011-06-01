package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import jsesh.graphics.export.ExportData;
import jsesh.graphics.export.GraphicalExporter;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.JSeshView;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractViewAction;

@SuppressWarnings("serial")
public class GenericExportAction extends AbstractViewAction {
	private GraphicalExporter exporter;

	public GenericExportAction(Application app, View view,
			GraphicalExporter exporter, String actionID) {
		super(app, view);
		BundleHelper.getInstance().configure(this,actionID);
		this.exporter = exporter;
	}

	public void actionPerformed(ActionEvent event) {
		JSeshView jSeshView = (JSeshView) getActiveView();
		JSeshApplicationModel applicationModel= (JSeshApplicationModel) getApplication().getModel();
		if (jSeshView != null && jSeshView.hasSelection())  {
			exporter.setDirectory(applicationModel.getCurrentDirectory());
			exporter.setOriginalDocumentFile(jSeshView.getURI());
			if (exporter.askUser(getActiveView().getComponent()) == JOptionPane.OK_OPTION) {
				ExportData exportData = jSeshView.getExportData();
				exporter.export(exportData);
				applicationModel.setCurrentDirectory(exporter.getDirectory());
			}
		}
	}

}

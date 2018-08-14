package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import jsesh.graphics.export.rtf.RTFExporterPresenter;
import jsesh.jhotdraw.ExportType;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.viewClass.JSeshView;
import jsesh.jhotdraw.Messages;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

@SuppressWarnings("serial")
public class ExportAsRTFAction extends AbstractViewAction {
	public static final String ID = "file.export.rtf";

	public ExportAsRTFAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	public void actionPerformed(ActionEvent arg0) {
		JSeshView jSeshView = (JSeshView) getActiveView();
		JSeshApplicationModel applicationModel = (JSeshApplicationModel) getApplication()
				.getModel();
		if (jSeshView != null) {
			RTFExporterPresenter rtfExporterUI;
			File exportFile = jSeshView.buildDefaultExportFile("rtf");
			rtfExporterUI = new RTFExporterPresenter(exportFile,
					applicationModel.getRTFExportPreferences(ExportType.FILE));

			if (rtfExporterUI.getOptionPanel(jSeshView,
					Messages.getString("exportAsRTF.title")).askAndSet() == JOptionPane.OK_OPTION) {
				rtfExporterUI.exportModel(jSeshView.getDrawingSpecifications(),
						jSeshView.getTopItemList());
				applicationModel.setCurrentDirectory(rtfExporterUI.getFile()
						.getParentFile());
			}
		}
	}
}

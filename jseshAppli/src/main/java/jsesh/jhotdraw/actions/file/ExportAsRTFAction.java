package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import jsesh.graphics.export.rtf.RTFExporterPresenter;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.constants.ExportType;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.documentview.JSeshViewCore;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;
import jsesh.resources.JSeshMessages;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

// TODO : cleanup this class and improve the architecture.

@SuppressWarnings("serial")
public class ExportAsRTFAction extends AbstractCoreViewAction {
	public static final String ID = "file.export.rtf";

	public ExportAsRTFAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	public void actionPerformed(ActionEvent arg0) {
		
		JSeshView jSeshView = (JSeshView) getActiveView();
		JSeshApplicationModel applicationModel = (JSeshApplicationModel) app.getModel();•
		
		if (jSeshView != null) {
			 JSeshViewCore core = jSeshView.core();
			RTFExporterPresenter rtfExporterUI;
			File exportFile = jSeshView.buildDefaultExportFile("rtf");
			rtfExporterUI = new RTFExporterPresenter(exportFile,
					appCore().getRTFExportPreferences(ExportType.FILE));

			if (rtfExporterUI.getOptionPanel(getActiveView().getComponent(),
					JSeshMessages.getString("exportAsRTF.title")).askAndSet() == JOptionPane.OK_OPTION) {
				rtfExporterUI.exportModel(core.getRenderContext(),
						core.getTopItemList());
				applicationModel.setCurrentDirectory(rtfExporterUI.getFile()
						.getParentFile());
			}
		}
	}
}

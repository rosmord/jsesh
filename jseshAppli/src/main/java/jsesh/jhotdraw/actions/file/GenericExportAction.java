package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;


import jsesh.graphics.export.generic.ExportData;
import jsesh.graphics.export.generic.GraphicalExporter;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.viewClass.JSeshView;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;

@SuppressWarnings("serial")
public class GenericExportAction extends AbstractViewAction {

    private final GraphicalExporter exporter;

    public GenericExportAction(Application app, View view,
            GraphicalExporter exporter, String actionID) {
        super(app, view);
        BundleHelper.getInstance().configure(this, actionID);
        this.exporter = exporter;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        JSeshView jSeshView = (JSeshView) getActiveView();
        JSeshApplicationModel applicationModel = (JSeshApplicationModel) getApplication().getModel();
        if (jSeshView != null) {
            if (!jSeshView.hasSelection()) {
                jSeshView.selectCurrentPage();
            }
            exporter.setDirectory(applicationModel.getCurrentDirectory());
            exporter.setOriginalDocumentFile(jSeshView.getURI());
            if (exporter.askUser(getActiveView().getComponent()) == FileOperationResult.OK) {
                ExportData exportData = jSeshView.getExportData();
                exporter.export(exportData);
                applicationModel.setCurrentDirectory(exporter.getDirectory());
            }
        }
    }
}

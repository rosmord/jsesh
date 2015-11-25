package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import jsesh.graphics.export.ExportData;
import jsesh.graphics.export.GraphicalExporter;
import jsesh.i18n.I18n;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.JSeshView;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

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
            if (jSeshView.hasSelection()) {
                exporter.setDirectory(applicationModel.getCurrentDirectory());
                exporter.setOriginalDocumentFile(jSeshView.getURI());
                if (exporter.askUser(getActiveView().getComponent()) == JOptionPane.OK_OPTION) {
                    ExportData exportData = jSeshView.getExportData();
                    exporter.export(exportData);
                    applicationModel.setCurrentDirectory(exporter.getDirectory());
                }
            } else {
                // The message is stored in module JSesh. We should definitly move it here...
                String message= I18n.getString("Exporter.noSelection");
                JOptionPane.showMessageDialog(jSeshView, message,
							"", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}

/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.qenherkhopeshef.swingUtils.portableFileDialog.FileOperationResult;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.documentview.JSeshViewCore;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;
import jsesh.ui.export.generic.ExportData;
import jsesh.ui.export.generic.GraphicalExporter;

@SuppressWarnings("serial")
public class GenericExportAction extends AbstractCoreViewAction {

    private final GraphicalExporter exporter;

    public GenericExportAction(Application app, View view,
            GraphicalExporter exporter, String actionID) {
        super(app, view);
        BundleHelper.getInstance().configureActionWithID(this, actionID);
        this.exporter = exporter;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        viewCore().ifPresent(v -> genericExport(v));
    }

    private void genericExport(JSeshViewCore v) {
        if (!v.hasSelection()) {
                v.selectCurrentPage();
            }
            exporter.setDirectory(getCurrentDirectory());
            exporter.setOriginalDocumentFile(getURI());
            if (exporter.askUser(getActiveView().getComponent()) == FileOperationResult.OK) {
                ExportData exportData = v.createExportData(1.0f);
                exporter.export(exportData);
                setCurrentDirectory(exporter.getDirectory());
            }
    }
}

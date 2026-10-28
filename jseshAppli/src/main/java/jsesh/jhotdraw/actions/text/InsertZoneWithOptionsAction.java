/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.actions.text;

import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.dialogs.ZoneParametersPanel;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;
import jsesh.model.ZoneParameters;
import jsesh.ui.editor.JMDCEditor;

/// Asks for the settings of a new zone, and puts the selection in it (or inserts
/// an empty zone). The cursor ends up in the zone.
@SuppressWarnings("serial")
public class InsertZoneWithOptionsAction extends AbstractCoreViewAction {

    public static final String ID = "text.insertZoneWithOptions";

    public InsertZoneWithOptionsAction(Application app, View view) {
        super(app, view);
        BundleHelper.getInstance().configureActionNoIcon(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JSeshView view = (JSeshView) getActiveView();
        if (view == null) {
            return;
        }
        JMDCEditor editor = view.getEditor();
        ZoneParametersPanel panel = new ZoneParametersPanel(ZoneParameters.DEFAULT);
        int choice = JOptionPane.showConfirmDialog(editor, panel,
                BundleHelper.getInstance().getLabel("zone.dialog.insertTitle"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice == JOptionPane.OK_OPTION) {
            editor.getWorkflow().addZone(panel.getParameters());
        }
    }
}

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
import jsesh.jhotdraw.dialogs.RunningAnnotationParametersPanel;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;
import jsesh.jhotdraw.utils.JSimpleDialog;
import jsesh.sharedvocabulary.RunningAnnotationKind;
import jsesh.ui.editor.JMDCEditor;

/// Asks for the length of a running annotation (haplography or extended
/// lacuna), then inserts it at the caret. The length is "fill" by default.
@SuppressWarnings("serial")
public class InsertRunningAnnotationAction extends AbstractCoreViewAction {

    public static final String HAPLOGRAPHY_ID = "text.insertHaplography";
    public static final String LACUNA_ID = "text.insertLacuna";

    private final RunningAnnotationKind kind;
    private final String titleKey;

    private InsertRunningAnnotationAction(Application app, View view, String id, RunningAnnotationKind kind,
            String titleKey) {
        super(app, view);
        this.kind = kind;
        this.titleKey = titleKey;
        BundleHelper.getInstance().configureActionWithID(this, id);
    }

    /// @return an action inserting an haplography.
    public static InsertRunningAnnotationAction haplography(Application app, View view) {
        return new InsertRunningAnnotationAction(app, view, HAPLOGRAPHY_ID, RunningAnnotationKind.HAPLOGRAPHY,
                "runningAnnotation.dialog.haplographyTitle");
    }

    /// @return an action inserting an extended lacuna.
    public static InsertRunningAnnotationAction lacuna(Application app, View view) {
        return new InsertRunningAnnotationAction(app, view, LACUNA_ID, RunningAnnotationKind.LACUNA,
                "runningAnnotation.dialog.lacunaTitle");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JSeshView view = (JSeshView) getActiveView();
        if (view == null) {
            return;
        }
        JMDCEditor editor = view.getEditor();
        if (!editor.isEditable()) {
            return;
        }
        RunningAnnotationParametersPanel panel = new RunningAnnotationParametersPanel();
        JSimpleDialog dialog = new JSimpleDialog(editor, panel, BundleHelper.getInstance().getLabel(titleKey));
        int choice = dialog.show();
        dialog.dispose();
        if (choice == JOptionPane.OK_OPTION) {
            editor.getWorkflow().insertElement(panel.getAnnotation(kind));
        }
    }
}

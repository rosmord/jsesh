/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.actions.text;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;
import jsesh.ui.editor.JMDCEditorWorkflow;
import jsesh.ui.editor.JMDCEditorWorkflow.CartoucheFillState;

/// Makes the cartouches of the selection (or the cartouche around the cursor)
/// fill their tab cell, or not: `<-ra-ms->[fill]`.
///
/// Meant for a `JCheckBoxMenuItem`. The menu has to call [#refresh()] when it
/// is about to be shown: the state depends on the cursor.
@SuppressWarnings("serial")
public class ToggleCartoucheFillAction extends AbstractCoreViewAction {

    public static final String ID = "text.cartoucheFill";

    public ToggleCartoucheFillAction(Application app, View view) {
        super(app, view);
        BundleHelper.getInstance().configureActionWithID(this, ID);
        putValue(Action.SELECTED_KEY, false);
        setEnabled(false);
    }

    private JMDCEditorWorkflow workflow() {
        JSeshView view = (JSeshView) getActiveView();
        return view == null ? null : view.getEditor().getWorkflow();
    }

    /// Updates the state of the menu item: checked if all the cartouches
    /// concerned fill; disabled if there is no cartouche which can fill.
    public void refresh() {
        JMDCEditorWorkflow workflow = workflow();
        CartoucheFillState state = workflow == null ? CartoucheFillState.NONE : workflow.getCartoucheFillAtCaret();
        setEnabled(state != CartoucheFillState.NONE);
        putValue(Action.SELECTED_KEY, state == CartoucheFillState.ON);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JMDCEditorWorkflow workflow = workflow();
        if (workflow == null) {
            return;
        }
        // Mixed selections are made to fill.
        boolean fill = workflow.getCartoucheFillAtCaret() != CartoucheFillState.ON;
        workflow.setCartoucheFillAtCaret(fill);
        refresh();
    }
}

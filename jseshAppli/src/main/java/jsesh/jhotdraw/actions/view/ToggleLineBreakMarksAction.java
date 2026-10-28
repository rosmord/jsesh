/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.jhotdraw.actions.view;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.documentview.JSeshView;

/// Shows or hides the explicit line breaks (`!`) of a document window, as paragraph
/// marks in a word processor. Lines which are only wrapped get no mark.
///
/// The setting belongs to the window and is not saved; printing and exports never
/// show the marks. Meant for a `JCheckBoxMenuItem`.
@SuppressWarnings("serial")
public class ToggleLineBreakMarksAction extends AbstractViewAction {

    public static final String ID = "view.toggleLineBreakMarks";

    public ToggleLineBreakMarksAction(Application app, View view) {
        super(app, view);
        BundleHelper.getInstance().configureActionWithID(this, ID);
        putValue(Action.SELECTED_KEY, false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JSeshView view = (JSeshView) getActiveView();
        if (view != null) {
            boolean show = !view.getEditor().isShowLineBreakMarks();
            view.getEditor().setShowLineBreakMarks(show);
            putValue(Action.SELECTED_KEY, show);
        }
    }
}

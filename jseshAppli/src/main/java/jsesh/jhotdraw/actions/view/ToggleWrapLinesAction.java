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
import jsesh.utils.preferences.JSeshPreferencesRoot;

/// Wraps or unwraps the lines of a document window to the width of the window.
///
/// This is a view option: the document is not changed, and printing and exports
/// never wrap. The choice is remembered in the user's preferences, and applied to
/// new windows. Meant for a `JCheckBoxMenuItem`.
@SuppressWarnings("serial")
public class ToggleWrapLinesAction extends AbstractViewAction {

    public static final String ID = "view.toggleWrapLines";

    /// Name of the preference which stores the mode.
    public static final String PREFERENCE_KEY = "view.wrapLines";

    public ToggleWrapLinesAction(Application app, View view) {
        super(app, view);
        BundleHelper.getInstance().configureActionWithID(this, ID);
        boolean wrap = JSeshPreferencesRoot.getPreferences().getBoolean(PREFERENCE_KEY, false);
        if (view instanceof JSeshView jseshView) {
            jseshView.getEditor().setWrapToComponentWidth(wrap);
        }
        putValue(Action.SELECTED_KEY, wrap);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JSeshView view = (JSeshView) getActiveView();
        if (view != null) {
            boolean wrap = !view.getEditor().isWrapToComponentWidth();
            view.getEditor().setWrapToComponentWidth(wrap);
            putValue(Action.SELECTED_KEY, wrap);
            JSeshPreferencesRoot.getPreferences().putBoolean(PREFERENCE_KEY, wrap);
        }
    }
}

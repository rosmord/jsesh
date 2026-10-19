/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor.actions.move;

import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;

import javax.swing.Action;

import jsesh.model.CaretGranularity;
import jsesh.ui.editor.JMDCEditor;
import jsesh.ui.editor.actionsUtils.EditorAction;
import jsesh.utils.preferences.JSeshPreferencesRoot;

/// Switches the cursor between "top-level only" and "inside quadrats" modes.
///
/// The action is meant to be used with a check box menu item: it keeps its
/// [Action#SELECTED_KEY] property in sync with the editor. The choice is
/// remembered in the user's preferences, and applied to new editors.
///
/// @see CaretGranularity
@SuppressWarnings("serial")
public class ToggleInnerCursorAction extends EditorAction {

	/// Name of the preference which stores the mode.
	public static final String PREFERENCE_KEY = "caret.insideQuadrats";

	public ToggleInnerCursorAction(JMDCEditor editor) {
		super(editor);
		if (editor != null) {
			boolean inner = getPreferences().getBoolean(PREFERENCE_KEY, false);
			editor.getWorkflow().setCaretGranularity(inner ? CaretGranularity.INNER : CaretGranularity.QUADRAT);
			putValue(Action.SELECTED_KEY, inner);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		boolean inner = editor.getWorkflow().getCaretGranularity() == CaretGranularity.QUADRAT;
		editor.getWorkflow().setCaretGranularity(inner ? CaretGranularity.INNER : CaretGranularity.QUADRAT);
		putValue(Action.SELECTED_KEY, inner);
		getPreferences().putBoolean(PREFERENCE_KEY, inner);
	}

	private static Preferences getPreferences() {
		return JSeshPreferencesRoot.getPreferences();
	}
}

/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor.actions.move;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import jsesh.model.CaretGranularity;
import jsesh.ui.editor.JMDCEditor;
import jsesh.ui.editor.actionsUtils.EditorAction;

/// Switches the cursor between "top-level only" and "inside quadrats" modes.
///
/// The action is meant to be used with a check box menu item: it keeps its
/// [Action#SELECTED_KEY] property in sync with the editor. The choice is
/// deliberately *not* stored in the preferences: each editor starts in
/// "top-level only" mode.
///
/// @see CaretGranularity
@SuppressWarnings("serial")
public class ToggleInnerCursorAction extends EditorAction {

	public ToggleInnerCursorAction(JMDCEditor editor) {
		super(editor);
		if (editor != null) {
			editor.getWorkflow().setCaretGranularity(CaretGranularity.QUADRAT);
			putValue(Action.SELECTED_KEY, false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		boolean inner = editor.getWorkflow().getCaretGranularity() == CaretGranularity.QUADRAT;
		editor.getWorkflow().setCaretGranularity(inner ? CaretGranularity.INNER : CaretGranularity.QUADRAT);
		putValue(Action.SELECTED_KEY, inner);
	}
}

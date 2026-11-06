/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor.actions.text;

import java.awt.event.ActionEvent;

import jsesh.sharedvocabulary.LineAlignment;
import jsesh.ui.editor.JMDCEditor;
import jsesh.ui.editor.actionsUtils.EditorAction;

/// Sets the alignment of the cell before the tab near the cursor.
public class SetTabAlignmentAction extends EditorAction {

    private static final long serialVersionUID = 1L;

    private final LineAlignment alignment;

    public SetTabAlignmentAction(JMDCEditor editor, LineAlignment alignment) {
        super(editor);
        this.alignment = alignment;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editor.getWorkflow().setTabAlignment(alignment);
    }
}

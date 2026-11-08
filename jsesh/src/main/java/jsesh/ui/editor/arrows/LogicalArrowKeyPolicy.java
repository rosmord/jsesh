/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor.arrows;

import java.util.Objects;

import jsesh.ui.editor.JMDCEditor;
import jsesh.ui.editor.JMDCEditorWorkflow;

/// Logical arrows: they never depend on the text being edited, only on an
/// [ArrowKeyMode] (which arrow goes forward, lines or columns).
///
/// The policy exists in two flavours (see [ArrowKeyPolicies]):
///
/// - [ArrowKeyPolicies#LOGICAL_FIXED]: the mode is a setting, chosen once;
/// - [ArrowKeyPolicies#LOGICAL_SWITCHABLE]: the user can change the mode at any
///   time (from a toolbar in the application). The mode is never changed
///   automatically.
///
/// Immutable: [#withMode(ArrowKeyMode)] gives a new policy.
///
/// @param mode how the arrows read the text.
/// @param switchable true if the user can change the mode while editing.
public record LogicalArrowKeyPolicy(ArrowKeyMode mode, boolean switchable) implements ArrowKeyPolicy {

    public LogicalArrowKeyPolicy {
        Objects.requireNonNull(mode);
    }

    /// @param newMode the new mode.
    /// @return the same policy, with another mode.
    public LogicalArrowKeyPolicy withMode(ArrowKeyMode newMode) {
        return new LogicalArrowKeyPolicy(newMode, switchable);
    }

    @Override
    public ArrowKeyPolicies kind() {
        return switchable ? ArrowKeyPolicies.LOGICAL_SWITCHABLE : ArrowKeyPolicies.LOGICAL_FIXED;
    }

    @Override
    public void move(JMDCEditor editor, ArrowKey key) {
        JMDCEditorWorkflow workflow = editor.getWorkflow();
        switch (mode.resolve(key)) {
            case NEXT -> workflow.cursorNext();
            case PREVIOUS -> workflow.cursorPrevious();
            case NEXT_LINE -> workflow.cursorDown();
            case PREVIOUS_LINE -> workflow.cursorUp();
        }
    }

    @Override
    public void extendSelection(JMDCEditor editor, ArrowKey key) {
        editor.getWorkflow().expandSelection(mode.resolve(key).expandDirection());
    }

    @Override
    public void moveByWord(JMDCEditor editor, boolean leftKey, boolean extend) {
        WordMoves.move(editor.getWorkflow(), mode.isWordMoveForward(leftKey), extend);
    }
}

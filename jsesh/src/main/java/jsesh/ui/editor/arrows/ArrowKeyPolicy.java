/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor.arrows;

import jsesh.ui.editor.JMDCEditor;

/// How an editor interprets the arrow keys (plain, with Shift, and the word
/// moves bound to Left and Right).
///
/// The move actions of the editor (`GoLeftAction`, `ExpandSelectionAction`,
/// `MoveByWordAction`...) only delegate to the policy of the editor
/// ([JMDCEditor#getArrowKeyPolicy()]). All the logic of a given way to handle
/// arrows lives in its implementation, so that approaches can be tried, compared
/// and dropped without touching the rest of the editor. The available policies
/// are listed in [ArrowKeyPolicies].
///
/// Implementations should be immutable: to change a setting of a policy, the
/// editor is given a new policy.
public interface ArrowKeyPolicy {

    /// Moves the cursor (plain arrow key).
    ///
    /// @param editor the editor.
    /// @param key the arrow key.
    void move(JMDCEditor editor, ArrowKey key);

    /// Extends the selection (Shift + arrow key).
    ///
    /// @param editor the editor.
    /// @param key the arrow key.
    void extendSelection(JMDCEditor editor, ArrowKey key);

    /// Moves the cursor, or extends the selection, by one word.
    ///
    /// @param editor the editor.
    /// @param leftKey true for the Left key, false for Right.
    /// @param extend true to extend the selection.
    void moveByWord(JMDCEditor editor, boolean leftKey, boolean extend);

    /// Moves to another zone. Only meaningful for policies which keep the
    /// cursor inside a zone; does nothing by default.
    ///
    /// @param editor the editor.
    /// @param move where to go.
    default void moveToZone(JMDCEditor editor, ZoneMove move) {
    }

    /// @return which of the [ArrowKeyPolicies] this policy is.
    ArrowKeyPolicies kind();

    /// Moves between zones, for [#moveToZone(JMDCEditor, ZoneMove)].
    enum ZoneMove {
        /// The next zone in the text.
        NEXT,
        /// The previous zone in the text.
        PREVIOUS,
        /// The zone (or text) which contains the current one.
        PARENT
    }
}

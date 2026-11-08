/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor.arrows;

import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;

/// The available ways to handle the arrow keys.
///
/// This is the one place to change to add or remove an approach (the user
/// chooses one in the preferences, see [ArrowKeyPreferences]).
public enum ArrowKeyPolicies {
    /// Arrows follow the screen, see [VisualArrowKeyPolicy].
    VISUAL,
    /// Logical arrows, with a mode chosen in the preferences, see
    /// [LogicalArrowKeyPolicy].
    LOGICAL_FIXED,
    /// Logical arrows, with a mode the user can change while editing, see
    /// [LogicalArrowKeyPolicy].
    LOGICAL_SWITCHABLE;

    /// Creates a policy of this kind.
    ///
    /// @param direction for logical policies, the direction they start with.
    /// @return a new policy.
    public ArrowKeyPolicy create(TextDirection direction) {
        return switch (this) {
            case VISUAL -> new VisualArrowKeyPolicy();
            case LOGICAL_FIXED -> new LogicalArrowKeyPolicy(
                    new ArrowKeyMode(direction, TextOrientation.HORIZONTAL), false);
            case LOGICAL_SWITCHABLE -> new LogicalArrowKeyPolicy(
                    new ArrowKeyMode(direction, TextOrientation.HORIZONTAL), true);
        };
    }
}

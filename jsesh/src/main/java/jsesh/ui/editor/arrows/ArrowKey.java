/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor.arrows;

/// The four physical arrow keys. What they do depends on the editor's
/// [ArrowKeyPolicy].
public enum ArrowKey {
    LEFT, RIGHT, UP, DOWN;

    /// @param dir the old integer code of the move actions: -1 for left, 1 for
    ///        right, -2 for up, 2 for down.
    /// @return the corresponding key.
    public static ArrowKey fromDirectionCode(int dir) {
        return switch (dir) {
            case -1 -> LEFT;
            case 1 -> RIGHT;
            case -2 -> UP;
            case 2 -> DOWN;
            default -> throw new IllegalArgumentException("incorrect dir " + dir);
        };
    }
}

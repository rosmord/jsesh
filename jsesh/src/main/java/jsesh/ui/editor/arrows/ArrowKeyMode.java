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

/// How the four arrow keys are interpreted by an editor.
///
/// The arrows are *logical*: they never depend on the text being edited. The
/// user tells the editor how to read them, with two independent choices (one
/// editor each, not stored in the document, never changed automatically):
///
/// - the **direction**: with [TextDirection#LEFT_TO_RIGHT], Right goes forward in
///   the text; with [TextDirection#RIGHT_TO_LEFT], Left does;
/// - the **orientation**: with [TextOrientation#HORIZONTAL] (lines), Left and
///   Right move in the line and Up and Down change line. With
///   [TextOrientation#VERTICAL] (columns), Up and Down move in the column, and
///   Left and Right change column (the direction tells on which side the next
///   column is).
///
/// @param direction the text direction the arrows are adapted to.
/// @param orientation the text orientation the arrows are adapted to.
public record ArrowKeyMode(TextDirection direction, TextOrientation orientation) {

    /// What a key does, in logical terms.
    public enum Command {
        /// Next position in the text.
        NEXT(1),
        /// Previous position in the text.
        PREVIOUS(-1),
        /// Next line (or column).
        NEXT_LINE(2),
        /// Previous line (or column).
        PREVIOUS_LINE(-2);

        private final int expandDirection;

        Command(int expandDirection) {
            this.expandDirection = expandDirection;
        }

        /// @return the code used by
        /// [jsesh.ui.editor.JMDCEditorWorkflow#expandSelection(int)]: ±1 for one position, ±2
        /// for one line.
        public int expandDirection() {
            return expandDirection;
        }

        /// @return true for [#NEXT] and [#NEXT_LINE].
        public boolean isForward() {
            return expandDirection > 0;
        }

        /// @return true for [#NEXT] and [#PREVIOUS].
        public boolean isInLine() {
            return Math.abs(expandDirection) == 1;
        }
    }

    /// The default mode: left-to-right, lines.
    public static ArrowKeyMode defaultMode() {
        return new ArrowKeyMode(TextDirection.LEFT_TO_RIGHT, TextOrientation.HORIZONTAL);
    }

    public ArrowKeyMode {
        if (direction == null || orientation == null) {
            throw new IllegalArgumentException("direction and orientation are required");
        }
    }

    public ArrowKeyMode withDirection(TextDirection newDirection) {
        return new ArrowKeyMode(newDirection, orientation);
    }

    public ArrowKeyMode withOrientation(TextOrientation newOrientation) {
        return new ArrowKeyMode(direction, newOrientation);
    }

    /// @param key an arrow key.
    /// @return what this key does in this mode.
    public Command resolve(ArrowKey key) {
        boolean ltr = direction.isLeftToRight();
        return switch (key) {
            case RIGHT -> orientation.isHorizontal()
                    ? (ltr ? Command.NEXT : Command.PREVIOUS)
                    : (ltr ? Command.NEXT_LINE : Command.PREVIOUS_LINE);
            case LEFT -> orientation.isHorizontal()
                    ? (ltr ? Command.PREVIOUS : Command.NEXT)
                    : (ltr ? Command.PREVIOUS_LINE : Command.NEXT_LINE);
            case DOWN -> orientation.isHorizontal() ? Command.NEXT_LINE : Command.NEXT;
            case UP -> orientation.isHorizontal() ? Command.PREVIOUS_LINE : Command.PREVIOUS;
        };
    }

    /// Word moves are bound to Left and Right only. They go forward or backward
    /// according to the direction, whatever the orientation.
    ///
    /// @param leftKey true for the Left key, false for Right.
    /// @return true if the word move goes forward in the text.
    public boolean isWordMoveForward(boolean leftKey) {
        return leftKey != direction.isLeftToRight();
    }
}

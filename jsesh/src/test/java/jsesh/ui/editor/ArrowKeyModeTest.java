/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

import static jsesh.ui.editor.ArrowKeyMode.Command.NEXT;
import static jsesh.ui.editor.ArrowKeyMode.Command.NEXT_LINE;
import static jsesh.ui.editor.ArrowKeyMode.Command.PREVIOUS;
import static jsesh.ui.editor.ArrowKeyMode.Command.PREVIOUS_LINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.ui.editor.ArrowKeyMode.Command;
import jsesh.ui.editor.ArrowKeyMode.Key;

class ArrowKeyModeTest {

    private static void check(TextDirection d, TextOrientation o, Command right, Command left, Command down,
            Command up) {
        ArrowKeyMode mode = new ArrowKeyMode(d, o);
        assertEquals(right, mode.resolve(Key.RIGHT), "right in " + mode);
        assertEquals(left, mode.resolve(Key.LEFT), "left in " + mode);
        assertEquals(down, mode.resolve(Key.DOWN), "down in " + mode);
        assertEquals(up, mode.resolve(Key.UP), "up in " + mode);
    }

    @Test
    void leftToRightLines() {
        check(TextDirection.LEFT_TO_RIGHT, TextOrientation.HORIZONTAL, NEXT, PREVIOUS, NEXT_LINE, PREVIOUS_LINE);
    }

    @Test
    void rightToLeftLines() {
        check(TextDirection.RIGHT_TO_LEFT, TextOrientation.HORIZONTAL, PREVIOUS, NEXT, NEXT_LINE, PREVIOUS_LINE);
    }

    @Test
    void leftToRightColumns() {
        check(TextDirection.LEFT_TO_RIGHT, TextOrientation.VERTICAL, NEXT_LINE, PREVIOUS_LINE, NEXT, PREVIOUS);
    }

    @Test
    void rightToLeftColumns() {
        check(TextDirection.RIGHT_TO_LEFT, TextOrientation.VERTICAL, PREVIOUS_LINE, NEXT_LINE, NEXT, PREVIOUS);
    }

    @Test
    void defaultIsLeftToRightLines() {
        assertEquals(new ArrowKeyMode(TextDirection.LEFT_TO_RIGHT, TextOrientation.HORIZONTAL),
                ArrowKeyMode.defaultMode());
    }

    @Test
    void wordMovesFollowTheDirectionOnly() {
        for (TextOrientation o : TextOrientation.values()) {
            ArrowKeyMode ltr = new ArrowKeyMode(TextDirection.LEFT_TO_RIGHT, o);
            assertTrue(ltr.isWordMoveForward(false));
            assertFalse(ltr.isWordMoveForward(true));
            ArrowKeyMode rtl = new ArrowKeyMode(TextDirection.RIGHT_TO_LEFT, o);
            assertTrue(rtl.isWordMoveForward(true));
            assertFalse(rtl.isWordMoveForward(false));
        }
    }

    @Test
    void expandDirectionsMatchTheWorkflowCodes() {
        assertEquals(1, NEXT.expandDirection());
        assertEquals(-1, PREVIOUS.expandDirection());
        assertEquals(2, NEXT_LINE.expandDirection());
        assertEquals(-2, PREVIOUS_LINE.expandDirection());
    }
}

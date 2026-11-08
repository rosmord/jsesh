/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor.arrows;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import jsesh.document.HieroglyphicTextModel;
import jsesh.model.MDCPosition;
import jsesh.model.constants.TextDirection;
import jsesh.ui.editor.JMDCEditorWorkflow;

/// The "follow the screen" arrows at zone boundaries (behaviour of JSesh 9.0).
class VisualArrowKeyPolicyTest {

    private static final TextDirection LR = TextDirection.LEFT_TO_RIGHT;

    private static JMDCEditorWorkflow workflow(String mdc) throws Exception {
        HieroglyphicTextModel model = new HieroglyphicTextModel();
        model.setMDCCode(mdc);
        return new JMDCEditorWorkflow(model, code -> null);
    }

    private static MDCPosition pos(int... path) {
        return MDCPosition.of(path);
    }

    private static void horizontally(JMDCEditorWorkflow w, boolean toRight) {
        VisualArrowKeyPolicy.cursorHorizontally(w, toRight, LR);
    }

    /// `A1-%zone[dir=rl](B1-B2)-A2`: zone content gaps `(1,0,0)` (right edge of
    /// the zone) to `(1,0,2)` (left edge); `(1)` is before the zone, `(2)` after it.
    @Test
    void arrowsFollowTheScreenAtTheBoundariesOfAZoneWithAnotherDirection() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%zone[dir=rl](B1-B2)-A2");
        // Right edge of the zone: right leaves it, to its right.
        w.setCursor(pos(1, 0, 0));
        horizontally(w, true);
        assertEquals(pos(2), w.getCaret().getInsertExactPosition());
        // Left edge of the zone: left leaves it, to its left.
        w.setCursor(pos(1, 0, 2));
        horizontally(w, false);
        assertEquals(pos(1), w.getCaret().getInsertExactPosition());
        // From after the zone, left enters it by its right edge.
        w.setCursor(pos(2));
        horizontally(w, false);
        assertEquals(pos(1, 0, 0), w.getCaret().getInsertExactPosition());
        // From before the zone, right enters it by its left edge.
        w.setCursor(pos(1));
        horizontally(w, true);
        assertEquals(pos(1, 0, 2), w.getCaret().getInsertExactPosition());
        // Inside, right goes towards the right, that is backwards in the zone.
        w.setCursor(pos(1, 0, 2));
        horizontally(w, true);
        assertEquals(pos(1, 0, 1), w.getCaret().getInsertExactPosition());
    }

    @Test
    void arrowsAreUnchangedAtTheBoundariesOfAZoneWithTheSameDirection() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%zone(B1-B2)-A2");
        w.setCursor(pos(1));
        horizontally(w, true);
        assertEquals(pos(1, 0, 0), w.getCaret().getInsertExactPosition());
        w.setCursor(pos(1, 0, 2));
        horizontally(w, true);
        assertEquals(pos(2), w.getCaret().getInsertExactPosition());
    }

    @Test
    void crossingAZoneBoundaryClearsTheSelection() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%zone[dir=rl](B1-B2)-A2");
        w.setCursor(pos(1, 0, 1));
        w.expandSelection(-1);
        horizontally(w, true);
        assertEquals(pos(2), w.getCaret().getInsertExactPosition());
        assertFalse(w.getCaret().hasMark());
    }
}

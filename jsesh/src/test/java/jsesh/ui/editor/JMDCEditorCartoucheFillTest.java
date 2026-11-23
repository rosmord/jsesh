/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

import static jsesh.ui.editor.JMDCEditorWorkflow.CartoucheFillState.MIXED;
import static jsesh.ui.editor.JMDCEditorWorkflow.CartoucheFillState.NONE;
import static jsesh.ui.editor.JMDCEditorWorkflow.CartoucheFillState.OFF;
import static jsesh.ui.editor.JMDCEditorWorkflow.CartoucheFillState.ON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jsesh.document.HieroglyphicTextModel;
import jsesh.model.MDCPosition;

/// Making the cartouches fill their tab cell (`<-ra->[fill]`) from the editor.
class JMDCEditorCartoucheFillTest {

    private static JMDCEditorWorkflow workflow(String mdc) throws Exception {
        HieroglyphicTextModel model = new HieroglyphicTextModel();
        model.setMDCCode(mdc);
        return new JMDCEditorWorkflow(model, code -> null);
    }

    private static MDCPosition p(int... path) {
        return MDCPosition.of(path);
    }

    @Test
    void theCursorInACartoucheChangesIt() throws Exception {
        JMDCEditorWorkflow w = workflow("X1-<-A1-B1->-C1");
        w.setCursor(p(1, 0, 0, 0, 1));
        assertEquals(OFF, w.getCartoucheFillAtCaret());
        assertTrue(w.setCartoucheFillAtCaret(true));
        assertEquals("X1-<-A1-B1->[fill]-C1", w.getMDCCode());
        assertEquals(ON, w.getCartoucheFillAtCaret());
        // The cursor has not moved.
        assertEquals(p(1, 0, 0, 0, 1), w.getCaret().getInsertExactPosition());
        assertTrue(w.setCartoucheFillAtCaret(false));
        assertEquals("X1-<-A1-B1->-C1", w.getMDCCode());
    }

    @Test
    void theCursorJustAfterACartoucheChangesIt() throws Exception {
        JMDCEditorWorkflow w = workflow("X1-<-A1-B1->-C1");
        w.setCursor(p(2));
        assertEquals(OFF, w.getCartoucheFillAtCaret());
        assertTrue(w.setCartoucheFillAtCaret(true));
        assertEquals("X1-<-A1-B1->[fill]-C1", w.getMDCCode());
        assertEquals(p(2), w.getCaret().getInsertExactPosition());
        assertEquals(ON, w.getCartoucheFillAtCaret());
    }

    @Test
    void theCursorJustAfterACartoucheInAZoneChangesIt() throws Exception {
        JMDCEditorWorkflow w = workflow("%zone(%tab-<-A1->-%tab-B1)");
        w.setCursor(p(0, 0, 2));
        assertTrue(w.setCartoucheFillAtCaret(true));
        assertEquals("%zone(%tab-<-A1->[fill]-%tab-B1)", w.getMDCCode().replaceAll("\\s", ""));
    }

    @Test
    void theCursorJustBeforeACartoucheDoesNothing() throws Exception {
        JMDCEditorWorkflow w = workflow("X1-<-A1->");
        w.setCursor(p(1));
        assertEquals(NONE, w.getCartoucheFillAtCaret());
    }

    @Test
    void theChangeCanBeUndone() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1->");
        w.setCursor(p(0, 0, 0, 0, 1));
        w.setCartoucheFillAtCaret(true);
        w.undo();
        assertEquals("<-A1->", w.getMDCCode());
        w.redo();
        assertEquals("<-A1->[fill]", w.getMDCCode());
    }

    @Test
    void aSelectionChangesAllTheCartouchesItContains() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1->-B1-<-C1->[fill]-<-D1->");
        w.setCursor(p(0));
        w.setMark(p(4));
        assertEquals(MIXED, w.getCartoucheFillAtCaret());
        assertTrue(w.setCartoucheFillAtCaret(true));
        assertEquals("<-A1->[fill]-B1-<-C1->[fill]-<-D1->[fill]", w.getMDCCode());
        assertEquals(ON, w.getCartoucheFillAtCaret());
        // The selection is kept.
        assertTrue(w.getCaret().hasSelection());
    }

    @Test
    void itWorksInAZone() throws Exception {
        JMDCEditorWorkflow w = workflow("%zone(%tab-<-A1->-%tab-B1!%tab-<-C1-D1->-%tab-B1)");
        w.setCursor(p(0, 0, 0));
        w.setMark(p(0, 0, 9));
        assertTrue(w.setCartoucheFillAtCaret(true));
        assertEquals(2, w.getMDCCode().split("\\[fill\\]", -1).length - 1);
    }

    @Test
    void thereIsNothingToChangeWithoutACartouche() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-B1-<-C1->");
        w.setCursor(p(1));
        assertEquals(NONE, w.getCartoucheFillAtCaret());
        assertFalse(w.setCartoucheFillAtCaret(true));
        assertEquals("A1-B1-<-C1->", w.getMDCCode());
    }

    @Test
    void aCartoucheWithOtherSignsInItsQuadratCannotFill() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1->*B1");
        w.setCursor(p(0));
        w.setMark(p(1));
        assertEquals(NONE, w.getCartoucheFillAtCaret());
        assertFalse(w.setCartoucheFillAtCaret(true));
    }
}

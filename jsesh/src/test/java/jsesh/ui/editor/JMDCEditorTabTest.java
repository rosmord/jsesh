/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jsesh.document.HieroglyphicTextModel;
import jsesh.model.MDCPosition;

/// The editor commands for dynamic tabs: insertion, and moves to another stop.
class JMDCEditorTabTest {

    private static JMDCEditorWorkflow workflow(String mdc) throws Exception {
        HieroglyphicTextModel model = new HieroglyphicTextModel();
        model.setMDCCode(mdc);
        return new JMDCEditorWorkflow(model, code -> null);
    }

    private static MDCPosition pos(int... path) {
        return MDCPosition.of(path);
    }

    private static String code(JMDCEditorWorkflow w) {
        return w.getMDCCode().replaceAll("\\s", "");
    }

    @Test
    void aTabIsInsertedAtTheCursor() throws Exception {
        JMDCEditorWorkflow w = workflow("%zone(A1-B1)");
        w.setCursor(pos(0, 0, 1));
        assertTrue(w.insertTab());
        assertEquals("%zone(A1-%tab-B1)", code(w));
        assertEquals(pos(0, 0, 2), w.getCaret().getInsertExactPosition());
    }

    @Test
    void aTabCannotGoInACartouche() throws Exception {
        JMDCEditorWorkflow w = workflow("<-A1-B1->");
        // Between A1 and B1, in the cartouche.
        w.setCursor(pos(0, 0, 0, 0, 0, 1));
        assertFalse(w.insertTab());
        assertEquals("<-A1-B1->", code(w));
    }

    @Test
    void theTabBeforeTheCursorMoves() throws Exception {
        // Zone content: 0 |A1| 1 |%tab| 2 |B1| 3 |%tab| 4 |C1| 5
        JMDCEditorWorkflow w = workflow("%zone(A1-%tab-B1-%tab-C1)");
        w.setCursor(pos(0, 0, 2));
        assertTrue(w.moveTabToNextStop());
        assertEquals("%zone(A1-%tab[n=2]-B1-%tab-C1)", code(w));
        assertEquals(pos(0, 0, 2), w.getCaret().getInsertExactPosition());
        assertTrue(w.moveTabToPreviousStop());
        assertEquals("%zone(A1-%tab-B1-%tab[n=3]-C1)", code(w));
    }

    @Test
    void theTabAfterTheCursorMovesIfThereIsNoneBefore() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%tab-B1");
        w.setCursor(pos(1));
        assertTrue(w.moveTabToNextStop());
        assertEquals("A1-%tab[n=2]-B1", code(w));
    }

    @Test
    void nothingHappensWithoutATab() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-B1-%tab-C1");
        w.setCursor(pos(1));
        assertFalse(w.moveTabToNextStop());
        assertFalse(w.moveTabToPreviousStop());
        w.setCursor(pos(2));
        assertFalse(w.moveTabToPreviousStop(), "the tab is at stop 1");
        assertEquals("A1-B1-%tab-C1", code(w));
    }

    @Test
    void aMoveIsUndoneInOneStep() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-%tab-B1-%tab[n=2]-C1");
        w.setCursor(pos(2));
        assertTrue(w.moveTabToNextStop());
        assertEquals("A1-%tab[n=2]-B1-%tab[n=3]-C1", code(w));
        w.undo();
        assertEquals("A1-%tab-B1-%tab[n=2]-C1", code(w));
    }

    @Test
    void theFirstTabOfTheSelectionMoves() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-B1-%tab-C1-%tab-D1");
        w.setCursor(pos(0));
        w.setMark(pos(6));
        assertTrue(w.moveTabToNextStop());
        assertEquals("A1-B1-%tab[n=2]-C1-%tab-D1", code(w));
    }

    @Test
    void theAlignmentOfTheCellBeforeTheTabChanges() throws Exception {
        JMDCEditorWorkflow w = workflow("%zone(A1-%tab-B1)");
        w.setCursor(pos(0, 0, 2));
        assertTrue(w.setTabAlignment(jsesh.sharedvocabulary.LineAlignment.END));
        assertEquals("%zone(A1-%tab[align=end]-B1)", code(w));
        assertFalse(w.setTabAlignment(jsesh.sharedvocabulary.LineAlignment.END), "already end");
        w.undo();
        assertEquals("%zone(A1-%tab-B1)", code(w));
    }
}

/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import jsesh.document.HieroglyphicTextModel;
import jsesh.model.MDCPosition;
import jsesh.sharedvocabulary.LineAlignment;

/// Reading and changing the alignment of lines in the editor
/// ([JMDCEditorWorkflow#getLineAlignmentAtCaret()],
/// [JMDCEditorWorkflow#setLineAlignment(LineAlignment)]).
class JMDCEditorLineAlignmentTest {

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
    void theAlignmentOfALineIsTheOneOfItsBreak() throws Exception {
        // 0 A1 1 ! 2 B1 3 ! 4 C1 5
        JMDCEditorWorkflow w = workflow("A1-![align=center]B1-!C1");
        w.setCursor(pos(0));
        assertEquals(Optional.of(LineAlignment.CENTER), w.getLineAlignmentAtCaret());
        w.setCursor(pos(1));
        assertEquals(Optional.of(LineAlignment.CENTER), w.getLineAlignmentAtCaret());
        w.setCursor(pos(3));
        assertEquals(Optional.of(LineAlignment.START), w.getLineAlignmentAtCaret());
    }

    @Test
    void theLastLineWithoutBreakTakesTheAlignmentOfThePreviousOne() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-![align=end]B1");
        w.setCursor(pos(3));
        assertEquals(Optional.of(LineAlignment.END), w.getLineAlignmentAtCaret());
        w = workflow("A1");
        assertEquals(Optional.of(LineAlignment.START), w.getLineAlignmentAtCaret());
    }

    @Test
    void differentAlignmentsInTheSelectionAreMixed() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-![align=center]B1-!C1");
        w.setCursor(pos(0));
        w.setMark(pos(3));
        assertEquals(Optional.empty(), w.getLineAlignmentAtCaret());
        // A selection which ends at the very start of a line doesn't touch it.
        w.setMark(pos(2));
        assertEquals(Optional.of(LineAlignment.CENTER), w.getLineAlignmentAtCaret());
    }

    @Test
    void settingTheAlignmentChangesTheBreakOfTheLine() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-!B1-!C1-!");
        w.setCursor(pos(2));
        w.setLineAlignment(LineAlignment.CENTER);
        assertEquals("A1-!B1-![align=center]C1-!", code(w));
        assertEquals(pos(2), w.getCaret().getInsertPosition());
        assertEquals(Optional.of(LineAlignment.CENTER), w.getLineAlignmentAtCaret());
    }

    @Test
    void settingTheAlignmentChangesAllTheSelectedLinesInOneStep() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-!B1-!C1-!");
        w.setCursor(pos(1));
        w.setMark(pos(4));
        w.setLineAlignment(LineAlignment.JUSTIFY);
        assertEquals("A1-![align=justify]B1-![align=justify]C1-!", code(w));
        // The selection stays.
        assertEquals(Optional.of(LineAlignment.JUSTIFY), w.getLineAlignmentAtCaret());
        w.undo();
        assertEquals("A1-!B1-!C1-!", code(w));
    }

    @Test
    void theLastLineWithoutBreakGetsOne() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-!B1");
        w.setCursor(pos(3));
        w.setLineAlignment(LineAlignment.END);
        assertEquals("A1-!B1-![align=end]", code(w));
        assertEquals(pos(3), w.getCaret().getInsertPosition());
    }

    @Test
    void theLastLineDoesNotGetABreakIfItInheritsTheAlignment() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-!B1");
        w.setCursor(pos(0));
        w.setMark(pos(3));
        w.setLineAlignment(LineAlignment.CENTER);
        assertEquals("A1-![align=center]B1", code(w));
    }

    @Test
    void nothingHappensIfTheAlignmentIsAlreadyThere() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-![align=center]B1");
        w.setCursor(pos(0));
        w.setLineAlignment(LineAlignment.CENTER);
        assertEquals("A1-![align=center]B1", code(w));
    }

    @Test
    void inAZoneTheLinesAreThoseOfTheZone() throws Exception {
        // Zone content: 0 B1 1 ! 2 B2 3
        JMDCEditorWorkflow w = workflow("A1-%zone(B1!B2)-A2");
        w.setCursor(pos(1, 0, 0));
        w.setLineAlignment(LineAlignment.END);
        assertEquals("A1-%zone(B1-![align=end]B2)-A2", code(w));
        assertEquals(Optional.of(LineAlignment.START), workflowAt(code(w), pos(2)).getLineAlignmentAtCaret());
    }

    @Test
    void aNewLineKeepsTheAlignmentOfTheLineWhereItIsInserted() throws Exception {
        JMDCEditorWorkflow w = workflow("A1-A2-![align=center]B1");
        w.setCursor(pos(1));
        w.insertNewLine();
        assertEquals("A1-![align=center]A2-![align=center]B1", code(w));
        // At the end of the last line, which inherits its alignment.
        w = workflow("A1-![align=end]B1");
        w.setCursor(pos(3));
        w.insertNewLine();
        assertEquals("A1-![align=end]B1-![align=end]", code(w));
    }

    private static JMDCEditorWorkflow workflowAt(String mdc, MDCPosition position) throws Exception {
        JMDCEditorWorkflow w = workflow(mdc);
        w.setCursor(position);
        return w;
    }
}

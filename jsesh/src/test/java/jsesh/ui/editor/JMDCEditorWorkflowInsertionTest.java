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

import java.awt.font.FontRenderContext;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jsesh.document.HieroglyphicTextModel;
import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.io.mdc.MdcTextHelper;
import jsesh.model.HieroglyphicText;
import jsesh.model.Insert;
import jsesh.model.constants.InsertionDirection;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.layout.insert.InsertionRanker;
import jsesh.render.style.JSeshStyle;

/// Building insertions (`%insert`) from the editor, and choosing among the ways of inserting.
class JMDCEditorWorkflowInsertionTest {

    private JMDCEditorWorkflow workflow;
    private InsertionRanker ranker;

    @BeforeEach
    void setUp() {
        workflow = new JMDCEditorWorkflow(new HieroglyphicTextModel(), code -> null);
        JSeshRenderContext renderContext = new JSeshRenderContext(JSeshStyle.DEFAULT,
                PredefinedFonts.buildAllEmbeddedFonts());
        ranker = new InsertionRanker(renderContext,
                new JSeshTechRenderContext(new FontRenderContext(null, true, true), 1.0));
    }

    private void text(String mdc) throws MDCSyntaxError {
        workflow.setMDCCode(mdc);
        workflow.cursorToEndOfLine();
    }

    private static String mdc(InsertionChoice choice) {
        HieroglyphicText text = new HieroglyphicText();
        text.addTopItem(choice.insert().deepCopy().buildTopItem());
        return MdcTextHelper.toMdc(text);
    }

    @Test
    void groupsTheTwoItemsBeforeTheCursor() throws MDCSyntaxError {
        text("A1-D36-t");
        assertTrue(workflow.groupAsInsertion(ranker));
        String result = workflow.getMDCCode();
        assertTrue(result.startsWith("A1-%insert[dir="), result);
        assertTrue(result.contains("D36") && result.contains("t"), result);
        assertTrue(workflow.hasInsertionBeforeCaret());
    }

    @Test
    void needsTwoItems() throws MDCSyntaxError {
        text("D36");
        assertFalse(workflow.groupAsInsertion(ranker));
        assertEquals("D36", workflow.getMDCCode());
    }

    @Test
    void theAutomaticChoiceIsTheBestRanked() throws MDCSyntaxError {
        text("D36-t");
        workflow.groupAsInsertion(ranker);
        assertTrue(workflow.prepareChoiceOfInsertion(ranker));
        assertEquals(0, workflow.getSelectedInsertionIndex());
        assertEquals(mdc(workflow.getInsertionChoices().get(0)), workflow.getMDCCode());
    }

    @Test
    void selectingReplacesTheInsertion() throws MDCSyntaxError {
        text("D36-t");
        workflow.groupAsInsertion(ranker);
        workflow.prepareChoiceOfInsertion(ranker);
        List<InsertionChoice> choices = workflow.getInsertionChoices();
        assertTrue(choices.size() >= 9, "at least the 9 directions with the current main group");
        workflow.selectInsertion(3);
        assertEquals(mdc(choices.get(3)), workflow.getMDCCode());
        workflow.selectInsertion(5);
        assertEquals(mdc(choices.get(5)), workflow.getMDCCode());
        assertTrue(workflow.hasInsertionBeforeCaret());
    }

    @Test
    void choicesLeaveASingleStepInTheUndoHistory() throws MDCSyntaxError {
        text("D36-t");
        workflow.groupAsInsertion(ranker);
        workflow.prepareChoiceOfInsertion(ranker);
        workflow.selectInsertion(3);
        workflow.selectInsertion(5);
        workflow.undo();
        assertEquals(mdc(workflow.getInsertionChoices().get(0)), workflow.getMDCCode());
        workflow.undo();
        assertEquals("D36-t", workflow.getMDCCode());
    }

    @Test
    void comingBackToTheOriginalLeavesNoTrace() throws MDCSyntaxError {
        text("D36-t");
        workflow.groupAsInsertion(ranker);
        workflow.prepareChoiceOfInsertion(ranker);
        workflow.selectInsertion(3);
        workflow.selectInsertion(0);
        workflow.undo();
        assertEquals("D36-t", workflow.getMDCCode());
    }

    @Test
    void nothingToChooseWithoutAnInsertion() throws MDCSyntaxError {
        text("D36-t");
        assertFalse(workflow.hasInsertionBeforeCaret());
        assertFalse(workflow.prepareChoiceOfInsertion(ranker));
    }

    private Insert currentInsert() {
        return workflow.getSelectedInsertion().insert();
    }

    @Test
    void choosingADirectionKeepsTheGroups() throws MDCSyntaxError {
        text("D36-t");
        workflow.groupAsInsertion(ranker);
        workflow.prepareChoiceOfInsertion(ranker);
        Insert before = currentInsert();
        workflow.selectInsertionDirection(InsertionDirection.BOTTOM_END);
        Insert after = currentInsert();
        assertEquals(InsertionDirection.BOTTOM_END, after.getDirection());
        assertTrue(after.getMain().equalsIgnoreId(before.getMain()));
        assertEquals(mdc(workflow.getSelectedInsertion()), workflow.getMDCCode());
    }

    @Test
    void enlargementOnlyAppliesWhereItHasAnEffect() throws MDCSyntaxError {
        text("D36-t");
        workflow.groupAsInsertion(ranker);
        workflow.prepareChoiceOfInsertion(ranker);
        workflow.selectInsertionDirection(InsertionDirection.TOP_END);
        workflow.changeInsertionEnlargement(10, 5);
        assertEquals(10, workflow.getInsertionDw());
        assertEquals(5, workflow.getInsertionDh());
        assertTrue(workflow.getMDCCode().startsWith("%insert[dir=te,dw=10,dh=5]"), workflow.getMDCCode());
        // dw has no effect from the top: it is not written, but it is kept.
        workflow.selectInsertionDirection(InsertionDirection.TOP);
        assertTrue(workflow.getMDCCode().startsWith("%insert[dir=t,dh=5]"), workflow.getMDCCode());
        workflow.selectInsertionDirection(InsertionDirection.TOP_END);
        assertTrue(workflow.getMDCCode().startsWith("%insert[dir=te,dw=10,dh=5]"), workflow.getMDCCode());
    }

    @Test
    void enlargementIsNeverNegative() throws MDCSyntaxError {
        text("D36-t");
        workflow.groupAsInsertion(ranker);
        workflow.prepareChoiceOfInsertion(ranker);
        workflow.changeInsertionEnlargement(-5, -5);
        assertEquals(0, workflow.getInsertionDw());
        assertEquals(0, workflow.getInsertionDh());
    }

    @Test
    void enlargementLeavesASingleStepInTheUndoHistory() throws MDCSyntaxError {
        text("D36-t");
        workflow.groupAsInsertion(ranker);
        String grouped = workflow.getMDCCode();
        workflow.prepareChoiceOfInsertion(ranker);
        workflow.selectInsertionDirection(InsertionDirection.CENTER);
        workflow.changeInsertionEnlargement(5, 0);
        workflow.changeInsertionEnlargement(5, 0);
        assertTrue(workflow.getMDCCode().startsWith("%insert[dir=c,dw=10]"), workflow.getMDCCode());
        workflow.undo();
        assertEquals(grouped, workflow.getMDCCode());
    }

    @Test
    void cancellingPutsBackTheOriginal() throws MDCSyntaxError {
        text("D36-t");
        workflow.groupAsInsertion(ranker);
        String grouped = workflow.getMDCCode();
        workflow.prepareChoiceOfInsertion(ranker);
        workflow.selectInsertionDirection(InsertionDirection.CENTER);
        workflow.changeInsertionEnlargement(5, 5);
        workflow.cancelChoiceOfInsertion();
        assertEquals(grouped, workflow.getMDCCode());
        workflow.undo();
        assertEquals("D36-t", workflow.getMDCCode());
    }

    @Test
    void anExistingEnlargementIsKept() throws MDCSyntaxError {
        text("%insert[dir=te,dw=20](D36,t)");
        assertTrue(workflow.prepareChoiceOfInsertion(ranker));
        assertEquals(20, workflow.getInsertionDw());
        assertEquals(InsertionDirection.TOP_END, currentInsert().getDirection());
        assertEquals(20, currentInsert().getDw());
    }
}

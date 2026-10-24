package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jsesh.document.HieroglyphicTextModel;
import jsesh.glyphs.signdata.PossibilitiesList;
import jsesh.glyphs.signdata.Possibility;

/// Choosing among the possibilities for a code with [JMDCEditorWorkflow], as an
/// alternative to cycling with the space key.
class JMDCEditorWorkflowPossibilitiesTest {

    private JMDCEditorWorkflow workflow;

    @BeforeEach
    void setUp() {
        PossibilitiesList m = new PossibilitiesList("m");
        m.addSign("G17");
        m.addSign("U1");
        m.addSign("Aa15");
        PossibilitiesList single = new PossibilitiesList("a");
        single.addSign("G1");
        // A repository gives the same list each time, as the real one does.
        Map<String, PossibilitiesList> lists = Map.of("m", m, "a", single);
        workflow = new JMDCEditorWorkflow(new HieroglyphicTextModel(), lists::get);
    }

    private void type(String keys) {
        for (char c : keys.toCharArray()) {
            workflow.keyTyped(c);
        }
    }

    @Test
    void choicesAreAvailableAfterTheCodeIsAccepted() {
        type("m ");
        assertTrue(workflow.prepareChoiceOfPossibility());
        assertEquals(List.of("G17", "U1", "Aa15"),
                workflow.getPossibilityChoices().stream().map(Possibility::getCode).toList());
        assertEquals(0, workflow.getSelectedPossibilityIndex());
    }

    @Test
    void preparingAcceptsTheCodeBeingTyped() {
        type("m");
        assertTrue(workflow.prepareChoiceOfPossibility());
        assertEquals("G17", workflow.getMDCCode());
    }

    @Test
    void selectingReplacesTheSignWithoutAddingOne() {
        type("m ");
        workflow.prepareChoiceOfPossibility();
        workflow.selectPossibility(2);
        assertEquals("Aa15", workflow.getMDCCode());
        assertEquals(2, workflow.getSelectedPossibilityIndex());
        workflow.selectPossibility(0);
        assertEquals("G17", workflow.getMDCCode());
    }

    @Test
    void selectionAndSpaceCyclingCanBeMixed() {
        type("m ");
        workflow.selectPossibility(1);
        type(" ");
        assertEquals("Aa15", workflow.getMDCCode());
    }

    @Test
    void nothingToChooseFromWithASinglePossibility() {
        type("a ");
        assertFalse(workflow.prepareChoiceOfPossibility());
    }

    @Test
    void selectionIsIgnoredOnceTheCursorHasMoved() {
        type("m ");
        workflow.prepareChoiceOfPossibility();
        workflow.cursorPrevious();
        workflow.selectPossibility(2);
        assertEquals("G17", workflow.getMDCCode());
    }

    @Test
    void outOfRangeSelectionIsIgnored() {
        type("m ");
        workflow.selectPossibility(7);
        workflow.selectPossibility(-1);
        assertEquals("G17", workflow.getMDCCode());
    }
}

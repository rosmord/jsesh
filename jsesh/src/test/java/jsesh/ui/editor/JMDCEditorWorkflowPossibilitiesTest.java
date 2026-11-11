package jsesh.ui.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jsesh.document.HieroglyphicTextModel;
import jsesh.glyphs.possibilities.PossibilitiesList;
import jsesh.glyphs.possibilities.Possibility;
import jsesh.glyphs.possibilities.PossibilityRepository;

/// Choosing among the possibilities for a code with [JMDCEditorWorkflow], as an
/// alternative to cycling with the space key.
class JMDCEditorWorkflowPossibilitiesTest {

    private JMDCEditorWorkflow workflow;
    private PossibilityRepository repository;

    @BeforeEach
    void setUp() {
        PossibilitiesList m = PossibilitiesList.ofSigns("m", List.of("G17", "U1", "Aa15"));
        PossibilitiesList single = PossibilitiesList.ofSigns("a", List.of("G1"));
        // A repository gives the same list each time, as the real one does.
        Map<String, PossibilitiesList> lists = Map.of("m", m, "a", single);
        repository = code -> lists.getOrDefault(code, PossibilitiesList.empty(code));
        workflow = new JMDCEditorWorkflow(new HieroglyphicTextModel(), repository);
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
                workflow.getPossibilityChoices().stream().map(Possibility::asMdC).toList());
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

    @Test
    void typingTheCodeAgainProposesTheLastChoice() {
        type("m  ");
        assertEquals("U1", workflow.getMDCCode());
        type("m ");
        assertEquals("U1U1", workflow.getMDCCode().replaceAll("[\\s-]", ""));
    }

    @Test
    void theLastChoiceMadeInThePopupIsRemembered() {
        type("m ");
        workflow.prepareChoiceOfPossibility();
        workflow.selectPossibility(2);
        type("m ");
        assertEquals("Aa15Aa15", workflow.getMDCCode().replaceAll("[\\s-]", ""));
        assertEquals(2, workflow.getSelectedPossibilityIndex());
    }

    @Test
    void theLastChoiceIsForgottenWhenTheListChanges() {
        PossibilitiesList[] current = { PossibilitiesList.ofSigns("m", List.of("G17", "U1")) };
        JMDCEditorWorkflow w = new JMDCEditorWorkflow(new HieroglyphicTextModel(), code -> current[0]);
        for (char c : "m  ".toCharArray()) {
            w.keyTyped(c);
        }
        assertEquals("U1", w.getMDCCode());
        // A new sign with the same value, e.g. an embedded sign.
        current[0] = PossibilitiesList.ofSigns("m", List.of("G17", "U1", "sign1"));
        for (char c : "m ".toCharArray()) {
            w.keyTyped(c);
        }
        assertEquals("U1G17", w.getMDCCode().replaceAll("[\\s-]", ""));
    }

    @Test
    void theSelectionIsNotSharedBetweenEditors() {
        JMDCEditorWorkflow other = new JMDCEditorWorkflow(new HieroglyphicTextModel(), repository);
        type("m  ");
        assertEquals("U1", workflow.getMDCCode());
        other.keyTyped('m');
        other.keyTyped(' ');
        assertEquals("G17", other.getMDCCode());
    }
}

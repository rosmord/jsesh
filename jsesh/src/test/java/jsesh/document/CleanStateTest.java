package jsesh.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.model.Hieroglyph;
import jsesh.model.TopItem;
import jsesh.model.Cadrat;

/// The "modified document" state, as seen by the text's listeners.
///
/// Listeners (e.g. the application's view, which decides whether to ask for
/// saving) are notified while an edit runs. They must already see the text
/// as modified, even for the very first edit.
public class CleanStateTest {

    private static List<TopItem> oneSign() {
        return List.of(new Cadrat(new Hieroglyph("A1")));
    }

    @Test
    public void listenerSeesFirstEditAsModification() {
        HieroglyphicTextModel text = new HieroglyphicTextModel();
        List<Boolean> seen = new ArrayList<>();
        text.addListener(e -> seen.add(text.isClean()));
        text.insertElementsAt(text.getLastPosition(), oneSign());
        assertFalse(seen.isEmpty());
        assertFalse(seen.get(seen.size() - 1), "listener saw a clean text after one edit");
        assertFalse(text.isClean());
    }

    @Test
    public void undoingTheOnlyEditLeavesTextClean() {
        HieroglyphicTextModel text = new HieroglyphicTextModel();
        text.insertElementsAt(text.getLastPosition(), oneSign());
        List<Boolean> seen = new ArrayList<>();
        text.addListener(e -> seen.add(text.isClean()));
        text.undo();
        assertEquals(0, text.getHieroglyphicText().getNumberOfChildren());
        assertTrue(seen.get(seen.size() - 1));
        assertTrue(text.isClean());
    }
}

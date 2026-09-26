package jsesh.document;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.model.AlphabeticCharacter;
import jsesh.model.MDCPosition;
import jsesh.model.TopItem;

/// Typed text is undone word by word.
public class TypingUndoTest {

    private void type(HieroglyphicTextModel text, String s) {
        for (char c : s.toCharArray()) {
            MDCPosition end = new MDCPosition(text.getModel(), text.getModel().getNumberOfChildren());
            List<TopItem> items = new ArrayList<>(AlphabeticCharacter.fromMdcText('l', String.valueOf(c)));
            text.insertTypedElementsAt(end, items);
        }
    }

    @Test
    public void typedWordIsUndoneInOneStep() {
        HieroglyphicTextModel text = new HieroglyphicTextModel();
        type(text, "abc");
        assertEquals(3, text.getModel().getNumberOfChildren());
        text.undo();
        assertEquals(0, text.getModel().getNumberOfChildren());
        text.redo();
        assertEquals(3, text.getModel().getNumberOfChildren());
    }

    @Test
    public void undoGoesWordByWord() {
        HieroglyphicTextModel text = new HieroglyphicTextModel();
        type(text, "ab cd");
        text.undo();
        // "cd" is removed, "ab " is kept.
        assertEquals(3, text.getModel().getNumberOfChildren());
        text.undo();
        assertEquals(0, text.getModel().getNumberOfChildren());
    }

    @Test
    public void nonAdjacentTypingIsNotMerged() {
        HieroglyphicTextModel text = new HieroglyphicTextModel();
        type(text, "ab");
        List<TopItem> x = new ArrayList<>(AlphabeticCharacter.fromMdcText('l', "x"));
        text.insertTypedElementsAt(new MDCPosition(text.getModel(), 0), x);
        text.undo();
        assertEquals(2, text.getModel().getNumberOfChildren());
    }
}

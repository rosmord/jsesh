package jsesh.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.io.mdc.MdCModelWriter;
import jsesh.model.AlphabeticCharacter;
import jsesh.model.Cadrat;
import jsesh.model.Hieroglyph;
import jsesh.model.LineBreak;
import jsesh.model.TextScope;
import jsesh.model.TopItem;

/// Undoable edits in a nested scope (the content of a cartouche), through
/// [HieroglyphicTextModel].
class ScopedEditTest {

    /// The content of the cartouche, when it is the first item of the text.
    private static final TextScope CARTOUCHE = TextScope.of(0, 0, 0, 0);

    private static HieroglyphicTextModel model(String mdc) throws Exception {
        HieroglyphicTextModel m = new HieroglyphicTextModel();
        m.setMDCCode(mdc);
        return m;
    }

    private static String mdc(HieroglyphicTextModel m) {
        return new MdCModelWriter().toMdC(m.getHieroglyphicText());
    }

    private static List<TopItem> quadrat(String code) {
        return List.of(new Cadrat(new Hieroglyph(code)));
    }

    @Test
    void insertInACartoucheIsUndoable() throws Exception {
        HieroglyphicTextModel m = model("<-A1-C1->-D1");
        m.insertElementsAt(CARTOUCHE, 1, quadrat("B1"));
        assertEquals("<-A1-B1-C1->-D1", mdc(m));
        m.undo();
        assertEquals("<-A1-C1->-D1", mdc(m));
        m.redo();
        assertEquals("<-A1-B1-C1->-D1", mdc(m));
    }

    @Test
    void removeInACartoucheIsUndoable() throws Exception {
        HieroglyphicTextModel m = model("<-A1-B1-C1->");
        m.removeElements(CARTOUCHE, 2, 0);
        assertEquals("<-C1->", mdc(m));
        m.undo();
        assertEquals("<-A1-B1-C1->", mdc(m));
    }

    @Test
    void replaceInACartoucheIsUndoable() throws Exception {
        HieroglyphicTextModel m = model("<-A1-B1-C1->");
        m.replaceElement(CARTOUCHE, 1, 3, quadrat("D1"));
        assertEquals("<-A1-D1->", mdc(m));
        m.undo();
        assertEquals("<-A1-B1-C1->", mdc(m));
    }

    @Test
    void lineBreakIsRefusedInACartouche() throws Exception {
        HieroglyphicTextModel m = model("<-A1->");
        assertThrows(IllegalArgumentException.class,
                () -> m.insertElementsAt(CARTOUCHE, 1, List.of(new LineBreak())));
        assertEquals("<-A1->", mdc(m));
    }

    @Test
    void typingInACartoucheIsUndoneWordByWord() throws Exception {
        HieroglyphicTextModel m = model("<-A1->");
        int index = 1;
        for (char c : "ab".toCharArray()) {
            List<TopItem> items = new ArrayList<>(AlphabeticCharacter.fromMdcText('l', String.valueOf(c)));
            m.insertTypedElementsAt(CARTOUCHE, index++, items);
        }
        assertEquals(3, CARTOUCHE.resolve(m.getHieroglyphicText()).getNumberOfChildren());
        m.undo();
        assertEquals("<-A1->", mdc(m));
    }

    @Test
    void topLevelEditsStillWork() throws Exception {
        HieroglyphicTextModel m = model("A1");
        m.insertElementsAt(TextScope.ROOT, 1, quadrat("B1"));
        assertEquals("A1-B1", mdc(m));
        m.undo();
        assertEquals("A1", mdc(m));
    }
}

package jsesh.document;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.model.Cadrat;
import jsesh.model.Hieroglyph;
import jsesh.model.MDCMark;
import jsesh.model.TextScope;

/// A mark standing in a cartouche stays there through undo and redo.
class ScopedUndoCaretTest {

    private static final TextScope CARTOUCHE = TextScope.of(0, 0, 0, 0);

    @Test
    void markSurvivesUndoAndRedo() throws Exception {
        HieroglyphicTextModel m = new HieroglyphicTextModel();
        m.setMDCCode("<-A1-C1->");
        MDCMark mark = new MDCMark(m.getHieroglyphicText(), CARTOUCHE.positionAt(1));
        m.insertElementsAt(CARTOUCHE, 1, List.of(new Cadrat(new Hieroglyph("B1"))));
        assertEquals(CARTOUCHE.positionAt(2), mark.getPosition());
        m.undo();
        assertEquals(CARTOUCHE.positionAt(1), mark.getPosition());
        m.redo();
        assertEquals(CARTOUCHE.positionAt(2), mark.getPosition());
    }
}

package jsesh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.io.mdc.MdCModelWriter;
import jsesh.mdcreader.MDCParserModelGenerator;

/// Tests for [TextScope] and [ItemSequence].
class TextScopeTest {

    private static HieroglyphicText parse(String mdc) throws Exception {
        return new MDCParserModelGenerator().parse(mdc);
    }

    private static MDCPosition p(int... path) {
        return MDCPosition.of(path);
    }

    @Test
    void topLevelPositionsAreInTheRoot() throws Exception {
        HieroglyphicText t = parse("A1-<-B1-C1->");
        assertEquals(TextScope.ROOT, TextScope.containing(t, p(1)));
        assertSame(t, TextScope.ROOT.resolve(t));
    }

    @Test
    void insideAQuadratIsStillTheRoot() throws Exception {
        HieroglyphicText t = parse("A1*B1");
        assertEquals(TextScope.ROOT, TextScope.containing(t, p(0, 0, 1)));
    }

    @Test
    void cartoucheContentIsAScope() throws Exception {
        HieroglyphicText t = parse("A1-<-B1-C1->");
        TextScope scope = TextScope.containing(t, p(1, 0, 0, 0, 1));
        assertEquals(TextScope.of(1, 0, 0, 0), scope);
        ItemSequence content = scope.resolve(t);
        assertTrue(content instanceof BasicItemList);
        assertEquals(2, content.getNumberOfChildren());
        assertEquals(1, scope.localIndexOf(p(1, 0, 0, 0, 1)));
        assertEquals(p(1, 0, 0, 0, 2), scope.positionAt(2));
    }

    @Test
    void insideAQuadratOfACartouche() throws Exception {
        HieroglyphicText t = parse("<-B1*C1->");
        // Between B1 and C1, in the first quadrat of the cartouche.
        MDCPosition inner = p(0, 0, 0, 0, 0, 0, 1);
        TextScope scope = TextScope.containing(t, inner);
        assertEquals(TextScope.of(0, 0, 0, 0), scope);
        assertTrue(scope.contains(inner));
        assertEquals(0, scope.localIndexOf(inner));
        assertFalse(scope.contains(p(1)));
    }

    @Test
    void nestedCartouches() throws Exception {
        HieroglyphicText t = parse("<-A1-<h-B1-h>->");
        // In the inner cartouche, which is the second quadrat of the outer one.
        TextScope scope = TextScope.containing(t, p(0, 0, 0, 0, 1, 0, 0, 0, 1));
        assertEquals(TextScope.of(0, 0, 0, 0, 1, 0, 0, 0), scope);
        assertEquals(1, scope.resolve(t).getNumberOfChildren());
    }

    @Test
    void cartoucheOnlyAcceptsBasicItems() throws Exception {
        HieroglyphicText t = parse("<-B1->");
        ItemSequence content = TextScope.of(0, 0, 0, 0).resolve(t);
        assertFalse(content.accepts(new LineBreak()));
        assertTrue(content.accepts(new Cadrat(new Hieroglyph("A1"))));
        assertThrows(IllegalArgumentException.class, () -> content.insertItemsAt(0, List.of(new LineBreak())));
    }

    @Test
    void insertAndRemoveInACartouche() throws Exception {
        HieroglyphicText t = parse("<-B1->");
        ItemSequence content = TextScope.of(0, 0, 0, 0).resolve(t);
        content.insertItemsAt(1, List.of(new Cadrat(new Hieroglyph("C1"))));
        assertEquals("<-B1-C1->", new MdCModelWriter().toMdC(t));
        List<TopItem> removed = content.removeItems(0, 1);
        assertEquals(1, removed.size());
        assertEquals("<-C1->", new MdCModelWriter().toMdC(t));
    }

    @Test
    void notAScope() throws Exception {
        HieroglyphicText t = parse("A1*B1");
        assertThrows(IllegalArgumentException.class, () -> TextScope.of(0, 0).resolve(t));
        assertThrows(IllegalArgumentException.class, () -> TextScope.of(5).resolve(t));
    }
}

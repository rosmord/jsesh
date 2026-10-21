package org.qenherkhopeshef.viewtoolkit1.drawing.textOriented;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.viewtoolkit1.drawing.element.SpacerElement;
import org.qenherkhopeshef.viewtoolkit1.drawing.textOriented.TextDocument;
import org.qenherkhopeshef.viewtoolkit1.drawing.textOriented.TextDocumentEvent;
import org.qenherkhopeshef.viewtoolkit1.drawing.textOriented.TextPosition;

public class TextDocumentTest {

    @Test
    public void newDocumentHasOneEmptyParagraph() {
        TextDocument doc = new TextDocument();
        assertEquals(1, doc.getParagraphCount());
        assertEquals(TextPosition.START, doc.getEnd());
    }

    @Test
    public void insertTextInParagraph() {
        TextDocument doc = new TextDocument();
        TextPosition end = doc.insertText(TextPosition.START, "Hello");
        assertEquals(new TextPosition(0, 5), end);
        doc.insertText(new TextPosition(0, 5), " world");
        assertEquals("Hello world", doc.getParagraph(0).getText());
    }

    @Test
    public void insertLineBreaksSplitsParagraphs() {
        TextDocument doc = new TextDocument();
        doc.insertText(TextPosition.START, "abcdef");
        List<TextDocumentEvent> events = new ArrayList<>();
        doc.addTextDocumentListener(events::add);
        TextPosition end = doc.insertText(new TextPosition(0, 3), "X\nY\r\nZ");
        assertEquals(3, doc.getParagraphCount());
        assertEquals("abcX", doc.getParagraph(0).getText());
        assertEquals("Y", doc.getParagraph(1).getText());
        assertEquals("Zdef", doc.getParagraph(2).getText());
        assertEquals(new TextPosition(2, 1), end);
        assertEquals(List.of(new TextDocumentEvent(TextDocumentEvent.Type.CHANGED, 0, 1),
                new TextDocumentEvent(TextDocumentEvent.Type.INSERTED, 1, 2)), events);
    }

    @Test
    public void deleteAcrossParagraphsMergesThem() {
        TextDocument doc = new TextDocument();
        doc.insertText(TextPosition.START, "first\nsecond\nthird");
        List<TextDocumentEvent> events = new ArrayList<>();
        doc.addTextDocumentListener(events::add);
        doc.delete(new TextPosition(2, 2), new TextPosition(0, 3)); // reversed order on purpose
        assertEquals(1, doc.getParagraphCount());
        assertEquals("firird", doc.getParagraph(0).getText());
        assertEquals(new TextDocumentEvent(TextDocumentEvent.Type.REMOVED, 1, 2), events.get(1));
    }

    @Test
    public void embeddedElementsFollowTheirText() {
        TextDocument doc = new TextDocument();
        SpacerElement a = new SpacerElement(10, 10);
        SpacerElement b = new SpacerElement(20, 20);
        doc.insertText(TextPosition.START, "0123");
        doc.insertElement(new TextPosition(0, 2), a);
        doc.insertElement(new TextPosition(0, 0), b); // b 0 1 a 2 3
        assertSame(b, doc.getParagraph(0).getElementAt(0));
        assertSame(a, doc.getParagraph(0).getElementAt(3));
        assertNull(doc.getParagraph(0).getElementAt(1));
        // Split between the two elements.
        doc.insertText(new TextPosition(0, 2), "\n");
        assertEquals(List.of(b), doc.getParagraph(0).getElements());
        assertEquals(List.of(a), doc.getParagraph(1).getElements());
        // Merge again, deleting b.
        doc.delete(new TextPosition(0, 0), new TextPosition(1, 0));
        assertEquals(List.of(a), doc.getParagraph(0).getElements());
        assertEquals("123", doc.getText(new TextPosition(0, 0), doc.getEnd()));
    }

    @Test
    public void insertedTextIsCleaned() {
        TextDocument doc = new TextDocument();
        doc.insertText(TextPosition.START, "a\tb\u0001c￼d");
        assertEquals("a    bcd", doc.getParagraph(0).getText());
    }

    @Test
    public void nextAndPreviousCrossParagraphs() {
        TextDocument doc = new TextDocument();
        doc.insertText(TextPosition.START, "ab\ncd");
        assertEquals(new TextPosition(1, 0), doc.next(new TextPosition(0, 2)));
        assertEquals(new TextPosition(0, 2), doc.previous(new TextPosition(1, 0)));
        assertNull(doc.previous(TextPosition.START));
        assertNull(doc.next(doc.getEnd()));
    }

    @Test
    public void builderAndGetText() {
        TextDocument doc = new TextDocument();
        TextDocument.Builder builder = new TextDocument.Builder();
        builder.appendText("one\ntwo ").appendElement(new SpacerElement(1, 1)).appendText("!").newParagraph();
        doc.setContent(builder);
        assertEquals(3, doc.getParagraphCount());
        assertEquals("one\ntwo !\n", doc.getText(TextPosition.START, doc.getEnd()));
    }
}

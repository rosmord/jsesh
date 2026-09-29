package jsesh.io.mdc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.HieroglyphicText;
import jsesh.model.MDCPosition;

/// Writing only part of a text, between two [MDCPosition]s.
class MdCModelWriterRangeTest {

    /// Positions: `0 |A1| 1 |A2| 2 |!| 3 |B1| 4 |B2| 5 |!| 6 |C1| 7`
    private HieroglyphicText text;

    @BeforeEach
    void setUp() throws Exception {
        text = new MDCParserModelGenerator().parse("A1-A2-!B1-B2-!C1");
    }

    private static MDCPosition pos(int i) {
        return new MDCPosition(i);
    }

    @Test
    void toMdCBetweenPositions() {
        MdCModelWriter writer = new MdCModelWriter();
        assertEquals("B1-B2", writer.toMdC(text, pos(3), pos(5)));
        assertEquals("B1-B2-!\n", writer.toMdC(text, pos(3), pos(6)));
        assertEquals("", writer.toMdC(text, pos(4), pos(4)));
        assertEquals(writer.toMdC(text), writer.toMdC(text, pos(0), text.getLastPosition()));
    }

    @Test
    void writeBetweenPositions() {
        StringWriter out = new StringWriter();
        new MdCModelWriter().write(out, text, pos(6), pos(7));
        assertEquals("C1", out.toString());
    }
}

package jsesh.model.unicode;

import static jsesh.model.unicode.UnicodeHieroglyphControls.BEGIN_SEGMENT;
import static jsesh.model.unicode.UnicodeHieroglyphControls.END_SEGMENT;
import static jsesh.model.unicode.UnicodeHieroglyphControls.HORIZONTAL_JOINER;
import static jsesh.model.unicode.UnicodeHieroglyphControls.INSERT_AT_BOTTOM;
import static jsesh.model.unicode.UnicodeHieroglyphControls.INSERT_AT_BOTTOM_START;
import static jsesh.model.unicode.UnicodeHieroglyphControls.INSERT_AT_MIDDLE;
import static jsesh.model.unicode.UnicodeHieroglyphControls.INSERT_AT_TOP;
import static jsesh.model.unicode.UnicodeHieroglyphControls.INSERT_AT_TOP_END;
import static jsesh.model.unicode.UnicodeHieroglyphControls.MIRROR_HORIZONTALLY;
import static jsesh.model.unicode.UnicodeHieroglyphControls.OVERLAY_MIDDLE;
import static jsesh.model.unicode.UnicodeHieroglyphControls.VERTICAL_JOINER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.Cadrat;
import jsesh.model.HieroglyphicText;
import jsesh.model.ShadingCode;
import jsesh.model.constants.TextDirection;

public class MdCToUnicodeConverterTest {

    /// The Unicode string for sign codes and control code points, in order.
    /// Strings are signs' MdC codes, integers are code points.
    private static String seq(Object... items) {
        StringBuilder b = new StringBuilder();
        for (Object o : items) {
            if (o instanceof Integer cp) {
                b.appendCodePoint(cp);
            } else {
                b.append(MdcUnicodeTable.INSTANCE.getUnicodeFor((String) o));
            }
        }
        return b.toString();
    }

    private static String convert(String mdc, boolean controls) throws Exception {
        return convert(mdc, controls, TextDirection.LEFT_TO_RIGHT);
    }

    private static String convert(String mdc, boolean controls, TextDirection direction) throws Exception {
        MdCToUnicodeConverter converter = new MdCToUnicodeConverter();
        converter.setIncludeFormatControlChars(controls);
        converter.setTextDirection(direction);
        return converter.convertToPlainUnicode(new MDCParserModelGenerator().parse(mdc));
    }

    @Test
    public void plainTextHasNoControls() throws Exception {
        assertEquals(seq("A1", "X1"), convert("A1:X1", false));
        assertEquals(seq("A1", "X1"), convert("%insert[dir=bs](A1,X1)", false));
        assertEquals(seq("A1") + "\n" + seq("X1"), convert("A1-!-X1", false));
    }

    @Test
    public void joiners() throws Exception {
        assertEquals(seq("A1", VERTICAL_JOINER, "X1"), convert("A1:X1", true));
        assertEquals(seq("A1", HORIZONTAL_JOINER, "X1"), convert("A1*X1", true));
    }

    @Test
    public void segmentsOnlyWhenPrecedenceRequiresThem() throws Exception {
        // HJ binds tighter than VJ: no segment needed in (a*b):c.
        assertEquals(seq("A1", HORIZONTAL_JOINER, "X1", VERTICAL_JOINER, "N35"), convert("(A1*X1):N35", true));
        // But a vertical group inside a horizontal join needs one.
        assertEquals(
                seq("A1", HORIZONTAL_JOINER, BEGIN_SEGMENT, "X1", VERTICAL_JOINER, "N35", END_SEGMENT),
                convert("A1*(X1:N35)", true));
    }

    @Test
    public void insertionIsContainerControlInserted() throws Exception {
        assertEquals(seq("A1", INSERT_AT_BOTTOM_START, "X1"), convert("%insert[dir=bs](A1,X1)", true));
    }

    @Test
    public void insertionOrderDoesNotDependOnReadingOrder() throws Exception {
        // Same insertion of X1 into A1, but X1 is read first.
        assertEquals(convert("%insert[dir=bs](A1,X1)", true),
                convert("%insert[dir=bs,target=2](X1,A1)", true));
    }

    @Test
    public void insertionDirections() throws Exception {
        assertEquals(seq("A1", INSERT_AT_TOP_END, "X1"), convert("%insert[dir=te](A1,X1)", true));
        assertEquals(seq("A1", INSERT_AT_MIDDLE, "X1"), convert("%insert[dir=c](A1,X1)", true));
        assertEquals(seq("A1", INSERT_AT_TOP, "X1"), convert("%insert[dir=t](A1,X1)", true));
        assertEquals(seq("A1", INSERT_AT_BOTTOM, "X1"), convert("%insert[dir=b](A1,X1)", true));
    }

    @Test
    public void insertionWithoutUnicodeControlIsWrittenInReadingOrder() throws Exception {
        assertEquals(seq("A1", "X1"), convert("%insert[dir=s](A1,X1)", true));
        assertEquals(seq("X1", "A1"), convert("%insert[dir=e,target=2](X1,A1)", true));
    }

    @Test
    public void insertedGroupIsInsideSegment() throws Exception {
        assertEquals(
                seq("A1", INSERT_AT_BOTTOM_START, BEGIN_SEGMENT, "X1", VERTICAL_JOINER, "N35", END_SEGMENT),
                convert("%insert[dir=bs](A1,(X1:N35))", true));
    }

    @Test
    public void nestedInsertions() throws Exception {
        // The inner insertion is the container of the outer one: no segment needed.
        assertEquals(seq("X1", INSERT_AT_BOTTOM_START, "A1", INSERT_AT_TOP_END, "N35"),
                convert("%insert[dir=te](%insert[dir=bs](X1,A1),N35)", true));
        // As the inserted element, it needs a segment.
        assertEquals(
                seq("N35", INSERT_AT_TOP_END, BEGIN_SEGMENT, "X1", INSERT_AT_BOTTOM_START, "A1", END_SEGMENT),
                convert("%insert[dir=te](N35,%insert[dir=bs](X1,A1))", true));
    }

    @Test
    public void overlayKeepsTheOrderOfTheMdC() throws Exception {
        // The last sign is on top, as in the MdC: no sorting.
        assertEquals(seq("S29", OVERLAY_MIDDLE, "F46"), convert("S29##F46", true));
        assertEquals(seq("F46", OVERLAY_MIDDLE, "S29"), convert("F46##S29", true));
    }

    @Test
    public void stackOfSingleSignsIsASignOverlay() throws Exception {
        assertEquals(seq("S29", OVERLAY_MIDDLE, "F46"), convert("%stack(S29,F46)", true));
        // spaces mean nothing in an overlay
        assertEquals(seq("S29", OVERLAY_MIDDLE, "F46"), convert("%stack(..S29,F46.)", true));
    }

    @Test
    public void stackOfGroupsUsesSegments() throws Exception {
        assertEquals(
                seq(BEGIN_SEGMENT, "X1", VERTICAL_JOINER, "N35", END_SEGMENT, OVERLAY_MIDDLE, "F46"),
                convert("%stack(X1:N35,F46)", true));
    }

    @Test
    public void unknownSignsGetNoControl() throws Exception {
        assertEquals(seq("A1") + "ZZZ999", convert("%insert[dir=bs](A1,ZZZ999)", true));
    }

    @Test
    public void mirror() throws Exception {
        assertEquals(seq("A1", MIRROR_HORIZONTALLY), convert("A1\\", true));
    }

    @Test
    public void rotationUsesAStandardizedVariationSequence() throws Exception {
        // D27 has a 90° sequence, but no 180° one.
        assertEquals(seq("D27", 0xFE00), convert("D27\\R90", true));
        assertEquals(seq("D27"), convert("D27\\R180", true));
        // Rotation is not a format control...
        assertEquals(seq("D27"), convert("D27\\R90", false));
    }

    @Test
    public void rotationIsCounterclockwiseInRightToLeftText() throws Exception {
        assertEquals(seq("D27", 0xFE00), convert("D27\\R270", true, TextDirection.RIGHT_TO_LEFT));
        assertEquals(seq("D27"), convert("D27\\R90", true, TextDirection.RIGHT_TO_LEFT));
    }

    @Test
    public void rotationComesBeforeMirror() throws Exception {
        assertEquals(seq("D27", 0xFE00, MIRROR_HORIZONTALLY), convert("D27\\R90\\", true));
    }

    @Test
    public void signShading() throws Exception {
        // "12" is top start + top end: the modifier "damaged at top" (U+1344B).
        assertEquals(seq("A1", 0x1344B), convert("A1\\shading12", true));
    }

    @Test
    public void quadratShading() throws Exception {
        HieroglyphicText text = new MDCParserModelGenerator().parse("A1:X1");
        ((Cadrat) text.getChildAt(0)).setShading(ShadingCode.FULL);
        MdCToUnicodeConverter converter = new MdCToUnicodeConverter();
        converter.setIncludeFormatControlChars(true);
        assertEquals(seq(BEGIN_SEGMENT, "A1", VERTICAL_JOINER, "X1", END_SEGMENT, 0x13455),
                converter.convertToPlainUnicode(text));
    }

    @Test
    public void damageModifiersCoverU13447ToU13455() {
        java.util.Set<Integer> all = new java.util.HashSet<>();
        for (int s = 1; s <= ShadingCode.FULL; s++) {
            int cp = UnicodeHieroglyphControls.damageModifier(s);
            assertEquals(true, cp >= 0x13447 && cp <= 0x13455, "shading " + s);
            all.add(cp);
        }
        assertEquals(15, all.size());
        assertEquals(0x13447, UnicodeHieroglyphControls.damageModifier(ShadingCode.TOP_START));
        assertEquals(0x13448, UnicodeHieroglyphControls.damageModifier(ShadingCode.BOTTOM_START));
        assertEquals(0x1344A, UnicodeHieroglyphControls.damageModifier(ShadingCode.TOP_END));
        assertEquals(0x1344E, UnicodeHieroglyphControls.damageModifier(ShadingCode.BOTTOM_END));
        assertEquals(0x13455, UnicodeHieroglyphControls.damageModifier(ShadingCode.FULL));
        assertEquals(-1, UnicodeHieroglyphControls.damageModifier(ShadingCode.NONE));
    }
}

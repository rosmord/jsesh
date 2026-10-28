package jsesh.render.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.font.FontRenderContext;
import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.AlphabeticCharacter;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Soft wrapping of lines (`GeometrySpecification#wrapWidth()`).
public class LineWrapTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-3;
    private static final String LONG_LINE = "A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1-A1";

    private MDCView layout(String mdc, float wrapWidth, boolean justified) throws MDCSyntaxError {
        JSeshStyle style = JSeshStyle.DEFAULT.copy()
                .geometry(g -> g.wrapWidth(wrapWidth))
                .options(o -> o.justified(justified)).build();
        JSeshRenderContext context = new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts());
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), context,
                new JSeshTechRenderContext(FRC, 1.0));
    }

    /// The distinct vertical positions of the sub views.
    private static List<Double> rows(MDCView text) {
        return java.util.stream.IntStream.range(0, text.getNumberOfSubviews())
                .mapToObj(i -> text.getSubView(i).getPosition().y)
                .distinct().sorted().toList();
    }

    @Test
    public void noWrappingByDefault() throws MDCSyntaxError {
        MDCView text = layout(LONG_LINE, 0, false);
        assertEquals(1, rows(text).size());
    }

    @Test
    public void longLinesAreWrappedAndFitTheWidth() throws MDCSyntaxError {
        MDCView unwrapped = layout(LONG_LINE, 0, false);
        float width = (float) (unwrapped.getWidth() / 3);
        MDCView text = layout(LONG_LINE, width, false);
        assertTrue(rows(text).size() >= 3, "rows: " + rows(text));
        assertTrue(text.getWidth() <= width + EPSILON, text.getWidth() + " > " + width);
        // No sub view is added: model and view stay in step.
        assertEquals(unwrapped.getNumberOfSubviews(), text.getNumberOfSubviews());
        // Wrapped rows are read from top to bottom.
        for (int i = 1; i < text.getNumberOfSubviews(); i++) {
            assertTrue(text.getSubView(i).getPosition().y >= text.getSubView(i - 1).getPosition().y - EPSILON);
        }
    }

    @Test
    public void anElementWiderThanTheLineIsPlacedAlone() throws MDCSyntaxError {
        MDCView text = layout("A1-A1-A1", 1, false);
        assertEquals(3, rows(text).size());
    }

    @Test
    public void explicitLineBreaksStillBreak() throws MDCSyntaxError {
        MDCView text = layout("A1-A1!A1-A1", 10000, false);
        assertEquals(2, rows(text).size());
    }

    @Test
    public void kernedElementsAreNotSeparated() throws MDCSyntaxError {
        String mdc = LONG_LINE + "-w-%hkern-D58-" + LONG_LINE;
        MDCView unwrapped = layout(mdc, 0, false);
        int kerned = LONG_LINE.split("-").length + 2; // index of D58
        for (float width = 20; width < unwrapped.getWidth(); width += 7) {
            MDCView text = layout(mdc, width, false);
            assertEquals(text.getSubView(kerned - 1).getPosition().y, text.getSubView(kerned).getPosition().y,
                    EPSILON, "width " + width);
        }
    }

    @Test
    public void wrappedLinesAreJustifiedToTheWrappingWidthButParagraphEndsAreNot() throws MDCSyntaxError {
        MDCView unwrapped = layout(LONG_LINE, 0, false);
        float width = (float) (unwrapped.getWidth() / 3);
        MDCView plain = layout(LONG_LINE + "!A1-A1", width, false);
        MDCView justified = layout(LONG_LINE + "!A1-A1", width, true);
        int last = plain.getNumberOfSubviews() - 1;
        // The paragraph end is not stretched.
        assertEquals(plain.getSubView(last).getPosition().x, justified.getSubView(last).getPosition().x, EPSILON);
        // The first line is: its last sign moved to the right.
        int firstRowEnd = 0;
        while (plain.getSubView(firstRowEnd + 1).getPosition().y == plain.getSubView(0).getPosition().y) {
            firstRowEnd++;
        }
        assertTrue(justified.getSubView(firstRowEnd).getPosition().x >= plain.getSubView(firstRowEnd).getPosition().x - EPSILON);
    }

    private static final String SENTENCE = "+lthe quick brown fox jumps over the lazy dog, and then it sleeps+s";

    @Test
    public void alphabeticTextIsCutAtSpacesAndPunctuation() throws MDCSyntaxError {
        MDCView unwrapped = layout(SENTENCE, 0, false);
        float width = (float) (unwrapped.getWidth() / 3);
        MDCView text = layout(SENTENCE, width, false);
        assertTrue(rows(text).size() >= 3, "rows: " + rows(text));
        for (int i = 0; i + 1 < text.getNumberOfSubviews(); i++) {
            if (text.getSubView(i + 1).getPosition().y > text.getSubView(i).getPosition().y) {
                AlphabeticCharacter c = (AlphabeticCharacter) text.getSubView(i).getModel();
                assertTrue(TextRun.canBreakAfter(c), "cut after '" + c.getMdcText() + "' at " + i);
            }
        }
        assertEquals(unwrapped.getNumberOfSubviews(), text.getNumberOfSubviews());
    }

    @Test
    public void aWordWhichIsTooLongIsCutAnywhere() throws MDCSyntaxError {
        String mdc = "+lsupercalifragilisticexpialidocious+s";
        MDCView unwrapped = layout(mdc, 0, false);
        float width = (float) (unwrapped.getWidth() / 3);
        MDCView text = layout(mdc, width, false);
        assertTrue(rows(text).size() >= 3, "rows: " + rows(text));
        assertTrue(text.getWidth() <= width + EPSILON, text.getWidth() + " > " + width);
    }

    @Test
    public void wrappedTextKeepsItsReadingOrderInBothDirections() throws MDCSyntaxError {
        MDCView unwrapped = layout(SENTENCE, 0, false);
        float width = (float) (unwrapped.getWidth() / 3);
        MDCView text = layout(SENTENCE, width, false);
        for (int i = 1; i < text.getNumberOfSubviews(); i++) {
            MDCView previous = text.getSubView(i - 1);
            MDCView current = text.getSubView(i);
            if (current.getPosition().y == previous.getPosition().y) {
                assertTrue(current.getPosition().x >= previous.getPosition().x - EPSILON, "char " + i);
            }
        }
    }
}

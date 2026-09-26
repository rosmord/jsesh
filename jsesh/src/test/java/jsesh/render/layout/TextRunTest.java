package jsesh.render.layout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

public class TextRunTest {

    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final double EPSILON = 1e-3;

    @Test
    public void widthsAddUpToTheRunWidth() {
        Font font = new Font("Serif", Font.PLAIN, 12);
        TextRun run = TextRun.measure(0, 5, font, List.of("h", "e", "l", "l", "o"), false, FRC);
        double sum = 0;
        for (double w : run.widths()) {
            assertTrue(w >= 0);
            sum += w;
        }
        assertEquals(run.width(), sum, EPSILON);
        // Left to right: increasing offsets.
        for (int k = 1; k < 5; k++) {
            assertTrue(run.offsets()[k] > run.offsets()[k - 1]);
        }
    }

    @Test
    public void runIsNotWiderThanSeparateCharacters() {
        Font font = new Font("Serif", Font.PLAIN, 40);
        TextRun av = TextRun.measure(0, 2, font, List.of("A", "V"), false, FRC);
        TextRun a = TextRun.measure(0, 1, font, List.of("A"), false, FRC);
        TextRun v = TextRun.measure(0, 1, font, List.of("V"), false, FRC);
        // With kerning, "AV" is usually narrower; it is never wider.
        assertTrue(av.width() <= a.width() + v.width() + EPSILON);
    }

    @Test
    public void combiningSequenceIsOnePiece() {
        Font font = new Font("Serif", Font.PLAIN, 12);
        TextRun run = TextRun.measure(0, 2, font, List.of("i̓", "b"), false, FRC);
        assertTrue(run.widths()[0] > 0);
        assertTrue(run.offsets()[1] > run.offsets()[0]);
    }

    @Test
    public void mirroredOffsetsReverseTheOrder() {
        Font font = new Font("Serif", Font.PLAIN, 12);
        TextRun run = TextRun.measure(0, 3, font, List.of("a", "b", "c"), false, FRC);
        double[] mirrored = run.layoutOffsets(true);
        assertTrue(mirrored[0] > mirrored[1]);
        assertTrue(mirrored[1] > mirrored[2]);
        assertEquals(run.width() - run.widths()[0], mirrored[0], EPSILON);
    }

    // Layout of a whole text.

    private MDCView layout(String mdc, TextDirection direction, TextOrientation orientation) throws MDCSyntaxError {
        JSeshStyle style = JSeshStyle.DEFAULT.copy()
                .options(o -> o.textDirection(direction).textOrientation(orientation))
                .build();
        JSeshRenderContext renderContext = new JSeshRenderContext(style, PredefinedFonts.buildAllEmbeddedFonts());
        JSeshTechRenderContext tech = new JSeshTechRenderContext(FRC, 1.0);
        return new ViewBuilder().buildView(new MDCParserModelGenerator().parse(mdc), renderContext, tech);
    }

    /// x position of a subview on screen (ViewDrawer mirrors right-to-left views).
    private double screenX(MDCView parent, int i) {
        MDCView v = parent.getSubView(i);
        if (parent.getDirection().isLeftToRight()) {
            return v.getPosition().x;
        }
        return parent.getInternalWidth() - v.getPosition().x - v.getWidth();
    }

    @Test
    public void lettersAreContiguous() throws MDCSyntaxError {
        MDCView view = layout("A1-+labc+s-A1", TextDirection.LEFT_TO_RIGHT, TextOrientation.HORIZONTAL);
        for (int i = 1; i < 3; i++) {
            MDCView v = view.getSubView(i);
            assertEquals(v.getPosition().x + v.getWidth(), view.getSubView(i + 1).getPosition().x, EPSILON);
        }
    }

    @Test
    public void latinTextReadsLeftToRightInRightToLeftDocuments() throws MDCSyntaxError {
        MDCView view = layout("A1-+labc+s-A1", TextDirection.RIGHT_TO_LEFT, TextOrientation.HORIZONTAL);
        // The hieroglyphs go right to left...
        assertTrue(screenX(view, 0) > screenX(view, 4));
        // ... but the letters left to right.
        assertTrue(screenX(view, 1) < screenX(view, 2));
        assertTrue(screenX(view, 2) < screenX(view, 3));
        // and the text is between the two signs.
        assertTrue(screenX(view, 4) < screenX(view, 1));
        assertTrue(screenX(view, 3) < screenX(view, 0));
    }

    @Test
    public void textStaysOnOneLineInColumns() throws MDCSyntaxError {
        MDCView view = layout("A1-+labc+s-A1", TextDirection.LEFT_TO_RIGHT, TextOrientation.VERTICAL);
        double y = view.getSubView(1).getPosition().y;
        assertEquals(y, view.getSubView(2).getPosition().y, EPSILON);
        assertEquals(y, view.getSubView(3).getPosition().y, EPSILON);
        assertTrue(view.getSubView(1).getPosition().x < view.getSubView(2).getPosition().x);
        assertTrue(view.getSubView(4).getPosition().y > y);
    }

    @Test
    public void textInCartoucheIsContiguous() throws MDCSyntaxError {
        MDCView view = layout("<-+lab+s->", TextDirection.LEFT_TO_RIGHT, TextOrientation.HORIZONTAL);
        // Find the view of the cartouche's basic item list.
        MDCView list = findListWithText(view);
        MDCView a = list.getSubView(0);
        MDCView b = list.getSubView(1);
        assertEquals(a.getPosition().x + a.getWidth(), b.getPosition().x, EPSILON);
    }

    private MDCView findListWithText(MDCView v) {
        if (v.getNumberOfSubviews() > 0
                && v.getSubView(0).getModel() instanceof jsesh.model.AlphabeticCharacter) {
            return v;
        }
        for (int i = 0; i < v.getNumberOfSubviews(); i++) {
            MDCView r = findListWithText(v.getSubView(i));
            if (r != null) {
                return r;
            }
        }
        return null;
    }
}

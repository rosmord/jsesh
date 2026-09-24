package jsesh.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import jsesh.parser.ast.AstAbsoluteGroup;
import jsesh.parser.ast.AstAlphabeticText;
import jsesh.parser.ast.AstCadrat;
import jsesh.parser.ast.AstCartouche;
import jsesh.parser.ast.AstDocument;
import jsesh.parser.ast.AstHBox;
import jsesh.parser.ast.AstHieroglyph;
import jsesh.parser.ast.AstHorizontalListElement;
import jsesh.parser.ast.AstModifier;
import jsesh.parser.ast.AstNode;
import jsesh.parser.ast.AstToggle;
import jsesh.parser.ast.WordEnding;
import jsesh.parser.lexer.ToggleType;

/**
 * Structural tests for {@link MDCParserAstGenerator}, in the same spirit as
 * {@link jsesh.mdcreader.MDCParserModelGeneratorTest}, but checking that the AST stays a
 * literal record of what was parsed (e.g. toggles are kept as nodes, instead
 * of being folded into item state as {@link jsesh.mdcreader.AstModelBuilder} does).
 */
public class MDCParserAstGeneratorTest {

    private AstDocument parse(String mdc) throws MDCSyntaxError {
        return new MDCParserAstGenerator().parse(mdc);
    }

    private AstHieroglyph hieroglyphAt(AstHBox hBox, int i) {
        return (AstHieroglyph) hBox.elements().get(i);
    }

    @Test
    public void testEmptyString() throws MDCSyntaxError {
        AstDocument document = parse("");
        assertEquals(0, document.topItems().items().size());
    }

    @Test
    public void testJuxtaposition_threeTopItems() throws MDCSyntaxError {
        AstDocument document = parse("i-w-r");
        List<AstNode> items = document.topItems().items();
        assertEquals(3, items.size());

        String[] expectedCodes = {"i", "w", "r"};
        for (int i = 0; i < 3; i++) {
            AstCadrat cadrat = (AstCadrat) items.get(i);
            assertEquals(1, cadrat.hBoxes().size());
            AstHBox hBox = cadrat.hBoxes().get(0);
            assertEquals(1, hBox.elements().size());
            assertEquals(expectedCodes[i], hieroglyphAt(hBox, 0).code());
        }
    }

    @Test
    public void testSuperposition_sameHBox() throws MDCSyntaxError {
        AstDocument document = parse("p*t");
        List<AstNode> items = document.topItems().items();
        assertEquals(1, items.size());

        AstCadrat cadrat = (AstCadrat) items.get(0);
        assertEquals(1, cadrat.hBoxes().size());
        AstHBox hBox = cadrat.hBoxes().get(0);
        assertEquals(2, hBox.elements().size());
        assertEquals("p", hieroglyphAt(hBox, 0).code());
        assertEquals("t", hieroglyphAt(hBox, 1).code());
    }

    @Test
    public void testStacking_twoHBoxes() throws MDCSyntaxError {
        AstDocument document = parse("a:m");
        AstCadrat cadrat = (AstCadrat) document.topItems().items().get(0);
        assertEquals(2, cadrat.hBoxes().size());
        assertEquals("a", hieroglyphAt(cadrat.hBoxes().get(0), 0).code());
        assertEquals("m", hieroglyphAt(cadrat.hBoxes().get(1), 0).code());
    }

    @Test
    public void testCodesAreNotCanonicalized() throws MDCSyntaxError {
        AstDocument document = parse("i");
        AstCadrat cadrat = (AstCadrat) document.topItems().items().get(0);
        assertEquals("i", hieroglyphAt(cadrat.hBoxes().get(0), 0).code());
    }

    @Test
    public void testModifiers() throws MDCSyntaxError {
        AstDocument document = parse("m\\det\\col50");
        AstCadrat cadrat = (AstCadrat) document.topItems().items().get(0);
        AstHieroglyph hieroglyph = hieroglyphAt(cadrat.hBoxes().get(0), 0);
        assertEquals("m", hieroglyph.code());

        List<AstModifier> modifiers = hieroglyph.modifiers().modifiers();
        assertEquals(2, modifiers.size());
        assertEquals("det", modifiers.get(0).name());
        assertNull(modifiers.get(0).value());
        assertEquals("col", modifiers.get(1).name());
        assertEquals(Integer.valueOf(50), modifiers.get(1).value());
    }

    @Test
    public void testWordEnd() throws MDCSyntaxError {
        // "_" right after a sign is a meaningful (word-ending) space.
        AstDocument document = parse("i_w");
        AstCadrat cadrat = (AstCadrat) document.topItems().items().get(0);
        AstHieroglyph hieroglyph = hieroglyphAt(cadrat.hBoxes().get(0), 0);
        assertEquals(WordEnding.WORD_END, hieroglyph.endingCode());
    }

    /**
     * A hieroglyph's explicit position ("{@code {{x,y,scale}}}") is read
     * as part of the {@code hieroglyph} production; this checks it lands
     * correctly on the finished, immutable {@link AstHieroglyph} record.
     */
    @Test
    public void testExplicitPosition() throws MDCSyntaxError {
        AstDocument document = parse("A1&&B1{{100,200,50}}");
        AstCadrat cadrat = (AstCadrat) document.topItems().items().get(0);
        AstHorizontalListElement element = cadrat.hBoxes().get(0).elements().get(0);
        assertTrue(element instanceof AstAbsoluteGroup);
        AstAbsoluteGroup absoluteGroup = (AstAbsoluteGroup) element;
        assertEquals(2, absoluteGroup.hieroglyphs().size());
        AstHieroglyph positioned = absoluteGroup.hieroglyphs().get(1);
        assertEquals(100, positioned.x());
        assertEquals(200, positioned.y());
        assertEquals(50, positioned.scale());
    }

    @Test
    public void testCartouche() throws MDCSyntaxError {
        AstDocument document = parse("<A1>");
        AstCadrat cadrat = (AstCadrat) document.topItems().items().get(0);
        AstHBox hBox = cadrat.hBoxes().get(0);
        AstHorizontalListElement element = hBox.elements().get(0);
        assertTrue(element instanceof AstCartouche);
        AstCartouche cartouche = (AstCartouche) element;
        assertEquals(1, cartouche.startPart());
        assertEquals(2, cartouche.endPart());
        assertEquals(1, cartouche.content().items().size());
    }

    /**
     * The distinguishing feature of the AST, compared to the document model:
     * a toggle is kept as its own node, in the position it was parsed at,
     * rather than being folded into hidden red/shaded state on later items.
     */
    @Test
    public void testToggleIsKeptAsANode() throws MDCSyntaxError {
        AstDocument document = parse("$r-i-$b-w");
        List<AstNode> items = document.topItems().items();
        assertEquals(4, items.size());
        assertTrue(items.get(0) instanceof AstToggle);
        assertEquals(ToggleType.RED, ((AstToggle) items.get(0)).toggleType());
        assertTrue(items.get(1) instanceof AstCadrat);
        assertTrue(items.get(2) instanceof AstToggle);
        assertEquals(ToggleType.BLACK, ((AstToggle) items.get(2)).toggleType());
        assertTrue(items.get(3) instanceof AstCadrat);
    }

    @Test
    public void testAlphabeticText() throws MDCSyntaxError {
        AstDocument document = parse("+lhello");
        List<AstNode> items = document.topItems().items();
        assertEquals(1, items.size());
        AstAlphabeticText text = (AstAlphabeticText) items.get(0);
        assertEquals('l', text.scriptCode());
        assertEquals("hello", text.text());
    }

    /**
     * Demonstrates that being sealed lets a consumer switch over node types
     * exhaustively (compiler-checked), as an alternative to {@link jsesh.parser.ast.AstVisitor}.
     */
    @Test
    public void testExhaustiveSwitchOverTopLevelNode() throws MDCSyntaxError {
        AstDocument document = parse("$r");
        AstNode node = document.topItems().items().get(0);
        String description = describe(node);
        assertEquals("toggle: RED", description);
    }

    private static String describe(AstNode node) {
        return switch (node) {
            case AstToggle t -> "toggle: " + t.toggleType().name();
            case AstCadrat c -> "cadrat";
            default -> "other";
        };
    }

    @Test
    public void testSyntaxError_unclosedCartouche() {
        assertThrows(MDCSyntaxError.class, () -> parse("<A1"));
    }

    @Test
    public void testSyntaxError_leadingColon() {
        assertThrows(MDCSyntaxError.class, () -> parse(":a"));
    }
}

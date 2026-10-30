package jsesh.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import jsesh.parser.ast.AstCadrat;
import jsesh.parser.ast.AstDocument;
import jsesh.parser.ast.AstHieroglyph;
import jsesh.parser.ast.AstModifierList;

/// Tests of the old rotation scheme.
public class MDCParserOldRotationTest {
    private AstDocument parse(String mdc) throws MDCSyntaxError {
        return new MDCParser().parse(mdc);
    }

    private AstCadrat quadrat(AstDocument document, int index) {
        return (AstCadrat) document.topItems().items().get(index);
    }

    @Test
    public void simpleRotation() throws MDCSyntaxError {
        AstCadrat quadrat = quadrat(parse("A1\\R10"), 0);
        AstHieroglyph h = (AstHieroglyph) quadrat.hBoxes().get(0).elements().get(0);
        AstModifierList modifiers = h.modifiers();
        boolean res = modifiers.modifiers().stream().anyMatch(m -> m.name().equals("R") && m.value() == 10);
        assertTrue(res);
    }

    @Test
    public void negativeRotation() throws MDCSyntaxError {
        AstCadrat quadrat = quadrat(parse("A1\\R-10"), 0);
        AstHieroglyph h = (AstHieroglyph) quadrat.hBoxes().get(0).elements().get(0);
        AstModifierList modifiers = h.modifiers();
        boolean res = modifiers.modifiers().stream().anyMatch(m -> m.name().equals("R") && m.value() == -10);
        assertTrue(res);
    }

    @Test
    public void negativeRotationAndSecondQuadrat() throws MDCSyntaxError {
        AstDocument parsedText = parse("A1\\R-10-B1");
        AstCadrat quadrat1 = quadrat(parsedText, 0);
        AstHieroglyph h = (AstHieroglyph) quadrat1.hBoxes().get(0).elements().get(0);
        AstModifierList modifiers = h.modifiers();
        boolean res = modifiers.modifiers().stream().anyMatch(m -> m.name().equals("R") && m.value() == -10);

        AstCadrat quadrat2 = quadrat(parsedText, 1);
        AstHieroglyph h2 = (AstHieroglyph) quadrat2.hBoxes().get(0).elements().get(0);
        assertEquals("B1", h2.code());
        assertTrue(res);
    }

}

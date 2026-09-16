package jsesh.parser.lex;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import java_cup.runtime.Symbol;

/**
 * Test of the lexer behaviour.
 * LexTest
 */
public class LexTest {
    @Test
    public void testRoundEnclosureG() throws IOException {
        StringReader input = new StringReader("<G");
        MDCLex lex = new MDCLex(input);
        Symbol token = lex.next_token();
        assert (token.value instanceof MDCStartOldCartouche);
        MDCStartOldCartouche start = (MDCStartOldCartouche) token.value;
        assertAll(
                "",
                () -> assertEquals(MDCCartoucheType.CIRCULAR_FENCE, start.getCartoucheType()),
                () -> assertEquals('a', start.getPart()));
    }

    @Test
    public void testRoundEnclosureg1() throws IOException {
        StringReader input = new StringReader("<g");
        MDCLex lex = new MDCLex(input);
        Symbol token = lex.next_token();
        assert (token.value instanceof MDCCartouche);
        MDCCartouche start = (MDCCartouche) token.value;        
        assertAll(
                "",
                () -> assertEquals(MDCCartoucheType.CIRCULAR_FENCE, start.getCartoucheType()),
                () -> assertEquals(1, start.getPart()));
    }

       @Test
    public void testRoundEnclosureg0() throws IOException {
        StringReader input = new StringReader("<g0");
        MDCLex lex = new MDCLex(input);
        Symbol token = lex.next_token();
        assert (token.value instanceof MDCCartouche);
        MDCCartouche start = (MDCCartouche) token.value;        
        assertAll(
                "",
                () -> assertEquals(MDCCartoucheType.CIRCULAR_FENCE, start.getCartoucheType()),
                () -> assertEquals(0, start.getPart()));
    }
}

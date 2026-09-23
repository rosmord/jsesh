package jsesh.parser.handmade;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import jsesh.parser.MDCSyntaxError;
import jsesh.parser.ast.AstDocument;

/**
 * A large corpus of Manuel de Codage edge cases -- originally assembled to
 * check {@link MDCHandmadeParser} against the (now retired) CUP-generated
 * parser, construct by construct. Kept as a crash-safety regression test:
 * every snippet must either parse successfully or be rejected with a clean
 * {@link MDCSyntaxError}, never anything else (NPE, StackOverflowError...).
 * Each snippet is tried with philological markers both as signs and as
 * grouping constructs.
 */
public class MDCHandmadeParserEquivalenceTest {

    @ParameterizedTest
    @ValueSource(strings = {
            // Taken from the existing parser tests
            "", ":a", "!!", "!", "!=200%", "?123", "(x)", "{l100,200}", "{L50,300}", "%[id=1]", "%clear",
            "+lhello", "+s", "+tnfr", "<1--0>-ra-mn:n-xpr-<0--2>", "<1--0>", "<A1", "<A1>", "<sA1>", "=n",
            "|note", "$r-$b-$-?-??-^-#b-#e", "$r-i-$b-w", "$r", "a:m", "a##b", "A1&&B1",
            "A1&&B1{{100,200,50}}", "i__w", "i_w", "i-w-r:a-C1-m-pt:p*t", "i-w-r", "i", "i#1", "i#1234",
            "m\\col50", "m\\det", "m\\det\\col50", "p*t", "p&t&n", "quadrat(x)[foo]", "t^^w", "t^^w&&&n",
            "w&&&t", "zone", "zone[id=1]", "[(x)]", "[[x]]",
            // Separators
            "-", "-i", "i-", "-i-", "i--w", "i - w", "i -", "i- -w",
            // Groups and ligatures
            "(i-w)", "(i-w-)", "(-i-w)", "()", "(i:w)*n", "i*(w:n)", "(i*(w:n):a)", "a#b", "a#b#c",
            "a&b&&c", "a&&b&c", "a&b##c", "i&w*n", "t^^w&n", "t&w^^n", "t^^w&&&n^^a", "w&&&t&&&n",
            "w&&&t^^n", "t&n^^w&&&x&y", "(a:b)^^w&&&(c:d)", "<s-A1-s>^^w", "t^^<A1>", "t^^",
            "&&&t", "^^w", "a&", "a&&", "a##", "a:", "a*", ":", "*a",
            // Hieroglyph decorations
            "=A1\\R90{{0,0,100}} B1", "A1{{1,2,3}}_B1", "A1{{1,2}}", "A1{{1,2,3,4}}", "A1\\?", "A1\\R-90",
            "A1\\ B1", "A1 \\R90", "=", "==A1", "A1 = B1", "o-O-.-..-//-v/-/-h/", "\"abc\"-\"a\\]\"",
            "`", "A1 B1  C1", "A1__B1", "A1 :B1", "A1: B1",
            // Cartouches
            "<-ra-mn-xpr->", "<s-A1-s>\\R90", "<s-A1-s>\\R90 B1", "<A1>\\R90", "<h1-A1-h2>",
            "<f-A1-f>", "<S-A1->", "<Sb-A1->", "<Hm-A1->", "<Fe-A1->", "<G-A1>", "<b-A1->", "<m-A1->",
            "<e-A1->", "<2-A1-1>", "<A1:B1>*C1", "<>", "<->", "<-<-A1->->", "<-A1", "A1>", "<0-A1-0>#12",
            // Philology (as signs or as groups depending on the setting)
            "[[-A1-]]", "[{-A1-}]", "[\"-A1-\"]", "['-A1-']", "[&-A1-&]", "[?-A1-?]", "[[A1]]*B1",
            "[[A1-]]", "[[-A1-?]", "[[", "]]", "[[A1:B1]]:C1", "[[+lbla-A1-$r-+s]]",
            // Shading and toggles
            "A1#1234", "A1#12-B1#34", "A1#-B1", "A1# B1", "#-A1", "-#-A1", "A1-#-B1", "A1-#b-B1-#e",
            "A1-#", "#", "A1#", "A1 #b B1", "(A1#12)", "A1:B1#", "(A1)#23", "A1-$r-B1-$b", "A1-?-B1",
            // Top-level items
            "A1-!-B1", "A1!B1", "A1 ! B1", "A1-!!-B1", "?10-A1", "?10?20", "%[a,b=c,d=3]", "%", "%[]",
            "%[a,]", "%[a=]", "%[=3]", "%clear-A1", "{l0,100}-A1", "zone[a,b=c,d=3]-A1", "zone-zone",
            "zone A1", "quadrat(A1:B1)", "quadrat(A1:B1)#12", "quadrat(A1)[a=1]#34", "quadrat A1",
            "quadrat(A1", "(quadrat(A1))", "+lbla-A1-+s-B1", "+lbla+s", "+i\\+escaped+s-A1",
            "|sup-A1", "A1|sup", "+s+s",
            // Items allowed in basic item lists but not at top level, and vice versa
            "(A1-!-B1)", "(A1-%clear)", "(+lbla-A1)", "(A1-$r-B1)", "(|sup)", "(zone)", "<A1-!-B1>",
            // Unknown and weird characters
            "A1~B1", "@", "@a", "A1;B1", "A1,B1", "A1{{a,b,c}}", "]", "[", "[a]", "{{", "}}",
            // Multiline
            "A1\nB1", "A1-\n-B1", "A1 \n B1", "A1\n\nB1",
    })
    public void parsesCleanly(String mdc) {
        for (boolean philologyAsSigns : new boolean[] { true, false }) {
            // Either result is fine: what matters is that parsing never
            // throws anything other than a clean MDCSyntaxError.
            handmadeParse(mdc, philologyAsSigns);
        }
    }

    private static String handmadeParse(String mdc, boolean philologyAsSigns) {
        MDCHandmadeParser parser = new MDCHandmadeParser();
        parser.setPhilologyAsSigns(philologyAsSigns);
        try {
            AstDocument doc = parser.parse(mdc);
            return doc.toString();
        } catch (MDCSyntaxError e) {
            return "ERROR";
        }
    }
}

package jsesh.parser;

import java.io.Reader;

import jsesh.parser.ast.AstDocument;

/**
 * A parser for MdC code which generates a literal AST (see
 * {@link jsesh.parser.ast}) for the code, instead of the usable document
 * model that {@link MDCParserModelGenerator} builds.
 *
 * @author rosmord
 * @see jsesh.parser.ast.AstDocument
 */
public class MDCParserAstGenerator {

    private final MDCParser parser = new MDCParser();

    public AstDocument parse(Reader in) throws MDCSyntaxError {
        return parser.parse(in);
    }

    public AstDocument parse(String text) throws MDCSyntaxError {
        return parser.parse(text);
    }

    /**
     * @return true if we are debugging.
     */
    public boolean isDebug() {
        return parser.isDebug();
    }

    /**
     * if true, philological markers, such as [[ and ]], are considered
     * as simple signs, and not as constructs.
     * @return true if philological markers are considered as simple signs.
     */
    public boolean isPhilologyAsSigns() {
        return parser.isPhilologyAsSigns();
    }

    public void setDebug(boolean v) {
        parser.setDebug(v);
    }

    public void setPhilologyAsSigns(boolean v) {
        parser.setPhilologyAsSigns(v);
    }
}

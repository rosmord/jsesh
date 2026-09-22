package jsesh.parser;

import java.io.Reader;
import java.io.StringReader;

import jsesh.parser.ast.AstBuilder;
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

    private final MDCParserFacade facade;

    public MDCParserAstGenerator() {
        facade = new MDCParserFacade(new AstBuilder());
    }

    public AstDocument parse(Reader in) throws MDCSyntaxError {
        facade.parse(in);
        return ((AstBuilder) facade.getBuilder()).getResult();
    }

    public AstDocument parse(String text) throws MDCSyntaxError {
        facade.parse(new StringReader(text));
        return ((AstBuilder) facade.getBuilder()).getResult();
    }

    /**
     * @return true if we are debugging.
     */
    public boolean isDebug() {
        return facade.isDebug();
    }

    /**
     * if true, philological markers, such as [[ and ]], are considered
     * as simple signs, and not as constructs.
     * @return true if philological markers are considered as simple signs.
     */
    public boolean isPhilologyAsSigns() {
        return facade.isPhilologyAsSigns();
    }

    public void setDebug(boolean v) {
        facade.setDebug(v);
    }

    public void setPhilologyAsSigns(boolean v) {
        facade.setPhilologyAsSigns(v);
    }
}

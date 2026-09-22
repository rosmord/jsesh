package jsesh.parser.ast;

import jsesh.model.api.PhilologyInterface;

/**
 * A philological bracket group, e.g. {@code [[...]]} for erased signs or
 * {@code [(...)]} for a minor addition.
 * <p>The opening and closing codes are normally equal, but both are kept as
 * parsed rather than assuming so.
 *
 * @author rosmord
 * @see jsesh.model.constants.SymbolCodes
 */
public record AstPhilology(int openingCode, int closingCode, AstBasicItemList content)
        implements AstInnerGroup, PhilologyInterface {

    /**
     * Hand-builds a philology group whose opening and closing codes match
     * (the normal case), from its content. For tests.
     */
    public static AstPhilology of(int code, AstNode... content) {
        return new AstPhilology(code, code, AstBasicItemList.of(content));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitPhilology(this);
    }
}

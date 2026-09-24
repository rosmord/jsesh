package jsesh.parser.ast;

import jsesh.parser.lexer.PhilologyKind;

/**
 * A philological bracket group, e.g. {@code [[...]]} for erased signs or
 * {@code [(...)]} for a minor addition.
 * <p>The opening and closing kinds are normally equal, but both are kept as
 * parsed rather than assuming so.
 *
 * @author rosmord
 */
public record AstPhilology(PhilologyKind openingKind, PhilologyKind closingKind, AstBasicItemList content)
        implements AstInnerGroup {

    /**
     * Hand-builds a philology group whose opening and closing codes match
     * (the normal case), from its content. For tests.
     */
    public static AstPhilology of(PhilologyKind kind, AstNode... content) {
        return new AstPhilology(kind, kind, AstBasicItemList.of(content));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitPhilology(this);
    }
}

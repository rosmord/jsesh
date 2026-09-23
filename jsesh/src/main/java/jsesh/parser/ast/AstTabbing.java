package jsesh.parser.ast;

/**
 * A complex tabulation: {@code %[...]}, with options such as {@code id},
 * {@code orientation} and {@code justification}.
 * <p>Unlike {@link jsesh.parser.AstModelBuilder}, which interprets those
 * options immediately (defaulting missing ones), this keeps the raw option
 * list as parsed; interpreting it is left to the consumer.
 *
 * @param options the raw options given between braces.
 * @author rosmord
 */
public record AstTabbing(AstOptionList options) implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitTabbing(this);
    }
}

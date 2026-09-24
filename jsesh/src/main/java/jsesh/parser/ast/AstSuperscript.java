package jsesh.parser.ast;

/**
 * A superscript text annotation.
 * <p>The text is kept exactly as the parser read it, including any "\"
 * protection characters in front of "\" and "-"; unescaping them, as
 * {@link jsesh.mdcreader.AstModelBuilder} does, is left to the consumer.
 *
 * @author rosmord
 */
public record AstSuperscript(String text) implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitSuperscript(this);
    }
}

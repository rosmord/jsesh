package jsesh.parser.ast;

/**
 * A page break.
 *
 * @author rosmord
 */
public record AstPageBreak() implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitPageBreak(this);
    }
}

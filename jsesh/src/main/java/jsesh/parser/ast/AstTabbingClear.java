package jsesh.parser.ast;

/**
 * The {@code %clear} construct, clearing all current tab stops.
 *
 * @author rosmord
 */
public record AstTabbingClear() implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitTabbingClear(this);
    }
}

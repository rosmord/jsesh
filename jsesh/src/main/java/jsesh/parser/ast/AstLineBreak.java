package jsesh.parser.ast;

/**
 * A line break.
 *
 * @param skip the vertical skip, as a percentage of line height (100 is a
 * normal skip).
 * @author rosmord
 */
public record AstLineBreak(int skip) implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitLineBreak(this);
    }
}

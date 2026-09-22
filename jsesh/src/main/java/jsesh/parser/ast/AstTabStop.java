package jsesh.parser.ast;

/**
 * A tab stop (Winglyph-style absolute tabulation).
 *
 * @param stopWidth the position, in tab units.
 * @author rosmord
 */
public record AstTabStop(int stopWidth) implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitTabStop(this);
    }
}

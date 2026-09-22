package jsesh.parser.ast;

/**
 * A horizontal rule.
 *
 * @param lineType 'l' for a single line, 'L' for a double line.
 * @param startPos start of the rule, in tab units from the left edge of the page.
 * @param endPos end of the rule, in tab units.
 * @author rosmord
 */
public record AstHRule(char lineType, int startPos, int endPos) implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitHRule(this);
    }
}

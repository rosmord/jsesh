package jsesh.parser.ast;


/**
 * Two hieroglyphs drawn one over the other ({@code a##b}).
 *
 * @author rosmord
 */
public record AstOverwrite(AstHieroglyph first, AstHieroglyph second) implements AstInnerGroup {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitOverwrite(this);
    }
}

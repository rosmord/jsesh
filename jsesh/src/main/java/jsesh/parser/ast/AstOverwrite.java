package jsesh.parser.ast;

import jsesh.model.api.OverwriteInterface;

/**
 * Two hieroglyphs drawn one over the other ({@code a##b}).
 *
 * @author rosmord
 */
public record AstOverwrite(AstHieroglyph first, AstHieroglyph second) implements AstInnerGroup, OverwriteInterface {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitOverwrite(this);
    }
}

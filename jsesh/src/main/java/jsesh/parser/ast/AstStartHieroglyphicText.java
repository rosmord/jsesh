package jsesh.parser.ast;

/**
 * The "+s" marker, toggling between latin and hieroglyphic text. Kept purely
 * so the AST is a faithful record of the source; {@link jsesh.mdcreader.AstModelBuilder}
 * discards this construct entirely when building the model.
 *
 * @author rosmord
 */
public record AstStartHieroglyphicText() implements AstNode {

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitStartHieroglyphicText(this);
    }
}

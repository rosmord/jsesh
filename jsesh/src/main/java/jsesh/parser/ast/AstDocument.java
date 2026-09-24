package jsesh.parser.ast;


/**
 * The root of a parsed Manuel de Codage text.
 *
 * @author rosmord
 */
public record AstDocument(AstTopItemList topItems) implements AstNode {

    /**
     * Hand-builds a document from its top-level items, for tests.
     */
    public static AstDocument of(AstNode... topItems) {
        return new AstDocument(AstTopItemList.of(topItems));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitDocument(this);
    }
}

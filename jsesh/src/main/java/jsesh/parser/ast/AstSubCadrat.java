package jsesh.parser.ast;


/**
 * A parenthesized group, embedding a {@link AstBasicItemList} (which may
 * itself contain cadrats) as a single element of an {@link AstHBox}.
 *
 * @author rosmord
 */
public record AstSubCadrat(AstBasicItemList content) implements AstInnerGroup {

    /**
     * Hand-builds a sub-cadrat from its content, for tests.
     */
    public static AstSubCadrat of(AstNode... content) {
        return new AstSubCadrat(AstBasicItemList.of(content));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitSubCadrat(this);
    }
}

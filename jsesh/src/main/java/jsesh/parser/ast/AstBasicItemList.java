package jsesh.parser.ast;

import java.util.List;


/**
 * A list of basic items: the restricted set of constructs (cadrats, text,
 * toggles) that can appear inside a cartouche, a sub-cadrat or a philology
 * group, as well as at top level.
 * <p>Built incrementally, via left-recursive grammar rules of unbounded
 * arity, hence the {@link #of} factory rather than a fixed-arity constructor
 * call at each parse site.
 *
 * @author rosmord
 */
public record AstBasicItemList(List<AstNode> items) implements AstNode {

    public AstBasicItemList {
        items = List.copyOf(items);
    }

    /**
     * Hand-builds a basic item list from its items, for tests.
     */
    public static AstBasicItemList of(AstNode... items) {
        return new AstBasicItemList(List.of(items));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitBasicItemList(this);
    }
}

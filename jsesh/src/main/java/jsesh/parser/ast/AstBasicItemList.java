package jsesh.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A list of basic items: the restricted set of constructs (cadrats, text,
 * toggles) that can appear inside a cartouche, a sub-cadrat or a philology
 * group, as well as at top level.
 * <p>Built incrementally, via left-recursive grammar rules of unbounded
 * arity; unlike most other AST nodes it cannot be a record. Structural
 * equality ({@link #equals}/{@link #hashCode}) is implemented by hand to
 * compensate.
 *
 * @author rosmord
 */
public final class AstBasicItemList implements AstNode {

    private final List<AstNode> items = new ArrayList<>();

    void addItem(AstNode item) {
        items.add(item);
    }

    public List<AstNode> items() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Hand-builds a basic item list from its items, for tests.
     */
    public static AstBasicItemList of(AstNode... items) {
        AstBasicItemList result = new AstBasicItemList();
        for (AstNode item : items) {
            result.addItem(item);
        }
        return result;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitBasicItemList(this);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof AstBasicItemList other && items.equals(other.items));
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    @Override
    public String toString() {
        return "AstBasicItemList" + items;
    }
}

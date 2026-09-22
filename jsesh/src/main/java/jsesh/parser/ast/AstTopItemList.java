package jsesh.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jsesh.model.api.TopItemListInterface;

/**
 * A list of top-level items, as they appear directly in a text line: cadrats,
 * cartouches, rules, breaks, toggles, zone markers, tabbing, and so on.
 * <p>Built incrementally, via left-recursive grammar rules of unbounded
 * arity; unlike most other AST nodes it cannot be a record. Structural
 * equality ({@link #equals}/{@link #hashCode}) is implemented by hand to
 * compensate.
 *
 * @author rosmord
 */
public final class AstTopItemList implements AstNode, TopItemListInterface {

    private final List<AstNode> items = new ArrayList<>();

    void addItem(AstNode item) {
        items.add(item);
    }

    public List<AstNode> items() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Hand-builds a top item list from its items, for tests.
     */
    public static AstTopItemList of(AstNode... items) {
        AstTopItemList result = new AstTopItemList();
        for (AstNode item : items) {
            result.addItem(item);
        }
        return result;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitTopItemList(this);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof AstTopItemList other && items.equals(other.items));
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    @Override
    public String toString() {
        return "AstTopItemList" + items;
    }
}

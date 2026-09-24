package jsesh.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A horizontal list of {@link AstHorizontalListElement}s: hieroglyphs,
 * ligatures, complex ligatures, cartouches, sub-cadrats, philology groups,
 * overwrites and absolute groups, juxtaposed or superposed with "*".
 * <p>Built incrementally, via a left-recursive grammar rule of unbounded
 * arity; unlike most other AST nodes it cannot be a record. Structural
 * equality ({@link #equals}/{@link #hashCode}) is implemented by hand to
 * compensate.
 *
 * @author rosmord
 */
public final class AstHBox implements AstNode {

    private final List<AstHorizontalListElement> elements = new ArrayList<>();

    void addElement(AstHorizontalListElement element) {
        elements.add(element);
    }

    public List<AstHorizontalListElement> elements() {
        return Collections.unmodifiableList(elements);
    }

    /**
     * Hand-builds an hbox from its elements, for tests.
     */
    public static AstHBox of(AstHorizontalListElement... elements) {
        AstHBox result = new AstHBox();
        for (AstHorizontalListElement element : elements) {
            result.addElement(element);
        }
        return result;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitHBox(this);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof AstHBox other && elements.equals(other.elements));
    }

    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    @Override
    public String toString() {
        return "AstHBox" + elements;
    }
}

package jsesh.parser.ast;

import java.util.List;


/**
 * A horizontal list of {@link AstHorizontalListElement}s: hieroglyphs,
 * ligatures, complex ligatures, cartouches, sub-cadrats, philology groups,
 * overwrites and absolute groups, juxtaposed or superposed with "*".
 * <p>Built incrementally, via a left-recursive grammar rule of unbounded
 * arity, hence the {@link #of} factory rather than a fixed-arity constructor
 * call at each parse site.
 *
 * @author rosmord
 */
public record AstHBox(List<AstHorizontalListElement> elements) implements AstNode {

    public AstHBox {
        elements = List.copyOf(elements);
    }

    /**
     * Hand-builds an hbox from its elements, for tests.
     */
    public static AstHBox of(AstHorizontalListElement... elements) {
        return new AstHBox(List.of(elements));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitHBox(this);
    }
}

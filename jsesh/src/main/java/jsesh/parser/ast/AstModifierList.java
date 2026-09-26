package jsesh.parser.ast;

import java.util.List;


/**
 * The list of modifiers attached to a hieroglyph, in parse order.
 * <p>Unlike {@link jsesh.model.ModifiersList}, this is a literal record: it
 * keeps every modifier exactly as parsed, including duplicates, instead of
 * merging repeated modifiers or giving special treatment to some of them.
 * <p>Built incrementally, via a left-recursive grammar rule of unbounded
 * arity, hence the {@link #of} factory rather than a fixed-arity constructor
 * call at each parse site.
 *
 * @author rosmord
 */
public record AstModifierList(List<AstModifier> modifiers) implements AstNode {

    public AstModifierList {
        modifiers = List.copyOf(modifiers);
    }

    /**
     * Hand-builds a modifier list, for tests. See {@link AstModifier#flag}
     * and {@link AstModifier#of}.
     */
    public static AstModifierList of(AstModifier... modifiers) {
        return new AstModifierList(List.of(modifiers));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitModifierList(this);
    }
}

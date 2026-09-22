package jsesh.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jsesh.model.api.ModifierListInterface;

/**
 * The list of modifiers attached to a hieroglyph, in parse order.
 * <p>Unlike {@link jsesh.model.ModifiersList}, this is a literal record: it
 * keeps every modifier exactly as parsed, including duplicates, instead of
 * merging repeated modifiers or giving special treatment to some of them.
 * <p>Built incrementally, via a left-recursive grammar rule of unbounded
 * arity; unlike most other AST nodes it cannot be a record. Structural
 * equality ({@link #equals}/{@link #hashCode}) is implemented by hand to
 * compensate.
 *
 * @author rosmord
 */
public final class AstModifierList implements AstNode, ModifierListInterface {

    private final List<AstModifier> modifiers = new ArrayList<>();

    void addModifier(String name, Integer value) {
        modifiers.add(new AstModifier(name, value));
    }

    public List<AstModifier> modifiers() {
        return Collections.unmodifiableList(modifiers);
    }

    /**
     * Hand-builds a modifier list, for tests. See {@link AstModifier#flag}
     * and {@link AstModifier#of}.
     */
    public static AstModifierList of(AstModifier... modifiers) {
        AstModifierList result = new AstModifierList();
        for (AstModifier modifier : modifiers) {
            result.modifiers.add(modifier);
        }
        return result;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitModifierList(this);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof AstModifierList other && modifiers.equals(other.modifiers));
    }

    @Override
    public int hashCode() {
        return modifiers.hashCode();
    }

    @Override
    public String toString() {
        return "AstModifierList" + modifiers;
    }
}

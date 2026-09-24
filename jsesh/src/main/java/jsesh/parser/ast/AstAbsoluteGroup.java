package jsesh.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A group of signs with explicit placement, built with "&amp;&amp;" (each
 * hieroglyph then typically carries a {@code {{x,y,scale}}} position).
 * <p>Built incrementally, via a left-recursive grammar rule of unbounded
 * arity; unlike most other AST nodes it cannot be a record. Structural
 * equality ({@link #equals}/{@link #hashCode}) is implemented by hand to
 * compensate.
 *
 * @author rosmord
 */
public final class AstAbsoluteGroup implements AstInnerGroup {

    private final List<AstHieroglyph> hieroglyphs = new ArrayList<>();

    void addHieroglyph(AstHieroglyph hieroglyph) {
        hieroglyphs.add(hieroglyph);
    }

    public List<AstHieroglyph> hieroglyphs() {
        return Collections.unmodifiableList(hieroglyphs);
    }

    /**
     * Hand-builds an absolute group from its hieroglyphs, for tests.
     */
    public static AstAbsoluteGroup of(AstHieroglyph... hieroglyphs) {
        AstAbsoluteGroup result = new AstAbsoluteGroup();
        for (AstHieroglyph hieroglyph : hieroglyphs) {
            result.addHieroglyph(hieroglyph);
        }
        return result;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitAbsoluteGroup(this);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof AstAbsoluteGroup other && hieroglyphs.equals(other.hieroglyphs));
    }

    @Override
    public int hashCode() {
        return hieroglyphs.hashCode();
    }

    @Override
    public String toString() {
        return "AstAbsoluteGroup" + hieroglyphs;
    }
}

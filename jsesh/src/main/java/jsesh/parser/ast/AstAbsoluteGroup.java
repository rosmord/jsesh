package jsesh.parser.ast;

import java.util.List;


/**
 * A group of signs with explicit placement, built with "&amp;&amp;" (each
 * hieroglyph then typically carries a {@code {{x,y,scale}}} position).
 * <p>Built incrementally, via a left-recursive grammar rule of unbounded
 * arity, hence the {@link #of} factory rather than a fixed-arity constructor
 * call at each parse site.
 *
 * @author rosmord
 */
public record AstAbsoluteGroup(List<AstHieroglyph> hieroglyphs) implements AstInnerGroup {

    public AstAbsoluteGroup {
        hieroglyphs = List.copyOf(hieroglyphs);
    }

    /**
     * Hand-builds an absolute group from its hieroglyphs, for tests.
     */
    public static AstAbsoluteGroup of(AstHieroglyph... hieroglyphs) {
        return new AstAbsoluteGroup(List.of(hieroglyphs));
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitAbsoluteGroup(this);
    }
}

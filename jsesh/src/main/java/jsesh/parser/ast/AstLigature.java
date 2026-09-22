package jsesh.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jsesh.model.api.LigatureInterface;

/**
 * A ligature: hieroglyphs joined with "&amp;" (e.g. {@code p&t&n}).
 * <p>Built incrementally, via a left-recursive grammar rule of unbounded
 * arity; unlike most other AST nodes it cannot be a record. Structural
 * equality ({@link #equals}/{@link #hashCode}) is implemented by hand to
 * compensate.
 *
 * @author rosmord
 */
public final class AstLigature implements AstInnerGroup, LigatureInterface {

    private final List<AstHieroglyph> hieroglyphs = new ArrayList<>();

    void addHieroglyph(AstHieroglyph hieroglyph) {
        hieroglyphs.add(hieroglyph);
    }

    public List<AstHieroglyph> hieroglyphs() {
        return Collections.unmodifiableList(hieroglyphs);
    }

    /**
     * Hand-builds a ligature from its hieroglyphs, for tests.
     */
    public static AstLigature of(AstHieroglyph... hieroglyphs) {
        AstLigature result = new AstLigature();
        for (AstHieroglyph hieroglyph : hieroglyphs) {
            result.addHieroglyph(hieroglyph);
        }
        return result;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitLigature(this);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof AstLigature other && hieroglyphs.equals(other.hieroglyphs));
    }

    @Override
    public int hashCode() {
        return hieroglyphs.hashCode();
    }

    @Override
    public String toString() {
        return "AstLigature" + hieroglyphs;
    }
}

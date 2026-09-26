package jsesh.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A quadrat: a vertical stack of {@link AstHBox}es.
 * <p>As in the parser/builder contract this node mirrors ({@code buildVBox}
 * and {@code buildCadrat} both operate on the very same object), a cadrat
 * doubles as the "vertical box" it is built from. Its stack of hboxes is
 * built incrementally, via a left-recursive grammar rule of unbounded arity,
 * across several parser methods — hence the mutable {@link Builder} rather
 * than a fixed-arity constructor call at a single parse site.
 *
 * @author rosmord
 * @see jsesh.model.Cadrat
 * @see jsesh.model.ShadingCode
 */
public record AstCadrat(List<AstHBox> hBoxes, int shading, AstOptionList options) implements AstNode {

    public AstCadrat {
        hBoxes = List.copyOf(hBoxes);
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitCadrat(this);
    }

    /**
     * Hand-builds a cadrat with no shading and no options, for tests.
     */
    public static AstCadrat of(AstHBox... hBoxes) {
        return builder().hBoxes(hBoxes).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Fluent construction of an {@link AstCadrat}, threaded across the
     * recursive-descent parser methods that build up a cadrat's hboxes one
     * at a time before its shading and options (if any) are known.
     */
    public static final class Builder {

        private final List<AstHBox> hBoxes = new ArrayList<>();
        private int shading;
        private AstOptionList options;

        private Builder() {
        }

        public Builder hBox(AstHBox hBox) {
            hBoxes.add(hBox);
            return this;
        }

        public Builder hBoxes(AstHBox... hBoxes) {
            Collections.addAll(this.hBoxes, hBoxes);
            return this;
        }

        public Builder shading(int shading) {
            this.shading = shading;
            return this;
        }

        public Builder options(AstOptionList options) {
            this.options = options;
            return this;
        }

        public AstCadrat build() {
            return new AstCadrat(hBoxes, shading, options);
        }
    }
}

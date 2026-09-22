package jsesh.parser.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jsesh.model.api.CadratInterface;
import jsesh.model.api.VBoxInterface;

/**
 * A quadrat: a vertical stack of {@link AstHBox}es.
 * <p>As in the parser/builder contract this node mirrors ({@code buildVBox}
 * and {@code buildCadrat} both operate on the very same object), a cadrat
 * doubles as the "vertical box" it is built from. Its stack of hboxes is
 * built incrementally, via a left-recursive grammar rule of unbounded arity,
 * so unlike most other AST nodes it cannot be a record. Structural equality
 * ({@link #equals}/{@link #hashCode}) is implemented by hand to compensate.
 *
 * @author rosmord
 * @see jsesh.model.Cadrat
 * @see jsesh.model.ShadingCode
 */
public final class AstCadrat implements AstNode, CadratInterface, VBoxInterface {

    private final List<AstHBox> hBoxes = new ArrayList<>();
    private int shading;
    private AstOptionList options;

    void addHBox(AstHBox hBox) {
        hBoxes.add(hBox);
    }

    public List<AstHBox> hBoxes() {
        return Collections.unmodifiableList(hBoxes);
    }

    /**
     * @return the shading, as a sum of the {@code jsesh.model.ShadingCode} bit
     * codes (1: top left, 2: top right, 4: bottom left, 8: bottom right).
     */
    public int shading() {
        return shading;
    }

    void setShading(int shading) {
        this.shading = shading;
    }

    /**
     * @return the options given after a {@code CADRAT(...)} construct, or
     * {@code null} if none were parsed.
     */
    public AstOptionList options() {
        return options;
    }

    void setOptions(AstOptionList options) {
        this.options = options;
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visitCadrat(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AstCadrat other)) {
            return false;
        }
        return shading == other.shading && hBoxes.equals(other.hBoxes) && Objects.equals(options, other.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hBoxes, shading, options);
    }

    @Override
    public String toString() {
        return "AstCadrat[hBoxes=" + hBoxes + ", shading=" + shading + ", options=" + options + "]";
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
     * Fluent construction of an {@link AstCadrat} for tests, covering
     * shading and options in addition to the hboxes that {@link #of} alone
     * cannot express.
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
            AstCadrat cadrat = new AstCadrat();
            for (AstHBox hBox : hBoxes) {
                cadrat.addHBox(hBox);
            }
            cadrat.setShading(shading);
            cadrat.setOptions(options);
            return cadrat;
        }
    }
}

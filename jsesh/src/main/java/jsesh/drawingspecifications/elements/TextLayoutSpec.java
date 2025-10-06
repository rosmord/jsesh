package jsesh.drawingspecifications.elements;

/**
 * Specifications for the layout of lines and columns.
 * 
 * @param lineSkip    distance between two lines (in points).
 * @param columnSkip  distance between two columns (in points).
 * @param tabUnitWidth get the width of the unit used in MdC tabulation (in points).
 * @author rosmord
 */
public record TextLayoutSpec(
        float lineSkip,
        float columnSkip,
        float tabUnitWidth) {

    public static final TextLayoutSpec DEFAULT = new TextLayoutSpec(
            6.0f,
            10.0f,
            (18f / 200f));

    /**
     * Create a copy builder.
     * 
     * @return
     */
    public Builder copy() {
        return new Builder(this);
    }

    /**
     * Builder for {@link TextLayoutSpec}.
     */
    public static class Builder {
        private float lineSkip;
        private float columnSkip;
        private float tabUnitWidth;

        public Builder(TextLayoutSpec spec) {
            this.lineSkip = spec.lineSkip;
            this.columnSkip = spec.columnSkip;
            this.tabUnitWidth = spec.tabUnitWidth;
        }

        public Builder lineSkip(float lineSkip) {
            this.lineSkip = lineSkip;
            return this;
        }

        public Builder columnSkip(float columnSkip) {
            this.columnSkip = columnSkip;
            return this;
        }

        public Builder tabUnitWidth(float tabUnitWidth) {
            this.tabUnitWidth = tabUnitWidth;
            return this;
        }

        public TextLayoutSpec build() {
            return new TextLayoutSpec(lineSkip, columnSkip, tabUnitWidth);
        }
    }
}

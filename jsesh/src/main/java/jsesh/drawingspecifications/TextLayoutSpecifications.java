package jsesh.drawingspecifications;

/**
 * Specifications for the layout of lines and columns.
 * 
 * @author rosmord
 */
public record TextLayoutSpecifications(
        float lineSkip,
        float columnSkip,
        float tabUnitWidth) {

    /**
     * Create a copy builder.
     * 
     * @return
     */
    public Builder copy() {
        return new Builder(this);
    }

    /**
     * Builder for {@link TextLayoutSpecifications}.
     */
    public static class Builder {
        private float lineSkip;
        private float columnSkip;
        private float tabUnitWidth;

        public Builder(TextLayoutSpecifications spec) {
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

        public TextLayoutSpecifications build() {
            return new TextLayoutSpecifications(lineSkip, columnSkip, tabUnitWidth);
        }
    }
}

package jsesh.drawingspecifications;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;

public record RenderingOptions(
        TextDirection textDirection,
        TextOrientation textOrientation,
        ShadingMode shadingStyle,
        boolean paged,
        boolean smallSignCentered,
        boolean justified // will be removed
) {

    public static final RenderingOptions DEFAULT = new RenderingOptions(
            TextDirection.LEFT_TO_RIGHT,
            TextOrientation.HORIZONTAL,
            ShadingMode.GRAY_SHADING,
            false,
            false,
            false);

    public Builder copy() {
        return new Builder(this);
    }

    /** copy builder class */
    public static class Builder {
        private TextDirection textDirection;
        private TextOrientation textOrientation;
        private ShadingMode shadingStyle;
        private boolean paged;
        private boolean smallSignCentered;
        private boolean justified;

        public Builder(RenderingOptions specs) {
            this.textDirection = specs.textDirection;
            this.textOrientation = specs.textOrientation;
            this.shadingStyle = specs.shadingStyle;
            this.paged = specs.paged;
            this.smallSignCentered = specs.smallSignCentered;
            this.justified = specs.justified;
        }

        public Builder textDirection(TextDirection textDirection) {
            this.textDirection = textDirection;
            return this;
        }

        public Builder textOrientation(TextOrientation textOrientation) {
            this.textOrientation = textOrientation;
            return this;
        }

        public Builder shadingStyle(ShadingMode shadingStyle) {
            this.shadingStyle = shadingStyle;
            return this;
        }

        public Builder paged(boolean paged) {
            this.paged = paged;
            return this;
        }

        public Builder smallSignCentered(boolean smallSignCentered) {
            this.smallSignCentered = smallSignCentered;
            return this;
        }

        public Builder justified(boolean justified) {
            this.justified = justified;
            return this;
        }

        public RenderingOptions build() {
            return new RenderingOptions(
                    textDirection,
                    textOrientation,
                    shadingStyle,
                    paged,
                    smallSignCentered,
                    justified);
        }
    }

}

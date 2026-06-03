package jsesh.drawingspecifications;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;

/**
 * Options which are related neither to geometry nor to fonts.
 */
public record LayoutOptions(
        TextDirection textDirection,
        TextOrientation textOrientation,
        boolean paged,
        boolean smallSignCentered,
        boolean justified // will be removed
) {

    public static final LayoutOptions DEFAULT = new LayoutOptions(
            TextDirection.LEFT_TO_RIGHT,
            TextOrientation.HORIZONTAL,
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
        private boolean paged;
        private boolean smallSignCentered;
        private boolean justified;

        public Builder(LayoutOptions specs) {
            this.textDirection = specs.textDirection;
            this.textOrientation = specs.textOrientation;
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

        public LayoutOptions build() {
            return new LayoutOptions(
                    textDirection,
                    textOrientation,
                    paged,
                    smallSignCentered,
                    justified);
        }
    }

}

package jsesh.render.style;

import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;

/// Options which are related neither to geometry nor to fonts.
///
/// `limitQuadratWidthInLines`: in horizontal text, whether a quadrat made of
/// several lines of signs is scaled down so as not to exceed the maximal
/// quadrat width. Columns always limit their quadrats' width. This is `true`
/// in the historical layout, which documents without the setting in their header
/// keep (see [jsesh.document.DocumentPreferences]); new documents use `false`.
public record LayoutOptions(
        TextDirection textDirection,
        TextOrientation textOrientation,
        boolean paged,
        boolean smallSignCentered,
        boolean justified, // will be removed
        boolean limitQuadratWidthInLines
) {

    public static final LayoutOptions DEFAULT = new LayoutOptions(
            TextDirection.LEFT_TO_RIGHT,
            TextOrientation.HORIZONTAL,
            false,
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
        private boolean limitQuadratWidthInLines;

        public Builder(LayoutOptions specs) {
            this.textDirection = specs.textDirection;
            this.textOrientation = specs.textOrientation;
            this.paged = specs.paged;
            this.smallSignCentered = specs.smallSignCentered;
            this.justified = specs.justified;
            this.limitQuadratWidthInLines = specs.limitQuadratWidthInLines;
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

        public Builder limitQuadratWidthInLines(boolean limitQuadratWidthInLines) {
            this.limitQuadratWidthInLines = limitQuadratWidthInLines;
            return this;
        }

        public LayoutOptions build() {
            return new LayoutOptions(
                    textDirection,
                    textOrientation,
                    paged,
                    smallSignCentered,
                    justified,
                    limitQuadratWidthInLines);
        }
    }

}

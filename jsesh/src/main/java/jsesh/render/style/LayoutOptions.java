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
///
/// `ignoreLineAlignment`: lay out every line at the start, whatever the
/// alignment written on its line break (see [jsesh.sharedvocabulary.LineAlignment]).
/// Used when the lines are wrapped to the width of the editor ("wrap to
/// component width"), where the display is optimised for reading and not
/// WYSIWYG. The content of a zone is not concerned: it has its own width.
public record LayoutOptions(
        TextDirection textDirection,
        TextOrientation textOrientation,
        boolean paged,
        boolean smallSignCentered,
        boolean limitQuadratWidthInLines,
        boolean ignoreLineAlignment
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
        private boolean limitQuadratWidthInLines;
        private boolean ignoreLineAlignment;

        public Builder(LayoutOptions specs) {
            this.textDirection = specs.textDirection;
            this.textOrientation = specs.textOrientation;
            this.paged = specs.paged;
            this.smallSignCentered = specs.smallSignCentered;
            this.limitQuadratWidthInLines = specs.limitQuadratWidthInLines;
            this.ignoreLineAlignment = specs.ignoreLineAlignment;
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

        public Builder ignoreLineAlignment(boolean ignoreLineAlignment) {
            this.ignoreLineAlignment = ignoreLineAlignment;
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
                    limitQuadratWidthInLines,
                    ignoreLineAlignment);
        }
    }

}

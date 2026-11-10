package jsesh.render.style;

import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;

/// Options which are related neither to geometry nor to fonts.
///
/// `honourMaxQuadratWidthInLines`: in horizontal text, whether a quadrat made of
/// several lines of signs is scaled down so as not to exceed the maximal
/// quadrat width. Columns always limit their quadrats' width. `true` by default
/// (see [jsesh.document.DocumentPreferences] for documents).
///
/// `uniformQuadratScaling`: how a quadrat is scaled down. If `false` (the
/// default), each line of the quadrat is first scaled down to the maximal
/// quadrat width, then the quadrat is scaled down to the maximal quadrat height
/// if needed: the lines of a quadrat may end with different scales. If `true`,
/// the quadrat is built at its natural size, then scaled as a whole, with the
/// smallest scale which respects both the width limit (if it applies) and the
/// height limit. This is independent of `honourMaxQuadratWidthInLines`: with
/// no width limit, the height limit alone is always applied uniformly.
///
/// `ignoreLineAlignment`: lay out every line at the start, whatever the
/// alignment written on its line break (see [jsesh.sharedvocabulary.LineAlignment]).
/// Used when the lines are wrapped to the width of the editor ("wrap to
/// component width"), where the display is optimised for reading and not
/// WYSIWYG. The content of a zone is not concerned: it has its own width.
/// Nor are columns, which are never wrapped.
///
/// `fitLoneSigns`: whether a low broad sign alone on a line of a quadrat (as a
/// lone `n` in a column) is enlarged to the width of the quadrat. This is
/// mostly visible in columns, where it fills the column width. `true` by
/// default; a zone can override it (`fitsigns=yes|no`, see
/// [jsesh.model.ZoneParameters]).
///
/// `retrograde`: retrograde writing. The signs and the content of each quadrat
/// are mirrored; the order of the quadrats along the line is not (see
/// [#quadratDirection()]).
public record LayoutOptions(
        TextDirection textDirection,
        TextOrientation textOrientation,
        boolean paged,
        boolean smallSignCentered,
        boolean honourMaxQuadratWidthInLines,
        boolean uniformQuadratScaling,
        boolean ignoreLineAlignment,
        boolean fitLoneSigns,
        boolean retrograde
) {

    /// The direction in which the content of a quadrat is laid out and drawn.
    ///
    /// It is the direction of the text, reversed in retrograde writing. The
    /// order of the quadrats along a line always follows [#textDirection()].
    /// Which elements follow which direction is decided by
    /// [jsesh.model.ModelElement#followsQuadratDirection()].
    ///
    /// @return the direction of the content of the quadrats.
    public TextDirection quadratDirection() {
        if (!retrograde) {
            return textDirection;
        }
        return textDirection.isLeftToRight() ? TextDirection.RIGHT_TO_LEFT : TextDirection.LEFT_TO_RIGHT;
    }

    public static final LayoutOptions DEFAULT = new LayoutOptions(
            TextDirection.LEFT_TO_RIGHT,
            TextOrientation.HORIZONTAL,
            false,
            false,
            true,
            false,
            false,
            true,
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
        private boolean honourMaxQuadratWidthInLines;
        private boolean uniformQuadratScaling;
        private boolean ignoreLineAlignment;
        private boolean fitLoneSigns;
        private boolean retrograde;

        public Builder(LayoutOptions specs) {
            this.textDirection = specs.textDirection;
            this.textOrientation = specs.textOrientation;
            this.paged = specs.paged;
            this.smallSignCentered = specs.smallSignCentered;
            this.honourMaxQuadratWidthInLines = specs.honourMaxQuadratWidthInLines;
            this.uniformQuadratScaling = specs.uniformQuadratScaling;
            this.ignoreLineAlignment = specs.ignoreLineAlignment;
            this.fitLoneSigns = specs.fitLoneSigns;
            this.retrograde = specs.retrograde;
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

        public Builder honourMaxQuadratWidthInLines(boolean honourMaxQuadratWidthInLines) {
            this.honourMaxQuadratWidthInLines = honourMaxQuadratWidthInLines;
            return this;
        }

        public Builder uniformQuadratScaling(boolean uniformQuadratScaling) {
            this.uniformQuadratScaling = uniformQuadratScaling;
            return this;
        }

        public Builder fitLoneSigns(boolean fitLoneSigns) {
            this.fitLoneSigns = fitLoneSigns;
            return this;
        }

        public Builder retrograde(boolean retrograde) {
            this.retrograde = retrograde;
            return this;
        }

        public LayoutOptions build() {
            return new LayoutOptions(
                    textDirection,
                    textOrientation,
                    paged,
                    smallSignCentered,
                    honourMaxQuadratWidthInLines,
                    uniformQuadratScaling,
                    ignoreLineAlignment,
                    fitLoneSigns,
                    retrograde);
        }
    }

}

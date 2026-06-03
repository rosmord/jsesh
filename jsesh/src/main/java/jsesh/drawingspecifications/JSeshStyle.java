package jsesh.drawingspecifications;

import java.util.function.Function;

/**
 * Specifications not related to layout.
 * 
 * @param geometry   everything related to dimensions, from line width to page
 *                   size
 * @param colorSpec  colors the various colours used in rendering
 * @param fontsSpec  fonts the fonts for non-hieroglyphic text
 * @param renderSpec rendering options (shading style, etc.)
 */
public record JSeshStyle(
        GeometrySpecification geometry,
        PaintingSpecifications painting,
        FontSpecification fonts,
        LayoutOptions options) {
            
    public static final JSeshStyle DEFAULT = new JSeshStyle(
            GeometrySpecification.DEFAULT,
            PaintingSpecifications.DEFAULT,
            FontSpecification.DEFAULT,
            LayoutOptions.DEFAULT);

    public Builder copy() {
        return new Builder(this);
    }

    /**
     * A builder for JSeshStyle objects.
     * 
     * <p> As the JSeshStyle contains nested records, we use a nice pattern, the functional builder pattern.
     * <code>JSeshStyle</code> has a Builder, but so does, for example, <code>ColorSpecification</code>.
     * 
     * In theory, to change a color, we should write something like:
     * 
     * <pre>
     * jseshStyle = jseshStyle.copy().
     *                painting(
     *                   jseshStyle.painting().builder().
     *                      blackColor(Color.GRAY)
     *                      .build()
     *                ).build();
     * </pre>
     * which is a bit cumbersome to write, which much boilerplate.
     * 
     * The <em>functional builder pattern</em> will take a function as argument.
     * this function will take a builder initialized with the current colors, and its task will be
     * to use this builder to modify those colors. The above code becomes:
     * <pre>
     * jseshStyle = jseshStyle.copy()
     *              .painting( pBuild ->  pBuild.blackColor(Color.GRAY))
     *              .build();
     * </pre>
     *
     *         
     */
    public static class Builder {
        private GeometrySpecification geometry;
        private PaintingSpecifications painting;
        private FontSpecification fonts;
        private LayoutOptions options;


        public Builder(JSeshStyle style) {
            this.geometry = style.geometry();
            this.painting = style.painting();
            this.fonts = style.fonts();
            this.options = style.options();
        }

        public Builder geometry(GeometrySpecification geometry) {
            this.geometry = geometry;
            return this;
        }

        /**
         * Functional builder pattern for geometry.
         * @param g a function which takes a builder use it to prepare a modified copy of the current object, and returns it.
         * @return
         */
        public Builder geometry(Function<GeometrySpecification.Builder,GeometrySpecification.Builder> g) {
            this.geometry = g.apply(this.geometry.copy()).build();
            return this;
        }

        public Builder painting(PaintingSpecifications colors) {
            this.painting = colors;
            return this;
        }

        /**
         * Functional builder pattern for colors.
         * @param p
         * @return
         */
        public Builder painting(Function<PaintingSpecifications.Builder, PaintingSpecifications.Builder> p) {
            this.painting = p.apply(this.painting.copy()).build();
            return this;
        }

        public Builder fonts(FontSpecification fonts) {
            this.fonts = fonts;
            return this;
        }

        /**
         * Functional builder pattern for Fonts.
         * @param f
         * @return
         */
        public Builder fonts(Function<FontSpecification.Builder, FontSpecification.Builder> f) {
            this.fonts = f.apply(this.fonts.copy()).build();
            return this;
        }

        public Builder options(LayoutOptions options) {
            this.options = options;
            return this;
        }

        public Builder options(Function<LayoutOptions.Builder, LayoutOptions.Builder> o) {
            this.options = o.apply(this.options.copy()).build();
            return this;
        }

        public JSeshStyle build() {
            return new JSeshStyle(geometry, painting, fonts, options);
        }

    }
}

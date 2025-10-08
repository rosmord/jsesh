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
        ColorSpecification colors,
        FontSpecification fonts,
        RenderingOptions options) {
    public static final JSeshStyle DEFAULT = new JSeshStyle(
            GeometrySpecification.DEFAULT,
            ColorSpecification.DEFAULT,
            FontSpecification.DEFAULT,
            RenderingOptions.DEFAULT);

    public Builder copy() {
        return new Builder(this);
    }

    /**
     * A builder for JSeshStyle objects.
     */
    public static class Builder {
        private GeometrySpecification geometry;
        private ColorSpecification colors;
        private FontSpecification fonts;
        private RenderingOptions options;


        public Builder(JSeshStyle style) {
            this.geometry = style.geometry();
            this.colors = style.colors();
            this.fonts = style.fonts();
            this.options = style.options();
        }

        public Builder geometry(GeometrySpecification geometry) {
            this.geometry = geometry;
            return this;
        }

        public Builder geometry(Function<GeometrySpecification.Builder,GeometrySpecification.Builder> g) {
            this.geometry = g.apply(this.geometry.copy()).build();
            return this;
        }

        public Builder colors(ColorSpecification colors) {
            this.colors = colors;
            return this;
        }

        public Builder colors(Function<ColorSpecification.Builder, ColorSpecification.Builder> c) {
            this.colors = c.apply(this.colors.copy()).build();
            return this;
        }

        public Builder fonts(FontSpecification fonts) {
            this.fonts = fonts;
            return this;
        }

        public Builder fonts(Function<FontSpecification.Builder, FontSpecification.Builder> f) {
            this.fonts = f.apply(this.fonts.copy()).build();
            return this;
        }

        public Builder options(RenderingOptions options) {
            this.options = options;
            return this;
        }

        public Builder options(Function<RenderingOptions.Builder, RenderingOptions.Builder> o) {
            this.options = o.apply(this.options.copy()).build();
            return this;
        }

        public JSeshStyle build() {
            return new JSeshStyle(geometry, colors, fonts, options);
        }

    }
}

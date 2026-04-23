package jsesh.mdcDisplayer.context;

import java.util.function.Function;

import jsesh.drawingspecifications.JSeshStyle;
import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;
import jsesh.mdcDisplayer.drawingElements.HieroglyphDrawer;

/**
 * A place to store rendering context technical information.
 * Currently, only FontRenderContext is stored here.
 * If it appears that it's always the only thing needed, we might
 * remove this class and use FontRenderContext directly.
 * 
 * <p>
 * {@link #graphicDeviceScale} is the scale of the output device, in <b>Graphics
 * units per
 * typographical point.</b> This is the scale used by the device if
 * g.getXScale() returns 1.0. It doesn't take the current scale applied to the
 * Graphics2D into account.
 * Note that lie sometimes: In the case of a screen zoom, for instance, we will
 * still provide
 * the original scale.
 * 
 * <p>
 * The current use of {@link #graphicDeviceScale} is mainly to choose if we use
 * the small body font in case the final
 * rendering of glyphs would be small.
 * 
 * 
 * @param jseshStyle the style in use.
 * @param hieroglyphShapeRepository the current font catalogue.
 */
public record JSeshRenderContext(JSeshStyle jseshStyle, HieroglyphShapeRepository hieroglyphShapeRepository) {

    /**
     * Returns a copy builder.
     */
    public Builder copy() {
        return new Builder(this);
    }

    /**
     * A builder for JSeshRenderContext.
     * 
     * @author rosmord
     *
     */
    public static class Builder {
        private JSeshStyle jseshStyle;
        private HieroglyphShapeRepository hieroglyphShapeRepository;

        public Builder(JSeshRenderContext original) {
            this.jseshStyle = original.jseshStyle();
            this.hieroglyphShapeRepository = original.hieroglyphShapeRepository();
        }

        public Builder jseshStyle(Function<JSeshStyle.Builder, JSeshStyle.Builder> styleFuntion) {
            this.jseshStyle = styleFuntion.apply(this.jseshStyle.copy()).build();
            return this;
        }

        public Builder jseshStyle(JSeshStyle newStyle) {
            this.jseshStyle = newStyle;
            return this;
        }

        public Builder HieroglyphsDrawer(HieroglyphShapeRepository hieroglyphShapeRepository) {
            this.hieroglyphShapeRepository = hieroglyphShapeRepository;
            return this;
        }

        public JSeshRenderContext build() {
            return new JSeshRenderContext(jseshStyle, hieroglyphShapeRepository);
        }
    }

    /**
     * Create a margin-less copy of this context for use in embedded graphics.
     * @return a copy of this context with margins set to 1 pixel.
     */
    public  JSeshRenderContext marginLessContext() {
        return this.copy()
                .jseshStyle(style -> style.geometry(g -> g
                        .leftMargin(01)
                        .topMargin(01)))
                .build();

    }
}

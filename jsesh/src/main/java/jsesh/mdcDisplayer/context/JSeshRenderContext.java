package jsesh.mdcDisplayer.context;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.util.function.Function;

import jsesh.drawingspecifications.JSeshStyle;
import jsesh.mdcDisplayer.drawingElements.HieroglyphsDrawer;

/**
 * A place to store rendering context technical information.
 * Currently, only FontRenderContext is stored here.
 * If it appears that it's always the only thing needed, we might
 * remove this class and use FontRenderContext directly.
 * 
 * <p> {@link #graphicDeviceScale} is the scale of the output device, in <b>Graphics units per
 * typographical point.</b> This is the scale used by the device if
 * g.getXScale() returns 1.0. It doesn't take the current scale applied to the Graphics2D into account.
 * Note that lie sometimes: In the case of a screen zoom, for instance, we will still provide
 * the original scale.
 * 
 * <p> The current use of {@link #graphicDeviceScale} is mainly to choose if we use the small body font in case the final
 * rendering of glyphs would be small.
 * 
 * 
 * @param fontRenderContext the FontRenderContext to use for font measurements.
 * @param graphicDeviceScale the scale factor of the graphic device.
 * @param jseshStyle the style in use.
 * @param hieroglyphDrawer the hieroglyph drawer in use.
 */
public record JSeshRenderContext(JSeshStyle jseshStyle, HieroglyphsDrawer hieroglyphDrawer) {


   

    /**
     * Returns a copy builder.
     */
    public Builder copy() {
        return new Builder(this);
    }

    /**
     * A builder for JSeshRenderContext.
     * @author rosmord
     *
     */    public static class Builder {
        private JSeshStyle jseshStyle;
        private HieroglyphsDrawer hieroglyphDrawer; 
       
        public Builder(JSeshRenderContext original) {
            this.jseshStyle = original.jseshStyle();
            this.hieroglyphDrawer = original.hieroglyphDrawer();
        }


        public Builder jseshStyle(Function<JSeshStyle.Builder, JSeshStyle.Builder>  styleFuntion) {
            this.jseshStyle = styleFuntion.apply(this.jseshStyle.copy()).build();
            return this;
        }

        public Builder HieroglyphsDrawer(HieroglyphsDrawer hieroglyphDrawer) {
            this.hieroglyphDrawer = hieroglyphDrawer;
            return this;
        }

        public JSeshRenderContext build() {
            return new JSeshRenderContext(jseshStyle, hieroglyphDrawer);
        }
    }
}

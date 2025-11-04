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
public record JSeshRenderContext(FontRenderContext fontRenderContext, double graphicDeviceScale, JSeshStyle jseshStyle, HieroglyphsDrawer hieroglyphDrawer) {


    /**
     * Returns an inaccurate default render context, which doens't really know where the text is printed.
     * @return
     */
    public static final JSeshRenderContext buildBadDefault(JSeshStyle jseshStyle, HieroglyphsDrawer hieroglyphDrawer) {
        return new JSeshRenderContext(
                new FontRenderContext(null, true, true), 1.0,
                jseshStyle,
                 hieroglyphDrawer);
    }

    /**
     * Returns a render context for a known Graphics.
     * @return
     */
    public static final JSeshRenderContext buildSimpleContext(Graphics g, double graphicDeviceScale, JSeshStyle jseshStyle, HieroglyphsDrawer hieroglyphDrawer) {
        Graphics2D g2 = (Graphics2D) g;
        return 
            new JSeshRenderContext(
                g2.getFontRenderContext()
                , graphicDeviceScale, jseshStyle, hieroglyphDrawer);
    }

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
        private FontRenderContext fontRenderContext;
        private double graphicDeviceScale;
        private JSeshStyle jseshStyle;
        private HieroglyphsDrawer hieroglyphDrawer; 
       
        public Builder(JSeshRenderContext original) {
            this.fontRenderContext = original.fontRenderContext();
            this.graphicDeviceScale = original.graphicDeviceScale();
            this.jseshStyle = original.jseshStyle();
            this.hieroglyphDrawer = original.hieroglyphDrawer();
        }

        public Builder fontRenderContext(FontRenderContext fontRenderContext) {
            this.fontRenderContext = fontRenderContext;
            return this;
        }

        public Builder graphicDeviceScale(double graphicDeviceScale) {
            this.graphicDeviceScale = graphicDeviceScale;
            return this;
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
            return new JSeshRenderContext(fontRenderContext, graphicDeviceScale, jseshStyle, hieroglyphDrawer);
        }
    }
}

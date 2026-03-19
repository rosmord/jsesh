package jsesh.mdcDisplayer.context;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

/**
 * Technical rendering information for JSesh, depending on the output device.
 * 
 */
public record JSeshTechRenderContext(
        FontRenderContext fontRenderContext, double graphicDeviceScale) {

    /**
     * Returns a render context for a known Graphics.
     * 
     * @return
     */
    public static final JSeshTechRenderContext buildSimpleContext(Graphics g, double graphicDeviceScale) {
        Graphics2D g2 = (Graphics2D) g;
        return new JSeshTechRenderContext(
                g2.getFontRenderContext(), graphicDeviceScale);
    }

    /// A default variant of the render context, which basically ignores the current
    /// settings.
    ///
    /// Useful if one wants to compute approximate layout without having a Graphics
    /// object available.
    public static final JSeshTechRenderContext APPROXIMATIVE_CONTEXT = new JSeshTechRenderContext(
            new FontRenderContext(null, true, true), 1.0);

    /// Returns the render context for a graphical component.
    /// 
    /// Less accurate than [buildSimpleContext], but can be used when no Graphics is available.
    /// 
    /// @param component a graphical component.
    /// @return a render context for the given component.
    public static final JSeshTechRenderContext buildForComponent(Component component) {
        GraphicsConfiguration gc = component.getGraphicsConfiguration();
        AffineTransform tx = (gc != null)
                ? gc.getDefaultTransform()
                : new AffineTransform();
        FontRenderContext frc = new FontRenderContext(tx, true, true);
        double scale = tx.getScaleX();
        return new JSeshTechRenderContext(frc, scale);
    }
}

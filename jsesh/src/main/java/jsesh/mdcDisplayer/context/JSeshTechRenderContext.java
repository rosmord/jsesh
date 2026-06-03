package jsesh.mdcDisplayer.context;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.function.Function;

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

        /**
         * Returns a render context for a known Graphics.
         * 
         * The graphic device scale is 1.0, which is usually what we want.
         * 
         * @return
         */
        public static final JSeshTechRenderContext buildSimpleContext(Graphics g) {
                return buildSimpleContext(g, 1.0);
        }

        /**
         * A default variant of the render context, which basically ignores the current
         * settings.
         * 
         * <p>
         * Useful if one wants to compute approximate layout without having a Graphics
         * object available.
         */
        public static final JSeshTechRenderContext APPROXIMATIVE_CONTEXT = new JSeshTechRenderContext(
                        new FontRenderContext(null, true, true), 1.0);

        /**
         * Default render context for vector graphics.
         * 
         * <p>
         * No antialiasing is needed.
         */
        public static final JSeshTechRenderContext VECTOR_CONTEXT = new JSeshTechRenderContext(
                        new FontRenderContext(null, false, true), 1.0);

        /**
         * Returns the render context for a graphical component.
         * 
         * @param component a graphical component.
         * @return a render context for the given component.
         */
        public static final JSeshTechRenderContext buildForComponent(Component component) {
                GraphicsConfiguration gc = component.getGraphicsConfiguration();
                AffineTransform tx = (gc != null)
                                ? gc.getDefaultTransform()
                                : new AffineTransform();
                FontRenderContext frc = new FontRenderContext(tx, true, true);
                double scale = tx.getScaleX();
                return new JSeshTechRenderContext(frc, scale);
        }

        /**
         * Runs a code with a simple, temporary tech render context, built on a bitmap
         * graphics.
         * 
         * @param function a function which takes the render context as argument, and
         *                 returns a value.
         * @return the value returned by the function.
         */
        public static final <T> T applyWithDefaultTechContext(Function<JSeshTechRenderContext, T> function) {
                // Create an image for the context
                // We use a 1x1 image, as we only need the font render context.
                Graphics g = null;
                try {
                        g = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB)
                                        .getGraphics();
                        JSeshTechRenderContext context = buildSimpleContext(g, 1.0);
                        return function.apply(context);
                } finally {
                        if (g != null)
                                g.dispose();
                }
        }

}

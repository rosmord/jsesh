package jsesh.mdcDisplayer.context;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;

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

}

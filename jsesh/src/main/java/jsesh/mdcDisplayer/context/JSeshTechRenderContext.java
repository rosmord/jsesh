package jsesh.mdcDisplayer.context;

import java.awt.font.FontRenderContext;

/**
 * Technical rendering information for JSesh, depending on the output device.
 * 
 * NOT USED YET.
 */
public record JSeshTechRenderContext(
    FontRenderContext fontRenderContext, double graphicDeviceScale
) {
    
}

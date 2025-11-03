package jsesh.mdcDisplayer.context;

import java.awt.font.FontRenderContext;

/**
 * A place to store rendering context technical information.
 */
public class RenderContext {
    FontRenderContext fontRenderContext;

    public RenderContext(FontRenderContext fontRenderContext) {
        this.fontRenderContext = fontRenderContext;
    }

    public FontRenderContext fontRenderContext() {
        return fontRenderContext;
    }
}

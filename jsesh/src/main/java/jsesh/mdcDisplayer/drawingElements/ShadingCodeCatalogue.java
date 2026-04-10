package jsesh.mdcDisplayer.drawingElements;

import java.awt.geom.Rectangle2D;
import java.util.Map;

/**
 * A catalogue for special codes, which are not handled by the normal
 * hieroglyphic fonts.
 * 
 * <p>
 * This handles shading codes (and not ecdotic symbols, which are handled by
 * {@link SpecialSymbolDrawer}).
 * 
 */
class ShadingCodeCatalogue {

    /**
     * A map code for signs not managed by FontManager.
     * 
     * Those are not ecdotic symbols, but shading. We might use something else for
     * them.
     */
    private Map<String, Rectangle2D.Float> shadingSizes;

    public ShadingCodeCatalogue(float a1Width, float a1Height) {

        shadingSizes = Map.of(
                "/", new Rectangle2D.Float(0, 0, a1Width / 2f, a1Height / 2f),
                "//", new Rectangle2D.Float(0, 0, a1Width, a1Height),
                "h/", new Rectangle2D.Float(0, 0, a1Width, a1Height / 2f),
                "v/", new Rectangle2D.Float(0, 0, a1Width / 2f, a1Height)
        );        
    }

    public boolean containsCode(String code) {
        return shadingSizes.containsKey(code);
    }

    public Rectangle2D getSizeForCode(String code) {
        return shadingSizes.get(code);
    }
}

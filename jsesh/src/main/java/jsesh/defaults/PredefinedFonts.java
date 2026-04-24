package jsesh.defaults;

import jsesh.hieroglyphs.fonts.CompositeHieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.GnutraceHieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.ResourcesHieroglyphicShapeRepository;

/**
 * Access to individual Predefined hieroglyphic fonts.
 * 
 * This will actual create fonts. If you want to use them, see {@link StandardFontShapeRepository}.
 * 
 * @see StandardFontShapeRepository
 */
public class PredefinedFonts {
    
    private PredefinedFonts() {        
    }
    
    /**
     * The standard JSesh font, defined in the <code>jseshGlyphs</code> resource folder.
     * @return a new instance of the standard JSesh font.
     */
    public static HieroglyphShapeRepository standardJSeshFont() {
        return new ResourcesHieroglyphicShapeRepository(
				"/jseshGlyphs");
    }

    /**
     * Returns the old font I created with GnuTrace in 1993.
     * @return
     */
    public static HieroglyphShapeRepository gnuTraceFont() {
        return GnutraceHieroglyphShapeRepository.getInstance();
    }

    /**
     * Create an instance of the default JSesh font, using first the standard JSesh font, and then the GnuTrace font as a fallback.
     * <p> Doesn't use user-defined fonts.
     * @return
     */
    public static CompositeHieroglyphShapeRepository compositeFont() {
        CompositeHieroglyphShapeRepository result = new CompositeHieroglyphShapeRepository();
        result.addHieroglyphicFontManager(standardJSeshFont());
        result.addHieroglyphicFontManager(gnuTraceFont());
        return result;
    }
}

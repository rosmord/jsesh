package jsesh.defaults;

import jsesh.hieroglyphs.fonts.Constants;
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
        return new ResourcesHieroglyphicShapeRepository(Constants.STANDARD_JSESH_FONT_RESOURCE_PATH);
    }

    /**
     * Returns the old font I created with GnuTrace in 1993.
     * @return
     */
    public static HieroglyphShapeRepository gnuTraceFont() {
        return GnutraceHieroglyphShapeRepository.getInstance();
    }

}

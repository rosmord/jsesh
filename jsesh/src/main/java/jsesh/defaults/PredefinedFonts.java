package jsesh.defaults;

import jsesh.hieroglyphs.fonts.CompositeHieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.GnutraceHieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.ResourcesHieroglyphicShapeRepository;

/**
 * Access to individual Predefined hieroglyphic fonts.
 * 
 * <p> Those methods are factories. They create <b>new</b> instances of the fonts. Usually, you will want to store them somewhere and share them 
 * between objects.
 * <p>In most cases, if you need an interactive display, you'd rather use {@link HieroglyphResourcesBuilder} which will
 * provide you with both fonts and database.
 * 
 * @see StandardFontShapeRepository
 */
public class PredefinedFonts {
    
    /**
     * Resource path for the standard JSesh font.
     */
    public static final String STANDARD_JSESH_FONT_RESOURCE_PATH = "/jseshGlyphs";

    private PredefinedFonts() {        
    }
    
    /**
     * The standard JSesh font, defined in the <code>jseshGlyphs</code> resource folder.
     * @return a new instance of the standard JSesh font.
     */
    public static HieroglyphShapeRepository buildStandardJSeshFont() {
        return new ResourcesHieroglyphicShapeRepository(PredefinedFonts.STANDARD_JSESH_FONT_RESOURCE_PATH);
    }

    /**
     * Returns the old font I created with GnuTrace in 1993.
     * @return
     */
    public static HieroglyphShapeRepository buildGnuTraceFont() {
        return GnutraceHieroglyphShapeRepository.getInstance();
    }

    /**
     * Returns a composite font built with JSesh standard font and the old GnuTrace font.
     * @return
     */
    public static HieroglyphShapeRepository buildAllEmbeddedFonts() {
        CompositeHieroglyphShapeRepository res = new CompositeHieroglyphShapeRepository();
        res.addHieroglyphicFontManager(buildStandardJSeshFont());
        res.addHieroglyphicFontManager(buildGnuTraceFont());
        return res;
    }

}

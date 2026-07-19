package jsesh.glyphs.fonts;

/**
 * Access to individual Predefined hieroglyphic fonts.
 *
 * <p> Those methods are factories. They create <b>new</b> instances of the fonts. Usually, you will want to store them somewhere and share them
 * between objects.
 * <p>In most cases, if you need an interactive display, you'd rather
 * use {@link jsesh.defaults.HieroglyphResourcesBuilder HieroglyphResourcesBuilder} which will
 * provide you with both fonts and database.
 *
 * <p>Note: the link above is deliberately fully qualified. Importing
 * {@code jsesh.defaults.HieroglyphResourcesBuilder} just to shorten it would
 * make {@code jsesh.glyphs} depend on {@code jsesh.defaults}, which sits above
 * it.
 * 
 * @author Serge Rosmorduc
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
        res.addRepository(buildStandardJSeshFont());
        res.addRepository(buildGnuTraceFont());
        return res;
    }

}

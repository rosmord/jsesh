package jsesh.search.ui;

import jsesh.hieroglyphs.fonts.CompositeHieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.ResourcesHieroglyphicShapeRepository;

/**
 * The font used for displaying wildcards in the search field.
 */
class WildcardFont extends ResourcesHieroglyphicShapeRepository {

    private static final String FONT_PATH = "/jsesh/search/wildcard";

    private static final WildcardFont INSTANCE = new WildcardFont();

    public static WildcardFont getInstance() {
        return INSTANCE;
    }

    WildcardFont( ) {
        super(FONT_PATH);
    }

    /**
     * Adds the wildcards to an existing font. Doesn't modify the original font.
     * @param original
     * @return a new font, with the wildcard symbols added.
     */
    public HieroglyphShapeRepository addToFont(HieroglyphShapeRepository original) {
        CompositeHieroglyphShapeRepository compositeManager = new CompositeHieroglyphShapeRepository();
        compositeManager.addHieroglyphicFontManager(original);
        compositeManager.addHieroglyphicFontManager(this);
        return compositeManager;
    }

    
    
}

package jsesh.search.ui;

import jsesh.hieroglyphs.fonts.ResourcesHieroglyphicShapeRepository;

/**
 * The font used for displaying wildcards in the search field.
 * <p> Beware: as of now, we don't have a clean system to release
 * listeners in a CompositeHieroglyphShapeRepository. It is suggested to create
 * a single instance of this class.
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
    
    
}

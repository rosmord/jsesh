package jsesh.defaults;

import jsesh.editor.PossibilityRepository;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;

/**
 * Repositories giving coordinated access to hieroglyphic fonts resources, including automated completion.
 */
public interface JseshFontKit {
    /**
     * Graphical definition of the fonts.
     * @return a HieroglyphShapeRepository
     */
    HieroglyphShapeRepository hieroglyphShapeRepository();
    
    /**
     * Source for text completion (might include a glossary, but not necessarily)..
     */
    PossibilityRepository possibilityRepository();

    /**
     * The hieroglyph database, which gives access to the list of signs, their properties, etc.
     * @return a HieroglyphDatabaseInterface
     */
    HieroglyphDatabaseInterface hieroglyphDatabase();

    /**
     * The default font kit, which doesn't rely on user preferences.
     * @return
     */
    public static JseshFontKit defaultFontKit() {
        return SharedDefaults.getInstance();
    }
}

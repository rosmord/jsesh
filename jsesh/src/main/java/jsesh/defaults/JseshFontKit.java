package jsesh.defaults;

import jsesh.editor.PossibilityRepository;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;

/**
 * A repository giving coordinated access to hieroglyphic fonts resources, including automated completion.
 */
public interface JseshFontKit {

    /**
     * Shared default font kit using only embedded font resources and no user definitions.
     * <p>Avoid weird side effects if one hosts a server <strong>and</strong> has her or his own font definitions for
     * interactive work with JSesh.
     * @return default shared font kit.
     */
    static JseshFontKit embeddedOnlyFontKit() {
        return SimpleFontKit.embeddedOnlyInstance();
    }

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

   
}

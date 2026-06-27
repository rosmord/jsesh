package jsesh.defaults;

import jsesh.editor.PossibilityRepository;
import jsesh.hieroglyphs.data.HieroglyphDatabase;
import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;

/**
 * A repository giving coordinated access to hieroglyphic fonts resources, including automated completion.
 */
public interface HieroglyphToolkit {

    /**
     * Shared default font kit using only standard font resources and no user definitions.
     * <p> will include : the standard jsesh fonts if the jar jseshGlyphs.jar is in the path, and the default legacy Gnutrace fonts if not.
     * <p>Avoid weird side effects if one hosts a server <strong>and</strong> has her or his own font definitions for
     * interactive work with JSesh.
     * @return default shared font kit.
     */
    static HieroglyphToolkit standardHieroglyphToolKit() {
        return SimpleHieroglyphToolkit.embeddedOnlyInstance();
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
    HieroglyphDatabase hieroglyphDatabase();

   
}

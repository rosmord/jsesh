package jsesh;

import jsesh.defaults.JseshFontKit;
import jsesh.editor.PossibilityRepository;
import jsesh.glossary.GlossaryManager;
import jsesh.hieroglyphs.data.HieroglyphDatabaseFactory;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.fonts.JSeshFullHieroglyphShapeRepository;

/**
 * Default configuration for the application, using user settings.
 * 
 * Gives access to:
 * 
 * <ul>
 * <li> the glossary
 * <li> the hieroglyph shape repository, with its user-specific directory
 * <li> the hieroglyph database
 * </ul>
 * 
 */
public class JSeshUserSignLibraryConfiguration implements JseshFontKit {

    private GlossaryManager glossaryManager;
    private JSeshFullHieroglyphShapeRepository hieroglyphShapeRepository;
    private HieroglyphDatabaseInterface hieroglyphDatabase;
    private PossibilityRepository possibilityRepository;

    /**
     * Creates the default configuration for the JSesh Application, using user specific settings.
     */
    public JSeshUserSignLibraryConfiguration() {
        glossaryManager = new GlossaryManager();
        hieroglyphShapeRepository = new JSeshFullHieroglyphShapeRepository();
        hieroglyphDatabase  = HieroglyphDatabaseFactory.buildWithUserDefinitions(hieroglyphShapeRepository);
        possibilityRepository = new PossibilityRepository(hieroglyphDatabase, glossaryManager.getGlossary());
    }

    /**
     * @return the glossaryManager
     */
    public GlossaryManager glossaryManager() {
        return glossaryManager;
    }

    public JSeshFullHieroglyphShapeRepository hieroglyphShapeRepository() {
        return hieroglyphShapeRepository;
    }

    public HieroglyphDatabaseInterface hieroglyphDatabase() {
        return hieroglyphDatabase;
    }

    @Override
    public PossibilityRepository possibilityRepository() {
        return possibilityRepository;
    }

}

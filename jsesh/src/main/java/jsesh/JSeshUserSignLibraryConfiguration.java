package jsesh;

import jsesh.glossary.GlossaryManager;
import jsesh.glossary.JSeshGlossary;
import jsesh.hieroglyphs.data.HieroglyphDatabaseFactory;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.fonts.JSeshFullHieroglyphShapeRepository;

/**
 * Default configuration for the application, using user settings.
 * 
 */
public class JSeshUserSignLibraryConfiguration {

    private GlossaryManager glossaryManager;
    private JSeshFullHieroglyphShapeRepository hieroglyphShapeRepository;
    private HieroglyphDatabaseInterface hieroglyphDatabase;

    public JSeshUserSignLibraryConfiguration() {
        glossaryManager = new GlossaryManager();
        hieroglyphShapeRepository = new JSeshFullHieroglyphShapeRepository();
        hieroglyphDatabase  = HieroglyphDatabaseFactory.buildWithUserDefinitions(hieroglyphShapeRepository);
    }

    public JSeshGlossary glossary() {
        return glossaryManager.getGlossary();
    }

    public JSeshFullHieroglyphShapeRepository hieroglyphShapeRepository() {
        return hieroglyphShapeRepository;
    }

    public HieroglyphDatabaseInterface hieroglyphDatabase() {
        return hieroglyphDatabase;
    }

}

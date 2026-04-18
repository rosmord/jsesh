package jsesh.defaults;

import jsesh.editor.PossibilityRepository;
import jsesh.glossary.JSeshGlossary;
import jsesh.hieroglyphs.data.HieroglyphDatabaseFactory;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;

/**
 * Shared defaults for commonly used resources.
 * 
 * JSesh 7.x to JSesh 8.x is mostly motivated by the removal of singletons in order to introduce a more robust architecture.
 * 
 * <p> However, there are use cases where singletons are useful:
 * 
 * <ul>
 * <li> when the object is <em>really</em> a singleton, for instance the Manuel de Codage, which is both immutable and unique (but is relatively heavy to create) ;
 * <li> as defaults used when building <strong>top-level</strong> objects. We want the JSesh library to be easy to use, which means that developpers should be able 
 *  to create a JMDCEditor without passing lots of parameters. A reasonable default should be available for fonts, style, etc. 
 * </ul>
 * 
 * <p> The current class gives access to reasonnable defaults. They are fixed, and don't rely on user preferences, which means that a software which uses them won't be impacted 
 * by user choices.
 * 
 * <p> A class which will reuse the settings from the user preferences is also available.
 */
public class SharedDefaults implements JseshFontKit {

    // Singleton implementation
    private static final SharedDefaults INSTANCE = new SharedDefaults();

    private HieroglyphShapeRepository hieroglyphShapeRepository;
    private PossibilityRepository possibilityRepository;
    private HieroglyphDatabaseInterface hieroglyphDatabase;

    public static SharedDefaults getInstance() {
        return INSTANCE;
    }

    
    private SharedDefaults() {
        hieroglyphShapeRepository = PredefinedFonts.compositeFont();
        
        // Empty glossary.
        JSeshGlossary glossary = new JSeshGlossary();
        
        hieroglyphDatabase = HieroglyphDatabaseFactory.buildPlainDefault(hieroglyphShapeRepository);
		
		possibilityRepository = new PossibilityRepository(hieroglyphDatabase, glossary);
    }

    
    /**
     * @return the hieroglyphDatabase
     */
    public HieroglyphDatabaseInterface hieroglyphDatabase() {
        return hieroglyphDatabase;
    }


    public HieroglyphShapeRepository hieroglyphShapeRepository() {
        return hieroglyphShapeRepository;
    }


    public PossibilityRepository possibilityRepository() {
        return possibilityRepository;
    }

    
}

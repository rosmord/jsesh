package jsesh.defaults;

import java.util.List;
import java.util.Optional;

import jsesh.editor.PossibilityRepository;
import jsesh.glossary.JSeshGlossary;
import jsesh.hieroglyphs.data.HieroglyphDatabaseFactory;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.fonts.CompositeHieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.DirectoryHieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;

/**
 * A simple implementation of the JseshFontKit interface, which gives access to
 * a set of default resources, including a composite font and an empty glossary.
 */
public class SimpleFontKit implements JseshFontKit {

    private HieroglyphShapeRepository hieroglyphShapeRepository;
    private PossibilityRepository possibilityRepository;
    private HieroglyphDatabaseInterface hieroglyphDatabase;

    /**
     * Shared default font kit using only embedded fonts, no user font, and no glossary.
     */
    public static JseshFontKit embeddedOnlyInstance() {
        return EmbeddedOnlyHolder.INSTANCE;
    }

    private static final class EmbeddedOnlyHolder {
        private static final JseshFontKit INSTANCE = buildFontWithoutUserDefinitions(
                buildCompositeFont(List.of(PredefinedFonts.standardJSeshFont(), PredefinedFonts.gnuTraceFont())),
                new JSeshGlossary());

        private EmbeddedOnlyHolder() {
        }
    }

    /**
     * Build a font kit with the given font and glossary.
     * <p>
     * Won't use user definitions for signs (created with SignInfo), only the
     * embedded ones.
     * <p>
     * the glossary allows the user to access predefined groups (or words) with a
     * simple transliteration. Passing an empty glossary is fine.
     * 
     * <p>
     * We strongly suggest to use a composite font, the last two fonts being the standard JSesh font and
     * the GnuTrace font. Use {@link PredefinedFonts#standardJSeshFont()} and
     * {@link PredefinedFonts#gnuTraceFont()}
     * to acccess them.
    
     * <p>
     * If you want to add fonts from a given folder, use
     * {@link DirectoryHieroglyphShapeRepository}.
     * 
     * @param fonts       a list of fonts.
     * @param glossary an glossary (can be empty, but should not be null).
     */
    public static SimpleFontKit buildFontWithoutUserDefinitions(HieroglyphShapeRepository font,
            JSeshGlossary glossary) {
        HieroglyphDatabaseInterface database = HieroglyphDatabaseFactory.buildPlainDefault(font);
        return new SimpleFontKit(font, glossary, database);
    }

    /**
     * Build a font kit with the given font, glossary, using users sign
     * descriptions.
     * <p>
     * will use the descriptions created by the user with SignInfo, in addition to
     * the embedded ones.
     * <p>
     * the glossary allows the user to access predefined groups (or words) with a
     * simple transliteration. Passing an empty glossary is fine.
     * 
     * <p>
     * We strongly suggest to use a composite font, the last two fonts being the standard JSesh font and
     * the GnuTrace font. Use {@link PredefinedFonts#standardJSeshFont()} and
     * {@link PredefinedFonts#gnuTraceFont()}
     * to acccess them.
     * <p>
     * If you want to add fonts from a given folder, use
     * {@link DirectoryHieroglyphShapeRepository}.
     * 
     * @param fonts       a list of fonts.
     * @param glossary a glossary (may be empty)
     */
    public static SimpleFontKit buildFontWithUserDescriptions(HieroglyphShapeRepository fonts,
            JSeshGlossary glossary) {        
        HieroglyphDatabaseInterface database = HieroglyphDatabaseFactory.buildWithUserDefinitions(fonts);
        return new SimpleFontKit(fonts, glossary, database);
    }

    /**
     * Creates a font kit with the given font, an optional glossary and database.
     * @param hieroglyphShapeRepository
     * @param glossary
     * @param database
     */
    private SimpleFontKit(HieroglyphShapeRepository hieroglyphShapeRepository, JSeshGlossary glossary,
            HieroglyphDatabaseInterface database) {
        this.hieroglyphShapeRepository = hieroglyphShapeRepository;

        this.hieroglyphDatabase = database;


        possibilityRepository = new PossibilityRepository(hieroglyphDatabase, glossary);
    }

    
    /**
     * Creates a font kit with the given font, possibility repository and database.
     * <p> In theory, they should be associated. 
     * @param hieroglyphShapeRepository
     * @param possibilityRepository
     * @param hieroglyphDatabase
     */
    public SimpleFontKit(HieroglyphShapeRepository hieroglyphShapeRepository,
            PossibilityRepository possibilityRepository, HieroglyphDatabaseInterface hieroglyphDatabase) {
        this.hieroglyphShapeRepository = hieroglyphShapeRepository;
        this.possibilityRepository = possibilityRepository;
        this.hieroglyphDatabase = hieroglyphDatabase;
    }

    

    private static HieroglyphShapeRepository buildCompositeFont(List<HieroglyphShapeRepository> fonts) {
        CompositeHieroglyphShapeRepository result = new CompositeHieroglyphShapeRepository();
        for (HieroglyphShapeRepository font : fonts) {
            result.addHieroglyphicFontManager(font);
        }
        return result;
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

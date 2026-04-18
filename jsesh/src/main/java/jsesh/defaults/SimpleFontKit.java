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
     * Build a font kit with the given fonts and glossary.
     * <p>
     * Won't use user definitions for signs (created with SignInfo), only the
     * embedded ones.
     * <p>
     * The fonts will be used in the order they are given, which means that the
     * first one will be used as the main font, and the others as fallbacks.
     * <p>
     * the glossary allows the user to access predefined groups (or words) with a
     * simple transliteration. Passing an empty glossary is fine.
     * 
     * <p>
     * We strongly suggest that the last two fonts be the standard JSesh font and
     * the GnuTrace font. Use {@link PredefinedFonts#standardJSeshFont()} and
     * {@link PredefinedFonts#gnuTraceFont()}
     * to acccess them.
     * <p>
     * If you want to add fonts from a given folder, use
     * {@link DirectoryHieroglyphShapeRepository}.
     * 
     * @param fonts       a list of fonts.
     * @param optGlossary an optional glossary.
     */
    public static SimpleFontKit buildFontWithoutUserDefinitions(List<HieroglyphShapeRepository> fonts,
            Optional<JSeshGlossary> optGlossary) {
        HieroglyphShapeRepository compositeFont = buildCompositeFont(fonts);
        HieroglyphDatabaseInterface database = HieroglyphDatabaseFactory.buildPlainDefault(compositeFont);
        return new SimpleFontKit(compositeFont, optGlossary, database);
    }

    /**
     * Build a font kit with the given fonts, glossary, using users sign
     * descriptions.
     * <p>
     * will use the descriptions created by the user with SignInfo, in addition to
     * the embedded ones.
     * <p>
     * The fonts will be used in the order they are given, which means that the
     * first one will be used as the main font, and the others as fallbacks.
     * <p>
     * the glossary allows the user to access predefined groups (or words) with a
     * simple transliteration. Passing an empty glossary is fine.
     * 
     * <p>
     * We strongly suggest that the last two fonts be the standard JSesh font and
     * the GnuTrace font. Use {@link PredefinedFonts#standardJSeshFont()} and
     * {@link PredefinedFonts#gnuTraceFont()}
     * to acccess them.
     * <p>
     * If you want to add fonts from a given folder, use
     * {@link DirectoryHieroglyphShapeRepository}.
     * 
     * @param fonts       a list of fonts.
     * @param optGlossary an optional glossary.
     */
    public static SimpleFontKit buildFontWithUserDescriptions(List<HieroglyphShapeRepository> fonts,
            Optional<JSeshGlossary> optGlossary) {
        HieroglyphShapeRepository compositeFont = buildCompositeFont(fonts);
        HieroglyphDatabaseInterface database = HieroglyphDatabaseFactory.buildWithUserDefinitions(compositeFont);
        return new SimpleFontKit(compositeFont, optGlossary, database);
    }

    private SimpleFontKit(HieroglyphShapeRepository hieroglyphShapeRepository, Optional<JSeshGlossary> optGlossary,
            HieroglyphDatabaseInterface database) {
        this.hieroglyphShapeRepository = hieroglyphShapeRepository;

        // hieroglyphDatabase =
        // HieroglyphDatabaseFactory.buildPlainDefault(hieroglyphShapeRepository);
        this.hieroglyphDatabase = database;

        JSeshGlossary glossary = optGlossary.orElseGet(() -> new JSeshGlossary());

        possibilityRepository = new PossibilityRepository(hieroglyphDatabase, glossary);
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

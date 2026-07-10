package jsesh.defaults;


import jsesh.editor.PossibilityRepository;
import jsesh.glossary.Glossary;
import jsesh.hieroglyphs.data.HieroglyphDatabase;
import jsesh.hieroglyphs.data.HieroglyphDatabaseFactory;
import jsesh.hieroglyphs.fonts.CompositeHieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.DirectoryHieroglyphShapeRepository;
import jsesh.hieroglyphs.fonts.HieroglyphShapeRepository;
import jsesh.utils.DirectoryHolder;

/**
 * A builder for HieroglyphResources.
 * 
 * <p> <b>Note about fonts</b> : the order of fonts is important. The first font 
 * added will be searched first, and so on.
 */
public class HieroglyphResourcesBuilder {

    private Glossary glossary = new Glossary();
    private CompositeHieroglyphShapeRepository shapes =  new CompositeHieroglyphShapeRepository();
    private boolean useUserDefinitions = false;

    /**
     * Add a shape repository to the resources.
     * @param font
     */
    public HieroglyphResourcesBuilder addFont(HieroglyphShapeRepository font) {
        // TODO : rename addHieroglyphicFontManager
        shapes.addHieroglyphicFontManager(font);
        return this;
    }


    /**
     * Adds a directory of user fonts. 
     * This method can be used multiple times.
     * 
     * <p> Note that folderFont may possibly point to "no" folder, 
     * in which case it will be ignored.
     * 
     * <p> Modifications to the folder in folderfont will be detected.
     * @param folderFont
     */
    public HieroglyphResourcesBuilder userFontFolder(DirectoryHolder folderFont) {
        addFont(new DirectoryHieroglyphShapeRepository(folderFont));
        return this;
    }

    /**
     * Decide if we use user sign definitions (created with SignInfo).
     * @param useUserDefinitions the useUserDefinitions to set
     */
    public HieroglyphResourcesBuilder useUserDefinitions(boolean useUserDefinitions) {
        this.useUserDefinitions = useUserDefinitions;
        return this;
    }

    /**
     * Adds an optional glossary which might be searched by the possibility repository.
     * @param glossary
     */
    public HieroglyphResourcesBuilder glossary(Glossary glossary) {
        this.glossary = glossary;
        return this;
    }
   
    public HieroglyphResources build() {
            HieroglyphDatabase database;
            if (useUserDefinitions) {
                database = HieroglyphDatabaseFactory.buildWithUserDefinitions(shapes);
            } else {
                database = HieroglyphDatabaseFactory.buildPlainDefault(shapes);
            }
            
        PossibilityRepository possibilities = new PossibilityRepository(database, glossary);
        return new HieroglyphResources(shapes, database, possibilities);
    }



    /**
     * Build resources, using only what JSesh sources provide.
     * @return
     */
    public static HieroglyphResources buildEmbedded() {
        return new HieroglyphResourcesBuilder()
                .addFont(PredefinedFonts.buildStandardJSeshFont())
                .addFont(PredefinedFonts.buildGnuTraceFont()).build();
    }


    /**
     * Builds resources, including the SignInfo user definitions, if any, but no custom fonts.
     * @return
     */
    public static HieroglyphResources buildWithUserDefinitions() {
                return new HieroglyphResourcesBuilder()
                .addFont(null)
                .addFont(PredefinedFonts.buildStandardJSeshFont())
                .addFont(PredefinedFonts.buildGnuTraceFont())
                .useUserDefinitions(true)
                .build();
    }

    /**
     * The whole resources, including user definitions and user fonts.
     * @param userFonts
     * @return
     */
    public static HieroglyphResources buildFull(DirectoryHolder userFonts) {
    return new HieroglyphResourcesBuilder()
        .userFontFolder(userFonts)                  // user signs override first...
        .addFont(PredefinedFonts.buildStandardJSeshFont())
        .addFont(PredefinedFonts.buildGnuTraceFont())    // ...gnutrace last (fallback)
        .useUserDefinitions(true)
        .build();
}


}

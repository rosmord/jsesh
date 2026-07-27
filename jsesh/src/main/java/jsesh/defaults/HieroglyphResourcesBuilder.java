package jsesh.defaults;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import jsesh.glossary.Glossary;
import jsesh.glossary.GlossaryManager;
import jsesh.glossary.PossibilityRepositoryFromGlossary;
import jsesh.glyphs.fonts.CompositeHieroglyphShapeRepository;
import jsesh.glyphs.fonts.DirectoryHieroglyphShapeRepository;
import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.glyphs.fonts.ResourcesHieroglyphicShapeRepository;
import jsesh.glyphs.signdata.HieroglyphDatabase;
import jsesh.glyphs.signdata.PossibilityRepository;
import jsesh.utils.io.DirectoryHolder;

/**
 * A builder for HieroglyphResources.
 * 
 * <b>Note about fonts</b> : the order of fonts is important. The first font
 * added will be searched first, and so on.
 */
public class HieroglyphResourcesBuilder {

    private Glossary glossary = new Glossary();
    private CompositeHieroglyphShapeRepository shapes = new CompositeHieroglyphShapeRepository();
    private boolean useUserDefinitions = false;

    /**
     * Add a shape repository to the resources.
     * 
     * @param font
     */
    public HieroglyphResourcesBuilder addFont(HieroglyphShapeRepository font) {
        shapes.addRepository(font);
        return this;
    }

    /**
     * Add a font which takes is signs in a directory.
     * This method can be used multiple times.
     * 
     * <p>
     * Note that fontDirectoryHolder may possibly point to "no" directory,
     * in which case it will be ignored.
     * 
     * <p>
     * The fontDirectoryHolder can be made to point to a different directory, in which case the system will 
     * take the new directory into account.
     * 
     * @param fontDirectoryHolder a DirectoryHolder which is a mutable reference to a directory.
     * @return the builder, for chaining.
     */
    public HieroglyphResourcesBuilder addFontDirectoryHolder(DirectoryHolder fontDirectoryHolder) {
        addFont(new DirectoryHieroglyphShapeRepository(fontDirectoryHolder));
        return this;
    }

    /**
     * Add a font which takes is signs in a fixed directory.
     * This method can be used multiple times.
     * 
     * <p> If the directory designated by fontDirectory doesn't exist, it will be 
     * as if it were empty.
     * @param fontDirectory can't be null.
     * @return the builder, for chaining.
     */
    public HieroglyphResourcesBuilder addFontDirectory(File fontDirectory) {
        addFont(new DirectoryHieroglyphShapeRepository(fontDirectory));
        return this;
    }

     /**
     * Add a font which takes is signs in a fixed directory.
     * This method can be used multiple times.
     * 
     * <p> If the directory designated by fontDirectory doesn't exist, it will be 
     * as if it were empty.
     * @param fontDirectoryPath the path to the directory; can't be null.
     * @return the builder, for chaining.
     */
    public HieroglyphResourcesBuilder addFontDirectory(String fontDirectoryPath) {
        addFont(new DirectoryHieroglyphShapeRepository(new File(fontDirectoryPath)));
        return this;
    }

    /**
     * Adds the standard JSesh font to the resources.
     * @return the builder, for chaining.
     */
    public HieroglyphResourcesBuilder addStandardJSeshFont() {
        addFont(PredefinedFonts.buildStandardJSeshFont());
        return this;
    }

    /**
     * Adds the default GnuTrace font to the resources.
     * (probably not very useful).
     * @return the builder, for chaining.
     */
    public HieroglyphResourcesBuilder addGnuTraceFont() {
        addFont(PredefinedFonts.buildGnuTraceFont());
        return this;
    }



    /**
     * Add a font which takes its signs in a resource (data included with the application jar).
     * @param resourcePath the path to the resource, relative to the root of the jar.
     * @return the builder, for chaining.
     */

    public HieroglyphResourcesBuilder addFontFromResource(String resourcePath) {
        addFont(new ResourcesHieroglyphicShapeRepository(resourcePath));
        return this;
    }
    
    /**
     * Decide if we use user sign definitions (created with SignInfo).
     * 
     * @param useUserDefinitions the useUserDefinitions to set
     */
    public HieroglyphResourcesBuilder useUserDefinitions(boolean useUserDefinitions) {
        this.useUserDefinitions = useUserDefinitions;
        return this;
    }

    

    /**
     * Adds a glossary (optional) which might be searched by the possibility repository.
     * 
     * @param glossary
     */
    public HieroglyphResourcesBuilder glossary(Glossary glossary) {
        this.glossary = glossary;
        return this;
    }

    /**
     * Build the HieroglyphResources, when everything has been configured.
     * @return the HieroglyphResources, ready to be used.
     */
    public HieroglyphResources build() {
        HieroglyphDatabase database;
        if (useUserDefinitions) {
            database = HieroglyphDatabaseFactory.buildWithUserDefinitions(shapes);
        } else {
            database = HieroglyphDatabaseFactory.buildPlainDefault(shapes);
        }

        PossibilityRepository possibilities = new PossibilityRepositoryFromGlossary(database, glossary);
        return new HieroglyphResources(shapes, database, possibilities);
    }

    /**
     * Build resources, using only what JSesh sources provide.
     * 
     * @return
     */
    public static HieroglyphResources buildEmbedded() {
        return new HieroglyphResourcesBuilder()
                .addFont(PredefinedFonts.buildStandardJSeshFont())
                .addFont(PredefinedFonts.buildGnuTraceFont()).build();
    }

    /**
     * Builds resources, including the SignInfo user definitions, if any, but no
     * custom fonts.
     * 
     * @return
     */
    public static HieroglyphResources buildWithUserDefinitions() {
        return new HieroglyphResourcesBuilder()
                .addFont(PredefinedFonts.buildStandardJSeshFont())
                .addFont(PredefinedFonts.buildGnuTraceFont())
                .useUserDefinitions(true)
                .build();
    }

    /**
     * The whole resources, including user definitions and user fonts.
     * 
     * <p> 
     * This one gives you access to the whole resources, while having the possibility
     * to edit and modify both the user glossary and the user font directory. 
     * <p>
     * If you don't want to access the user glossary, you can always create an empty
     * glossary with `new Glossary()`.
     * 
     * <p> if you don't need to modify them, just to use them, the method 
     * {@link #buildFullFromUserPreferences()} is probably more convenient.
     * 
     * @param userFontsDirectoryHolder the directory containing user fonts
     * @param glossary                 the glossary (used for completion)
     * @return
     */
    public static HieroglyphResources buildFull(DirectoryHolder userFontsDirectoryHolder, Glossary glossary) {
        return new HieroglyphResourcesBuilder()
                .addFontDirectoryHolder(userFontsDirectoryHolder) // user signs override first...
                .addFont(PredefinedFonts.buildStandardJSeshFont())
                .addFont(PredefinedFonts.buildGnuTraceFont()) // ...gnutrace last (fallback)
                .useUserDefinitions(true)
                .glossary(glossary) // add the glossary
                .build();
    }

    /**
     * The whole resources, using whatever the user has already configured through JSesh's own preferences : user fonts directory and glossary.
     *
     * <p>
     * This is the method to use when you want an editor or field to behave
     * like JSesh itself, with no need to manage a {@link UserFontDirectoryManager}
     * or a {@link jsesh.glossary.GlossaryManager} yourself. If the glossary
     * can't be read, the failure is logged and an empty glossary is used.
     *
     * @return
     */
    public static HieroglyphResources buildFullFromUserPreferences() {
        UserFontDirectoryManager userFontDirectoryManager = UserFontDirectoryManager.buildUserFontManager();
        GlossaryManager glossaryManager = new GlossaryManager();
        try {
            glossaryManager.read();
        } catch (RuntimeException e) {
            Logger.getLogger(HieroglyphResourcesBuilder.class.getName())
                    .log(Level.WARNING, "Could not read the user glossary", e);
        }
        return buildFull(userFontDirectoryManager.getUserFontHolder(), glossaryManager.getGlossary());
    }

}

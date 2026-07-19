/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.jhotdraw;

import java.awt.Font;
import java.io.File;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import jsesh.ui.clipboard.MDCClipboardPreferences;
import jsesh.ui.defaults.HieroglyphResources;
import jsesh.ui.defaults.UserFontDirectoryManager;
import jsesh.render.style.FontSpecification;
import jsesh.render.style.JSeshStyle;
import jsesh.ui.editor.JSeshStyleReference;
import jsesh.ui.glossary.PossibilityRepository;
import jsesh.ui.glossary.GlossaryManager;
import jsesh.ui.glossary.JGlossaryEditor;
import jsesh.ui.export.html.HTMLExporter;
import jsesh.ui.export.pdfExport.PDFExportPreferences;
import jsesh.ui.export.rtf.RTFExportGranularity;
import jsesh.ui.export.rtf.RTFExportPreferences;
import jsesh.glyphs.data.HieroglyphDatabase;
import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.jhotdraw.constants.ExportType;
import jsesh.jhotdraw.dialogs.CorpusSearchDialogFrame;
import jsesh.jhotdraw.documentview.JSeshViewCore;
import jsesh.jhotdraw.preferences.JSeshStyleHelper;
import jsesh.jhotdraw.preferences.application.model.ExportPreferences;
import jsesh.jhotdraw.preferences.application.model.FontInfo;
import jsesh.platform.preferences.JSeshPreferencesRoot;
import jsesh.search.clientApi.SearchTarget;
import jsesh.search.ui.JWildcardPanel;
import jsesh.search.ui.SearchPanelFactory;
import jsesh.search.ui.SearchUIResources;
import jsesh.ui.palette.PalettePresenter;
import jsesh.ui.widgets.signimportdialog.ExternalSignImporter;
import jsesh.ui.widgets.utils.MDCIconFactory;
import jsesh.utils.JSeshWorkingDirectory;

/**
 * Framework-agnostic part of the JSesh application.
 * 
 * <p>
 * Deals with all information which is not specific to the JHotdraw framework.
 *
 * @author rosmord
 */
public class JSeshApplicationCore {

    /**
     * pdf export folder property name for preferences.
     */
    private static final String QUICK_PDF_EXPORT_DIRECTORY = "quickPdfExportDirectory";

    /**
     * Folder for exporting small pdf pictures (useful for press release).
     */
    private File quickPDFExportDirectory = new File("pdfExports");

    /**
     * The RTF Preference currently selected for copy/paste.
     *
     * We will change the way everything is done there. Currently : SMALL and
     * LARGE are directly picked from the RTFExportPreferences array (resp. 0
     * and 1). FILE uses the 0 and WYSIWYG the 1 slots data, but only for the
     * sizes.
     */
    private ExportType exportType = ExportType.SMALL;

    /**
     * Specific export information for PDF Files.
     */
    private final PDFExportPreferences pdfExportPreferences;

    /**
     * Base style for <em>new</em> documents.
     */
    private JSeshStyle newDocumentStyle;

    /**
     * Style for dialogs and widgets.
     * 
     * <p>
     * Shared between all components.
     */

    private final JSeshStyleReference jseshComponentsStyle;

    /**
     * Should we use the embedded transliteration font?
     */
    private boolean useEmbeddedTransliterationFont = true;

    /**
     * JSesh Glossary Manager.
     */

    private final GlossaryManager glossaryManager;

    /**
     * Hieroglyphic database.
     */
    private final HieroglyphDatabase hieroglyphDatabase;

    /**
     * Hieroglyphic fonts
     */

    private final HieroglyphShapeRepository hieroglyphShapeRepository;

    /**
     * Extended resources for the search UI (mainly exitended font with wildcards).
     */
    private final SearchUIResources searchUIResources;


    /**
     * Folder for user-defined hieroglyphic signs.
     */

    private UserFontDirectoryManager userFontDirectoryManager;

    /**
     * the Possibility repository, allowing one to use autocompletion on signs.
     */
    private final PossibilityRepository possibilityRepository;

    /**
     * Other information for copy/paste (file formats, for instance).
     */
    private MDCClipboardPreferences clipboardPreferences = new MDCClipboardPreferences();

    /**
     * Information about the HTML export.
     */
    private final HTMLExporter htmlExporter;

    private ExportPreferences exportPreferences;

    /**
     * The glossary editor.
     */
    private JGlossaryEditor glossaryEditor;

    /**
     * Dialog for importing new signs.
     * A bit outdated now.
     */
    private ExternalSignImporter externalSignImporter;

    /**
     * The factory for icons defined from Manuel de Codage code.
     */
    private MDCIconFactory mdcIconFactory;

    /**
     * The hieroglyphic sign palette, shared by all views.
     * <p>
     * Lazily built by {@link #palettePresenter()}.
     */
    private PalettePresenter palettePresenter;

    /**
     * The corpus search dialog, shared by the whole application.
     * <p>
     * Lazily built by {@link #corpusSearchDialog()}, once
     * {@link #setFrontEnd(JSeshFrontEnd)} has been called.
     */
    private CorpusSearchDialogFrame corpusSearchDialog;

    /**
     * The software which displays this application core.
     * <p>
     * Everything which involves document windows (opening a file, inserting a
     * sign in the current text...) is delegated to it.
     */
    private JSeshFrontEnd frontEnd;


    public JSeshApplicationCore(HieroglyphResources hieroglyphResources, UserFontDirectoryManager userFontDirectoryManager,
         GlossaryManager glossaryManager) {
        this.jseshComponentsStyle = new JSeshStyleReference(
            JSeshStyle.DEFAULT.copy()
                .geometry(g -> g.scaleToHeight(40.0))
            .build()
        );
        this.userFontDirectoryManager = userFontDirectoryManager;
        this.glossaryManager = glossaryManager;
        this.hieroglyphDatabase = hieroglyphResources.database();
        this.hieroglyphShapeRepository = hieroglyphResources.hieroglyphShapeRepository();
        this.possibilityRepository = hieroglyphResources.possibilityRepository();
        this.searchUIResources = new SearchUIResources(jseshComponentsStyle, hieroglyphResources);
        this.mdcIconFactory = new MDCIconFactory(hieroglyphShapeRepository);
        this.newDocumentStyle = JSeshStyle.DEFAULT;
        loadPreferences();
        // derived fields.
        this.pdfExportPreferences = new PDFExportPreferences();
        this.htmlExporter = new HTMLExporter(hieroglyphShapeRepository);

        // Dialogs
        glossaryEditor = new JGlossaryEditor(glossaryManager, jseshComponentsStyle, hieroglyphResources);
        externalSignImporter = new ExternalSignImporter(userFontDirectoryManager, hieroglyphResources.hieroglyphShapeRepository());
    }

    /**
     * Load preferences (from the java preferences system) and apply them to the
     * application.
     *
     * <p>
     * the application settings will mostly be applied to <b>new</b> documents,
     * except for hieroglyphic font source.
     */
    public final void loadPreferences() {
        // Access to Java preferences.
        Preferences preferences = JSeshPreferencesRoot.getPreferences();
        // extract geometry and the like from the preferences.
        updateJSeshStyleFromPreferences(preferences);
        // extract font information from the preferences.
        setFontInfo(FontInfo.getFromPreferences());
        quickPDFExportDirectory = new File(preferences.get(
                QUICK_PDF_EXPORT_DIRECTORY, new File(getCurrentDirectory(),
                        "quickPdf").getAbsolutePath()));
        exportPreferences = ExportPreferences.getFromPreferences(preferences);
        clipboardPreferences = MDCClipboardPreferences.getFromPreferences(preferences);
    }

    /**
     * Save the current preferences to the java preferences system.
     */
    public void savePreferences() {
        Preferences preferences = JSeshPreferencesRoot.getPreferences();
        // save the creator software version.
        preferences.put("prefversion", "8.0");

        FontInfo fontInfo = getFontInfo();
        fontInfo.savetoPrefs();
        JSeshStyleHelper.saveJSeshStyleToPreferences(newDocumentStyle(), preferences);
        preferences.put(QUICK_PDF_EXPORT_DIRECTORY, quickPDFExportDirectory.getAbsolutePath());
        exportPreferences.saveToPrefs(preferences);
        clipboardPreferences.saveToPrefs(preferences);
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The style to use for new documents.
     * 
     * @return the current style.
     */
    public JSeshStyle newDocumentStyle() {
        return newDocumentStyle;
    }

    /**
     * The style to use for hieroglyphic dialogs and the like.
     * <p>
     * This is used, for instance, in the glossary and the search dialog. They all
     * share the same style.
     * <p>
     * It's not used for jsesh documents.
     * 
     * @return the jseshComponentsStyle
     */
    public JSeshStyleReference jseshComponentsStyle() {
        return jseshComponentsStyle;
    }

    /**
     * Choose which copy/paste configuration to use.
     * 
     * <p>
     * {@link ExportType#FILE} should not be used for copy/paste.
     * <p>
     * (it should probably not be an exportype ?)
     *
     * @param exportType
     */
    public void selectCopyPasteConfiguration(ExportType exportType) {
        if (exportType == ExportType.FILE) {
            throw new IllegalArgumentException(
                    "Incorrect export type for copy/paste " + ExportType.FILE);
        }
        this.exportType = exportType;
    }

    /**
     * Get the working directory for the application.
     * 
     * @return
     */
    public synchronized File getCurrentDirectory() {
        return JSeshWorkingDirectory.getWorkingDirectory();
    }

    /**
     * Sets the working directory for the application.
     * 
     * @param currentDirectory
     */
    public synchronized void setCurrentDirectory(File currentDirectory) {
        JSeshWorkingDirectory.setWorkingDirectory(currentDirectory);
    }

    /**
     * Returns the PDF export preferences.
     * 
     * @return the PDF export preferences.
     */
    public PDFExportPreferences getPDFExportPreferences() {
        return pdfExportPreferences;
    }

    /**
     * Preferences for RTF exportation.
     * 
     * @return the RTF export preferences.
     */
    public RTFExportPreferences getCurrentRTFPreferences() {
        return getRTFExportPreferences(exportType);
    }

    /**
     * Returns suitable RTF preferences for a type of export.
     *
     * @param exportType
     * @return
     */
    public RTFExportPreferences getRTFExportPreferences(ExportType exportType) {
        int cadratHeight = switch (exportType) {
            case SMALL -> (int) exportPreferences.getquadratHeightSmall();
            case LARGE -> (int) exportPreferences.getquadratHeightLarge();
            case FILE -> (int) exportPreferences.getquadratHeightFile();
            case WYSIWYG -> (int) exportPreferences.getquadratHeightWysiwyg();
        };

        RTFExportGranularity granularity = exportType == ExportType.WYSIWYG ? RTFExportGranularity.ONE_LARGE_PICTURE
                : exportPreferences.getGranularity();
        boolean respectOriginalTextLayout = exportType == ExportType.WYSIWYG
                || exportPreferences.isTextLayoutRespected();
        return new RTFExportPreferences(cadratHeight, granularity, respectOriginalTextLayout,
                exportPreferences.getGraphicFormat());
    }

    /**
     * Sets the export preferences for copy/paste.
     * 
     * @param exportPreferences
     */
    public void setExportPreferences(ExportPreferences exportPreferences) {
        this.exportPreferences = exportPreferences;
    }

    public ExportPreferences getExportPreferences() {
        return exportPreferences;
    }

    /**
     * Gets the HTML exporter.
     * <p>
     * It's a persistent object, to keep the current configuration.
     * 
     * @return
     */
    public HTMLExporter getHTMLExporter() {
        return htmlExporter;
    }

    /**
     * Returns the folder used for quick PDF export.
     * 
     * @return the folder used for quick PDF export.
     */
    public File getQuickPDFExportFolder() {
        return quickPDFExportDirectory;
    }

    /**
     * Sets the folder used for quick PDF export.
     * 
     * @param exportFolder the folder used for quick PDF export.
     */
    public void setQuickPDFExportFolder(File exportFolder) {
        this.quickPDFExportDirectory = exportFolder;
    }

    /**
     * Gets the clipboard preferences for copy/paste.
     * 
     * @return the clipboard preferences for copy/paste.
     */
    public MDCClipboardPreferences getClipboardPreferences() {
        return this.clipboardPreferences;
    }

    /**
     * Sets the clipboard preferences for copy/paste.
     * 
     * @param prefs the clipboard preferences for copy/paste.
     */
    public void setClipboardPreferences(MDCClipboardPreferences prefs) {
        this.clipboardPreferences = prefs;
    }

    /**
     * Return the current font info configuration.
     * 
     * @return
     */
    public FontInfo getFontInfo() {
        FontSpecification specifications = newDocumentStyle().fonts();
        Font baseFont = specifications.plainFont();
        Font transliterationFont = specifications.transliterationFont();
        return new FontInfo(
                userFontDirectoryManager.getUserFontHolder().optDirectory(),
                baseFont,
                transliterationFont,
                this.useEmbeddedTransliterationFont,
                specifications.translitUnicode(),
                specifications.yodChoice());
    }

    /**
     * Sets the font info configuration.
     * 
     * @param fontInfo
     */
    public void setFontInfo(FontInfo fontInfo) {
        this.useEmbeddedTransliterationFont = fontInfo.isUseEmbeddedFont();
        // Sets the jseshStyle
        this.newDocumentStyle = fontInfo.applyApplyToJSeshStyle(newDocumentStyle());
        // and update the hieroglyphic font.
        fontInfo.applyToUserFontDirectory(userFontDirectoryManager);
    }

    

    /**
     * @return the glossaryManager
     */
    public GlossaryManager getGlossaryManager() {
        return glossaryManager;
    }

    /**
     * @return the hieroglyphDatabase
     */
    public HieroglyphDatabase getHieroglyphDatabase() {
        return hieroglyphDatabase;
    }

    /**
     * @return the hieroglyphShapeRepository
     */
    public HieroglyphShapeRepository getHieroglyphShapeRepository() {
        return hieroglyphShapeRepository;
    }

    // ---------- private methods

    /**
     * Update JSesh style from the saved preferences.
     * 
     * <p>
     * Note that this will only modify NEW documents.
     * 
     * @param preferences
     */
    private void updateJSeshStyleFromPreferences(Preferences preferences) {
        this.newDocumentStyle = JSeshStyleHelper.javaPreferencesToJSeshStyle(preferences, newDocumentStyle);
    }

    /**
     * Returns the hieroglyph compendium needed by most JSesh components.
     * <p>
     * They will find everything they need to draw and search hieroglyphs.
     * 
     * @return a hieroglyph compendium.
     */
    public HieroglyphResources getHieroglyphResources() {
        return new HieroglyphResources(hieroglyphShapeRepository, hieroglyphDatabase, possibilityRepository);
    }

    /**
     * Apply the new document style to an existing view.
     * 
     * @param viewCore the view whose style should change.
     */
    public void applyNewDocumentStyleTo(JSeshViewCore viewCore) {
        viewCore.setJSeshStyle(newDocumentStyle());
    }

    /**
     * Use the style of an existing view as the new document style.
     * @param viewCore
     */
    public void updateNewDocumentStyleFrom(JSeshViewCore viewCore) {
        this.newDocumentStyle = viewCore.getJSeshStyle();
    }

    /**
     * The glossary editor.
     * 
     * @return
     */
    public JGlossaryEditor glossaryEditor() {
        return glossaryEditor;
    }


    /**
     * Initialize a Search panel with a given search adapter.
     * @param searchAdapter
     * @return
     */
    public JWildcardPanel createSearchPanel(SearchTarget searchAdapter) {
        return SearchPanelFactory.createWildCardPanel(searchAdapter, searchUIResources);        
    }

    /**
     * Sets the software which displays this application.
     * <p>
     * Must be called before any of the application-level dialogs is used, as
     * they all need to act on documents.
     *
     * @param frontEnd the front-end for this core.
     */
    public void setFrontEnd(JSeshFrontEnd frontEnd) {
        this.frontEnd = frontEnd;
    }

    /**
     * The front-end, when we are sure it's needed.
     *
     * @return the front-end.
     */
    private JSeshFrontEnd frontEnd() {
        if (frontEnd == null) {
            throw new IllegalStateException(
                    "Bug : setFrontEnd should be called before using the application dialogs.");
        }
        return frontEnd;
    }

    /**
     * Display an informative message to the user.
     *
     * @param message the message to display.
     */
    public void setMessage(String message) {
        frontEnd().setMessage(message);
    }

    /**
     * The corpus search dialog.
     * <p>
     * There is only one for the whole application, shared by the action which
     * displays it and by the front-end, which needs to register it as a
     * secondary window.
     *
     * @return the corpus search dialog.
     */
    public CorpusSearchDialogFrame corpusSearchDialog() {
        if (corpusSearchDialog == null) {
            corpusSearchDialog = new CorpusSearchDialogFrame(frontEnd(), searchUIResources);
        }
        return corpusSearchDialog;
    }

    /**
     * The hieroglyphic sign palette.
     * <p>
     * There is only one for the whole application, as it's shared by all views.
     *
     * @return the palette presenter.
     */
    public PalettePresenter palettePresenter() {
        if (palettePresenter == null) {
            palettePresenter = new PalettePresenter(hieroglyphShapeRepository, hieroglyphDatabase);
            palettePresenter.setHieroglyphPaletteListener(code -> frontEnd().insertCodeInActiveDocument(code));
        }
        return palettePresenter;
    }


    public ExternalSignImporter externalSignImporter() {
        return externalSignImporter;
    }


    /**
     * Access a factory that will generate (and cache) pictures of MdC code, mainly for labels and GUI elements.
     * @return the mdcIconFactory
     */
    public MDCIconFactory getMdcIconFactory() {
        return mdcIconFactory;
    }

    public Optional<File> getHieroglyphsDirectory() {
        return userFontDirectoryManager.getUserFontHolder().optDirectory();
    }

    /**
     * @return the searchUIResources
     */
    public SearchUIResources getSearchUIResources() {
        return searchUIResources;
    }

}

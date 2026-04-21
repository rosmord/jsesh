package jsesh.jhotdraw;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.apple.eawt.Application;

import jsesh.JSeshUserSignLibraryConfiguration;
import jsesh.clipboard.MDCClipboardPreferences;
import jsesh.defaults.JseshFontKit;
import jsesh.drawingspecifications.JSeshStyle;
import jsesh.drawingspecifications.PaintingSpecifications;
import jsesh.drawingspecifications.ShadingMode;
import jsesh.editor.PossibilityRepository;
import jsesh.glossary.GlossaryManager;
import jsesh.graphics.export.html.HTMLExporter;
import jsesh.graphics.export.pdfExport.PDFExportPreferences;
import jsesh.graphics.export.rtf.RTFExportGranularity;
import jsesh.graphics.export.rtf.RTFExportPreferences;
import jsesh.hieroglyphs.fonts.JSeshFullHieroglyphShapeRepository;
import jsesh.jhotdraw.applicationPreferences.model.ExportPreferences;
import jsesh.jhotdraw.applicationPreferences.model.FontInfo;
import jsesh.mdc.constants.JSeshInfoConstants;
import jsesh.utils.JSeshWorkingDirectory;

/**
 * Framework-agnostic part of the JSesh application. Deals with all information
 * which is not specific to the JHotdraw framework.
 *
 * @author rosmord
 */
public class JSeshApplicationBase {

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
    private final PDFExportPreferences pdfExportPreferences = new PDFExportPreferences();

    /**
     * Base style for <em>new</em> documents.
     */
    private JSeshStyle defaultDrawingSpecifications = JSeshStyle.DEFAULT;

    /**
     * Other information for copy/paste (file formats, for instance).
     */
    private MDCClipboardPreferences clipboardPreferences = new MDCClipboardPreferences();

    /**
     * Information about fonts (used to build DrawingSpecifications).
     */
    private FontInfo fontInfo;

    
    /**
     * Information about the HTML export.
     */
    private final HTMLExporter htmlExporter;

    private ExportPreferences exportPreferences;
    
    

    public JSeshApplicationBase(GlossaryManager glossaryManager,  JSeshFullHieroglyphShapeRepository hieroglyphShapeRepository) {
        JSeshUserSignLibraryConfiguration appdef = new JSeshUserSignLibraryConfiguration();
        appdef.fontKit().hieroglyphDatabase();
        appdef.glossary();
        appdef.
        loadPreferences();
    }

    /**
     * Change a number of preferences for this program according to the user
     * preferences.
     *
     * <p>
     * Changes since the "old" JSesh version :
     * <ul>
     * <li>Instead of keeping an exhaustive list of file name for each format,
     * the file name will be built using the current document name. This will
     * lead to a simpler (and more usual) system.</li>
     * </ul>
     */
    public final void loadPreferences() {

        Preferences preferences = Preferences.userNodeForPackage(this
                .getClass());

        loadDrawingSpecificationPreferences(preferences);
        setFontInfo(FontInfo.getFromPreferences(preferences));
        quickPDFExportDirectory = new File(preferences.get(
                QUICK_PDF_EXPORT_DIRECTORY, new File(getCurrentDirectory(),
                        "quickPdf").getAbsolutePath()));
        exportPreferences = ExportPreferences.getFromPreferences(preferences);
        clipboardPreferences = MDCClipboardPreferences.getFromPreferences(preferences);
    }

   
    public void savePreferences() {
        Preferences preferences = Preferences.userNodeForPackage(this
                .getClass());

        // Also save the creator software version.
        preferences.put("prefversion", "5.1");

        fontInfo.savetoPrefs(preferences);
        saveDrawingSpecificationPreferences(preferences);
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
     * Returns a copy of the current default drawing specifications.
     *
     * @return
     */
    public PaintingSpecifications getDefaultDrawingSpecifications() {
        return defaultDrawingSpecifications.copy();
    }

    RTFExportPreferences getCurrentRTFPreferences() {
        return getRTFExportPreferences(exportType);
    }

    /**
     * Choose which copy/paste configuration to use. {@link ExportType#FILE}
     * should not be used for copy/paste.
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

    public synchronized File getCurrentDirectory() {
        return JSeshWorkingDirectory.getWorkingDirectory();
    }

    public synchronized void setCurrentDirectory(File currentDirectory) {
        JSeshWorkingDirectory.setWorkingDirectory(currentDirectory);
    }

    public PDFExportPreferences getPDFExportPreferences() {
        return pdfExportPreferences;
    }

    /**
     * Returns suitable RTF preferences for a type of export.
     *
     * @param exportType
     * @return
     */
    public RTFExportPreferences getRTFExportPreferences(ExportType exportType) {
        RTFExportPreferences result = new RTFExportPreferences(0, null);
        switch (exportType) {
            case SMALL:
                result.setCadratHeight((int) exportPreferences
                        .getquadratHeightSmall());
                break;
            case LARGE:
                result.setCadratHeight((int) exportPreferences
                        .getquadratHeightLarge());
                break;
            case FILE:
                result.setCadratHeight((int) exportPreferences
                        .getquadratHeightFile());
                break;
            case WYSIWYG:
                result.setCadratHeight((int) exportPreferences
                        .getquadratHeightWysiwyg());
                break;
        }

        if (exportType != ExportType.WYSIWYG) {
            result.setExportGranularity(exportPreferences.getGranularity());
        } else {
            result.setExportGranularity(RTFExportGranularity.ONE_LARGE_PICTURE);
        }

        result.setExportGraphicFormat(exportPreferences.getGraphicFormat());
        result.setRespectOriginalTextLayout(exportPreferences
                .isTextLayoutRespected() || exportType == ExportType.WYSIWYG);
        return result;
    }

    public HTMLExporter getHTMLExporter() {
        return htmlExporter;
    }

    public File getQuickPDFExportFolder() {
        return quickPDFExportDirectory;
    }

    public void setQuickPDFExportFolder(File exportFolder) {
        this.quickPDFExportDirectory = exportFolder;
    }

    public MDCClipboardPreferences getClipboardPreferences() {
        return this.clipboardPreferences;
    }

    public void setClipboardPreferences(MDCClipboardPreferences prefs) {
        this.clipboardPreferences = prefs;
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

    public FontInfo getFontInfo() {
        return fontInfo;
    }

    public void setFontInfo(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
        // TODO : use a better system here... Meanwhile, we will stay with the current one.
        DefaultHieroglyphShapeRepository.getInstance().setDirectory(fontInfo.getHieroglyphsFolder());
        PaintingSpecifications d = defaultDrawingSpecifications.copy();
        fontInfo.applyToDrawingSpecifications(d);
        defaultDrawingSpecifications = d;
    }

    public void setDefaultDrawingSpecifications(
            PaintingSpecifications drawingSpecifications) {
        defaultDrawingSpecifications = drawingSpecifications.copy();
    }

    
}

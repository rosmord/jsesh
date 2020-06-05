package jsesh.jhotdraw;

import jsesh.graphics.export.rtf.RTFExportPreferences;
import jsesh.graphics.export.html.HTMLExporter;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import jsesh.graphics.export.rtf.RTFExportGranularity;
import jsesh.graphics.export.pdfExport.PDFExportPreferences;
import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import jsesh.jhotdraw.applicationPreferences.model.ExportPreferences;
import jsesh.jhotdraw.applicationPreferences.model.FontInfo;
import jsesh.mdc.constants.JSeshInfoConstants;
import jsesh.mdcDisplayer.clipboard.MDCClipboardPreferences;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;
import jsesh.mdcDisplayer.preferences.ShadingStyle;
import jsesh.utils.JSeshWorkingDirectory;

/**
 * Framework-agnostic part of the JSesh application. Deals with all information
 * which is not specific to the JHotdraw framework.
 *
 * TODO : fix the export preferences system. Using annotation might be a good
 * idea for this.
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
     * Base drawing specifications for <em>new</em> documents.
     */
    private DrawingSpecification defaultDrawingSpecifications = new DrawingSpecificationsImplementation();

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
    private final HTMLExporter htmlExporter = new HTMLExporter();

    private ExportPreferences exportPreferences;

    public JSeshApplicationBase() {
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

    /**
     * Save the part related to drawing preferences...
     *
     * @param preferences
     */
    private void loadDrawingSpecificationPreferences(Preferences preferences) {
//        defaultDrawingSpecifications.setColorForProperty("det", Color.YELLOW);
//        defaultDrawingSpecifications.setColorForProperty("blue", Color.BLUE);
        // Dimensions...
        defaultDrawingSpecifications.setStandardSignHeight((float) preferences.getDouble(JSeshInfoConstants.JSESH_STANDARD_SIGN_HEIGHT, 18.0));
        defaultDrawingSpecifications.setLineSkip((float) preferences.getDouble(JSeshInfoConstants.JSESH_LINE_SKIP, 6));
        defaultDrawingSpecifications.setColumnSkip((float) preferences.getDouble(JSeshInfoConstants.JSESH_COLUMN_SKIP, 10));
        defaultDrawingSpecifications.setMaxCadratHeight((float) preferences.getDouble(JSeshInfoConstants.JSESH_MAX_QUADRANT_HEIGHT, 18));
        defaultDrawingSpecifications.setMaxCadratWidth((float) preferences.getDouble(JSeshInfoConstants.JSESH_MAX_QUADRANT_WIDTH, 22));
        defaultDrawingSpecifications.setSmallSignsCentered(preferences.getBoolean(JSeshInfoConstants.JSESH_SMALL_SIGNS_CENTRED, false));

        defaultDrawingSpecifications.setSmallBodyScaleLimit(preferences.getDouble(
                JSeshInfoConstants.JSESH_SMALL_BODY_SCALE_LIMIT, 12.0));
        defaultDrawingSpecifications.setCartoucheLineWidth((float) preferences
                .getDouble(JSeshInfoConstants.JSESH_CARTOUCHE_LINE_WIDTH, 1.0));

        // Shading
        if (preferences.getBoolean(JSeshInfoConstants.JSESH_USE_LINES_FOR_SHADING, true)) {
            defaultDrawingSpecifications.setShadingStyle(ShadingStyle.LINE_HATCHING);
        } else {
            defaultDrawingSpecifications.setShadingStyle(ShadingStyle.GRAY_SHADING);
        }
    }

    private void saveDrawingSpecificationPreferences(Preferences preferences) {
        // From default document...
        preferences.putDouble(JSeshInfoConstants.JSESH_STANDARD_SIGN_HEIGHT, defaultDrawingSpecifications.getStandardSignHeight()); //ok
        preferences.putDouble(JSeshInfoConstants.JSESH_LINE_SKIP, defaultDrawingSpecifications.getLineSkip());//ok
        preferences.putDouble(JSeshInfoConstants.JSESH_COLUMN_SKIP, defaultDrawingSpecifications.getColumnSkip());//ok
        preferences.putDouble(JSeshInfoConstants.JSESH_MAX_QUADRANT_HEIGHT, defaultDrawingSpecifications.getMaxCadratHeight());//ok
        preferences.putDouble(JSeshInfoConstants.JSESH_MAX_QUADRANT_WIDTH, defaultDrawingSpecifications.getMaxCadratWidth());//ok
        preferences.putBoolean(JSeshInfoConstants.JSESH_SMALL_SIGNS_CENTRED, defaultDrawingSpecifications.isSmallSignsCentered());//ok
        preferences.putDouble(JSeshInfoConstants.JSESH_SMALL_BODY_SCALE_LIMIT, defaultDrawingSpecifications.getSmallBodyScaleLimit());//ok
        preferences.putBoolean(JSeshInfoConstants.JSESH_USE_LINES_FOR_SHADING, defaultDrawingSpecifications.getShadingStyle().equals(ShadingStyle.LINE_HATCHING));//ok
        preferences.putDouble(JSeshInfoConstants.JSESH_CARTOUCHE_LINE_WIDTH, defaultDrawingSpecifications.getCartoucheLineWidth());	//ok
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
    public DrawingSpecification getDefaultDrawingSpecifications() {
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
                        .getquadrantHeightSmall());
                break;
            case LARGE:
                result.setCadratHeight((int) exportPreferences
                        .getquadrantHeightLarge());
                break;
            case FILE:
                result.setCadratHeight((int) exportPreferences
                        .getquadrantHeightFile());
                break;
            case WYSIWYG:
                result.setCadratHeight((int) exportPreferences
                        .getquadrantHeightWysiwyg());
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
        DefaultHieroglyphicFontManager.getInstance().setDirectory(fontInfo.getHieroglyphsFolder());
        DrawingSpecification d = defaultDrawingSpecifications.copy();
        fontInfo.applyToDrawingSpecifications(d);
        defaultDrawingSpecifications = d;
    }

    public void setDefaultDrawingSpecifications(
            DrawingSpecification drawingSpecifications) {
        defaultDrawingSpecifications = drawingSpecifications.copy();
    }

}

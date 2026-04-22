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

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

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
import jsesh.graphics.export.rtf.RTFExportGraphicFormat;
import jsesh.graphics.export.rtf.RTFExportPreferences;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
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

    public JSeshApplicationBase(JSeshUserSignLibraryConfiguration appDef) {
        JSeshUserSignLibraryConfiguration appdef = new JSeshUserSignLibraryConfiguration();
        appdef.hieroglyphDatabase();
        appdef.glossary();
        appdef.hieroglyphShapeRepository();
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
        // extract geometry and the like from the preferences.
        updateJSeshStyleFromPreferences(preferences);
        // extract font information from the preferences.
        setFontInfo(FontInfo.getFromPreferences(preferences));
        quickPDFExportDirectory = new File(preferences.get(
                QUICK_PDF_EXPORT_DIRECTORY, new File(getCurrentDirectory(),
                        "quickPdf").getAbsolutePath()));
        exportPreferences = ExportPreferences.getFromPreferences(preferences);
        clipboardPreferences = MDCClipboardPreferences.getFromPreferences(preferences);
    }

    private void updateJSeshStyleFromPreferences(Preferences preferences) {
        throw new UnsupportedOperationException("Unimplemented method 'updateJSeshStyleFromPreferences'");
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

    /**
     * Return the current font info configuration.
     * 
     * @return
     */
    public FontInfo getFontInfo() {
        return fontInfo;
    }

    /**
     * Sets the font info configuration.
     * 
     * @param fontInfo
     */
    public void setFontInfo(FontInfo fontInfo) {
        this.fontInfo = fontInfo;
        // Sets the jseshStyle
        // and update the hieroglyphic font.
        throw new UnsupportedOperationException("Write me!");
    }

}

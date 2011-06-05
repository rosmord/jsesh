package jsesh.jhotdraw;

import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import jsesh.editor.MDCModelTransferableBroker;
import jsesh.graphics.export.RTFExportPreferences;
import jsesh.graphics.export.RTFExportPreferences.RTFExportGranularity;
import jsesh.graphics.export.pdfExport.PDFExportPreferences;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.clipboard.JSeshPasteFlavors;
import jsesh.mdcDisplayer.clipboard.MDCClipboardPreferences;
import jsesh.mdcDisplayer.clipboard.MDCModelTransferable;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;
import jsesh.mdcDisplayer.preferences.ShadingStyle;
import jsesh.utils.JSeshWorkingDirectory;

/**
 * Framework-agnostic part of the JSesh application. Deals with all information
 * which is not specific to the JHotdraw framework.
 * 
 * TODO : fix the export preferences system.
 * @author rosmord
 */

public class JSeshApplicationBase implements MDCModelTransferableBroker {

	private static final String CURRENT_HIEROGLYPHS_SOURCE = "currentHieroglyphsSource";

	/**
	 * Folder for exporting small pdf pictures (useful for press release).
	 */
	private File quickPDFExportDirectory;

	private File currentDirectory;

	/**
	 * Export information for copy/paste.
	 */
	private RTFExportPreferences[] rtfExportPreferences = new RTFExportPreferences[2];

	/**
	 * The RTF Preference currently selected for copy/paste.
	 * 
	 * We will change the way everything is done there. Currently : SMALL and
	 * LARGE are directly picked from the RTFExportPreferences array (resp. 0
	 * and 1). FILE uses the 0 and WYSIWYG the 1 slots data, but only for the
	 * sizes.
	 */
	private ExportType exportType = ExportType.SMALL;

	private PDFExportPreferences pdfExportPreferences = new PDFExportPreferences();

	/**
	 * Base drawing specifications for <em>new</em> documents.
	 */
	private DrawingSpecificationsImplementation drawingSpecifications = new DrawingSpecificationsImplementation();

	/**
	 * Other information for copy/paste (file formats, for instance).
	 */
	private MDCClipboardPreferences clipboardPreferences = new MDCClipboardPreferences();

	/**
	 * Folder containing the user's hieroglyphs.
	 */
	private File currentHieroglyphsSource;

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
	public void loadPreferences() {

		Preferences preferences = Preferences.userNodeForPackage(this
				.getClass());

		// Dimensions...
		drawingSpecifications.setSmallBodyScaleLimit(preferences.getDouble(
				"smallBodyScaleLimit", 12.0));
		drawingSpecifications.setCartoucheLineWidth((float) preferences
				.getDouble("cartoucheLineWidth", 1.0));

		// Shading
		if (preferences.getBoolean("useLinesForShading", true)) {
			drawingSpecifications.setShadingStyle(ShadingStyle.LINE_HATCHING);
		} else {
			drawingSpecifications.setShadingStyle(ShadingStyle.GRAY_SHADING);
		}

		// working directories
		File workingDirectory = JSeshWorkingDirectory.getWorkingDirectory();

		// Quick pdf export...
		quickPDFExportDirectory = new File(preferences.get(
				"quickPdfExportDirectory", new File(workingDirectory,
						"quickPdf").getAbsolutePath()));

		// Sets the source for user glyphs.
		loadCurrentHieroglyphicSource(preferences);

		// Cut and paste preferences.
		String prefNames[] = {"small","large", "file" };
		int defaultHeight[] = { 20, 12, 12 };
		RTFExportGranularity defaultGranularity[] = {
				RTFExportGranularity.ONE_PICTURE_PER_CADRAT,
				RTFExportGranularity.ONE_PICTURE_PER_CADRAT,
				RTFExportGranularity.GROUPED_CADRATS };

		for (int i = 0; i < rtfExportPreferences.length; i++) {
			String name = prefNames[i];
			rtfExportPreferences[i]= new RTFExportPreferences();
			rtfExportPreferences[i].setCadratHeight(preferences.getInt("rtf_"
					+ name + "_size", defaultHeight[i]));

			rtfExportPreferences[i].setExportGranularity(RTFExportGranularity
					.getGranularity(preferences.getInt("rtf_" + name + "_mode",
							defaultGranularity[i].getId())));

			rtfExportPreferences[i]
					.setExportGraphicFormat(RTFExportPreferences.RTFExportGraphicFormat
							.getMode(preferences.getInt("rtf_" + name
									+ "_graphicformat", 0)));

			rtfExportPreferences[i].setRespectOriginalTextLayout(preferences
					.getBoolean("rtf_" + name + "_respect_layout", true));
		}

		// Clipboard preferences...
		clipboardPreferences.setImageWanted(preferences.getBoolean(
				"imageWanted", false));
		clipboardPreferences.setPdfWanted(preferences.getBoolean("pdfWanted",
				false));
		clipboardPreferences.setRtfWanted(preferences.getBoolean("rtfWanted",
				true));
		clipboardPreferences.setTextWanted(preferences.getBoolean("textWanted",
				true));
	}

	/**
	 * Sets the source for user glyphs.
	 * 
	 * @param preferences
	 */
	private void loadCurrentHieroglyphicSource(Preferences preferences) {
		// directory looked for loading external hieroglyph fonts

		String currentHieroglyphicPath = null;
		if (currentHieroglyphsSource != null) {
			currentHieroglyphicPath = currentHieroglyphsSource
					.getAbsolutePath();
		}

		currentHieroglyphicPath = preferences.get(CURRENT_HIEROGLYPHS_SOURCE,
				currentHieroglyphicPath);

		if (currentHieroglyphicPath != null)
			currentHieroglyphsSource = new File(currentHieroglyphicPath);
	}

	private void saveCurrentHieroglyphicSource(Preferences preferences) {
		if (currentHieroglyphsSource == null)
			preferences.remove(CURRENT_HIEROGLYPHS_SOURCE);
		else {
			preferences.put(CURRENT_HIEROGLYPHS_SOURCE,
					currentHieroglyphsSource.getAbsolutePath());
		}
	}

	public void savePreferences() {
		Preferences preferences = Preferences.userNodeForPackage(this
				.getClass());

		// Also save the creator software version.
		preferences.put("prefversion", "5.0");

		saveCurrentHieroglyphicSource(preferences);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	public DrawingSpecificationsImplementation getDrawingSpecifications() {
		return drawingSpecifications;
	}

	public MDCModelTransferable buildTransferable(TopItemList top) {
		return buildTransferable(top,
				JSeshPasteFlavors.getTransferDataFlavors(clipboardPreferences));

	}

	public MDCModelTransferable buildTransferable(TopItemList top,
			DataFlavor[] dataFlavors) {

		MDCModelTransferable result = new MDCModelTransferable(dataFlavors, top);
		result.setDrawingSpecifications(getDrawingSpecifications());
		result.setRtfPreferences(getCurrentRTFPreferences());
		result.setClipboardPreferences(clipboardPreferences);
		return result;
	}

	private RTFExportPreferences getCurrentRTFPreferences() {
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
		if (exportType == ExportType.FILE)
			throw new IllegalArgumentException(
					"Incorrect export type for copy/paste " + ExportType.FILE);
		this.exportType = exportType;
	}

	public File getCurrentDirectory() {
		return currentDirectory;
	}

	public void setCurrentDirectory(File currentDirectory) {
		this.currentDirectory = currentDirectory;
	}

	public PDFExportPreferences getPDFExportPreferences() {
		return pdfExportPreferences;
	}

	/**
	 * Returns suitable RTF preferences for a type of export.
	 * @param exportType
	 * @return
	 */
	public RTFExportPreferences getRTFExportPreferences(ExportType exportType) {
		switch (exportType) {
		case WYSIWYG:
		case FILE:
			RTFExportPreferences prefs = new RTFExportPreferences();
			prefs.setCadratHeight(rtfExportPreferences[1].getCadratHeight());
			prefs.setExportGraphicFormat(rtfExportPreferences[1]
					.getExportGraphicFormat());
			prefs.setExportGranularity(RTFExportGranularity.ONE_LARGE_PICTURE);
			prefs.setRespectOriginalTextLayout(true);
			return prefs;
		case SMALL:
			return rtfExportPreferences[0];
		case LARGE:
			return rtfExportPreferences[1];
		default:
			throw new RuntimeException("???? invalid value for exportType "
					+ exportType);
		}
	}
}

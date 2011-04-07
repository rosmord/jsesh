package jsesh.jhotdraw;

import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import jsesh.editor.MDCModelTransferableBroker;
import jsesh.graphics.export.RTFExportPreferences;
import jsesh.graphics.export.RTFExportPreferences.RTFExportGranularity;
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
 * @author rosmord
 */

public class JSeshApplicationBase implements MDCModelTransferableBroker {

	private static final String CURRENT_HIEROGLYPHS_SOURCE = "currentHieroglyphsSource";

	/**
	 * Folder for exporting small pdf pictures (useful for press release).
	 */
	private File quickPDFExportDirectory;

	/**
	 * Export information for copy/paste.
	 */
	private RTFExportPreferences[] rtfExportPreferences = new RTFExportPreferences[3];

	/**
	 * The RTF Preference currently selected for copy/paste.
	 * 
	 * Note that if the index is 0 or 1, it will be taken from the export preference
	 * array. Index 2 corresponds to "Wysiwyg" export. The dimensional data will
	 * be taken from the "large" export preferences, but other data will be
	 * ignored.
	 *
	 */
	private int rtfExportPreferencesIndex= 0;

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
		String prefNames[] = { "large", "small", "file" };
		int defaultHeight[] = { 20, 12, 12 };
		RTFExportGranularity defaultGranularity[] = {
				RTFExportGranularity.ONE_PICTURE_PER_CADRAT,
				RTFExportGranularity.ONE_PICTURE_PER_CADRAT,
				RTFExportGranularity.GROUPED_CADRATS };

		for (int i = 0; i < 3; i++) {
			String name = prefNames[i];
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
		if (rtfExportPreferencesIndex == 2) {
			RTFExportPreferences prefs = new RTFExportPreferences();
			prefs.setCadratHeight(rtfExportPreferences[1].getCadratHeight());
			prefs.setExportGraphicFormat(rtfExportPreferences[1]
					.getExportGraphicFormat());
			prefs.setExportGranularity(RTFExportGranularity.ONE_LARGE_PICTURE);
			prefs.setRespectOriginalTextLayout(true);
			return prefs;
		} else {
			return rtfExportPreferences[rtfExportPreferencesIndex];
		}
	}
	
	/**
	 * Choose which copy/paste configuration to use.
	 * 0, 1 or 2. 2 is Wysiwyg.
	 * @param configurationNumber 0, 1 or 2.
	 */
	public void selectCopyPasteConfiguration(int configurationNumber) {
		rtfExportPreferencesIndex = configurationNumber;
	}

}

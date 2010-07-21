package jsesh.mdcDisplayer.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;

import jsesh.mdc.model.ListOfTopItems;

/**
 * Non Standard Paste flavour availables in JSesh.
 * 
 * @author rosmord
 *
 */
public class JSeshPasteFlavors {

	public static final DataFlavor MACPictFlavor = new DataFlavor(
			"image/x-macpict", null);

	public static final DataFlavor PDFFlavor = new DataFlavor(
			"application/pdf", null);

	public static final DataFlavor ListOfTopItemsFlavor = new DataFlavor(
			ListOfTopItems.class, null);

	public static final DataFlavor RTFFlavor = new DataFlavor("text/rtf", null);

	/**
	 * Returns the possible data flavours, depending on preferences.
	 * The set of ALL possible data flavors is currently :  {@link #PDFFlavor}, 
	 * {@link #RTFFlavor}, {@link DataFlavor#imageFlavor}, {@link #ListOfTopItemsFlavor}
	 * and {@link DataFlavor#stringFlavor}.
	 * 
	 * @param clipboardPreferences
	 * @return
	 */
	public static DataFlavor[] getTransferDataFlavors(MDCClipboardPreferences clipboardPreferences) {
		ArrayList list = new ArrayList();
		
		list.add(JSeshPasteFlavors.ListOfTopItemsFlavor);

		if (clipboardPreferences.isPdfWanted()) {
			list.add(JSeshPasteFlavors.PDFFlavor);
		}

		if (clipboardPreferences.isRtfWanted()) {
			list.add(JSeshPasteFlavors.RTFFlavor);
		}

		if (clipboardPreferences.isImageWanted()) {
			list.add(DataFlavor.imageFlavor);
		}

		if (clipboardPreferences.isTextWanted()) {
			list.add(DataFlavor.stringFlavor);
		}

		return (DataFlavor[]) list.toArray(new DataFlavor[list.size()]);
	}

}

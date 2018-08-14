package jsesh.mdcDisplayer.clipboard;

import java.awt.datatransfer.DataFlavor;
import java.util.ArrayList;

import org.qenherkhopeshef.graphics.vectorClipboard.EMFTransferable;

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
     * Returns the possible data flavours, depending on preferences. The set of
     * ALL possible data flavors is currently : null     {@link #PDFFlavor}, 
	 * {@link #RTFFlavor}, {@link DataFlavor#imageFlavor}, {@link #ListOfTopItemsFlavor}
     * and {@link DataFlavor#stringFlavor}.
     *
     * @param clipboardPreferences
     * @return
     */
    public static DataFlavor[] getTransferDataFlavors(MDCClipboardPreferences clipboardPreferences) {
        ArrayList<DataFlavor> list = new ArrayList<>();

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

        if (clipboardPreferences.isEmfWanted()) {
            // Direct-to-clipboard Window Copy/paste. 
            // note that loading the class EMFTransferable will perform some
            // static magic with SystemFlavorMap
            list.add(EMFTransferable.EMF_FLAVOR);
        }
        return list.toArray(new DataFlavor[list.size()]);
    }

}

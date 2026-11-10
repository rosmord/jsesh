package jsesh.ui.clipboard;

import java.awt.datatransfer.DataFlavor;

import jsesh.model.ListOfTopItems;

/**
 * Non Standard Paste flavour availables in JSesh.
 *
 * @author rosmord
 *
 */
public class JSeshPasteFlavors {

    private JSeshPasteFlavors() {}

    public static final DataFlavor MACPictFlavor = new DataFlavor(
            "image/x-macpict", null);

    public static final DataFlavor PDFFlavor = new DataFlavor(
            "application/pdf", null);

    public static final DataFlavor ListOfTopItemsFlavor = new DataFlavor(
            ListOfTopItems.class, null);

    public static final DataFlavor RTFFlavor = new DataFlavor("text/rtf", null);

    /// Copied text with the embedded signs it uses ([JSeshFragment]).
    /// Offered instead of [#ListOfTopItemsFlavor] when the text uses
    /// embedded signs.
    public static final DataFlavor JSeshFragmentFlavor = new DataFlavor(
            JSeshFragment.class, null);

   

}

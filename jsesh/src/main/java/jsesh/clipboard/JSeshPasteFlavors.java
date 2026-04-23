package jsesh.clipboard;

import java.awt.datatransfer.DataFlavor;

import jsesh.mdc.model.ListOfTopItems;

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

   

}

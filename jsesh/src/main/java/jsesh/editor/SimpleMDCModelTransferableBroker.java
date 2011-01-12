/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editor;

import java.awt.datatransfer.DataFlavor;

import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.clipboard.JSeshPasteFlavors;
import jsesh.mdcDisplayer.clipboard.MDCClipboardPreferences;
import jsesh.mdcDisplayer.clipboard.MDCModelTransferable;

/**
 * Trivial implementation of a MDCModelTransferableBroker.
 */
public class SimpleMDCModelTransferableBroker implements
		MDCModelTransferableBroker {

	/* (non-Javadoc)
	 * @see jsesh.editor.MDCModelTransferableBroker#buildTransferable(jsesh.mdc.model.TopItemList)
	 */
	public MDCModelTransferable buildTransferable(TopItemList top) {
		return buildTransferable(top, JSeshPasteFlavors.getTransferDataFlavors(new MDCClipboardPreferences()));
			}

	public MDCModelTransferable buildTransferable(TopItemList top,
			DataFlavor[] dataFlavors) {
		return new MDCModelTransferable(dataFlavors, top);

	}

}

/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editor;

import java.awt.datatransfer.DataFlavor;

import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.clipboard.MDCModelTransferable;

/**
 * Interface for objects which can provide transferable for use with the clipboard.
 * <p> Thanks to this, we can:
 * <ul> 
 * <li>either provide a simple, standalone implementation
 * <li>or, use a shared implementation which can allow us to configure the transfer parameter.
 * </ul>
 * Currently, we use this in order to be able to change the copy and paste preferences in an 
 * uniform way through JSesh.
 * @author rosmord
 */
public interface MDCModelTransferableBroker {

	/**
	 * Create a transferable for the given TopItemList
	 * @param top
	 * @return
	 */
	MDCModelTransferable buildTransferable(TopItemList top);

	MDCModelTransferable buildTransferable(TopItemList top,
			DataFlavor[] dataFlavors);

}

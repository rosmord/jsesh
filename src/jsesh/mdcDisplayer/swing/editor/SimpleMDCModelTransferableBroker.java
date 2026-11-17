/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.editor;

import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.clipboard.MDCModelTransferable;

/**
 * Trivial implementation of a MDCModelTransferableBroker.
 */
public class SimpleMDCModelTransferableBroker implements
		MDCModelTransferableBroker {

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.editor.MDCModelTransferableBroker#buildTransferable(jsesh.mdc.model.TopItemList)
	 */
	public MDCModelTransferable buildTransferable(TopItemList top) {
		return new MDCModelTransferable(top);
	}

}

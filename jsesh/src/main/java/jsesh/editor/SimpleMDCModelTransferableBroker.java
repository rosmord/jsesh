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
package jsesh.editor;

import java.awt.datatransfer.DataFlavor;

import jsesh.clipboard.MDCClipboardPreferences;
import jsesh.clipboard.MDCModelTransferable;
import jsesh.graphics.export.rtf.RTFExportPreferences;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.context.JSeshRenderContext;

/**
 * Trivial implementation of a MDCModelTransferableBroker.
 */
public class SimpleMDCModelTransferableBroker implements
        MDCModelTransferableBroker {
   
    private RTFExportPreferences rtfPrefs = new RTFExportPreferences();

    /**
     * Change the RTF export preferences to use for the transferables created by this broker.
     * <p> Note that this will not change the preferences of already created transferables.
     * @param rtfPrefs the rtfPrefs to set      
     */

    public void setRTFExportPreferences(RTFExportPreferences rtfPrefs) {
        this.rtfPrefs = rtfPrefs;
    }

    @Override
    public MDCModelTransferable buildTransferable(TopItemList top, JSeshRenderContext renderContext) {
        return buildTransferable(top, renderContext, new MDCClipboardPreferences().getTransferDataFlavors());
    }

    @Override
    public MDCModelTransferable buildTransferable(TopItemList top, JSeshRenderContext renderContext,
            DataFlavor[] dataFlavors) {
        return new MDCModelTransferable(dataFlavors, top, rtfPrefs, renderContext);        
    }

}

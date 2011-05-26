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
package jsesh.editor.actions.edit;

import java.awt.event.ActionEvent;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actions.AppDefaultFactory;
import jsesh.editor.actionsUtils.EditorAction;

import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.BundledActionFiller;

/**
 * @author S. Rosmorduc
 *  
 */
@SuppressWarnings("serial")
public class PasteAction extends EditorAction {
	
	public static String ID= "jsesh.edit.paste";
	
	public PasteAction(JMDCEditor editor) {
		super(editor);
		AppDefaults appDefaults= AppDefaultFactory.getAppDefaults();
		BundledActionFiller.initAction(this, appDefaults);
	}

	/**
	 * Init a new PasteAction for a given editor.
	 * <p> The AppDefault is passed as an argument to avoid creating it over and over.
	 * @param editor
	 * @param appDefaults
	 */
	public PasteAction(JMDCEditor editor, AppDefaults appDefaults) {
		super(editor);	
		BundledActionFiller.initAction(this, appDefaults);
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (this.editor.isEditable())
			this.editor.paste();
	}

}
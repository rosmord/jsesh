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
package jsesh.editor.actionsUtils;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import jsesh.editor.JMDCEditor;

/**
 * Base action for applied to a JMDCEditor.
 * the action object can be created without editor, in which case it will be disabled.
 * This allows one to use the actions when no editor is available.
 * (in particular for JHotdraw). 
 * @author rosmord
 */
@SuppressWarnings("serial")
public abstract class EditorAction extends AbstractAction {

	protected JMDCEditor editor;
	
	public EditorAction(JMDCEditor editor) {
		super();
		this.editor = editor;
		if (editor == null)
			setEnabled(false);
	}

	public EditorAction(JMDCEditor editor, String name) {
		super(name);
		this.editor= editor;
		if (editor == null)
			setEnabled(false);
	}

	public EditorAction(JMDCEditor editor, String name, Icon icon) {
		super(name, icon);
		this.editor= editor;
		if (editor == null)
			setEnabled(false);
	}

}

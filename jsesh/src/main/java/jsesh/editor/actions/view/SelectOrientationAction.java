/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editor.actions.view;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actions.generic.EditorAction;
import jsesh.mdc.constants.TextOrientation;

/**
 * @author rosmord
 *
 */
public class SelectOrientationAction extends EditorAction {

	TextOrientation orientation;
	
	/**
	 * @param horizontal
	 */
	public SelectOrientationAction(JMDCEditor editor, TextOrientation horizontal) {
		super(editor);
		this.orientation= horizontal;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (editor.isEditable())
			editor.setTextOrientation(orientation);
	}

}

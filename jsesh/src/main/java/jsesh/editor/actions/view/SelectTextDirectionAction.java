/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editor.actions.view;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;
import jsesh.mdc.constants.TextDirection;

/**
 * @author rosmord
 *
 */
public class SelectTextDirectionAction extends EditorAction {
	TextDirection direction;

	/**
	 * @param direction
	 */
	public SelectTextDirectionAction(JMDCEditor editor, TextDirection direction) {
		super(editor);
		this.direction = direction;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (editor.isEditable())
			editor.setTextDirection(direction);
	}
	
	
}

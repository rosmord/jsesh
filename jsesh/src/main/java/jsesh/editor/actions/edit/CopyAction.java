/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor.actions.edit;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.AbstractSelectionAction;
import jsesh.swing.KeyUtils;

/**
 * 
 * @author S. Rosmorduc
 */
@SuppressWarnings("serial")
public class CopyAction extends AbstractSelectionAction {
	
	public CopyAction(JMDCEditor editor) {
		super(editor, "copy");
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		editor.copy();
	}
}

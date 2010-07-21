/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.AbstractSelectionAction;
import jsesh.swingUtils.KeyUtils;

/**
 * @author S. Rosmorduc
 *
 *
 *
 */
public class CopyAction extends AbstractSelectionAction {
	
	public CopyAction(JMDCEditor editor) {
		super(editor, "copy");
		putValue(ACCELERATOR_KEY, KeyUtils.buildCommandShortCut(KeyEvent.VK_C));
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		editor.copy();
	}

}

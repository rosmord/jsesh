/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

import jsesh.editor.JMDCEditor;
import jsesh.swing.KeyUtils;

/**
 * @author S. Rosmorduc
 *  
 */
public class PasteAction extends EditorAction {

	public PasteAction(JMDCEditor editor) {
		super(editor, "paste");
		putValue(ACCELERATOR_KEY, KeyUtils.buildCommandShortCut(KeyEvent.VK_V));
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
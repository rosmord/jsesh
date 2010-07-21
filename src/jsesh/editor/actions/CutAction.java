/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import jsesh.editor.JMDCEditor;
import jsesh.mdcDisplayer.swing.actionsUtils.AbstractSelectionAction;
import jsesh.swingUtils.KeyUtils;

/**
 * @author S. Rosmorduc
 * 
 */
public class CutAction extends AbstractSelectionAction {

	public CutAction(JMDCEditor editor) {
		super(editor, "cut");
		putValue(ACCELERATOR_KEY, KeyUtils.buildCommandShortCut(KeyEvent.VK_X));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (editor.isEditable())
			editor.cut();
	}

}

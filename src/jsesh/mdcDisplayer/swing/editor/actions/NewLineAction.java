/*
 * Created on 1 janv. 2002
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jsesh.mdcDisplayer.swing.editor.JMDCEditor;

/**
 * @author S. Rosmorduc
 *
 */
public class NewLineAction extends AbstractAction {
	public NewLineAction() {
		super("New Line");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		JMDCEditor d = (JMDCEditor) e.getSource();
		d.getWorkflow().insertNewLine();
	}
	

}

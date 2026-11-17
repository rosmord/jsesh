/*
 * Created on 2 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import jsesh.mdcDisplayer.swing.editor.JMDCEditor;

/**
 * @author S. Rosmorduc
 *
 */
public class ExpandSelectionAction extends AbstractAction implements Action {

	private int dir;
	/**
	 * Action that expand the selection in the direction dir.
	 * @param dir : -1 for left, 1 for right.
	 */
	public ExpandSelectionAction(int dir) {
		this.dir= dir;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		JMDCEditor d=  (JMDCEditor) e.getSource();
		d.getWorkflow().expandSelection(dir);
	}

}

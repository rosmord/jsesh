/*
 * Created on 30 sept. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.mdcDisplayer.swing.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jsesh.mdcDisplayer.swing.editor.JMDCEditor;

/**
 * Action of adding an element to the lower level of a cadrat. 
 *   @author rosmord
 *
 */
public class AddLowerLevelAction extends AbstractAction {
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		JMDCEditor d=  (JMDCEditor) e.getSource();
		d.getWorkflow().addSignInLowerLevel();
	}

}

/*
 * Created on 30 sept. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.mdcDisplayer.swing.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jsesh.mdcDisplayer.swing.editor.JMDCEditor;

/**
 * TODO describe type 
 *   @author rosmord
 *
 */
public class GoLeftAction extends AbstractAction {

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		JMDCEditor d=  (JMDCEditor) e.getSource();
		if (d.getDrawingSpecifications().getTextDirection().isLeftToRight()) {
			if (d.getDrawingSpecifications().getTextOrientation().isHorizontal())
				d.getWorkflow().cursorPrevious();
			else
				d.getWorkflow().cursorUp();
		} else {
			if (d.getDrawingSpecifications().getTextOrientation().isHorizontal()) {
				d.getWorkflow().cursorNext();
			} else {
				d.getWorkflow().cursorDown();
			}
		}
	}

}

/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdcDisplayer.swing.editor.JMDCEditor;

/**
 * @author rosmord
 *
 */
public class SelectTextDirectionAction extends AbstractAction {
	TextDirection direction;

	/**
	 * @param direction
	 */
	public SelectTextDirectionAction(TextDirection direction) {
		this.direction = direction;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		JMDCEditor d=  (JMDCEditor) e.getSource();
		d.setTextDirection(direction);
	}
	
	
}

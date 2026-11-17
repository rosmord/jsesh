/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jsesh.mdc.constants.TextOrientation;
import jsesh.mdcDisplayer.swing.editor.JMDCEditor;

/**
 * @author rosmord
 *
 */
public class SelectOrientationAction extends AbstractAction {

	TextOrientation orientation;
	/**
	 * @param horizontal
	 */
	public SelectOrientationAction(TextOrientation horizontal) {
		this.orientation= horizontal;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		JMDCEditor d=  (JMDCEditor) e.getSource();
		if (d.isEditable())
			d.setTextOrientation(orientation);
	}

}

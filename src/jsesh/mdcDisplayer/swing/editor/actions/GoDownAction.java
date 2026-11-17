/*
 * Created on 10 nov. 2004
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
public class GoDownAction extends AbstractAction {

	public void actionPerformed(ActionEvent e) {
		JMDCEditor d = (JMDCEditor) e.getSource();

		if (d.getDrawingSpecifications().getTextOrientation().isHorizontal())
			d.getWorkflow().cursorDown();
		else
			d.getWorkflow().cursorNext();

	}

}

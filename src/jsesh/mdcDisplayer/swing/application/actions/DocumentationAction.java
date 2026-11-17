/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.application.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jsesh.docdisplayer.DocDisplayer;

public class DocumentationAction extends AbstractAction {
	public DocumentationAction(String name) {
		super(name);
	}

	public void actionPerformed(ActionEvent e) {
		DocDisplayer.getInstance().setVisible(true);
	}
}
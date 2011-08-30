/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editorSoftware.actions;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jsesh.swing.about.AboutDisplayer;

public class AboutAction extends AbstractAction {
	Frame frame;
	public AboutAction(String name, Frame mainFrame) {
		super(name);
		this.frame= mainFrame;
	}

	public void actionPerformed(ActionEvent e) {
		AboutDisplayer about= new AboutDisplayer(frame);
		about.show();
	}
}
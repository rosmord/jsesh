package jsesh.mdcDisplayer.swing.editor.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

import jsesh.mdcDisplayer.swing.editor.JMDCEditor;

public class SetModeAction extends AbstractAction {

	private char mode;
	
	public SetModeAction(char c) {
		this.mode= c;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JMDCEditor) {
			JMDCEditor ed = (JMDCEditor) e.getSource();
			ed.getWorkflow().setMode(mode);
		}
	}
	
}
package jsesh.mdcDisplayer.swing.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jsesh.mdcDisplayer.swing.editor.JMDCEditor;

public class RedoAction extends AbstractAction {

	public RedoAction() {
		super("redo");
	}
	
	public void actionPerformed(ActionEvent e) {
		JMDCEditor d=  (JMDCEditor) e.getSource();
		if (d.isEditable())
			d.getWorkflow().redo();	
	}

}

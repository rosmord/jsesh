package jsesh.mdcDisplayer.swing.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jsesh.mdcDisplayer.swing.editor.JMDCEditor;

public class UndoAction extends AbstractAction {

	public UndoAction() {
		super("undo");
	}
	
	public void actionPerformed(ActionEvent e) {
		JMDCEditor d=  (JMDCEditor) e.getSource();
		if (d.isEditable())
			d.getWorkflow().undo();	
	}

}

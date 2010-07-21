package jsesh.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import jsesh.editor.JMDCEditor;
import jsesh.swingUtils.KeyUtils;

public class UndoAction extends EditorAction {

	public UndoAction(JMDCEditor editor) {
		super(editor,"undo");
		putValue(ACCELERATOR_KEY, KeyUtils.buildCommandShortCut(KeyEvent.VK_Z));
	}
	
	public void actionPerformed(ActionEvent e) {
		if (editor.isEditable())
			editor.getWorkflow().undo();	
	}

}

package jsesh.editor.actions;

import java.awt.event.ActionEvent;

import jsesh.editor.JMDCEditor;

public class SetModeAction extends EditorAction {

	private char mode;
	
	public SetModeAction(JMDCEditor editor, char c) {
		super(editor);
		this.mode= c;
	}

	public void actionPerformed(ActionEvent e) {
		editor.getWorkflow().setMode(mode);
	}
	
}
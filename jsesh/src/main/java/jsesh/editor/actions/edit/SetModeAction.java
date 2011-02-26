package jsesh.editor.actions.edit;

import java.awt.event.ActionEvent;

import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.BundledActionFiller;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;

@SuppressWarnings("serial")
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
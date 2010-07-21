package jsesh.editor.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import jsesh.editor.JMDCEditor;

public abstract class EditorAction extends AbstractAction {

	protected JMDCEditor editor;
	
	public EditorAction(JMDCEditor editor) {
		super();
		this.editor = editor;
	}

	public EditorAction(JMDCEditor editor, String name) {
		super(name);
		this.editor= editor;
	}

	public EditorAction(JMDCEditor editor, String name, Icon icon) {
		super(name, icon);
		this.editor= editor;
	}


	

}

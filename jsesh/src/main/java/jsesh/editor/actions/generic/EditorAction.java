package jsesh.editor.actions.generic;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import jsesh.editor.JMDCEditor;

/**
 * Base action for applied to a JMDCEditor.
 * the action object can be created without editor, in which case it will be disabled.
 * This allows one to use the actions when no editor is available.
 * (in particular for JHotdraw). 
 * @author rosmord
 */
@SuppressWarnings("serial")
public abstract class EditorAction extends AbstractAction {

	protected JMDCEditor editor;
	
	public EditorAction(JMDCEditor editor) {
		super();
		this.editor = editor;
		if (editor == null)
			setEnabled(false);
	}

	public EditorAction(JMDCEditor editor, String name) {
		super(name);
		this.editor= editor;
		if (editor == null)
			setEnabled(false);
	}

	public EditorAction(JMDCEditor editor, String name, Icon icon) {
		super(name, icon);
		this.editor= editor;
		if (editor == null)
			setEnabled(false);
	}

}

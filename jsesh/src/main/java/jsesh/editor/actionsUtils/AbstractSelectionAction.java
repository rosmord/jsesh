/*
 * Created on 1 janv. 2002
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor.actionsUtils;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actions.generic.EditorAction;
import jsesh.editor.caret.MDCCaret;
import jsesh.editor.caret.MDCCaretChangeListener;

/**
 * An abstract class for actions which work on the selection.
 * 
 * @author S. Rosmorduc
 *  
 */
abstract public class AbstractSelectionAction extends EditorAction implements
		MDCCaretChangeListener {
	MDCCaret caret= null;

	
	/**
	 * 
	 */
	public AbstractSelectionAction(JMDCEditor editor) {
		super(editor);
	}
	
	/**
	 * @param name
	 */
	public AbstractSelectionAction(JMDCEditor editor,String name) {
		super(editor,name);
	}

	/**
	 * @param name
	 * @param icon
	 */
	public AbstractSelectionAction(JMDCEditor editor, String name, Icon icon) {
		super(editor, name, icon);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.MDCCaretChangeListener#caretChanged(jsesh.mdcDisplayer.draw.MDCCaret)
	 */
	public void caretChanged(MDCCaret caret) {
		setEnabled(caret.hasMark());
	}
	
}
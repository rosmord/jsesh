/*
 * Created on 1 janv. 2002
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.actions;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import jsesh.mdcDisplayer.draw.MDCCaret;
import jsesh.mdcDisplayer.draw.MDCCaretChangeListener;

/**
 * An abstract class for actions which work on the selection.
 * 
 * @author S. Rosmorduc
 *  
 */
abstract public class AbstractSelectionAction extends AbstractAction implements
		MDCCaretChangeListener {
	MDCCaret caret= null;

	
	/**
	 * 
	 */
	public AbstractSelectionAction() {
	}
	
	/**
	 * @param name
	 */
	public AbstractSelectionAction(String name) {
		super(name);
	}

	/**
	 * @param name
	 * @param icon
	 */
	public AbstractSelectionAction(String name, Icon icon) {
		super(name, icon);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.MDCCaretChangeListener#caretChanged(jsesh.mdcDisplayer.draw.MDCCaret)
	 */
	public void caretChanged(MDCCaret caret) {
		setEnabled(caret.hasMark());
	}
	
	/**
	 * Links the state of this action to this caret.
	 * @param caret
	 *
	public void chooseCaret(MDCCaret caret) {
		
		if (this.caret != null) {
			this.caret.removeCaretChangeListener(this);
		}
		this.caret= caret;
		this.caret.addCaretChangeListener(this);
		
		
	}*/
	
	
}
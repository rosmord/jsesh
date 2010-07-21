/*
 * Created on 7 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor;

import jsesh.editor.caret.MDCCaret;
import jsesh.mdc.model.operations.ModelOperation;

/**
 * @author S. Rosmorduc
 *
 */
public class MDCModelEditionAdapter implements MDCModelEditionListener {

	/* (non-Javadoc)
	 * @see jsesh.editor.MDCModelEditionListener#textEdited(jsesh.mdc.model.operations.ModelOperation)
	 */
	public void textEdited(ModelOperation op) {
	}

	/* (non-Javadoc)
	 * @see jsesh.editor.MDCModelEditionListener#textChanged()
	 */
	public void textChanged() {
	}

	/* (non-Javadoc)
	 * @see jsesh.editor.MDCModelEditionListener#codeChanged(java.lang.StringBuffer)
	 */
	public void codeChanged(StringBuffer code) {
	}

	/* (non-Javadoc)
	 * @see jsesh.editor.MDCModelEditionListener#focusGained(java.lang.StringBuffer)
	 */
	public void focusGained(StringBuffer code) {
	}

	/* (non-Javadoc)
	 * @see jsesh.editor.MDCModelEditionListener#focusLost()
	 */
	public void focusLost() {
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.draw.MDCCaretChangeListener#caretChanged(jsesh.mdcDisplayer.draw.MDCCaret)
	 */
	public void caretChanged(MDCCaret caret) {
	}
	
	public void separatorChanged() {
	}

}

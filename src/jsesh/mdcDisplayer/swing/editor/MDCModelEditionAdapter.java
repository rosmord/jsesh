/*
 * Created on 7 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.editor;

import jsesh.mdc.model.operations.ModelOperation;
import jsesh.mdcDisplayer.draw.MDCCaret;

/**
 * @author S. Rosmorduc
 *
 */
public class MDCModelEditionAdapter implements MDCModelEditionListener {

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.editor.MDCModelEditionListener#textEdited(jsesh.mdc.model.operations.ModelOperation)
	 */
	public void textEdited(ModelOperation op) {
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.editor.MDCModelEditionListener#textChanged()
	 */
	public void textChanged() {
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.editor.MDCModelEditionListener#codeChanged(java.lang.StringBuffer)
	 */
	public void codeChanged(StringBuffer code) {
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.editor.MDCModelEditionListener#focusGained(java.lang.StringBuffer)
	 */
	public void focusGained(StringBuffer code) {
	}

	/* (non-Javadoc)
	 * @see jsesh.mdcDisplayer.swing.editor.MDCModelEditionListener#focusLost()
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

/*
 * Created on 30 sept. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.editor;

import jsesh.editor.caret.MDCCaretChangeListener;
import jsesh.mdc.model.operations.ModelOperation;

/**
 * MDC editing listeners are warned about any change in the process of editing a hieroglyphic text.
 * <ul>
 * <li> Modification of the text
 * <li> caret change.
 * <li> complete change of text.
 * </ul>
 * code change listeners are notified when the code associated with an editor changes.
 * 
 * Each editor for Manuel de Codage is associated with a StringBuffer,
 * which contains a code for the next sign to enter. It's usually convenient 
 * to display this code, hence this interface.
 * 
 * <p> As it might be convenient to use the same place for <em>all</em> codes,
 * when editing multiple documents, a code change listener is warned of :
 * 
 * <ul>
 * <li> modifications of the code </li>
 * <li> focus gained by the editor they are attached to </li>
 * <li> focus lost by the editor they are attached to </li>
 * </ul>
 * 
 * @author rosmord
 *
 */
public interface MDCModelEditionListener extends MDCCaretChangeListener {
	/**
	 * Called when the text has been edited.
	 * @param op
	 */
	void textEdited(ModelOperation op);
	
	/**
	 * Called when a new text has been loaded.
	 */
	void textChanged(); 
	
	void separatorChanged();
	
	void codeChanged(StringBuffer code);
	
	void focusGained(StringBuffer code);
	
	void focusLost();
}


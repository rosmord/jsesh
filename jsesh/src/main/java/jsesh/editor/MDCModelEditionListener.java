/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
/*
 * Created on 30 sept. 2004 by rosmord
 */
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


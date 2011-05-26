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
package jsesh.editor.actions.text;

import java.awt.event.ActionEvent;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;

/**
 * A simple class for easy text addition. A graphic representation of the text
 * to add will be used as icon.
 * 
 * @author S. Rosmorduc
 * 
 */
@SuppressWarnings("serial")
public class InsertElementIconAction extends EditorAction {

	private ModelElement modelElement;

	/**
	 * Insert a sign for a given lexical item.
	 * 
	 * @param editor
	 * @param modelElement
	 *            the element which will be inserted.
	 * @param mdcText
	 *            the MdC text used to produce the icon
	 * @see jsesh.mdc.constants.SymbolCodes
	 */
	public InsertElementIconAction(JMDCEditor editor, ModelElement modelElement) {
		super(editor);
		this.modelElement = modelElement.deepCopy();
	}

	/**
	 * Insert a sign for a given lexical item.
	 * 
	 * @param editor
	 * @param symbolCode
	 *            the code for the lexical item (from the {@link SymbolCodes}
	 *            interface).
	 * @param mdcText
	 *            the MdC text used to produce the icon
	 * @see jsesh.mdc.constants.SymbolCodes
	 */
	public InsertElementIconAction(JMDCEditor editor, int symbolCode) {
		this(editor, new Hieroglyph(symbolCode));
	}

	public void actionPerformed(ActionEvent e) {
		if (editor.isEditable())
			editor.getWorkflow().insertElement(modelElement);
	}

}
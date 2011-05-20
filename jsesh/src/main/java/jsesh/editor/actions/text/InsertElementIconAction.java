package jsesh.editor.actions.text;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;
import jsesh.mdc.constants.LexicalSymbolsUtils;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;
import jsesh.swing.ImageIconFactory;

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
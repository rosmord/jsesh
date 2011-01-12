package jsesh.editorSoftware.actions;

import java.awt.event.ActionEvent;

import jsesh.editorSoftware.MDCDisplayerAppliWorkflow;
import jsesh.editorSoftware.actions.generic.MDCIconAction;
import jsesh.mdc.constants.LexicalSymbolsUtils;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;

/**
 * A simple class for easy text addition.
 * A graphic representation of the text to add will be used as icon.
 * 
 * @author S. Rosmorduc
 * 
 */
public class InsertElementIconAction extends MDCIconAction {

	ModelElement element;

	/**
	 * @param element
	 * @param mdcText
	 * 
	 */
	public InsertElementIconAction(ModelElement element, String mdcText, MDCDisplayerAppliWorkflow workflow) {
		super(workflow,mdcText);
		this.element = element;
	}

	/**
	 * Insert a sign for a given lexical item.
	 * @param symbolCode the code for the lexical item (from the SymbolCodes interface).
	 * @param workflow
	 * @see jsesh.mdc.constants.SymbolCodes
	 */
	public InsertElementIconAction(int symbolCode, MDCDisplayerAppliWorkflow workflow) {
		super(workflow,LexicalSymbolsUtils.getStringForLexicalItem(symbolCode));
		this.element = new Hieroglyph(symbolCode);
	}
	
	public void actionPerformed(ActionEvent e) {
		workflow.insertElement(element);
	}

}
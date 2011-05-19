package jsesh.jhotdraw.actions.text;

import java.awt.event.ActionEvent;

import jsesh.editor.JMDCEditor;
import jsesh.jhotdraw.JSeshView;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.mdc.constants.LexicalSymbolsUtils;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;
import jsesh.swing.ImageIconFactory;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractViewAction;

/**
 * A simple class for easy text addition.
 *  <p>We are not going to add all text addition commands to the editor object itself,
 *  so this class allows application-level actions for this task.
 * A graphic representation of the text to add will be used as icon.
 * 
 * @author S. Rosmorduc
 */
@SuppressWarnings("serial")
public class InsertElementAction extends AbstractViewAction {

	private ModelElement element;

	
	public InsertElementAction(Application app, View view, ModelElement element, String mdcTextForIcon) {
		super(app,view);		
		this.element = element.deepCopy();
		putValue(SMALL_ICON, ImageIconFactory.buildImage(mdcTextForIcon));
	}
	
	public InsertElementAction(Application app, View view, int glyphCode, String mdcTextForIcon) {
		this(app, view, new Hieroglyph(glyphCode), LexicalSymbolsUtils.getStringForLexicalItem(glyphCode));
	}

	/** 
	 * Icon-less insert element action.
	 * @param app
	 * @param view
	 * @param glyphCode see {@link SymbolCodes}
	 */
	public InsertElementAction(Application app, View view, String id, int glyphCode) {
		this(app, view, new Hieroglyph(glyphCode), LexicalSymbolsUtils.getStringForLexicalItem(glyphCode));
		BundleHelper.getInstance().configure(this,id);
	}

	
	public void actionPerformed(ActionEvent e) {
		JSeshView jSeshView= (JSeshView) getActiveView();
		JMDCEditor editor = jSeshView.getEditor();
		if (editor.isEditable()) {
			editor.getWorkflow().insertElement(element);
		}
	}

}
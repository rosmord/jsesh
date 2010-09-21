package jsesh.jhotdraw;

import jsesh.editor.JMDCEditor;
import jsesh.mdc.file.MDCDocument;

/**
 * An abstract (more or less framework-agnostic) representation of an editing session of a JSesh document.
 * @author rosmord
 *
 */
public class JSeshViewModel {
	private MDCDocument mdcDocument;
	private JMDCEditor editor;

	public JSeshViewModel() {
			mdcDocument= new MDCDocument();
			editor= new JMDCEditor(mdcDocument.getHieroglyphicTextModel());
	}
	
	public JMDCEditor getEditor() {
		return editor;
	}
	
	public MDCDocument getMdcDocument() {
		return mdcDocument;
	}
	

	public void setCurrentDocument(MDCDocument doc) {
		mdcDocument = doc;
		editor.setHieroglyphiTextModel(
				mdcDocument.getHieroglyphicTextModel());
		editor.setTextDirection(mdcDocument.getMainDirection());
		editor.setTextOrientation(
				mdcDocument.getMainOrientation());
		
		// What to do with those ???? 
//		this.getDrawingSpecifications().setSmallSignsCentered(
//				currentDocument.isSmallSignsCentred());
	}

}

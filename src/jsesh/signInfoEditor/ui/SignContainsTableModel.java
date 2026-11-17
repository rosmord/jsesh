package jsesh.signInfoEditor.ui;

import jsesh.hieroglyphs.SignDescriptionConstants;
import jsesh.signInfoEditor.model.EditableSignInfo;
import jsesh.signInfoEditor.model.SignInfoProperty;
import jsesh.signInfoEditor.model.XMLInfoProperty;


public class SignContainsTableModel extends SignPropertyTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SignContainsTableModel(EditableSignInfo editableSignInfo) {
		super(editableSignInfo, SignDescriptionConstants.CONTAINS, 
				new String[] {"Part"}, new String[] {
			SignDescriptionConstants.PART_CODE	 
		});
	}

	protected XMLInfoProperty buildDefaultSignProperty(String code) {
		XMLInfoProperty prop= new SignInfoProperty(SignDescriptionConstants.CONTAINS, true);
		prop.setAttribute(SignDescriptionConstants.PART_CODE, code);
		return prop;
	}

	
}

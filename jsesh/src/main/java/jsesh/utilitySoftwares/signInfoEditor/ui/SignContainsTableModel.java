package jsesh.utilitySoftwares.signInfoEditor.ui;

import jsesh.hieroglyphs.data.SignDescriptionConstants;
import jsesh.utilitySoftwares.signInfoEditor.model.EditableSignInfo;
import jsesh.utilitySoftwares.signInfoEditor.model.SignInfoProperty;
import jsesh.utilitySoftwares.signInfoEditor.model.XMLInfoProperty;


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

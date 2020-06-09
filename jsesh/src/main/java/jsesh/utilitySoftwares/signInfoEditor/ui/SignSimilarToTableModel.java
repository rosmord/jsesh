package jsesh.utilitySoftwares.signInfoEditor.ui;

import jsesh.hieroglyphs.data.SignDescriptionConstants;
import jsesh.utilitySoftwares.signInfoEditor.model.EditableSignInfo;
import jsesh.utilitySoftwares.signInfoEditor.model.SignInfoProperty;
import jsesh.utilitySoftwares.signInfoEditor.model.XMLInfoProperty;


public class SignSimilarToTableModel extends SignPropertyTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SignSimilarToTableModel(EditableSignInfo editableSignInfo) {
		super(editableSignInfo, SignDescriptionConstants.SIMILAR_TO, 
				new String[] {"Base Sign"}, new String[] {
			SignDescriptionConstants.BASE_SIGN	 
		});
	}

	protected XMLInfoProperty buildDefaultSignProperty(String code) {
		XMLInfoProperty prop= new SignInfoProperty(SignDescriptionConstants.SIMILAR_TO, true);
		prop.setAttribute(SignDescriptionConstants.BASE_SIGN, code);
		return prop;
	}

}

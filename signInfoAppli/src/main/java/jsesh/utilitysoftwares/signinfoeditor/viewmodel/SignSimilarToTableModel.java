package jsesh.utilitysoftwares.signinfoeditor.viewmodel;

import jsesh.glyphs.data.SignDescriptionConstants;
import jsesh.utilitysoftwares.signinfoeditor.model.EditableSignInfo;
import jsesh.utilitysoftwares.signinfoeditor.model.SignInfoProperty;


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

	protected SignInfoProperty buildDefaultSignProperty(String code) {
		SignInfoProperty prop= new SignInfoProperty(SignDescriptionConstants.SIMILAR_TO, true);
		prop.setAttribute(SignDescriptionConstants.BASE_SIGN, code);
		return prop;
	}

}

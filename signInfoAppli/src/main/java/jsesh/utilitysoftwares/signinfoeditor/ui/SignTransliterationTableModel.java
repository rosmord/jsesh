package jsesh.utilitysoftwares.signinfoeditor.ui;

import jsesh.hieroglyphs.data.SignDescriptionConstants;
import jsesh.hieroglyphs.data.SignValueType;
import jsesh.utilitysoftwares.signinfoeditor.model.EditableSignInfo;
import jsesh.utilitysoftwares.signinfoeditor.model.SignInfoProperty;
import jsesh.utilitysoftwares.signinfoeditor.model.XMLInfoProperty;

/**
 * Table model for a table which will display the transliteration information.
 * 
 * Fields are :
 * <ul>
 * <li>transliteration
 * <li>type (phonogram|ideogram|abbreviation|typical) 'phonogram'
 * <li>use (keyboard|palette|informative) 'keyboard'
 * 
 * </ul>
 * @author rosmord
 */
class SignTransliterationTableModel extends SignPropertyTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5961390370709512489L;


	public SignTransliterationTableModel(EditableSignInfo editableSignInfo) {
		super(editableSignInfo, 
                        SignDescriptionConstants.HAS_TRANSLITERATION, 
                        new String[] {
				"Transliteration","type", "use"}, 
                        new String[] {SignDescriptionConstants.TRANSLITERATION, SignDescriptionConstants.TYPE, SignDescriptionConstants.USE}
                );
	}


        @Override
	protected XMLInfoProperty buildDefaultSignProperty(String code) {
		XMLInfoProperty prop= new SignInfoProperty(SignDescriptionConstants.HAS_TRANSLITERATION, true);
		prop.setAttribute(SignDescriptionConstants.SIGN, editableSignInfo.getCode());
		prop.setAttribute(SignDescriptionConstants.TRANSLITERATION, code);
		prop.setAttribute(SignDescriptionConstants.USE, SignDescriptionConstants.PALETTE);
		prop.setAttribute(SignDescriptionConstants.TYPE,SignValueType.PHONOGRAM.toString());
		return prop;
	}
	
	

}

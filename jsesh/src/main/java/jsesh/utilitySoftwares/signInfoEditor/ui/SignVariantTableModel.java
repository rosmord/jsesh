package jsesh.utilitySoftwares.signInfoEditor.ui;

import jsesh.hieroglyphs.data.SignDescriptionConstants;
import jsesh.utilitySoftwares.signInfoEditor.model.EditableSignInfo;
import jsesh.utilitySoftwares.signInfoEditor.model.SignInfoProperty;
import jsesh.utilitySoftwares.signInfoEditor.model.XMLInfoProperty;


public class SignVariantTableModel extends SignPropertyTableModel {


	/**
	 * 
	 */
	private static final long serialVersionUID = 3032569625558338457L;


	public SignVariantTableModel(EditableSignInfo editableSignInfo) {
		super(editableSignInfo, SignDescriptionConstants.VARIANT_OF, new String[]{
				"base", "is similar", "degree"
		}, new String[] {SignDescriptionConstants.BASE_SIGN, SignDescriptionConstants.IS_SIMILAR, SignDescriptionConstants.LINGUISTIC});
	}

	/**
	 * Create an empty model (to use when no sign is declared).
	 */
	public SignVariantTableModel() {
		this(new EditableSignInfo(""));
	}

	

	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result= super.getValueAt(rowIndex, columnIndex);
		// col 1 is boolean.
		if (columnIndex == 1) {
			result= Boolean.valueOf("y".equals(result));
		}
		return result;
	}

	/**
	 * Write
	 */
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if (columnIndex == 1) {
			String newVal= "n";
			if (((Boolean)value).booleanValue())
				newVal= "y";
			super.setValueAt(newVal, rowIndex, columnIndex);
		} else {
			super.setValueAt(value, rowIndex, columnIndex);
		}
	}
	
	public Class getColumnClass(int columnIndex) {
		Class clazz= null;
		switch (columnIndex) {
		case 0:
			clazz= String.class;
			break;
		case 1:
			clazz= Boolean.class;
			break;
		case 2:
			clazz= String.class;
			break;
		default:
		}
		return clazz;
	}
	

	protected XMLInfoProperty buildDefaultSignProperty(String code) {
		XMLInfoProperty property= new SignInfoProperty(SignDescriptionConstants.VARIANT_OF,true);
		property.setAttribute(SignDescriptionConstants.BASE_SIGN, code);
		property.setAttribute(SignDescriptionConstants.LINGUISTIC, SignDescriptionConstants.UNSPECIFIED);
		property.setAttribute(SignDescriptionConstants.IS_SIMILAR, "y");
		return property;
	}
}

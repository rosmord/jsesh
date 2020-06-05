package jsesh.hieroglyphs.graphics;

import jsesh.utils.EnumBase;

public class HorizontalGravity extends EnumBase {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6781549643233784214L;
	
	public static final HorizontalGravity START= new HorizontalGravity(0, "START", 's');
	public static final HorizontalGravity CENTER= new HorizontalGravity(1, "CENTER", 'c');
	public static final HorizontalGravity END= new HorizontalGravity(2, "END", 'e');
	
	private char code;
	private HorizontalGravity(int id, String designation, char code) {
		super(id, designation);
		this.code= code;
	}
	
	/**
	 * The code used to represent this kind of gravity in XML files.
	 * @return
	 */
	public char getCode() {
		return code;
	}

	/**
	 * Gets the opposite of this kind of gravity.
	 * @return
	 */
	public HorizontalGravity flip() {
		HorizontalGravity result= CENTER;
		switch (code) {
		case 's':
			result= END;
			break;
		case 'e':
			result= START;
			break;
		}
		return result;
	}
}

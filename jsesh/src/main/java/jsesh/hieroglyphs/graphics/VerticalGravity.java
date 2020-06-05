package jsesh.hieroglyphs.graphics;

import jsesh.utils.EnumBase;

public class VerticalGravity extends EnumBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8473962859514820194L;

	public static final VerticalGravity TOP = new VerticalGravity(0, "TOP", 't');

	public static final VerticalGravity CENTER = new VerticalGravity(1,
			"CENTER", 'c');

	public static final VerticalGravity BOTTOM = new VerticalGravity(2,
			"BOTTOM", 'b');

	private char code;

	private VerticalGravity(int id, String designation, char code) {
		super(id, designation);
		this.code = code;
	}

	/**
	 * Return the code used to represent this gravity in XML files.
	 * 
	 * @return
	 */
	public char getCode() {
		return code;
	}

	public VerticalGravity flip() {

		VerticalGravity result = CENTER;
		switch (code) {
		case 't':
			result = BOTTOM;
			break;
		case 'b':
			result = TOP;
			break;
		}
		return result;
	}

}

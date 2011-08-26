package jsesh.mdc.constants;

import jsesh.utils.EnumBase;

/**
 * Parameters for various Manuel de codage dialects.
 * 
 * @author rosmord
 * 
 */
public enum Dialect {

	OTHER(0, "other"), INSCRIBE(1, "inscribe"), JSESH(100, "jsesh"), MDC2007(4,
			"mdc2007"), MACSCRIBE(3, "macscribe"), MDC88(5, "mdc88"), TKSESH(6,
			"tksesh", false), WINGLYPH(5, "winglyph"), JSESH1(2, "jsesh1");

	
	/**
	 * @see #isEditorialMarksAsSign()
	 */
	private boolean editorialMarksAsSign = true;

	private int id;
	private String designation;
	
	private Dialect(int id, String designation) {
		this.id= id;
		this.designation= designation;
	}

	private Dialect(int id, String designation, boolean editorialMarksAsSign) {
		this(id, designation);
		this.editorialMarksAsSign = editorialMarksAsSign;
	}

	public int getId() {
		return id;
	}
	
	public String getDesignation() {
		return designation;
	}

	
	/**
	 * Are ecdotic marks represented as signs in this dialect?. Most dialects of
	 * the Manuel consider editorial marks as simple signs. Tksesh (and
	 * hierotex) consider them as parenthesis. The original manual says nothing
	 * definitive on the subject, although it tends to support the tksesh
	 * interpretation. However, actual practices show that one [[ may be closed
	 * by two ']]', or vice-versa. Hence, It's wiser to use encoding which
	 * consider [[ as a simple sign.
	 */
	public boolean isEditorialMarksAsSign() {
		return editorialMarksAsSign;
	}
}

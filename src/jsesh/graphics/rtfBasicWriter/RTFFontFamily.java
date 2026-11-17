/**
 * 
 */
package jsesh.graphics.rtfBasicWriter;

import jsesh.utils.EnumBase;

/**
 * @author rosmord
 *
 */
public class RTFFontFamily extends EnumBase {

	/**
	 * code for serialization.
	 */
	private static final long serialVersionUID = 3704953614899767833L;
	
	static public final RTFFontFamily NIL= new RTFFontFamily(0,"NIL", "\\fnil");
	static public final RTFFontFamily ROMAN= new RTFFontFamily(1,"ROMAN","\\froman");
	static public final RTFFontFamily SWISS= new RTFFontFamily(2,"SWISS","\\fswiss");
	static public final RTFFontFamily MODERN= new RTFFontFamily(3,"MODERN","\\fmodern");
	static public final RTFFontFamily SCRIPT= new RTFFontFamily(4,"SCRIPT","\\fscript");
	static public final RTFFontFamily DECOR= new RTFFontFamily(5,"DECOR","\\fdecor");
	static public final RTFFontFamily TECH= new RTFFontFamily(6,"TECH","\\ftech");
	static public final RTFFontFamily BIDI= new RTFFontFamily(7,"BIDI","\\fbidi");
	
	
	private String rtfCode;
	
	/**
	 * @param id
	 * @param designation
	 */
	private RTFFontFamily(int id, String designation, String rtfCode) {
		super(id, designation);
		this.rtfCode= rtfCode;
	}

	/**
	 * Return a code suitable for inclusion in a RTF file.
	 * @return the rtfCode
	 */
	public String getRtfCode() {
		return rtfCode;
	}
}

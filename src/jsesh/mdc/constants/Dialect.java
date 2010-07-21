package jsesh.mdc.constants;

import jsesh.utils.EnumBase;

/**
 * Parameters for various Manuel de codage dialects.
 * @author rosmord
 *
 */
public class Dialect extends EnumBase {
	
	private static final long serialVersionUID = 6028126156927171774L;
	
	/**
	 * @see #isEditorialMarksAsSign()
	 */
	private boolean editorialMarksAsSign= true;
	
	public static final Dialect OTHER= new Dialect(0, "other");
	public static final Dialect INSCRIBE= new Dialect(1,"inscribe");
	public static final Dialect JSESH= new Dialect(100,"jsesh");
	public static final Dialect MDC2007= new Dialect(4, "mdc2007");
	public static final Dialect MACSCRIBE= new Dialect(3,"macscribe");
	public static final Dialect MDC88= new Dialect(5,"mdc88");
	public static final Dialect TKSESH= new Dialect(6,"tksesh",false);
	public static final Dialect WINGLYPH= new Dialect(5,"winglyph");
	public static final Dialect JSESH1 = new Dialect(2,"jsesh1");
	
	//int OTHERS= 0;
	//int INSCRIBE= 1;
	//int JSESH1=2;
	//int MACSCRIBE= 3;
	//int MDC2007=4;
	//int MDC88= 5;
	//int TKSESH= 6;
	//int WINGLYPH= 7;
	//int JSESH=100; // Old JSesh format
	
	private Dialect(int id, String designation) {
		super(id, designation);
	}
	
	private Dialect(int id, String designation, boolean editorialMarksAsSign) {
		super(id, designation);
		this.editorialMarksAsSign = editorialMarksAsSign;
	}



	/**
	 * Are ecdotic marks represented as signs in this dialect?.
	 * Most dialects of the Manuel consider editorial marks as simple signs.
	 * Tksesh (and hierotex) consider them as parenthesis. The original manual says
	 * nothing definitive on the subject, although it tends to support the tksesh interpretation.
	 * However, actual practices show that one [[ may be closed by two ']]', or vice-versa.
	 * Hence, It's wiser to use encoding which consider [[ as a simple sign.
	 */
	public boolean isEditorialMarksAsSign() {
		return editorialMarksAsSign;
	}
}

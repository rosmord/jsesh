package jsesh.mdc.constants;

import jsesh.utils.EnumBase;

public class ToggleType extends EnumBase
{
	public static final ToggleType SHADINGTOGGLE= new ToggleType(1,"SHADINGTOGGLE", "#");
	public static final ToggleType SHADINGON= new ToggleType(2,"SHADINGON", "#b");
	public static final ToggleType SHADINGOFF= new ToggleType(3,"SHADINGOFF", "#e");
	public static final ToggleType RED= new ToggleType(4,"RED", "$r");
	public static final ToggleType BLACK= new ToggleType(5,"BLACK", "$b");
	public static final ToggleType LACUNA= new ToggleType(6,"LACUNA", "?");
	public static final ToggleType LINELACUNA= new ToggleType(7,"LINELACUNA", "??");
	public static final ToggleType OMMIT= new ToggleType(8,"OMMIT", "^");
	public static final ToggleType BLACKRED= new ToggleType(9,"BLACKRED", "$");

	private String mdc;

	protected ToggleType(int id, String descr, String mdc)
	{
		super(id, descr);
		this.mdc= mdc;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "Toggle  " + getDesignation();
	}

	
	/**
	 * @return the manuel de codage code for this toggle.
	 */
	public String getMDC()
	{
		return mdc;
	}

}
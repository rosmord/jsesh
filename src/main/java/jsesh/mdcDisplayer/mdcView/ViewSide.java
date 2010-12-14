package jsesh.mdcDisplayer.mdcView;

import jsesh.utils.EnumBase;

/**
 * Constants for sides of views. 
 * <p> To make code independent of text direction, we use START and END instead of EAST and WEST.
 *  
 * @author rosmord
 *
 */
public class ViewSide extends EnumBase {
	
	public static final ViewSide TOP= new ViewSide(0, "TOP");
	public static final ViewSide BOTTOM= new ViewSide(1, "BOTTOM");
	public static final ViewSide START= new ViewSide(2, "START");
	public static final ViewSide END= new ViewSide(3, "END");
	
	
	private ViewSide(int id, String designation) {
		super(id, designation);
	}
	
}

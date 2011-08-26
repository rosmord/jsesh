package jsesh.mdcDisplayer.preferences;

import jsesh.utils.EnumBase;

/**
 * How  shading should be rendered.
 * TODO replace by a simple boolean ???
 * @author rosmord
 *
 */
@SuppressWarnings("serial")
public class ShadingStyle extends EnumBase {

	public static final ShadingStyle LINE_HATCHING= new ShadingStyle(0, "LINE_HATCHING");
	
	public static final ShadingStyle GRAY_SHADING= new ShadingStyle(1, "GRAY_SHADING");
	
	private ShadingStyle(int id, String designation) {
		super(id, designation);
	}

}

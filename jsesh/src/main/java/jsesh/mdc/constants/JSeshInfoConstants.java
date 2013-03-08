package jsesh.mdc.constants;

/**
 * Identifiers used in the various versions of JSesh info.
 * 
 * @author rosmord
 */
public interface JSeshInfoConstants {
	String JSESH_INFO_PREFIX = "JSesh_";

	String JSESH_INFO = "JSesh_Info";

	String JSESH_PAGE_WIDTH = "JSesh_page_width";

	String JSESH_PAGE_HEIGHT = "JSesh_page_height";

	String JSESH_TEXT_ORiGIN_X = "JSesh_text_origin_x";

	String JSESH_TEXT_ORiGIN_Y = "JSesh_text_origin_y";

	String JSESH_TEXT_WIDTH = "JSesh_text_width";

	String JSESH_TEXT_HEIGHT = "JSesh_text_height";

	/**
	 * Either horizontal or vertical.
	 */
	String JSESH_PAGE_ORIENTATION = "JSesh_page_orientation";

	/**
	 * Either left-to-right or right-to-left
	 */
	String JSESH_PAGE_DIRECTION = "JSesh_page_direction";

	/**
	 * 0 or 1 value for small signs centering.
	 * 
	 * Note : we decided to use the British form (and not the US English one) as
	 * an afterthough, but we certainly don't want to break existing files. So,
	 * in the header, JSesh_small_sign_centered will remain. Defaults to 0
	 * (false).
	 */
	String JSESH_SMALL_SIGNS_CENTRED = "JSesh_small_sign_centered";

	String JSESH_CARTOUCHE_LINE_WIDTH = "JSesh_cartouche_line_width";
	String JSESH_MAX_QUADRANT_WIDTH = "JSesh_max_quadrant_width";
	String JSESH_MAX_QUADRANT_HEIGHT = "JSesh_max_quadrantHeight";
	String JSESH_LINE_SKIP = "JSesh_line_skip";
	String JSESH_COLUMN_SKIP = "JSesh_column_skip";
	String JSESH_USE_LINES_FOR_SHADING = "JSesh_use_lines_for_shading";
	String JSESH_STANDARD_SIGN_HEIGHT = "JSesh_standard_sign_height";
	String JSESH_SMALL_BODY_SCALE_LIMIT = "JSesh_small_body_scale_limit";
	String JSESH_SMALL_SKIP = "JSesh_small_skip";

}

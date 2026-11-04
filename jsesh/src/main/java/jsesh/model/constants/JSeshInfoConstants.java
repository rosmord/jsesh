package jsesh.model.constants;

/**
 * Identifiers used in the various versions of JSesh info.
 * 
 * @author rosmord
 */
public interface JSeshInfoConstants {
	String JSESH_INFO_PREFIX = "JSesh_";

	String JSESH_INFO = "JSesh_Info";

	/// Width of the page, in layout units. 0 (the default) means that the
	/// document has no page: it is as large as its text needs.
	String JSESH_PAGE_WIDTH = "JSesh_page_width";

	/// Height of the page, in layout units. 0 means "not specified".
	String JSESH_PAGE_HEIGHT = "JSesh_page_height";

	/// Margins of the page, in layout units.
	String JSESH_PAGE_MARGIN_LEFT = "JSesh_page_margin_left";
	String JSESH_PAGE_MARGIN_RIGHT = "JSesh_page_margin_right";
	String JSESH_PAGE_MARGIN_TOP = "JSesh_page_margin_top";
	String JSESH_PAGE_MARGIN_BOTTOM = "JSesh_page_margin_bottom";

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
	String JSESH_ALPHABETIC_TEXT_BASELINE = "JSesh_alphabetic_text_baseline";

	/**
	 * true or false: in horizontal text, are quadrats limited to the maximal
	 * quadrat width? Documents which don't have this property get true (the
	 * historical layout).
	 */
	String JSESH_LIMIT_QUADRAT_WIDTH_IN_LINES = "JSesh_limit_quadrat_width_in_lines";

}

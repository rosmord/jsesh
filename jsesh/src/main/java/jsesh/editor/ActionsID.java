package jsesh.editor;

/**
 * Actions ID for the JSesh editor widget.
 * All actions in the editor's action map will (utimately) have their ID here.
 * @author rosmord
 */
public interface ActionsID {

    String CLEAR_SELECTION = "jsesh.edit.clearSelection";
    String SELECT_ALL=  "jsesh.edit.selectAll";
    /**
     * ID for one of the variants of copyAsAction implemented in a JMDCEditor.
     * To get the corresponding object from a editor, use its actionMap.
     */
    String COPY_AS_MDC = "COPY_AS_MDC";
    /**
     * ID for one of the variants of copyAsAction implemented in a JMDCEditor.
     * To get the corresponding object from a editor, use its actionMap.
     */
    String COPY_AS_BITMAP = "COPY_AS_BITMAP";
    /**
     * ID for one of the variants of copyAsAction implemented in a JMDCEditor.
     * To get the corresponding object from a editor, use its actionMap.
     */
    String COPY_AS_RTF = "COPY_AS_RTF";
    /**
     * ID for one of the variants of copyAsAction implemented in a JMDCEditor.
     * To get the corresponding object from a editor, use its actionMap.
     */
    String COPY_AS_PDF = "COPY_AS_PDF";
	String GO_RIGHT = "GO RIGHT";
	String INSERT_CHAR = "INSERT CHAR";
	String BEGINNING_OF_LINE = "BEGINNING OF LINE";
	String END_OF_LINE = "END_OF_LINE";
	String COPY = "COPY";
	String CUT = "CUT";
	String UNDO = "UNDO";
	String REDO = "REDO";
	String GROUP_HORIZONTAL = "GROUP HORIZONTAL";
	String GROUP_VERTICAL = "GROUP VERTICAL";
	String EXPAND_SELECTION_LEFT = "EXPAND_SELECTION_LEFT";
	String EXPAND_SELECTION_RIGHT = "EXPAND_SELECTION_RIGHT";
	String EXPAND_SELECTION_DOWN = "EXPAND_SELECTION_DOWN";
	String EXPAND_SELECTION_UP = "EXPAND_SELECTION_UP";
	String NEW_LINE = "NEW_LINE";
	String NEW_PAGE = "NEW_PAGE";
	String GO_DOWN = "GO DOWN";
	String GO_UP = "GO UP";
	String SELECT_HORIZONTAL_ORIENTATION = "SELECT_HORIZONTAL_ORIENTATION";
	String SELECT_VERTICAL_ORIENTATION = "SELECT_VERTICAL_ORIENTATION";
	String SELECT_L2R_DIRECTION = "SELECT_L2R_DIRECTION";
	String SELECT_R2L_DIRECTION = "SELECT_R2L_DIRECTION";
	String SET_MODE_LATIN = "SET_MODE_LATIN";
	String SET_MODE_HIEROGLYPHS = "SET_MODE_HIEROGLYPHS";
	String SET_MODE_ITALIC = "SET_MODE_ITALIC";
	String SET_MODE_BOLD = "SET_MODE_BOLD";
	String GO_LEFT = "GO LEFT";
}

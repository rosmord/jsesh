/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.editor;

/**
 * Actions ID for the JSesh editor widget.
 * All actions in the editor's action map will (ultimately) have their ID here.
 * @author Serge Rosmorduc
 */
public interface ActionsID {

    String BEGINNING_OF_LINE = "edit.beginningOfLine";
    String CLEAR_SELECTION = "edit.clearSelection";
    String COPY = "edit.copy";
    String COPY_AS_BITMAP = "edit.copyAsBitmap";
    String COPY_AS_MDC = "edit.copyAsMDC";
    String COPY_AS_PDF = "edit.copyAsPDF";
	String COPY_AS_RTF = "edit.copyAsRTF";
	String CUT = "edit.cut";
	String END_OF_LINE = "edit.EndOfLine";
	String EXPAND_SELECTION_DOWN = "edit.expandSelectionDown";
	String EXPAND_SELECTION_LEFT = "edit.expandSelectionLeft";
	String EXPAND_SELECTION_RIGHT = "edit.expandSelectionRight";
	String EXPAND_SELECTION_UP = "edit.expandSelectionUp";
	String GO_DOWN = "edit.down";
	String GO_LEFT = "edit.left";
	String GO_RIGHT = "edit.right";
	String GO_UP = "edit.up";
	String GROUP_HORIZONTAL = "text.groupHorizontally";
	String GROUP_VERTICAL = "text.groupVertically";
	String NEW_LINE = "text.newLine";
	String NEW_PAGE = "text.newPage";
	String PASTE = "edit.paste";
	String REDO = "edit.redo";
	String SELECT_ALL=  "edit.selectAll";
	String SELECT_HORIZONTAL_ORIENTATION = "SELECT_HORIZONTAL_ORIENTATION";
	String SELECT_L2R_DIRECTION = "SELECT_L2R_DIRECTION";
	String SELECT_R2L_DIRECTION = "SELECT_R2L_DIRECTION";
	String SELECT_VERTICAL_ORIENTATION = "SELECT_VERTICAL_ORIENTATION";
	String SET_MODE_BOLD = "edit.setModeBold";
	String SET_MODE_HIEROGLYPHS = "edit.setModeHieroglyphs";
	String SET_MODE_ITALIC = "edit.setModeItalic";
	String SET_MODE_LATIN = "edit.setModeLatin";
	String SET_MODE_TRANSLIT = "edit.setModeTranslit";
	String SET_MODE_LINENUMBER = "edit.setModeLineNumber";	
	String UNDO = "edit.undo";
	String LIGATURE_ELEMENTS = "text.ligatureElements";
	String LIGATURE_GROUP_WITH_GLYPH= "text.ligatureGroupWithGlyph";
	String LIGATURE_GLYPH_WITH_GROUP = "text.ligatureGlyphWithGroup";
	String EXPLODE_GROUP = "text.explodeGroup";
	String INSERT_SPACE= "text.insertSpace";
	String INSERT_HALF_SPACE="text.insertHalfSpace";
	String INSERT_RED_POINT="text.insertRedPoint";
	String INSERT_BLACK_POINT="text.insertBlackPoint";
	String SHADE_ZONE ="text.shadeZone";
	String UNSHADE_ZONE ="text.unshadeZone";
	String RED_ZONE = "text.redZone";
	String BLACK_ZONE= "text.blackZone";
	String REVERSE_SIGN = "sign.reverseSign";
	String TOGGLE_SIGN_IS_RED = "sign.toggleSignIsRed";
	String TOGGLE_SIGN_IS_WIDE = "sign.toggleSignIsWide";
	String TOGGLE_IGNORED_SIGN = "sign.toggleIgnoredSign";
	String TOGGLE_GRAMMAR = "sign.toggleGrammar";
	String SIGN_IS_SENTENCE_END = "sign.setSentenceEnd";
	String SIGN_IS_WORD_END = "sign.setWordEnd";
	String SIGN_IS_INSIDE_WORD = "sign.setSignInsideWord";
}

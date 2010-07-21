/*
 * Created on 1 oct. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.editor;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import jsesh.editor.actions.CopyAction;
import jsesh.editor.actions.CopyAsAction;
import jsesh.editor.actions.CutAction;
import jsesh.editor.actions.EditorAction;
import jsesh.editor.actions.ExpandSelectionAction;
import jsesh.editor.actions.GoDownAction;
import jsesh.editor.actions.GoLeftAction;
import jsesh.editor.actions.GoRightAction;
import jsesh.editor.actions.GoUpAction;
import jsesh.editor.actions.GroupHorizontalAction;
import jsesh.editor.actions.GroupVerticalAction;
import jsesh.editor.actions.NewLineAction;
import jsesh.editor.actions.NewPageAction;
import jsesh.editor.actions.PasteAction;
import jsesh.editor.actions.RedoAction;
import jsesh.editor.actions.SelectOrientationAction;
import jsesh.editor.actions.SelectTextDirectionAction;
import jsesh.editor.actions.SetModeAction;
import jsesh.editor.actions.UndoAction;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdcDisplayer.clipboard.JSeshPasteFlavors;

import org.qenherkhopeshef.utils.PlatformDetection;

/**
 * Manages input for an MDC Editor. It might be interesting to make it public.
 * 
 * IMPORTANT : clean. Suppress unused stuff.
 * 
 * @author rosmord
 */

public class MDCEditorKeyManager extends KeyAdapter {
	
	public static final String GO_LEFT = "GO LEFT";

	public static final String GO_RIGHT = "GO RIGHT";

	public static final String INSERT_CHAR = "INSERT CHAR";

	public static final String BEGINNING_OF_LINE = "BEGINNING OF LINE";

	public static final String END_OF_LINE = "END_OF_LINE";

	public static final String COPY = "COPY";

	public static final String COPY_AS_PDF = "COPY_AS_PDF";

	public static final String COPY_AS_RTF = "COPY_AS_RTF";
	
	public static final String COPY_AS_BITMAP = "COPY_AS_BITMAP";

	public static final String COPY_AS_MDC = "COPY_AS_MDC";

	public static final String PASTE = "PASTE";

	public static final String CUT = "CUT";

	public static final String UNDO = "UNDO";

	public static final String REDO = "REDO";

	public static final String GROUP_HORIZONTAL = "GROUP HORIZONTAL";

	public static final String GROUP_VERTICAL = "GROUP VERTICAL";

	public static final String EXPAND_SELECTION_LEFT = "EXPAND_SELECTION_LEFT";

	public static final String EXPAND_SELECTION_RIGHT = "EXPAND_SELECTION_RIGHT";

	public static final String EXPAND_SELECTION_DOWN = "EXPAND_SELECTION_DOWN";

	public static final String EXPAND_SELECTION_UP = "EXPAND_SELECTION_UP";

	public static final String NEW_LINE = "NEW_LINE";

	public static final String NEW_PAGE = "NEW_PAGE";

	public static final String GO_DOWN = "GO DOWN";

	public static final String GO_UP = "GO UP";

	public static final String SELECT_HORIZONTAL_ORIENTATION = "SELECT_HORIZONTAL_ORIENTATION";

	public static final String SELECT_VERTICAL_ORIENTATION = "SELECT_VERTICAL_ORIENTATION";

	public static final String SELECT_L2R_DIRECTION = "SELECT_L2R_DIRECTION";

	public static final String SELECT_R2L_DIRECTION = "SELECT_R2L_DIRECTION";

	public static final String SET_MODE_LATIN = "SET_MODE_LATIN";
	public static final String SET_MODE_HIEROGLYPHS = "SET_MODE_HIEROGLYPHS";
	public static final String SET_MODE_ITALIC = "SET_MODE_ITALIC";
	public static final String SET_MODE_BOLD = "SET_MODE_BOLD";
	
	
	/**
	 * The "normal " control key for this system.
	 */
	private String controlKey;

	private ActionMap actionMap;

	private InputMap inputMap;

	private JMDCEditor editor;

	MDCEditorKeyManager(JMDCEditor editor) {
		this.editor= editor;
		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
			controlKey = "meta";
		} else {
			controlKey = "control";
		}
		inputMap = new InputMap();
		actionMap = new ActionMap();
		addInputs();
		addActions();
		control(editor);
	}

	private void addAction(String command, Action action) {
		actionMap.put(command, action);
	}

	private void addActions() {
		addAction(GO_RIGHT, new GoRightAction(editor));
		addAction(GO_LEFT, new GoLeftAction(editor));
		addAction(GO_DOWN, new GoDownAction(editor));
		addAction(GO_UP, new GoUpAction(editor));
		addAction(BEGINNING_OF_LINE, new EditorAction(editor,"Beginning of line") {
			public void actionPerformed(ActionEvent e) {			
				editor.getWorkflow().cursorToBeginningOfLine();
			}
		});

		addAction(END_OF_LINE, new EditorAction(editor, "End Of line") {

			public void actionPerformed(ActionEvent e) {
				editor.getWorkflow().cursorToEndOfLine();
			}
		});
		addAction(NEW_LINE, new NewLineAction(editor));
		addAction(NEW_PAGE, new NewPageAction(editor));
		addAction(COPY, new CopyAction(editor));
		addAction(COPY_AS_PDF, new CopyAsAction(editor,"Copy as PDF",JSeshPasteFlavors.PDFFlavor));
		addAction(COPY_AS_RTF, new CopyAsAction(editor,"Copy as RTF",JSeshPasteFlavors.RTFFlavor));
		addAction(COPY_AS_BITMAP, new CopyAsAction(editor,"Copy as Bitmap",DataFlavor.imageFlavor));
		addAction(COPY_AS_MDC, new CopyAsAction(editor, "Copy as MDC",DataFlavor.stringFlavor));

		addAction(PASTE, new PasteAction(editor));
		addAction(CUT, new CutAction(editor));
		addAction(GROUP_HORIZONTAL, new GroupHorizontalAction(editor));
		addAction(GROUP_VERTICAL, new GroupVerticalAction(editor));
		addAction(EXPAND_SELECTION_LEFT, new ExpandSelectionAction(editor, -1));
		addAction(EXPAND_SELECTION_RIGHT, new ExpandSelectionAction(editor, 1));
		addAction(EXPAND_SELECTION_UP, new ExpandSelectionAction(editor, -2));
		addAction(EXPAND_SELECTION_DOWN, new ExpandSelectionAction(editor, 2));
		addAction(SELECT_HORIZONTAL_ORIENTATION, new SelectOrientationAction(editor,
				TextOrientation.HORIZONTAL));
		addAction(SELECT_VERTICAL_ORIENTATION, new SelectOrientationAction(editor,
				TextOrientation.VERTICAL));
		addAction(SELECT_L2R_DIRECTION, new SelectTextDirectionAction(editor,
				TextDirection.LEFT_TO_RIGHT));
		addAction(SELECT_R2L_DIRECTION, new SelectTextDirectionAction(editor,
				TextDirection.RIGHT_TO_LEFT));
		addAction(UNDO, new UndoAction(editor));
		addAction(REDO, new RedoAction(editor));
		addAction(SET_MODE_LATIN, new SetModeAction(editor, 'l'));
		addAction(SET_MODE_HIEROGLYPHS, new SetModeAction(editor, 's'));
		addAction(SET_MODE_ITALIC, new SetModeAction(editor, 'i'));
		addAction(SET_MODE_BOLD, new SetModeAction(editor, 'b'));

	}

	private void addInput(String code, String command) {
		inputMap.put(KeyStroke.getKeyStroke(code), command);
	}

	private void addInputs() {
		addInput("LEFT", GO_LEFT);
		addInput("RIGHT", GO_RIGHT);
		addInput("DOWN", GO_DOWN);
		addInput("UP", GO_UP);
		addInput("HOME", BEGINNING_OF_LINE);
		addInput("END", END_OF_LINE);
		// addInput("BACK_SPACE", BACK_SPACE);
		// addInput(':', ADD_LOWER_LEVEL);
		// addInput('*', ADD_SAME_LEVEL);
		// There are a number of ways to enter new cadrats :
		// addInput('-', ADD_NEW_CADRAT);
		// addInput("SPACE", ADD_NEW_CADRAT);
		// addInput("ENTER", ADD_NEW_CADRAT); // ENTER SHOULD ALSO CREATE A NEW
		// LINE !

		addInput("shift ENTER", NEW_PAGE);
		addInput("ENTER", NEW_LINE);
		addInput(controlKey + " Z", UNDO);
		addInput(controlKey + " Y", REDO);

		addInput(controlKey + " C", COPY);
		addInput(controlKey + " X", CUT);
		addInput(controlKey + " V", PASTE);
		addInput(controlKey + " H", GROUP_HORIZONTAL);
		addInput(controlKey + " G", GROUP_VERTICAL);

		addInput("shift LEFT", EXPAND_SELECTION_LEFT);
		addInput("shift RIGHT", EXPAND_SELECTION_RIGHT);
		addInput("shift DOWN", EXPAND_SELECTION_DOWN);
		addInput("shift UP", EXPAND_SELECTION_UP);
		addInput(controlKey + " N", SET_MODE_HIEROGLYPHS);
		addInput(controlKey + " D", SET_MODE_LATIN);
		addInput(controlKey + " I", SET_MODE_ITALIC);
		addInput(controlKey + " B", SET_MODE_BOLD);
	}

	private void control(JMDCEditor editor) {
		editor.addKeyListener(this);
		editor.setActionMap(actionMap);
		editor.setInputMap(JComponent.WHEN_FOCUSED, inputMap);
	}

	public ActionMap getActionMap() {
		return actionMap;
	}

	public InputMap getInputMap() {
		return inputMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
		// On the mac, the "Alt" key is used to generate a number of characters
		// (e.g. square []).
		// On other architectures, it is used as a shortcut for menus. So we
		// behave differently.

		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
			if (e.isActionKey() || e.isControlDown() || e.isMetaDown())
				return;
		} else if (e.isActionKey() || e.isControlDown() || e.isMetaDown()
				|| e.isAltDown())
			return;

		// Codes handled by actions.
		if (e.getKeyChar() == '\n')
			return;
		if (e.getSource() instanceof JMDCEditor) {
			JMDCEditor editor = (JMDCEditor) e.getSource();
			if (!editor.isEditable())
				return;
			if (editor.getWorkflow().getMode() == 's' && e.getKeyChar() == '#') {
				editor.showShadingPopup();
				return;
			}
			editor.getWorkflow().keyTyped(e.getKeyChar());
		}
	}
}

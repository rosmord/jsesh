/*
 * Created on 1 oct. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.mdcDisplayer.swing.editor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.qenherkhopeshef.utils.PlatformDetection;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdcDisplayer.swing.editor.actions.CopyAction;
import jsesh.mdcDisplayer.swing.editor.actions.CutAction;
import jsesh.mdcDisplayer.swing.editor.actions.ExpandSelectionAction;
import jsesh.mdcDisplayer.swing.editor.actions.GoDownAction;
import jsesh.mdcDisplayer.swing.editor.actions.GoLeftAction;
import jsesh.mdcDisplayer.swing.editor.actions.GoRightAction;
import jsesh.mdcDisplayer.swing.editor.actions.GoUpAction;
import jsesh.mdcDisplayer.swing.editor.actions.GroupHorizontalAction;
import jsesh.mdcDisplayer.swing.editor.actions.GroupVerticalAction;
import jsesh.mdcDisplayer.swing.editor.actions.NewLineAction;
import jsesh.mdcDisplayer.swing.editor.actions.NewPageAction;
import jsesh.mdcDisplayer.swing.editor.actions.PasteAction;
import jsesh.mdcDisplayer.swing.editor.actions.RedoAction;
import jsesh.mdcDisplayer.swing.editor.actions.SelectOrientationAction;
import jsesh.mdcDisplayer.swing.editor.actions.SelectTextDirectionAction;
import jsesh.mdcDisplayer.swing.editor.actions.SetModeAction;
import jsesh.mdcDisplayer.swing.editor.actions.UndoAction;

/**
 * Manages input for an MDC Editor. It might be interesting to make it public.
 * This class uses the "singleton" design pattern, as it is stateless.
 * 
 * IMPORTANT : clean. Suppress unused stuff.
 * 
 * @author rosmord
 */

public class MDCEditorKeyManager extends KeyAdapter {
	// static final String ADD_LOWER_LEVEL= "ADD_LOWER_LEVEL";
	// static final String ADD_NEW_CADRAT= "ADD_NEW_CADRAT";
	// static final String ADD_SAME_LEVEL= "ADD_SAME_LEVEL";
	// static final String BACK_SPACE= "BACK SPACE";
	public static final String GO_LEFT = "GO LEFT";

	public static final String GO_RIGHT = "GO RIGHT";

	public static final String INSERT_CHAR = "INSERT CHAR";

	public static final String BEGINNING_OF_LINE = "BEGINNING OF LINE";

	public static final String END_OF_LINE = "END_OF_LINE";

	public static final String COPY = "COPY";

	public static final String PASTE = "PASTE";

	public static final String CUT = "CUT";

	public static final String UNDO = "UNDO";
	
	public static final String REDO = "REDO";
	
	public static final String GROUP_HORIZONTAL = "GROUP HORIZONTAL";

	public static final String GROUP_VERTICAL = "GROUP VERTICAL";

	public static final String EXPAND_SELECTION_LEFT = "EXPAND_SELECTION_LEFT";

	public static final String EXPAND_SELECTION_RIGHT = "EXPAND_SELECTION_RIGHT";

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
	 */
	static private MDCEditorKeyManager instance = null;

	/**
	 * The "normal " control key for this system.
	 */
	private String controlKey;

	/**
	 * return the singleton instance.
	 * 
	 * @return the key manager.
	 */
	public static MDCEditorKeyManager getInstance() {
		if (instance == null) {
			instance = new MDCEditorKeyManager();
		}
		return instance;
	}

	private ActionMap actionMap;

	private InputMap inputMap;

	private MDCEditorKeyManager() {
		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
			controlKey = "meta";
		} else {
			controlKey = "control";
		}
		inputMap = new InputMap();
		actionMap = new ActionMap();
		addInputs();
		addActions();
	}

	private void addAction(String command, Action action) {
		actionMap.put(command, action);
	}

	private void addActions() {
		addAction(GO_RIGHT, new GoRightAction());
		addAction(GO_LEFT, new GoLeftAction());
		addAction(GO_DOWN, new GoDownAction());
		addAction(GO_UP, new GoUpAction());
		addAction(BEGINNING_OF_LINE, new AbstractAction("Beginning of line") {
			public void actionPerformed(ActionEvent e) {
				JMDCEditor ed = (JMDCEditor) e.getSource();
				ed.getWorkflow().cursorToBeginningOfLine();
			}
		});

		addAction(END_OF_LINE, new AbstractAction("End Of line") {

			public void actionPerformed(ActionEvent e) {
				JMDCEditor ed = (JMDCEditor) e.getSource();
				ed.getWorkflow().cursorToEndOfLine();
			}
		});
		// addAction(BACK_SPACE, new BackSpaceAction());
		// addAction(ADD_LOWER_LEVEL, new AddLowerLevelAction());
		// addAction(ADD_SAME_LEVEL, new AddSameLevelAction());
		// addAction(ADD_NEW_CADRAT, new AddNewCadratAction());
		addAction(NEW_LINE, new NewLineAction());
		addAction(NEW_PAGE, new NewPageAction());
		addAction(COPY, new CopyAction());
		addAction(PASTE, new PasteAction());
		addAction(CUT, new CutAction());
		addAction(GROUP_HORIZONTAL, new GroupHorizontalAction());
		addAction(GROUP_VERTICAL, new GroupVerticalAction());
		addAction(EXPAND_SELECTION_LEFT, new ExpandSelectionAction(-1));
		addAction(EXPAND_SELECTION_RIGHT, new ExpandSelectionAction(1));
		addAction(SELECT_HORIZONTAL_ORIENTATION, new SelectOrientationAction(
				TextOrientation.HORIZONTAL));
		addAction(SELECT_VERTICAL_ORIENTATION, new SelectOrientationAction(
				TextOrientation.VERTICAL));
		addAction(SELECT_L2R_DIRECTION, new SelectTextDirectionAction(
				TextDirection.LEFT_TO_RIGHT));
		addAction(SELECT_R2L_DIRECTION, new SelectTextDirectionAction(
				TextDirection.RIGHT_TO_LEFT));
		addAction(UNDO, new UndoAction());
		addAction(REDO, new RedoAction());
		addAction(SET_MODE_LATIN, new SetModeAction('l'));
		addAction(SET_MODE_HIEROGLYPHS, new SetModeAction('s'));
		addAction(SET_MODE_ITALIC, new SetModeAction('i'));
		addAction(SET_MODE_BOLD, new SetModeAction('b'));
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
		addInput(controlKey+ " N", SET_MODE_HIEROGLYPHS);
		addInput(controlKey+ " D", SET_MODE_LATIN);
		addInput(controlKey+ " I", SET_MODE_ITALIC);
		addInput(controlKey+ " B", SET_MODE_BOLD);

	}

	public void control(JMDCEditor editor) {
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
		// On the mac, the "Alt" key is used to generate a number of characters (e.g. square []).
		// On other architectures, it is used as a shortcut for menus. So we behave differently.
		
		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
			if (e.isActionKey() || e.isControlDown() || e.isMetaDown())
				return;			
		} else
			if (e.isActionKey() || e.isControlDown() || e.isMetaDown()|| e.isAltDown())
				return;

		// Codes handled by actions.
		if (e.getKeyChar() == '\n')
			return;
		if (e.getSource() instanceof JMDCEditor) {
			JMDCEditor editor = (JMDCEditor) e.getSource();
			if (! editor.isEditable())
				return;
			editor.getWorkflow().keyTyped(e.getKeyChar());
		}
	}
}

/*
 * Created on 1 oct. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.editor;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import jsesh.editor.actions.AppDefaultFactory;
import jsesh.editor.actions.edit.CopyAction;
import jsesh.editor.actions.edit.CopyAsAction;
import jsesh.editor.actions.edit.CutAction;
import jsesh.editor.actions.edit.ExpandSelectionAction;
import jsesh.editor.actions.edit.PasteAction;
import jsesh.editor.actions.edit.RedoAction;
import jsesh.editor.actions.edit.SetModeAction;
import jsesh.editor.actions.edit.UndoAction;
import jsesh.editor.actions.move.GoDownAction;
import jsesh.editor.actions.move.GoLeftAction;
import jsesh.editor.actions.move.GoRightAction;
import jsesh.editor.actions.move.GoUpAction;
import jsesh.editor.actions.view.SelectOrientationAction;
import jsesh.editor.actions.view.SelectTextDirectionAction;
import jsesh.editor.actionsUtils.DelegatingAction;
import jsesh.editor.actionsUtils.Enabler;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdcDisplayer.clipboard.JSeshPasteFlavors;

import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.utils.PlatformDetection;

/**
 * Manages input for an MDC Editor. It might be interesting to make it public.
 * 
 * IMPORTANT : clean. Suppress unused stuff.
 * 
 * @author rosmord
 */

public class MDCEditorKeyManager extends KeyAdapter {

	/**
	 * The "normal " control key for this system.
	 */
	private String controlKey;

	private ActionMap actionMap;

	private InputMap inputMap;

	private JMDCEditor editor;

	MDCEditorKeyManager(JMDCEditor editor) {
		this.editor = editor;
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
		AppDefaults appDefaults = AppDefaultFactory.getAppDefaults();
		// For actions which modify their edited object...
		Enabler editorEnabler = new Enabler() {
			public boolean canDo() {
				return editor.isEditable();
			}
		};

		// MOVE
		addAction(ActionsID.GO_RIGHT, new GoRightAction(editor));
		addAction(ActionsID.GO_LEFT, new GoLeftAction(editor));
		addAction(ActionsID.GO_DOWN, new GoDownAction(editor));
		addAction(ActionsID.GO_UP, new GoUpAction(editor));
		// COPY/PASTE
		addAction(ActionsID.COPY, new CopyAction(editor));
		addAction(ActionsID.CUT, new CutAction(editor));
		addAction(ActionsID.COPY_AS_PDF, new CopyAsAction(editor,
				"Copy as PDF", JSeshPasteFlavors.PDFFlavor));
		addAction(ActionsID.COPY_AS_RTF, new CopyAsAction(editor,
				"Copy as RTF", JSeshPasteFlavors.RTFFlavor));
		addAction(ActionsID.COPY_AS_BITMAP, new CopyAsAction(editor,
				"Copy as Bitmap", DataFlavor.imageFlavor));
		addAction(ActionsID.COPY_AS_MDC, new CopyAsAction(editor,
				"Copy as MDC", DataFlavor.stringFlavor));
		addAction(PasteAction.ID, new PasteAction(editor));
		
		// DELEGATE ACTIONS...
		addDelegateAction(ActionsID.BEGINNING_OF_LINE, editor,
				"cursorToBeginningOfLine", appDefaults, null);
		addDelegateAction(ActionsID.END_OF_LINE, editor, "cursorToEndOfLine",
				appDefaults, null);

		// actions which modify the text content need to work on editable
		// objects.
		addDelegateAction(ActionsID.NEW_LINE, editor, "insertNewLine",
				appDefaults, editorEnabler);
		
		addDelegateAction(ActionsID.NEW_PAGE, editor, "insertPageBreak",
				appDefaults, editorEnabler);
		
		addDelegateAction(ActionsID.SELECT_ALL, editor, "selectAll",
				appDefaults, null);

		addDelegateAction(ActionsID.CLEAR_SELECTION, editor, "clearSelection",
				appDefaults, null);

		addDelegateAction(ActionsID.GROUP_HORIZONTAL, editor, "groupHorizontally", appDefaults, editorEnabler);

		addDelegateAction(ActionsID.GROUP_VERTICAL, editor, "groupVertically", appDefaults, editorEnabler);
		
		addAction(ActionsID.EXPAND_SELECTION_LEFT, new ExpandSelectionAction(
				editor, -1));
		addAction(ActionsID.EXPAND_SELECTION_RIGHT, new ExpandSelectionAction(
				editor, 1));
		addAction(ActionsID.EXPAND_SELECTION_UP, new ExpandSelectionAction(
				editor, -2));
		addAction(ActionsID.EXPAND_SELECTION_DOWN, new ExpandSelectionAction(
				editor, 2));
		addAction(ActionsID.SELECT_HORIZONTAL_ORIENTATION,
				new SelectOrientationAction(editor, TextOrientation.HORIZONTAL));
		addAction(ActionsID.SELECT_VERTICAL_ORIENTATION,
				new SelectOrientationAction(editor, TextOrientation.VERTICAL));
		addAction(ActionsID.SELECT_L2R_DIRECTION,
				new SelectTextDirectionAction(editor,
						TextDirection.LEFT_TO_RIGHT));
		addAction(ActionsID.SELECT_R2L_DIRECTION,
				new SelectTextDirectionAction(editor,
						TextDirection.RIGHT_TO_LEFT));
		addAction(ActionsID.UNDO, new UndoAction(editor));
		addAction(ActionsID.REDO, new RedoAction(editor));
		addAction(ActionsID.SET_MODE_LATIN, new SetModeAction(editor, 'l'));
		addAction(ActionsID.SET_MODE_HIEROGLYPHS,
				new SetModeAction(editor, 's'));
		addAction(ActionsID.SET_MODE_ITALIC, new SetModeAction(editor, 'i'));
		addAction(ActionsID.SET_MODE_BOLD, new SetModeAction(editor, 'b'));

	}

	private void addInput(String code, String command) {
		inputMap.put(KeyStroke.getKeyStroke(code), command);
	}

	// NOTE : add all relevant input so that the object can be used stand-alone.

	private void addInputs() {
		addInput("LEFT", ActionsID.GO_LEFT);
		addInput("RIGHT", ActionsID.GO_RIGHT);
		addInput("DOWN", ActionsID.GO_DOWN);
		addInput("UP", ActionsID.GO_UP);
		addInput("HOME", ActionsID.BEGINNING_OF_LINE);
		addInput("END", ActionsID.END_OF_LINE);
		// addInput("BACK_SPACE", BACK_SPACE);
		// addInput(':', ADD_LOWER_LEVEL);
		// addInput('*', ADD_SAME_LEVEL);
		// There are a number of ways to enter new cadrats :
		// addInput('-', ADD_NEW_CADRAT);
		// addInput("SPACE", ADD_NEW_CADRAT);
		// addInput("ENTER", ADD_NEW_CADRAT); // ENTER SHOULD ALSO CREATE A NEW
		// LINE !

		addInput("shift ENTER", ActionsID.NEW_PAGE);
		addInput("ENTER", ActionsID.NEW_LINE);
		addInput(controlKey + " Z", ActionsID.UNDO);
		addInput(controlKey + " Y", ActionsID.REDO);

		addInput(controlKey + " C", ActionsID.COPY);
		addInput(controlKey + " X", ActionsID.CUT);
		addInput(controlKey + " V", PasteAction.ID);

		if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX)
			addInput(controlKey + " J", ActionsID.GROUP_HORIZONTAL); // J On
																		// mac..
		else
			addInput(controlKey + " H", ActionsID.GROUP_HORIZONTAL); // J On
																		// mac..
		addInput(controlKey + " G", ActionsID.GROUP_VERTICAL);

		addInput("shift LEFT", ActionsID.EXPAND_SELECTION_LEFT);
		addInput("shift RIGHT", ActionsID.EXPAND_SELECTION_RIGHT);
		addInput("shift DOWN", ActionsID.EXPAND_SELECTION_DOWN);
		addInput("shift UP", ActionsID.EXPAND_SELECTION_UP);

		addInput(controlKey + " E", ActionsID.SET_MODE_HIEROGLYPHS);
		addInput(controlKey + " D", ActionsID.SET_MODE_LATIN);
		addInput(controlKey + " I", ActionsID.SET_MODE_ITALIC);
		addInput(controlKey + " B", ActionsID.SET_MODE_BOLD);
	}

	private void control(JMDCEditor editor) {
		editor.addKeyListener(this);
		editor.setActionMap(actionMap);
		editor.setInputMap(JComponent.WHEN_FOCUSED, inputMap);
		// editor.setInputMap(JComponent.WHEN_FOCUSED, new InputMap());
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

	/**
	 * Add an action.
	 * 
	 * @param ID
	 *            the ID used to identify the action in both map and property
	 *            files.
	 * @param editor
	 *            the editor which will receive the action.
	 * @param methodName
	 *            the method of the action (called on the editor's workflow).
	 * @param appDefaults
	 *            the application default for action properties (shortcut,
	 *            names, icons...)
	 * @param enabler
	 *            an optional enabler (may be null).
	 */
	private void addDelegateAction(String ID, JMDCEditor editor,
			String methodName, AppDefaults appDefaults, Enabler enabler) {
		addAction(ID, new DelegatingAction(editor.getWorkflow(), methodName,
				ID, appDefaults));
	}
}

/*
 * Created on 1 oct. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.editor;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
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
import jsesh.editor.actions.text.EditorShadeAction;
import jsesh.editor.actions.text.InsertElementIconAction;
import jsesh.editor.actions.view.SelectOrientationAction;
import jsesh.editor.actions.view.SelectTextDirectionAction;
import jsesh.editor.actionsUtils.DelegatingAction;
import jsesh.editor.actionsUtils.Enabler;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.ModelElement;
import jsesh.mdcDisplayer.clipboard.JSeshPasteFlavors;
import jsesh.swing.ImageIconFactory;

import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.BundledActionFiller;
import org.qenherkhopeshef.utils.PlatformDetection;

/**
 * Manages input for an MDC Editor.
 * 
 * @author rosmord
 */

class MDCEditorKeyManager extends KeyAdapter {

	private List<Action> shadingActions= new ArrayList<Action>();
	
	private List<Action> signShadingActions= new ArrayList<Action>();
	
	private static class ActionMapper {
	
		private ActionMap actionMap;

		private InputMap inputMap;

		private JMDCEditor editor;

		private AppDefaults appDefaults;

		ActionMapper(JMDCEditor editor) {
			this.editor = editor;
			//if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX) {
				//shortcutKey = "meta";
			//} else {
				//shortcutKey = "control";
			//}
			appDefaults = AppDefaultFactory.getAppDefaults();
			inputMap = new InputMap();
			actionMap = new ActionMap();
			addInputs();
			addActions();
			control(editor);
		}

		private void addActions() {
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
			addAction(ActionsID.PASTE, new PasteAction(editor));

			addAction(ActionsID.COPY_AS_PDF, new CopyAsAction(editor,
					JSeshPasteFlavors.PDFFlavor));
			addAction(ActionsID.COPY_AS_RTF, new CopyAsAction(editor,
					JSeshPasteFlavors.RTFFlavor));
			addAction(ActionsID.COPY_AS_BITMAP, new CopyAsAction(editor,
					DataFlavor.imageFlavor));
			addAction(ActionsID.COPY_AS_MDC, new CopyAsAction(editor,
					DataFlavor.stringFlavor));

			// DELEGATE ACTIONS...
			addDelegateAction(ActionsID.BEGINNING_OF_LINE, editor,
					"cursorToBeginningOfLine", null);
			addDelegateAction(ActionsID.END_OF_LINE, editor,
					"cursorToEndOfLine", null);

			// actions which modify the text content need to work on editable
			// objects.
			addDelegateAction(ActionsID.NEW_LINE, editor, "insertNewLine",
					editorEnabler);
			
			addDelegateAction(ActionsID.NEW_PAGE, editor, "insertPageBreak",
					editorEnabler);

			addDelegateAction(ActionsID.SELECT_ALL, editor, "selectAll", null);

			addDelegateAction(ActionsID.CLEAR_SELECTION, editor,
					"clearSelection", null);

			addDelegateAction(ActionsID.GROUP_HORIZONTAL, editor,
					"groupHorizontally", editorEnabler);

			addDelegateAction(ActionsID.GROUP_VERTICAL, editor,
					"groupVertically", editorEnabler);

			addDelegateAction(ActionsID.LIGATURE_ELEMENTS, editor,
					"ligatureElements", editorEnabler);
			
			addDelegateAction(ActionsID.LIGATURE_GLYPH_WITH_GROUP, editor,
					"ligatureHieroglyphWithGroup", editorEnabler);
			
			addDelegateAction(ActionsID.LIGATURE_GROUP_WITH_GLYPH, editor,
					"ligatureGroupWithHieroglyph", editorEnabler);

			addDelegateAction(ActionsID.EXPLODE_GROUP, editor,
					"explodeGroup", editorEnabler);

			addDelegateAction(ActionsID.INSERT_SPACE, editor, "insertSpace", editorEnabler);
			addDelegateAction(ActionsID.INSERT_HALF_SPACE, editor, "insertHalfSpace", editorEnabler);
			
			addInsertAction(ActionsID.INSERT_BLACK_POINT, editor, SymbolCodes.BLACKPOINT, editorEnabler );
			addInsertAction(ActionsID.INSERT_RED_POINT, editor, SymbolCodes.REDPOINT, editorEnabler );
			
			addDelegateAction(ActionsID.SHADE_ZONE, editor, "shadeZone", editorEnabler);
			addDelegateAction(ActionsID.UNSHADE_ZONE, editor, "unshadeZone", editorEnabler);
			addDelegateAction(ActionsID.RED_ZONE, editor, "paintZoneInRed", editorEnabler);
			addDelegateAction(ActionsID.BLACK_ZONE, editor, "paintZoneInBlack", editorEnabler);

			
			addAction(ActionsID.EXPAND_SELECTION_LEFT,
					new ExpandSelectionAction(editor, -1));
			addAction(ActionsID.EXPAND_SELECTION_RIGHT,
					new ExpandSelectionAction(editor, 1));
			addAction(ActionsID.EXPAND_SELECTION_UP, new ExpandSelectionAction(
					editor, -2));
			addAction(ActionsID.EXPAND_SELECTION_DOWN,
					new ExpandSelectionAction(editor, 2));
			addAction(ActionsID.SELECT_HORIZONTAL_ORIENTATION,
					new SelectOrientationAction(editor,
							TextOrientation.HORIZONTAL));
			addAction(ActionsID.SELECT_VERTICAL_ORIENTATION,
					new SelectOrientationAction(editor,
							TextOrientation.VERTICAL));
			addAction(ActionsID.SELECT_L2R_DIRECTION,
					new SelectTextDirectionAction(editor,
							TextDirection.LEFT_TO_RIGHT));
			addAction(ActionsID.SELECT_R2L_DIRECTION,
					new SelectTextDirectionAction(editor,
							TextDirection.RIGHT_TO_LEFT));
			addAction(ActionsID.UNDO, new UndoAction(editor));
			addAction(ActionsID.REDO, new RedoAction(editor));
			// editing modes...
			addEditingModeAction(ActionsID.SET_MODE_LATIN, 'I');
			addEditingModeAction(ActionsID.SET_MODE_HIEROGLYPHS, 's');
			addEditingModeAction(ActionsID.SET_MODE_ITALIC, 'i');
			addEditingModeAction(ActionsID.SET_MODE_BOLD, 'b');
			addEditingModeAction(ActionsID.SET_MODE_LINENUMBER, '|');
			addEditingModeAction(ActionsID.SET_MODE_TRANSLIT, 't');
			
			// Shading 
			for (Entry<String, Action> e: EditorShadeAction.generateActionMap(editor).entrySet()) {
				actionMap.put(e.getKey(), e.getValue());
			}
		}


		/**
		 * Bind keystrokes to actions
		 */
		private void addInputs() {
		}

		private void control(JMDCEditor editor) {
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

		/**
		 * Add an action linked to a method of JMDCEditor.
		 * 
		 * @param ID
		 *            the ID used to identify the action in both map and
		 *            property files.
		 * @param editor
		 *            the editor which will receive the action.
		 * @param methodName
		 *            the method of the action (called on the editor's
		 *            workflow).
		 * @param appDefaults
		 *            the application default for action properties (shortcut,
		 *            names, icons...)
		 * @param enabler
		 *            an optional enabler (may be null).
		 */
		private void addDelegateAction(String ID, JMDCEditor editor,
				String methodName, Enabler enabler) {
			addAction(ID,
					new DelegatingAction(editor.getWorkflow(), methodName));
		}


		/**
		 * Add a action for inserting a given element.
		 * @param id
		 * @param editor
		 * @param symbolCode see {@link SymbolCodes}
		 * @param editorEnabler
		 */
		private void addInsertAction(String id,
				JMDCEditor editor, int symbolCode,
				Enabler editorEnabler) {
			Action action= new InsertElementIconAction(editor, symbolCode) ;
			addAction(id, action);
		}
		
		private void addInsertAction(String id,
				JMDCEditor editor, ModelElement modelElement,
				Enabler editorEnabler) {
			Action action= new InsertElementIconAction(editor, modelElement) ;
			addAction(id, action);
		}
		
		
		
		private void addEditingModeAction(String actionID, char mode) {
			SetModeAction action = new SetModeAction(editor, mode);
			addAction(actionID, action);
		}

		private void addAction(String actionID, Action action) {
			actionMap.put(actionID, action);
			BundledActionFiller.initActionProperties(action, actionID,
					appDefaults);
			getActionMap().put(actionID, action);
			// Bind the action keystrokes according to the action properties.
			Object accelerator = action.getValue(Action.ACCELERATOR_KEY);
			if (accelerator != null)
				getInputMap().put((KeyStroke) accelerator, actionID);
			// Bind the Icon according to the IconMdC property
			String iconMdcCode= appDefaults.getString(actionID+ "." + "IconMdC");
			if (iconMdcCode != null) {
				ImageIcon icon = ImageIconFactory.buildImage((String) iconMdcCode);
				action.putValue(Action.SMALL_ICON, icon);			
			}
		}

	}

	public MDCEditorKeyManager(JMDCEditor editor) {
		super();
		new ActionMapper(editor);
		editor.addKeyListener(this);
	}

	/*
	 * Control direct key typing (not shortcuts).
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

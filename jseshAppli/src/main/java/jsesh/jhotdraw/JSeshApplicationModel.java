package jsesh.jhotdraw;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import jsesh.editor.ActionsID;
import jsesh.editor.JMDCEditor;
import jsesh.jhotdraw.actions.file.ExportAsBitmapAction;
import jsesh.jhotdraw.actions.file.ImportPDFAction;
import jsesh.swing.signPalette.HieroglyphPaletteListener;
import jsesh.swing.signPalette.JSimplePalette;
import jsesh.swing.signPalette.PalettePresenter;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.DefaultApplicationModel;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.edit.ClearSelectionAction;
import org.jhotdraw_7_4_1.app.action.edit.DeleteAction;
import org.jhotdraw_7_4_1.app.action.edit.DuplicateAction;
import org.jhotdraw_7_4_1.app.action.edit.SelectAllAction;
import org.jhotdraw_7_4_1.gui.URIChooser;
import org.qenherkhopeshef.jhotdrawChanges.StandardMenuBuilder;

@SuppressWarnings("serial")
public class JSeshApplicationModel extends DefaultApplicationModel {

	/**
	 * Some actions require a knowledge of the application...
	 */
	private Application application;

	@Override
	public void initApplication(Application a) {
		super.initApplication(a);
		this.application = a;
	}

	@Override
	public List<JMenu> createMenus(Application a, View v) {
		List<JMenu> menus = new ArrayList<JMenu>();
		menus.add(createTextMenu(a,(JSeshView)v));
		return menus;
	}

	private JMenu createTextMenu(Application a, JSeshView v) {
		JMenu textMenu= new JMenu();
		BundleHelper.configure(textMenu, "text");
		addToMenu(textMenu, a,v, ActionsID.GROUP_HORIZONTAL);
		addToMenu(textMenu, a,v, ActionsID.GROUP_VERTICAL);

		return textMenu;
	}

	/**
	 * Optional action addition.
	 * @param menu
	 * @param a
	 * @param v
	 * @param actionID
	 */
	private void addToMenu(JMenu menu, Application a, JSeshView v,
			String actionID) {
		Action action= a.getActionMap(v).get(actionID);
		if (action != null)
			menu.add(action);
	}

	private JSimplePalette createHieroglyphicPalette() {
		PalettePresenter palettePresenter = new PalettePresenter();
		palettePresenter
				.setHieroglyphPaletteListener(new MyHieroglyphicPaletteListener());
		return palettePresenter.getSimplePalette();
	}

	@Override
	public List<JToolBar> createToolBars(Application a, View p) {
		List<JToolBar> toolbars = new ArrayList<JToolBar>();
		JToolBar hoolbar = new JToolBar("hieroglyphs");
		hoolbar.add(createHieroglyphicPalette());
		// toolbars.add(hoolbar);
		toolbars.add(hoolbar);
		return toolbars;
	}

	public ActionMap createActionMap(Application a, View v) {
		JMDCEditor editor = null;
		JSeshView jseshView = (JSeshView) v;
		if (jseshView != null) {
			editor = jseshView.getEditor();
		}

		ActionMap map = super.createActionMap(a, v);
		map.remove(DuplicateAction.ID);
		map.remove(DeleteAction.ID);
		map.put(SelectAllAction.ID,
				getEditorAction(editor, jsesh.editor.ActionsID.SELECT_ALL));
		map.put(ClearSelectionAction.ID,
				getEditorAction(editor, jsesh.editor.ActionsID.CLEAR_SELECTION));
		addEditorAction(map, editor, ActionsID.COPY_AS_PDF);
		addEditorAction(map, editor, ActionsID.COPY_AS_RTF);
		addEditorAction(map, editor, ActionsID.COPY_AS_BITMAP);
		addEditorAction(map, editor, ActionsID.COPY_AS_MDC);

		addEditorAction(map, editor, ActionsID.SET_MODE_HIEROGLYPHS);
		addEditorAction(map, editor, ActionsID.SET_MODE_ITALIC);
		addEditorAction(map, editor, ActionsID.SET_MODE_LATIN);
		addEditorAction(map, editor, ActionsID.SET_MODE_BOLD);
		
		addEditorAction(map, editor, ActionsID.GROUP_HORIZONTAL);
		addEditorAction(map, editor, ActionsID.GROUP_VERTICAL);

		return map;
	}

	private void addEditorAction(ActionMap map, JMDCEditor editor,
			String editorActionID) {
		map.put(editorActionID, getEditorAction(editor, editorActionID));

	}

	/**
	 * Returns an action linked to the current editor in a safe way. If there is
	 * no current editor, returns null...
	 * 
	 * @param editor
	 * @param id
	 * @return
	 */
	private Action getEditorAction(JMDCEditor editor, String id) {
		if (editor == null) {
			return null;
		} else {
			return editor.getActionMap().get(id);
		}
	}

	@Override
	public URIChooser createOpenChooser(Application a, View v) {
		return super.createOpenChooser(a, v);
	}

	@Override
	public URIChooser createSaveChooser(Application a, View v) {
		return super.createSaveChooser(a, v);
	}

	private class MyHieroglyphicPaletteListener implements
			HieroglyphPaletteListener {

		public void signSelected(String code) {
			if (application.getActiveView() != null
					&& application.getActiveView() instanceof JSeshView) {
				JSeshView currentView = (JSeshView) application.getActiveView();
				currentView.insertCode(code);
			} else {
				System.err.println(application.getActiveView());
			}
		}
	}

	@Override
	/**
	 * Create the standard menus for this application.
	 * This method is not in the original jhotdraw 7. A better, yet similar, option has 
	 * been made available recently, and will be used a bit later.
	 * 
	 * Remark : action creation should not go here !!!!!!
	 */
	public StandardMenuBuilder getStandardMenuBuilder() {
		return new StandardMenuBuilder() {
			public void atEndOfFileMenu(JMenu fileMenu, Application app,
					View view) {
				// add the import and export as menus...
				JMenu importMenu = BundleHelper.configure(new JMenu(),
						"file.import");
				importMenu.add(new ImportPDFAction(app, view));
				JMenu exportMenu = BundleHelper.configure(new JMenu(),
						"file.export");
				exportMenu.add(new ExportAsBitmapAction(app, view));

				fileMenu.add(importMenu);
				fileMenu.add(exportMenu);
			}

			public void atEndOfEditMenu(JMenu editMenu, Application app,
					View view) {
				editMenu.addSeparator();
				JMenu copyAsMenu = BundleHelper.configure(new JMenu(),
						"edit.copyAs");
				ActionMap map = app.getActionMap(view);
				copyAsMenu.add(map.get(ActionsID.COPY_AS_PDF));
				copyAsMenu.add(map.get(ActionsID.COPY_AS_RTF));
				copyAsMenu.add(map.get(ActionsID.COPY_AS_BITMAP));
				copyAsMenu.add(map.get(ActionsID.COPY_AS_MDC));

				editMenu.add(copyAsMenu);
				editMenu.addSeparator();
				editMenu.add(map.get(ActionsID.SET_MODE_HIEROGLYPHS));
				editMenu.add(map.get(ActionsID.SET_MODE_LATIN));
				editMenu.add(map.get(ActionsID.SET_MODE_ITALIC));

				// translit
				// line/page
				// short text
				// copy large size
				// copy small size
				// copy wysiwyg
			}

			public void afterFileOpen(JMenu fileMenu, Application app, View view) {
			}

			public void afterFileNew(JMenu fileMenu, Application app, View view) {
			}

			public void afterFileClose(JMenu fileMenu, Application app,
					View view) {
			}
		};
	}

}

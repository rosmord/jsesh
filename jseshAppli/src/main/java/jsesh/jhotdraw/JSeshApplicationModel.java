package jsesh.jhotdraw;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import jsesh.jhotdraw.actions.ExportAsBitmapAction;
import jsesh.jhotdraw.actions.ImportPDFAction;
import jsesh.swing.signPalette.HieroglyphPaletteListener;
import jsesh.swing.signPalette.JSimplePalette;
import jsesh.swing.signPalette.PalettePresenter;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.DefaultApplicationModel;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.edit.DeleteAction;
import org.jhotdraw_7_4_1.app.action.edit.DuplicateAction;
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
	public List<JMenu> createMenus(Application a, View p) {
		List<JMenu> menus = new ArrayList<JMenu>();

		return menus;
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
		ActionMap map = super.createActionMap(a, v);
		map.remove(DuplicateAction.ID);
		map.remove(DeleteAction.ID);
		return map;
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
	 * Create the menus for this application.
	 * This method is not in the original jhotdraw 7. A better, yet similar, option has 
	 * been made available recently, and will be used a bit later.
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
				exportMenu.add(new ExportAsBitmapAction(app,view));
				fileMenu.add(importMenu);
				fileMenu.add(exportMenu);
			}

			public void atEndOfEditMenu(JMenu editMenu, Application app,
					View view) {
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

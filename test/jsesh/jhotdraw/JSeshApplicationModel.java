package jsesh.jhotdraw;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import jsesh.signPalette.HieroglyphPaletteListener;
import jsesh.signPalette.HieroglyphicPaletteDialog;

import org.jhotdraw.app.Application;
import org.jhotdraw.app.DefaultApplicationModel;
import org.jhotdraw.app.View;
import org.jhotdraw.app.action.edit.DeleteAction;
import org.jhotdraw.app.action.edit.DuplicateAction;
import org.jhotdraw.gui.URIChooser;

public class JSeshApplicationModel extends DefaultApplicationModel {

	/**
	 * Some actions require a knowledge of the application...
	 */
	private Application application;
	
	@Override
	public void initApplication(Application a) {
		super.initApplication(a);
		this.application= a;
		HieroglyphicPaletteDialog paletteDialog= new HieroglyphicPaletteDialog(null);
		a.addWindow(paletteDialog.getDialog(), null);
		paletteDialog.getDialog().setVisible(true);
		paletteDialog.setHieroglyphPaletteListener(new MyHieroglyphicPaletteListener());
		
	}
	
	@Override
	public List<JMenu> createMenus(Application a, View p) {
		List<JMenu> menus = new ArrayList<JMenu>();
		return menus;
	}

	@Override
	public List<JToolBar> createToolBars(Application a, View p) {
		ArrayList<JToolBar> toolbars = new ArrayList<JToolBar>();
	
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

	private class MyHieroglyphicPaletteListener implements HieroglyphPaletteListener {

		public void signSelected(String code) {
			if (application.getActiveView() != null && application.getActiveView() instanceof JSeshView) {
				JSeshView currentView= (JSeshView) application.getActiveView();
				currentView.insertCode(code);
			} else {
				System.err.println(application.getActiveView());
			}
		}
		
	}
}

package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import jsesh.jhotdraw.JSeshView;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.preferences.ui.DrawingSpecificationsPresenter;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractViewAction;

@SuppressWarnings("serial")
public class EditDocumentPreferencesAction extends AbstractViewAction {

	public EditDocumentPreferencesAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	public static final String ID = "file.documentProperties";

	public void actionPerformed(ActionEvent arg0) {
		JSeshView v = (JSeshView) getActiveView();
		if (v != null) {
			// TODO : ok, enought with current drawing specifications system...
			// we are going to use the old, simpler system with only a class...
			DrawingSpecificationsImplementation d = (DrawingSpecificationsImplementation) v
					.getDrawingSpecifications();
			DrawingSpecificationsPresenter presenter = new DrawingSpecificationsPresenter();
			presenter
					.loadPreferences(new DrawingSpecificationsImplementation());
			if (presenter.showDialog(v) == JOptionPane.OK_OPTION) {
				presenter.updatePreferences(d);
				v.setDrawingSpecifications(d);
			}
		}
	}
}

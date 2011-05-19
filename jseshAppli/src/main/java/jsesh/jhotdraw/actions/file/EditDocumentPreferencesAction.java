package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;

import jsesh.jhotdraw.actions.BundleHelper;


import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractViewAction;

@SuppressWarnings("serial")
public class EditDocumentPreferencesAction extends AbstractViewAction {

	public EditDocumentPreferencesAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	public static final String ID= "file.documentProperties";

	public void actionPerformed(ActionEvent arg0) {
		throw new UnsupportedOperationException("write me!");
	}
}

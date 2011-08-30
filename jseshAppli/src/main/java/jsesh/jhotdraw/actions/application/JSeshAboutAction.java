package jsesh.jhotdraw.actions.application;

import java.awt.Component;
import java.awt.event.ActionEvent;

import jsesh.swing.about.AboutDisplayer;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.action.app.AboutAction;

@SuppressWarnings("serial")
public class JSeshAboutAction extends AboutAction {

	
	public JSeshAboutAction(Application app) {
		super(app);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		Application app = getApplication();
		Component c = app.getComponent();
		if (c != null && c.getBounds().isEmpty()) {
			c = null;
		}

		AboutDisplayer aboutDisplayer = new AboutDisplayer(c);
		aboutDisplayer.show();

	}
}

package jsesh.jhotdraw.actions.application;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.action.app.AbstractPreferencesAction;

@SuppressWarnings("serial")
public class JSeshApplicationPreferenceAction extends AbstractPreferencesAction {

	public JSeshApplicationPreferenceAction(Application app) {
		super(app);
	}

	public void actionPerformed(ActionEvent e) {
		JOptionPane.showMessageDialog(getApplication().getComponent(), "Here soon a preference menu");
	}
}

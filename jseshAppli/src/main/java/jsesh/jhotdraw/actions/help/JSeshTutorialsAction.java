package jsesh.jhotdraw.actions.help;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;

import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.action.app.AboutAction;

/// Opens the JSesh tutorials web page in the user's browser.
@SuppressWarnings("serial")
public class JSeshTutorialsAction extends AboutAction {

	public static final String ID = "help.tutorials";

	private static final String TUTORIALS_URL = "https://jsesh.qenherkhopeshef.org/tutorials";

	public JSeshTutorialsAction(Application app) {
		super(app);
		BundleHelper.getInstance().configureActionNoIcon(this);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			try {
				Desktop.getDesktop().browse(URI.create(TUTORIALS_URL));
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}
}

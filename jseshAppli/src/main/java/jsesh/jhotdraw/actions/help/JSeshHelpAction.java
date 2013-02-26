package jsesh.jhotdraw.actions.help;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.*;

import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.action.app.AboutAction;

@SuppressWarnings("serial")
public class JSeshHelpAction extends AboutAction {

	public static final String ID = "help.documentation";

	public JSeshHelpAction(Application app) {
		super(app);
		BundleHelper.getInstance().configure(this);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		//Application app = getApplication();
		if (Desktop.isDesktopSupported()) {
			if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				try {
					URI uri= new URI("http://jseshdoc.qenherkhopeshef.org/");
					Desktop.getDesktop().browse(uri);
				} catch (URISyntaxException e) {
					e.printStackTrace();
					throw new RuntimeException(e);					
				} catch (IOException e) {		
					e.printStackTrace();
					throw new RuntimeException(e);					
				}
			}
		}

	}
}

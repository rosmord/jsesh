package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;

import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractViewAction;

@SuppressWarnings("serial")
public class SetAsModelAction extends AbstractViewAction {

	public static final String ID="file.setAsModel";

	public SetAsModelAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	public void actionPerformed(ActionEvent e) {
		throw new UnsupportedOperationException("Write me");
	}
	
}

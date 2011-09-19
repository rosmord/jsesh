package jsesh.jhotdraw.actions.edit;

import java.awt.event.ActionEvent;

import jsesh.jhotdraw.JSeshView;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;
import org.jhotdraw_7_6.app.action.edit.SelectAllAction;
import org.jhotdraw_7_6.util.ResourceBundleUtil;

@SuppressWarnings("serial")
public class JSeshSelectAllAction extends AbstractViewAction {

	public JSeshSelectAllAction(Application app, View view) {
		super(app, view);
		ResourceBundleUtil labels = ResourceBundleUtil
				.getBundle("org.jhotdraw_7_6.app.Labels");
		labels.configureAction(this, SelectAllAction.ID);
	}

	public void actionPerformed(ActionEvent e) {
		JSeshView v= (JSeshView) getActiveView();
		if (v != null) {
			v.getEditor().getWorkflow().selectAll();
		}
	}

}

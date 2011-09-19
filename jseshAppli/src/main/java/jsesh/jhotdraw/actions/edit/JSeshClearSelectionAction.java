package jsesh.jhotdraw.actions.edit;

import java.awt.event.ActionEvent;

import jsesh.jhotdraw.JSeshView;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;
import org.jhotdraw_7_6.app.action.edit.ClearSelectionAction;
import org.jhotdraw_7_6.util.ResourceBundleUtil;

@SuppressWarnings("serial")
/**
 * Clear the selection.
 * <p>Note that we could consider using the notion of abstract selection action, in jhotdraw, or the similar system 
 * used by old versions of JSesh.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public class JSeshClearSelectionAction extends AbstractViewAction {

	public JSeshClearSelectionAction(Application app, View view) {
		super(app, view);
		ResourceBundleUtil labels = ResourceBundleUtil
				.getBundle("org.jhotdraw_7_6.app.Labels");
		labels.configureAction(this, ClearSelectionAction.ID);
	}

	public void actionPerformed(ActionEvent e) {
		JSeshView v= (JSeshView) getActiveView();
		if (v != null) {
			v.getEditor().getWorkflow().clearSelection();
		}
	}

}

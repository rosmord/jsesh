package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;

import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.JSeshView;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

/**
 * This action uses the current view's defaults as generic defaults for new views.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
@SuppressWarnings("serial")
public class SetAsModelAction extends AbstractViewAction {

	public static final String ID="file.setAsModel";

	public SetAsModelAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	public void actionPerformed(ActionEvent e) {
		JSeshView view= (JSeshView) getActiveView();
		JSeshApplicationModel app = (JSeshApplicationModel) getApplication().getModel();
		app.setDefaultDrawingSpecifications(view.getDrawingSpecifications());
	}
	
}

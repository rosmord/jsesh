package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;

import jsesh.jhotdraw.JSeshApplicationCore;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.documentview.JSeshViewCore;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

/**
 * Apply the standard default preferences to the current view.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
@SuppressWarnings("serial")
public class ApplySavedStyleAction extends AbstractCoreViewAction {

	public static final String ID="file.applyModel";

	public ApplySavedStyleAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

        @Override
	public void actionPerformed(ActionEvent e) {
		appCore().applyNewDocumentStyleTo(viewCore());		
	}
	
}

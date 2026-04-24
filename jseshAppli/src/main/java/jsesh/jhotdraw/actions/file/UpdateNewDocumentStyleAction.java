package jsesh.jhotdraw.actions.file;

import java.awt.event.ActionEvent;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;

/**
 * This action uses the current view's defaults as generic defaults for new
 * views.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
@SuppressWarnings("serial")
public class UpdateNewDocumentStyleAction extends AbstractCoreViewAction {

	public static final String ID = "file.setAsModel";

	public UpdateNewDocumentStyleAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	public void actionPerformed(ActionEvent e) {
		viewCore().ifPresent(v -> {
			appCore().updateNewDocumentStyleFrom(v);
		});
	}

}

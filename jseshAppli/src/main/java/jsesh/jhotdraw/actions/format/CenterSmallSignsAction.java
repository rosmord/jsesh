package jsesh.jhotdraw.actions.format;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;

import jsesh.jhotdraw.JSeshView;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractViewAction;

/**
 * Action used to toggle the centering of small signs.
 * <p>
 * This action is somehow awkard, as it is targeted to a very specific SWING
 * component, i.e. a JCheckBoxMenuItem.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
@SuppressWarnings("serial")
public class CenterSmallSignsAction extends AbstractViewAction {
	public static final String ID = "format.centerSmallSigns";

	public CenterSmallSignsAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
		//
		setPropertyName(JSeshView.DOCUMENT_INFO_PROPERTY);
	}

	public void actionPerformed(ActionEvent e) {
		JSeshView view = (JSeshView) getActiveView();

		if (view != null) {
			JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
			view.setSmallSignsCentered(item.isSelected());
		}
	}

	@Override
	public JSeshView getActiveView() {
		return (JSeshView) super.getActiveView();
	}

	@Override
	protected void updateView() {
		putValue(Action.SELECTED_KEY, getActiveView() != null
				&& getActiveView().getEditor().isSmallSignsCentered());
	}

}

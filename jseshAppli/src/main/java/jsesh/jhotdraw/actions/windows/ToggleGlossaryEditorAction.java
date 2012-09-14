package jsesh.jhotdraw.actions.windows;

import java.awt.event.ActionEvent;

import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.action.AbstractApplicationAction;
import org.jhotdraw_7_6.app.action.ActionUtil;

@SuppressWarnings("serial")
public class ToggleGlossaryEditorAction extends AbstractApplicationAction {

	public static final String ID = "windows.toggleGlossaryEditor";

	public ToggleGlossaryEditorAction(Application app) {
		super(app);
		BundleHelper.getInstance().configure(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JSeshApplicationModel model = (JSeshApplicationModel) getApplication()
				.getModel();
		boolean visible = model.getGlossaryEditor().getFrame().isVisible();
		model.getGlossaryEditor().getFrame().setVisible(!visible);
		putValue(ActionUtil.SELECTED_KEY, !visible);
	}
	
}

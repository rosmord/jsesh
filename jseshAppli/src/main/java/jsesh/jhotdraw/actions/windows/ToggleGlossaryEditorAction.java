package jsesh.jhotdraw.actions.windows;

import java.awt.event.ActionEvent;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.action.ActionUtil;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.AbstractCoreApplicationAction;
import jsesh.jhotdraw.utils.ComponentMenuActionChecker;

@SuppressWarnings("serial")
public class ToggleGlossaryEditorAction extends AbstractCoreApplicationAction {

	public static final String ID = "windows.toggleGlossaryEditor";

	public ToggleGlossaryEditorAction(Application app) {
		super(app);
		BundleHelper.getInstance().configure(this);
		// Link this action to the state of the glossary editor.
		appCore().glossaryEditor().getFrame().addComponentListener(
			new ComponentMenuActionChecker(this));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		boolean visible = appCore().glossaryEditor().isVisible();
		appCore().glossaryEditor().setVisible(!visible);
		putValue(ActionUtil.SELECTED_KEY, !visible);
	}

}

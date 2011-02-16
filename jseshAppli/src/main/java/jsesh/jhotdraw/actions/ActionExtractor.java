package jsesh.jhotdraw.actions;

import javax.swing.Action;

import jsesh.jhotdraw.JSeshView;

/**
 * Extracts an action from a view, or create one if no view is available.
 * @author rosmord
 */
public class ActionExtractor {
	public static Action extractActionFromView(JSeshView view, String ACTIONID) {
		if (view != null) {
			return view.getEditor().getActionMap().get(ACTIONID);
		} else {
			return null;
		}
	}
}

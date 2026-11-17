package jsesh.mdcDisplayer.swing.actionsUtils;

import javax.swing.AbstractAction;
import javax.swing.Icon;

/**
 * A kind of actions which will be enabled or disabled according to changes in the  preferences.
 * @author rosmord
 *
 */
abstract public class PreferenceBoundAction extends AbstractAction implements PreferencesChangeListener {

	public PreferenceBoundAction() {
		super();
	}

	public PreferenceBoundAction(String name, Icon icon) {
		super(name, icon);
	}

	public PreferenceBoundAction(String name) {
		super(name);
	}
}

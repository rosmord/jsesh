package jsesh.jhotdraw.actions.edit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

import jsesh.jhotdraw.actions.BundleHelper;

@SuppressWarnings("serial")
public class OpenHieroglyphicMenuAction extends AbstractAction {
	public static String ID= "hieroglyphicMenu";
	
	private JPopupMenu menu;
	private JButton parent;
	
	public OpenHieroglyphicMenuAction(JButton parent, JPopupMenu hieroglyphicMenu) {
		BundleHelper.getInstance().configure(this);
		this.menu= hieroglyphicMenu;
		this.parent= parent;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		menu.show(parent, 0, 0);
	}


}

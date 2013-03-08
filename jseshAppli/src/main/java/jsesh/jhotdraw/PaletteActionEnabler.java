package jsesh.jhotdraw;

import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.jhotdraw_7_6.app.action.ActionUtil;

/**
 * Manage action activation according to the state of a frame (for check box
 * menu items).
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
public class PaletteActionEnabler  {

	private Action action;
	private MyComponentListener listener;
	private Component component;
	
	
	public PaletteActionEnabler(Action action, Component component) {
		super();
		this.action = action;
		this.component= component;
		this.listener= new MyComponentListener(action, component);		
	}

	public void detach() {
		component.removeComponentListener(listener);
	}
	
	private class  MyComponentListener extends ComponentAdapter {
		public MyComponentListener(Action action, Component component) {
			super();
			component.addComponentListener(this);
		}

		@Override
		public void componentShown(ComponentEvent e) {
			checkBox();
		}

		@Override
		public void componentHidden(ComponentEvent e) {
			uncheckBox();
		}
	} 

	private void checkBox() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				action.putValue(ActionUtil.SELECTED_KEY, true);
			}
		});
	}

	private void uncheckBox() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				action.putValue(ActionUtil.SELECTED_KEY, false);
			}
		});
	}

}

package jsesh.jhotdraw.actions.format;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import jsesh.jhotdraw.JSeshView;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.mdc.constants.TextDirection;

import org.jhotdraw_7_4_1.app.Application;
import org.jhotdraw_7_4_1.app.View;
import org.jhotdraw_7_4_1.app.action.AbstractViewAction;

@SuppressWarnings("serial")
public class SetDocumentDirectionAction extends AbstractViewAction {

	public static final String ID= "format.setDirection_";
	private TextDirection textDirection;
	
	public SetDocumentDirectionAction(Application app, View view, TextDirection textDirection) {
		super(app, view);
		this.textDirection= textDirection;
		BundleHelper.getInstance().configure(this, getID());		
		setPropertyName(JSeshView.DOCUMENT_INFO_PROPERTY);
	}

	public String getID() {
		return ID + textDirection.getDesignation();
	}

	public void actionPerformed(ActionEvent e) {
		JSeshView view = (JSeshView) getActiveView();
		if (view != null) {
			//JRadioButtonMenuItem item= (JRadioButtonMenuItem) e.getSource();
			view.setTextDirection(textDirection);
		}
	}

	@Override
	public JSeshView getActiveView() {
		return (JSeshView) super.getActiveView();
	}

	@Override
	protected void updateView() {
		// The test on textDirection != null is there because the method is called by the constructor of the parent class, at a time when 
		// textOrientation hasn't got a value yet.
		
		putValue(Action.SELECTED_KEY, getActiveView() != null && textDirection != null 
				&& textDirection.equals(getActiveView().getEditor().getTextDirection()));
	}

	
}

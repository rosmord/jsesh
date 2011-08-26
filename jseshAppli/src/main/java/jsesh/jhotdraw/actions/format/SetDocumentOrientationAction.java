package jsesh.jhotdraw.actions.format;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import jsesh.jhotdraw.JSeshView;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.mdc.constants.TextOrientation;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

@SuppressWarnings("serial")
public class SetDocumentOrientationAction extends AbstractViewAction {

	public static final String ID= "format.setOrientation_";
	private TextOrientation textOrientation;
	
	public SetDocumentOrientationAction(Application app, View view, TextOrientation textOrientation) {
		super(app, view);
		this.textOrientation= textOrientation;
		BundleHelper.getInstance().configure(this, getID());		
		setPropertyName(JSeshView.DOCUMENT_INFO_PROPERTY);
	}

	public String getID() {
		return ID + textOrientation.name();
	}

	public void actionPerformed(ActionEvent e) {
		JSeshView view = (JSeshView) getActiveView();
		if (view != null) {
			//JRadioButtonMenuItem item= (JRadioButtonMenuItem) e.getSource();
			view.setTextOrientation(textOrientation);
		}
	}

	@Override
	public JSeshView getActiveView() {
		return (JSeshView) super.getActiveView();
	}

	@Override
	protected void updateView() {
		// The test on textOrientation != null is there because the method is called by the constructor of the parent class, at a time when 
		// textOrientation hasn't got a value yet.
		
		putValue(Action.SELECTED_KEY, getActiveView() != null && textOrientation != null 
				&& textOrientation.equals(getActiveView().getEditor().getTextOrientation()));
	}

	
}

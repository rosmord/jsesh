package jsesh.jhotdraw.actions.edit;

import java.awt.event.ActionEvent;

import jsesh.glossary.JGlossaryEditor;
import jsesh.jhotdraw.JSeshApplicationModel;
import jsesh.jhotdraw.JSeshView;
import jsesh.jhotdraw.actions.BundleHelper;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

/**
 * An action which adds the current selection to the so-called glossary.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
@SuppressWarnings("serial")
public class AddToGlossaryAction extends AbstractViewAction {
	public static final String ID = "edit.addToGlossary";

	public AddToGlossaryAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		JSeshView v = (JSeshView) getActiveView();
		JSeshApplicationModel app= (JSeshApplicationModel) getApplication().getModel();
		if (v != null) {
			if (! v.getEditor().hasSelection()) return;
			String mdc= v.getEditor().getSelection().toMdC();
			JGlossaryEditor editor= app.getGlossaryEditor();
			editor.getFrame().setVisible(true);
			editor.prepareToAdd(mdc);
		}
	}
	
}

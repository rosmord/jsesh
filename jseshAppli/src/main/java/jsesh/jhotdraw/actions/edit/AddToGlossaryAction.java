package jsesh.jhotdraw.actions.edit;

import java.awt.event.ActionEvent;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

import jsesh.glossary.JGlossaryEditor;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;

/**
 * An action which adds the current selection to the so-called glossary.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
@SuppressWarnings("serial")
public class AddToGlossaryAction extends AbstractCoreViewAction {
	public static final String ID = "edit.addToGlossary";

	public AddToGlossaryAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		viewCore()
				.filter(v -> v.getEditor().hasSelection())
				.ifPresent(v -> {
					String mdc = v.getEditor().getSelection().toMdC();
					JGlossaryEditor editor = appCore().glossaryEditor();
					editor.getFrame().setVisible(true);
					editor.prepareToAdd(mdc);
				});
	}

}

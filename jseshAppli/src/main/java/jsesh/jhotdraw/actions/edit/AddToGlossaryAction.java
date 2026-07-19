package jsesh.jhotdraw.actions.edit;

import java.awt.event.ActionEvent;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

import jsesh.io.mdc.MdCModelWriter;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;
import jsesh.model.TopItemList;
import jsesh.render.draw.MDCIconFactory;
import jsesh.ui.glossary.JGlossaryEditor;

/**
 * An action which adds the current selection to the so-called glossary.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
@SuppressWarnings("serial")
public class AddToGlossaryAction extends AbstractCoreViewAction {
	public static final String ID = "edit.addToGlossary";

	public AddToGlossaryAction(Application app, View view, MDCIconFactory mdcIconFactory) {
		super(app, view);
		BundleHelper.getInstance().configureActionWithIcon(this, mdcIconFactory);
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		viewCore()
				.filter(v -> v.getEditor().hasSelection())
				.ifPresent(v -> {
					MdCModelWriter mdCModelWriter = new MdCModelWriter();
					TopItemList list = v.getEditor().getSelection();
					String mdc = mdCModelWriter.toMdC(list);
					JGlossaryEditor editor = appCore().glossaryEditor();
					editor.getFrame().setVisible(true);
					editor.prepareToAdd(mdc);
				});
	}

}

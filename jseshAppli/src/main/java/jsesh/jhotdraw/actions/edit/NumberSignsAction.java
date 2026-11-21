package jsesh.jhotdraw.actions.edit;

import java.awt.event.ActionEvent;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;

/// Gives a unique id (`\sid`) to each sign of the document, and makes the editor
/// keep them: see `jsesh.document.SignIdManager`.
///
/// The change of the text can be undone with Undo, but the document remains
/// "numbered" until [RemoveSignIdsAction] is used.
@SuppressWarnings("serial")
public class NumberSignsAction extends AbstractCoreViewAction {

    public static final String ID = "text.numberSigns";

    public NumberSignsAction(Application app, View view) {
        super(app, view);
        BundleHelper.getInstance().configureActionWithID(this, ID);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        viewCore().ifPresent(v -> v.getEditor().getHieroglyphicTextModel().numberSigns());
    }
}

package jsesh.jhotdraw.actions.edit;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;

import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;

/// Removes all the sign ids (`\sid`) of the document, and stops the editor from
/// giving ids: see `jsesh.document.SignIdManager`.
///
/// The change of the text can be undone with Undo, but the document is no longer
/// "numbered".
@SuppressWarnings("serial")
public class RemoveSignIdsAction extends AbstractCoreViewAction {

    public static final String ID = "text.removeSignIds";

    public RemoveSignIdsAction(Application app, View view) {
        super(app, view);
        BundleHelper.getInstance().configureActionWithID(this, ID);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        viewCore().ifPresent(v -> {
            int choice = JOptionPane.showConfirmDialog(v.getEditor(),
                    BundleHelper.getInstance().getLabel("text.removeSignIds.confirm"),
                    BundleHelper.getInstance().getLabel("text.removeSignIds.text"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.OK_OPTION) {
                v.getEditor().getHieroglyphicTextModel().removeSignIds();
            }
        });
    }
}

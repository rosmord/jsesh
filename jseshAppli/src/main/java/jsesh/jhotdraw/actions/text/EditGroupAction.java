package jsesh.jhotdraw.actions.text;

import javax.swing.JOptionPane;

import org.jhotdraw_7_6.app.AbstractView;
import org.jhotdraw_7_6.app.Application;
import org.jhotdraw_7_6.app.View;
import org.jhotdraw_7_6.app.action.AbstractViewAction;

import jsesh.ui.editor.JMDCEditor;
import jsesh.ui.editor.actionsUtils.EditorAction;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.jhotdraw.documentview.JSeshView;
import jsesh.jhotdraw.utils.AbstractCoreViewAction;
import jsesh.model.AbsoluteGroup;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.ui.widgets.groupEditor.GroupEditorDialog;

/**
 * The action used to edit a group.
 * 
 * It will open a dialog to edit the group, and then replace the current
 * selection by the edited group.
 * 
 * @author rosmord
 */
@SuppressWarnings("serial")
public class EditGroupAction extends AbstractCoreViewAction {

	public EditGroupAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configureActionWithIcon(this, appCore().getMdcIconFactory());
	}

	public static final String ID = "text.editGroup";


	public void actionPerformed(java.awt.event.ActionEvent arg0) {
		JSeshView jSeshView = (JSeshView) getActiveView();
		if (jSeshView != null) {
			JMDCEditor editor = jSeshView.getEditor();
			JSeshRenderContext renderContext = editor.getRenderContext();
			JSeshTechRenderContext techContext = JSeshTechRenderContext.buildForComponent(editor);
			AbsoluteGroup g = editor.getWorkflow().buildAbsoluteGroup(renderContext, techContext);

			if (g != null) {
				GroupEditorDialog d = new GroupEditorDialog(renderContext);
				d.setGroup(g);
				int choice = JOptionPane.showConfirmDialog(editor, d, "Group editor", JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE);
				if (choice == JOptionPane.OK_OPTION) {
					editor.getWorkflow().replaceSelectionByAbsoluteGroup(g);
				}
			}
		}
	}

}

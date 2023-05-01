package jsesh.jhotdraw.actions.text;

import javax.swing.JOptionPane;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;
import jsesh.jhotdraw.actions.BundleHelper;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.swing.groupEditor.GroupEditorDialog;

@SuppressWarnings("serial")
public class EditGroupAction extends EditorAction {

	public static final String ID="text.editGroup";
	
	public EditGroupAction(JMDCEditor editor) {
		super(editor);
		BundleHelper.getInstance().configure(this);
	}

	public void actionPerformed(java.awt.event.ActionEvent arg0) {

		AbsoluteGroup g = editor.getWorkflow().buildAbsoluteGroup();

		if (g != null) {
			GroupEditorDialog d = new GroupEditorDialog(editor.getDrawingSpecifications());			
			d.setGroup(g);
			int choice = JOptionPane.showConfirmDialog(editor, d, "Group editor", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (choice == JOptionPane.OK_OPTION) {
				editor.getWorkflow().replaceSelectionByAbsoluteGroup(g);
			}
		}
	}

}

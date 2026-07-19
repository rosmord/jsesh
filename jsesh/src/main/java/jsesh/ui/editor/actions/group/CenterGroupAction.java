package jsesh.ui.editor.actions.group;

import jsesh.ui.editor.JMDCEditor;
import jsesh.ui.editor.actionsUtils.EditorAction;

import java.awt.event.ActionEvent;

/**
 * Center a sign or a group of sign vertically.
 * Add spaces both on top and below those signs.
 */
public class CenterGroupAction extends EditorAction {
    public  static final  String ID = "text.centerGroup";

    public CenterGroupAction(JMDCEditor editor) {
        super(editor);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        editor.getWorkflow().centerGroup();
    }
}

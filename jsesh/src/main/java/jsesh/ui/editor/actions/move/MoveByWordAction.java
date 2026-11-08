package jsesh.ui.editor.actions.move;

import java.awt.event.ActionEvent;

import jsesh.ui.editor.ArrowKeyMode;
import jsesh.ui.editor.JMDCEditor;
import jsesh.ui.editor.actionsUtils.EditorAction;

/// Moves the cursor (or extends the selection) one word of alphabetic text to
/// the left or to the right. Outside of alphabetic text, moves by one element.
///
/// Left and right are logical: the direction of the editor's [ArrowKeyMode]
/// tells which one goes forward (the orientation is ignored).
///
/// @author rosmord
@SuppressWarnings("serial")
public class MoveByWordAction extends EditorAction {

    private final boolean left;
    private final boolean extendSelection;

    /// @param editor the editor.
    /// @param left true to move left, false to move right.
    /// @param extendSelection true to extend the selection instead of moving
    /// the cursor.
    public MoveByWordAction(JMDCEditor editor, boolean left, boolean extendSelection) {
        super(editor);
        this.left = left;
        this.extendSelection = extendSelection;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean forward = editor.getArrowKeyMode().isWordMoveForward(left);
        if (extendSelection) {
            editor.getWorkflow().expandSelectionByWord(forward ? 1 : -1);
        } else if (forward) {
            editor.getWorkflow().cursorNextWord();
        } else {
            editor.getWorkflow().cursorPreviousWord();
        }
    }
}

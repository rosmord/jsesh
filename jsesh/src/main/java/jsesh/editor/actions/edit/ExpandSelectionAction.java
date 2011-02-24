/*
 * Created on 2 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor.actions.edit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;

/**
 * @author S. Rosmorduc
 *
 */
public class ExpandSelectionAction extends EditorAction  {

	private int dir;
	/**
	 * Action that expand the selection in the direction dir.
	 * @param editor : the editor.
	 * @param dir : -1 for left, 1 for right ; -2 for left and -1 for right.
	 */
	public ExpandSelectionAction(JMDCEditor editor, int dir) {
		super(editor);
		this.dir= dir;
		if (dir != 1 && dir != -1 && dir != -2 && dir != 2)
			throw new RuntimeException("incorrect dir");
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		int actualDir= 0;
		// Transform "dir" into the correct value.
		if (editor.getDrawingSpecifications().getTextDirection().isLeftToRight()) {
			if (editor.getDrawingSpecifications().getTextOrientation().isHorizontal()) {
					// dir is already correct.
				actualDir= dir;
			} else {
				switch (dir) {
				case 1: actualDir= 2; break;
				case -1: actualDir= -2; break;
				case 2: actualDir= 1; break;
				case -2: actualDir= -1; break;
				}
			}
		} else {
			if (editor.getDrawingSpecifications().getTextOrientation().isHorizontal()) {
				if (dir == 1 || dir == -1)
					actualDir= -dir;
				else 
					actualDir= dir;
			} else {
				switch (dir) {
				case 1: actualDir= -2; break;
				case -1: actualDir= 2; break;
				case 2: actualDir= 1; break;
				case -2: actualDir= -1; break;
				}
			}
		}
		editor.getWorkflow().expandSelection(actualDir);
	}
}

/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.editor.actions.edit;

import java.awt.event.ActionEvent;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;

/**
 * @author S. Rosmorduc
 *
 */
@SuppressWarnings("serial")
public class ExpandSelectionAction extends EditorAction  {

	private int dir;
	/**
	 * Action that expand the selection in the direction dir.
	 * @param editor : the editor.
	 * @param dir : -1 for left, 1 for right ; -2 for up and -1 for down.
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

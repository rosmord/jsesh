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
package jsesh.ui.editor.actions.edit;

import java.awt.event.ActionEvent;

import jsesh.ui.editor.ArrowKeyPreferences;
import jsesh.ui.editor.JMDCEditor;
import jsesh.ui.editor.actionsUtils.EditorAction;

/// Extends the selection (Shift + arrow key).
///
/// The move is logical, as for the plain arrows (see [ArrowKeyPreferences]):
/// left and right go backward or forward in the text, up and down go to the
/// previous or next line (or column).
///
/// @author S. Rosmorduc
@SuppressWarnings("serial")
public class ExpandSelectionAction extends EditorAction {

	private final int dir;

	/// @param editor the editor.
	/// @param dir -1 for left, 1 for right, -2 for up, 2 for down.
	public ExpandSelectionAction(JMDCEditor editor, int dir) {
		super(editor);
		this.dir = dir;
		if (dir != 1 && dir != -1 && dir != -2 && dir != 2)
			throw new RuntimeException("incorrect dir");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int actualDir = dir;
		if (dir == 1 || dir == -1) {
			actualDir = ArrowKeyPreferences.isForward(dir == 1) ? 1 : -1;
		}
		editor.getWorkflow().expandSelection(actualDir);
	}
}

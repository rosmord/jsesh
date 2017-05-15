/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient
egyptian hieroglyphic texts.

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
package jsesh.jhotdraw.utils;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;


/**
 * Helper class for creating menus without bothering if the parent window is a
 * frame or a dialog.
 * 
 * @author rosmord
 */
public class JDialogHelper {

	static public JDialog createDialog(Component parent, String title,
			boolean modal) {
		Window ancestor = null;
		if (parent != null)
			ancestor = SwingUtilities.getWindowAncestor(parent);
		JDialog dialog;
		if (ancestor instanceof Frame || ancestor == null) {
			dialog = new JDialog((Frame) ancestor, title, modal);
		} else if (ancestor instanceof Dialog) {
			dialog = new JDialog((Dialog) ancestor, title, modal);
		} else {
			dialog = new JDialog((Frame) null, title, modal);
		}
		// Note : jdk 1.6 introduces new constructors which might be of some use
		// here
		// taking Window as argument.
		return dialog;
	}

}

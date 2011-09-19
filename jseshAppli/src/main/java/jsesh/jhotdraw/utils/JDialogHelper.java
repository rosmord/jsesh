/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.jhotdraw.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jsesh.jhotdraw.Messages;
import net.miginfocom.swing.MigLayout;

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

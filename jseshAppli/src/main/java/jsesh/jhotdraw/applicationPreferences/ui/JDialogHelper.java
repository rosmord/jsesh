/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.jhotdraw.applicationPreferences.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Helper class for creating menus without bothering if the parent window is a frame or a dialog.
 * @author rosmord
 */
class JDialogHelper {

    static public JDialog createDialog(Component parent, String title, boolean modal) {
        Window ancestor = SwingUtilities.getWindowAncestor(parent);
        JDialog dialog;
        if (ancestor instanceof Frame) {
            dialog = new JDialog((Frame) ancestor, title, modal);
        } else if (ancestor instanceof Dialog) {
            dialog = new JDialog((Dialog) ancestor, title, modal);
        } else {
            dialog = new JDialog((Frame) null, title, modal);
        }
        // Note : jdk 1.6 introduces new constructors which might be of some use here 
        // taking Window as argument.
        return dialog;
    }
}

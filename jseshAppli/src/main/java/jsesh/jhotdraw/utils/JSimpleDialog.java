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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import jsesh.resources.JSeshMessages;
import jsesh.utils.swing.OrientationUtils;
import net.miginfocom.swing.MigLayout;


/// A simple dialog form for getting information from the user. Manages possible
/// problems with focus and JFormattedTextFields (especially in Quaqua UI).
///
/// The form is placed in a scroll pane, and the dialog never gets larger than
/// the screen: on a small screen, the user scrolls the form, the OK and Cancel
/// buttons staying visible. The dialog can be resized.
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public class JSimpleDialog {

    private boolean okSelected = false;

    private final JButton okButton;
    private final JButton cancelButton;
    private final JDialog dialog;

    public JSimpleDialog(Component parent, Component form, String title) {
        dialog = JDialogHelper.createDialog(parent, title, true);
        okButton = new JButton(JSeshMessages.getString("generic.ok.text"));
        cancelButton = new JButton(JSeshMessages.getString("generic.cancel.text"));

        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        Container content = dialog.getContentPane();
        content.setLayout(new MigLayout("", "[grow, fill]", "[grow, fill][]"));
        JScrollPane scrollPane = new JScrollPane(form);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        content.add(scrollPane, "wrap");
        content.add(okButton, "split 2, tag ok, growx 0");
        content.add(cancelButton, "tag cancel, growx 0");
        dialog.pack();
        fitToScreen(parent, scrollPane);
        dialog.setResizable(true);
        dialog.getRootPane().setDefaultButton(okButton);
        OrientationUtils.fixComponentOrientation(dialog);
    }

    /// Shrinks the dialog if it is larger than the screen, and centres it on
    /// its parent.
    private void fitToScreen(Component parent, JScrollPane scrollPane) {
        Rectangle screen = FormLayoutHelper.usableScreenBounds(parent);
        Dimension size = dialog.getSize();
        int height = Math.min(size.height, screen.height);
        int width = size.width;
        if (height < size.height) {
            // Room for the vertical scroll bar, to avoid a horizontal one.
            width += scrollPane.getVerticalScrollBar().getPreferredSize().width;
        }
        width = Math.min(width, screen.width);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(parent);
    }

    /**
     * Display the dialog
     *
     * @return one of {@link JOptionPane#OK_OPTION} or
     * {@link JOptionPane#CANCEL_OPTION}
     */
    public int show() {
        okSelected = false;
        dialog.setVisible(true);
        return okSelected ? JOptionPane.OK_OPTION : JOptionPane.CANCEL_OPTION;
    }

    public void dispose() {
        dialog.dispose();
    }

    void ok() {
        okSelected = true;
        if (dialog.getFocusOwner() instanceof JFormattedTextField) {
            try {
                ((JFormattedTextField) dialog.getFocusOwner()).commitEdit();
            } catch (ParseException e) {
                // Do nothing (parse exception should be ignored here).
            }
        }
        dialog.setVisible(false);
    }

    void cancel() {
        okSelected = false;
        dialog.setVisible(false);
    }

}

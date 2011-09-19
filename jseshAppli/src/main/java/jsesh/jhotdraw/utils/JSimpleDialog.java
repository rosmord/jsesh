package jsesh.jhotdraw.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;

import net.miginfocom.swing.MigLayout;

import jsesh.jhotdraw.Messages;

/**
 * A simple dialog form for getting information from the user. Manages possible
 * problems with focus and JFormattedTextFields (especially in Quaqua UI).
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 * 
 */
public class JSimpleDialog {

	private boolean okSelected = false;

	private JButton okButton, cancelButton;
	private JDialog dialog;

	public JSimpleDialog(Component parent, Component form, String title) {
		dialog = JDialogHelper.createDialog(parent, title, true);
		okButton = new JButton(Messages.getString("ok"));
		cancelButton = new JButton(Messages.getString("cancel"));

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
		content.setLayout(new MigLayout());
		content.add(form, "wrap");
		content.add(okButton, "split 2, tag ok");
		content.add(cancelButton, "tag cancel");
		dialog.pack();
		dialog.setResizable(false);
		dialog.getRootPane().setDefaultButton(okButton);
	}

	/**
	 * Display the dialog
	 * 
	 * @return one of {@link JOptionPane#OK_OPTION} or
	 *         {@link JOptionPane#CANCEL_OPTION}
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

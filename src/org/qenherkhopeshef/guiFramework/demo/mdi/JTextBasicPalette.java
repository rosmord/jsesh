/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qenherkhopeshef.guiFramework.demo.mdi;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;

/**
 * 
 * A demonstration for an external palette whose control will be redirected to
 * the current focused window (if any).
 * 
 * @author rosmord
 */
public class JTextBasicPalette extends JDialog {

	private static final long serialVersionUID = 1L;
	
	
    public JTextBasicPalette(final TextEditorApplicationController controller) {
    		JButton textOne= new JButton("one");
    		JButton textTwo= new JButton("two");
    		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS)	);
    		add(textOne);
    		add(textTwo);
    		textOne.addActionListener(new ActionListener() {		
				@Override
				public void actionPerformed(ActionEvent e) {
					controller.insertText("one");
				}
			});
    		textOne.addActionListener(new ActionListener() {		
				@Override
				public void actionPerformed(ActionEvent e) {
					controller.insertText("two");
				}
			});
    		pack();
        setVisible(true);
    }
}

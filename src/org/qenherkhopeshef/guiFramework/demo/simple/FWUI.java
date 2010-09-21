package org.qenherkhopeshef.guiFramework.demo.simple;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JToolBar;

import org.qenherkhopeshef.guiFramework.busy.BusyAdvanceGlassPane;

/**
 * User interface elements.
 * A frame for the main window, a label as a mock graphical element simulating the actual display,
 * and an "advanceGlassPane" for blocking the interface during long processing.
 * @author rosmord
 *
 */
public class FWUI {
	private JFrame jframe= new JFrame("demo application");
	private JLabel label= new JLabel("test");
	private BusyAdvanceGlassPane busyAdvanceGlassPane;
	
	public FWUI() {
		busyAdvanceGlassPane= new BusyAdvanceGlassPane(jframe);
		busyAdvanceGlassPane.setMessage("loading");
		busyAdvanceGlassPane.setMessageColor(new Color(1, 0	, 0, 0.5f));
		busyAdvanceGlassPane.setTransparency(0.9f);
		
		jframe.getContentPane().setLayout(new BorderLayout());
		jframe.getContentPane().add(label, BorderLayout.CENTER);
	}
	
	public JFrame getJframe() {
		return jframe;
	}
	
	public JLabel getLabel() {
		return label;
	}
	
	public BusyAdvanceGlassPane getBusyAdvanceGlassPane() {
		return busyAdvanceGlassPane;
	}

	public void addToolbar(JToolBar toolbar) {
		jframe.getContentPane().add(toolbar, BorderLayout.WEST);
	}

	public void setMenu(JMenuBar menuBar) {
		jframe.setJMenuBar(menuBar);
	}

	public void pack() {
		jframe.pack();
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true);
	}
}

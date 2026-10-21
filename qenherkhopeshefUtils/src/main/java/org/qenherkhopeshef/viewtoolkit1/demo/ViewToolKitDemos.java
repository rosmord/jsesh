package org.qenherkhopeshef.viewtoolkit.demo;

import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.qenherkhopeshef.viewtoolkit.demo.picture.PictureDrawerDemo;
import org.qenherkhopeshef.viewtoolkit.demo.text.TextEditorDemo;
import org.qenherkhopeshef.viewtoolkit.demo.uml.UmlDiagramDemo;

/// Launcher for the three viewToolKit demos.
///
/// Each demo can also be started on its own (they all have a `main` method).
public final class ViewToolKitDemos {

	private ViewToolKitDemos() {
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(ViewToolKitDemos::showLauncher);
	}

	private static void showLauncher() {
		JFrame frame = new JFrame("viewToolKit demos");
		JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
		panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		panel.add(new JLabel("Three small applications built on org.qenherkhopeshef.viewtoolkit:"));
		panel.add(button("1. Text editor (embedded pictures, very long texts)", TextEditorDemo::open));
		panel.add(button("2. Picture drawer (open and closed lines)", PictureDrawerDemo::open));
		panel.add(button("3. UML class diagram displayer", UmlDiagramDemo::open));
		frame.getContentPane().add(panel);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private static JButton button(String label, Runnable action) {
		JButton b = new JButton(label);
		b.addActionListener(e -> action.run());
		return b;
	}
}

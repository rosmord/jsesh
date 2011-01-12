/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editorSoftware.actions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class DisplayJavaPropertiesAction extends AbstractAction {
	private Component frame;
	
	public DisplayJavaPropertiesAction(String name, Component frame) {
		super(name);
		this.frame= frame;
	}

	public void actionPerformed(ActionEvent e) {
		Properties props = System.getProperties();
		StringWriter string = new StringWriter();
		PrintWriter out = new PrintWriter(string);
		props.list(out);
		out.println("Available charsets:");
		for (Iterator iterator= Charset.availableCharsets().keySet().iterator();
			iterator.hasNext();) {
			out.println((String)iterator.next());
		}
		JTextArea textArea = new JTextArea(string.toString());
		JScrollPane pane = new JScrollPane(textArea);
		pane.setPreferredSize(new Dimension(320, 200));
		JOptionPane.showMessageDialog(frame, pane);
	}
}
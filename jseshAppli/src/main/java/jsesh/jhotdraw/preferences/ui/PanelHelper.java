package jsesh.jhotdraw.preferences.ui;

import java.awt.Component;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jsesh.jhotdraw.Messages;

public class PanelHelper {
	
	JPanel panel;
	
	

	public PanelHelper(JPanel panel) {
		super();
		this.panel = panel;
	}

	public  void addLabel(String labelCode) {
		panel.add(new JLabel(Messages.getString(labelCode)));
	}
	
	public void add(Component component, String constraints) {
		panel.add(component, constraints);
	}

	public void add(Component component) {
		panel.add(component);
	}
	
	public void addWithLabel(String labelCode, Component component, String constraints) {
		addLabel(labelCode);
		add(component, constraints);
	}
	
	public void addWithLabel(String labelCode, Component component) {
		addLabel(labelCode);
		add(component);
	}
}

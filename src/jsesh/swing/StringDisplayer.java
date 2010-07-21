/*
 * Created on 29 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.swing;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JTextField;


/**
 * Simple MVC oriented label.
 * @author S. Rosmorduc
 *
 */
public class StringDisplayer extends JTextField implements Observer {
	
	public StringDisplayer(StringModel model) {
		super(model.getText());
		init(model);
	}
	
	public StringDisplayer(StringModel model, int size) {
		super(model.getText(),size);
		init(model);
	}
	
	private void init(StringModel model) {
		setEnabled(false);
		setFocusable(false);
		model.addObserver(this);
		setDisabledTextColor(getForeground());
	}


	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		if (o instanceof StringModel) 
			setText(((StringModel)o).getText());
	}
}

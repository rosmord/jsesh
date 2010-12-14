/*
 * Created on 29 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.swing;

import java.util.Observable;


/**
 * @author S. Rosmorduc
 *
 */
public class SimpleStringModel extends Observable implements StringModel{
	private String text;
	
	/**
	 * @param text
	 */
	public SimpleStringModel(String text) {
		super();
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		if (! this.text.equals(text))
		{
			this.text = text;
			setChanged();
		}
		notifyObservers();
	}
}

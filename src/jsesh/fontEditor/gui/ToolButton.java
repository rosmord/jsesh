/*
 * Created on 1 nov. 2003
 *
 * (C) S. Rosmorduc
 * This file is free software under the LGLP
 * 
 */
package jsesh.fontEditor.gui;


import javax.swing.JToggleButton;


/**
 * This button is used to set a current tool.
 * @author rosmord
 */
public class ToolButton extends JToggleButton {
	Tool tool;
	
	/**
	 * Create a button controling a given tool.
	 * @param text
	 * @param outil
	 */
	public ToolButton(String text, Tool outil) {
		super(text);
		this.tool= outil;
	}

	

	/**
	 * @return the associated tool.
	 */
	public Tool getTool() {
		return tool;
	}

}

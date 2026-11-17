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
	 * 
	 */
	public ToolButton(String text, Tool outil) {
		super(text);
		this.tool= outil;
	}

	

	/**
	 * @return
	 */
	public Tool getTool() {
		return tool;
	}

}

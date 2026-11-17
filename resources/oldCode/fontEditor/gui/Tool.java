/*
 * Created on 1 nov. 2003
 *
 * (C) S. Rosmorduc
 * This file is free software under the LGLP
 * 
 */
package jsesh.fontEditor.gui;


import javax.swing.event.MouseInputAdapter;


/**
 * @author rosmord
 */
public abstract class Tool extends MouseInputAdapter {

	private ToolContext contexte;

	public Tool() {
		setContexte(null);
	}


	/**
	 * Fixe la vue sur laquelle l'tool fonctionne.
	 * @param contexte
	 * 
	 */
	public void setContexte(ToolContext contexte) {
		this.contexte= contexte;
		initialiser();
	}

	/**
	 * Méthode appelée quand un tool est sélectionné,
	 * ou quand il change de vue.
	 * 
	 */

	protected abstract void initialiser();

	
	/**
	 * @return current context.
	 * 
	 */
	public ToolContext getContexte() {
		return contexte;
	}

}

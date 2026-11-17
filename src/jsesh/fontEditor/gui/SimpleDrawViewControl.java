/*
 * Created on 1 nov. 2003
 *
 * (C) S. Rosmorduc
 * This file is free software under the LGLP
 * 
 */
package jsesh.fontEditor.gui;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;


/**
 * @author rosmord
 */

public class SimpleDrawViewControl extends MouseInputAdapter {

	/**
	 * L'tool courant. 
	 * <p> Le contrôle délèguera le traitement de l'événement à cet tool.
	 */

	private Tool outil;

	/**
	 * 
	 * Le contexte est un espace de travail sur lequel l'tool peut agir.
	 * 
	 */

	private ToolContext contexte;

	public SimpleDrawViewControl(ToolContext contexte) {
		// Noter que quand on fixe la vue, il faut 
		// s'enregistrer comme son listener.
		// Si on écrit une méthode setVue, alors
		// il faut l'utiliser ici au lieu de dupliquer du code.
		setContexte(contexte);
	}

	

	public void setContexte(ToolContext contexte) {
		if (this.contexte != null) {
			this.contexte.deconnecte(this);
		}
		contexte.connecte(this);
		this.contexte= contexte;
	}
	/**
	 * Fixe le tool courant.
	 * @param outil the new tool
	 *
	 */
	public void setOutil(Tool outil) {
		this.outil= outil;
		// On doit dire à l'tool sur quelle vue il travaille.
		outil.setContexte(contexte);
	}

	/**
	 * @return current tool.
	 *
	 */
	public Tool getOutil() {
		return outil;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		if (outil != null) {
			outil.mouseClicked(e);
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		if (outil != null)
			outil.mouseDragged(e);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		if (outil != null)
			outil.mouseEntered(e);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		if (outil != null)
			outil.mouseExited(e);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		if (outil != null)
			outil.mouseMoved(e);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		if (outil != null)
			outil.mousePressed(e);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		if (outil != null)
			outil.mouseReleased(e);
	}

}

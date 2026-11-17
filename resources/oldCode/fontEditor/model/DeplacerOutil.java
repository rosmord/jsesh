/*
 * Created on 1 nov. 2003
 *
 * (C) S. Rosmorduc
 * This file is free software under the LGLP
 * 
 */
package jsesh.fontEditor.model;

import java.awt.event.MouseEvent;

import jsesh.fontEditor.control.Command;
import jsesh.fontEditor.control.DeplacerCommande;
import jsesh.fontEditor.gui.Tool;

/**
 * @author rosmord
 */
public class DeplacerOutil extends Tool {

	/**
	 * anciennes coordonnées de la souris.
	 */ 
	private Point oldPos;

	/**
	 * forme manipulée.
	 */
	private Forme forme;

	/**
	 * Tolérance (en pixels).
	 * Il serait sans doute intéressant de déplacer cette variable ailleurs.
	 * Il n'y a pas de raison pour que plusieurs outils 
	 * différents aient des tolérances différentes.
	 * 
	 * <p> On pourrait avoir une classe Preferences, qui permettrait de stocker ce genre de données,
	 * plutôt que d'en faire des constantes ou des variables statiques.
	 */
	public int tolerance= 100;

	/* (non-Javadoc)
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
	 */
	public void mouseDragged(MouseEvent e) {
		if (forme != null) {
			// Quand la souris est déplacée avec un bouton enfoncé :
			Point p= getContexte().getPointFromEvent(e);
			// Calculer le déplacement de la souris
			double dx= p.getX() - oldPos.getX();
			double dy= p.getY() - oldPos.getY();
			// Se rappeler la position actuelle de la souris pour le prochain
			// déplacement.
			oldPos= p;
			Command deplacer= new DeplacerCommande(forme,dx,dy);
			// Ajouter le déplacement à la liste des commandes.
			// Déplacer la forme sélectionnée du même déplacement que la souris
			getContexte().add(deplacer);
		}
	}

	/*
	 * Première étape dans un déplacement : trouver la forme.
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		// Récupérer la position de la souris.
		oldPos= getContexte().getPointFromEvent(e);
		// Calcul de la tolérance : on veut 2 pixels de tolerance à l'écran.
		double tol= getContexte().getView().screenDistanceToModel(tolerance);
		forme= getContexte().getDessin().trouverFormeQuiContient(oldPos, tol);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		initialiser();
	}

	/* (non-Javadoc)
	 * @see simpledraw.sol3.Tool#reset()
	 */
	public void initialiser() {
		forme= null;
		oldPos= null;
	}
}

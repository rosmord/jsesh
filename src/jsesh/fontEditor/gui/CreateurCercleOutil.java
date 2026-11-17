/*
 * Created on 1 nov. 2003
 *
 * (C) S. Rosmorduc
 * This file is free software under the LGLP
 * 
 */
package jsesh.fontEditor.gui;

import java.awt.event.MouseEvent;

import jsesh.fontEditor.control.Command;
import jsesh.fontEditor.control.CreerCercleCommande;
import jsesh.fontEditor.model.Cercle;
import jsesh.fontEditor.model.Point;

/**
 * Tool gérant la création de cercles. 
 * Exercice : 
 * 	écrire un tool pour gérer la création de Segment.
 * <p> On remarquera que les deux outils sont pratiquement identiques.
 * Il serait donc intéressant d'écrire un tool plus générique,
 * permettant la sélection de <em>n</em> points, puis créant quelque chose 
 * à partir de ces <em>n</em> points. 
 * @author rosmord
 */
public class CreateurCercleOutil extends Tool {

	/**
		* Points utilisés lors de la création de nouvelles formes.
		* Quand ils n'ont pas été sélectionnés, ils sont à null.
		*/
	private Point p1, p2;


	/**
	 * La création de cercle est géré par deux clicks de souris.
	 */
	public void mouseClicked(MouseEvent e) {
		if (p1 == null) {
			// On récupère le premier point.
			p1= getContexte().getPointFromEvent(e);
		} else if (p2 == null) {
			// On récupère le second point, et on crée :
			p2= getContexte().getPointFromEvent(e);
			double rayon=  Math.sqrt(p1.distanceCarre(p2));
			if (rayon != 0.0) {
				// Construire un cercle
				Cercle aCreer=  new Cercle(p1,rayon);
		
				// Créer la commande idoine
				Command creation= new CreerCercleCommande(getContexte().getDessin(), aCreer);
				// la passer au gestionnaire :
				getContexte().add(creation);
				// On remet le contrôle à 0 :
				initialiser();
			}	
		}
	}


	/* (non-Javadoc)
	 * @see simpledraw.sol3.Tool#initialiser()
	 */
	public void initialiser() {
		p1= null;
		p2= null;
	}

}

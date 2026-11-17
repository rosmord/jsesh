/*
 * Created on 9 sept. 2003
 *
 * (C) S. Rosmorduc
 * This file is free software under the LGLP
 * 
 */
package jsesh.fontEditor.model;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Observable;
import java.util.Observer;



/**
 * @author rosmord
 */
public abstract class Forme extends Observable implements Observer {
	
	Color couleur;
	
	public Forme() {
		couleur= Color.BLACK;
	}
	
	/**
	 * 
	 * @param g
	 */
	abstract public void dessiner(Graphics2D g);
	
	/**
	 * teste si la forme contient le point p.
	 * @param p
	 * @param tolerance : le <em>carré</em> de la distance (en coordonnées dessin) utilisée comme marge d'erreur
	 * 	      pour certaines formes comme les segments.
	 * @return
	 */
	
	abstract public boolean contient(Point p, double tolerance);
	
	/**
	 * @return
	 */
	public Color getCouleur() {
		return couleur;
	}

	/**
	 * @param color
	 */
	public void setCouleur(Color color) {
		if (color != couleur)
			setChanged();
		couleur= color;
		notifyObservers();		
	}
	
	
	/* 
	 * Quand un élément de la forme (en particulier un point)
	 * est modifié, cette procédure prévient les observateurs de la forme.
	 */

	public void update(Observable o, Object arg) {
		setChanged();
		notifyObservers();
	}

	/**
	 * Déplace une forme de dx,dy
	 * @param dx
	 * @param dy
	 */
	abstract  public void deplacer(double dx, double dy);
}

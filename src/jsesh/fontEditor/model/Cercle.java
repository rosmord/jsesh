/*
 * Created on 9 sept. 2003
 *
 * (C) S. Rosmorduc
 * This file is free software under the LGLP
 * 
 */
package jsesh.fontEditor.model;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;


/**
 * @author rosmord
 */
public class Cercle extends Forme {
	
	private Point centre;
	private double r;
	
	public Cercle(Point centre, double rayon) {
		r= rayon;
		this.centre= centre;
	}
	
	/* (non-Javadoc)
	 * @see simpledraw.part1.Forme#contient(int, int)
	 */
	public boolean contient(Point p, double tolerance) {
		return centre.distanceCarre(p) <= r*r;
	}

	/* (non-Javadoc)
	 * @see simpledraw.part1.Forme#dessiner(java.awt.Graphics2D)
	 */
	public void dessiner(Graphics2D g) {
		g.setColor(getCouleur());
		// Nous utilisons une "Shape" au lieu 
		// d'appeler drawOval, ce qui facilite la vie
		// (on peut alors avoir des coordonnées réelles).
		Shape s= new Ellipse2D.Double(
			centre.getX() - getRayon(),
			centre.getY() - getRayon(),
			2* getRayon(),
			2* getRayon()
		);
		g.draw(s);
	}
	/**
	 * @return le centre de ce cercle
	 */
	public Point getCentre() {
		return centre;
	}

	/**
	 * @return le rayon de ce cercle
	 */
	public double getRayon() {
		return r;
	}

	/**
	 * @param rayon fixe le rayon du cercle
	 */
	public void setRayon(double rayon) {
		r= rayon;
		setChanged();
		notifyObservers();
	}

	/* (non-Javadoc)
	 * @see simpledraw.sol2.Forme#deplacer(double, double)
	 */
	public void deplacer(double dx, double dy) {
		centre.deplacer(dx,dy);
		setChanged();
		notifyObservers();
	}

}

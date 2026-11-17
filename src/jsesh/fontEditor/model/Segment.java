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
import java.awt.geom.Line2D;


/**
 * @author rosmord
 */
public class Segment extends Forme {
	private Point a, b;

	/**
	 * 
	 */
	public Segment(Point a, Point b) {
		this.a= a;
		this.b= b;
	}

	/**
	 * 
	 */
	public boolean contient(Point p, double tolerance) {
		double distance;
		
		// On teste d'abord si p se projette entre a et b :
		// c.-à-d. que ab.ap > 0 et ba.bp > 0 :
		if (Point.scalaire(a, b, a, p) > 0.0
			&& Point.scalaire(b, a, b, p) > 0.0) {
				// Dans ce cas, la distance de p au segment
				// est la distance de p à la droite (ab)
			distance= distanceADroiteAuCarre(p);
		} else {
			// Sinon, c'est le min de ap et bp.
			distance = a.distanceCarre(p);
			double dpb= b.distanceCarre(p);
			if (dpb < distance)
				distance= dpb;
		}
		return distance < tolerance;
	}

	/**
	 * Calcule le carré de la distance entre le point p et la droite (ab).
	 * 
	 * @param p
	 * @return
	 */
	public  double distanceADroiteAuCarre(Point p) {
		double distance;
		double prodvect=
			(p.getX() - a.getX()) * (b.getY() - a.getY())
				- (p.getY() - a.getY()) * (b.getX() - a.getX());
		double normeAB2= a.distanceCarre(b);
		distance= (prodvect * prodvect) / normeAB2;
		return distance;
	}

	/* (non-Javadoc)
	 * @see sol.Forme#dessiner(java.awt.Graphics2D)
	 */
	public void dessiner(Graphics2D g) {
		Shape s= new Line2D.Double(a.getX(), a.getY(),
			b.getX(), b.getY());
		g.draw(s);
	}

	/**
	 * @return
	 */
	public Point getA() {
		return a;
	}

	/**
	 * @return
	 */
	public Point getB() {
		return b;
	}

	/* (non-Javadoc)
	 * @see simpledraw.sol2.Forme#deplacer(double, double)
	 */
	public void deplacer(double dx, double dy) {
		a.deplacer(dx,dy);
		b.deplacer(dx,dy);
		setChanged();
		notifyObservers();
	}

}

/*
 * Created on 9 sept. 2003
 *
 * (C) S. Rosmorduc
 * This file is free software under the LGLP
 * 
 */
package jsesh.fontEditor.model;

/**
 * @author rosmord
 */

public class Point {
	private double x,y;
	
	public Point() {
		x=0; y=0;
	}
	
	public Point(double x, double y) {
		this.x= x;
		this.y=y;
	}
	
	
	/**
	 * @return x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @return y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param i
	 */
	public void setX(double i) {	
		x= i;
	}

	/**
	 * @param i
	 */
	public void setY(double i) {
		y= i;
	}

	public double distanceCarre(Point autre) {
		return (x-autre.x)*(x-autre.x) +
			(y-autre.y)*(y-autre.y);
	}
	
	/**
	 * calcule le produit scalaire ab.cd
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @return the scalar produc ab.cd.
	 */
	public static double scalaire(Point a, Point b, Point c, Point d) {
		return (b.x-a.x)*(d.x-c.x) + (b.y-a.y)*(d.y-c.y);		
	}
	
	public static double distanceADroite(Point p, Point a, Point b) {
			double distance;
			double prodvect=
				(p.getX() - a.getX()) * (b.getY() - a.getY())
					- (p.getY() - a.getY()) * (b.getX() - a.getX());
			double normeAB2= a.distanceCarre(b);
			distance= (prodvect * prodvect) / normeAB2;
			return distance;
		}

	/**
	 * Fixe les coordonnées d'un point.
	 * @param x
	 * @param y
	 */
	public void setCoords(double x, double y) {
		this.x= x;
		this.y= y;
	}

	/**
	 * @param dx
	 * @param dy
	 */
	public void deplacer(double dx, double dy) {
		x= x+dx;
		y= y+dy;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "("+ x + ", " + y + ")";
	}


}

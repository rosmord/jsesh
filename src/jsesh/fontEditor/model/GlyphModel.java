/*
 * Created on 9 sept. 2003
 *
 * (C) S. Rosmorduc
 * This file is free software under the LGLP
 * 
 */
package jsesh.fontEditor.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;


/**
 * The model for a glyph.
 * <p>The model is not only the glyph's drawing. It also contains 
 * a number of guidelines and the like. 
 * @author rosmord
 */
public class GlyphModel extends Observable implements Observer {

	List liste;

	public GlyphModel() {
		liste= new ArrayList();
	}

	public void add(Forme f) {
		setChanged(); // Le dessin est modifié par l'ajout d'une forme
		f.addObserver(this); // on demande à être prévenu quand la forme change
		liste.add(f);
		notifyObservers();
	}

	public void remove(Forme f) {
		setChanged();
		f.deleteObserver(this);
		liste.remove(f);
		notifyObservers();
	}

	public int size() {
		return liste.size();
	}

	public Forme getForme(int i) {
		return (Forme) liste.get(i);
	}

	/**
	 * méthode appelée automatiquement par les éléments du dessin
	 * quand ils changent. 
	 * @param o
	 * @param arg
	 */
	public void update(Observable o, Object arg) {
		setChanged();
		notifyObservers();
	}

	/**
	 * Find the form which was clicked.
	 * @param p point clicked.
	 * @param tolerance
	 * @return the form closer to the point.
	 */
	public Forme trouverFormeQuiContient(Point p, double tolerance) {
		Forme forme;
		forme= null;
		for (int i= 0; i < size(); i++) {
			if (getForme(i).contient(p, tolerance))
				forme= getForme(i);
		}
		return forme;
	}
}

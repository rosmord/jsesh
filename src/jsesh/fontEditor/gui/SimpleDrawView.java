/*
 * Created on 9 sept. 2003
 *
 * (C) S. Rosmorduc
 * This file is free software under the LGLP
 * 
 */
package jsesh.fontEditor.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import jsesh.fontEditor.model.GlyphModel;
import jsesh.fontEditor.model.Point;

/**
 * @author rosmord
 */
public class SimpleDrawView extends JPanel 
	implements Observer {

	private double scale;
	
	private GlyphModel glyphModel;

	public SimpleDrawView(GlyphModel dessin) {
		this.glyphModel= dessin;
		scale= 1.0;
		dessin.addObserver(this);
		setBackground(Color.WHITE);
		// On fixe la dimension souhaitable du composant.
		// Cela permet ensuite d'utiliser la méthode pack()
		setPreferredSize(new Dimension(320,200));	 
			initialize();
	}
	
	
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setBackground(new java.awt.Color(226,37,37));
        this.setSize(424, 97);
			
	}
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2= (Graphics2D)g;
		g2.scale(scale,scale);
		for (int i= 0; i< glyphModel.size(); i++)
			{
				glyphModel.getForme(i).dessiner(g2);
			}
	}


	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		// When a part of the model is modified, the drawing is redrawn.
		repaint();
	}
	
	/**
	 * @return 
	 */
	public GlyphModel getGlyphModel() {
		return glyphModel;
	}

	/**
	 * @param glyphModel 
	 */
	public void setGlyphModel(GlyphModel dessin) {
		this.glyphModel = dessin;
	}

	public Point screenToModel(int x, int y) {
		return new Point(x/scale, y/scale);
	}
	
	public double screenDistanceToModel(int d) {
		return d/scale;
	}

	/**
	 * @return
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * @param d
	 */
	public void setScale(double d) {
		scale= d;
		repaint();
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"

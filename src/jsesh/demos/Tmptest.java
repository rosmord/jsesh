/*
 * Created on 16 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.demos;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.GeneralPath;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * @author S. Rosmorduc
 *
 */
public class Tmptest extends JFrame {

	/**
	 * 
	 */
	public Tmptest() {
		super("fonts fonts fonts");
		setSize(640,480);
		getContentPane().add(new SimpleCanvas());
	}
	
	public static void main(String[] args) {
		Tmptest t= new Tmptest();
		t.setVisible(true);
	}
}

class SimpleCanvas extends JPanel {
	
	/**
	 * 
	 */
	public SimpleCanvas() {
		setBackground(Color.WHITE);
		setForeground(Color.BLACK);
	}
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d;
		g2d= (Graphics2D) g;
		//g2d.scale(2.5,2.5);
		GraphicsEnvironment env= GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font f[]= env.getAllFonts();
		for (int i= 0; i< f.length; i++)
			System.out.println(f[i]);
		Font font= new Font("Serif", Font.BOLD, 40);
		GlyphVector vect= font.createGlyphVector(g2d.getFontRenderContext(),"hello");
		//g2d.translate(100,100);
		BasicStroke stroke= new BasicStroke(40);;
		GeneralPath s= new GeneralPath();
		s.moveTo(100,100);
		s.quadTo(200,400, 500,390);
		s.quadTo(200,100,100,100);
		s.closePath();
		g2d.setColor(Color.BLUE);
		g2d.draw(s);
		g2d.setStroke(new BasicStroke(3));
		g2d.setColor(new Color(255,0,0,150));
		Shape s1= stroke.createStrokedShape(s);
		g2d.draw(s1);
		
	}
}
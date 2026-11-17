package jsesh.externalFonts.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import jsesh.bzr.simple.ShapeChar;
import jsesh.utils.GraphicsUtils;

/**
 * A widget able to display a ShapeChar.
 * @author rosmord
 */
public class ShapeDisplayer extends JComponent {
	private ShapeChar shape;

	/**
	 * The height, in model coordinates, of the displayer window.
	 */
	private double displayHeight;

	
	public ShapeDisplayer() {
		shape= null;
		displayHeight= 24;
		setPreferredSize(new Dimension(100,100));
		setBackground(Color.WHITE);		
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);				
		g.clearRect(0, 0, getWidth(), getHeight());
		Graphics2D g2d= (Graphics2D) g;
		GraphicsUtils.antialias(g2d);
		double s= getHeight()/ displayHeight ;
		g2d.scale(s, s);
		g2d.setColor(Color.BLACK);		
		if (shape != null) {
			g2d.translate(0, displayHeight - shape.getBbox().getHeight());
			shape.draw(g2d, 0, 0, 1, 1, 0);
			//g2d.draw(shape.getShape());
			g2d.setStroke(new BasicStroke(0.25f));
			g2d.setColor(Color.RED);
			g2d.draw(shape.getBbox());
			//g2d.fill(shape.getShape());
		}
	}
	
	/**
	 * Sets the shape of the sign which will be displayed
	 * @param shape : a character shape.
	 */
	public void setShape(ShapeChar shape) {
		this.shape = shape;
		repaint();
	}
	
	/**
	 * Sets the maximum scale which will be displayed
	 * @param displayHeight
	 */
	public void setDisplayHeight(double displayHeight) {
		this.displayHeight = displayHeight;
		repaint();
	}
	
	public ShapeChar getShape() {
		return shape;
	}
	
	public double getDisplayHeight() {
		return displayHeight;
	}

	public boolean inResizeArea(int x, int y) {
		// TODO Auto-generated method stub
		return false;
	}

	public double convertToModelY(int y) {
		double scale= displayHeight/getHeight();
		return displayHeight- (y*scale);
	}
}

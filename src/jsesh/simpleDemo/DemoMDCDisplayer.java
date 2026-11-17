package jsesh.simpleDemo;

/**
 * DemoMDCDisplayer.java
 * 
 *
 * Simple hieroglyphic text displayer
 * 
 * Created: Mon Jun 10 13:08:21 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 * 
 */
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.HieroglyphicFontManager;
import jsesh.swingUtils.GraphicsUtils;


public class DemoMDCDisplayer extends JPanel {
    private HieroglyphicText text;
    private HieroglyphicDrawer drawer;
    private double scale= 2.0;
    
    public DemoMDCDisplayer (){
	setSize(getPreferredSize());
	setBackground(Color.white);

	HieroglyphicFontManager manager= DefaultHieroglyphicFontManager.getInstance();
	drawer= new HieroglyphicDrawer(manager);
	setDoubleBuffered(true);
    }
    
    public Dimension getPreferredSize() {
	if (text == null)
	    return new Dimension(6000, 600);
	else {
	    // Evaluate text dimensions :
	    int textHeight= (int)(drawer.getTextHeight(text) * scale);
	    return new Dimension(6000, textHeight);
	}
		 
    }
    
    /**
     * Get the value of text.
     * @return value of text.
     */
    public HieroglyphicText getText() {
	return text;
    }
    
    /**
     * Set the value of text.
     * @param v  Value to assign to text.
     */
    public void setText(HieroglyphicText  v) {
	this.text = v;
	this.revalidate();
	repaint();
    }

    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	Graphics2D g2d = (Graphics2D) g;
	GraphicsUtils.antialias(g);
	g2d.scale(scale, scale);
	drawer.draw(g2d, text);
    }
}// DemoMDCDisplayer




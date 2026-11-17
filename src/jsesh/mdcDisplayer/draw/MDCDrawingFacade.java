/*
 * Created on 12 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.StringReader;

import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.layout.MDCEditorKit;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;
import jsesh.swingUtils.GraphicsUtils;

/**
 * A simple class for programmers who want to draw hieroglyphs.
 * 
 * TODO : reuse this class in all image creating soft.
 * 
 * @author S. Rosmorduc
 * 
 */
public class MDCDrawingFacade {

	private boolean philologySign = true;

	private DrawingSpecifications drawingSpecifications = null;

	private int maxWidth= 2000;
	private int maxHeight= 2000;

	private int cadratHeight= 20;

	/**
	 * Generate a picture for the manuel de codage text passed as argument.
	 * 
	 * @param mdcCodes :
	 *            a description, in manuel de codage, of the text.
	 * @return an image of the text.
	 * @throws MDCSyntaxError
	 */

	public BufferedImage createImage(String mdcCodes) throws MDCSyntaxError {
		MDCParserModelGenerator gen = new MDCParserModelGenerator();
		gen.setPhilologyAsSigns(isPhilologySign());
		TopItemList t = gen.parse(new StringReader(mdcCodes));
		return createImage(t);
	}

	/**
	 * Generate a picture for a TopItemList passed as argument.
	 * 
	 * @param t
	 * @return a new bufferedImage.
	 */
	public BufferedImage createImage(TopItemList t) {
		drawingSpecifications = getDrawingSpecifications();

		ViewBuilder viewBuilder = new SimpleViewBuilder();
		MDCView v = viewBuilder.buildView(t, drawingSpecifications);
		BufferedImage result;
		double scale = getScale();

		int width = (int) Math.ceil(v.getWidth() * scale) + 2;
		int height = (int) Math.ceil(v.getHeight() * scale) + 2;

		if (width > maxWidth)
			width = maxWidth;
		if (height > maxHeight)
			height = maxHeight;

		result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) result.getGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, width, height);		
		g.scale(scale, scale);
		GraphicsUtils.antialias(g);
		g.setColor(Color.BLACK);
		ViewDrawer drawer = new ViewDrawer();
		drawer.draw(g, v, drawingSpecifications);
		g.dispose();
		return result;

	}

	/**
	 * Draws the data on an existing graphic.
	 * 
	 * @param mdcText
	 * @param g0
	 * @param x
	 * @param y
	 * @return the bounding box of the drawn text.
	 * @throws MDCSyntaxError 
	 */
	public Rectangle2D draw(String mdcText, Graphics2D g, double x, double y) throws MDCSyntaxError {
		MDCParserModelGenerator gen = new MDCParserModelGenerator();
		gen.setPhilologyAsSigns(isPhilologySign());
		TopItemList t = gen.parse(new StringReader(mdcText));
		return draw(t,g,x,y);
	}
	
	
	/**
	 * Draws the data on an existing graphic.
	 *
	 * @param t
	 * @param g
	 * @param x
	 * @param y
	 * @return the bounding box of the drawn text.
	 */
	
	public Rectangle2D draw(TopItemList t, Graphics2D g, double x, double y) {
		
		Graphics2D g1= (Graphics2D) g.create();
		drawingSpecifications = getDrawingSpecifications();

		ViewBuilder viewBuilder = new SimpleViewBuilder();
		MDCView v = viewBuilder.buildView(t, drawingSpecifications);
		double scale = getScale();

		int width = (int) Math.ceil(v.getWidth() * scale) + 2;
		int height = (int) Math.ceil(v.getHeight() * scale) + 2;

		if (width > maxWidth)
			width = maxWidth;
		if (height > maxHeight)
			height = maxHeight;

		g1.setBackground(Color.WHITE);
		g1.translate(x, y);
		g1.scale(scale, scale);
		g1.setColor(Color.BLACK);
		ViewDrawer drawer = new ViewDrawer();
		drawer.draw(g1, v, drawingSpecifications);
		g1.dispose();
		return new Rectangle2D.Double(x,y,width,height);
	}
	
	
	public boolean isPhilologySign() {
		return philologySign;
	}

	private double getScale() {
		return cadratHeight / drawingSpecifications.getBaseLength();
	}

	/**
	 * sets the way philological parenthesis will be considered when read. If
	 * true, they will be considered as mere signs. If false, they will be
	 * considered as parenthesis (and thus will require matching).
	 * 
	 * @param philologySign
	 */

	public void setPhilologySign(boolean philologySign) {
		this.philologySign = philologySign;
	}

	public DrawingSpecifications getDrawingSpecifications() {
		if (drawingSpecifications == null)
			return MDCEditorKit.getBasicMDCEditorKit()
					.getDrawingSpecifications();
		else
			return drawingSpecifications;
	}

	public void setDrawingSpecifications(
			DrawingSpecifications drawingSpecifications) {
		this.drawingSpecifications = drawingSpecifications;
	}

	/**
	 * @param width
	 * @param height
	 */
	public void setMaxSize(int width, int height) {
		this.maxWidth = width;
		this.maxHeight = height;
	}

	/**
	 * Set the approximative cadrat height, in pixels.
	 * <p>
	 * Default value is 20.
	 * 
	 * @param cadratHeight
	 */
	public void setCadratHeight(int cadratHeight) {
		//if (drawingSpecifications != null) {
			this.cadratHeight = cadratHeight;
		//}
	}
}
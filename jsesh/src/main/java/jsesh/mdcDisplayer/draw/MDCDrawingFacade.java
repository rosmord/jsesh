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
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;

import org.qenherkhopeshef.graphics.utils.GraphicsUtils;

/**
 * A simple class for programmers who want to draw hieroglyphs.
 * TODO:  make it possible to avoid computing a drawing twice in all cases
 * 		a) create code for generating SVG
 * 		b) create code taking a graphic2D factory as argument.  
 * TODO : reuse this class in all image creating soft.
 * TODO : move it in a more logical (?) place.
 * 
 * @author S. Rosmorduc
 * 
 */
public class MDCDrawingFacade {

	private boolean philologySign = true;

	private DrawingSpecification drawingSpecifications = new DrawingSpecificationsImplementation();

	private int maxWidth = 2000;
	private int maxHeight = 2000;

	private int cadratHeight = 20;

	/**
	 * Generate a picture for the manuel de codage text passed as argument.
	 * 
	 * @param mdcCodes
	 *            : a description, in manuel de codage, of the text.
	 * @return an image of the text.
	 * @throws MDCSyntaxError
	 */

	public BufferedImage createImage(String mdcCodes) throws MDCSyntaxError {
		TopItemList t = buidTopItemList(mdcCodes);
		return createImage(t);
	}

	private TopItemList buidTopItemList(String mdcCodes) throws MDCSyntaxError {
		MDCParserModelGenerator gen = new MDCParserModelGenerator();
		gen.setPhilologyAsSigns(isPhilologySign());
		TopItemList t = gen.parse(new StringReader(mdcCodes));
		return t;
	}

	/**
	 * Generate a picture for a TopItemList passed as argument.
	 * 
	 * @param t
	 * @return a new bufferedImage.
	 */
	public BufferedImage createImage(TopItemList t) {
		ViewAndBounds viewAndBounds= new ViewAndBounds(t,0,0);
		
		BufferedImage result;

		int width= (int) viewAndBounds.bounds.getWidth();
		int height= (int) viewAndBounds.bounds.getHeight();
		
		if (width > maxWidth)
			width = maxWidth;
		if (height > maxHeight)
			height = maxHeight;

		result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) result.getGraphics();
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, width, height);
		GraphicsUtils.antialias(g);
		viewAndBounds.draw(g);
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
	public Rectangle2D draw(String mdcText, Graphics2D g, double x, double y)
			throws MDCSyntaxError {
		TopItemList t = buidTopItemList(mdcText);
		return draw(t, g, x, y);
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

		Graphics2D g1 = (Graphics2D) g.create();
		ViewAndBounds viewAndBounds = new ViewAndBounds(t, x, y);
		viewAndBounds.draw(g1);
		g1.dispose();
		return viewAndBounds.bounds;
	}

	/**
	 * Computes the bounds of a particular text without drawing it.
	 * 
	 * @param t
	 * @param x
	 * @param y
	 * @return
	 */
	public Rectangle2D getBounds(TopItemList t, double x, double y) {
		ViewAndBounds viewAndBounds = new ViewAndBounds(t, x, y);
		return viewAndBounds.bounds;
	}
	
	/**
	 * Computes the bounds of a particular text without drawing it.
	 * 
	 * @param mdc : the text, in Manuel de Codage format.
	 * @param x
	 * @param y
	 * @return
	 * @throws MDCSyntaxError 
	 */
	public Rectangle2D getBounds(String mdc, double x, double y) throws MDCSyntaxError {
		ViewAndBounds viewAndBounds = new ViewAndBounds(buidTopItemList(mdc), x, y);
		return viewAndBounds.bounds;
	}

	public boolean isPhilologySign() {
		return philologySign;
	}

	private double getScale() {
		return cadratHeight
				/ drawingSpecifications.getHieroglyphsDrawer().getHeightOfA1();
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

	public DrawingSpecification getDrawingSpecifications() {
		if (drawingSpecifications == null)
			return MDCEditorKit.getBasicMDCEditorKit()
					.getDrawingSpecifications();
		else
			return drawingSpecifications;
	}

	public void setDrawingSpecifications(
			DrawingSpecification drawingSpecifications) {
		this.drawingSpecifications = drawingSpecifications;
	}

	/**
	 * Set maximal picture size (only for bitmap pictures).
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
		this.cadratHeight = cadratHeight;
	}

	private class ViewAndBounds {
		public MDCView view;
		public Rectangle2D bounds;

		public ViewAndBounds(TopItemList t, double x, double y) {
			ViewBuilder viewBuilder = new SimpleViewBuilder();
			view = viewBuilder.buildView(t, drawingSpecifications);
			double scale = getScale();

			int width = (int) Math.ceil(view.getWidth() * scale) + 2;
			int height = (int) Math.ceil(view.getHeight() * scale) + 2;

			bounds = new Rectangle2D.Double(x, y, width, height);
		}

		public void draw(Graphics2D g) {
			double scale = getScale();

			g.setBackground(Color.WHITE);

			g.translate(bounds.getMinX(), bounds.getMinY());
			g.scale(scale, scale);
			g.setColor(Color.BLACK);
			ViewDrawer drawer = new ViewDrawer();
			drawer.draw(g, view, drawingSpecifications);
		}
	}

}
/*
 * Created on 12 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.draw;

import java.io.StringReader;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.utils.MDCSyntaxError;
import jsesh.mdcDisplayer.layout.MDCEditorKit;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.layout.ViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
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

	int maxWidth, maxHeight;

	private int cadratHeight;

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
		maxWidth = 2000;
		maxHeight = 2000;
		cadratHeight = 20;
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
		if (drawingSpecifications != null) {
			this.cadratHeight = cadratHeight;
		}
	}
}
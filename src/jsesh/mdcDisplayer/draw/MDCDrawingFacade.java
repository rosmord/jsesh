/*
 * Created on 12 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.StringReader;

import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.utils.MDCSyntaxError;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;
import jsesh.utils.GraphicsUtils;

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

	double scale = 1.0;

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
	private BufferedImage createImage(TopItemList t) {
		drawingSpecifications = getDrawingSpecifications();

		ViewBuilder viewBuilder = new SimpleViewBuilder(drawingSpecifications);
		MDCView v = viewBuilder.buildView(t);
		BufferedImage result;
		result = new BufferedImage((int) Math.ceil(v.getWidth() * scale) + 2,
				(int) Math.ceil(v.getHeight() * scale) + 2,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) result.getGraphics();
		g.scale(scale, scale);
		GraphicsUtils.antialias(g);
		g.setColor(Color.BLACK);
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, result.getWidth(), result.getHeight());
		ViewDrawer drawer = new ViewDrawer();
		drawer.draw(g, v, drawingSpecifications);
		g.dispose();
		return result;

	}

	public boolean isPhilologySign() {
		return philologySign;
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

	public double getScale() {
		return scale;
	}

	public void setScale(double scale) {
		this.scale = scale;
	}
}
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
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.imageio.ImageIO;

import jsesh.drawingspecifications.JSeshStyle;
import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.context.JSeshRenderContext;
import jsesh.mdcDisplayer.context.JSeshTechRenderContext;
import jsesh.mdcDisplayer.drawingElements.HieroglyphicDrawerDispatcher;
import jsesh.mdcDisplayer.drawingElements.HieroglyphsDrawer;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;
import jsesh.swing.utils.GraphicsUtils;

/**
 * A simple class for programmers who want to draw hieroglyphs.
 * TODO: make it possible to avoid computing a drawing twice in all cases
 * a) create code for generating SVG
 * b) create code taking a graphic2D factory as argument.
 * TODO : reuse this class in all image creating soft.
 * TODO : move it in a more logical (?) place.
 * 
 * @author S. Rosmorduc
 * 
 */
public class MDCDrawingFacade {

	private boolean philologySign = true;

	private final HieroglyphsDrawer hieroglyphsDrawer;

	private JSeshStyle jseshStyle = JSeshStyle.DEFAULT;

	/**
	 * How many pixels on the device to make a typographical point?
	 */
	private double deviceScale = 1.0;

	private int maxWidth = 2000;

	private int maxHeight = 2000;

	private int cadratHeight = 20;

	public MDCDrawingFacade() {
		this.hieroglyphsDrawer = new HieroglyphicDrawerDispatcher(new DefaultHieroglyphicFontManager());
	}

	public MDCDrawingFacade(HieroglyphsDrawer hieroglyphsDrawer) {
		super();
		this.hieroglyphsDrawer = hieroglyphsDrawer;
	}

	/**
	 * Generate a picture for the manuel de codage text passed as argument.
	 * 
	 * @param mdcCodes
	 *                 : a description, in manuel de codage, of the text.
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
		// First, create a dummy picture to compute the target image size.

		BufferedImage dummy = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		Graphics2D g0 = (Graphics2D) dummy.getGraphics();
		JSeshRenderContext renderContext0 = new JSeshRenderContext(jseshStyle, hieroglyphsDrawer);
		JSeshTechRenderContext techRenderContext = buildTechContext(g0);

		ViewAndBounds viewAndBounds = new ViewAndBounds(t, 0, 0, renderContext0, techRenderContext);

		BufferedImage result;

		int width = (int) viewAndBounds.bounds.getWidth();
		int height = (int) viewAndBounds.bounds.getHeight();

		if (width > maxWidth)
			width = maxWidth;
		if (height > maxHeight)
			height = maxHeight;

		// Now, build the actual image.
		result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) result.getGraphics();
		JSeshRenderContext renderContext = new JSeshRenderContext(jseshStyle, hieroglyphsDrawer);
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, width, height);
		GraphicsUtils.antialias(g);
		viewAndBounds.draw(g, renderContext, techRenderContext);
		g.dispose();
		g0.dispose();
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
		JSeshRenderContext renderContext = new JSeshRenderContext(jseshStyle, hieroglyphsDrawer);
		JSeshTechRenderContext techRenderContext = buildTechContext(g1);
		ViewAndBounds viewAndBounds = new ViewAndBounds(t, x, y, renderContext, techRenderContext);
		viewAndBounds.draw(g1, renderContext, techRenderContext);
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
		Graphics2D g0 = (Graphics2D) new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB).getGraphics();
		ViewAndBounds viewAndBounds = new ViewAndBounds(t, x, y, new JSeshRenderContext(jseshStyle, hieroglyphsDrawer),
				buildTechContext(g0));
		g0.dispose();
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
		Graphics2D g0 = (Graphics2D) new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB).getGraphics();
		ViewAndBounds viewAndBounds = new ViewAndBounds(buidTopItemList(mdc), x, y, 
		new JSeshRenderContext(jseshStyle, hieroglyphsDrawer),
		buildTechContext(g0)
	);
		g0.dispose();
		return viewAndBounds.bounds;
	}

	private JSeshTechRenderContext buildTechContext(Graphics2D g0) {
		return JSeshTechRenderContext.buildSimpleContext(g0, deviceScale);
	}

	public boolean isPhilologySign() {
		return philologySign;
	}

	private double getScale() {
		// Uses the actual font to compute scale. We need it.
		return cadratHeight
				/ hieroglyphsDrawer.getHeightOfA1();
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

	public JSeshStyle getJseshStyle() {
		return jseshStyle;
	}

	public void setJseshStyle(
			JSeshStyle drawingSpecifications) {
		this.jseshStyle = drawingSpecifications;
	}

	public void setDeviceScale(double deviceScale) {
		this.deviceScale = deviceScale;
	}

	/**
	 * Returns the scale of the graphic device, in graphic units per
	 * typographical point. This is the scale used by the device if
	 * g.getXScale() returns 1.0, not the current scale. Note that we could be
	 * lying. In the case of a screen zoom, for instance, we will still provide
	 * the original scale.
	 * 
	 * @return
	 */
	public double getDeviceScale() {
		return deviceScale;
	}

	/**
	 * Set maximal picture size (only for bitmap pictures).
	 * 
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

		public ViewAndBounds(TopItemList t, double x, double y, JSeshRenderContext renderContext,
				JSeshTechRenderContext techRenderContext) {
			ViewBuilder viewBuilder = new ViewBuilder();			
			view = viewBuilder.buildView(t, renderContext, techRenderContext);
			double scale = getScale();

			int width = (int) Math.ceil(view.getWidth() * scale) + 2;
			int height = (int) Math.ceil(view.getHeight() * scale) + 2;

			bounds = new Rectangle2D.Double(x, y, width, height);
		}

		public void draw(Graphics2D g, JSeshRenderContext renderContext, JSeshTechRenderContext techRenderContext) {
			double scale = getScale();

			g.setBackground(Color.WHITE);

			g.translate(bounds.getMinX(), bounds.getMinY());
			g.scale(scale, scale);
			g.setColor(Color.BLACK);
			ViewDrawer drawer = new ViewDrawer();
			drawer.draw(g, renderContext, techRenderContext, view);
		}
	}

	public static void main(String[] args) {
		System.out.println("Test of MDCDrawingFacade");
		MDCDrawingFacade facade = new MDCDrawingFacade();
		String mdc = "i-w-r:a-ra-m-p*t:pt";
		try {
			BufferedImage img = facade.createImage(mdc);
			ImageIO.write(img, "png", new File("testPict.png"));
			System.out.println("Image created : " + img.getWidth() + " x " + img.getHeight());
		} catch (MDCSyntaxError | IOException e) {
			e.printStackTrace();
		}
	}

}
/*
 * Created on 12 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.render.draw;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.StringReader;

import jsesh.glyphs.fonts.PredefinedFonts;
import jsesh.model.TopItemList;
import jsesh.parser.MDCParserModelGenerator;
import jsesh.parser.MDCSyntaxError;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.elements.HieroglyphDrawer;
import jsesh.render.style.JSeshStyle;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;
import jsesh.utils.swing.GraphicsUtils;

/**
 * A simple class for programmers who want to draw hieroglyphs.
 * <p>
 * Four independent knobs affect the output, and it's easy to confuse them:
 * <ul>
 * <li>{@link #setCadratHeight(int)} (pixels) is the practical "how big should
 * this render" knob: it's the output height of a reference sign (A1). It
 * overrides, for output-scaling purposes only, the {@code standardSignHeight}
 * carried by the current {@link JSeshStyle}.
 * <li>the {@link JSeshStyle} passed via {@link #setStyle(JSeshStyle)} (points)
 * fixes the <i>proportions</i> of everything relative to everything else
 * (margins, cartouche thickness, etc.) — its own absolute point values do not
 * determine the final pixel size, only {@code cadratHeight} does.
 * <li>{@link #setDeviceScale(double)} is unrelated to apparent output size: it
 * only tunes the rasterizer precision ({@code FontRenderContext}) for the
 * target device.
 * <li>{@link #setMaxSize(int, int)} is a safety cap, used only by
 * {@link #createImage}, for bitmap output.
 * </ul>
 * Use {@link #buildDefault()} or {@link #builder()} for the common case, and
 * {@link #builder(JSeshRenderContext)} when a custom style or font repository
 * is needed.
 *
 * @author S. Rosmorduc
 */
public class MDCDrawingFacade {

	private boolean philologySign = true;

	private JSeshRenderContext jSeshRenderContext;

	private final HieroglyphDrawer hieroglyphDrawer;

	/**
	 * How many pixels on the device to make a typographical point?
	 */
	private double deviceScale = 1.0;

	private int maxWidth = 2000;

	private int maxHeight = 2000;

	private int cadratHeight = 20;

	/**
	 * Build a MDCDrawingFacade for easy rendering of hieroglyphs.
	 * <p>
	 * If you need a MDCDrawing facade for quick rendering of glyphs, and if you are
	 * sure you
	 * don't need to specific adjustments, you can use the static method
	 * buildDefault().
	 * 
	 * @param jSeshRenderContext
	 */
	public MDCDrawingFacade(JSeshRenderContext jSeshRenderContext) {
		this.jSeshRenderContext = jSeshRenderContext;
		this.hieroglyphDrawer = new HieroglyphDrawer(jSeshRenderContext.hieroglyphShapeRepository());
	}

	/**
	 * Set the style to use for rendering.
	 * <p> Note: currently, the style does not affect the actual output size, which is controlled by {@link #setCadratHeight(int)}.
	 * It only affects the relative proportions of the various elements (margins, cartouche thickness, etc.).
	 * @param jSeshStyle
	 */
	public void setStyle(JSeshStyle jSeshStyle) {
		this.jSeshRenderContext = jSeshRenderContext.copy().jseshStyle(jSeshStyle).build();
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
		JSeshTechRenderContext techRenderContext = buildTechContext(g0);

		ViewAndBounds viewAndBounds = new ViewAndBounds(t, 0, 0, jSeshRenderContext, techRenderContext);

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
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, width, height);
		GraphicsUtils.antialias(g);
		viewAndBounds.draw(g, jSeshRenderContext, techRenderContext);
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
	 * Computes the bounds of a particular text without drawing it.
	 * 
	 * @param t
	 * @param x
	 * @param y
	 * @return
	 */
	public Rectangle2D getBounds(TopItemList t, double x, double y) {
		Graphics2D g0 = (Graphics2D) new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB).getGraphics();
		ViewAndBounds viewAndBounds = new ViewAndBounds(t, x, y, jSeshRenderContext,
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
				jSeshRenderContext,
				buildTechContext(g0));
		g0.dispose();
		return viewAndBounds.bounds;
	}

	/**
	 * sets the way philological parenthesis will be considered when read. If
	 * true, they will be considered as mere signs. If false, they will be
	 * considered as parenthesis (and thus will require matching).
	 * 
	 * You won't need to call this method in most cases.
	 * It's only needed to handles old TkSesh texts. and by now, you would have transformed
	 * them if you are a former user of TkSesh.
	 * 
	 * @param philologySign
	 */
	public void setPhilologySign(boolean philologySign) {
		this.philologySign = philologySign;
	}
	
	/**	 
	 * @return the philologySign
	 */
	private boolean isPhilologySign() {
		return philologySign;
	}

	/**
	 * Sets the output device scale, in graphic units per typographical point.
	 * <p> Usually left alone.
	 * @param deviceScale
	 */
	public void setDeviceScale(double deviceScale) {
		this.deviceScale = deviceScale;
	}

	/**
	 * Set maximal picture size (only for bitmap pictures).
	 * 
	 * <p> It will prevent the creation of bitmaps larger than the specified size.
	 * @param width
	 * @param height
	 */
	public void setMaxSize(int width, int height) {
		this.maxWidth = width;
		this.maxHeight = height;
	}

	/**
	 * Set the approximative cadrat height, in pixels.
	 * <p> Default value is 20.
	 * 
	 * <p> If you want to control the picture size, this is the method to call.
	 * @param cadratHeight
	 */
	public void setCadratHeight(int cadratHeight) {
		this.cadratHeight = cadratHeight;
	}

	/**
	 * Builds a default MDCDrawingFacade - READ the documentation for what it
	 * implies.
	 * <p>
	 * The rendered hieroglyphs will have default settings, and <b>the hieroglyphic
	 * font won't contain the users' additions.</b>
	 * 
	 * @return a new default instance of MDCDrawingFacade.
	 */
	public static MDCDrawingFacade buildDefault() {
		return new MDCDrawingFacade(
				new JSeshRenderContext(JSeshStyle.DEFAULT, PredefinedFonts.buildAllEmbeddedFonts()));
	}

	/**
	 * Returns a builder for a MDCDrawingFacade, seeded with the same default
	 * style and embedded fonts as {@link #buildDefault()}.
	 * <p>
	 * This is the recommended entry point for the common case: e.g.
	 * {@code MDCDrawingFacade.builder().cadratHeight(24).build()}.
	 *
	 * @return a new Builder.
	 */
	public static Builder builder() {
		return new Builder(new JSeshRenderContext(JSeshStyle.DEFAULT, PredefinedFonts.buildAllEmbeddedFonts()));
	}

	/**
	 * Returns a builder for a MDCDrawingFacade using a custom render context
	 * (custom style and/or font repository) — the entry point for the
	 * complex/advanced case.
	 *
	 * @param jSeshRenderContext the render context (style + hieroglyph shape
	 *                           repository) to draw with.
	 * @return a new Builder.
	 */
	public static Builder builder(JSeshRenderContext jSeshRenderContext) {
		return new Builder(jSeshRenderContext);
	}

	/**
	 * A fluent builder for {@link MDCDrawingFacade}, mirroring the
	 * {@code copy()}/{@code Builder} idiom used by {@link JSeshRenderContext}
	 * and {@link JSeshStyle}.
	 */
	public static class Builder {
		private JSeshRenderContext jSeshRenderContext;
		private boolean philologySign = true;
		private double deviceScale = 1.0;
		private int maxWidth = 2000;
		private int maxHeight = 2000;
		private int cadratHeight = 20;

		private Builder(JSeshRenderContext jSeshRenderContext) {
			this.jSeshRenderContext = jSeshRenderContext;
		}

		/**
		 * Sets the style to draw with (see the class documentation for how this
		 * interacts with {@link #cadratHeight(int)}).
		 */
		public Builder style(JSeshStyle style) {
			this.jSeshRenderContext = jSeshRenderContext.copy().jseshStyle(style).build();
			return this;
		}

		public Builder philologySign(boolean philologySign) {
			this.philologySign = philologySign;
			return this;
		}

		public Builder deviceScale(double deviceScale) {
			this.deviceScale = deviceScale;
			return this;
		}

		public Builder maxSize(int maxWidth, int maxHeight) {
			this.maxWidth = maxWidth;
			this.maxHeight = maxHeight;
			return this;
		}

		public Builder cadratHeight(int cadratHeight) {
			this.cadratHeight = cadratHeight;
			return this;
		}

		public MDCDrawingFacade build() {
			MDCDrawingFacade facade = new MDCDrawingFacade(jSeshRenderContext);
			facade.philologySign = philologySign;
			facade.deviceScale = deviceScale;
			facade.maxWidth = maxWidth;
			facade.maxHeight = maxHeight;
			facade.cadratHeight = cadratHeight;
			return facade;
		}
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

	/**
	 * Draws the data on an existing graphic.
	 * 
	 * @param t
	 * @param g
	 * @param x
	 * @param y
	 * @return the bounding box of the drawn text.
	 */

	private Rectangle2D draw(TopItemList t, Graphics2D g, double x, double y) {
		Graphics2D g1 = (Graphics2D) g.create();
		JSeshTechRenderContext techRenderContext = buildTechContext(g1);
		ViewAndBounds viewAndBounds = new ViewAndBounds(t, x, y, jSeshRenderContext, techRenderContext);
		viewAndBounds.draw(g1, jSeshRenderContext, techRenderContext);
		g1.dispose();
		return viewAndBounds.bounds;
	}

	private JSeshTechRenderContext buildTechContext(Graphics2D g0) {
		return JSeshTechRenderContext.buildSimpleContext(g0, deviceScale);
	}

	private double getScale() {
		// cadratHeight (pixels) overrides the style's standardSignHeight (points)
		// for output-scaling purposes; delegate to the single formula owner.
		JSeshStyle scaledStyle = jSeshRenderContext.jseshStyle().copy()
				.geometry(g -> g.standardSignHeight(cadratHeight))
				.build();
		return hieroglyphDrawer.scaleFromFontToStyle(scaledStyle);
	}

	private TopItemList buidTopItemList(String mdcCodes) throws MDCSyntaxError {
		MDCParserModelGenerator gen = new MDCParserModelGenerator();
		gen.setPhilologyAsSigns(isPhilologySign());
		TopItemList t = gen.parse(new StringReader(mdcCodes));
		return t;
	}

}
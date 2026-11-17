package jsesh.mdcDisplayer.draw;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jsesh.mdc.constants.Constants;
import jsesh.mdc.constants.LexicalSymbolsUtils;
import jsesh.mdc.constants.ScriptCodes;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.Cartouche;
import jsesh.mdc.model.HRule;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.OptionsMap;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.Philology;
import jsesh.mdc.model.Superscript;
import jsesh.mdc.model.TopItemState;
import jsesh.mdc.model.ZoneStart;
import jsesh.mdcDisplayer.mdcView.RelativePosition;

/**
 * This file is free Software under the GNU LESSER GENERAL PUBLIC LICENCE.
 * 
 * 
 * (c) Serge Rosmorduc
 * 
 * @author Serge Rosmorduc
 *  
 */
public class SimpleElementDrawer extends ElementDrawer {

	private boolean shading;

	public void reset() {
		shading = false;
		setDrawingState(new TopItemState());
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitAlphabeticText(jsesh.mdc.model.AlphabeticText)
	 */
	public void visitAlphabeticText(AlphabeticText t) {
		if (!postfix)
			return;
		if (t.getScriptCode() != ScriptCodes.COMMENT) {
			g.setFont(getDrawingSpecifications().getFont(t.getScriptCode()));
			g.drawString(t.getText().toString(), 0, g.getFontMetrics()
					.getAscent());
		}
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitCartouche(jsesh.mdc.model.Cartouche)
	 * 
	 * NOTE: Cartouches are drawn larger than the typical cadrat Height, so that
	 * the signs in the cartouche be as tall as the ones outside. The cartouche
	 * positioning is done with setStartPoint(). If a Layout modifies the
	 * cartouche's startPoint, it won't work. So we might be interested in
	 * having a slightly richer model for views, with kinds of "alignment
	 * points", usable for horizontal and vertical positionning.
	 *  
	 */

	public void visitCartouche(Cartouche c) {
		if (!postfix)
			return;
		Stroke s = drawingSpecifications.getCartoucheStroke(c.getType());
		float w1 = drawingSpecifications.getCartoucheStartWidth(c.getType(), c
				.getStartPart());
		float w2 = drawingSpecifications.getCartoucheEndWidth(c.getType(), c
				.getEndPart());
		float dy = drawingSpecifications.getCartoucheTopMargin(c.getType());
		// The respective limits of this cartouche's parts.
		//
		//    start p1------p3 end
		//   
		//           p2------p4
		//      <....>
		//        w1 w2
		//
		dy = 0;
		Point2D p1, p2, p3, p4;
		p1 = new Point2D.Float(w1, -dy);
		p2 = new Point2D.Float(w1, currentView.getHeight() + dy);
		p3 = new Point2D.Float(currentView.getWidth() - w2, -dy);
		p4 = new Point2D.Float(currentView.getWidth() - w2, currentView
				.getHeight()
				+ dy);

		g.setStroke(s);
		// Start
		if (c.getStartPart() != 0) {
			switch (c.getType()) {
			case 'c': {
				g.draw(new CubicCurve2D.Double(w1, -dy, -w1 / 3, -dy, -w1 / 3,
						currentView.getHeight() + dy, w1, currentView
								.getHeight()
								+ dy));
				if (c.getStartPart() == 2)
					g.draw(new Line2D.Double(0, p1.getY(), 0, p2.getY()));
			}
				break;
			case 'h': {
				Point2D pa = new Point2D.Float(0, -dy);
				Point2D pb = new Point2D.Float(0, currentView.getHeight() + dy);
				drawHutEnd(c.getStartPart(), pa, pb, p1, p2);
			}
				break;
			case 's': // Serekh
			{
				Point2D pa = new Point2D.Float(0, -dy);
				Point2D pb = new Point2D.Float(0, currentView.getHeight() + dy);
				if (c.getStartPart() == 1) {
					drawHutEnd(c.getStartPart(), pa, pb, p1, p2);
				} else
					drawSerekhEnd(p1, p2, pa, pb);
			}
				break;
			case 'f':
				break;
			}
		}
		// Middle part.

		switch (c.getType()) {
		case 'c':
		case 'h':
		case 's':

			g.draw(new Line2D.Float(p1, p3));
			g.draw(new Line2D.Float(p2, p4));
		case 'f':
			g.setStroke(s);
			g.draw(new Line2D.Float(p1, p3));
			g.draw(new Line2D.Float(p2, p4));
			break;
		}
		// End
		if (c.getEndPart() != 0) {
			switch (c.getType()) {
			case 'c':
				g.draw(new CubicCurve2D.Double(p3.getX(), p3.getY(),
						currentView.getWidth() + w1 / 3, p3.getY(), currentView
								.getWidth()
								+ w1 / 3, p4.getY(), p4.getX(), p4.getY()));
				if (c.getEndPart() == 2)
					g.draw(new Line2D.Double(currentView.getWidth(), p3.getY(),
							currentView.getWidth(), p4.getY()));
				break;
			case 'h': {
				Point2D pa = new Point2D.Float(currentView.getWidth(), -dy);
				Point2D pb = new Point2D.Float(currentView.getWidth(),
						currentView.getHeight() + dy);
				drawHutEnd(c.getEndPart(), pa, pb, p3, p4);
			}
				break;
			case 's': // serekh
			{
				Point2D pa = new Point2D.Float(currentView.getWidth(), -dy);
				Point2D pb = new Point2D.Float(currentView.getWidth(),
						currentView.getHeight() + dy);

				if (c.getEndPart() == 1) {
					drawHutEnd(c.getEndPart(), pa, pb, p3, p4);
				} else {
					drawSerekhEnd(p3, p4, pa, pb);
				}
			}
				break;
			case 'f':
				break;
			}
		}
		// end
	}

	/**
	 * Draws the "palace facade" part of an horizontal serekh.
	 * 
	 * @param p1
	 *            top corner for the text part of the serekh
	 * @param p2
	 *            bottom corner of the text
	 * @param pa
	 *            top corner of the end of the palace facade
	 * @param pb
	 *            bottom corner of the end of the palace facade
	 */
	private void drawSerekhEnd(Point2D p1, Point2D p2, Point2D pa, Point2D pb) {
		// The width of this part :
		double dx = pa.getX() - p1.getX();
		double dy = p2.getY() - p1.getY();
		// vertical lines positions (corniches)
		double line1x = p1.getX() + dx * 0.1;
		double line2x = p1.getX() + dx * 0.3;
		double line3x = p1.getX() + dx * 0.4;
		// inner recesses
		double recessX = p1.getX() + dx * 0.5;

		g.draw(new Line2D.Double(p1, pa));
		g.draw(new Line2D.Double(p2, pb));
		g.draw(new Line2D.Double(line1x, p1.getY(), line1x, p2.getY()));

		g.setStroke(drawingSpecifications.getFineStroke());
		g.draw(new Line2D.Double(line2x, p1.getY(), line2x, p2.getY()));
		g.draw(new Line2D.Double(line3x, p1.getY(), line3x, p2.getY()));

		// Vertical positions for inner lines :

		g.setStroke(drawingSpecifications.getFineStroke());
		// recesses
		for (int i = 1; i < 10; i += 3) {
			double posy1 = p1.getY() + 0.1 * dy * i;
			double posy2 = p1.getY() + 0.1 * dy * (i + 1);
			double posy3 = p1.getY() + 0.1 * dy * (i + 2);
			double endX = pa.getX();

			g.draw(new Line2D.Double(recessX, posy1, endX, posy1));
			g.draw(new Line2D.Double(recessX, posy3, endX, posy3));
			g.draw(new Line2D.Double(recessX, posy1, recessX, posy3));

			double innerX = recessX + 0.2 * (endX - recessX);
			g.draw(new Line2D.Double(innerX, posy2, endX, posy2));

		}

		g.setStroke(drawingSpecifications.getCartoucheStroke('s'));

	}

	private void drawHutEnd(int part, Point2D pa, Point2D pb, Point2D p1,
			Point2D p2) {
		if (part != 0) {
			g.draw(new Line2D.Float(pa, pb));
			g.draw(new Line2D.Float(pa, p1));
			g.draw(new Line2D.Float(pb, p2));

			// squareSize is either negative or positive :
			// it's positive for start of hut-sign, and negative for end.

			double squareSize = (p1.getX() - pa.getX()) * 0.75;

			switch (part) {
			// square in the bottom :
			case 2: {
				Point2D pca, pc, pcb;
				// point on the vertical line
				pca = new Point2D.Double(pb.getX(), pb.getY()
						- Math.abs(squareSize));
				// point on the horizontal line
				pcb = new Point2D.Double(pb.getX() + squareSize, pb.getY());
				// square corner
				pc = new Point2D.Double(pb.getX() + squareSize, pb.getY()
						- Math.abs(squareSize));
				g.draw(new Line2D.Float(pca, pc));
				g.draw(new Line2D.Float(pc, pcb));
			}
				break;
			case 3: {
				Point2D pca, pc, pcb;
				pca = new Point2D.Double(pa.getX(), pa.getY()
						+ Math.abs(squareSize));
				pcb = new Point2D.Double(pa.getX() + squareSize, pa.getY());
				pc = new Point2D.Double(pa.getX() + squareSize, p1.getY()
						+ Math.abs(squareSize));
				g.draw(new Line2D.Float(pca, pc));
				g.draw(new Line2D.Float(pc, pcb));
			}
			}
		}
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
	 */
	public void visitHieroglyph(Hieroglyph h) {
		if (!postfix)
			return;
		if (h.getRelativeSize() != 100)
			g.scale(h.getRelativeSize() / 100f, h.getRelativeSize() / 100f);
		if (h.getModifiers().getBoolean("red")) {
			g.setColor(drawingSpecifications.getRedColor());
		}
		if (h.getModifiers().getBoolean("i")) {
			g.setColor(drawingSpecifications.getGrayColor());
		}
		switch (h.getType()) {
		// Note : the only "special" codes are red points and probably shades.
		// The other codes are dealt with by the HieroglyphsDrawer.
		case Constants.REDPOINT: {
			Color col = g.getColor();
			g.setColor(drawingSpecifications.getRedColor());
			drawingSpecifications.getHieroglyphsDrawer().draw(g, h.getCode(),
					0, currentView);
			g.setColor(col);
		}
			break;
		case Constants.HALFSPACE:
		case Constants.FULLSPACE:
			break;
		case Constants.FULLSHADE:
		case Constants.VERTICALSHADE:
		case Constants.HORIZONTALSHADE:
		case Constants.QUATERSHADE: {
			// This should probably be in SimpleHieroglyphDrawer (which should
			// be called SimpleSignDrawer !)
			Color col = g.getColor();
			g.setColor(drawingSpecifications.getGrayColor());
			Rectangle2D.Float r = new Rectangle2D.Float(0, 0, currentView
					.getInternalWidth(), currentView.getInternalHeight());
			g.fill(r);
			g.setColor(col);
		}
			break;
		// NOTE : Obviously, it would be better to create some kind of "symbol"
		// class for signs which are not hieroglyphs.
		case Constants.BEGINERASE:
		case Constants.ENDERASE:
		case Constants.BEGINEDITORADDITION:
		case Constants.ENDEDITORADDITION:
		case Constants.BEGINEDITORSUPERFLUOUS:
		case Constants.ENDEDITORSUPERFLUOUS:
		case Constants.BEGINPREVIOUSLYREADABLE:
		case Constants.ENDPREVIOUSLYREADABLE:
		case Constants.BEGINSCRIBEADDITION:
		case Constants.ENDSCRIBEADDITION: {
			String txt = LexicalSymbolsUtils.getStringForPhilology(h.getType());
			g.setFont(drawingSpecifications.getFont('l'));
			g.drawString(txt, 0, g.getFontMetrics().getAscent());
		}
			break;
		default:
			String c = h.getCode();
			// g.setStroke(drawingSpecifications.getNullStroke());
			if (h.isReversed()) {
				g.transform(new AffineTransform(-1, 0, 0, 1, currentView
						.getWidth(), 0));
			}
			if (h.getModifiers().isFillX()) {
			}
			if (drawingSpecifications.getHieroglyphsDrawer().isKnown(c)) {
				drawingSpecifications.getHieroglyphsDrawer().draw(g, c,
						h.getAngle(), currentView);
			} else {
				g.setFont(drawingSpecifications.getSuperScriptFont());
				float x, y;
				Dimension2D r = drawingSpecifications
						.getSuperScriptDimensions(c);
				g.drawString(c, 0, (float) r.getHeight());
			}
			break;

		}
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitHRule(jsesh.mdc.model.HRule)
	 */
	public void visitHRule(HRule h) {
		if (!postfix)
			return;
		if (h.getType() == 'L')
			g.setStroke(drawingSpecifications.getWideStroke());
		else
			g.setStroke(drawingSpecifications.getFineStroke());
		g.draw(new Line2D.Float(h.getStartPos() / 200f
				* drawingSpecifications.getBaseLength(), 0, h.getEndPos()
				/ 200f * drawingSpecifications.getBaseLength(), 0));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.ElementDrawer#visitPageBreak(jsesh.mdc.model.PageBreak)
	 */
	public void visitPageBreak(PageBreak b) {
		if (!postfix)
			return;
		if (!isPaged())
			g.draw(new Line2D.Float(0, 0, 10000f, 0));
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitPhilology(jsesh.mdc.model.Philology)
	 */
	public void visitPhilology(Philology p) {
		if (!postfix)
			return;
		try {

			drawBracket(p.getType() * 2 + 1, currentView.getWidth()
					- drawingSpecifications.getPhilologyWidth(p.getType()), 0);
			drawBracket(p.getType() * 2, 0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void drawBracket(int code, float x, float y) {
		Font f = drawingSpecifications.getFont('l');

		// Save current transformation.
		//AffineTransform old= g.getTransform();
		Graphics2D tmpG = (Graphics2D) g.create();
		tmpG.setFont(f);
		tmpG.translate(x, y);
		String s = LexicalSymbolsUtils.getStringForPhilology(code);
		Rectangle2D d = drawingSpecifications.getTextDimensions('l', s);

		double scalex = drawingSpecifications.getPhilologyWidth(code)
				/ d.getWidth();

		double scaley = currentView.getHeight() / d.getHeight();
		//scaley= 4;
		tmpG.scale(scalex, scaley);

		tmpG.drawString(s, (float) (-d.getMinX() * scalex), (float) ((d
				.getHeight() - d.getMaxY()) * scaley));

		//tmpG.drawString(s, 0, g.getFontMetrics().getAscent());

		// Restore :
		//g.setTransform(old);
		tmpG.dispose();
	}

	/**
	 * @see jsesh.mdc.model.ModelElementVisitor#visitSuperScript(jsesh.mdc.model.Superscript)
	 */
	public void visitSuperScript(Superscript s) {
		if (!postfix)
			return;
		String text = s.getText();
		Dimension2D dims = drawingSpecifications.getSuperScriptDimensions(text);
		g.setFont(getDrawingSpecifications().getSuperScriptFont());
		g.drawString(text.toString(), 0, g.getFontMetrics().getAscent());
		g.setStroke(drawingSpecifications.getFineStroke());
		g
				.draw(new Line2D.Float((float) dims.getWidth() / 2.0f,
						(float) dims.getHeight()
								+ drawingSpecifications.getSmallSkip(),
						(float) dims.getWidth() / 2.0f, currentView
								.getInternalHeight()));

	}

	/*
	 * 
	 * Note that the coordinate system in any of these methods is scaled !
	 */
	public void visitCadrat(Cadrat c) {
		if (postfix && isShadeAfter())
			doShade(c);
		if ((!postfix) && (!isShadeAfter()))
			doShade(c);
	}

	// Shade a cadrat. Ok, that's exactly what visitcadrat does, but it's
	// clearer (and does it need to be a cadrat ?)

	private void doShade(Cadrat c) {
		int localshading = c.getShading();
		// NOTE : we use a very simple system for global shading.
		if (shading)
			localshading = Cadrat.FULL;
		if (localshading != Cadrat.NONE) {
			Rectangle2D.Double r = new Rectangle2D.Double(0, 0, currentView
					.getInternalWidth() / 2,
					currentView.getInternalHeight() / 2);
			// IMPORTANT : we use the alpha chanel for transparency.
			// If this doesn't work for some implementations of Graphics2D,
			// We should have a "B plan".
			Color gray = drawingSpecifications.getGrayColor();
			Paint currentPaint = g.getPaint();
			g.setPaint(gray);
			if ((localshading & Cadrat.TOP_START) != 0) {
				// IMPORTANT : ensure this works in right-to-left environment
				// also.
				g.fill(r);
			}
			if ((localshading & Cadrat.TOP_END) != 0) {
				r.x = currentView.getInternalWidth() / 2;
				g.fill(r);
			}
			if ((localshading & Cadrat.BOTTOM_START) != 0) {
				r.x = 0;
				r.y = currentView.getInternalHeight() / 2;
				g.fill(r);
			}
			if ((localshading & Cadrat.BOTTOM_END) != 0) {
				r.x = currentView.getInternalWidth() / 2;
				r.y = currentView.getInternalHeight() / 2;
				g.fill(r);
			}

			// Fill the space between this view and the next one.
			// Note that it would be probably better to use a dedicated method
			// for this (r.width computation could
			// be much simpler, and things would be clearer).
			if (shading
					&& currentView.getNextViewPosition().getXAnchor() == RelativePosition.PREVIOUS) {
				// Shade the inter-cadrat space
				r.x = currentView.getInternalWidth();
				r.y = 0f;
				r.height = currentView.getInternalHeight();
				r.width = (currentView.getNextViewPosition().getDx() - currentView
						.getInternalWidth());
				g.fill(r);
			}
			g.setPaint(currentPaint);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.ModelElementVisitor#visitZoneStart(jsesh.mdc.model.ZoneStart)
	 */
	public void visitZoneStart(ZoneStart start) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.ModelElementVisitor#visitOptionList(jsesh.mdc.model.OptionsMap)
	 */
	public void visitOptionList(OptionsMap list) {

	}
}
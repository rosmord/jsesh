package jsesh.tmp.simpleDemo;

/**
 * Simple drawing system for hieroglyphic texts. Only deal with basic
 * cadrats. This is only a "proof of concept" implementation.
 *
 *
 * Created: Mon Jun 10 16:34:26 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 */
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

import jsesh.bzr.simple.ShapeChar;
import jsesh.hieroglyphs.HieroglyphicFontManager;
import jsesh.utils.GraphicsUtils;

public class HieroglyphicDrawer {

	private double scale;

	/**
	 * Height of a line of hieroglyphs.
	 */
	public double lineHeight = 18.0;

	public double lineSkip = 5.0;

	/**
	 * Minimal space between signs.
	 */
	public double minSpace = 1.0;

	// Current drawing position.
	private double xPos, yPos;

	Graphics2D g;


	HieroglyphicText t;
	HieroglyphicFontManager paleography;

	public HieroglyphicDrawer(HieroglyphicFontManager paleography) {
		this.paleography = paleography;
		scale= 1.0;
	}

	public void draw(Graphics2D g, HieroglyphicText t) {
		this.g = g;
		this.t = t;
		if (g != null && t != null) {
			GraphicsUtils.antialias(g);
			g.scale(scale,scale);
			draw();
		}
	}

	private void drawCadrat(List cadrat) {
		// A cadrat is a pile of horizontal boxes.
		// First, we compute the cadrat's "natural" size :
		Iterator hboxIterator;

		// Minimal area used by the cadrat, if we consider NO SPACE 
		// between the signs

		double width = 0.0;
		double height = 0.0;

		// widths for the cadrat's lines 
		double linesWidths[];
		double linesHeigths[];

		// space used by interlinear spaces in this cadrat
		double verticalSpace = 0.0;

		if (cadrat.size() > 1)
			verticalSpace = cadrat.size() * minSpace;

		linesWidths = new double[cadrat.size()];
		linesHeigths = new double[cadrat.size()];

		hboxIterator = cadrat.iterator();

		int lineNum = 0;
		// For all lines in the cadrat

		while (hboxIterator.hasNext()) {
			List signList = (List) hboxIterator.next();
			double cadratLineHeight = 0.0;
			double cadratLineWidth = 0.0;
			double horizontalSpacing = 0.0;
			// The test is useful only if signList is empty.
			if (signList.size() > 1)
				horizontalSpacing = minSpace * (signList.size() - 1);

			Iterator signIterator = signList.iterator();
			while (signIterator.hasNext()) {
				String code = (String) signIterator.next();
				ShapeChar mychar = paleography.get(code);
				if (mychar != null) {
					Rectangle2D bbox = mychar.getBbox();
					cadratLineWidth += bbox.getMaxX();
					if (bbox.getMaxY() > cadratLineHeight)
						cadratLineHeight = bbox.getMaxY();
				}
			}
			height += cadratLineHeight;
			linesWidths[lineNum] = cadratLineWidth;
			linesHeigths[lineNum++] = cadratLineHeight;

			if (cadratLineWidth + horizontalSpacing > width)
				width = cadratLineWidth + horizontalSpacing;
		}

		// Now, we reduce the cadrat signs size accordingly
		// In this test version, no scaling is done regarding the cadrat's width.

		double goodScale = lineHeight / (height + verticalSpace);

		if (goodScale > 1.0)
			goodScale = 1.0;

		// In the final program, we will ask signs to draw themselves in a rectangle, telling 
		// them if they have neighbours, and the sign drawing system will chose the sign position
		// and/or the sign size accordingly

		// Vertical spacing between lines
		double vspace = minSpace * goodScale;

		double x = xPos;
		double y = yPos;

		// If the cadrat's extends does not cover the full line height :

		if (height * goodScale < lineHeight) {
			// if there is only *one* line, place it on the bottom line.
			if (cadrat.size() == 1) {
				y += (lineHeight - height * goodScale);
			} else // evenly distribute the space between lines
				{
				vspace =
					(lineHeight - height * goodScale) / (cadrat.size() - 1);
			}
		}

		hboxIterator = cadrat.iterator();
		lineNum = 0;
		while (hboxIterator.hasNext()) {
			List signList = (List) hboxIterator.next();
			// line height
			double thisLineHeight = linesHeigths[lineNum] * goodScale;

			double thisLineWidth = linesWidths[lineNum++] * goodScale;

			// horizontal spacing between signs
			double hspace = minSpace * goodScale;

			x = xPos;

			if (thisLineWidth < width * goodScale) {
				// If there is only one sign, center it :
				if (signList.size() == 1) {
					hspace = 0.0;
					x = x + (width * goodScale - thisLineWidth) / 2;
				} else {
					hspace =
						(width * goodScale - thisLineWidth)
							/ (signList.size() - 1);
				}
			}

			// Now, draw the line
			Iterator signIterator = signList.iterator();
			while (signIterator.hasNext()) {
				String code = (String) signIterator.next();
				// System.out.println("drawing " + code);
				ShapeChar mychar = paleography.get(code);
				if (mychar != null) {
					Rectangle2D bbox = mychar.getBbox();
					if (g != null)
						mychar.draw(
							g,
							x,
							y + (thisLineHeight - bbox.getMaxY() * goodScale),
							goodScale,
							goodScale,0);

					x += bbox.getMaxX() * goodScale + hspace;
				}
			}
			y += thisLineHeight + vspace;
		}

		// advance xPos :
		xPos = xPos + width * goodScale + minSpace;
	}

	private void draw() {
		Iterator lineIterator;
		double yMin, yMax;

		Rectangle2D r = (Rectangle2D) g.getClipBounds();

		if (r != null) {
			yMin = r.getMinY() - (lineHeight + lineSkip);
			yMax = r.getMaxY() + lineHeight + lineSkip;
		} else {
			yMin = 0;
			yMax = getTextHeight(t);
		}

		xPos = 0;
		yPos = 0;

		lineIterator = t.lines.iterator();
		while (lineIterator.hasNext()) {
			Iterator cadratIterator = ((List) lineIterator.next()).iterator();
			if (yPos >= yMin && yPos <= yMax) {
				while (cadratIterator.hasNext()) {
					List cadrat = (List) cadratIterator.next();
					drawCadrat(cadrat);
				}
			}
			yPos = yPos + lineHeight + lineSkip;
			xPos = 0.0;
		}
	}

	public double getTextWidth(HieroglyphicText t) {
		double result = 0.0;
		this.t = t;
		xPos = 0;
		yPos = 0;
		g = null;

		Iterator lineIterator;

		lineIterator = t.lines.iterator();
		while (lineIterator.hasNext()) {
			Iterator cadratIterator = ((List) lineIterator.next()).iterator();
			while (cadratIterator.hasNext()) {
				List cadrat = (List) cadratIterator.next();
				drawCadrat(cadrat);
			}
			if (xPos > result)
				result = xPos;
			xPos = 0;
		}
		return result*scale;
	}

	public double getTextHeight(HieroglyphicText t) {
		double res;
		if (t.lines.size() > 1)
			res= t.lines.size() * (lineHeight + lineSkip);
		else
			res = (lineHeight);
			
		return res*scale;
	}

	/**
	 * @param h
	 */
	public void setSigneHeight(int h) {
		scale= h/18.0;
	}

} // HieroglyphicDrawer

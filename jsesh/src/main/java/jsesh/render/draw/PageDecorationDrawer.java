package jsesh.render.draw;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

import jsesh.render.style.JSeshStyle;
import jsesh.render.style.PageDecoration;
import jsesh.render.style.PageSpecification;
import jsesh.render.style.PageTextPosition;

/// Writes the page number and the title in the margins of a page.
///
/// The texts use the plain font of the style. They are centered vertically in
/// the top or bottom margin, and lie between the left and right margins.
public class PageDecorationDrawer {

	private PageDecorationDrawer() {
	}

	/// Draws the texts of a page.
	///
	/// @param g the graphics. Its color is used as is.
	/// @param style the style, which gives the page, the texts and the font.
	/// @param pageNumber the number of the page, from 1.
	/// @param pageTop the vertical position of the top of the page.
	public static void draw(Graphics2D g, JSeshStyle style, int pageNumber, double pageTop) {
		PageSpecification page = style.geometry().page();
		PageDecoration decoration = style.geometry().pageDecoration();
		if (!page.isPaginated() || !decoration.isDefined()) {
			return;
		}
		Font font = style.fonts().plainFont();
		LineMetrics metrics = font.getLineMetrics("Ag", g.getFontRenderContext());
		for (PageTextPosition position : PageTextPosition.values()) {
			String text = decoration.textAt(position, pageNumber);
			if (text != null) {
				drawText(g, font, metrics, page, position, text, pageTop);
			}
		}
	}

	private static void drawText(Graphics2D g, Font font, LineMetrics metrics, PageSpecification page,
			PageTextPosition position, String text, double pageTop) {
		Rectangle2D bounds = font.getStringBounds(text, g.getFontRenderContext());
		double x;
		if (position.isLeft()) {
			x = page.marginLeft();
		} else if (position.isRight()) {
			x = page.pageWidth() - page.marginRight() - bounds.getWidth();
		} else {
			x = page.marginLeft() + (page.usableWidth() - bounds.getWidth()) / 2;
		}
		// The text is centered in the margin, but stays inside the page.
		double ascent = metrics.getAscent();
		double descent = metrics.getDescent();
		double y;
		if (position.isTop()) {
			y = page.marginTop() / 2 + (ascent - descent) / 2;
			y = Math.max(y, ascent);
		} else {
			y = page.pageHeight() - page.marginBottom() / 2 + (ascent - descent) / 2;
			y = Math.min(y, page.pageHeight() - descent);
		}
		g.setFont(font);
		g.drawString(text, (float) x, (float) (pageTop + y));
	}
}

package org.qenherkhopeshef.viewtoolkit.drawing.textOriented;

import java.awt.Graphics2D;
import java.awt.font.GraphicAttribute;
import java.awt.geom.Rectangle2D;

import org.qenherkhopeshef.viewtoolkit.drawing.element.GraphicalElement;

/// Lets a [GraphicalElement] stand in a line of text, as a big "character".
///
/// Used with `TextAttribute.CHAR_REPLACEMENT`, so that the standard Java text
/// layout classes (line breaking, hit testing, carets) handle embedded
/// elements for us. The element sits on the baseline.
final class EmbeddedElementGraphic extends GraphicAttribute {

	/// Horizontal room left on each side of the element.
	private static final float PADDING = 1;

	private final GraphicalElement element;

	EmbeddedElementGraphic(GraphicalElement element) {
		super(GraphicAttribute.ROMAN_BASELINE);
		this.element = element;
	}

	@Override
	public float getAscent() {
		return (float) element.getBounds().getHeight();
	}

	@Override
	public float getDescent() {
		return 0;
	}

	@Override
	public float getAdvance() {
		return (float) element.getBounds().getWidth() + 2 * PADDING;
	}

	@Override
	public void draw(Graphics2D graphics, float x, float y) {
		Graphics2D g = (Graphics2D) graphics.create();
		Rectangle2D b = element.getBounds();
		// GraphicalElement.draw() places the element at its origin; cancel it.
		g.translate(x + PADDING - b.getMinX(), y - getAscent() - b.getMinY());
		element.draw(g);
		g.dispose();
	}
}

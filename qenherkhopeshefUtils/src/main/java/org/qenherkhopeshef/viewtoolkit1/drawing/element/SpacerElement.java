package org.qenherkhopeshef.viewtoolkit.drawing.element;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;

import org.qenherkhopeshef.swingUtils.DoubleDimensions;

/// An invisible element with a fixed preferred size.
///
/// Useful to reserve room in a composite (an empty compartment, a gap...).
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public class SpacerElement extends GraphicalElement {

	private final Dimension2D size;

	public SpacerElement(double width, double height) {
		this.size = new DoubleDimensions(width, height);
		setMargin(Margin.NONE);
	}

	@Override
	protected void drawElement(Graphics2D g) {
		// Nothing to draw.
	}

	@Override
	public Dimension2D getPreferredSize() {
		return size;
	}
}

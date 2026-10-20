package org.qenherkhopeshef.viewToolKit.drawing.element;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.qenherkhopeshef.swingUtils.DoubleDimensions;
import org.qenherkhopeshef.viewToolKit.drawing.element.property.ObjectProperty;

/// A shape element, which can be filled and/or stroked.
///
/// The shape is expressed in the element's own coordinate system: (0,0) is the
/// element origin. The preferred size of the element covers the shape from this
/// origin, so shapes are expected to have non-negative coordinates.
///
/// By default, the shape is filled in black and not stroked (which was the
/// historical behaviour of this class).
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public class ShapeElement extends GraphicalElement {

	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	private ObjectProperty<Color> color = new ObjectProperty<Color>(
			"color", this, Color.BLACK, false);

	private ObjectProperty<Color> lineColor = new ObjectProperty<Color>(
			"lineColor", this, TRANSPARENT, false);

	private ObjectProperty<Double> lineWidth = new ObjectProperty<Double>(
			"lineWidth", this, 1.0, true);

	private ObjectProperty<Shape> shape;

	public ShapeElement(Shape shape) {
		this.shape = new ObjectProperty<Shape>("shape", this, shape, true);
	}

	@Override
	protected void drawElement(Graphics2D g) {
		Color oldColor = g.getColor();
		Stroke oldStroke = g.getStroke();
		if (getColor().getAlpha() != 0) {
			g.setColor(getColor());
			g.fill(getShape());
		}
		if (isStroked()) {
			g.setColor(getLineColor());
			g.setStroke(buildStroke(getLineWidth()));
			g.draw(getShape());
		}
		g.setColor(oldColor);
		g.setStroke(oldStroke);
	}

	private boolean isStroked() {
		return getLineColor().getAlpha() != 0;
	}

	private static Stroke buildStroke(double width) {
		return new BasicStroke((float) width, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
	}

	@Override
	public Dimension2D getPreferredSize() {
		Rectangle2D r = getShape().getBounds2D();
		double extra = isStroked() ? getLineWidth() / 2 : 0;
		return new DoubleDimensions(Math.max(0, r.getMaxX() + extra),
				Math.max(0, r.getMaxY() + extra));
	}

	@Override
	public boolean contains(Point2D point, double tolerance) {
		Rectangle2D b = getBounds();
		Point2D local = new Point2D.Double(point.getX() - b.getMinX(),
				point.getY() - b.getMinY());
		if (getColor().getAlpha() != 0 && getShape().contains(local))
			return true;
		double w = (isStroked() ? getLineWidth() : 0) + 2 * tolerance;
		return w > 0 && buildStroke(w).createStrokedShape(getShape()).contains(local);
	}

	/// The fill color (transparent for no fill).
	public Color getColor() {
		return color.getValue();
	}

	/// Sets the fill color (use a transparent color for no fill).
	public void setColor(Color fillColor) {
		this.color.setValue(fillColor);
	}

	/// The color used to stroke the shape outline (transparent for no outline).
	public Color getLineColor() {
		return lineColor.getValue();
	}

	public void setLineColor(Color lineColor) {
		this.lineColor.setValue(lineColor);
	}

	public double getLineWidth() {
		return lineWidth.getValue();
	}

	public void setLineWidth(double lineWidth) {
		this.lineWidth.setValue(lineWidth);
	}

	public Shape getShape() {
		return shape.getValue();
	}

	public void setShape(Shape newShape) {
		shape.setValue(newShape);
	}

}

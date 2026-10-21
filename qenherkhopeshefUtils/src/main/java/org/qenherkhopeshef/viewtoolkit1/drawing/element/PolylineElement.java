package org.qenherkhopeshef.viewtoolkit.drawing.element;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.qenherkhopeshef.swingUtils.DoubleDimensions;
import org.qenherkhopeshef.viewtoolkit.drawing.element.property.ObjectProperty;
import org.qenherkhopeshef.viewtoolkit.drawing.event.PropertyChangedEvent;
import org.qenherkhopeshef.viewtoolkit.drawing.element.property.Property;

/// A line made of connected segments, either open (a polyline) or closed (a
/// polygon).
///
/// Points are given and returned in the coordinates of the element's parent
/// (usually, the drawing). Internally, they are kept relative to the element
/// origin, which is maintained at the top-left of the points' bounding box
/// (minus room for the line width), so that [#getBounds()] always tightly
/// covers the drawn line.
///
/// A closed polyline can be filled with [#setFillColor(Color)].
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public class PolylineElement extends GraphicalElement {

	private static final Color TRANSPARENT = new Color(0, 0, 0, 0);

	/// Pseudo-property used to signal a change in the points.
	private static final Property POINTS_PROPERTY = new Property("points", true);

	/// Points, relative to the element origin.
	private final List<Point2D> localPoints = new ArrayList<>();

	private Dimension2D size = new DoubleDimensions(0, 0);

	private final ObjectProperty<Boolean> closed;

	private final ObjectProperty<Color> lineColor = new ObjectProperty<Color>(
			"lineColor", this, Color.BLACK, false);

	private final ObjectProperty<Double> lineWidth = new ObjectProperty<Double>(
			"lineWidth", this, 1.0, true);

	private final ObjectProperty<Color> fillColor = new ObjectProperty<Color>(
			"fillColor", this, TRANSPARENT, false);

	/// Builds a polyline.
	///
	/// @param points the points, in the parent's coordinates.
	/// @param closed true for a polygon.
	public PolylineElement(List<? extends Point2D> points, boolean closed) {
		this.closed = new ObjectProperty<Boolean>("closed", this, closed, false);
		setPoints(points);
	}

	public PolylineElement(boolean closed) {
		this(Collections.<Point2D>emptyList(), closed);
	}

	@Override
	protected void drawElement(Graphics2D g) {
		if (localPoints.isEmpty())
			return;
		Path2D path = buildPath();
		if (isClosed() && getFillColor().getAlpha() != 0) {
			g.setColor(getFillColor());
			g.fill(path);
		}
		g.setColor(getLineColor());
		g.setStroke(new BasicStroke((float) getLineWidth(),
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		if (localPoints.size() == 1) {
			// A single point: draw a dot.
			Point2D p = localPoints.get(0);
			g.draw(new Line2D.Double(p, p));
		} else {
			g.draw(path);
		}
	}

	private Path2D buildPath() {
		Path2D path = new Path2D.Double();
		boolean first = true;
		for (Point2D p : localPoints) {
			if (first)
				path.moveTo(p.getX(), p.getY());
			else
				path.lineTo(p.getX(), p.getY());
			first = false;
		}
		if (isClosed())
			path.closePath();
		return path;
	}

	@Override
	public Dimension2D getPreferredSize() {
		return size;
	}

	/// Hit test: is the point near one of the segments (or inside the shape, if
	/// it's closed and filled) ?
	@Override
	public boolean contains(Point2D point, double tolerance) {
		Rectangle2D b = getBounds();
		double x = point.getX() - b.getMinX();
		double y = point.getY() - b.getMinY();
		double maxDistance = tolerance + getLineWidth() / 2;
		int n = localPoints.size();
		if (n == 1)
			return localPoints.get(0).distance(x, y) <= maxDistance;
		int segments = isClosed() ? n : n - 1;
		for (int i = 0; i < segments; i++) {
			Point2D a = localPoints.get(i);
			Point2D c = localPoints.get((i + 1) % n);
			if (Line2D.ptSegDist(a.getX(), a.getY(), c.getX(), c.getY(), x, y) <= maxDistance)
				return true;
		}
		return isClosed() && getFillColor().getAlpha() != 0 && n > 2
				&& buildPath().contains(x, y);
	}

	public int getPointCount() {
		return localPoints.size();
	}

	/// Returns a point, in the parent's coordinates.
	public Point2D getPoint(int i) {
		Rectangle2D b = getBounds();
		Point2D p = localPoints.get(i);
		return new Point2D.Double(p.getX() + b.getMinX(), p.getY() + b.getMinY());
	}

	/// Returns a copy of the points, in the parent's coordinates.
	public List<Point2D> getPoints() {
		List<Point2D> result = new ArrayList<>(localPoints.size());
		for (int i = 0; i < localPoints.size(); i++)
			result.add(getPoint(i));
		return result;
	}

	/// Replaces all points (given in the parent's coordinates).
	public final void setPoints(List<? extends Point2D> points) {
		normalize(new ArrayList<Point2D>(points));
	}

	/// Moves one point (coordinates in the parent's system).
	public void setPoint(int i, double x, double y) {
		List<Point2D> points = getPoints();
		points.set(i, new Point2D.Double(x, y));
		normalize(points);
	}

	public void addPoint(double x, double y) {
		List<Point2D> points = getPoints();
		points.add(new Point2D.Double(x, y));
		normalize(points);
	}

	/// Removes the last point, if any.
	public void removeLastPoint() {
		List<Point2D> points = getPoints();
		if (!points.isEmpty()) {
			points.remove(points.size() - 1);
			normalize(points);
		}
	}

	/// Moves the whole line.
	public void translate(double dx, double dy) {
		Rectangle2D b = getBounds();
		setOrigin(b.getMinX() + dx, b.getMinY() + dy);
	}

	/// Recomputes the origin and the local points from absolute points.
	private void normalize(List<Point2D> absolutePoints) {
		localPoints.clear();
		if (absolutePoints.isEmpty()) {
			size = new DoubleDimensions(0, 0);
		} else {
			double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
			double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
			for (Point2D p : absolutePoints) {
				minX = Math.min(minX, p.getX());
				minY = Math.min(minY, p.getY());
				maxX = Math.max(maxX, p.getX());
				maxY = Math.max(maxY, p.getY());
			}
			// Round joins and caps don't go further than half the line width.
			double pad = getLineWidth() / 2 + 1;
			double ox = minX - pad, oy = minY - pad;
			for (Point2D p : absolutePoints)
				localPoints.add(new Point2D.Double(p.getX() - ox, p.getY() - oy));
			size = new DoubleDimensions(maxX - minX + 2 * pad, maxY - minY + 2 * pad);
			setOrigin(ox, oy);
		}
		fireDrawingEvent(new PropertyChangedEvent(this, POINTS_PROPERTY));
	}

	public boolean isClosed() {
		return closed.getValue();
	}

	public void setClosed(boolean closed) {
		this.closed.setValue(closed);
	}

	public Color getLineColor() {
		return lineColor.getValue();
	}

	public void setLineColor(Color color) {
		lineColor.setValue(color);
	}

	public double getLineWidth() {
		return lineWidth.getValue();
	}

	/// Sets the line width. As it changes the room needed around the points,
	/// the element origin is recomputed.
	public void setLineWidth(double width) {
		List<Point2D> points = getPoints();
		lineWidth.setValue(width);
		normalize(points);
	}

	/// Fill color, used only for closed lines. Transparent means "not filled".
	public Color getFillColor() {
		return fillColor.getValue();
	}

	public void setFillColor(Color color) {
		fillColor.setValue(color);
	}
}

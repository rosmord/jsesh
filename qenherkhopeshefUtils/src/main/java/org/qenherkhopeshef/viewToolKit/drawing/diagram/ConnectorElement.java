package org.qenherkhopeshef.viewToolKit.drawing.diagram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Dimension2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.qenherkhopeshef.swingUtils.DoubleDimensions;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.Margin;
import org.qenherkhopeshef.viewToolKit.drawing.element.property.ObjectProperty;
import org.qenherkhopeshef.viewToolKit.drawing.element.property.Property;
import org.qenherkhopeshef.viewToolKit.drawing.event.PropertyChangedEvent;

/// A line linking two elements (its *source* and its *target*).
///
/// The line goes from the center of one element to the center of the other,
/// and is cut at their borders. An element linked to itself gets a small loop
/// on its top-right corner. Each end can be decorated (see [ConnectorEnd]),
/// and the connector can carry three labels: one in its middle, and one near
/// each end (for instance, UML multiplicities).
///
/// The connector computes its geometry from the current bounds of its ends
/// in [#updateGeometry()]. In a [DiagramDrawing], this method is called
/// automatically each time one of the ends changes. Both ends are supposed to
/// be in the same coordinate system as the connector.
///
/// @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
public class ConnectorElement extends GraphicalElement {

	private static final Property ROUTE_PROPERTY = new Property("route", true);

	/// Room left around the line for its decorations.
	private static final double DECORATION_SIZE = 18;

	private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

	private final GraphicalElement source;

	private final GraphicalElement target;

	private final ObjectProperty<ConnectorEnd> sourceEnd = new ObjectProperty<ConnectorEnd>(
			"sourceEnd", this, ConnectorEnd.NONE, false);

	private final ObjectProperty<ConnectorEnd> targetEnd = new ObjectProperty<ConnectorEnd>(
			"targetEnd", this, ConnectorEnd.NONE, false);

	private final ObjectProperty<Boolean> dashed = new ObjectProperty<Boolean>(
			"dashed", this, false, false);

	private final ObjectProperty<Color> color = new ObjectProperty<Color>(
			"color", this, Color.BLACK, false);

	private final ObjectProperty<String> label = new ObjectProperty<String>(
			"label", this, "", true);

	private final ObjectProperty<String> sourceLabel = new ObjectProperty<String>(
			"sourceLabel", this, "", true);

	private final ObjectProperty<String> targetLabel = new ObjectProperty<String>(
			"targetLabel", this, "", true);

	private Font font = new Font("SansSerif", Font.PLAIN, 11);

	/// Sideways shift of the line (see [#setOffset(double)]).
	private double offset = 0;

	/// The route, relative to the element origin.
	private List<Point2D> localRoute = new ArrayList<>();

	/// Label anchors (centers of the labels), relative to the element origin.
	private Point2D labelCenter, sourceLabelCenter, targetLabelCenter;

	private Dimension2D size = new DoubleDimensions(0, 0);

	public ConnectorElement(GraphicalElement source, GraphicalElement target) {
		this.source = source;
		this.target = target;
		setMargin(Margin.NONE);
		updateGeometry();
	}

	public GraphicalElement getSource() {
		return source;
	}

	public GraphicalElement getTarget() {
		return target;
	}

	/// Recomputes the connector route from the bounds of its ends.
	public void updateGeometry() {
		List<Point2D> route = computeRoute();
		Rectangle2D box = new Rectangle2D.Double(route.get(0).getX(), route.get(0).getY(), 0, 0);
		for (Point2D p : route)
			box.add(p);
		box = new Rectangle2D.Double(box.getMinX() - DECORATION_SIZE, box.getMinY() - DECORATION_SIZE,
				box.getWidth() + 2 * DECORATION_SIZE, box.getHeight() + 2 * DECORATION_SIZE);

		// Labels.
		int n = route.size();
		int middle = (n - 1) / 2;
		Point2D mid = midPoint(route.get(middle), route.get(middle + 1));
		Point2D absLabel = placeLabel(getLabel(), mid, route.get(middle), route.get(middle + 1), 4, box);
		Point2D absSource = placeLabel(getSourceLabel(), along(route.get(0), route.get(1), 20),
				route.get(0), route.get(1), 6, box);
		Point2D absTarget = placeLabel(getTargetLabel(), along(route.get(n - 1), route.get(n - 2), 20),
				route.get(n - 2), route.get(n - 1), 6, box);

		double ox = box.getMinX(), oy = box.getMinY();
		localRoute = new ArrayList<>();
		for (Point2D p : route)
			localRoute.add(new Point2D.Double(p.getX() - ox, p.getY() - oy));
		labelCenter = toLocal(absLabel, ox, oy);
		sourceLabelCenter = toLocal(absSource, ox, oy);
		targetLabelCenter = toLocal(absTarget, ox, oy);
		size = new DoubleDimensions(box.getWidth(), box.getHeight());
		setOrigin(ox, oy);
		fireDrawingEvent(new PropertyChangedEvent(this, ROUTE_PROPERTY));
	}

	private static Point2D toLocal(Point2D p, double ox, double oy) {
		return p == null ? null : new Point2D.Double(p.getX() - ox, p.getY() - oy);
	}

	private List<Point2D> computeRoute() {
		Rectangle2D a = source.getBounds();
		List<Point2D> route = new ArrayList<>();
		if (source == target) {
			double dx = Math.min(30, a.getWidth() / 3);
			double dy = Math.min(30, a.getHeight() / 3);
			double loop = 25;
			route.add(new Point2D.Double(a.getMaxX() - dx, a.getMinY()));
			route.add(new Point2D.Double(a.getMaxX() - dx, a.getMinY() - loop));
			route.add(new Point2D.Double(a.getMaxX() + loop, a.getMinY() - loop));
			route.add(new Point2D.Double(a.getMaxX() + loop, a.getMinY() + dy));
			route.add(new Point2D.Double(a.getMaxX(), a.getMinY() + dy));
		} else {
			Rectangle2D b = target.getBounds();
			Point2D ca = new Point2D.Double(a.getCenterX(), a.getCenterY());
			Point2D cb = new Point2D.Double(b.getCenterX(), b.getCenterY());
			if (offset != 0) {
				// Shift the line sideways (see setOffset), but keep its ends
				// inside the elements.
				double len = ca.distance(cb);
				if (len > 0) {
					double nx = -(cb.getY() - ca.getY()) / len * offset;
					double ny = (cb.getX() - ca.getX()) / len * offset;
					ca = insideOf(a, ca.getX() + nx, ca.getY() + ny);
					cb = insideOf(b, cb.getX() + nx, cb.getY() + ny);
				}
			}
			route.add(clip(a, ca, cb));
			route.add(clip(b, cb, ca));
		}
		return route;
	}

	private static Point2D insideOf(Rectangle2D r, double x, double y) {
		return new Point2D.Double(Math.max(r.getMinX() + 1, Math.min(r.getMaxX() - 1, x)),
				Math.max(r.getMinY() + 1, Math.min(r.getMaxY() - 1, y)));
	}

	/// Intersection of the segment [center, towards] with the border of rect
	/// (center being the center of rect).
	private static Point2D clip(Rectangle2D rect, Point2D center, Point2D towards) {
		double dx = towards.getX() - center.getX();
		double dy = towards.getY() - center.getY();
		if (dx == 0 && dy == 0)
			return center;
		double tx = dx == 0 ? Double.POSITIVE_INFINITY : (rect.getWidth() / 2) / Math.abs(dx);
		double ty = dy == 0 ? Double.POSITIVE_INFINITY : (rect.getHeight() / 2) / Math.abs(dy);
		double t = Math.min(1, Math.min(tx, ty));
		return new Point2D.Double(center.getX() + t * dx, center.getY() + t * dy);
	}

	private static Point2D midPoint(Point2D a, Point2D b) {
		return new Point2D.Double((a.getX() + b.getX()) / 2, (a.getY() + b.getY()) / 2);
	}

	/// The point at a given distance from a, towards b.
	private static Point2D along(Point2D a, Point2D b, double distance) {
		double len = a.distance(b);
		if (len == 0)
			return a;
		double t = Math.min(distance, len / 2) / len;
		return new Point2D.Double(a.getX() + t * (b.getX() - a.getX()), a.getY() + t * (b.getY() - a.getY()));
	}

	/// Computes the center of a label placed beside the segment (a,b), near
	/// anchor, and enlarges box so that it contains the label.
	///
	/// @return the label center, or null for an empty label.
	private Point2D placeLabel(String text, Point2D anchor, Point2D a, Point2D b, double gap, Rectangle2D box) {
		if (text.isEmpty())
			return null;
		Rectangle2D textBounds = font.getStringBounds(text, FRC);
		double dx = b.getX() - a.getX(), dy = b.getY() - a.getY();
		double len = Math.hypot(dx, dy);
		// Normal to the segment, pointing upwards (or rightwards for a
		// horizontal normal).
		double nx = len == 0 ? 0 : -dy / len;
		double ny = len == 0 ? -1 : dx / len;
		if (ny > 0 || (ny == 0 && nx < 0)) {
			nx = -nx;
			ny = -ny;
		}
		// Distance from the line to the label center, so that the label box
		// doesn't touch the line.
		double distance = gap + Math.abs(nx) * textBounds.getWidth() / 2
				+ Math.abs(ny) * textBounds.getHeight() / 2;
		Point2D center = new Point2D.Double(anchor.getX() + nx * distance, anchor.getY() + ny * distance);
		box.add(new Rectangle2D.Double(center.getX() - textBounds.getWidth() / 2 - 1,
				center.getY() - textBounds.getHeight() / 2 - 1, textBounds.getWidth() + 2,
				textBounds.getHeight() + 2));
		return center;
	}

	@Override
	protected void drawElement(Graphics2D g) {
		if (localRoute.size() < 2)
			return;
		g.setColor(getColor());
		if (isDashed()) {
			g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
					new float[] { 6f, 4f }, 0f));
		} else {
			g.setStroke(new BasicStroke(1f));
		}
		Path2D path = new Path2D.Double();
		path.moveTo(localRoute.get(0).getX(), localRoute.get(0).getY());
		for (int i = 1; i < localRoute.size(); i++)
			path.lineTo(localRoute.get(i).getX(), localRoute.get(i).getY());
		g.draw(path);
		g.setStroke(new BasicStroke(1f));
		int n = localRoute.size();
		drawEnd(g, getSourceEnd(), localRoute.get(0), localRoute.get(1));
		drawEnd(g, getTargetEnd(), localRoute.get(n - 1), localRoute.get(n - 2));
		g.setFont(font);
		drawLabel(g, getLabel(), labelCenter);
		drawLabel(g, getSourceLabel(), sourceLabelCenter);
		drawLabel(g, getTargetLabel(), targetLabelCenter);
	}

	/// Draws an end decoration.
	///
	/// Hollow decorations are filled in white, so that they hide the line.
	private void drawEnd(Graphics2D g, ConnectorEnd end, Point2D tip, Point2D from) {
		double len = tip.distance(from);
		if (end == ConnectorEnd.NONE || len == 0)
			return;
		double ux = (tip.getX() - from.getX()) / len, uy = (tip.getY() - from.getY()) / len;
		double vx = -uy, vy = ux;
		Path2D p = new Path2D.Double();
		switch (end) {
		case OPEN_ARROW:
			p.moveTo(tip.getX() - 12 * ux + 6 * vx, tip.getY() - 12 * uy + 6 * vy);
			p.lineTo(tip.getX(), tip.getY());
			p.lineTo(tip.getX() - 12 * ux - 6 * vx, tip.getY() - 12 * uy - 6 * vy);
			g.draw(p);
			return;
		case HOLLOW_TRIANGLE:
			p.moveTo(tip.getX(), tip.getY());
			p.lineTo(tip.getX() - 14 * ux + 8 * vx, tip.getY() - 14 * uy + 8 * vy);
			p.lineTo(tip.getX() - 14 * ux - 8 * vx, tip.getY() - 14 * uy - 8 * vy);
			p.closePath();
			break;
		case HOLLOW_DIAMOND:
		case FILLED_DIAMOND:
			p.moveTo(tip.getX(), tip.getY());
			p.lineTo(tip.getX() - 9 * ux + 5.5 * vx, tip.getY() - 9 * uy + 5.5 * vy);
			p.lineTo(tip.getX() - 18 * ux, tip.getY() - 18 * uy);
			p.lineTo(tip.getX() - 9 * ux - 5.5 * vx, tip.getY() - 9 * uy - 5.5 * vy);
			p.closePath();
			break;
		default:
			return;
		}
		g.setColor(end == ConnectorEnd.FILLED_DIAMOND ? getColor() : Color.WHITE);
		g.fill(p);
		g.setColor(getColor());
		g.draw(p);
	}

	private void drawLabel(Graphics2D g, String text, Point2D center) {
		if (center == null || text.isEmpty())
			return;
		Rectangle2D textBounds = font.getStringBounds(text, FRC);
		LineMetrics metrics = font.getLineMetrics(text, FRC);
		float x = (float) (center.getX() - textBounds.getWidth() / 2);
		float y = (float) (center.getY() - textBounds.getHeight() / 2 + metrics.getAscent());
		g.drawString(text, x, y);
	}

	@Override
	public Dimension2D getPreferredSize() {
		return size;
	}

	/// True if the point is near the line (labels and decorations are ignored).
	@Override
	public boolean contains(Point2D point, double tolerance) {
		Rectangle2D b = getBounds();
		double x = point.getX() - b.getMinX(), y = point.getY() - b.getMinY();
		for (int i = 0; i + 1 < localRoute.size(); i++) {
			Point2D p = localRoute.get(i), q = localRoute.get(i + 1);
			if (Line2D.ptSegDist(p.getX(), p.getY(), q.getX(), q.getY(), x, y) <= tolerance + 1)
				return true;
		}
		return false;
	}

	public ConnectorEnd getSourceEnd() {
		return sourceEnd.getValue();
	}

	public void setSourceEnd(ConnectorEnd end) {
		sourceEnd.setValue(end);
	}

	public ConnectorEnd getTargetEnd() {
		return targetEnd.getValue();
	}

	public void setTargetEnd(ConnectorEnd end) {
		targetEnd.setValue(end);
	}

	public boolean isDashed() {
		return dashed.getValue();
	}

	public void setDashed(boolean value) {
		dashed.setValue(value);
	}

	public Color getColor() {
		return color.getValue();
	}

	public void setColor(Color value) {
		color.setValue(value);
	}

	/// The label drawn in the middle of the connector.
	public String getLabel() {
		return label.getValue();
	}

	public void setLabel(String text) {
		label.setValue(text);
		updateGeometry();
	}

	/// The label drawn near the source end.
	public String getSourceLabel() {
		return sourceLabel.getValue();
	}

	public void setSourceLabel(String text) {
		sourceLabel.setValue(text);
		updateGeometry();
	}

	/// The label drawn near the target end.
	public String getTargetLabel() {
		return targetLabel.getValue();
	}

	public void setTargetLabel(String text) {
		targetLabel.setValue(text);
		updateGeometry();
	}

	/// The sideways shift of the line.
	public double getOffset() {
		return offset;
	}

	/// Shifts the line sideways, so that several connectors between the same
	/// elements don't overlap.
	///
	/// A positive offset moves the line to the left of the direction from the
	/// source to the target (in screen coordinates, y going down). Ignored for
	/// self connectors. [DiagramDrawing] sets it automatically.
	public void setOffset(double offset) {
		if (this.offset != offset) {
			this.offset = offset;
			updateGeometry();
		}
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
		updateGeometry();
	}

	@Override
	public String toString() {
		return "[connector " + source + " -> " + target + "]";
	}
}

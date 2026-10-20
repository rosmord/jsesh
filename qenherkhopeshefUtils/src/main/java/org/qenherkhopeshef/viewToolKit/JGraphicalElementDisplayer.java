package org.qenherkhopeshef.viewToolKit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.qenherkhopeshef.guiFramework.busy.BusyInterface;
import org.qenherkhopeshef.swingUtils.GraphicsUtils;
import org.qenherkhopeshef.viewToolKit.drawing.Drawing;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.event.CursorChangedEvent;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingEvent;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingEventAdapter;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingListener;

/// Basic Displayer for a drawing.
///
/// The displayer only draws the elements which intersect the visible area (it
/// asks the drawing for them with [Drawing#getElementsInZone(Rectangle2D)]), so
/// it can display very large drawings, provided the drawing itself is able to
/// answer this question efficiently.
///
/// Elements are drawn by increasing layer, then decorations are drawn on top.
///
/// The displayer can scale the drawing ([#setScale(double)]). Mouse
/// coordinates must then be converted with [#toDrawingPoint(Point)] before
/// being passed to the drawing.
///
/// It implements [Scrollable] so that it behaves well in a `JScrollPane`;
/// with [#setTracksViewportWidth(boolean)], its width follows the viewport (a
/// text drawing can then use it as its line width).
///
/// @author rosmord
@SuppressWarnings("serial")
public class JGraphicalElementDisplayer<T extends Drawing> extends JPanel implements Scrollable {

	T drawing;

	private BusyInterface busyInterface = null;

	boolean busy = false;

	/**
	 * Possible editor used.
	 */
	private JComponent currentEditor = null;

	private final GDrawingListener drawingListener = new GDrawingListener();

	private int rightMargin = 10;

	private int bottomMargin = 10;

	private double scale = 1.0;

	private int unitIncrement = 16;

	private boolean tracksViewportWidth = false;

	public JGraphicalElementDisplayer(T drawing) {
		setRequestFocusEnabled(true);
		// setFocusable(true);
		setBackground(Color.WHITE);
		setDrawing(drawing);
		setLayout(null);
	}

	@Override
	public Dimension getPreferredSize() {
		if (drawing == null) {
			return new Dimension(640, 480);
		}
		Rectangle2D rec = drawing.getPreferredSize();
		if (rec == null || drawing.isEmpty() && rec.getWidth() == 0 && rec.getHeight() == 0) {
			return new Dimension(640, 480);
		} else {
			return new Dimension((int) Math.ceil(rec.getWidth() * scale) + 2
					+ rightMargin, (int) Math.ceil(rec.getHeight() * scale) + 2
					+ bottomMargin);
		}
	}

	public final void setDrawing(T newDrawing) {
		if (drawing != newDrawing) {
			if (drawing != null)
				drawing.remove(drawingListener);
			this.drawing = newDrawing;
			if (drawing != null)
				drawing.addDrawingListener(drawingListener);
			invalidate();
			repaint();
			revalidate();
			showCursor();
		}
	}

	/**
	 * The underlying model.
	 *
	 * @return
	 */
	public T getDrawing() {
		return drawing;
	}

	/// The scale used to display the drawing (1 means one drawing unit per pixel).
	public double getScale() {
		return scale;
	}

	/// Sets the display scale.
	public void setScale(double scale) {
		if (scale <= 0)
			throw new IllegalArgumentException("scale must be positive");
		this.scale = scale;
		invalidate();
		revalidate();
		repaint();
	}

	/// Converts a point in component coordinates (e.g. a mouse position) into
	/// drawing coordinates.
	public Point2D toDrawingPoint(Point point) {
		return new Point2D.Double(point.getX() / scale, point.getY() / scale);
	}

	/// Converts a rectangle in drawing coordinates into component coordinates.
	public Rectangle toComponentRectangle(Rectangle2D r) {
		int x = (int) Math.floor(r.getMinX() * scale);
		int y = (int) Math.floor(r.getMinY() * scale);
		int maxX = (int) Math.ceil(r.getMaxX() * scale);
		int maxY = (int) Math.ceil(r.getMaxY() * scale);
		return new Rectangle(x, y, maxX - x, maxY - y);
	}

	/// Converts a rectangle in component coordinates into drawing coordinates.
	public Rectangle2D toDrawingRectangle(Rectangle r) {
		return new Rectangle2D.Double(r.getX() / scale, r.getY() / scale,
				r.getWidth() / scale, r.getHeight() / scale);
	}

	/// Returns the elements at a given point, in component coordinates.
	public Collection<GraphicalElement> getElementsAt(Point point) {
		return getElementsAt(point.x, point.y);
	}

	private class GDrawingListener extends DrawingEventAdapter implements
			DrawingListener {

        @Override
		public void drawingChanged(DrawingEvent e) {
			e.accept(this);
		}

		/**
		 * Default behaviour when drawing has changed.
		 */
		@Override
		public void visitDefault(DrawingEvent ev) {
			// Do something for dimension changes ?
			invalidate();
			repaint();
			revalidate();
		}

		@Override
		public void visitCursorChangedEvent(
				CursorChangedEvent cursorChangedEvent) {
			visitDefault(cursorChangedEvent);
			Rectangle2D cursorBounds = drawing.getCursorBounds();
			if (cursorBounds != null)
				showCursor();
		}
	}

	/**
	 * Marks this object as "busy". Delegates the busy-ness to a possible
	 * busyInterface object (which might be null).
	 *
	 * @param b
	 */
	public void setBusy(boolean b) {
		if (busyInterface != null)
			busyInterface.setBuzy(b);
		repaint();
	}

	/// Scrolls the display so that the cursor (if any) is visible.
	///
	/// The actual scrolling is done later, after the component has been
	/// re-validated.
	public void showCursor() {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
				if (drawing == null)
					return;
				Rectangle2D cursorBounds = drawing.getCursorBounds();
				if (cursorBounds != null) {
					// Make sure our size is up to date before scrolling.
					if (getParent() != null)
						getParent().validate();
					scrollRectToVisible(toComponentRectangle(cursorBounds));
				}
			}
		});
	}

	/**
	 * Marks the advancement of a lenghty operation on this object. Delegates
	 * the busy-ness to a possible busyInterface object (which might be null).
	 *
	 * @param percent
	 */
	public void setAdvance(int percent) {
		if (busyInterface != null)
			busyInterface.setAdvance(percent);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (drawing == null)
			return;
		Graphics2D g2d = (Graphics2D) g.create();
		GraphicsUtils.antialias(g2d);
		g2d.scale(scale, scale);
		// Clip bounds are expressed in the scaled (that is, drawing) system.
		Rectangle clipBounds = g2d.getClipBounds();
		if (clipBounds == null)
			clipBounds = toComponentRectangle(drawing.getPreferredSize());
		Rectangle2D enlarged = new Rectangle2D.Double(clipBounds.getMinX() - 1,
				clipBounds.getMinY() - 1, clipBounds.getWidth() + 2,
				clipBounds.getHeight() + 2);
		List<GraphicalElement> graphics = new ArrayList<GraphicalElement>(
				drawing.getElementsInZone(enlarged));
		// Stable sort: drawings may already give a meaningful order inside a layer.
		Collections.sort(graphics, Comparator.comparingInt(GraphicalElement::getLayer));
		for (GraphicalElement elt : graphics) {
			elt.draw(g2d);
		}

		for (GraphicalElement elt : drawing.getDecorationsInZone(enlarged)) {
			elt.draw(g2d);
		}

		if (currentEditor != null) {
			currentEditor.validate();
			currentEditor.paint(g2d);
		}

		g2d.dispose();
		// paintComponents(g); // Not sure that this is really good ?
	}

	/**
	 * Choose a system which will be used to block the interface when this
	 * object is rendering the text.
	 *
	 * @param busyInterface
	 */
	public void setBusyInterface(BusyInterface busyInterface) {
		this.busyInterface = busyInterface;
	}

	public void setCurrentEditor(JComponent currentEditor) {
		this.currentEditor = currentEditor;
		repaint();
	}

	/// Returns the elements at a given point, in component coordinates.
	public Collection<GraphicalElement> getElementsAt(int x, int y) {
		if (drawing == null)
			return Collections.emptyList();
		return drawing.getElementsAt(toDrawingPoint(new Point(x, y)));
	}

	/// Sets the number of pixels scrolled by one click on a scrollbar arrow
	/// (or one mouse wheel notch).
	public void setUnitIncrement(int unitIncrement) {
		this.unitIncrement = unitIncrement;
	}

	/// If true, the displayer width will follow the width of its enclosing
	/// viewport (no horizontal scrolling).
	public void setTracksViewportWidth(boolean tracksViewportWidth) {
		this.tracksViewportWidth = tracksViewportWidth;
		revalidate();
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return unitIncrement;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		int extent = orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
		return Math.max(unitIncrement, extent - unitIncrement);
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		// Also fill the viewport when the drawing is narrower than it.
		return tracksViewportWidth
				|| getParent() != null && getParent().getWidth() > getPreferredSize().width;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		// Fill the viewport when the drawing is smaller than it.
		return getParent() != null && getParent().getHeight() > getPreferredSize().height;
	}

}

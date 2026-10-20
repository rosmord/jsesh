package org.qenherkhopeshef.viewToolKit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Scrollable;

import org.qenherkhopeshef.guiFramework.busy.BusyInterface;
import org.qenherkhopeshef.viewToolKit.drawing.Drawing;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;

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
/// All the work is done by a [GraphicalElementDisplayerDelegate]. Use that
/// class directly if you would rather not inherit from this one.
///
/// @author rosmord
@SuppressWarnings("serial")
public class JGraphicalElementDisplayer<T extends Drawing> extends JPanel implements Scrollable {

	private final GraphicalElementDisplayerDelegate<T> delegate;

	public JGraphicalElementDisplayer(T drawing) {
		setRequestFocusEnabled(true);
		setBackground(Color.WHITE);
		delegate = new GraphicalElementDisplayerDelegate<>(this, drawing);
		setLayout(null);
	}

	@Override
	public Dimension getPreferredSize() {
		// May be called by the superclass constructor, before the delegate exists.
		if (delegate == null)
			return super.getPreferredSize();
		return delegate.getPreferredSize();
	}

	public final void setDrawing(T newDrawing) {
		delegate.setDrawing(newDrawing);
	}

	/// The underlying model.
	public T getDrawing() {
		return delegate.getDrawing();
	}

	/// The scale used to display the drawing (1 means one drawing unit per pixel).
	public double getScale() {
		return delegate.getScale();
	}

	/// Sets the display scale.
	public void setScale(double scale) {
		delegate.setScale(scale);
	}

	/// Converts a point in component coordinates (e.g. a mouse position) into
	/// drawing coordinates.
	public Point2D toDrawingPoint(Point point) {
		return delegate.toDrawingPoint(point);
	}

	/// Converts a rectangle in drawing coordinates into component coordinates.
	public Rectangle toComponentRectangle(Rectangle2D r) {
		return delegate.toComponentRectangle(r);
	}

	/// Converts a rectangle in component coordinates into drawing coordinates.
	public Rectangle2D toDrawingRectangle(Rectangle r) {
		return delegate.toDrawingRectangle(r);
	}

	/// Returns the elements at a given point, in component coordinates.
	public Collection<GraphicalElement> getElementsAt(Point point) {
		return delegate.getElementsAt(point);
	}

	/// Returns the elements at a given point, in component coordinates.
	public Collection<GraphicalElement> getElementsAt(int x, int y) {
		return delegate.getElementsAt(x, y);
	}

	/// Marks this object as "busy". Delegates the busy-ness to a possible
	/// busyInterface object (which might be null).
	public void setBusy(boolean b) {
		delegate.setBusy(b);
	}

	/// Scrolls the display so that the cursor (if any) is visible.
	///
	/// The actual scrolling is done later, after the component has been
	/// re-validated.
	public void showCursor() {
		delegate.showCursor();
	}

	/// Marks the advancement of a lengthy operation on this object. Delegates
	/// the busy-ness to a possible busyInterface object (which might be null).
	public void setAdvance(int percent) {
		delegate.setAdvance(percent);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		delegate.paint(g);
	}

	/// Chooses a system which will be used to block the interface when this
	/// object is rendering the text.
	public void setBusyInterface(BusyInterface busyInterface) {
		delegate.setBusyInterface(busyInterface);
	}

	public void setCurrentEditor(JComponent currentEditor) {
		delegate.setCurrentEditor(currentEditor);
	}

	/// Sets the number of pixels scrolled by one click on a scrollbar arrow
	/// (or one mouse wheel notch).
	public void setUnitIncrement(int unitIncrement) {
		delegate.setUnitIncrement(unitIncrement);
	}

	/// If true, the displayer width will follow the width of its enclosing
	/// viewport (no horizontal scrolling).
	public void setTracksViewportWidth(boolean tracksViewportWidth) {
		delegate.setTracksViewportWidth(tracksViewportWidth);
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return delegate.getPreferredScrollableViewportSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return delegate.getScrollableUnitIncrement(visibleRect, orientation, direction);
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return delegate.getScrollableBlockIncrement(visibleRect, orientation, direction);
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return delegate.getScrollableTracksViewportWidth();
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return delegate.getScrollableTracksViewportHeight();
	}

}

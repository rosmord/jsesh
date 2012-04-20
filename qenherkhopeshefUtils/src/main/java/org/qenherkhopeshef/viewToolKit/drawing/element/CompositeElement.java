package org.qenherkhopeshef.viewToolKit.drawing.element;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.qenherkhopeshef.swingUtils.DoubleDimensions;
import org.qenherkhopeshef.viewToolKit.drawing.event.CompositeEvent;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingEvent;
import org.qenherkhopeshef.viewToolKit.drawing.event.GraphicalElementManager;

/**
 * At the time being, a vertically arranged graphical element. Layout will be
 * separated from the composite in a next version.
 * 
 * @author rosmord
 * 
 */
public class CompositeElement extends GraphicalElement {

	/**
	 * Is this element being updated ? All methods which change children
	 * elements should set this method to true to avoid recursive modifications
	 * to the composite.
	 */
	private boolean inUpdate = false;

	private List<GraphicalElement> elements = new ArrayList<GraphicalElement>();
	// private HashMap<GraphicalElement, Alignment> alignmentMap = new
	// HashMap<GraphicalElement, Alignment>();
	private Dimension2D preferredSize;
	private CompositeContentManager contentManager = new CompositeContentManager();
	private CompositeAxis axis= CompositeAxis.VERTICAL;
	
	@Override
	public void drawElement(Graphics2D g) {
		for (GraphicalElement elt : this) {
			elt.draw(g);
		}
	}

	@Override
	public Dimension2D getPreferredSize() {
		if (preferredSize == null) {
			Rectangle2D rect = new Rectangle2D.Double();
			for (GraphicalElement elt : this) {
				rect.add(elt.getDecoratedBounds());
			}
			preferredSize = new DoubleDimensions(rect.getWidth(), rect
					.getHeight());
		}
		return preferredSize;
	}

	public void addElement(GraphicalElement elt) {
		elements.add(elt);
		elt.setManager(this.contentManager);
	}

	@Override
	public Iterator<GraphicalElement> iterator() {
		return elements.iterator();
	}

	/**
	 * Get inner elements which intersect a certain zone.
	 * 
	 * @param rectangle
	 *            a zone in the drawing.
	 * @return
	 */
	@Override
	public Collection<GraphicalElement> getElementsInZone(Rectangle2D rectangle) {
		double dx = getBounds().getMinX();
		double dy = getBounds().getMinY();
		// First express the rectangle into the composite's inner system:
		Rectangle2D newRect = new Rectangle2D.Double(rectangle.getMinX() - dx,
				rectangle.getMinY() - dy, rectangle.getWidth(), rectangle
						.getHeight());
		// Now we do some work. In this version, we consider that composite
		// contain few elements.
		Collection<GraphicalElement> result = new ArrayList<GraphicalElement>();
		for (GraphicalElement elt : this) {
			if (newRect.intersects(elt.getBounds()))
				result.add(elt);
		}
		return result;
	}

	@Override
	public void setBounds(double x, double y, double width, double height) {
		super.setBounds(x, y, width, height);
		boolean oldInUpdate = inUpdate;
		inUpdate = true;
		for (GraphicalElement elt : this) {
			Rectangle2D b = elt.getBounds();
			Margin m= elt.getMargin();
			elt.setBounds(b.getMinX(), b.getMinY(), width - m.getTotalMarginWidth(), b.getHeight());
		}
		fixDecoratorBounds();
		inUpdate = oldInUpdate;
	}

	/**
	 * Method which should be called after the last element has been added.
	 */
	public void pack() {
		boolean oldInUpdate = inUpdate;
		inUpdate = true;
		preferredSize = null;
		double w = 0;
		double y = 0;
		for (GraphicalElement elt : this) {
			Dimension2D pref = elt.getTotalPreferredSize();
			if (pref.getWidth() > w)
				w = pref.getWidth();
		}
		for (GraphicalElement elt : this) {
			Dimension2D pref = elt.getTotalPreferredSize();
			Margin m= elt.getMargin();
			elt.setOrigin(m.getLeft(), y+ m.getTop());
			y += pref.getHeight();
		}
		setBounds(getBounds().getMinX(), getBounds().getMinY(), w, y);
		inUpdate = oldInUpdate;
	}

	@Override
	public String toString() {
		return "[ composite " + elements + "]";
	}

	private class CompositeContentManager implements GraphicalElementManager {

		public void eventOccurred(DrawingEvent ev) {
			if (!inUpdate) {
				inUpdate = true;
				// Normally, tests that the geometry of the content of this
				// element has changed.
				preferredSize = null;
				pack();
				// Given the geometric nature of this element,
				// We should change its geometry here...

				// Now forward the event.
				CompositeEvent compositeEvent = new CompositeEvent(
						CompositeElement.this, ev);
				if (getManager() != null)
					getManager().eventOccurred(compositeEvent);
				inUpdate = false;
			}
		}
	}
	
	/**
	 * Sets the axis along which the composite will be packed.
	 * <p>This method does not pack the composite. Packing must 
	 * be called explicitly.
	 * <p>
	 * @param axis
	 */
	public void setAxis(CompositeAxis axis) {
		this.axis = axis;
	}
	
	/**
	 * Gets the composite axis. Default axis is {@link CompositeAxis#VERTICAL}.
	 * @return
	 */
	public CompositeAxis getAxis() {
		return axis;
	}
}

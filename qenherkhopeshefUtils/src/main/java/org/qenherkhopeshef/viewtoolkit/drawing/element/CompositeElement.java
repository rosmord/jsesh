package org.qenherkhopeshef.viewtoolkit.drawing.element;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.qenherkhopeshef.viewtoolkit.drawing.element.layout.CompositeLayout;
import org.qenherkhopeshef.viewtoolkit.drawing.element.layout.HorizontalCompositeLayout;
import org.qenherkhopeshef.viewtoolkit.drawing.element.layout.VerticalCompositeLayout;
import org.qenherkhopeshef.viewtoolkit.drawing.event.CompositeEvent;
import org.qenherkhopeshef.viewtoolkit.drawing.event.DrawingEvent;
import org.qenherkhopeshef.viewtoolkit.drawing.event.GraphicalElementManager;

/**
 * A general composite element.
 * <p> The element layout is decided by one of the available layout classes
 * (see {@link #setAxis(CompositeAxis)} and {@link #setLayout(CompositeLayout)}).
 * <p> <strong>Dynamics between layout and element size</strong> :
 * Basically, there are two ways for doing the layout. The size of the element might
 * be computed from its content, and the layout done, so to speak, <em>a mimima,</em>
 * or the layout might try to fill some existing space.
 * <p> {@link #pack()} does the first; {@link #setBounds(double, double, double, double)}
 * then stretches the children along the cross axis (their alignment decides
 * how they are drawn in the extra space).
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

	private final List<GraphicalElement> elements = new ArrayList<GraphicalElement>();

	private Dimension2D preferredSize;
    /**
     * Manages elements removal and all that.
     */
	private final CompositeContentManager contentManager = new CompositeContentManager();
    
	private CompositeAxis axis= CompositeAxis.VERTICAL;
	
    private CompositeLayout layout= new VerticalCompositeLayout();
    
	/// Draws the children which intersect the current clip.
	@Override
	public void drawElement(Graphics2D g) {
		Rectangle clip = g.getClipBounds();
		for (GraphicalElement elt : this) {
			if (clip == null || clip.intersects(elt.getDecoratedBounds()))
				elt.draw(g);
		}
	}

	@Override
	public Dimension2D getPreferredSize() {
		if (preferredSize == null) {
			preferredSize = layout.minimalSize(this);
		}
		return preferredSize;
	}

	/// Adds a child element. Call [#pack()] afterwards to update the layout.
	///
	/// @param elt an element which doesn't belong to any container yet.
	/// @throws IllegalArgumentException if `elt` already belongs to a drawing
	///         or a composite (including this one).
	public void addElement(GraphicalElement elt) {
		if (elt.getManager() != null)
			throw new IllegalArgumentException(
					"element already belongs to a container: " + elt);
		elements.add(elt);
		preferredSize = null;
		elt.setManager(this.contentManager);
	}

	/// Removes a child element. Call [#pack()] afterwards to update the layout.
	public void removeElement(GraphicalElement elt) {
		if (elements.remove(elt)) {
			preferredSize = null;
			elt.setManager(null);
		}
	}
    public int getNumberOfChildren() {
        return elements.size();
    }
    
    public GraphicalElement getChild(int i) {
        return elements.get(i);
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
		try {
			layout.fitChildren(this, width, height);
			fixDecoratorBounds();
		} finally {
			inUpdate = oldInUpdate;
		}
	}

	/**
	 * Method which should be called after the last element has been added.
	 */
	public void pack() {
		boolean oldInUpdate = inUpdate;
		inUpdate = true;
		try {
			preferredSize = null;
			layout.layoutComposite(this);
		} finally {
			inUpdate = oldInUpdate;
		}
	}

    

	@Override
	public String toString() {
		return "[ composite " + elements + "]";
	}

	private class CompositeContentManager implements GraphicalElementManager {

		public void eventOccurred(DrawingEvent ev) {
			if (!inUpdate) {
				inUpdate = true;
				try {
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
				} finally {
					inUpdate = false;
				}
			}
		}
	}
	
	/// Sets the axis along which the composite will be packed.
	///
	/// Selects the matching standard layout ([VerticalCompositeLayout] or
	/// [HorizontalCompositeLayout]). This method does not pack the composite.
	/// Packing must be called explicitly.
	public void setAxis(CompositeAxis axis) {
		this.axis = axis;
		setLayout(axis == CompositeAxis.HORIZONTAL ? new HorizontalCompositeLayout()
				: new VerticalCompositeLayout());
	}

	/// Sets a specific layout for this composite. Does not pack the composite.
	public void setLayout(CompositeLayout layout) {
		this.layout = layout;
		preferredSize = null;
	}

	public CompositeLayout getLayout() {
		return layout;
	}
	
	/**
	 * Gets the composite axis. Default axis is {@link CompositeAxis#VERTICAL}.
	 * @return
	 */
	public CompositeAxis getAxis() {
		return axis;
	}
}

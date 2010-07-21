package jsesh.mdcDisplayer.viewToolkit.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import jsesh.mdcDisplayer.viewToolkit.drawing.event.DrawingEvent;
import jsesh.mdcDisplayer.viewToolkit.drawing.event.GraphicalElementManager;
import jsesh.mdcDisplayer.viewToolkit.drawing.event.PropertyChangedEvent;
import jsesh.mdcDisplayer.viewToolkit.elements.properties.BoundsProperty;
import jsesh.mdcDisplayer.viewToolkit.elements.properties.DecoratorProperty;
import jsesh.mdcDisplayer.viewToolkit.elements.properties.ObjectProperty;
import jsesh.utils.DoubleDimensions;

/**
 * Baseclass for Graphical elements. Inspired by the JHotdraw framework.
 * 
 * <p>Note that (according to a usual practice with composites) 
 * we moved a number of method from composite to this class.
 * it makes it far easier to write code in an uniform way, as basically, leaf elements
 * are considered as a kind of "empty" composite.
 * 
 * @author rosmord
 */
public abstract class GraphicalElement implements Iterable<GraphicalElement> {


	
	private GraphicalElementManager manager;

	private GraphicalElement decorator = null;
	
	/**
	 * The layer of the element in the picture.
	 */
	private ObjectProperty<Integer> layer= new ObjectProperty<Integer>("layer", this, 0, true);

	// As the position of the element and its dimensions are rather orthogonal
	// notions, we prefer to leave them in different objects.

	/**
	 * Top left point.
	 */
	private Point2D origin = new Point2D.Double();

	private Dimension2D dimension = null;

	/**
	 * The way this element will be drawn if its bounds exceed its preferred
	 * size.
	 */
	// private Alignment alignment = Alignment.LEFT;
	private ObjectProperty<Alignment> alignmentProperty = new ObjectProperty<Alignment>(
			"alignment", this, Alignment.LEFT, false);

	// private Color background= new Color(0,0,0,0);
	private ObjectProperty<Color> backgroundProperty = new ObjectProperty<Color>(
			"background", this, new Color(0, 0, 0, 0), false);

	/**
	 * Normally called when the element is added to a drawing. The drawing is
	 * responsible for calling this method.
	 * 
	 * @param drawing
	 */
	public void setManager(GraphicalElementManager manager) {
		this.manager = manager;
		// for (GraphicalElement elt: this) {
		// elt.setManager(manager); // Or maybe this element ?
		// }
	}

	public GraphicalElementManager getManager() {
		return manager;
	}

	final public void draw(Graphics2D g) {
		Graphics2D g1 = (Graphics2D) g.create();
		Rectangle2D bounds = getBounds();
		g1.setBackground(getBackground());
		Color oldColor = g1.getColor();
		g1.setColor(getBackground());
		g1.fill(bounds);
		g1.setColor(oldColor);
		// Horizontal alignment.
		double diffx = getBounds().getWidth() - getPreferredSize().getWidth();
		double dx = 0;
		switch (getAlignment()) {
		case LEFT:
			dx = 0;
			break;
		case RIGHT:
			dx = diffx;
			break;
		case CENTERED:
			dx = diffx / 2;
			break;
		case FILL:
		default:
			dx = 0;
			break;
		}
		g1.translate(bounds.getMinX() + dx, bounds.getMinY());
		if (getDecorator() != null)
			getDecorator().draw(g1);
		drawElement(g1);
		g1.dispose();
	}

	/**
	 * Get element boundaries, ignoring any decoration.
	 * 
	 * @return
	 */
	public Rectangle2D getBounds() {
		if (dimension == null)
			dimension = getPreferredSize();
		return new Rectangle2D.Double(origin.getX(), origin.getY(), dimension
				.getWidth(), dimension.getHeight());
	}

	/**
	 * Get element boundaries, including its decoration.
	 * 
	 * @return
	 */
	public Rectangle2D getDecoratedBounds() {
		return getBounds(); // TODO Change me...
	}

	/**
	 * Actual drawing of an element. The element origin has already been moved.
	 * so this method should not try to deal with element bounds. Consider that
	 * the element position is 0,0.
	 * 
	 * @param g
	 *            a graphic context created for drawing this specific element.
	 */
	abstract protected void drawElement(Graphics2D g);

	/**
	 * Returns an iterator to this element's children. Usually, elements have no
	 * children. In this case, the iterator iterates over an empty set !
	 */
	public Iterator<GraphicalElement> iterator() {
		return new HashSet<GraphicalElement>().iterator();
	}

	/**
	 * Computes the "natural size" for this elements (depends on its content).
	 * 
	 * @return
	 */
	abstract public Dimension2D getPreferredSize();

	public void setOrigin(double x, double y) {
		origin = new Point2D.Double(x, y);
		fireDrawingEvent(new PropertyChangedEvent(this, new BoundsProperty()));
	}

	public void setBounds(double x, double y, double width, double height) {
		origin = new Point2D.Double(x, y);
		dimension = new DoubleDimensions(width, height);
		fixDecoratorBounds();
		fireDrawingEvent(new PropertyChangedEvent(this, new BoundsProperty()));
	}

	/**
	 * The way this element will be drawn if its bounds exceed its prefered
	 * size.
	 */
	public Alignment getAlignment() {
		return alignmentProperty.getValue();
	}

	/**
	 * The way this element will be drawn if its bounds exceed its prefered
	 * size.
	 */
	public void setAlignment(Alignment alignment) {
		alignmentProperty.setValue(alignment);
	}

	public void setDecorator(GraphicalElement newDecorator) {
		if (newDecorator != decorator) {
			this.decorator = newDecorator;
			fixDecoratorBounds();
			fireDrawingEvent(new PropertyChangedEvent(this,
					new DecoratorProperty()));
		}
	}

	protected void fixDecoratorBounds() {
		if (dimension != null && decorator != null) {
			// TODO : propagate decoration bounds.
			decorator.setBounds(1, 1, dimension
					.getWidth()-2, dimension.getHeight()-2);
		}
	}
	
	public void setBackground(Color background) {
		backgroundProperty.setValue(background);
	}

	public Color getBackground() {
		return backgroundProperty.getValue();
	}
	
	public int getLayer() {
		return layer.getValue();
	}
	
	public void setLayer(int layer) {
		this.layer.setValue(layer);
	}

	public void fireDrawingEvent(DrawingEvent e) {
		if (manager != null)
			manager.eventOccurred(e);
	}

	public GraphicalElement getDecorator() {
		return decorator;
	}
	
	/**
	 * Get inner elements which intersect a certain zone.
	 * If the element is not a composite one, will return the empty set.
	 * @param rectangle
	 *            a zone in the drawing.
	 * @return 
	 */
	public Collection<GraphicalElement> getElementsInZone(Rectangle2D rectangle) {
		return new HashSet<GraphicalElement>();
	}

	/**
	 * Returns the inner elements that contain a certain point.
	 * Returns the empty set when the element has no inner elements. 
	 * @param point2D
	 * @return
	 */
	public Collection<GraphicalElement> getElementsAt(Point2D point2D) {
		return getElementsInZone(new Rectangle2D.Double(point2D.getX(), point2D
				.getY(), 1, 1));
	}
	

}

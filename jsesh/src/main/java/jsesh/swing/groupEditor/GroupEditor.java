/*
 * Created on 28 nov. 2004
 *
 * This file is distributed under the GNU Lesser Public Licence.
 * 
 * (c) Serge Rosmorduc.
 */
package jsesh.swing.groupEditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Dimension2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.layout.MDCEditorKit;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

import org.qenherkhopeshef.graphics.utils.GraphicsUtils;

/**
 * Editor for groups and ligatures.
 * <p>
 * when a sign is selected, a red frame is drawn around it. moving this frame
 * will move the sign. The handles in the frame corner, and around the frame,
 * allow resizing. shift and mouse movement allow rotation.
 * <p>
 * The cursor will change accordingly.
 * 
 * @author rosmord
 */

public class GroupEditor extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1003487338247822571L;

	private static class Handle {
		public Shape shape;

		public int vpos, hpos;

		/**
		 * @param shape
		 * @param vpos
		 * @param hpos
		 */
		public Handle(Shape shape, int hpos, int vpos) {
			this.shape = shape;
			this.vpos = vpos;
			this.hpos = hpos;
		}
	}

	/**
	 * At this level, the group editor control's function is just to build
	 * events.
	 * 
	 * @author rosmord
	 */
	class LowLevelControl implements MouseInputListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
		 */

		public void mouseClicked(MouseEvent e) {
			for (int i = 0; i < groupEditorEventListeners.size(); i++)
				((GroupEditorListener) groupEditorEventListeners.get(i))
						.mouseClicked(buildEvent(e));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
		 * )
		 */
		public void mouseDragged(MouseEvent e) {
			for (int i = 0; i < groupEditorEventListeners.size(); i++)
				((GroupEditorListener) groupEditorEventListeners.get(i))
						.mouseDragged(buildEvent(e));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
		 */
		public void mouseEntered(MouseEvent e) {
			for (int i = 0; i < groupEditorEventListeners.size(); i++)
				((GroupEditorListener) groupEditorEventListeners.get(i))
						.mouseEntered(buildEvent(e));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
		 */
		public void mouseExited(MouseEvent e) {
			for (int i = 0; i < groupEditorEventListeners.size(); i++)
				((GroupEditorListener) groupEditorEventListeners.get(i))
						.mouseExited(buildEvent(e));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent
		 * )
		 */
		public void mouseMoved(MouseEvent e) {
			for (int i = 0; i < groupEditorEventListeners.size(); i++)
				((GroupEditorListener) groupEditorEventListeners.get(i))
						.mouseMoved(buildEvent(e));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent e) {
			for (int i = 0; i < groupEditorEventListeners.size(); i++)
				((GroupEditorListener) groupEditorEventListeners.get(i))
						.mousePressed(buildEvent(e));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent e) {
			for (int i = 0; i < groupEditorEventListeners.size(); i++)
				((GroupEditorListener) groupEditorEventListeners.get(i))
						.mouseReleased(buildEvent(e));
		}
	}

	// The modes
	/**
	 * In RESIZE mode, handles are used for resizing the signs.
	 */
	public static final int RESIZE = 1;

	/**
	 * In ROTATION mode, handles rotate signs.
	 */
	public static final int ROTATION = 2;

	/**
	 * In move mode, there are no handles.
	 */
	public static final int MOVE = 3;

	/**
	 * The drawing specifications. TODO find a good policy for drawing
	 * specifications !!!!
	 */
	DrawingSpecification drawingSpecifications = MDCEditorKit
			.getBasicMDCEditorKit().getDrawingSpecifications();

	// The group we are editing.
	private AbsoluteGroup group;

	private ArrayList groupEditorEventListeners = new ArrayList();

	/**
	 * Handle sizes
	 */

	private int handleSize = 10;

	/**
	 * Frame line width.
	 */
	private int lineWidth = 2;

	/**
	 * The editing mode ; either RESIZE or ROTATE.
	 */
	private int mode = RESIZE;

	private double scale = 10;

	// the selected sign.
	private int selected = -1;

	private double sideMargin = 5;

	private double topMargin = 5;

	public GroupEditor() {
		setBackground(Color.WHITE);
		LowLevelControl control = new LowLevelControl();
		addMouseListener(control);
		addMouseMotionListener(control);
		// TODO : fix the system so that it handle right-to-left signs.
		// Currently, we simply force left-to-right order
		drawingSpecifications = drawingSpecifications.copy();
		drawingSpecifications.setTextDirection(TextDirection.LEFT_TO_RIGHT);
	}

	public void addGroupEditorEventListener(GroupEditorListener l) {
		groupEditorEventListeners.add(l);
	}

	/**
	 * Translates a mouse event into a significant group editor event.
	 * 
	 * @param e
	 * @return the corresponding event.
	 */
	private GroupEditorEvent buildEvent(MouseEvent e) {
		GroupEditorEvent result = null;
		Point2D p = getModelPoint(e.getPoint());
		MDCView v = getView();

		// First, test handles for the selected shape.
		if (selected != -1 && mode != MOVE) {
			MDCView subv = v.getSubView(selected);
			// Test the selected sign handles.
			Handle handles[] = getHandles(subv);
			for (int i = 0; result == null && i < handles.length; i++) {
				if (handles[i].shape.contains(p))
					result = new GroupEditorEvent(group, p, selected,
							handles[i].hpos, handles[i].vpos);
			}
		}

		// Test all shapes.
		// First, we try an exact system, based on actual shapes.
		// However, the click must fall on a "black" line.

		for (int i = 0; (result == null) && i < v.getNumberOfSubviews(); i++) {
			// Test the view itself.
			MDCView subv = v.getSubView(i);
			Hieroglyph h = group.getHieroglyphAt(i);

			Area s1 = getGlyphArea(subv, h);
			if (s1.contains(p)) {
				result = new GroupEditorEvent(group, p, i);
			}
		}
		if (result == null) {
			result = new GroupEditorEvent(group, p, -1);
		}
		return result;
	}

	/**
	 * Should probably move somewhere else.
	 * @param subv
	 * @param h
	 * @return
	 */
	private Area getGlyphArea(MDCView subv, Hieroglyph h) {
		Area area;
		if (h.getType() == SymbolCodes.SMALLTEXT) {
			Dimension2D dims = drawingSpecifications.getSuperScriptDimensions(h.getSmallText());
			double height= h.getRelativeSize() / 100.0 * dims.getHeight();
			double width= h.getRelativeSize() / 100.0 * dims.getWidth();
			area= new Area(new Rectangle2D.Double(subv.getPosition().x, subv.getPosition().y, width, height));
		} else {
			area = drawingSpecifications.getHieroglyphsDrawer().getSignArea(
					h.getCode(), subv.getPosition().x, subv.getPosition().y,
					h.getRelativeSize() / 100.0, h.getRelativeSize() / 100.0,
					h.getAngle(), h.isReversed());
		}
		return area;
	}

	private Handle buildHandle(double x, double y, double diameter, int hpos,
			int vpos) {
		Shape shape;
		if (mode == ROTATION)
			shape = new Ellipse2D.Double(x, y, diameter, diameter);
		else
			shape = new Rectangle2D.Double(x, y, diameter, diameter);
		return new Handle(shape, hpos, vpos);
	}

	/**
	 * @return Returns the group.
	 */

	public AbsoluteGroup getGroup() {
		return group;
	}

	/**
	 * Build the list of handles.
	 * 
	 * <pre>
	 *  1 2 : top handles
	 *    4 : middle handles
	 *  6 7 : bottom handles
	 * </pre>
	 * 
	 * @param v
	 *            the view.
	 * @return the handles.
	 */

	private Handle[] getHandles(MDCView v) {
		Handle result[] = new Handle[8];
		Point2D orig = getViewPosition(v);
		double w = v.getWidth();
		double h = v.getHeight();
		double radius = (handleSize / (2 * scale));
		double diameter = 2 * radius;
		int i = 0;

		result[i++] = buildHandle(orig.getX() - radius, orig.getY() - radius,
				diameter, GroupEditorEvent.LEFT, GroupEditorEvent.TOP);
		result[i++] = buildHandle(orig.getX() - radius + w / 2, orig.getY()
				- radius, diameter, GroupEditorEvent.MIDDLE,
				GroupEditorEvent.TOP);
		result[i++] = buildHandle(orig.getX() - radius + w, orig.getY()
				- radius, diameter, GroupEditorEvent.RIGHT,
				GroupEditorEvent.TOP);
		result[i++] = buildHandle(orig.getX() - radius, orig.getY() + h / 2
				- radius, diameter, GroupEditorEvent.LEFT,
				GroupEditorEvent.MIDDLE);
		result[i++] = buildHandle(orig.getX() - radius + w, orig.getY() + h / 2
				- radius, diameter, GroupEditorEvent.RIGHT,
				GroupEditorEvent.MIDDLE);
		result[i++] = buildHandle(orig.getX() - radius, orig.getY() + h
				- radius, diameter, GroupEditorEvent.LEFT,
				GroupEditorEvent.BOTTOM);
		result[i++] = buildHandle(orig.getX() - radius + w / 2, orig.getY() + h
				- radius, diameter, GroupEditorEvent.MIDDLE,
				GroupEditorEvent.BOTTOM);
		result[i++] = buildHandle(orig.getX() - radius + w, orig.getY() + h
				- radius, diameter, GroupEditorEvent.RIGHT,
				GroupEditorEvent.BOTTOM);
		return result;
	}

	public int getHandleSize() {
		return handleSize;
	}

	public int getLineWidth() {
		return lineWidth;
	}

	public int getMode() {
		return mode;
	}

	/**
	 * Return a point in model coordinates
	 * 
	 * @param p
	 *            : a point in screen coordinates.
	 * @return a point in model coordinates
	 */
	private Point2D getModelPoint(Point p) {
		double x = (p.getX() / scale - sideMargin);
		double y = (p.getY() / scale - topMargin);
		return new Point2D.Double(x, y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		if (group == null) {
			return new Dimension(640, 480);
		} else {
			MDCView v = getView();
			int w = (int) ((v.getWidth() + 2 * this.sideMargin) * scale);
			int h = (int) ((v.getHeight() + 2 * topMargin) * scale);
			return new Dimension(w, h);
		}
	}

	public double getScale() {
		return scale;
	}

	/**
	 * Return the index of the selected element, or -1 if none.
	 * 
	 * @return the index of the selected element, or -1 if none.
	 */
	public int getSelected() {
		return selected;
	}

	public double getSideMargin() {
		return sideMargin;
	}

	public double getTopMargin() {
		return topMargin;
	}

	private MDCView getView() {
		MDCView view = null;
		if (group != null) {
			SimpleViewBuilder builder = new SimpleViewBuilder();
			view = builder.buildView(group, drawingSpecifications);
		} else {
			view = new MDCView(null);
		}
		return view;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// The view is rebuilt for each redraw, as it's very simple.
		MDCView view = getView();
		// Sets the graphics environment
		Graphics2D g2d = (Graphics2D) g.create();
		// We want antialiasing !!!
		GraphicsUtils.antialias(g2d);

		g2d.scale(scale, scale);
		g2d.translate(this.sideMargin, this.topMargin);
		double wd = (lineWidth / g2d.getTransform().getScaleX());

		if (group != null) {
			// draw the signs
			ViewDrawer drawer = new ViewDrawer();
			drawer.draw(g2d, view, drawingSpecifications);
			// Draw the frame around the selected sign.
			if (selected >= 0) {
				// The view for this sign :
				MDCView v = view.getSubView(selected);
				// For absolute groups, the view placement is ... absolute
				// (This could be a bug source, if we changed the layout for
				// this element)
				Point2D orig = getViewPosition(v);
				double w = v.getWidth();
				double h = v.getHeight();

				// g2d.setColor(new Color(255,0,0,100));
				g2d.setColor(Color.RED);
				g2d.setStroke(new BasicStroke((float) wd));
				// Draw the frame
				g2d
						.draw(new Rectangle2D.Double(orig.getX(), orig.getY(),
								w, h));
				// Draw the handles
				if (mode != MOVE) {
					Handle handles[] = getHandles(v);
					for (int i = 0; i < handles.length; i++)
						g2d.draw(handles[i].shape);
				}
			}
		}
		g2d.setColor(Color.RED);
		// Sets the actual width for drawing :
		float f[] = { 0.5f, 1 };
		g2d.setStroke(new BasicStroke((float) (wd / 2), BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_MITER, 2, f, 0));

		g2d.draw(new Line2D.Double(0, 0, 1000, 0));
		g2d.draw(new Line2D.Double(0, 0, 0, 1000));
		g2d.dispose();
	}

	public void removeGroupEditorEventListener(GroupEditorListener l) {
		groupEditorEventListeners.remove(l);
	}

	/**
	 * @param group
	 *            The group to set.
	 */
	public void setGroup(AbsoluteGroup group) {
		this.group = group;
		selected = -1;
		revalidate();
	}

	public void setHandleSize(int handleSize) {
		this.handleSize = handleSize;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}

	public void setMode(int mode) {
		this.mode = mode;
		repaint();
	}

	public void setScale(double scale) {
		this.scale = scale;
		revalidate();
	}

	public void setSelected(int selected) {
		this.selected = selected;
		repaint();
	}

	public void setSideMargin(double sideMargin) {
		this.sideMargin = sideMargin;
		revalidate();
	}

	public void setTopMargin(double topMargin) {
		this.topMargin = topMargin;
		revalidate();
	}

	/**
	 * Rotate the selected sign by the correct angle.
	 * 
	 * @param angle
	 */
	public void rotate(double angle) {
		revalidate();
		repaint();
	}

	/**
	 * moves the selected sign.
	 * 
	 * @param dx
	 * @param dy
	 */
	public void move(double dx, double dy) {
		if (selected != -1) {
			MDCView v = getView().getSubView(selected);
			Point2D p = getViewPosition(v);
			double x = p.getX() + dx;
			double y = p.getY() + dy;
			// Convert to integers :
			double unitSize = drawingSpecifications.getHieroglyphsDrawer()
					.getGroupUnitLength();
			x = x / unitSize;
			y = y / unitSize;
			Hieroglyph h = group.getHieroglyphAt(selected);
			h.setExplicitPosition((int) x, (int) y, h.getRelativeSize());
			revalidate();
			repaint();
		}
	}

	/*
	 * Change the size of the selected glyph. Currently, the aspect ratio
	 * doesn't change.
	 */

	public void resizeTo(double dx, double dy, int horizontalHandlePosition,
			int verticalHandlePosition) {
		// FOR LATER IMPLEMENTATION : If both an hside and vside are
		// selected, the sign aspect ratio will be kept.

		if (selected != -1) {
			double scale = 1;
			double x, y;

			MDCView v = getView().getSubView(selected);
			Point2D orig = getViewPosition(v);

			x = orig.getX();
			y = orig.getY();

			if (horizontalHandlePosition != GroupEditorEvent.MIDDLE) {
				double newWidth;
				if (horizontalHandlePosition == GroupEditorEvent.LEFT) {
					newWidth = v.getWidth() - dx;
					x = x + dx;
				} else {
					newWidth = v.getWidth() + dx;
				}
				if (newWidth > 0)
					scale = newWidth / v.getWidth();
			} else {
				double newHeight;
				if (verticalHandlePosition == GroupEditorEvent.TOP) {
					newHeight = v.getHeight() - dy;
					y = y + dy;
				} else {
					newHeight = v.getHeight() + dy;
				}
				if (newHeight > 0)
					scale = newHeight / v.getHeight();
			}

			Hieroglyph h = group.getHieroglyphAt(selected);
			// Convert to integers :
			// double unitSize = 1000.0 / drawingSpecifications.getBaseLength();
			x = x
					/ drawingSpecifications.getHieroglyphsDrawer()
							.getGroupUnitLength();
			y = y
					/ drawingSpecifications.getHieroglyphsDrawer()
							.getGroupUnitLength();

			scale = scale * h.getRelativeSize();
			if (scale < 5)
				scale = 5;
			h.setExplicitPosition((int) x, (int) y, (int) scale);
			repaint();
			invalidate();
		}

	}

	/**
	 * Not too reliable system for sub view positions.
	 * 
	 * @param subview
	 * @return the position of subview.
	 */
	private Point2D getViewPosition(MDCView subview) {
		return new Point2D.Double(subview.getPosition().x, subview
				.getPosition().y);
	}

	/**
	 * Reverse the selected hieroglyph's orientation.
	 */
	public void reverse() {

	}

	/**
	 * Rotate the selected sign around its center c.
	 * <p>
	 * the angle is given by the two vectors cp1 and cp2.
	 * 
	 * @param p1
	 * @param p2
	 */
	public void rotate(Point2D p1, Point2D p2) {
		if (selected != -1) {
			MDCView v = getView().getSubView(selected);
			Point2D orig = getViewPosition(v);
			Point2D center = new Point2D.Double(orig.getX() + v.getWidth() / 2,
					orig.getY() + v.getHeight() / 2);
			// Compute the vectors.
			Point2D v1 = new Point2D.Double(p1.getX() - center.getX(), p1
					.getY()
					- center.getY());
			Point2D v2 = new Point2D.Double(p2.getX() - center.getX(), p2
					.getY()
					- center.getY());
			double d1 = v1.distance(0, 0);
			double d2 = v2.distance(0, 0);
			if (d1 == 0 || d2 == 0)
				return;
			double cos = (v1.getX() * v2.getX() + v1.getY() * v2.getY())
					/ (d1 * d2);
			double sin = (v1.getX() * v2.getY() - v1.getY() * v2.getX())
					/ (d1 * d2);

			double alpha = Math.acos(cos);

			if (sin < 0)
				alpha = 2 * Math.PI - alpha;
			Hieroglyph h = group.getHieroglyphAt(selected);
			double angle = (alpha * 180.0 / Math.PI) + h.getAngle();
			h.setAngle((int) angle);
			repaint();
			revalidate();
		}
	}

	public void rotate(int angle) {
		if (selected != -1) {
			Hieroglyph h = group.getHieroglyphAt(selected);
			h.setAngle((int) angle);
			repaint();
			revalidate();
		}
	}

	public void resetSign() {
		if (selected != -1) {
			Hieroglyph h = group.getHieroglyphAt(selected);
			h.setExplicitPosition(0, 0, 100);
			repaint();
			revalidate();
		}
	}

	/**
	 * select the next sign.
	 */
	public void next() {
		if (group != null) {
			selected = (selected + 2) % (group.getNumberOfChildren() + 1) - 1;
			repaint();
		}
	}

	/**
	 * 
	 */
	public void previous() {
		if (group != null) {
			selected = selected - 1;
			if (selected == -2)
				selected = group.getNumberOfChildren() - 1;
			repaint();
		}
	}

}
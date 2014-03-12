package org.qenherkhopeshef.viewToolKit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.qenherkhopeshef.guiFramework.busy.BusyInterface;
import org.qenherkhopeshef.swingUtils.GraphicsUtils;
import org.qenherkhopeshef.viewToolKit.drawing.Drawing;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.event.CursorChangedEvent;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingEvent;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingEventAdapter;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingListener;

/**
 * Basic Displayer for a composite drawing.
 * 
 * @author rosmord
 */
@SuppressWarnings("serial")
public class JGraphicalElementDisplayer<T extends Drawing> extends JPanel {

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

	public JGraphicalElementDisplayer(T drawing) {
		setRequestFocusEnabled(true);
		// setFocusable(true);
		setBackground(Color.WHITE);
		setDrawing(drawing);
		setLayout(null);
	}

	@Override
	public Dimension getPreferredSize() {
		if (drawing == null || drawing.isEmpty()) {
			return new Dimension(640, 480);
		} else {
			Rectangle2D rec = drawing.getPreferredSize();
			return new Dimension((int) Math.ceil(rec.getWidth()) + 2
					+ bottomMargin, (int) Math.ceil(rec.getHeight()) + 2
					+ rightMargin);
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

	public Collection<GraphicalElement> getElementsAt(Point point) {
		return drawing.getElementsAt(point);
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

	/**
	 * Ensure the element
	 */
	public void showCursor() {
		SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
				Rectangle2D cursorBounds = drawing.getCursorBounds();
				if (cursorBounds != null) {
					// Compute the rectangle :
					Rectangle area = new Rectangle(
							(int) cursorBounds.getMinX(), (int) cursorBounds
									.getMinY(), (int) cursorBounds.getWidth(),
							(int) cursorBounds.getHeight());					
					scrollRectToVisible(area);
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
		Graphics2D g2d = (Graphics2D) g.create();
		GraphicsUtils.antialias(g2d);
		Rectangle clipBounds = g2d.getClipBounds();
		Rectangle2D enlarged = new Rectangle2D.Double(clipBounds.getMinX() - 1,
				clipBounds.getMinY() - 1, clipBounds.getWidth() + 2,
				clipBounds.getHeight() + 2);
		Collection<GraphicalElement> graphics = drawing
				.getElementsInZone(enlarged);
		for (GraphicalElement elt : graphics) {
			elt.draw(g2d);
		}

		for (GraphicalElement elt : drawing.getDecorations()) {
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

	public Collection<GraphicalElement> getElementsAt(int x, int y) {
		Collection<GraphicalElement> result = drawing
				.getElementsAt(new Point2D.Double(x, y)); // when some scaling
															// is proposed, it
															// will be easy to
															// use it here.
		return result;
	}

}

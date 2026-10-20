package org.qenherkhopeshef.viewToolKit.demo.picture;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.qenherkhopeshef.viewToolKit.JGraphicalElementDisplayer;
import org.qenherkhopeshef.viewToolKit.drawing.GridDrawing;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.Margin;
import org.qenherkhopeshef.viewToolKit.drawing.element.PolylineElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.ShapeElement;

/// Mouse and keyboard handling for the picture drawer.
///
/// Three tools:
///
/// - **select**: click a line to select it, drag it to move it, drag one of
///   its handles to move a point; Delete removes it;
/// - **open line** and **closed line**: click to add points, double-click (or
///   Enter, or right-click) to finish, Escape to cancel, Backspace to remove
///   the last point. A closed line is also finished by clicking on its first
///   point.
///
/// The line being drawn, the rubber band and the selection handles are
/// *decorations* of the drawing: they are displayed, but are not part of the
/// picture.
final class PictureDrawerControl {

	enum Tool {
		SELECT, OPEN_LINE, CLOSED_LINE
	}

	/// Hit tolerance, in pixels (converted to drawing units with the scale).
	private static final double TOLERANCE = 5;

	private static final double HANDLE_SIZE = 8;

	private final JGraphicalElementDisplayer<GridDrawing> display;

	private final GridDrawing drawing;

	private Tool tool = Tool.OPEN_LINE;

	// Style for new lines (and for the selected one).
	private Color lineColor = Color.BLACK;
	private double lineWidth = 2;
	private boolean filled = false;

	/// The line being drawn (not yet in the drawing).
	private PolylineElement inProgress = null;

	private PolylineElement selected = null;

	// Drag state.
	private int draggedVertex = -1;
	private Point2D lastDragPoint = null;

	/// Receives a short help text when the state changes.
	private final Consumer<String> help;

	PictureDrawerControl(JGraphicalElementDisplayer<GridDrawing> display, Consumer<String> help) {
		this.display = display;
		this.drawing = display.getDrawing();
		this.help = help;
		display.setFocusable(true);
		Mouse mouse = new Mouse();
		display.addMouseListener(mouse);
		display.addMouseMotionListener(mouse);
		display.addKeyListener(new Keys());
		setTool(tool);
	}

	// ------------------------------------------------------------------
	// Commands (called by the user interface).

	void setTool(Tool tool) {
		cancelLine();
		this.tool = tool;
		if (tool != Tool.SELECT)
			select(null);
		display.setCursor(Cursor.getPredefinedCursor(
				tool == Tool.SELECT ? Cursor.DEFAULT_CURSOR : Cursor.CROSSHAIR_CURSOR));
		help.accept(switch (tool) {
		case SELECT -> "Click a line to select it; drag it to move it, or drag a handle to move a point. "
				+ "Delete removes the selection.";
		case OPEN_LINE -> "Click to add points; double-click, right-click or Enter to finish. "
				+ "Escape cancels, Backspace removes the last point.";
		case CLOSED_LINE -> "Click to add points; click the first point, double-click or press Enter to "
				+ "close the shape. Escape cancels.";
		});
	}

	void setLineColor(Color color) {
		this.lineColor = color;
		if (selected != null) {
			selected.setLineColor(color);
			selected.setFillColor(fillColorFor(selected));
		}
	}

	void setLineWidth(double width) {
		this.lineWidth = width;
		if (selected != null) {
			selected.setLineWidth(width);
			updateHandles();
		}
	}

	void setFilled(boolean filled) {
		this.filled = filled;
		if (selected != null)
			selected.setFillColor(fillColorFor(selected));
	}

	void deleteSelection() {
		if (selected != null) {
			PolylineElement s = selected;
			select(null);
			drawing.remove(s);
		}
	}

	void bringSelectionToFront() {
		if (selected != null)
			drawing.bringToFront(selected);
	}

	void sendSelectionToBack() {
		if (selected != null)
			drawing.sendToBack(selected);
	}

	void clear() {
		cancelLine();
		select(null);
		drawing.clear();
	}

	// ------------------------------------------------------------------

	private Color fillColorFor(PolylineElement line) {
		if (!filled || !line.isClosed())
			return new Color(0, 0, 0, 0);
		Color c = line.getLineColor();
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), 70);
	}

	private double tolerance() {
		return TOLERANCE / display.getScale();
	}

	private void select(PolylineElement line) {
		selected = line;
		updateHandles();
	}

	/// Shows the handles of the selected line, or the line in progress.
	private void updateHandles() {
		List<GraphicalElement> decorations = new ArrayList<>();
		if (selected != null) {
			for (Point2D p : selected.getPoints())
				decorations.add(handle(p));
		}
		drawing.setDecorations(decorations);
	}

	private GraphicalElement handle(Point2D p) {
		double size = HANDLE_SIZE / display.getScale();
		ShapeElement handle = new ShapeElement(new Rectangle2D.Double(0, 0, size, size));
		handle.setColor(Color.WHITE);
		handle.setLineColor(new Color(30, 90, 220));
		handle.setLineWidth(1 / display.getScale());
		handle.setMargin(Margin.NONE);
		handle.setOrigin(p.getX() - size / 2, p.getY() - size / 2);
		return handle;
	}

	/// Shows the line in progress, with a rubber band towards the mouse.
	private void updatePreview(Point2D mouse) {
		List<GraphicalElement> decorations = new ArrayList<>();
		if (inProgress != null) {
			decorations.add(inProgress);
			List<Point2D> band = new ArrayList<>();
			band.add(inProgress.getPoint(inProgress.getPointCount() - 1));
			if (mouse != null)
				band.add(mouse);
			if (inProgress.isClosed() && inProgress.getPointCount() > 1 && mouse != null)
				band.add(inProgress.getPoint(0));
			PolylineElement rubberBand = new PolylineElement(band, false);
			rubberBand.setLineColor(new Color(120, 120, 120, 160));
			rubberBand.setLineWidth(1 / display.getScale());
			decorations.add(rubberBand);
			// Show where to click to close the shape.
			if (inProgress.isClosed() && inProgress.getPointCount() >= 3)
				decorations.add(handle(inProgress.getPoint(0)));
		}
		drawing.setDecorations(decorations);
	}

	private void addPoint(Point2D p) {
		if (inProgress == null) {
			inProgress = new PolylineElement(List.of(p), tool == Tool.CLOSED_LINE);
			inProgress.setLineColor(lineColor);
			inProgress.setLineWidth(lineWidth);
			inProgress.setFillColor(fillColorFor(inProgress));
		} else {
			inProgress.addPoint(p.getX(), p.getY());
		}
		updatePreview(p);
	}

	private void finishLine() {
		if (inProgress != null) {
			int needed = inProgress.isClosed() ? 3 : 2;
			if (inProgress.getPointCount() >= needed)
				drawing.addElement(inProgress);
			inProgress = null;
		}
		updatePreview(null);
	}

	private void cancelLine() {
		inProgress = null;
		updatePreview(null);
	}

	/// The topmost line near a point.
	private PolylineElement lineAt(Point2D p) {
		double t = tolerance();
		List<GraphicalElement> candidates = new ArrayList<>(drawing.getElementsInZone(
				new Rectangle2D.Double(p.getX() - t, p.getY() - t, 2 * t, 2 * t)));
		for (int i = candidates.size() - 1; i >= 0; i--) {
			if (candidates.get(i) instanceof PolylineElement line && line.contains(p, t))
				return line;
		}
		return null;
	}

	private int vertexAt(PolylineElement line, Point2D p) {
		double max = HANDLE_SIZE / display.getScale();
		for (int i = 0; i < line.getPointCount(); i++) {
			if (line.getPoint(i).distance(p) <= max)
				return i;
		}
		return -1;
	}

	private class Mouse extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			display.requestFocusInWindow();
			Point2D p = display.toDrawingPoint(e.getPoint());
			if (tool == Tool.SELECT) {
				draggedVertex = selected == null ? -1 : vertexAt(selected, p);
				if (draggedVertex < 0)
					select(lineAt(p));
				lastDragPoint = p;
			} else if (SwingUtilities.isRightMouseButton(e) || e.getClickCount() >= 2) {
				finishLine();
			} else if (inProgress != null && inProgress.isClosed() && inProgress.getPointCount() >= 3
					&& vertexAt(inProgress, p) == 0) {
				finishLine();
			} else {
				addPoint(p);
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			Point2D p = display.toDrawingPoint(e.getPoint());
			if (tool == Tool.SELECT && selected != null && lastDragPoint != null) {
				if (draggedVertex >= 0)
					selected.setPoint(draggedVertex, p.getX(), p.getY());
				else
					selected.translate(p.getX() - lastDragPoint.getX(), p.getY() - lastDragPoint.getY());
				lastDragPoint = p;
				updateHandles();
			} else if (tool != Tool.SELECT && inProgress != null) {
				updatePreview(p);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			lastDragPoint = null;
			draggedVertex = -1;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			Point2D p = display.toDrawingPoint(e.getPoint());
			if (tool != Tool.SELECT) {
				if (inProgress != null)
					updatePreview(p);
			} else {
				boolean onSomething = (selected != null && vertexAt(selected, p) >= 0) || lineAt(p) != null;
				display.setCursor(Cursor.getPredefinedCursor(onSomething ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR));
			}
		}
	}

	private class Keys extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_ENTER -> finishLine();
			case KeyEvent.VK_ESCAPE -> {
				cancelLine();
				select(null);
			}
			case KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE -> {
				if (inProgress != null) {
					if (inProgress.getPointCount() > 1) {
						inProgress.removeLastPoint();
						updatePreview(null);
					} else {
						cancelLine();
					}
				} else {
					deleteSelection();
				}
			}
			default -> {
				return;
			}
			}
			e.consume();
		}
	}
}

package org.qenherkhopeshef.viewtoolkit.demo.uml;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.qenherkhopeshef.viewtoolkit.JGraphicalElementDisplayer;
import org.qenherkhopeshef.viewtoolkit.demo.uml.UmlParser.UmlParseException;
import org.qenherkhopeshef.viewtoolkit.drawing.diagram.DiagramDrawing;
import org.qenherkhopeshef.viewtoolkit.drawing.element.GraphicalElement;

/// Demo 3: a UML class diagram displayer.
///
/// The diagram is described in a PlantUML-like text (left); "Render" (or
/// menu-shortcut + Enter) parses it, and [UmlDiagramPresenter] builds a
/// [DiagramDrawing] of [ClassBoxElement]s linked by connectors, placed by a
/// simple automatic layout. Class boxes can then be dragged around: the
/// connectors follow.
public final class UmlDiagramDemo {

	private static final String SAMPLE = """
			' The viewToolKit itself.
			interface Drawing {
			  + getElementsInZone(zone : Rectangle2D)
			  + getDecorations()
			  + getPreferredSize() : Rectangle2D
			}
			abstract class AbstractDrawing {
			  - listeners : Set<DrawingListener>
			  # manage(element : GraphicalElement)
			  # eventOccurredInDrawing(ev : DrawingEvent)
			}
			class GridDrawing {
			  - cells : Map<Long, Set<GraphicalElement>>
			  + putAt(element, x, y)
			  + remove(element)
			}
			class DiagramDrawing {
			  + addConnector(c : ConnectorElement)
			}
			class TextDrawing {
			  - heights : HeightIndex
			  + getPositionAt(p : Point2D) : TextPosition
			  + layoutPendingParagraphs(nanos : long)
			}
			class TabularDrawing
			class HeightIndex {
			  + prefix(i : int) : double
			  + indexAt(y : double) : int
			}
			abstract class GraphicalElement {
			  - origin : Point2D
			  - margin : Margin
			  + draw(g : Graphics2D)
			  # drawElement(g : Graphics2D)
			  + getPreferredSize() : Dimension2D
			}
			class CompositeElement {
			  + addElement(e : GraphicalElement)
			  + pack()
			}
			interface CompositeLayout {
			  + layoutComposite(c : CompositeElement)
			}
			class TextElement
			class ShapeElement
			class PolylineElement
			class ConnectorElement
			class ParagraphElement
			class TextDocument {
			  + insertText(pos : TextPosition, text : String)
			  + delete(from : TextPosition, to : TextPosition)
			}
			class JGraphicalElementDisplayer {
			  - scale : double
			  + setDrawing(d : Drawing)
			}

			Drawing <|.. AbstractDrawing
			AbstractDrawing <|-- GridDrawing
			AbstractDrawing <|-- TextDrawing
			AbstractDrawing <|-- TabularDrawing
			GridDrawing <|-- DiagramDrawing
			GraphicalElement <|-- CompositeElement
			GraphicalElement <|-- TextElement
			GraphicalElement <|-- ShapeElement
			GraphicalElement <|-- PolylineElement
			GraphicalElement <|-- ConnectorElement
			CompositeElement <|-- ParagraphElement
			CompositeElement "1" o-- "*" GraphicalElement : children
			CompositeElement --> "1" CompositeLayout : layout
			ConnectorElement --> "2" GraphicalElement : ends
			TextDrawing *-- "1" HeightIndex
			TextDrawing --> "1" TextDocument : displays
			TextDrawing ..> ParagraphElement : creates
			JGraphicalElementDisplayer --> "1" Drawing : displays
			""";

	private final JFrame frame = new JFrame("viewToolKit demo: UML diagram displayer");

	private final DiagramDrawing drawing = new DiagramDrawing();

	private final JGraphicalElementDisplayer<DiagramDrawing> display = new JGraphicalElementDisplayer<>(drawing);

	private final UmlDiagramPresenter presenter = new UmlDiagramPresenter(drawing);

	private final JTextArea source = new JTextArea(SAMPLE);

	private final JLabel status = new JLabel(" ");

	private UmlDiagramDemo() {
		source.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		source.setTabSize(2);
		int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		source.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, shortcut), "render");
		source.getActionMap().put("render", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				render();
			}
		});
		JButton renderButton = new JButton("Render");
		renderButton.addActionListener(e -> render());
		JPanel left = new JPanel(new BorderLayout());
		left.add(new JScrollPane(source), BorderLayout.CENTER);
		left.add(renderButton, BorderLayout.SOUTH);

		JPanel right = new JPanel(new BorderLayout());
		right.add(buildToolBar(), BorderLayout.NORTH);
		right.add(new JScrollPane(display), BorderLayout.CENTER);
		new DragControl().install(display);

		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		split.setDividerLocation(330);
		status.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
		frame.getContentPane().add(split, BorderLayout.CENTER);
		frame.getContentPane().add(status, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(1300, 850);
		frame.setLocationByPlatform(true);
		render();
	}

	/// Opens a new diagram window. Must be called on the event dispatch thread.
	public static void open() {
		new UmlDiagramDemo().frame.setVisible(true);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(UmlDiagramDemo::open);
	}

	private JComponent buildToolBar() {
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.add(button("Zoom +", () -> display.setScale(display.getScale() * 1.25)));
		bar.add(button("Zoom −", () -> display.setScale(display.getScale() / 1.25)));
		bar.add(button("100 %", () -> display.setScale(1)));
		bar.addSeparator();
		bar.add(button("Automatic layout", presenter::layout));
		return bar;
	}

	private static JButton button(String label, Runnable action) {
		JButton b = new JButton(label);
		b.addActionListener(e -> action.run());
		return b;
	}

	private void render() {
		try {
			UmlModel model = UmlParser.parse(source.getText());
			presenter.show(model);
			status.setForeground(Color.DARK_GRAY);
			status.setText(model.getClasses().size() + " classes, " + model.getRelations().size()
					+ " relations. Drag the classes to move them.");
		} catch (UmlParseException e) {
			status.setForeground(Color.RED.darker());
			status.setText("Error " + e.getMessage());
			highlightLine(e.line);
		}
	}

	private void highlightLine(int line) {
		try {
			int start = source.getLineStartOffset(line - 1);
			int end = source.getLineEndOffset(line - 1);
			source.requestFocusInWindow();
			source.select(start, end);
		} catch (javax.swing.text.BadLocationException e) {
			// ignore: the line doesn't exist.
		}
	}

	/// Drags class boxes. Moving a box is just changing its origin: the
	/// drawing re-indexes it and re-routes its connectors.
	private class DragControl extends MouseAdapter {

		private ClassBoxElement dragged = null;
		private Point2D last = null;

		void install(JGraphicalElementDisplayer<?> d) {
			d.addMouseListener(this);
			d.addMouseMotionListener(this);
		}

		private ClassBoxElement boxAt(Point2D p) {
			List<GraphicalElement> elements = new ArrayList<>(drawing.getElementsAt(p));
			for (int i = elements.size() - 1; i >= 0; i--) {
				if (elements.get(i) instanceof ClassBoxElement box && box.getBounds().contains(p))
					return box;
			}
			return null;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			last = display.toDrawingPoint(e.getPoint());
			ClassBoxElement box = boxAt(last);
			for (ClassBoxElement b : presenter.getBoxes())
				b.setSelected(b == box);
			dragged = box;
			if (box != null) {
				drawing.bringToFront(box);
				status.setForeground(Color.DARK_GRAY);
				status.setText(box.getClassName() + ": " + drawing.getConnectors(box).size() + " relation(s).");
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (dragged != null) {
				Point2D p = display.toDrawingPoint(e.getPoint());
				Rectangle2D b = dragged.getBounds();
				// Keep the boxes in the positive quadrant.
				double x = Math.max(0, b.getMinX() + p.getX() - last.getX());
				double y = Math.max(0, b.getMinY() + p.getY() - last.getY());
				dragged.setOrigin(x, y);
				last = p;
				display.scrollRectToVisible(display.toComponentRectangle(dragged.getBounds()));
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			dragged = null;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			boolean onBox = boxAt(display.toDrawingPoint(e.getPoint())) != null;
			display.setCursor(Cursor.getPredefinedCursor(onBox ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR));
		}
	}
}

package org.qenherkhopeshef.viewToolKit.demo.picture;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.qenherkhopeshef.viewToolKit.JGraphicalElementDisplayer;
import org.qenherkhopeshef.viewToolKit.demo.picture.PictureDrawerControl.Tool;
import org.qenherkhopeshef.viewToolKit.drawing.GridDrawing;
import org.qenherkhopeshef.viewToolKit.drawing.element.PolylineElement;

/// Demo 2: a picture drawer, with open and closed lines.
///
/// The picture is a [GridDrawing] of [PolylineElement]s; the editing is done by
/// [PictureDrawerControl]. "Add 10 000 random lines" shows that the grid index
/// keeps the display fast with many elements.
public final class PictureDrawerDemo {

	private static final Map<String, Color> COLORS = new LinkedHashMap<>();
	static {
		COLORS.put("Black", Color.BLACK);
		COLORS.put("Red", new Color(200, 30, 30));
		COLORS.put("Blue", new Color(30, 80, 200));
		COLORS.put("Green", new Color(30, 140, 50));
		COLORS.put("Orange", new Color(240, 140, 0));
		COLORS.put("Purple", new Color(130, 40, 160));
	}

	private final JFrame frame = new JFrame("viewToolKit demo: picture drawer");

	private final GridDrawing drawing = new GridDrawing(100, 100);

	private final JGraphicalElementDisplayer<GridDrawing> display = new JGraphicalElementDisplayer<>(drawing);

	private final JLabel help = new JLabel(" ");

	private final PictureDrawerControl control = new PictureDrawerControl(display, help::setText);

	private PictureDrawerDemo() {
		drawing.setMinimalSize(1600, 1200);
		addSampleLines();
		help.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
		frame.getContentPane().add(buildToolBar(), BorderLayout.NORTH);
		frame.getContentPane().add(new JScrollPane(display), BorderLayout.CENTER);
		frame.getContentPane().add(help, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(1000, 750);
		frame.setLocationByPlatform(true);
	}

	/// Opens a new drawer window. Must be called on the event dispatch thread.
	public static void open() {
		PictureDrawerDemo demo = new PictureDrawerDemo();
		demo.frame.setVisible(true);
		demo.display.requestFocusInWindow();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(PictureDrawerDemo::open);
	}

	private JToolBar buildToolBar() {
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		ButtonGroup group = new ButtonGroup();
		addTool(bar, group, "Select", Tool.SELECT, false);
		addTool(bar, group, "Open line", Tool.OPEN_LINE, true);
		addTool(bar, group, "Closed line", Tool.CLOSED_LINE, false);
		bar.addSeparator();

		JComboBox<String> colors = new JComboBox<>(COLORS.keySet().toArray(new String[0]));
		colors.setMaximumSize(colors.getPreferredSize());
		colors.addActionListener(e -> control.setLineColor(COLORS.get((String) colors.getSelectedItem())));
		bar.add(new JLabel(" Color "));
		bar.add(colors);

		JSpinner width = new JSpinner(new SpinnerNumberModel(2.0, 0.5, 30.0, 0.5));
		width.setMaximumSize(width.getPreferredSize());
		width.addChangeListener(e -> control.setLineWidth((Double) width.getValue()));
		bar.add(new JLabel(" Width "));
		bar.add(width);

		JCheckBox fill = new JCheckBox("Fill closed lines");
		fill.addActionListener(e -> control.setFilled(fill.isSelected()));
		bar.add(fill);
		bar.addSeparator();

		bar.add(button("Front", control::bringSelectionToFront));
		bar.add(button("Back", control::sendSelectionToBack));
		bar.add(button("Delete", control::deleteSelection));
		bar.add(button("Clear", control::clear));
		bar.addSeparator();
		bar.add(button("Zoom +", () -> display.setScale(display.getScale() * 1.25)));
		bar.add(button("Zoom −", () -> display.setScale(display.getScale() / 1.25)));
		bar.add(button("Add 10 000 random lines", this::addRandomLines));
		return bar;
	}

	private void addTool(JToolBar bar, ButtonGroup group, String label, Tool tool, boolean selected) {
		JToggleButton button = new JToggleButton(label, selected);
		button.addActionListener(e -> {
			control.setTool(tool);
			display.requestFocusInWindow();
		});
		group.add(button);
		bar.add(button);
	}

	private JButton button(String label, Runnable action) {
		JButton button = new JButton(label);
		button.addActionListener(e -> {
			action.run();
			display.requestFocusInWindow();
		});
		return button;
	}

	private void addSampleLines() {
		// A house (closed lines) and a path (an open line).
		PolylineElement walls = polyline(true, 120, 300, 320, 300, 320, 450, 120, 450);
		walls.setFillColor(new Color(240, 220, 170));
		PolylineElement roof = polyline(true, 100, 300, 220, 200, 340, 300);
		roof.setLineColor(new Color(200, 30, 30));
		roof.setFillColor(new Color(200, 30, 30, 90));
		PolylineElement door = polyline(false, 200, 450, 200, 380, 240, 380, 240, 450);
		PolylineElement path = polyline(false, 220, 450, 260, 520, 330, 560, 450, 580, 600, 570);
		path.setLineColor(new Color(150, 110, 60));
		path.setLineWidth(4);
		for (PolylineElement line : List.of(walls, roof, door, path))
			drawing.addElement(line);
	}

	private static PolylineElement polyline(boolean closed, double... coordinates) {
		List<Point2D> points = new ArrayList<>();
		for (int i = 0; i + 1 < coordinates.length; i += 2)
			points.add(new Point2D.Double(coordinates[i], coordinates[i + 1]));
		PolylineElement line = new PolylineElement(points, closed);
		line.setLineWidth(2);
		return line;
	}

	private void addRandomLines() {
		Random random = new Random();
		List<Color> palette = new ArrayList<>(COLORS.values());
		for (int i = 0; i < 10_000; i++) {
			double x = random.nextDouble() * 5000, y = random.nextDouble() * 4000;
			List<Point2D> points = new ArrayList<>();
			int n = 2 + random.nextInt(4);
			for (int k = 0; k < n; k++)
				points.add(new Point2D.Double(x + random.nextGaussian() * 25, y + random.nextGaussian() * 25));
			PolylineElement line = new PolylineElement(points, random.nextBoolean());
			line.setLineColor(palette.get(random.nextInt(palette.size())));
			drawing.addElement(line);
		}
	}
}

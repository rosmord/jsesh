package org.qenherkhopeshef.viewToolKit.demo.text;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import org.qenherkhopeshef.viewToolKit.JGraphicalElementDisplayer;
import org.qenherkhopeshef.viewToolKit.drawing.element.ImageElement;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingEvent;
import org.qenherkhopeshef.viewToolKit.drawing.textOriented.TextDocument;
import org.qenherkhopeshef.viewToolKit.drawing.textOriented.TextDrawing;
import org.qenherkhopeshef.viewToolKit.drawing.textOriented.TextPosition;

/// Demo 1: a simple text editor, with embedded pictures, able to edit very
/// long texts.
///
/// - the model is a [TextDocument];
/// - the view is a [TextDrawing] displayed in a [JGraphicalElementDisplayer];
/// - [TextEditorControl] handles the keyboard and the mouse.
///
/// Try "File > Generate long text": the text is displayed immediately, as
/// only the visible paragraphs are laid out. The others are laid out in the
/// background (see the status bar), which progressively makes the scroll bar
/// exact.
public final class TextEditorDemo {

	private static final String WELCOME = """
			Welcome to the viewToolKit text editor demo.
			This editor is built on the viewToolKit framework: the text is a TextDocument, displayed by a TextDrawing \
			in a JGraphicalElementDisplayer. The text is wrapped to the window width; resize the window to see it \
			follow.
			You can type, select with the mouse or with shift + arrows, copy and paste (plain text), and insert \
			pictures with the Insert menu. Pictures are ordinary graphical elements, laid out like big characters. \
			Here is one: ￼ and here is a vector star: ￼ which is a PolylineElement, the same element \
			used by the picture drawer demo.
			To check that the editor copes with very long texts, use "File > Generate long text". Only the visible \
			paragraphs are laid out; the others get an estimated height, which is refined in the background (see \
			the status bar).
			Keys: arrows, Home/End, Page Up/Down, menu-shortcut + arrows for line/document start and end, \
			double-click selects a word, triple-click a paragraph.""";

	private static final String LOREM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod "
			+ "tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud "
			+ "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in "
			+ "reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint "
			+ "occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

	private final JFrame frame = new JFrame("viewToolKit demo: text editor");

	private final TextDocument document = new TextDocument();

	private final TextDrawing drawing = new TextDrawing(document);

	private final JGraphicalElementDisplayer<TextDrawing> display = new JGraphicalElementDisplayer<>(drawing);

	private final JScrollPane scrollPane = new JScrollPane(display);

	private final JLabel status = new JLabel(" ");

	private final TextEditorControl control = new TextEditorControl(display);

	/// Lays out the remaining paragraphs when the application is idle.
	private final Timer backgroundLayout = new Timer(25, e -> layoutInBackground());

	private TextEditorDemo() {
		fillWelcomeText();
		display.setTracksViewportWidth(true);
		display.setUnitIncrement(24);
		display.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				adaptPageWidth();
			}
		});
		drawing.addDrawingListener(this::drawingChanged);

		status.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
		frame.setJMenuBar(buildMenus());
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		frame.getContentPane().add(status, BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setSize(800, 700);
		frame.setLocationByPlatform(true);
		updateStatus();
	}

	/// Opens a new editor window. Must be called on the event dispatch thread.
	public static void open() {
		TextEditorDemo demo = new TextEditorDemo();
		demo.frame.setVisible(true);
		demo.display.requestFocusInWindow();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(TextEditorDemo::open);
	}

	private void fillWelcomeText() {
		TextDocument.Builder builder = new TextDocument.Builder();
		// The builder drops object replacement characters from text: split
		// the text on them, and insert the elements in their place.
		String[] parts = WELCOME.split("￼", -1);
		builder.appendText(parts[0]).appendElement(SamplePictures.landscapeElement(0));
		builder.appendText(parts[1]).appendElement(SamplePictures.star(28));
		builder.appendText(parts[2]);
		document.setContent(builder);
	}

	private void adaptPageWidth() {
		double available = display.getWidth() / display.getScale() - drawing.getPageMargin().getTotalMarginWidth()
				- 12;
		drawing.setPageWidth(Math.max(150, available));
	}

	private void drawingChanged(DrawingEvent e) {
		updateStatus();
		if (drawing.getLayoutCompletion() < 1 && !backgroundLayout.isRunning())
			backgroundLayout.start();
	}

	private void updateStatus() {
		TextPosition caret = drawing.getCaret();
		String layout = drawing.getLayoutCompletion() < 1
				? String.format("   ·   layout: %.1f %% exact", 100 * drawing.getLayoutCompletion())
				: "";
		status.setText(String.format("Paragraph %,d / %,d   ·   offset %d%s", caret.paragraph() + 1,
				drawing.getParagraphCount(), caret.offset(), layout));
	}

	/// One slice of background layout.
	///
	/// Correcting the height of paragraphs above the visible area would move
	/// the visible text; we compensate by scrolling, so that the paragraph at
	/// the top of the window stays in place.
	private void layoutInBackground() {
		if (scrollPane.getVerticalScrollBar().getValueIsAdjusting())
			return; // don't fight the user.
		Rectangle visible = display.getVisibleRect();
		double top = visible.y / display.getScale();
		int anchor = drawing.getParagraphIndexAt(top);
		double delta = top - drawing.getParagraphTop(anchor);
		boolean more = drawing.layoutPendingParagraphs(8_000_000L);
		double newTop = drawing.getParagraphTop(anchor) + delta;
		if (Math.abs(newTop - top) >= 1) {
			scrollPane.validate(); // apply the new display size before scrolling.
			scrollPane.getViewport().setViewPosition(
					new Point(visible.x, (int) Math.round(newTop * display.getScale())));
		}
		if (!more)
			backgroundLayout.stop();
	}

	// ------------------------------------------------------------------
	// Menus.

	private JMenuBar buildMenus() {
		int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		JMenuBar bar = new JMenuBar();

		JMenu file = new JMenu("File");
		file.add(item("New", KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut), e -> document.setText("")));
		file.add(item("Open text file…", KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut), e -> openTextFile()));
		file.add(item("Generate long text…", null, e -> generateLongText()));
		file.addSeparator();
		file.add(item("Close", KeyStroke.getKeyStroke(KeyEvent.VK_W, shortcut), e -> frame.dispose()));
		bar.add(file);

		// Shortcuts for these are handled by the menu, not by the control.
		JMenu edit = new JMenu("Edit");
		edit.add(item("Cut", KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcut), e -> control.cut()));
		edit.add(item("Copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcut), e -> control.copy()));
		edit.add(item("Paste", KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcut), e -> control.paste()));
		edit.addSeparator();
		edit.add(item("Select all", KeyStroke.getKeyStroke(KeyEvent.VK_A, shortcut), e -> control.selectAll()));
		bar.add(edit);

		JMenu insert = new JMenu("Insert");
		insert.add(item("Picture from file…", null, e -> insertPictureFromFile()));
		insert.add(item("Sample picture", null,
				e -> control.insertElement(SamplePictures.landscapeElement(new Random().nextInt(20)))));
		insert.add(item("Vector star", null, e -> control.insertElement(SamplePictures.star(28))));
		bar.add(insert);

		JMenu view = new JMenu("View");
		view.add(item("Bigger text", KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, shortcut), e -> changeFontSize(2)));
		view.add(item("Smaller text", KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, shortcut), e -> changeFontSize(-2)));
		bar.add(view);
		return bar;
	}

	private static JMenuItem item(String label, KeyStroke key, ActionListener action) {
		JMenuItem item = new JMenuItem(label);
		if (key != null)
			item.setAccelerator(key);
		item.addActionListener(action);
		return item;
	}

	private void changeFontSize(int delta) {
		Font font = drawing.getFont();
		float size = Math.max(8, Math.min(72, font.getSize2D() + delta));
		TextPosition caret = drawing.getCaret();
		drawing.setFont(font.deriveFont(size));
		drawing.setCaret(caret);
	}

	private void openTextFile() {
		JFileChooser chooser = new JFileChooser();
		if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
			return;
		File f = chooser.getSelectedFile();
		try {
			String text;
			try {
				text = Files.readString(f.toPath(), StandardCharsets.UTF_8);
			} catch (CharacterCodingException e) {
				text = Files.readString(f.toPath(), StandardCharsets.ISO_8859_1);
			}
			document.setText(text);
			frame.setTitle("viewToolKit demo: " + f.getName());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, "Can't read " + f + ": " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void generateLongText() {
		String answer = JOptionPane.showInputDialog(frame, "Number of paragraphs:", "100000");
		if (answer == null)
			return;
		int count;
		try {
			count = Integer.parseInt(answer.trim().replace("_", "").replace(",", "").replace(" ", ""));
		} catch (NumberFormatException e) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			Random random = new Random(1);
			// A few pictures, shared by all the paragraphs which display them.
			ImageElement[] pictures = new ImageElement[4];
			for (int i = 0; i < pictures.length; i++)
				pictures[i] = new ImageElement(SamplePictures.landscape(i));
			String[] words = LOREM.split(" ");
			TextDocument.Builder builder = new TextDocument.Builder();
			StringBuilder paragraph = new StringBuilder();
			for (int i = 0; i < count; i++) {
				if (i > 0)
					builder.newParagraph();
				paragraph.setLength(0);
				paragraph.append("§ ").append(String.format("%,d", i + 1)).append(". ");
				int length = 5 + random.nextInt(120);
				for (int w = 0; w < length; w++)
					paragraph.append(words[random.nextInt(words.length)]).append(' ');
				builder.appendText(paragraph.toString());
				if (i % 250 == 7)
					builder.appendText(" Picture: ").appendElement(pictures[i % pictures.length]);
			}
			document.setContent(builder);
			frame.setTitle("viewToolKit demo: " + String.format("%,d", count) + " paragraphs");
		} finally {
			frame.setCursor(Cursor.getDefaultCursor());
		}
	}

	private void insertPictureFromFile() {
		JFileChooser chooser = new JFileChooser();
		if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION)
			return;
		try {
			BufferedImage image = ImageIO.read(chooser.getSelectedFile());
			if (image == null) {
				JOptionPane.showMessageDialog(frame, "Not a picture file.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			double maxWidth = Math.max(50, drawing.getPageWidth() - 10);
			control.insertElement(new ImageElement(image, maxWidth, 400));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, "Can't read the picture: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}

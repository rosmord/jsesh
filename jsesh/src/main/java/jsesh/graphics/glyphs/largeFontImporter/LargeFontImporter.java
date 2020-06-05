package jsesh.graphics.glyphs.largeFontImporter;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jsesh.graphics.glyphs.ui.ShapeDisplayer;
import jsesh.hieroglyphs.graphics.ShapeChar;

import org.qenherkhopeshef.guiFramework.PropertyHolder;
import org.qenherkhopeshef.guiFramework.SimpleApplicationFactory;

public class LargeFontImporter implements PropertyHolder {

	public class GlyphSelectionListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent arg0) {
			if (fontTable.getSelectedRow() != -1) {
				ShapeChar shape = model.getShapeCharForPos(fontTable
						.getSelectedRow());
				shapeDisplayer.setShape(shape);
			}
		}

	}

	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
			this);

	private JTable fontTable = new JTable();

	private ShapeDisplayer shapeDisplayer = new ShapeDisplayer();

	private LargeFontImporterModel model = new LargeFontImporterModel();

	private JFrame jframe;

	private JButton setSizeButton;

	public LargeFontImporter() throws IOException {
		SimpleApplicationFactory fw = new SimpleApplicationFactory(
				"jsesh.graphics.glyphs.largeFontImporter.largeFontImporter8n", //$NON-NLS-1$ 
				"menu.txt", this); //$NON-NLS-1$

		fontTable.setRowHeight(60);
		fontTable.setModel(model);
		fontTable.setDefaultRenderer(FontGlyph.class, new FontGlyphRenderer());
		fontTable.getSelectionModel().addListSelectionListener(
				new GlyphSelectionListener());

		shapeDisplayer.addMouseMotionListener(new ResizerListener());

		setSizeButton = new JButton("zzzzz"); //$NON-NLS-1$
		jframe = new JFrame(fw.getAppDefaults().getString("APPLICATION.LABEL")); //$NON-NLS-1$
		jframe.setJMenuBar(fw.getJMenuBar());
		jframe.getContentPane().setLayout(new BorderLayout());
		jframe.getContentPane().add(new JScrollPane(fontTable),
				BorderLayout.CENTER);

		JPanel resizePanel = new JPanel();
		resizePanel.setLayout(new BoxLayout(resizePanel, BoxLayout.LINE_AXIS));
		resizePanel.add(shapeDisplayer);
		resizePanel.add(setSizeButton);
		jframe.getContentPane().add(resizePanel, BorderLayout.SOUTH);

		jframe.pack();
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true);

	}

	public void importSystemFont() {
		String[] availableFonts = getAvailableFonts();
		String result = (String) JOptionPane
				.showInputDialog(
						null,
						Messages.getString("LargeFontImporter.CHOOSE_A_FONT"), Messages.getString("LargeFontImporter.FONT_SELECTOR"), JOptionPane.QUESTION_MESSAGE, //$NON-NLS-1$ //$NON-NLS-2$
						null, availableFonts, null);
		if (result != null) {
			Font f = new Font(result, Font.PLAIN, 48);
			// Font f = result.deriveFont(48f);
			model.setFont(f);
			model.setFontName(result);
			shapeDisplayer.setShapeScale(model.getShapeScale());
		}
	}

	public void importFileFont() {
		JFileChooser chooser = new JFileChooser();
		if (chooser.showOpenDialog(jframe) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			try {
				FileInputStream in = new FileInputStream(file);
				Font f = Font.createFont(Font.TRUETYPE_FONT, in);
				model.setFont(f);
			} catch (FontFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String[] getAvailableFonts() {
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		return ge.getAvailableFontFamilyNames();
	}

	public static void main(String[] args) throws InterruptedException,
			InvocationTargetException {
		SwingUtilities.invokeAndWait(() -> {
                    try {
                        new LargeFontImporter();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
	}

	public void exportFontIntoJSesh() {
		try {
			if (JOptionPane
					.showConfirmDialog(
							null,
							Messages
									.getString("LargeFontImporter.DO_YOU_WANT_EXPORT_FONT"), Messages.getString("LargeFontImporter.CONFIRM_EXPORT"), //$NON-NLS-1$ //$NON-NLS-2$
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
				model.exportSigns();
		} catch (DuplicateEntriesException e) {
			JOptionPane
					.showMessageDialog(
							null,
							"", Messages.getString("LargeFontImporter.DUPLICATE_CODES"), //$NON-NLS-1$ //$NON-NLS-2$
							JOptionPane.ERROR_MESSAGE);
		}
	}

	public void saveDescriptionFile() {
		JFileChooser chooser = new JFileChooser();

		if (chooser.showSaveDialog(jframe) == JFileChooser.APPROVE_OPTION) {
			try {
				FileOutputStream out = new FileOutputStream(chooser
						.getSelectedFile());
				model.saveFile(out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void openDescriptionFile() {
		JFileChooser chooser = new JFileChooser();

		if (chooser.showOpenDialog(jframe) == JFileChooser.APPROVE_OPTION) {
			try {
				FileInputStream in = new FileInputStream(chooser
						.getSelectedFile());
				model.loadFile(in);
				shapeDisplayer.setShapeScale(model.getShapeScale());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return propertyChangeSupport;
	}

	public void exit() {
		if (!model.isSaved()) {
			if (JOptionPane
					.showConfirmDialog(
							jframe,
							Messages
									.getString("LargeFontImporter.UNSAVED_DATA_EXISTS"), Messages.getString("LargeFontImporter.CONFIRM_EXIT"), JOptionPane.YES_NO_OPTION) //$NON-NLS-1$ //$NON-NLS-2$
			== JOptionPane.YES_OPTION) {
				System.exit(0);
			}
		} else
			System.exit(0);
	}

	private class ResizerListener implements MouseMotionListener {

		public void mouseDragged(MouseEvent e) {
			double y = shapeDisplayer.convertToModelY(e.getY());
			double scale = y / shapeDisplayer.getShape().getBbox().getHeight();
			model.setShapeScale(scale);
			shapeDisplayer.setShapeScale(scale);
		}

		public void mouseMoved(MouseEvent e) {
		}

	}
}

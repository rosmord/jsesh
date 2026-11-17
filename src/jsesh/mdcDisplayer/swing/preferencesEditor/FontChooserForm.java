package jsesh.mdcDisplayer.swing.preferencesEditor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FontChooserForm extends JPanel {
	JButton browseHieroglyphs = new JButton();

	JButton browseLatin = new JButton();

	JButton browseItalic = new JButton();

	JButton browseBold = new JButton();

	JButton browseTranslitteration = new JButton();

	JFormattedTextField hieroglyphicFontDirectoryField = new JFormattedTextField();

	JTextField latinFontField = new JTextField();

	JTextField italicFontField = new JTextField();

	JTextField boldFontField = new JTextField();

	JTextField translitterationFontField = new JTextField();

	/**
	 * Default constructor
	 */
	public FontChooserForm() {
		initializePanel();
	}

	/**
	 * Adds fill components to empty cells in the first row and first column of
	 * the grid. This ensures that the grid spacing will be the same as shown in
	 * the designer.
	 * 
	 * @param cols
	 *            an array of column indices in the first row where fill
	 *            components should be added.
	 * @param rows
	 *            an array of row indices in the first column where fill
	 *            components should be added.
	 */
	void addFillComponents(Container panel, int[] cols, int[] rows) {
		Dimension filler = new Dimension(10, 10);

		boolean filled_cell_11 = false;
		CellConstraints cc = new CellConstraints();
		if (cols.length > 0 && rows.length > 0) {
			if (cols[0] == 1 && rows[0] == 1) {
				/** add a rigid area */
				panel.add(Box.createRigidArea(filler), cc.xy(1, 1));
				filled_cell_11 = true;
			}
		}

		for (int index = 0; index < cols.length; index++) {
			if (cols[index] == 1 && filled_cell_11) {
				continue;
			}
			panel.add(Box.createRigidArea(filler), cc.xy(cols[index], 1));
		}

		for (int index = 0; index < rows.length; index++) {
			if (rows[index] == 1 && filled_cell_11) {
				continue;
			}
			panel.add(Box.createRigidArea(filler), cc.xy(1, rows[index]));
		}

	}

	/**
	 * Helper method to load an image file from the CLASSPATH
	 * 
	 * @param imageName
	 *            the package and name of the file to load relative to the
	 *            CLASSPATH
	 * @return an ImageIcon instance with the specified image file
	 * @throws IllegalArgumentException
	 *             if the image resource cannot be loaded.
	 */
	public ImageIcon loadImage(String imageName) {
		try {
			ClassLoader classloader = getClass().getClassLoader();
			java.net.URL url = classloader.getResource(imageName);
			if (url != null) {
				ImageIcon icon = new ImageIcon(url);
				return icon;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException("Unable to load image: " + imageName);
	}

	public JPanel createPanel() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE",
				"CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		JLabel jlabel1 = new JLabel();
		jlabel1.setText("Hieroglyphic font directory");
		jpanel1.add(jlabel1, cc.xy(2, 2));

		browseHieroglyphs.setActionCommand("Browse");
		browseHieroglyphs.setName("browseHieroglyphs");
		browseHieroglyphs.setText("Browse");
		jpanel1.add(browseHieroglyphs, cc.xy(6, 2));

		JLabel jlabel2 = new JLabel();
		jlabel2.setEnabled(false);
		jlabel2.setText("Latin font");
		jpanel1.add(jlabel2, cc.xy(2, 4));

		JLabel jlabel3 = new JLabel();
		jlabel3.setEnabled(false);
		jlabel3.setText("Italic font");
		jpanel1.add(jlabel3, cc.xy(2, 6));

		JLabel jlabel4 = new JLabel();
		jlabel4.setEnabled(false);
		jlabel4.setText("Bold font");
		jpanel1.add(jlabel4, cc.xy(2, 8));

		JLabel jlabel5 = new JLabel();
		jlabel5.setEnabled(false);
		jlabel5.setText("Translitteration font");
		jpanel1.add(jlabel5, cc.xy(2, 10));

		browseLatin.setActionCommand("Browse");
		browseLatin.setEnabled(false);
		browseLatin.setName("browseLatin");
		browseLatin.setText("Browse");
		jpanel1.add(browseLatin, cc.xy(6, 4));

		browseItalic.setActionCommand("Browse");
		browseItalic.setEnabled(false);
		browseItalic.setName("browseItalic");
		browseItalic.setText("Browse");
		jpanel1.add(browseItalic, cc.xy(6, 6));

		browseBold.setActionCommand("Browse");
		browseBold.setEnabled(false);
		browseBold.setName("browseBold");
		browseBold.setText("Browse");
		jpanel1.add(browseBold, cc.xy(6, 8));

		browseTranslitteration.setActionCommand("Browse");
		browseTranslitteration.setEnabled(false);
		browseTranslitteration.setName("browseTranslitteration");
		browseTranslitteration.setText("Browse");
		jpanel1.add(browseTranslitteration, cc.xy(6, 10));

		hieroglyphicFontDirectoryField.setColumns(30);
		hieroglyphicFontDirectoryField
				.setName("hieroglyphicFontDirectoryField");
		jpanel1.add(hieroglyphicFontDirectoryField, cc.xy(4, 2));

		latinFontField.setEnabled(false);
		latinFontField.setName("latinFontField");
		jpanel1.add(latinFontField, cc.xy(4, 4));

		italicFontField.setEnabled(false);
		italicFontField.setName("italicFontField");
		jpanel1.add(italicFontField, cc.xy(4, 6));

		boldFontField.setEnabled(false);
		boldFontField.setName("boldFontField");
		jpanel1.add(boldFontField, cc.xy(4, 8));

		translitterationFontField.setEnabled(false);
		translitterationFontField.setName("translitterationFontField");
		jpanel1.add(translitterationFontField, cc.xy(4, 10));

		addFillComponents(jpanel1, new int[] { 1, 2, 3, 4, 5, 6, 7 },
				new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 });
		return jpanel1;
	}

	public JTextField getBoldFontField() {
		return boldFontField;
	}

	public JButton getBrowseBold() {
		return browseBold;
	}

	public JButton getBrowseHieroglyphs() {
		return browseHieroglyphs;
	}

	public JButton getBrowseItalic() {
		return browseItalic;
	}

	public JButton getBrowseLatin() {
		return browseLatin;
	}

	public JButton getBrowseTranslitteration() {
		return browseTranslitteration;
	}

	public JFormattedTextField getHieroglyphicFontDirectoryField() {
		return hieroglyphicFontDirectoryField;
	}

	public JTextField getItalicFontField() {
		return italicFontField;
	}

	public JTextField getLatinFontField() {
		return latinFontField;
	}

	public JTextField getTranslitterationFontField() {
		return translitterationFontField;
	}

	/**
	 * Initializer
	 */
	protected void initializePanel() {
		setLayout(new BorderLayout());
		add(createPanel(), BorderLayout.CENTER);
	}

}

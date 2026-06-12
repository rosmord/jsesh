/*
 * JSimplePalette.java
 *
 * Created on 2 juillet 2007, 15:10
 * 
 * ported to miglayout.
 * 
 * TODO: internationalize the labels.
 * @author Serge Rosmorduc
 */

package jsesh.swing.signPalette;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.qenherkhopeshef.swingUtils.lists.ListItem;

import jsesh.hieroglyphs.data.HieroglyphFamily;
import net.miginfocom.swing.MigLayout;

/**
 * Simple palette window.
 * @author  rosmord
 */
@SuppressWarnings("serial")
public class JSimplePalette extends JPanel {


    private JButton backButton;
    private JComboBox<ListItem<HieroglyphFamily>> categoryChooserCB;
    private JComboBox<CharSequence> containsCB;
    private JLabel glyphCodeLabel;
    private JEditorPane glyphInfoText;
    private JLabel glyphPictureLabel;
    private JCheckBox inUserPaletteCheckBox;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JPanel jPanel1;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JSeparator jSeparator1;
    private JComboBox<String> secondaryTagCB;
    private JCheckBox showAllCheckBox;
    private JButton showContainingButtons;
    private JButton showVariantsButton;
    private JList<String> signTable;
    private JComboBox<String> tagChooserCB;
    private JTextField translitterationFilterField;
    
    /** Creates new form JSimplePalette */
    public JSimplePalette() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        showContainingButtons = new JButton();
        inUserPaletteCheckBox = new JCheckBox();
        showVariantsButton = new JButton();
        categoryChooserCB = new JComboBox<>();
        jScrollPane1 = new JScrollPane();
        glyphInfoText = new JEditorPane();
        showAllCheckBox = new JCheckBox();
        jLabel1 = new JLabel();
        jPanel1 = new JPanel();
        glyphCodeLabel = new JLabel();
        glyphPictureLabel = new JLabel();
        translitterationFilterField = new JTextField();
        tagChooserCB = new JComboBox<>();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jScrollPane2 = new JScrollPane();
        signTable = new JList<>();
        jLabel4 = new JLabel();
        secondaryTagCB = new JComboBox<>();
        jSeparator1 = new JSeparator();
        backButton = new JButton();
        containsCB = new JComboBox<>();

        showContainingButtons.setIcon(new ImageIcon(getClass().getResource("/jsesh/swing/signPalette/partof.png"))); // NOI18N
        showContainingButtons.setToolTipText("Display signs which contain the selected sign");

        inUserPaletteCheckBox.setText("user Pal.");
        inUserPaletteCheckBox.setToolTipText("sign should appear in user palette.");

        showVariantsButton.setIcon(new ImageIcon(getClass().getResource("/jsesh/swing/signPalette/var.png"))); // NOI18N
        showVariantsButton.setToolTipText("Display variants of sign");


        glyphInfoText.setEditable(false);
        jScrollPane1.setViewportView(glyphInfoText);

        showAllCheckBox.setText("Show all");
        showAllCheckBox.setToolTipText("Display all signs in the category, including variants");

        jLabel1.setText("Search");

        glyphCodeLabel.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // Or take standard and change size.
        glyphCodeLabel.setText("        ");

        glyphPictureLabel.setBackground(java.awt.Color.WHITE);
        glyphPictureLabel.setBorder(BorderFactory.createEtchedBorder());
        
        jPanel1.setLayout(new MigLayout("insets 0", "[grow, fill]", ""));
        jPanel1.add(glyphPictureLabel, "growx, w 200!, h 74!, wrap");
        jPanel1.add(glyphCodeLabel);

        translitterationFilterField.setColumns(5);
        translitterationFilterField.setToolTipText("Filter for translitteration\n");

        tagChooserCB.setToolTipText("Allows to select a sub-category of a hieroglyph family.");

        jLabel2.setText("Family");
        jLabel3.setText("Sub-Family");
        jLabel4.setText("Sub-Sub-Family");

        signTable.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        signTable.setVisibleRowCount(-1);
        jScrollPane2.setViewportView(signTable);

        backButton.setIcon(new ImageIcon(getClass().getResource("/jsesh/swing/signPalette/stock_left-16.png"))); // NOI18N
        backButton.setToolTipText("<html>previously selected glyphs\n(JSesh remembers the last three selected signs).\n");

        setLayout(new MigLayout("insets dialog", "[trailing][grow, fill]", ""));

        add(jLabel2, "sg lbl");
        add(categoryChooserCB, "growx, wrap");

        add(jLabel3, "sg lbl");
        add(tagChooserCB, "growx, wrap");

        add(jLabel4, "sg lbl");
        add(secondaryTagCB, "growx, wrap");

        add(showAllCheckBox);
        add(containsCB, "split 3, growx");
        add(jLabel1);
        add(translitterationFilterField, "wrap");

        add(jScrollPane2, "span 2, grow, push, wrap");

        add(jSeparator1, "span 2, growx, wrap");

        add(jPanel1, "aligny top");
        add(jScrollPane1, "grow, h 108!, wrap");

        add(showVariantsButton, "span 2, split 4");
        add(inUserPaletteCheckBox);
        add(backButton);
        add(showContainingButtons, "gapleft push");
    }

    
	public JComboBox<ListItem<HieroglyphFamily>> getCategoryChooserCB() {
		return categoryChooserCB;
	}

	public JLabel getGlyphCodeLabel() {
		return glyphCodeLabel;
	}

	public JEditorPane getGlyphInfoText() {
		return glyphInfoText;
	}

	public JLabel getGlyphPictureLabel() {
		return glyphPictureLabel;
	}

    public JList<String> getSignTable() {
        return signTable;
    }
    
    public JCheckBox getShowAllCheckBox() {
		return showAllCheckBox;
	}

	public JButton getShowContainingButtons() {
		return showContainingButtons;
	}

	public JButton getShowVariantsButton() {
		return showVariantsButton;
	}

	public JTextField getTransliterationFilterField() {
		return translitterationFilterField;
	}
    
	public JCheckBox getInUserPaletteCheckBox() {
		return inUserPaletteCheckBox;
	}
	
	public JComboBox<String> getTagChooserCB() {
		return tagChooserCB;
	}

    public JComboBox<String> getSecondaryTagCB() {
        return secondaryTagCB;
    }

    public JButton getBackButton() {
        return backButton;
    }

    public JComboBox<CharSequence> getContainsCB() {
        return containsCB;
    }
}

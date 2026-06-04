/*
 * JSimplePalette.java
 *
 * Created on 2 juillet 2007, 15:10
 */

package jsesh.swing.signPalette;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import net.miginfocom.swing.MigLayout;

import jsesh.hieroglyphs.data.HieroglyphFamily;

/**
 * Simple palette window.
 * @author  rosmord
 */
@SuppressWarnings("serial")
public class JSimplePalette extends javax.swing.JPanel {
    
    /** Creates new form JSimplePalette */
    public JSimplePalette() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        showContainingButtons = new javax.swing.JButton();
        inUserPaletteCheckBox = new javax.swing.JCheckBox();
        showVariantsButton = new javax.swing.JButton();
        categoryChooserCB = new javax.swing.JComboBox<>();
        jScrollPane1 = new javax.swing.JScrollPane();
        glyphInfoText = new javax.swing.JEditorPane();
        showAllCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        glyphCodeLabel = new javax.swing.JLabel();
        glyphPictureLabel = new javax.swing.JLabel();
        translitterationFilterField = new javax.swing.JTextField();
        tagChooserCB = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        signTable = new javax.swing.JList<>();
        jLabel4 = new javax.swing.JLabel();
        secondaryTagCB = new javax.swing.JComboBox<>();
        jSeparator1 = new javax.swing.JSeparator();
        backButton = new javax.swing.JButton();
        containsCB = new javax.swing.JComboBox<>();

        showContainingButtons.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/swing/signPalette/partof.png"))); // NOI18N
        showContainingButtons.setToolTipText("Display signs which contain the selected sign");

        inUserPaletteCheckBox.setText("user Pal.");
        inUserPaletteCheckBox.setToolTipText("sign should appear in user palette.");

        showVariantsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/swing/signPalette/var.png"))); // NOI18N
        showVariantsButton.setToolTipText("Display variants of sign");


        glyphInfoText.setEditable(false);
        jScrollPane1.setViewportView(glyphInfoText);

        showAllCheckBox.setText("Show all");
        showAllCheckBox.setToolTipText("Display all signs in the category, including variants");

        jLabel1.setText("Search");

        glyphCodeLabel.setFont(new java.awt.Font("Lucida Grande", 0, 18));
        glyphCodeLabel.setText("        ");

        glyphPictureLabel.setBackground(java.awt.Color.white);
        glyphPictureLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jPanel1.setLayout(new MigLayout("insets 0", "[grow, fill]", ""));
        jPanel1.add(glyphPictureLabel, "growx, h 74!, wrap");
        jPanel1.add(glyphCodeLabel);

        translitterationFilterField.setColumns(5);
        translitterationFilterField.setToolTipText("Filter for translitteration\n");

        tagChooserCB.setToolTipText("Allows to select a sub-category of a hieroglyph family.");

        jLabel2.setText("Family");
        jLabel3.setText("Sub-Family");
        jLabel4.setText("Sub-Sub-Family");

        signTable.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        signTable.setVisibleRowCount(-1);
        jScrollPane2.setViewportView(signTable);

        backButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/swing/signPalette/stock_left-16.png"))); // NOI18N
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

    private javax.swing.JButton backButton;
    private javax.swing.JComboBox<HieroglyphFamily> categoryChooserCB;
    private javax.swing.JComboBox<CharSequence> containsCB;
    private javax.swing.JLabel glyphCodeLabel;
    private javax.swing.JEditorPane glyphInfoText;
    private javax.swing.JLabel glyphPictureLabel;
    private javax.swing.JCheckBox inUserPaletteCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JComboBox<String> secondaryTagCB;
    private javax.swing.JCheckBox showAllCheckBox;
    private javax.swing.JButton showContainingButtons;
    private javax.swing.JButton showVariantsButton;
    private javax.swing.JList<String> signTable;
    private javax.swing.JComboBox<String> tagChooserCB;
    private javax.swing.JTextField translitterationFilterField;
    
	public javax.swing.JComboBox<HieroglyphFamily> getCategoryChooserCB() {
		return categoryChooserCB;
	}

	public javax.swing.JLabel getGlyphCodeLabel() {
		return glyphCodeLabel;
	}

	public javax.swing.JEditorPane getGlyphInfoText() {
		return glyphInfoText;
	}

	public javax.swing.JLabel getGlyphPictureLabel() {
		return glyphPictureLabel;
	}

    public JList<String> getSignTable() {
        return signTable;
    }
    
    public javax.swing.JCheckBox getShowAllCheckBox() {
		return showAllCheckBox;
	}

	public javax.swing.JButton getShowContainingButtons() {
		return showContainingButtons;
	}

	public javax.swing.JButton getShowVariantsButton() {
		return showVariantsButton;
	}

	public javax.swing.JTextField getTransliterationFilterField() {
		return translitterationFilterField;
	}
    
	public javax.swing.JCheckBox getInUserPaletteCheckBox() {
		return inUserPaletteCheckBox;
	}
	
	public javax.swing.JComboBox<String> getTagChooserCB() {
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

/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 *
 * JSimplePalette.java
 *
 * Created on 2 juillet 2007, 15:10
 * 
 * ported to miglayout.
 * 
 * TODO: internationalize the labels.
 * @author Serge Rosmorduc
 */

package jsesh.ui.palette;

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
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.qenherkhopeshef.swingUtils.lists.ListItem;

import jsesh.glyphs.signdata.HieroglyphFamily;
import jsesh.resources.JSeshMessages;
import jsesh.utils.swing.OrientationUtils;
import net.miginfocom.swing.MigLayout;

/**
 * Simple palette window.
 * 
 * @author rosmord
 */
@SuppressWarnings("serial")
public class JHieroglyphicPalette extends JPanel {

    private JButton backButton;
    private JComboBox<ListItem<HieroglyphFamily>> categoryChooserCB;
    private JComboBox<ListItem<String>> containsCB;
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
    private JSlider signSizeSlider;
    private JLabel smallSignSizeLabel;
    private JLabel largeSignSizeLabel;

    /** Creates new form JSimplePalette */
    public JHieroglyphicPalette() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to
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
        signSizeSlider = new JSlider();
        smallSignSizeLabel = new JLabel();
        largeSignSizeLabel = new JLabel();

        showContainingButtons.setIcon(new ImageIcon(getClass().getResource("/jsesh/ui/palette/partof.png"))); // NOI18N
        showContainingButtons.setToolTipText("Display signs which contain the selected sign");

        inUserPaletteCheckBox.setText("user Pal.");
        inUserPaletteCheckBox.setToolTipText("sign should appear in user palette.");

        showVariantsButton.setIcon(new ImageIcon(getClass().getResource("/jsesh/ui/palette/var.png"))); // NOI18N
        showVariantsButton.setToolTipText("Display variants of sign");

        glyphInfoText.setEditable(false);
        jScrollPane1.setViewportView(glyphInfoText);

        showAllCheckBox.setText("Show all");
        showAllCheckBox.setToolTipText("Display all signs in the category, including variants");

        jLabel1.setText("Search");

        // Derived from the look and feel's font, so that it follows the interface scale.
        java.awt.Font labelFont = UIManager.getFont("Label.font");
        if (labelFont != null) {
            glyphCodeLabel.setFont(labelFont.deriveFont(labelFont.getSize2D() * 1.4f));
        }
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

        backButton.setIcon(new ImageIcon(getClass().getResource("/jsesh/ui/palette/stock_left-16.png"))); // NOI18N
        backButton
                .setToolTipText("<html>previously selected glyphs\n(JSesh remembers the last three selected signs).\n");

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

        // The range and value of the slider are set by the presenter.
        signSizeSlider.setFocusable(false);
        signSizeSlider.setToolTipText(JSeshMessages.getString("palette.signSize.tooltip"));

        add(showVariantsButton, "span 2, split 7");
        add(inUserPaletteCheckBox);
        add(backButton);
        add(smallSignSizeLabel, "gapleft push, gapright 0");
        add(signSizeSlider, "w 60:100:140, gapleft 0, gapright 0");
        add(largeSignSizeLabel, "gapleft 0");
        add(showContainingButtons, "gapleft unrel");
        
        OrientationUtils.fixComponentOrientation(this);
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

    public JComboBox<ListItem<String>> getContainsCB() {
        return containsCB;
    }

    /// The slider which sets the height of the signs in the list.
    public JSlider getSignSizeSlider() {
        return signSizeSlider;
    }

    /// Legend of the size slider, on the "small" side.
    public JLabel getSmallSignSizeLabel() {
        return smallSignSizeLabel;
    }

    /// Legend of the size slider, on the "large" side.
    public JLabel getLargeSignSizeLabel() {
        return largeSignSizeLabel;
    }
}

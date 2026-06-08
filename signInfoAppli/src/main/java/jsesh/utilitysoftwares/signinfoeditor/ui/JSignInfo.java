/*
 * JSignInfo.java
 *
 * Created on 27 septembre 2007, 19:40
 */

package jsesh.utilitysoftwares.signinfoeditor.ui;

import javax.swing.JButton;

import jsesh.utilitysoftwares.signinfoeditor.model.SignInfoProperty;
import net.miginfocom.swing.MigLayout;

/**
 * Display for editing signs information.
 * 
 * @author  rosmord
 */
public class JSignInfo extends javax.swing.JPanel {
	
	/** Creates new form JSignInfo */
    public JSignInfo() {
        initComponents();
    }
    
    private void initComponents() {
        jLabel2 = new javax.swing.JLabel();
        signCodeField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        transliterationTable = new javax.swing.JTable();
        transliterationAddButton = new javax.swing.JButton();
        transliterationRemoveButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        previousButton = new javax.swing.JButton();
        nextButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        signGlyphLabel = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        variantRemoveButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        variantTable = new javax.swing.JTable();
        variantAddButton = new javax.swing.JButton();
        variantField = new javax.swing.JTextField();
        variantOfLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        partsTable = new javax.swing.JTable();
        partField = new javax.swing.JTextField();
        partsLabel = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        partAddButton = new javax.swing.JButton();
        partRemoveButton = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        descriptionField = new jsesh.editor.JMDCEditor();
        langSelectCB = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        previousDescriptionButton = new javax.swing.JButton();
        nextDescriptionButton = new javax.swing.JButton();
        removeDescriptionButton = new javax.swing.JButton();
        addDescriptionButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        tagList = new javax.swing.JList<>();
        jPanel7 = new javax.swing.JPanel();
        tagAddButton = new javax.swing.JButton();
        tagRemoveButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        availableTagList = new javax.swing.JList<>();
        copyPreviousSignTagsButton = new javax.swing.JButton();
        transliterationField = new javax.swing.JTextField();
        alwaysDisplayCheckBox = new javax.swing.JCheckBox();

        jLabel2.setText("Code"); // NOI18N

        signCodeField.setEditable(false);
        signCodeField.setText(" ");

       
        jScrollPane1.setViewportView(transliterationTable);

        transliterationAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_add.png"))); // NOI18N

        transliterationRemoveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_remove.png"))); // NOI18N

        jLabel1.setText("Transliteration"); // NOI18N

        previousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/previous.png"))); // NOI18N

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/next.png"))); // NOI18N

        // jPanel2: glyph display
        jPanel2.setLayout(new MigLayout("insets dialog, fill"));
        jPanel2.add(signGlyphLabel, "grow, h 100!");

        jTabbedPane1.setToolTipText("Free text description of the sign"); // NOI18N

        jPanel1.setToolTipText("<html>\n<p>The notion of variant used here is somehow ad-hoc.</p>\n<p>\tThe problem of variants is that there are two different notions behind it, both useful in our software.\n<p>\tThe first notion is LINGUISTIC variant. A sign is a linguistic variant of another one if it has the same uses.\n<p>\tFor instance, Y2 is a linguistic variant of Y1. Now, Y2 also \"looks like\" Y1. We will call it a \"graphical variation\".\n<p>\tBoth notions are independant, though statistically linked. For instance, Z7 is a linguistic variant of G43, but not a \n\tgraphical variation thereof.\n\t  \n<p> \tthe notion of \"looking like\" another sign is covered by the \"isSimilar\" attribute.\n\t \n<p>\tIn lots of cases, especially for determinatives, the signs are not always fully substitutable one for another.\n\tTo allow the use of 'variant' information in searches, we introduce the \"linguistic\" attribute.\n\t\n<p>\tlet B be a variant of A.\n<ul>\n<li>\t\"full\" means that all uses of B are also possible uses of A, and all uses of A are uses of B.\n<li>\t\"other\" means that B is more specific than A, or that the degree is unknown\n<li>\t\"partial\" means that the uses of A and B intersect, but they have also both significantly different uses.\n<p>\t\tFor instance, the D36 sign (ayin) is a partial variant of D37 (di), as D36 can write \"di\". However,\n\t\tin this case, I would not consider D37 as a variant of D36, because it would cause more harm than good.\n<li>\t\"no\" is used when the sign is not at all a linguistic variant. In this case, isSimilar is normally \"y\".\n</ul>\n\n</html>");

        variantRemoveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_remove.png"))); // NOI18N

        
        variantTable.setToolTipText("<html>\nFor an explanation of variants in JSesh, see the documentation. \n<p>It's a bit tricky.\n</html>");
        jScrollPane4.setViewportView(variantTable);

        variantAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_add.png"))); // NOI18N

        variantField.setColumns(5);

        variantOfLabel.setText("Variant of"); // NOI18N

 
        partsTable.setToolTipText("<html>\nList signs which are part of this one.\n<p>this sign contains a complex sign, don't list the latter's parts.</p>\n<p>Consider for instance a C98 Osiris figure, wearing the Atef crown ; <p>\n<p> you can list S8 (the Atef) as part of C98, but you should not list the Maat feather, which is part\nof S8 anyway.</p>\n<p>If it's useful, JSesh has enough information for retrieving it.</p>\n</html>");
        jScrollPane3.setViewportView(partsTable);

        partField.setColumns(5);

        partsLabel.setText("Parts"); // NOI18N

        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.PAGE_AXIS));

        partAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_add.png"))); // NOI18N
        jPanel9.add(partAddButton);

        partRemoveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_remove.png"))); // NOI18N
        jPanel9.add(partRemoveButton);

        // jPanel1: Relationships tab — parts (left) and variants (right), each with label+field row and scrollpane row
        jPanel1.setLayout(new MigLayout("insets dialog", "[][grow,fill][pref]unrelated*2[][grow,fill][pref]", "[][grow]"));
        jPanel1.add(partsLabel);
        jPanel1.add(partField, "growx");
        jPanel1.add(jPanel9, "spany 2, top");
        jPanel1.add(variantOfLabel);
        jPanel1.add(variantField, "growx");
        jPanel1.add(variantAddButton, "wrap");
        jPanel1.add(jScrollPane3, "span 2, grow");
        jPanel1.add(jScrollPane4, "span 2, grow");
        jPanel1.add(variantRemoveButton, "top");

        jTabbedPane1.addTab("Relationships", jPanel1);

        descriptionField.setToolTipText("Sign Description in Manuel de codage format. May contain any kind of information: sign values, bibliography..."); // NOI18N
        descriptionField.setScale(1.0);

        jScrollPane6.setViewportView(descriptionField);

        langSelectCB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "en", "fr", "de" }));

        jLabel7.setText("language: ");

        jLabel8.setText("Sign Description:");

        previousDescriptionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/previous.png"))); // NOI18N

        nextDescriptionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/next.png"))); // NOI18N

        removeDescriptionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_remove.png"))); // NOI18N

        addDescriptionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_add.png"))); // NOI18N

        // jPanel8: description navigation — prev/next on left, add/remove pushed to right
        jPanel8.setLayout(new MigLayout("insets 0"));
        jPanel8.add(previousDescriptionButton);
        jPanel8.add(nextDescriptionButton, "gapafter unrelated");
        jPanel8.add(addDescriptionButton, "gapleft push, gapafter unrelated");
        jPanel8.add(removeDescriptionButton);

        // jPanel3: Description tab — lang selector + nav buttons on top, description editor below
        jPanel3.setLayout(new MigLayout("insets dialog", "[][pref][grow,fill]", "[]18[]6[grow]"));
        jPanel3.add(jLabel7);
        jPanel3.add(langSelectCB, "wmax 187");
        jPanel3.add(jPanel8, "spany 2, top, wrap");
        jPanel3.add(jLabel8, "span 2, wrap");
        jPanel3.add(jScrollPane6, "span 3, grow");

        jTabbedPane1.addTab("Description", jPanel3);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("This sign's tags"));

        jScrollPane7.setViewportView(tagList);

        // jPanel6: sign's current tags list
        jPanel6.setLayout(new MigLayout("insets dialog, fill"));
        jPanel6.add(jScrollPane7, "grow");

        tagAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/next.png"))); // NOI18N

        tagRemoveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/previous.png"))); // NOI18N

        // jPanel7: tag transfer buttons (stacked vertically)
        jPanel7.setLayout(new MigLayout("insets 0, wrap 1"));
        jPanel7.add(tagAddButton, "growx");
        jPanel7.add(tagRemoveButton, "growx");

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Available tags"));

        jScrollPane2.setViewportView(availableTagList);

        // jPanel5: available tags list
        jPanel5.setLayout(new MigLayout("insets dialog, fill"));
        jPanel5.add(jScrollPane2, "grow");

        // jPanel10: [available tags] [transfer buttons] [sign's tags]
        jPanel10.setLayout(new MigLayout("insets dialog", "[grow,fill][pref][grow,fill]", "[grow]"));
        jPanel10.add(jPanel5, "grow");
        jPanel10.add(jPanel7, "aligny center");
        jPanel10.add(jPanel6, "grow");

        copyPreviousSignTagsButton.setText("Copy Previous Sign Tags");

        // jPanel4: Tags tab — tag selector at top, copy button at bottom
        jPanel4.setLayout(new MigLayout("insets dialog, wrap 1, fill", "[grow]", "[grow][]"));
        jPanel4.add(jPanel10, "grow");
        jPanel4.add(copyPreviousSignTagsButton, "al center");

        jTabbedPane1.addTab("tags", jPanel4);

        alwaysDisplayCheckBox.setText("basic sign");
        alwaysDisplayCheckBox.setToolTipText("<html>\n<p>States if the sign is part of the \"basic\" palette or the extended one.</p>\n<p>\nIf unchecked, the sign will be displayed in the palette <em>only</em> if\n\"display all\" is selected.\n</p>\n<p>\nCurrently, signs in the Gardiner list, as well as extended signs which are <em>not</em> variant\nof other signs are supposed to be basic signs.\n</p>\n</html>");

        // Main layout: [code label][code field] 14px gap [translit label][translit field][add/remove buttons]
        // Row 1: labels + fields; row 2: glyph panel + translit table (with remove button); row 3: checkbox;
        // row 4: tabbed pane (full width); row 5: prev/next navigation
        setLayout(new MigLayout("insets dialog", "[][grow,fill]14[][grow,fill][]", ""));
        add(jLabel2);
        add(signCodeField, "growx");
        add(jLabel1);
        add(transliterationField, "growx");
        add(transliterationAddButton, "wrap");
        add(jPanel2, "span 2, grow");
        add(jScrollPane1, "span 2, spany 2, grow");
        add(transliterationRemoveButton, "spany 2, top, wrap");
        add(alwaysDisplayCheckBox, "span 2, wrap");
        add(jTabbedPane1, "span 5, grow, gapy unrelated, wrap");
        add(previousButton, "tag back");
        add(nextButton, "tag next");
    }
    
    
    private javax.swing.JButton addDescriptionButton;
    private javax.swing.JCheckBox alwaysDisplayCheckBox;
    private javax.swing.JList<String> availableTagList;
    private javax.swing.JButton copyPreviousSignTagsButton;
    private jsesh.editor.JMDCEditor descriptionField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox<String> langSelectCB;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton nextDescriptionButton;
    private javax.swing.JButton partAddButton;
    private javax.swing.JTextField partField;
    private javax.swing.JButton partRemoveButton;
    private javax.swing.JLabel partsLabel;
    private javax.swing.JTable partsTable;
    private javax.swing.JButton previousButton;
    private javax.swing.JButton previousDescriptionButton;
    private javax.swing.JButton removeDescriptionButton;
    private javax.swing.JTextField signCodeField;
    private javax.swing.JLabel signGlyphLabel;
    private javax.swing.JButton tagAddButton;
    private javax.swing.JList<SignInfoProperty> tagList;
    private javax.swing.JButton tagRemoveButton;
    private javax.swing.JButton transliterationAddButton;
    private javax.swing.JTextField transliterationField;
    private javax.swing.JButton transliterationRemoveButton;
    private javax.swing.JTable transliterationTable;
    private javax.swing.JButton variantAddButton;
    private javax.swing.JTextField variantField;
    private javax.swing.JLabel variantOfLabel;
    private javax.swing.JButton variantRemoveButton;
    private javax.swing.JTable variantTable;

	/**
	 * @return the nextButton
	 */
	public javax.swing.JButton getNextButton() {
		return nextButton;
	}

	/**
	 * @return the partAddButton
	 */
	public javax.swing.JButton getPartAddButton() {
		return partAddButton;
	}

	/**
	 * @return the partField
	 */
	public javax.swing.JTextField getPartField() {
		return partField;
	}

	public javax.swing.JTable getPartsTable() {
		return partsTable;
	}

	/**
	 * @return the partRemoveButton
	 */
	public javax.swing.JButton getPartRemoveButton() {
		return partRemoveButton;
	}

	/**
	 * @return the previousButton
	 */
	public javax.swing.JButton getPreviousButton() {
		return previousButton;
	}

	/**
	 * @return the signCodeField
	 */
	public javax.swing.JTextField getSignCodeField() {
		return signCodeField;
	}

	/**
	 * @return the signGlyphLabel
	 */
	public javax.swing.JLabel getSignGlyphLabel() {
		return signGlyphLabel;
	}
	

	/**
	 * @return the transliterationAddButton
	 */
	public javax.swing.JButton getTransliterationAddButton() {
		return transliterationAddButton;
	}

	/**
	 * @return the transliterationRemoveButton
	 */
	public javax.swing.JButton getTransliterationRemoveButton() {
		return transliterationRemoveButton;
	}

	/**
	 * @return the transliterationTable
	 */
	public javax.swing.JTable getTransliterationTable() {
		return transliterationTable;
	}

	/**
	 * @return the variantAddButton
	 */
	public javax.swing.JButton getVariantAddButton() {
		return variantAddButton;
	}

	/**
	 * @return the variantField
	 */
	public javax.swing.JTextField getVariantField() {
		return variantField;
	}

	/**
	 * @return the variantRemoveButton
	 */
	public javax.swing.JButton getVariantRemoveButton() {
		return variantRemoveButton;
	}

	/**
	 * @return the variantTable
	 */
	public javax.swing.JTable getVariantTable() {
		return variantTable;
	}
    
	public jsesh.editor.JMDCEditor getDescriptionField() {
		return descriptionField;
	}

	public javax.swing.JComboBox<String> getLangSelectCB() {
		return langSelectCB;
	}

	public javax.swing.JTextField getTransliterationField() {
		return transliterationField;
	}

	/**
	 * @return the availableTagList
	 */
	public javax.swing.JList<String> getAvailableTagList() {
		return availableTagList;
	}

	/**
	 * @return the tagAddButton
	 */
	public javax.swing.JButton getTagAddButton() {
		return tagAddButton;
	}


	/**
	 * @return the tagRemoveButton
	 */
	public javax.swing.JButton getTagRemoveButton() {
		return tagRemoveButton;
	}
    
	public javax.swing.JList<SignInfoProperty> getTagList() {
		return tagList;
	}

	/**
	 * @return the addDescriptionButton
	 */
	public javax.swing.JButton getAddDescriptionButton() {
		return addDescriptionButton;
	}

	/**
	 * @return the nextDescriptionButton
	 */
	public javax.swing.JButton getNextDescriptionButton() {
		return nextDescriptionButton;
	}

	/**
	 * @return the previousDescriptionButton
	 */
	public javax.swing.JButton getPreviousDescriptionButton() {
		return previousDescriptionButton;
	}

	/**
	 * @return the removeDescriptionButton
	 */
	public javax.swing.JButton getRemoveDescriptionButton() {
		return removeDescriptionButton;
	}

	/**
	 * @return the alwaysDisplayCheckBox
	 */
	public javax.swing.JCheckBox getAlwaysDisplayCheckBox() {
		return alwaysDisplayCheckBox;
	}

	public javax.swing.JLabel getPartsLabel() {
		return partsLabel;
	}

	public javax.swing.JLabel getVariantOfLabel() {
		return variantOfLabel;
	}

    public JButton getCopyPreviousSignTagsButton() {
        return copyPreviousSignTagsButton;
    }
	
	
	
}

/*
 * JSignInfo.java
 *
 * Created on 27 septembre 2007, 19:40
 */

package jsesh.utilitysoftwares.signinfoeditor.ui.form;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import jsesh.utilitysoftwares.signinfoeditor.model.SignInfoProperty;
import jsesh.utilitysoftwares.signinfoeditor.ui.utils.IconFactory;
import net.miginfocom.swing.MigLayout;

/**
 * Display for editing signs information.
 * 
 * @author  rosmord
 */
public class JSignInfo extends JPanel {
	
    private JButton addDescriptionButton;
    private JCheckBox alwaysDisplayCheckBox;
    private JList<String> availableTagList;
    private JButton copyPreviousSignTagsButton;
    private jsesh.editor.JMDCEditor descriptionField;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JPanel jPanel1;
    private JPanel jPanel10;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JPanel jPanel5;
    private JPanel jPanel6;
    private JPanel jPanel7;
    private JPanel jPanel8;
    private JPanel jPanel9;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane3;
    private JScrollPane jScrollPane4;
    private JScrollPane jScrollPane6;
    private JScrollPane jScrollPane7;
    private JTabbedPane jTabbedPane1;
    private JComboBox<String> langSelectCB;
    private JButton nextButton;
    private JButton nextDescriptionButton;
    private JButton partAddButton;
    private JTextField partField;
    private JButton partRemoveButton;
    private JLabel partsLabel;
    private JTable partsTable;
    private JButton previousButton;
    private JButton previousDescriptionButton;
    private JButton removeDescriptionButton;
    private JTextField signCodeField;
    private JLabel signGlyphLabel;
    private JButton tagAddButton;
    private JList<SignInfoProperty> tagList;
    private JButton tagRemoveButton;
    private JButton transliterationAddButton;
    private JTextField transliterationField;
    private JButton transliterationRemoveButton;
    private JTable transliterationTable;
    private JButton variantAddButton;
    private JTextField variantField;
    private JLabel variantOfLabel;
    private JButton variantRemoveButton;
    private JTable variantTable;

    public JSignInfo() {
        initComponents();
    }
    
    private void initComponents() {
        jLabel2 = new JLabel();
        signCodeField = new JTextField();
        jScrollPane1 = new JScrollPane();
        transliterationTable = new JTable();
        transliterationAddButton = new JButton();
        transliterationRemoveButton = new JButton();
        jLabel1 = new JLabel();
        previousButton = new JButton();
        nextButton = new JButton();
        jPanel2 = new JPanel();
        signGlyphLabel = new JLabel();
        jTabbedPane1 = new JTabbedPane();
        jPanel1 = new JPanel();
        variantRemoveButton = new JButton();
        jScrollPane4 = new JScrollPane();
        variantTable = new JTable();
        variantAddButton = new JButton();
        variantField = new JTextField();
        variantOfLabel = new JLabel();
        jScrollPane3 = new JScrollPane();
        partsTable = new JTable();
        partField = new JTextField();
        partsLabel = new JLabel();
        jPanel9 = new JPanel();
        partAddButton = new JButton();
        partRemoveButton = new JButton();
        jPanel3 = new JPanel();
        jScrollPane6 = new JScrollPane();
        descriptionField = new jsesh.editor.JMDCEditor();
        langSelectCB = new JComboBox<>();
        jLabel7 = new JLabel();
        jLabel8 = new JLabel();
        jPanel8 = new JPanel();
        previousDescriptionButton = new JButton();
        nextDescriptionButton = new JButton();
        removeDescriptionButton = new JButton();
        addDescriptionButton = new JButton();
        jPanel4 = new JPanel();
        jPanel10 = new JPanel();
        jPanel6 = new JPanel();
        jScrollPane7 = new JScrollPane();
        tagList = new JList<>();
        jPanel7 = new JPanel();
        tagAddButton = new JButton();
        tagRemoveButton = new JButton();
        jPanel5 = new JPanel();
        jScrollPane2 = new JScrollPane();
        availableTagList = new JList<>();
        copyPreviousSignTagsButton = new JButton();
        transliterationField = new JTextField();
        alwaysDisplayCheckBox = new JCheckBox();

        jLabel2.setText("Code"); // NOI18N

        signCodeField.setEditable(false);
        signCodeField.setText(" ");

       
        jScrollPane1.setViewportView(transliterationTable);

        transliterationAddButton.setIcon(IconFactory.getAddIcon()); 

        transliterationRemoveButton.setIcon(IconFactory.getRemoveIcon());

        jLabel1.setText("Transliteration"); // NOI18N

        previousButton.setIcon(IconFactory.getPreviousIcon());

        nextButton.setIcon(IconFactory.getNextIcon());

        // jPanel2: glyph display
        jPanel2.setLayout(new MigLayout("insets dialog, fill"));
        jPanel2.add(signGlyphLabel, "grow, h 100!");

        jTabbedPane1.setToolTipText("Free text description of the sign"); // NOI18N

        jPanel1.setToolTipText("<html>\n<p>The notion of variant used here is somehow ad-hoc.</p>\n<p>\tThe problem of variants is that there are two different notions behind it, both useful in our software.\n<p>\tThe first notion is LINGUISTIC variant. A sign is a linguistic variant of another one if it has the same uses.\n<p>\tFor instance, Y2 is a linguistic variant of Y1. Now, Y2 also \"looks like\" Y1. We will call it a \"graphical variation\".\n<p>\tBoth notions are independant, though statistically linked. For instance, Z7 is a linguistic variant of G43, but not a \n\tgraphical variation thereof.\n\t  \n<p> \tthe notion of \"looking like\" another sign is covered by the \"isSimilar\" attribute.\n\t \n<p>\tIn lots of cases, especially for determinatives, the signs are not always fully substitutable one for another.\n\tTo allow the use of 'variant' information in searches, we introduce the \"linguistic\" attribute.\n\t\n<p>\tlet B be a variant of A.\n<ul>\n<li>\t\"full\" means that all uses of B are also possible uses of A, and all uses of A are uses of B.\n<li>\t\"other\" means that B is more specific than A, or that the degree is unknown\n<li>\t\"partial\" means that the uses of A and B intersect, but they have also both significantly different uses.\n<p>\t\tFor instance, the D36 sign (ayin) is a partial variant of D37 (di), as D36 can write \"di\". However,\n\t\tin this case, I would not consider D37 as a variant of D36, because it would cause more harm than good.\n<li>\t\"no\" is used when the sign is not at all a linguistic variant. In this case, isSimilar is normally \"y\".\n</ul>\n\n</html>");

        variantRemoveButton.setIcon(IconFactory.getRemoveIcon());

        
        variantTable.setToolTipText("<html>\nFor an explanation of variants in JSesh, see the documentation. \n<p>It's a bit tricky.\n</html>");
        jScrollPane4.setViewportView(variantTable);

        variantAddButton.setIcon(IconFactory.getAddIcon());

        variantField.setColumns(5);

        variantOfLabel.setText("Variant of"); // NOI18N

 
        partsTable.setToolTipText("<html>\nList signs which are part of this one.\n<p>this sign contains a complex sign, don't list the latter's parts.</p>\n<p>Consider for instance a C98 Osiris figure, wearing the Atef crown ; <p>\n<p> you can list S8 (the Atef) as part of C98, but you should not list the Maat feather, which is part\nof S8 anyway.</p>\n<p>If it's useful, JSesh has enough information for retrieving it.</p>\n</html>");
        jScrollPane3.setViewportView(partsTable);

        partField.setColumns(5);

        partsLabel.setText("Parts"); // NOI18N

        jPanel9.setLayout(new BoxLayout(jPanel9, BoxLayout.PAGE_AXIS));

        partAddButton.setIcon(IconFactory.getAddIcon());
        jPanel9.add(partAddButton);

        partRemoveButton.setIcon(IconFactory.getRemoveIcon());
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

        langSelectCB.setModel(new DefaultComboBoxModel<>(new String[] { "en", "fr", "de" }));

        jLabel7.setText("language: ");

        jLabel8.setText("Sign Description:");

        previousDescriptionButton.setIcon(IconFactory.getPreviousIcon());

        nextDescriptionButton.setIcon(IconFactory.getNextIcon());

        removeDescriptionButton.setIcon(IconFactory.getRemoveIcon());

        addDescriptionButton.setIcon(IconFactory.getAddIcon());

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

        jPanel6.setBorder(BorderFactory.createTitledBorder("This sign's tags"));

        jScrollPane7.setViewportView(tagList);

        // jPanel6: sign's current tags list
        jPanel6.setLayout(new MigLayout("insets dialog, fill"));
        jPanel6.add(jScrollPane7, "grow");

        tagAddButton.setIcon(IconFactory.getNextIcon());

        tagRemoveButton.setIcon(IconFactory.getPreviousIcon());

        // jPanel7: tag transfer buttons (stacked vertically)
        jPanel7.setLayout(new MigLayout("insets 0, wrap 1"));
        jPanel7.add(tagAddButton, "growx");
        jPanel7.add(tagRemoveButton, "growx");

        jPanel5.setBorder(BorderFactory.createTitledBorder("Available tags"));

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
        add(previousButton, "span, split, tag back");
        add(nextButton, "tag next");
    }
    
	/**
	 * @return the nextButton
	 */
	public JButton getNextButton() {
		return nextButton;
	}

	/**
	 * @return the partAddButton
	 */
	public JButton getPartAddButton() {
		return partAddButton;
	}

	/**
	 * @return the partField
	 */
	public JTextField getPartField() {
		return partField;
	}

	public JTable getPartsTable() {
		return partsTable;
	}

	/**
	 * @return the partRemoveButton
	 */
	public JButton getPartRemoveButton() {
		return partRemoveButton;
	}

	/**
	 * @return the previousButton
	 */
	public JButton getPreviousButton() {
		return previousButton;
	}

	/**
	 * @return the signCodeField
	 */
	public JTextField getSignCodeField() {
		return signCodeField;
	}

	/**
	 * @return the signGlyphLabel
	 */
	public JLabel getSignGlyphLabel() {
		return signGlyphLabel;
	}
	

	/**
	 * @return the transliterationAddButton
	 */
	public JButton getTransliterationAddButton() {
		return transliterationAddButton;
	}

	/**
	 * @return the transliterationRemoveButton
	 */
	public JButton getTransliterationRemoveButton() {
		return transliterationRemoveButton;
	}

	/**
	 * @return the transliterationTable
	 */
	public JTable getTransliterationTable() {
		return transliterationTable;
	}

	/**
	 * @return the variantAddButton
	 */
	public JButton getVariantAddButton() {
		return variantAddButton;
	}

	/**
	 * @return the variantField
	 */
	public JTextField getVariantField() {
		return variantField;
	}

	/**
	 * @return the variantRemoveButton
	 */
	public JButton getVariantRemoveButton() {
		return variantRemoveButton;
	}

	/**
	 * @return the variantTable
	 */
	public JTable getVariantTable() {
		return variantTable;
	}
    
	public jsesh.editor.JMDCEditor getDescriptionField() {
		return descriptionField;
	}

	public JComboBox<String> getLangSelectCB() {
		return langSelectCB;
	}

	public JTextField getTransliterationField() {
		return transliterationField;
	}

	/**
	 * @return the availableTagList
	 */
	public JList<String> getAvailableTagList() {
		return availableTagList;
	}

	/**
	 * @return the tagAddButton
	 */
	public JButton getTagAddButton() {
		return tagAddButton;
	}


	/**
	 * @return the tagRemoveButton
	 */
	public JButton getTagRemoveButton() {
		return tagRemoveButton;
	}
    
	public JList<SignInfoProperty> getTagList() {
		return tagList;
	}

	/**
	 * @return the addDescriptionButton
	 */
	public JButton getAddDescriptionButton() {
		return addDescriptionButton;
	}

	/**
	 * @return the nextDescriptionButton
	 */
	public JButton getNextDescriptionButton() {
		return nextDescriptionButton;
	}

	/**
	 * @return the previousDescriptionButton
	 */
	public JButton getPreviousDescriptionButton() {
		return previousDescriptionButton;
	}

	/**
	 * @return the removeDescriptionButton
	 */
	public JButton getRemoveDescriptionButton() {
		return removeDescriptionButton;
	}

	/**
	 * @return the alwaysDisplayCheckBox
	 */
	public JCheckBox getAlwaysDisplayCheckBox() {
		return alwaysDisplayCheckBox;
	}

	public JLabel getPartsLabel() {
		return partsLabel;
	}

	public JLabel getVariantOfLabel() {
		return variantOfLabel;
	}

    public JButton getCopyPreviousSignTagsButton() {
        return copyPreviousSignTagsButton;
    }
	
	
	
}

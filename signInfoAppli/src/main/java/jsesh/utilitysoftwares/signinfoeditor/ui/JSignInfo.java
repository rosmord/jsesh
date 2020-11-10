/*
 * JSignInfo.java
 *
 * Created on 27 septembre 2007, 19:40
 */

package jsesh.utilitysoftwares.signinfoeditor.ui;

import javax.swing.JButton;

/**
 * Display for editing signs information.
 * @author  rosmord
 */

public class JSignInfo extends javax.swing.JPanel {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -6732974872590584101L;
	
	/** Creates new form JSignInfo */
    public JSignInfo() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        langSelectCB = new javax.swing.JComboBox();
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
        tagList = new javax.swing.JList();
        jPanel7 = new javax.swing.JPanel();
        tagAddButton = new javax.swing.JButton();
        tagRemoveButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        availableTagList = new javax.swing.JList();
        copyPreviousSignTagsButton = new javax.swing.JButton();
        transliterationField = new javax.swing.JTextField();
        alwaysDisplayCheckBox = new javax.swing.JCheckBox();

        jLabel2.setText("Code"); // NOI18N

        signCodeField.setEditable(false);
        signCodeField.setText(" ");

        transliterationTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Transliteration", "Relevance"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(transliterationTable);

        transliterationAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_add.png"))); // NOI18N

        transliterationRemoveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_remove.png"))); // NOI18N

        jLabel1.setText("Transliteration"); // NOI18N

        previousButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/previous.png"))); // NOI18N

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/next.png"))); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(signGlyphLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(signGlyphLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.setToolTipText("Free text description of the sign"); // NOI18N

        jPanel1.setToolTipText("<html>\n<p>The notion of variant used here is somehow ad-hoc.</p>\n<p>\tThe problem of variants is that there are two different notions behind it, both useful in our software.\n<p>\tThe first notion is LINGUISTIC variant. A sign is a linguistic variant of another one if it has the same uses.\n<p>\tFor instance, Y2 is a linguistic variant of Y1. Now, Y2 also \"looks like\" Y1. We will call it a \"graphical variation\".\n<p>\tBoth notions are independant, though statistically linked. For instance, Z7 is a linguistic variant of G43, but not a \n\tgraphical variation thereof.\n\t  \n<p> \tthe notion of \"looking like\" another sign is covered by the \"isSimilar\" attribute.\n\t \n<p>\tIn lots of cases, especially for determinatives, the signs are not always fully substitutable one for another.\n\tTo allow the use of 'variant' information in searches, we introduce the \"linguistic\" attribute.\n\t\n<p>\tlet B be a variant of A.\n<ul>\n<li>\t\"full\" means that all uses of B are also possible uses of A, and all uses of A are uses of B.\n<li>\t\"other\" means that B is more specific than A, or that the degree is unknown\n<li>\t\"partial\" means that the uses of A and B intersect, but they have also both significantly different uses.\n<p>\t\tFor instance, the D36 sign (ayin) is a partial variant of D37 (di), as D36 can write \"di\". However,\n\t\tin this case, I would not consider D37 as a variant of D36, because it would cause more harm than good.\n<li>\t\"no\" is used when the sign is not at all a linguistic variant. In this case, isSimilar is normally \"y\".\n</ul>\n\n</html>");

        variantRemoveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_remove.png"))); // NOI18N

        variantTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        variantTable.setToolTipText("<html>\nFor an explanation of variants in JSesh, see the documentation. \n<p>It's a bit tricky.\n</html>");
        jScrollPane4.setViewportView(variantTable);

        variantAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_add.png"))); // NOI18N

        variantField.setColumns(5);

        variantOfLabel.setText("Variant of"); // NOI18N

        partsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        partsTable.setToolTipText("<html>\nList signs which are part of this one.\n<p>this sign contains a complex sign, don't list the latter's parts.</p>\n<p>Consider for instance a C98 Osiris figure, wearing the Atef crown ; <p>\n<p> you can list S8 (the Atef) as part of C98, but you should not list the Maat feather, which is part\nof S8 anyway.</p>\n<p>If it's useful, JSesh has enough information for retrieving it.</p>\n</html>");
        jScrollPane3.setViewportView(partsTable);

        partField.setColumns(5);

        partsLabel.setText("Parts"); // NOI18N

        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.PAGE_AXIS));

        partAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_add.png"))); // NOI18N
        jPanel9.add(partAddButton);

        partRemoveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_remove.png"))); // NOI18N
        jPanel9.add(partRemoveButton);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(partsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(partField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE))
                    .add(jScrollPane3, 0, 223, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(variantOfLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(variantField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE))
                    .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 222, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(variantRemoveButton, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(variantAddButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {variantAddButton, variantRemoveButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.linkSize(new java.awt.Component[] {partsLabel, variantOfLabel}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(16, 16, 16)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(partsLabel)
                    .add(partField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(variantOfLabel)
                    .add(variantAddButton)
                    .add(variantField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jPanel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                    .add(jScrollPane4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE))
                .addContainerGap())
            .add(jPanel1Layout.createSequentialGroup()
                .add(51, 51, 51)
                .add(variantRemoveButton)
                .add(131, 131, 131))
        );

        jTabbedPane1.addTab("Relationships", jPanel1);

        descriptionField.setToolTipText("Sign Description in Manuel de codage format. May contain any kind of information: sign values, bibliography..."); // NOI18N
        descriptionField.setScale(1.0);

        org.jdesktop.layout.GroupLayout descriptionFieldLayout = new org.jdesktop.layout.GroupLayout(descriptionField);
        descriptionField.setLayout(descriptionFieldLayout);
        descriptionFieldLayout.setHorizontalGroup(
            descriptionFieldLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 513, Short.MAX_VALUE)
        );
        descriptionFieldLayout.setVerticalGroup(
            descriptionFieldLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 82, Short.MAX_VALUE)
        );

        jScrollPane6.setViewportView(descriptionField);

        langSelectCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "en", "fr", "de" }));

        jLabel7.setText("language: ");

        jLabel8.setText("Sign Description:");

        previousDescriptionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/previous.png"))); // NOI18N

        nextDescriptionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/next.png"))); // NOI18N

        removeDescriptionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_remove.png"))); // NOI18N

        addDescriptionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/edit_add.png"))); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createSequentialGroup()
                .add(previousDescriptionButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(nextDescriptionButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 143, Short.MAX_VALUE)
                .add(addDescriptionButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(removeDescriptionButton))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(previousDescriptionButton)
                .add(nextDescriptionButton)
                .add(removeDescriptionButton)
                .add(addDescriptionButton))
        );

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel3Layout.createSequentialGroup()
                                .add(jLabel7)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(langSelectCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 187, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(jLabel8))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jPanel3Layout.createSequentialGroup()
                        .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jLabel7)
                            .add(langSelectCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(18, 18, 18)
                        .add(jLabel8))
                    .add(jPanel8, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Description", jPanel3);

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("This sign's tags"));

        tagList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane7.setViewportView(tagList);

        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                .addContainerGap())
        );

        tagAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/next.png"))); // NOI18N

        tagRemoveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/jsesh/utilitysoftwares/signinfoeditor/icons/previous.png"))); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(tagAddButton)
            .add(tagRemoveButton)
        );

        jPanel7Layout.linkSize(new java.awt.Component[] {tagAddButton, tagRemoveButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel7Layout.createSequentialGroup()
                .add(tagAddButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tagRemoveButton))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Available tags"));

        availableTagList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(availableTagList);

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout jPanel10Layout = new org.jdesktop.layout.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel10Layout.createSequentialGroup()
                .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        copyPreviousSignTagsButton.setText("Copy Previous Sign Tags");

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.CENTER, copyPreviousSignTagsButton)
            .add(org.jdesktop.layout.GroupLayout.CENTER, jPanel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                .add(jPanel10, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(copyPreviousSignTagsButton))
        );

        jTabbedPane1.addTab("tags", jPanel4);

        alwaysDisplayCheckBox.setText("basic sign");
        alwaysDisplayCheckBox.setToolTipText("<html>\n<p>States if the sign is part of the \"basic\" palette or the extended one.</p>\n<p>\nIf unchecked, the sign will be displayed in the palette <em>only</em> if\n\"display all\" is selected.\n</p>\n<p>\nCurrently, signs in the Gardiner list, as well as extended signs which are <em>not</em> variant\nof other signs are supposed to be basic signs.\n</p>\n</html>");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(previousButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(nextButton)
                        .add(531, 531, 531))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel2)
                                .add(6, 6, 6)
                                .add(signCodeField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                                .add(14, 14, 14))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, alwaysDisplayCheckBox, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                                    .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel1)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(transliterationField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
                            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(transliterationAddButton, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(transliterationRemoveButton))
                        .addContainerGap())))
        );

        layout.linkSize(new java.awt.Component[] {transliterationAddButton, transliterationRemoveButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(jLabel1)
                    .add(signCodeField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(transliterationField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(transliterationAddButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(transliterationRemoveButton)
                    .add(layout.createSequentialGroup()
                        .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 138, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(alwaysDisplayCheckBox))
                    .add(jScrollPane1, 0, 0, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(previousButton)
                    .add(nextButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addDescriptionButton;
    private javax.swing.JCheckBox alwaysDisplayCheckBox;
    private javax.swing.JList availableTagList;
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
    private javax.swing.JComboBox langSelectCB;
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
    private javax.swing.JList tagList;
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
    // End of variables declaration//GEN-END:variables
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

	public javax.swing.JComboBox getLangSelectCB() {
		return langSelectCB;
	}

	public javax.swing.JTextField getTransliterationField() {
		return transliterationField;
	}

	/**
	 * @return the availableTagList
	 */
	public javax.swing.JList getAvailableTagList() {
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
    
	public javax.swing.JList getTagList() {
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
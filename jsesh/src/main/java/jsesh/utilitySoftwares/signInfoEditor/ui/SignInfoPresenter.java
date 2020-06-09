/*
 * SignInfoPresenter.java
 * 
 * Created on 1 oct. 2007, 11:46:32
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.utilitySoftwares.signInfoEditor.ui;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import jsesh.hieroglyphs.data.GardinerCode;
import jsesh.hieroglyphs.graphics.HieroglyphicBitmapBuilder;
import jsesh.hieroglyphs.data.SignDescriptionConstants;
import jsesh.hieroglyphs.data.SignValueType;
import jsesh.hieroglyphs.data.SignVariantType;
import jsesh.hieroglyphs.data.io.SignDescriptionReader;
import jsesh.hieroglyphs.resources.HieroglyphResources;
import jsesh.swing.signPalette.HieroglyphPaletteListener;
import jsesh.swing.utils.SimpleStringTransfertHandler;
import jsesh.utilitySoftwares.signInfoEditor.events.SignInfoModelEvent;
import jsesh.utilitySoftwares.signInfoEditor.events.SignInfoModelEventListener;
import jsesh.utilitySoftwares.signInfoEditor.events.TagEvent;
import jsesh.utilitySoftwares.signInfoEditor.helpers.SignInfoModelBuilder;
import jsesh.utilitySoftwares.signInfoEditor.helpers.SignInfoModelXMLWriter;
import jsesh.utilitySoftwares.signInfoEditor.model.EditableSignInfo;
import jsesh.utilitySoftwares.signInfoEditor.model.SignInfoModel;
import jsesh.utilitySoftwares.signInfoEditor.model.SignInfoProperty;

import org.qenherkhopeshef.guiFramework.PropertyHolder;
import org.xml.sax.SAXException;

/**
 * Control layer for the sign information editor.
 * The model is in fact a rather plain representation of the XML data,
 * with everything represented as "properties".
 * @author rosmord
 */
public class SignInfoPresenter implements HieroglyphPaletteListener,
        PropertyHolder {

    private static final String EXPERT_FILE_PATH = "EXPERT_FILE_PATH";
    private static final int bitmapBorder = 2;
    private static final int bitmapSize = 30;
    private static final int LABEL_PADDING = 3;
    private DefaultListModel availableTagListModel;
    private EditableSignInfo copiedSignInfo;
    /**
     * The file being edited.
     */
    private File currentFile = null;
    /**
     * The "expert mode" file.
     */
    private File expertFile = null;
    private EditableSignInfo currentSign;
    /**
     * Are we editing data. true if the user can edit data, false if we are in
     * the process of changing the current sign.
     */
    private boolean editing = true;
    /**
     * Are we working in expert mode ? In expert mode, the file we save to and
     * load from is a single XML file.
     */
    private boolean expertMode = false;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
            this);
    private SignContainsTableModel signContainsTableModel;
    /**
     * Index of the current sign description.
     */
    private int signDescriptionIndex = -1;
    /**
     * The list of available descriptions for this sign.
     */
    private List signDescriptionsList = new ArrayList();
    private SignInfoModel signInfoModel;
    private DefaultListModel signTagListModel;
    private SignTransliterationTableModel signTransliterationTableModel;
    private SignVariantTableModel signVariantTableModel;
    private JSignInfo view = new JSignInfo();
    private EditableSignInfo previousSign;

    public SignInfoPresenter(SignInfoModel signInfoModel) {

        this.setSignInfoModel(signInfoModel);

        // Init "expert file" from preferences...
        Preferences preferences = Preferences.userNodeForPackage(this.getClass());
        String expertFileName = preferences.get(EXPERT_FILE_PATH, "");
        if (!"".equals(expertFileName)) {
            expertFile = new File(expertFileName);


        }
        view.getSignCodeField().setEditable(true);
        view.getSignCodeField().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateCodeFromField();
            }
        });

        view.getAlwaysDisplayCheckBox().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateAlwaysDisplayData();
            }
        });
        // view.getLangSelectCB().setEnabled(false);
        view.getLangSelectCB().setModel(
                new DefaultComboBoxModel(
                SignDescriptionConstants.LANGUAGES_CODES));

        TransferHandler dropHandler = new SignCodeTransfertHandler();
        view.getSignGlyphLabel().setTransferHandler(dropHandler);
        view.getSignCodeField().setTransferHandler(dropHandler);

        view.getPartsLabel().setTransferHandler(
                new TableLabelHandler(view.getPartsTable()));
        view.getVariantOfLabel().setTransferHandler(
                new TableLabelHandler(view.getVariantTable()));

        prepareVariantControls();
        prepareTransliterationControls();
        preparePartControls();
        prepareTagControls();
        prepareDescriptionControls();
    }

    public void copy() {
        copiedSignInfo = new EditableSignInfo(currentSign);
        Iterator it = copiedSignInfo.getPropertyList().iterator();
        while (it.hasNext()) {
            SignInfoProperty prop = (SignInfoProperty) it.next();
            prop.setUserDefinition(true);
        }
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public File getExpertFile() {
        return expertFile;
    }

    public void setExpertFile(File expertFile) {
        this.expertFile = expertFile;
        Preferences preferences = Preferences.userNodeForPackage(this.getClass());
        preferences.put(EXPERT_FILE_PATH, expertFile.getAbsolutePath());
    }

    public String getCurrentSignCode() {
        if (currentSign != null) {
            return currentSign.getCode();

        } else {
            return "";

        }
    }

    public JSignInfo getPanel() {
        return this.view;
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    public SignInfoModel getSignInfoModel() {
        return signInfoModel;
    }

    public void goToFirstSign() {
        setCurrentSign(getSignInfoModel().getFirstCode());
    }

    public boolean hasNext() {
        return getSignInfoModel().getCodeAfter(getCurrentSignCode()) != null;
    }

    public boolean hasPrevious() {
        return getSignInfoModel().getCodeBefore(getCurrentSignCode()) != null;
    }

    public boolean isExpertMode() {
        return expertMode;
    }

    public void nextSign() {
        String next = getSignInfoModel().getCodeAfter(getCurrentSignCode());
        if (next != null) {
            setCurrentSign(next);

        }
    }

    /**
     * Open the whole JSesh editing file.
     */
    public void openExpertFile() {
        openFile(expertFile);
    }

    /**
     * Open a new User file.
     *
     * @param userSignDefinitionFile
     */
    public void openFile(File userSignDefinitionFile) {
        SignInfoModel newSignInfoModel;
        // In expert mode, all data come from this file...
        if (expertMode) {
            newSignInfoModel = new SignInfoModel();

        } else // In "user mode", we first read the system data.
        {
            newSignInfoModel = readSystemInfoModel();


        }
        try {
            currentFile = userSignDefinitionFile;
            if (currentFile == null) {
                return;

            }
            SignInfoModelBuilder signInfoModelBuilder = new SignInfoModelBuilder(
                    newSignInfoModel);
            SignDescriptionReader signDescriptionReader = new SignDescriptionReader(
                    signInfoModelBuilder);
            // Read user file.
            signInfoModelBuilder.setInUserPart(true);
            signDescriptionReader.readSignDescription(new FileInputStream(
                    userSignDefinitionFile));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setSignInfoModel(newSignInfoModel);
        newSignInfoModel.clearDirtyFlag();
    }

    /**
     * Open system file.
     */
    public void openSystemFile() {
        SignInfoModel newSignInfoModel = readSystemInfoModel();
        setSignInfoModel(newSignInfoModel);
        newSignInfoModel.clearDirtyFlag();
    }

    public void paste() {
        currentSign.addAttributesOf(copiedSignInfo);
        // Reload all sign data.
        setCurrentSign(getCurrentSignCode());
    }

    public void previousSign() {
        String previous = getSignInfoModel().getCodeBefore(getCurrentSignCode());
        if (previous != null) {
            setCurrentSign(previous);

        }
    }

    public void save() {
        File toSave = currentFile;
        if (expertMode) {
            toSave = expertFile;

        }
        try {
            SignInfoModelXMLWriter signInfoModelXMLWriter = new SignInfoModelXMLWriter(
                    new FileOutputStream(toSave));
            signInfoModelXMLWriter.writeModel(getSignInfoModel());
            signInfoModel.clearDirtyFlag();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Error saving file " + currentFile.getAbsolutePath(), "Save failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void saveFile(File f) {
        currentFile = f;
        save();
    }

    public void setCurrentSign(String code) {
        editing = false;

        currentSign = getSignInfoModel().getSign(code);

        if (currentSign == null) {
            currentSign = new EditableSignInfo("");
        // disable all entries.
        } else {

            // update sign code
            view.getSignCodeField().setText(code);
            JLabel signIconLabel = view.getSignGlyphLabel();
            // update sign display
            signIconLabel.setIcon(HieroglyphicBitmapBuilder.createHieroglyphIcon(code, signIconLabel.getHeight() - 2 * LABEL_PADDING, LABEL_PADDING, signIconLabel));

            // table of signs of whom we are a variant.

            bindVariantOfTable();
            bindTransliterationTable();
            bindPartsTable();
            bindTags();
            bindAvailableTagList();
            bindDescriptions();

            view.getAlwaysDisplayCheckBox().setSelected(
                    currentSign.isAlwaysDisplay());
            if (currentSign.isAlwaysDisplay() && !currentSign.isAlwaysDisplayProvidedByUser()) {
                view.getAlwaysDisplayCheckBox().setEnabled(false);

            } else {
                view.getAlwaysDisplayCheckBox().setEnabled(true);

            }
        }
        enableActiveButtons();
        editing = true;
    }

    public void setExpertMode(boolean newExpertMode) {
        boolean oldMode = this.expertMode;
        this.expertMode = newExpertMode;
        propertyChangeSupport.firePropertyChange("expertMode", oldMode,
                newExpertMode);
    }

    /**
     * @see HieroglyphPaletteListener#signSelected(String)
     */
    public void signSelected(String code) {
        // DO NOTHING ?
    }

    protected void addDescription() {
        SignInfoProperty prop = new SignInfoProperty(
                SignDescriptionConstants.SIGN_DESCRIPTION, "", true);
        prop.setAttribute(SignDescriptionConstants.LANG, "en");
        currentSign.add(prop);
        signDescriptionsList.add(prop);
        signDescriptionIndex = signDescriptionsList.size() - 1;
        updateDisplayedDescription();
    }

    protected void addTag() {
        String tag = (String) view.getAvailableTagList().getSelectedValue();
        if (tag == null) {
            return;

        }
        SignInfoProperty prop = new SignInfoProperty(
                SignDescriptionConstants.HAS_TAG, true);
        prop.setAttribute(SignDescriptionConstants.SIGN, currentSign.getCode());
        prop.setAttribute(SignDescriptionConstants.TAG, tag);
        currentSign.add(prop);
        signTagListModel.addElement(prop);
        bindAvailableTagList();
    }

    protected void copyPreviousSignTags() {
        String previousSignCode = signInfoModel.getCodeBefore(getCurrentSignCode());

        if (previousSignCode != null) {
            previousSign = signInfoModel.getSign(previousSignCode);
            List tags = previousSign.getPropertyList(SignDescriptionConstants.HAS_TAG);
            for (Iterator it = tags.iterator(); it.hasNext();) {
                SignInfoProperty oldTag= (SignInfoProperty) it.next();
                SignInfoProperty newTag= new SignInfoProperty(SignDescriptionConstants.HAS_TAG, true);
                newTag.setAttribute(SignDescriptionConstants.SIGN, currentSign.getCode());
                newTag.setAttribute(SignDescriptionConstants.TAG, oldTag.get(SignDescriptionConstants.TAG));
                currentSign.add(newTag);
                signTagListModel.addElement(newTag);
                bindAvailableTagList();
            }
        }
    }
    protected void changeDescriptionText() {
        if (editing) {
            if (signDescriptionIndex == -1) {
                return;
            // Either edit or create the current sign description.

            }
            final SignInfoProperty prop = getCurrentDescription();
            if (prop.isUserDefinition()) {
                prop.setContent(view.getDescriptionField().getMDCText());
            } else {
                // We must not edit this field... revert it to original value
                // would be kind of recursive (we are called because the text
                // has changed, and we change the text once more).
                // So, we use :
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        editing = false;
                        view.getDescriptionField().setMDCText(prop.getContent());
                        editing = true;
                    }
                });
            }
        }
    }

    protected void nextDescription() {
        if (signDescriptionIndex < signDescriptionsList.size() - 1) {
            signDescriptionIndex++;
            updateDisplayedDescription();
        }
    }

    protected void previousDescription() {
        if (signDescriptionIndex > 0) {
            signDescriptionIndex--;
            updateDisplayedDescription();
        }
    }

    protected void removeDescription() {
        if (signDescriptionIndex != -1) {
            SignInfoProperty prop = getCurrentDescription();
            if (prop.isUserDefinition()) {
                if (JOptionPane.showConfirmDialog(view,
                        "Really suppress this description", "Confirm deletion",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    currentSign.remove(prop);
                    signDescriptionsList.remove(signDescriptionIndex);
                    signDescriptionIndex--;
                    // we suppressed the first description...
                    if (signDescriptionIndex == -1 && signDescriptionsList.size() > 0) {
                        signDescriptionIndex = 0;

                    }
                }
                updateDisplayedDescription();
            }
        }
    }

    protected void removeTag() {
        SignInfoProperty prop = (SignInfoProperty) view.getTagList().getSelectedValue();
        if (prop == null || !prop.isUserDefinition()) {
            return;

        }
        signTagListModel.removeElement(prop);
        currentSign.remove(prop);
        bindAvailableTagList();
    }

    

    protected void updateAlwaysDisplayData() {
        // We can only change the status of the sign if a positive information
        // has not been given at the system level.
        if (currentSign != null && !(currentSign.isAlwaysDisplay() && !currentSign.isAlwaysDisplayProvidedByUser())) {
            currentSign.setAlwaysDisplay(view.getAlwaysDisplayCheckBox().isSelected());
            currentSign.setAlwaysDisplayProvidedByUser(true);
        }
    }

    protected void updateCodeFromField() {
        String newCode = view.getSignCodeField().getText();
        setCurrentSign(newCode);
    }

    protected void updateDescriptionLanguage() {
        if (this.signDescriptionIndex != -1) {
            SignInfoProperty prop = getCurrentDescription();
            if (prop.isUserDefinition()) {
                String newLang = (String) view.getLangSelectCB().getSelectedItem();
                prop.setAttribute(SignDescriptionConstants.LANG, newLang);
            }
        }
    }

    private void bindAvailableTagList() {
        // Build a list of tag names.
        TreeSet l = new TreeSet(getSignInfoModel().getTags());
        // Remove those from the current sign:
        List l1 = currentSign.getPropertyList(SignDescriptionConstants.HAS_TAG);
        for (int i = 0; i < l1.size(); i++) {
            l.remove(((SignInfoProperty) l1.get(i)).get(SignDescriptionConstants.TAG));
        }
        // We want to sort those, with a) the tags available for this sign's
        // family
        // and then the other tags.
        String family = GardinerCode.createGardinerCode(getCurrentSignCode()).getFamily();
        SortedSet tagsForFamily = getSignInfoModel().getTagsForFamily(family);
        // Remove already used tags from "tagsForFamily".
        tagsForFamily.retainAll(l);
        // let l be the remaining tags:
        l.removeAll(tagsForFamily);

        availableTagListModel = new DefaultListModel();
        // Add all tags in for this family to the table model.
        Iterator it = tagsForFamily.iterator();
        while (it.hasNext()) {
            String tag = (String) it.next();
            availableTagListModel.addElement(tag);
        }
        // add Other tags.
        it = l.iterator();
        while (it.hasNext()) {
            String tag = (String) it.next();
            availableTagListModel.addElement(tag);
        }

        view.getAvailableTagList().setModel(availableTagListModel);
    }

    private void bindDescriptions() {
        view.getDescriptionField().clearText();

        this.signDescriptionsList = currentSign.getPropertyList(SignDescriptionConstants.SIGN_DESCRIPTION);
        if (signDescriptionsList.isEmpty()) {
            signDescriptionIndex = -1;

        } else {
            signDescriptionIndex = 0;

        }
        updateDisplayedDescription();
    }

    private void bindFirstHieroglyphicColumn(JTable table,
            SignPropertyTableModel tableModel) {
        // First column. Has a renderer, but the editor is the normal string
        // editor.
        table.getColumnModel().getColumn(0).setCellRenderer(
                new HieroglyphicCodeRenderer(bitmapSize, bitmapBorder));
        // Drag and drop support:
        table.setTransferHandler(new SignCodeToTableTransfertHandler(
                tableModel));
    }

    private void bindPartsTable() {
        view.getPartField().setText("");
        JTable partTable = view.getPartsTable();
        signContainsTableModel = new SignContainsTableModel(currentSign);
        partTable.setModel(signContainsTableModel);
        bindFirstHieroglyphicColumn(partTable, signContainsTableModel);
    }

    private void bindTags() {
        JList tagList = view.getTagList();
        signTagListModel = new DefaultListModel();
        List propertyList = currentSign.getPropertyList(SignDescriptionConstants.HAS_TAG);
        for (int i = 0; i < propertyList.size(); i++) {
            signTagListModel.addElement(propertyList.get(i));
        }
        tagList.setModel(signTagListModel);
        // Ask for tags to be rendered only as their tag name.
        tagList.setCellRenderer(new DefaultListCellRenderer() {

            /*
             * (non-Javadoc)
             *
             * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList,
             *      java.lang.Object, int, boolean, boolean)
             */
            public Component getListCellRendererComponent(JList list,
                    Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                // We will in fact trick the default into rendering another
                // value
                SignInfoProperty prop = (SignInfoProperty) value;
                String newValue = prop.get("tag");
                return super.getListCellRendererComponent(list, newValue,
                        index, isSelected, cellHasFocus);
            }
        });

    }

    private void bindTransliterationTable() {

        JTable transliterationTable = view.getTransliterationTable();
        view.getTransliterationField().setText("");
        signTransliterationTableModel = new SignTransliterationTableModel(
                currentSign);
        transliterationTable.setModel(signTransliterationTableModel);
        // adapt display for second and third columns.

        JComboBox<SignValueType> typeCB = new JComboBox();
        for (SignValueType valueType: SignValueType.values())
            typeCB.addItem(valueType);

        transliterationTable.getColumnModel().getColumn(1).setCellEditor(
                new DefaultCellEditor(typeCB));

        	
        JComboBox useCB = new JComboBox();
        useCB.addItem(SignDescriptionConstants.KEYBOARD);
        useCB.addItem(SignDescriptionConstants.PALETTE);
        useCB.addItem(SignDescriptionConstants.INFORMATIVE);
        transliterationTable.getColumnModel().getColumn(2).setCellEditor(
                new DefaultCellEditor(useCB));

    }

    /**
     */
    private void bindVariantOfTable() {
        JTable variantTable = view.getVariantTable();
        view.getVariantField().setText("");

        signVariantTableModel = new SignVariantTableModel(currentSign);
        variantTable.setModel(signVariantTableModel);

        // Sets the display and edit behaviour of the first column:
        bindFirstHieroglyphicColumn(variantTable, signVariantTableModel);

        // second column: boolean, no problem

        //
        // third column: use a combobox. Content is ListOption
        //
        JComboBox combobox = new JComboBox();
        for (SignVariantType variantType: SignVariantType.values()) {
            combobox.addItem(variantType.toString());
        }
        combobox.addItem(SignDescriptionConstants.NO);
        combobox.addItem(SignDescriptionConstants.UNSPECIFIED);
        variantTable.getColumnModel().getColumn(2).setCellEditor(
                new DefaultCellEditor(combobox));
    }

    /**
     * See what buttons can be activated.
     */
    private void enableActiveButtons() {
        if (currentSign == null) {
            view.getNextButton().setEnabled(false);
            view.getPreviousButton().setEnabled(false);
        } else {
            view.getPreviousButton().setEnabled(hasPrevious());
            view.getNextButton().setEnabled(hasNext());
        }
    }

    private SignInfoProperty getCurrentDescription() {
        return (SignInfoProperty) signDescriptionsList.get(signDescriptionIndex);
    }

    private void prepareDescriptionControls() {
        // Current Description update...

        view.getDescriptionField().getHieroglyphicTextModel().addObserver(
                new Observer() {

                    public void update(Observable o, Object arg) {
                        changeDescriptionText();
                    }
                });
        // Change in the displayed description.
        view.getNextDescriptionButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                nextDescription();
            }
        });
        view.getPreviousDescriptionButton().addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        previousDescription();
                    }
                });
        view.getAddDescriptionButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addDescription();
            }
        });
        view.getRemoveDescriptionButton().addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        removeDescription();
                    }
                });
        view.getLangSelectCB().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                updateDescriptionLanguage();
            }
        });
    }

    private void preparePartControls() {
        view.getPartsTable().setRowHeight(30);
        new GrowableTableControl(view.getPartsTable(), view.getPartAddButton(),
                view.getPartRemoveButton(), view.getPartField());
    }

    private void prepareTagControls() {
        signTagListModel = new DefaultListModel();

        view.getTagList().setModel(signTagListModel);
        // Too bad... we are using lists, not tables
        view.getTagAddButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                addTag();
            }
        });

        view.getTagRemoveButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                removeTag();
            }
        });

        view.getCopyPreviousSignTagsButton().addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                copyPreviousSignTags();
            }
        });

        view.getTagList().addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    removeTag();
                }
            }
        });

        view.getAvailableTagList().addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    addTag();
                }
            }
        });


        view.getAvailableTagList().getInputMap().put(
                KeyStroke.getKeyStroke("SPACE"), "addTag");
        view.getAvailableTagList().getActionMap().put("addTag",
                new AbstractAction() {

                    public void actionPerformed(ActionEvent e) {
                        // When no selection is made, the "cursor position" is
                        // represented
                        // by the anchorSelectionIndex.
                        int anchor = view.getAvailableTagList().getAnchorSelectionIndex();
                        view.getAvailableTagList().setSelectedIndex(anchor);
                        addTag();
                    }
                });

        view.getTagList().getInputMap().put(KeyStroke.getKeyStroke("SPACE"),
                "removeTag");
        view.getTagList().getActionMap().put("removeTag", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                // See addTag for explanation of the following.
                int anchor = view.getTagList().getAnchorSelectionIndex();
                view.getTagList().setSelectedIndex(anchor);

                removeTag();
            }
        });

    }

    private void prepareTransliterationControls() {
        new GrowableTableControl(view.getTransliterationTable(), view.getTransliterationAddButton(), view.getTransliterationRemoveButton(), view.getTransliterationField());
    }

    private void prepareVariantControls() {
        // Prepare and adapt elements
        view.getVariantTable().setRowHeight(30);
        new GrowableTableControl(view.getVariantTable(), view.getVariantAddButton(), view.getVariantRemoveButton(), view.getVariantField());
    }

    private SignInfoModel readSystemInfoModel() {
        SignInfoModel newSignInfoModel = new SignInfoModel();
        SignInfoModelBuilder signInfoModelBuilder = new SignInfoModelBuilder(
                newSignInfoModel);
        SignDescriptionReader signDescriptionReader = new SignDescriptionReader(
                signInfoModelBuilder);

        // Read the basic JSesh description.
        signInfoModelBuilder.setInUserPart(false);
        InputStream in1 = HieroglyphResources.getSignsDescriptionXML();

        try {
            signDescriptionReader.readSignDescription(in1);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return newSignInfoModel;
    }

    private void setSignInfoModel(SignInfoModel signInfoModel) {
        this.signInfoModel = signInfoModel;
        signInfoModel.addSignInfoModelEventListener(new SignInfoModelEventListener() {

            public void signInfoModelChanged(SignInfoModelEvent event) {
                // TODO: when the model improves (if it does improve
                // here), create a proper listener.
                // and use a visitor pattern
                if (event instanceof TagEvent) {
                    bindAvailableTagList();
                }
            }
        });
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                goToFirstSign();
            }
        });

    }

    /**
     * Refreshes the display so that it show the currently selected description.
     */
    private void updateDisplayedDescription() {
        boolean prevEditing = editing;
        editing = false;
        if (signDescriptionIndex != -1) {
            view.getLangSelectCB().setEnabled(true);
            SignInfoProperty prop = getCurrentDescription();
            view.getLangSelectCB().setSelectedItem(
                    prop.get(SignDescriptionConstants.LANG));
            view.getDescriptionField().setEditable(prop.isUserDefinition());
            view.getDescriptionField().setEnabled(true);
            view.getDescriptionField().setMDCText(prop.getContent());
            view.getNextDescriptionButton().setEnabled(
                    signDescriptionIndex < signDescriptionsList.size() - 1);
            view.getPreviousDescriptionButton().setEnabled(
                    signDescriptionIndex > 0);
            view.getLangSelectCB().setEnabled(prop.isUserDefinition());
            view.getRemoveDescriptionButton().setEnabled(
                    prop.isUserDefinition());
        } else {
            view.getLangSelectCB().setEnabled(false);
            view.getDescriptionField().setEditable(false);
            view.getDescriptionField().setEnabled(false);
            view.getDescriptionField().clearText();
            view.getNextDescriptionButton().setEnabled(false);
            view.getPreviousDescriptionButton().setEnabled(false);
        }
        editing = prevEditing;

    }

    /**
     * A handler for drag and drop on a table label.
     *
     * @author rosmord
     *
     */
    public class TableLabelHandler extends SimpleStringTransfertHandler {

        private static final long serialVersionUID = -5053910096927047598L;
        private JTable table;

        private TableLabelHandler(JTable table) {
            super();
            this.table = table;
        }

        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            boolean result = super.canImport(comp, transferFlavors);
            return result;
        }

        public boolean importData(JComponent comp, Transferable t) {
            GrowableModel model = (GrowableModel) table.getModel();
            String txt = getString(t);
            model.addRow(txt);

            return true;
        }
    }

    private class SignCodeTransfertHandler extends SimpleStringTransfertHandler {

        private static final long serialVersionUID = -8926546474934672791L;

        public boolean importData(JComponent comp, Transferable t) {
            setCurrentSign(getString(t));
            return true;
        }
    }
}

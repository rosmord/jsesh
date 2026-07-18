package jsesh.swing.signPalette;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.qenherkhopeshef.observable.ObservableEventListener;
import org.qenherkhopeshef.swingUtils.lists.ComboboxUtils;
import org.qenherkhopeshef.swingUtils.lists.DataListItem;
import org.qenherkhopeshef.swingUtils.lists.LabelListItem;
import org.qenherkhopeshef.swingUtils.lists.ListItem;

import jsesh.editor.JMDCEditor;
import jsesh.glyphs.data.HieroglyphDatabase;
import jsesh.glyphs.data.HieroglyphFamily;
import jsesh.glyphs.data.PossibilitiesList;
import jsesh.glyphs.data.Possibility;
import jsesh.glyphs.data.SignDescriptionConstants;
import jsesh.glyphs.data.coremdc.CanonicalCode;
import jsesh.glyphs.data.coremdc.GardinerCode;
import jsesh.glyphs.data.coremdc.ManuelDeCodage;
import jsesh.glyphs.fonts.HieroglyphShapeRepository;
import jsesh.glyphs.fonts.HieroglyphShapeRepositoryChangedEvent;
import jsesh.glyphs.shape.ShapeChar;
import jsesh.glyphs.tools.HieroglyphPictureBuilder;
import jsesh.preferences.JSeshPreferencesRoot;

/**
 * Control and data feed for the simple palette.
 *
 * Currently, one might prefer to use HieroglyphicPaletteDialog, which provides
 * also a glyph information tab. (See, however, method createFullPalette).
 *
 *
 * TODO : use a MultiLingual label instead of the plain tag name...
 *
 * Some cleanup done : minimized the number of public methods. TODO : remove the
 * back button to navigate in recent selection. The dropdown list is sufficient.
 *
 * @author rosmord
 *
 */
public class PalettePresenter {

    private static final String USER_PALETTE_PREF = "userPalette";
    private static final String OTHERS = "others";
    private static final String ALL = "all";
    private static final String USER_PALETTE = "1";
    private static final String LAST_USED = "0";
    private static final String ALL_FAMILIES = "all families";
    private static final String TALL_NARROW_SIGNS = "Tall Narrow Signs";
    private static final String LOW_BROAD_SIGNS = "Low Broad Signs";
    private static final String LOW_NARROW_SIGNS = "Low Narrow Signs";

    /**
     * Height of glyphs in the displayed list.
     */
    private final int LIST_CELL_HEIGHT = 50;

    private class InsertSignAction extends AbstractAction {

        private static final long serialVersionUID = 6512709688609818997L;

        @Override
        public void actionPerformed(ActionEvent e) {
            String code = getSelectedCode();
            if (code != null) {
                sendCode(code);
            }
        }
    }

    /**
     * The graphic component.
     */
    private final JSimplePalette simplePalette;
    /**
     * Field for detailled sign information.
     */
    private final JMDCEditor signDescriptionField;
    /**
     * Field for glyph information.
     */
    private final JTextArea glyphDescriptionField;

    /**
     * The object which will receive events from this palette.
     */
    private HieroglyphPaletteListener hieroglyphPaletteListener;

    /**
     * Abstract data about codes.
     */
    private final HieroglyphDatabase hieroglyphsDatabase;

    /**
     * Font manager (knows about sign drawings).
     */
    private final HieroglyphShapeRepository hieroglyphicFontManager;

    private final ObservableEventListener<HieroglyphShapeRepositoryChangedEvent> shapeRepositoryListener =
            e -> SwingUtilities.invokeLater(this::updateAccordingToSelectedCategory);

    /**
     * Signs already got from the palette, for faster retrieval.
     */
    private final Set<String> lastUsed = new TreeSet<>(
            ManuelDeCodage.getCodeComparator());
    /**
     * Signs placed in the user palette.
     */
    private final Set<String> userPalette = new TreeSet<>(
            ManuelDeCodage.getCodeComparator());
    private Set<String> partSet;
    private Set<String> variantSet;
    /**
     * Selected sign codes (may be empty strings)
     */
    private final String[] selectedSignCodes = { "", "", "", "", "", "" };
    /**
     * Index of the selected sign code in the buffer.
     */
    private int selectedSignCodeIndex = 0;

    /**
     * A dialog (which can be null or can be used to display this palette).
     */
    private HieroglyphicPaletteDialog dialog;

    public PalettePresenter(HieroglyphShapeRepository hieroglyphicFontManager,
            HieroglyphDatabase hieroglyphDatabase) {
        this.hieroglyphsDatabase = hieroglyphDatabase;
        this.hieroglyphicFontManager = hieroglyphicFontManager;
        simplePalette = new JSimplePalette();

        signDescriptionField = new JMDCEditor();
        signDescriptionField.setScale(2.0);
        signDescriptionField.setEditable(false);

        glyphDescriptionField = new JTextArea();
        glyphDescriptionField.setEditable(false);

        preparePalette();
        loadPalette();
        hieroglyphicFontManager.addListener(shapeRepositoryListener);
    }

    public void dispose() {
        hieroglyphicFontManager.removeListener(shapeRepositoryListener);
    }

    // Link the graphic component to its logic.
    private void preparePalette() {
        // Sign families
        DefaultComboBoxModel<ListItem<HieroglyphFamily>> familyCBModel = new DefaultComboBoxModel<>();
        List<HieroglyphFamily> families = hieroglyphsDatabase.getFamilies();
        familyCBModel.addElement(new LabelListItem<>("Select a sign family"));
        familyCBModel.addElement(
                new DataListItem<>(new HieroglyphFamily(LAST_USED, "Latest signs")));
        familyCBModel
                .addElement(
                        new DataListItem<>(
                                new HieroglyphFamily(USER_PALETTE, "User Palette")));

        for (int i = 0; i < families.size(); i++) {
            familyCBModel.addElement(
                    new DataListItem<>(
                            families.get(i)));
        }
        HieroglyphFamily[] lastFamilies = {
                new HieroglyphFamily(ALL_FAMILIES, "All Signs"),
                new HieroglyphFamily(TALL_NARROW_SIGNS,
                        "Tall Narrow Signs"),
                new HieroglyphFamily(LOW_BROAD_SIGNS,
                        "Low Broad Signs"),
                new HieroglyphFamily(LOW_NARROW_SIGNS,
                        "Low Narrow Signs")
        };

        for (HieroglyphFamily f : lastFamilies) {
            familyCBModel.addElement(new DataListItem<>(f));
        }

        simplePalette.getCategoryChooserCB().setModel(familyCBModel);
        simplePalette.getCategoryChooserCB().setMaximumRowCount(28);

        // activate buttons.
        simplePalette.getShowContainingButtons().addActionListener(e -> {
            selectContaining();
        });

        simplePalette.getBackButton().addActionListener(e -> {
            selectPreviousSign();
        });

        simplePalette.getShowVariantsButton().addActionListener(e -> {
            selectVariants();
        });

        simplePalette.getTransliterationFilterField().addActionListener(e -> {
            selectFromTransliterationOrCode();
        });

        simplePalette.getContainsCB().addActionListener(e -> {
            filterFromContainsCB();
        });
        // Sign table
        SignListCellRenderer renderer = new SignListCellRenderer(simplePalette, this.hieroglyphicFontManager);
        renderer.setBitmapHeight(LIST_CELL_HEIGHT);

        JList<String> signTable = simplePalette.getSignTable();
        // Aspect and drawing related stuff.
        signTable.setCellRenderer(renderer);
        // signTable.setPrototypeCellValue("A248B");
        signTable.setFixedCellHeight((int) (LIST_CELL_HEIGHT * 1.2));
        signTable.setFixedCellWidth((int) (2.5 * LIST_CELL_HEIGHT));

        signTable.getSelectionModel().addListSelectionListener(
                new PaletteRowSelectionListener());

        // Manage clicks on the individual signs.
        signTable.addMouseListener(new SimplePaletteMouseListener());
        // Manage keyboard selection of signs (space and enter)
        InputMap newInputMap = new InputMap();
        newInputMap.setParent(signTable.getInputMap());
        newInputMap.put(KeyStroke.getKeyStroke("ENTER"), "INSERT_SIGN");
        newInputMap.put(KeyStroke.getKeyStroke("SPACE"), "INSERT_SIGN");

        signTable.setInputMap(JComponent.WHEN_FOCUSED, newInputMap);

        SignListCellRenderer smallListRenderer = new SignListCellRenderer(
                simplePalette, hieroglyphicFontManager);
        smallListRenderer.setDisplaySignsCodes(true);
        smallListRenderer.setBitmapHeight(20);
        simplePalette.getContainsCB().setRenderer(smallListRenderer);
        simplePalette.getContainsCB().setPrototypeDisplayValue(new DataListItem<String>("A1"));

        ActionMap newActionMap = new ActionMap();
        newActionMap.setParent(signTable.getActionMap());
        newActionMap.put("INSERT_SIGN", new InsertSignAction());
        signTable.setActionMap(newActionMap);

        // Simple action, refreshes the list according to the selection.
        ActionListener updateToSelectedCategoryListener = (ActionEvent e) -> {
            updateAccordingToSelectedCategory();
        };

        // Control for user palette:
        simplePalette.getInUserPaletteCheckBox().addActionListener(e -> {
            toggleSignInUserPalette();
        });

        // Control for tags
        simplePalette.getTagChooserCB().addActionListener(e -> {
            selectTag();
        });

        simplePalette.getSecondaryTagCB().addActionListener(e -> {
            selectSecondaryTag();
        });
        // the table is refereshed when the selection changes, or when the
        // checkbox is checked.
        simplePalette.getCategoryChooserCB().addActionListener(
                updateToSelectedCategoryListener);
        simplePalette.getShowAllCheckBox().addActionListener(
                updateToSelectedCategoryListener);

        simplePalette.getCategoryChooserCB().setSelectedItem(new DataListItem<>(families.get(0)));

        simplePalette.getShowAllCheckBox().setSelected(true);

        updateAccordingToSelectedCategory();

    }

    /**
     * Returns the family currently selected, if any.
     *
     * @return
     */
    private Optional<HieroglyphFamily> getSelectedFamily() {
        // "Exercice de style" in functional programming.
        // Nice, but probably not readable for future JSesh maintainers.
        // I Comment it and replace it with its imperative version

        // return ComboboxUtils.getSelectedItem(simplePalette.getCategoryChooserCB()).flatMap(
        //         (ListItem<HieroglyphFamily> selected) -> switch (selected) {
        //             case LabelListItem<HieroglyphFamily> e -> Optional.<HieroglyphFamily>empty();
        //             case DataListItem<HieroglyphFamily> e1 -> {
        //                 HieroglyphFamily f = e1.getValue();
        //                 yield Optional.<HieroglyphFamily>of(f);
        //             }
        //         });
        Optional<ListItem<HieroglyphFamily>> optSelectedItem = ComboboxUtils.getSelectedItem(simplePalette.getCategoryChooserCB());
        if (optSelectedItem.isEmpty()) {
            return Optional.empty();
        } else if (optSelectedItem.get() instanceof DataListItem<HieroglyphFamily> dataListItem) {
            return Optional.of(dataListItem.getValue());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Called when a specific tag is selected in the first level of tags.
     */
    protected void selectTag() {
        // Fast fix... should be improved...
        if (simplePalette.getCategoryChooserCB().getSelectedIndex() == 0) {
            return;
        }
        String currentTag = getMainTag();

        if (currentTag != null) {
            setDisplayedSigns(getCompatibleSignsForTag(currentTag));
            updateSecondaryTagCB();
        }
    }

    /**
     * Returns a collection of compatible sign codes for a given tag in the
     * current family.
     *
     * @return
     */
    private Collection<String> getCompatibleSignsForTag(String tag) {
        Collection<String> signs = new TreeSet<>(
                ManuelDeCodage.getCodeComparator());
        if (tag != null) {
            switch (tag) {
                case ALL:
                    // The signs order is given by getSignsCodeInPaletteFamily
                    signs = new ArrayList<>();
                    signs.addAll(getSignsCodeInPaletteFamily());
                    break;
                case OTHERS:
                    /* filter signs without tags. */
                    for (String code : getSignsCodeInPaletteFamily()) {
                        if (hieroglyphsDatabase.getTagsForSign(code).isEmpty()) {
                            signs.add(code);
                        }
                    }
                    break;
                default:
                    /* filter signs in general */
                    for (String code : getSignsCodeInPaletteFamily()) {
                        if (hieroglyphsDatabase.getTagsForSign(code).contains(tag)) {
                            signs.add(code);
                        }
                    }
                    break;

            }
        }
        return signs;
    }

    /**
     * Return all signs codes in a given family, for both actual families and
     * the pseudo-families managed by the palette. (this includes ALL,
     * LAST_USED)
     */
    private Collection<String> getSignsCodeInPaletteFamily() {
        return getSelectedFamily().map(
                family -> {
                    Collection<String> result;
                    switch (family.getCode()) {
                        case LAST_USED:
                            result = lastUsed;
                            break;
                        case USER_PALETTE:
                            result = userPalette;
                            break;
                        case ALL_FAMILIES:
                            result = hieroglyphsDatabase.getCodesForFamily("", true);
                            break;
                        case TALL_NARROW_SIGNS:
                            result = ManuelDeCodage.getInstance().getTallNarrowSigns();
                            break;
                        case LOW_BROAD_SIGNS:
                            result = ManuelDeCodage.getInstance().getLowBroadSigns();
                            break;
                        case LOW_NARROW_SIGNS:
                            result = ManuelDeCodage.getInstance().getLowNarrowSigns();
                            break;
                        default:
                            // Add a whole family...
                            boolean getAllSigns = simplePalette.getShowAllCheckBox()
                                    .isSelected();
                            // Get the list of codes for the given sign family.
                            result = hieroglyphsDatabase.getCodesForFamily(family
                                    .getCode(), getAllSigns);
                            break;
                    }
                    return result;
                }).orElse(Collections.emptyList());

    }

    /**
     * Called when a tag is selected in the secondary level of tags.
     */
    protected void selectSecondaryTag() {
        String secondaryTag = (String) simplePalette.getSecondaryTagCB()
                .getSelectedItem();
        String mainTag = getMainTag();
        Collection<String> signs = getCompatibleSignsForTag(mainTag);
        if (secondaryTag != null) {
            signs.retainAll(getCompatibleSignsForTag(secondaryTag));
        }
        setDisplayedSigns(signs);
    }

    protected void toggleSignInUserPalette() {
        String code = getSelectedCode();
        if (code != null) {
            if (simplePalette.getInUserPaletteCheckBox().isSelected()) {
                userPalette.add(code);
            } else {
                userPalette.remove(code);
            }
            // Save user palette (should be done on program exit).
            // TODO : move this to the program exit.
            savePalette();
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void savePalette() {
        Preferences prefs = JSeshPreferencesRoot.getPreferences();
        StringBuilder buffer = new StringBuilder();
        String sep = "";
        for (String code : userPalette) {
            buffer.append(sep);
            buffer.append(code);
            sep = " ";
        }
        prefs.put(USER_PALETTE_PREF, buffer.toString());
        try {
            prefs.sync();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    private void loadPalette() {
        Preferences prefs = JSeshPreferencesRoot.getPreferences();
        String codes[] = prefs.get(USER_PALETTE_PREF, "").split(" ");
        userPalette.clear();
        userPalette.addAll(Arrays.asList(codes));
    }

    /**
     * Get all signs which have a specific translitteration or code. Actually,
     * get all signs whose transliteration or code contain the text in the text
     * field.
     */
    protected void selectFromTransliterationOrCode() {
        String trl = simplePalette.getTransliterationFilterField().getText().trim();
        if ("".equals(trl))
            return;
        PossibilitiesList l;
        // If it looks like a code...
        if (trl.matches("[a-zA-Z].*[0-9].*")) {
            l = hieroglyphsDatabase.getSuitableSignsForCode(trl).add(
                    hieroglyphsDatabase.getCodesStartingWith(trl));

        } else {
            l = hieroglyphsDatabase.getPossibilityFor(trl,
                    SignDescriptionConstants.PALETTE);
        }
        // We only want Gardiner codes in our list (or similar stuff).
        Iterator<Possibility> it;
        if (l != null) {
            it = l.asList().iterator();
        } else {
            it = Collections.<Possibility>emptyList().iterator();
        }
        ArrayList<String> content = new ArrayList<>();
        while (it.hasNext()) {
            Possibility c = it.next();
            if (c.isSingleSign()
                    && GardinerCode.isWellFormedGardinerCode(c.getCode())) {
                content.add(c.getCode());
            }
        }
        selectNoFamily();
        setDisplayedSigns(content);
    }

    /**
     * list variants of the selected sign.
     */
    protected void selectVariants() {
        if (variantSet == null) {
            variantSet = new TreeSet<>(ManuelDeCodage.getCodeComparator());
            partSet = null;
            variantSet.add(getSelectedCode());
        }
        TreeSet<String> tmp = new TreeSet<>(
                ManuelDeCodage.getCodeComparator());
        tmp.addAll(variantSet);
        Iterator<String> it = tmp.iterator();
        while (it.hasNext()) {
            String code = it.next();
            variantSet.addAll(
                    hieroglyphsDatabase.getVariants(code).stream()
                            .map(variant -> variant.getCode())
                            .collect(Collectors.toList()));
        }
        selectNoFamily();
        setDisplayedSigns(variantSet);
    }

    /**
     * Select the signs which contain the selected sign (first click) and then
     * signs which contain signs which contain the selected sign, etc.
     */
    protected void selectContaining() {
        if (partSet == null) {
            variantSet = null;
            partSet = new TreeSet<>(ManuelDeCodage.getCodeComparator());
            partSet.add(getSelectedCode());
        }
        TreeSet<String> tmp = new TreeSet<>(
                ManuelDeCodage.getCodeComparator());
        tmp.addAll(partSet);
        Iterator<String> it = tmp.iterator();
        while (it.hasNext()) {
            String code = it.next();
            partSet.addAll(hieroglyphsDatabase.getSignsContaining(code));
        }
        selectNoFamily();
        setDisplayedSigns(partSet);
    }

    /**
     * Filter displayed signs to find those which contain the selected sign.
     * 
     * @param code
     */
    protected void selectFilteredContaining(String code) {
        if (!"".equals(code) && null != code) {
            // Compute the closure of the part of relation.
            TreeSet<String> containingSign = new TreeSet<>();
            Stack<String> toDo = new Stack<>();
            toDo.add(code);
            while (!toDo.isEmpty()) {
                String sign = toDo.pop();
                if (!containingSign.contains(sign)) {
                    toDo.addAll(hieroglyphsDatabase.getSignsContaining(sign));
                    containingSign.add(sign);
                }
            }
            // Closure computed. Now the list of displayed signs:
            TreeSet<String> display = new TreeSet<>(
                    ManuelDeCodage.getCodeComparator());
            display.addAll(getDisplayedSigns());
            display.retainAll(containingSign);
            selectNoFamily();
            setDisplayedSigns(display);
        }
    }

    /**
     * Method called when a new cell is focused.
     */
    protected void selectedCellChanged() {
        JList<String> signTable = simplePalette.getSignTable();

        String code = signTable.getSelectedValue();
        if (code != null) {
            setSelectedCode(code);
        }
    }

    /**
     * Allows to use Drag and drop with the slab. Currently will copy the
     * selected sign <em>code</em>. We will write something that also propose a
     * MdC Fragment later.
     *
     * @param dragEnabled
     */
    public void setDragEnabled(boolean dragEnabled) {
        // Add drag and drop facilities (to move later)
        simplePalette.getSignTable().setDragEnabled(dragEnabled);
    }

    private String getSelectedCode() {
        return selectedSignCodes[selectedSignCodeIndex];
    }

    private void updateAccordingToSelectedCategory() {
        // Skip index 0 (no-op)
        if (simplePalette.getCategoryChooserCB().getSelectedIndex() > 0) {
            selectCategory(getSelectedFamily());
        }
    }

    /**
     * Select a sign category to display in the palette.
     *
     * @param optFamily
     */
    private void selectCategory(Optional<HieroglyphFamily> optFamily) {
        optFamily.ifPresent(family -> {
            simplePalette.getTagChooserCB().setModel(new DefaultComboBoxModel<String>());
            // Allow two special families :
            // last signs : the signs chosen from the palette by the user
            // user palette: a user-specific palette.
            Collection<String> signs = getCompatibleSignsForTag(ALL);
            setDisplayedSigns(signs);
            // Prepare the content of the tag selectors...
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            model.addElement(ALL);

            /* Compute the tags associated with all displayed signs */
            Set<String> tagList = new TreeSet<>();
            for (String code : getDisplayedSigns()) {
                tagList.addAll(hieroglyphsDatabase.getTagsForSign(code));
            }
            /* Insert them in the combobox */
            Iterator<String> tagListIterator = tagList.iterator();
            while (tagListIterator.hasNext()) {
                model.addElement(tagListIterator.next());
            }
            model.addElement(OTHERS);
            simplePalette.getTagChooserCB().setModel(model);
            simplePalette.getSecondaryTagCB().setModel(new DefaultComboBoxModel<>());
        });
    }

    /**
     * display those signs in the large sign list, and add their parts to the
     * sub sign combo box.
     *
     * @param signsCodes
     */
    private void setDisplayedSigns(Collection<String> signsCodes) {
        String[] l = new String[signsCodes.size()];
        signsCodes.toArray(l);

        DefaultListModel<String> model = new DefaultListModel<>();
        // add the signs to the list model for general display.
        for (String code : signsCodes) {
            model.addElement(code);
        }

        simplePalette.getSignTable().setModel(model);
        simplePalette.getSignTable().scrollRectToVisible(new Rectangle(0, 0));
        updateSubSignCombobox();
    }

    private void updateSubSignCombobox() {

        TreeSet<String> containedSigns = new TreeSet<>(
                ManuelDeCodage.getCodeComparator());
        // add the signs to the list model for general display.
        // add the signs parts to the list model for the sub-sign selector.
        for (String code : getDisplayedSigns()) {
            for (String subSignCode : hieroglyphsDatabase.getSignsIn(code)) {
                containedSigns.add(subSignCode);
            }
        }

        DefaultComboBoxModel<ListItem<String>> subSignListModel = new DefaultComboBoxModel<>();
        // add the title as a string buffer. This way, it won't be rendered :-)
        subSignListModel
                .addElement(
                        new LabelListItem<>(
                                "filter signs containing"));

        for (String oldSign : selectedSignCodes) {
            if (!oldSign.equals("")) {
                subSignListModel.addElement(
                        new DataListItem<String>(
                                oldSign));
            }
        }
        subSignListModel.addElement(
                new LabelListItem<>(
                        " "));

        for (String signIcon : containedSigns) {
            subSignListModel.addElement(
                    new DataListItem<String>(
                            signIcon));
        }
        simplePalette.getContainsCB().setModel(subSignListModel);
    }

    /**
     * returns the collection of the sign codes currectly displayed
     *
     * @return a collection of String.
     */
    private Collection<String> getDisplayedSigns() {
        TreeSet<String> result = new TreeSet<>(
                ManuelDeCodage.getCodeComparator());
        // run through the possible signs...
        ListModel<String> listModel = simplePalette.getSignTable().getModel();
        for (int i = 0; i < listModel.getSize(); i++) {
            String code = listModel.getElementAt(i);
            result.add(code);
        }
        return result;
    }

    public JSimplePalette getSimplePalette() {
        return simplePalette;
    }

    /**
     * Create a panel with this palette and a tab to display sign informations.
     *
     * @return a panel containing the palette panel, plus sign info.
     */
    public JTabbedPane createComplexPalette() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // This part should probably be handled by using another prepared
        // element.
        tabbedPane.addTab("Palette", getSimplePalette());

        JPanel infoPanel = new JPanel();

        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        JScrollPane sp1 = new JScrollPane(getGlyphDescriptionField());
        sp1.setBorder(BorderFactory.createTitledBorder("Glyph info"));

        JScrollPane sp2 = new JScrollPane(getSignDescriptionField());
        sp2.setBorder(BorderFactory.createTitledBorder("Sign Info"));
        infoPanel.add(sp2);
        infoPanel.add(sp1);

        tabbedPane.addTab("Sign Description", infoPanel);
        return tabbedPane;
    }

    /**
     * Returns a dialog suitable for displaying this palette.
     * <p>
     * Creates the dialog if needed.
     *
     * @return
     */
    public HieroglyphicPaletteDialog getDialog() {
        if (dialog == null) {
            dialog = new HieroglyphicPaletteDialog(this);
        }
        return dialog;
    }

    private final class PaletteRowSelectionListener implements
            ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                selectedCellChanged();
            }
        }
    }

    /**
     * Mouse listener used to send data to the main window.
     *
     * @author rosmord
     *
     */
    private class SimplePaletteMouseListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            // Retrieve the code corresponding to the clicked cell.
            JList<String> table = simplePalette.getSignTable();
            String code = table.getSelectedValue();

            if (code == null || "".equals(code)) {
                return;
            }
            // If there is a code, fill the data
            // fillData(code);
            // In the case of double click, send the data
            if (e.getClickCount() >= 2) {
                sendCode(code);
            }
        }
    }

    /**
     * Sets the listener which will be warned when the palette is clicked.
     *
     * @param hieroglyphPaletteListener
     */
    public void setHieroglyphPaletteListener(
            HieroglyphPaletteListener hieroglyphPaletteListener) {
        this.hieroglyphPaletteListener = hieroglyphPaletteListener;
    }

    private void sendCode(String code) {
        if (hieroglyphPaletteListener != null) {
            hieroglyphPaletteListener.signSelected(code);
        }

        lastUsed.add(code);
    }

    /**
     * Update the various fields displaying the current sign using the available
     * data.
     *
     * @param code
     */
    private void setSelectedCode(String code) {
        selectedSignCodeIndex = (selectedSignCodeIndex + 1)
                % selectedSignCodes.length;
        selectedSignCodes[selectedSignCodeIndex] = code;
        displaySelectedSignInfo();
        updateSubSignCombobox();
    }

    /**
     * Displays info about the currently selected sign.
     *
     * @param signIndex
     */
    private void displaySelectedSignInfo() {
        String code = getSelectedCode();

        JLabel iconLabel = simplePalette.getGlyphPictureLabel();
        JLabel codeLabel = simplePalette.getGlyphCodeLabel();
        JEditorPane infoView = simplePalette.getGlyphInfoText();

        codeLabel.setText(code);
        if ("".equals(code)) {
            iconLabel.setIcon(null);
            infoView.setText("");
        } else {
            HieroglyphPictureBuilder hieroglyphPictureBuilder = new HieroglyphPictureBuilder(hieroglyphicFontManager,
                    simplePalette);
            CanonicalCode canonicalCode = ManuelDeCodage.getInstance().getCanonicalCode(code);
            hieroglyphPictureBuilder.drawIconInLabel(iconLabel, canonicalCode, 4);
            List<String> values = hieroglyphsDatabase.getValuesFor(code);
            if (values == null) {
                values = new ArrayList<>();
            }

            infoView.setContentType("text/html");
            StringBuilder content = new StringBuilder();
            content.append("<dl><dt><b>values</b></dt><dd>");
            for (int i = 0; i < values.size(); i++) {
                content.append(values.get(i));
                if (i != values.size() - 1) {
                    content.append(", ");
                }

            }
            content.append("</dd></dl>");
            infoView.setText(content.toString());

            simplePalette.getInUserPaletteCheckBox().setSelected(
                    userPalette.contains(code));
            String fullDescription = hieroglyphsDatabase.getDescriptionFor(code);

            try {
                signDescriptionField.setMDCText(fullDescription);
            } catch (RuntimeException e) {
                // e.printStackTrace();
                signDescriptionField.setMDCText("+lErroneous code for +s"
                        + code + "+l " + code
                        + " description. Please notice or correct.");
            }
            ShapeChar shape = hieroglyphicFontManager.get(canonicalCode);
            if (shape != null) {
                glyphDescriptionField.setText(shape.getDocumentation());
            } else {
                glyphDescriptionField.setText("");
            }
        }
        variantSet = null;
        partSet = null;
    }

    /**
     * Circle through the list of selected signs.
     */
    private void selectPreviousSign() {
        // Previous code (modulo the size of the buffer)
        selectedSignCodeIndex = (selectedSignCodeIndex
                + selectedSignCodes.length - 1)
                % selectedSignCodes.length;
        displaySelectedSignInfo();
    }

    private void filterFromContainsCB() {
        Optional<ListItem<String>> optSelected = ComboboxUtils.getSelectedItem(simplePalette.getContainsCB());
        optSelected.ifPresent(selected -> {
            if (selected instanceof DataListItem<String> selectedFilter) {
                selectFilteredContaining(selectedFilter.getValue());
                selectNoFamily();
            }
        });        
    }

    /**
     * Sets the selected family to "none".
     * <p>
     * Called when a manipulation invalidate the currently selected family. (for
     * instance, filtering signs by transliteration).
     * <p>
     * If we don't do that, on the mac, the user can't directly reselect the
     * previously displayed sign family.
     */
    private void selectNoFamily() {
        simplePalette.getCategoryChooserCB().setSelectedIndex(0);
    }

    private void updateSecondaryTagCB() {
        TreeSet<String> tags = new TreeSet<>();// Strings...
        // run through the possible signs...
        Collection<String> displayedSigns = getDisplayedSigns();
        for (String code : displayedSigns) {
            tags.addAll(hieroglyphsDatabase.getTagsForSign(code));
        }
        DefaultComboBoxModel<String> secondaryCBModel = new DefaultComboBoxModel<>(
                tags.toArray(new String[tags.size()]));
        simplePalette.getSecondaryTagCB().setModel(secondaryCBModel);
    }

    private JMDCEditor getSignDescriptionField() {
        return signDescriptionField;
    }

    private JTextArea getGlyphDescriptionField() {
        return glyphDescriptionField;
    }

    private String getMainTag() {
        return (String) simplePalette.getTagChooserCB().getSelectedItem();
    }
}

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
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jsesh.editor.JMDCEditor;
import jsesh.hieroglyphs.CompositeHieroglyphsManager;
import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.GardinerCode;
import jsesh.hieroglyphs.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.HieroglyphFamily;
import jsesh.hieroglyphs.HieroglyphicBitmapBuilder;
import jsesh.hieroglyphs.ManuelDeCodage;
import jsesh.hieroglyphs.PossibilitiesList;
import jsesh.hieroglyphs.ShapeChar;
import jsesh.hieroglyphs.SignDescriptionConstants;

/**
 * Control and data feed for the simple palette.
 * 
 * Currently, one might prefer to use HieroglyphicPaletteDialog, which provides
 * also a glyph information tab. (See, however, method createFullPalette).
 * 
 * 
 * TODO : use a MultiLingual label instead of the plain tag name...
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

	private class InsertSignAction extends AbstractAction {
		private static final long serialVersionUID = 6512709688609818997L;

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
	private JSimplePalette simplePalette;
	/**
	 * Field for detailled sign information.
	 */
	private JMDCEditor signDescriptionField;
	/**
	 * Field for glyph information.
	 */
	private JTextArea glyphDescriptionField;
	private HieroglyphPaletteListener hieroglyphPaletteListener;
	private HieroglyphDatabaseInterface hieroglyphsManager;
	/**
	 * Signs already got from the palette, for faster retrieval.
	 */
	private Set<String> lastUsed = new TreeSet<String>(
			GardinerCode.getCodeComparator());
	/**
	 * Signs placed in the user palette.
	 */
	private Set<String> userPalette = new TreeSet<String>(
			GardinerCode.getCodeComparator());
	private Set<String> partSet;
	private Set<String> variantSet;
	/**
	 * Selected sign codes (may be empty strings)
	 */
	private String[] selectedSignCodes = { "", "", "", "", "", "" };
	/**
	 * Index of the selected sign code in the buffer.
	 */
	private int selectedSignCodeIndex = 0;

	/**
	 * A dialog (which can be null or can be used to display this palette).
	 */
	private HieroglyphicPaletteDialog dialog;

	public PalettePresenter() {
		hieroglyphsManager = CompositeHieroglyphsManager.getInstance();
		simplePalette = new JSimplePalette();

		signDescriptionField = new JMDCEditor();
		signDescriptionField.setScale(1);

		glyphDescriptionField = new JTextArea();
		glyphDescriptionField.setEditable(false);

		preparePalette();
		loadPalette();
	}

	// Link the graphic component to its logic.
	private void preparePalette() {
		// Sign families
		DefaultComboBoxModel comboModel = new DefaultComboBoxModel();
		List<HieroglyphFamily> families = hieroglyphsManager.getFamilies();
		comboModel.addElement("Select a sign family");
		comboModel.addElement(new HieroglyphFamily(LAST_USED, "Latest signs"));
		comboModel
				.addElement(new HieroglyphFamily(USER_PALETTE, "User Palette"));

		for (int i = 0; i < families.size(); i++) {
			comboModel.addElement(families.get(i));
		}
		comboModel.addElement(new HieroglyphFamily(ALL_FAMILIES, "All Signs"));
		comboModel.addElement(new HieroglyphFamily(TALL_NARROW_SIGNS,
				"Tall Narrow Signs"));
		comboModel.addElement(new HieroglyphFamily(LOW_BROAD_SIGNS,
				"Low Broad Signs"));
		comboModel.addElement(new HieroglyphFamily(LOW_NARROW_SIGNS,
				"Low Narrow Signs"));

		simplePalette.getCategoryChooserCB().setModel(comboModel);
		simplePalette.getCategoryChooserCB().setMaximumRowCount(28);

		// activate buttons.

		simplePalette.getShowContainingButtons().addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						selectContaining();
					}
				});

		simplePalette.getBackButton().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				selectPreviousSign();
			}
		});

		simplePalette.getShowVariantsButton().addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						selectVariants();
					}
				});

		simplePalette.getTranslitterationFilterField().addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						selectFromTranslitterationOrCode();
					}
				});

		simplePalette.getContainsCB().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filterFromContainsCB();
			}
		});
		// Sign table
		SignListCellRenderer renderer = new SignListCellRenderer(simplePalette);
		final int bitmapHeight = 40;
		renderer.setBitmapHeight(bitmapHeight);

		JList signTable = simplePalette.getSignTable();
		// Aspect and drawing related stuff.
		signTable.setCellRenderer(renderer);
		// signTable.setPrototypeCellValue("A248B");
		signTable.setFixedCellHeight((int) (bitmapHeight * 1.2));
		signTable.setFixedCellWidth((int) (2.5 * bitmapHeight));

		// signTable.setRowHeight((int) (renderer.getBitmapHeight() * 1.2));
		// Row and column selection don't mean anything in our context. Away
		// with them !
		// signTable.setRowSelectionAllowed(true);
		// signTable.setColumnSelectionAllowed(true);
		// Listen for selection (in fact focus) change.
		// signTable.getColumnModel().addColumnModelListener(
		// new PaletteColumnListener());
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
				simplePalette);
		smallListRenderer.setDisplaySignsCodes(true);
		smallListRenderer.setBitmapHeight(20);
		simplePalette.getContainsCB().setRenderer(smallListRenderer);
		simplePalette.getContainsCB().setPrototypeDisplayValue("A1");

		ActionMap newActionMap = new ActionMap();
		newActionMap.setParent(signTable.getActionMap());
		newActionMap.put("INSERT_SIGN", new InsertSignAction());
		signTable.setActionMap(newActionMap);

		// Simple action, refreshes the list according to the selection.
		ActionListener updateToSelectedCategoryListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				updateAccordingToSelectedCategory();
			}
		};

		// Control for user palette:
		simplePalette.getInUserPaletteCheckBox().addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						toggleSignInUserPalette();
					}
				});

		// Control for tags

		simplePalette.getTagChooserCB().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				selectTag();
			}
		});

		simplePalette.getSecondaryTagCB().addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						selectSecondaryTag();
					}
				});
		// the table is refereshed when the selection changes, or when the
		// checkbox is checked.
		simplePalette.getCategoryChooserCB().addActionListener(
				updateToSelectedCategoryListener);
		simplePalette.getShowAllCheckBox().addActionListener(
				updateToSelectedCategoryListener);

		simplePalette.getCategoryChooserCB().setSelectedItem(families.get(0));

		simplePalette.getShowAllCheckBox().setSelected(true);

		System.out.println(UIManager.getLookAndFeel().toString());
		// if (PlatformDetection.getPlatform() == PlatformDetection.MACOSX &&
		// UIManager.getLookAndFeel().toString().contains("Quaqua")) {
		// }

		// First update.
		updateAccordingToSelectedCategory();

	}

	/**
	 * Returns the family currently selected.
	 * 
	 * @return
	 */
	public HieroglyphFamily getSelectedFamily() {
		HieroglyphFamily hieroglyphFamily = (HieroglyphFamily) simplePalette
				.getCategoryChooserCB().getSelectedItem();
		// In some case, there might be no selected item
		if (hieroglyphFamily == null) {
			hieroglyphFamily = (HieroglyphFamily) simplePalette
					.getCategoryChooserCB().getModel().getElementAt(0);
			selectNoFamily();
		}
		return hieroglyphFamily;
	}

	/**
	 * Called when a specific tag is selected in the first level of tags.
	 */
	protected void selectTag() {
		// Fast fix... should be improved...
		if (simplePalette.getCategoryChooserCB().getSelectedIndex() == 0)
			return;
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
		Collection<String> signs = new TreeSet<String>(
				GardinerCode.getCodeComparator());
		if (tag != null) {
			if (ALL.equals(tag)) {
				// The signs order is given by getSignsCodeInPaletteFamily
				signs = new ArrayList<String>();
				signs.addAll(getSignsCodeInPaletteFamily());
			} else if (OTHERS.equals(tag)) {
				/* filter signs without tags. */
				for (Iterator<String> iter = getSignsCodeInPaletteFamily()
						.iterator(); iter.hasNext();) {
					String code = iter.next();
					if (hieroglyphsManager.getTagsForSign(code).isEmpty())
						signs.add(code);
				}

			} else {
				/* filter signs in general */
				for (Iterator<String> iter = getSignsCodeInPaletteFamily()
						.iterator(); iter.hasNext();) {
					String code = iter.next();
					if (hieroglyphsManager.getTagsForSign(code).contains(tag))
						signs.add(code);
				}
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
		Collection<String> result = null;
		if (getSelectedFamily().getCode().equals(LAST_USED)) {
			result = lastUsed;
		} else if (getSelectedFamily().getCode().equals(USER_PALETTE)) {
			result = userPalette;
		} else if (getSelectedFamily().getCode().equals(ALL_FAMILIES)) {
			result = hieroglyphsManager.getCodesForFamily("", true);
		} else if (getSelectedFamily().getCode().equals(TALL_NARROW_SIGNS)) {
			result = ManuelDeCodage.getInstance().getTallNarrowSigns();
		} else if (getSelectedFamily().getCode().equals(LOW_BROAD_SIGNS)) {
			result = ManuelDeCodage.getInstance().getLowBroadSigns();
		} else if (getSelectedFamily().getCode().equals(LOW_NARROW_SIGNS)) {
			result = ManuelDeCodage.getInstance().getLowNarrowSigns();
		} else { // Add a whole family...
			boolean getAllSigns = simplePalette.getShowAllCheckBox()
					.isSelected();
			// Get the list of codes for the given sign family.
			result = hieroglyphsManager.getCodesForFamily(getSelectedFamily()
					.getCode(), getAllSigns);
		}
		return result;
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

	private void savePalette() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		StringBuffer buffer = new StringBuffer();
		String sep = "";
		for (Iterator<String> it = userPalette.iterator(); it.hasNext();) {
			String code = it.next();
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
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		String codes[] = prefs.get(USER_PALETTE_PREF, "").split(" ");
		userPalette.clear();
		userPalette.addAll(Arrays.asList(codes));
	}

	/**
	 * Get all signs which have a specific translitteration or code. Actually,
	 * get all signs whose transliteration or code contain the text in the text
	 * field.
	 */
	protected void selectFromTranslitterationOrCode() {
		String trl = simplePalette.getTranslitterationFilterField().getText();
		PossibilitiesList l;
		// If it looks like a code...
		if (trl.matches(".*[0-9].*")) {
			l = hieroglyphsManager.getSuitableSignsForCode(trl).add(
					hieroglyphsManager.getCodesStartingWith(trl));

		} else {
			l = hieroglyphsManager.getPossibilityFor(trl,
					SignDescriptionConstants.PALETTE);
		}
		// We only want Gardiner codes in our list (or similar stuff).
		Iterator<String> it;
		if (l != null) {
			it = l.asList().iterator();
		} else {
			List<String> empty = Collections.emptyList();
			it = empty.iterator();
		}
		ArrayList<String> content = new ArrayList<String>();
		while (it.hasNext()) {
			String c = it.next();
			if (GardinerCode.isCorrectGardinerCode(c)) {
				content.add(c);
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
			variantSet = new TreeSet<String>(GardinerCode.getCodeComparator());
			partSet = null;
			variantSet.add(getSelectedCode());
		}
		TreeSet<String> tmp = new TreeSet<String>(
				GardinerCode.getCodeComparator());
		tmp.addAll(variantSet);
		Iterator<String> it = tmp.iterator();
		while (it.hasNext()) {
			String code = it.next();
			variantSet.addAll(hieroglyphsManager.getVariants(code));
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
			partSet = new TreeSet<String>(GardinerCode.getCodeComparator());
			partSet.add(getSelectedCode());
		}
		TreeSet<String> tmp = new TreeSet<String>(
				GardinerCode.getCodeComparator());
		tmp.addAll(partSet);
		Iterator<String> it = tmp.iterator();
		while (it.hasNext()) {
			String code = it.next();
			partSet.addAll(hieroglyphsManager.getSignsContaining(code));
		}
		selectNoFamily();
		setDisplayedSigns(partSet);
	}

	/**
	 * Filter displayed signs to find those which contain the selected sign.
	 */
	protected void selectFilteredContaining(String code) {
		if (!"".equals(code) && null != code) {
			// Compute the closure of the part of relation.
			TreeSet<String> containingSign = new TreeSet<String>();
			Stack<String> toDo = new Stack<String>();
			toDo.add(code);
			while (!toDo.isEmpty()) {
				String sign = toDo.pop();
				if (!containingSign.contains(sign)) {
					toDo.addAll(hieroglyphsManager.getSignsContaining(sign));
					containingSign.add(sign);
				}
			}
			// Closure computed. Now the list of displayed signs:
			TreeSet<String> display = new TreeSet<String>(
					GardinerCode.getCodeComparator());
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
		JList signTable = simplePalette.getSignTable();

		String code = (String) signTable.getSelectedValue();
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
		if (simplePalette.getCategoryChooserCB().getSelectedIndex() > 0)
			selectCategory(getSelectedFamily());
	}

	/**
	 * Select a sign category to display in the palette.
	 * 
	 * @param family
	 */
	public void selectCategory(HieroglyphFamily family) {
		simplePalette.getTagChooserCB().setModel(new DefaultComboBoxModel());
		// Allow two special families :
		// last signs : the signs chosen from the palette by the user
		// user palette: a user-specific palette.
		Collection<String> signs = getCompatibleSignsForTag(ALL);
		setDisplayedSigns(signs);
		// Prepare the content of the tag selectors...
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement(ALL);

		/* Compute the tags associated with all displayed signs */
		Set<String> tagList = new TreeSet<String>();
		for (Iterator<String> signIterator = getDisplayedSigns().iterator(); signIterator
				.hasNext();) {
			String code = signIterator.next();
			tagList.addAll(hieroglyphsManager.getTagsForSign(code));
		}
		/* Insert them in the combobox */
		Iterator<String> tagListIterator = tagList.iterator();
		while (tagListIterator.hasNext()) {
			model.addElement(tagListIterator.next());
		}
		model.addElement(OTHERS);
		simplePalette.getTagChooserCB().setModel(model);
		simplePalette.getSecondaryTagCB().setModel(new DefaultComboBoxModel());
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

		DefaultListModel model = new DefaultListModel();
		// add the signs to the list model for general display.
		for (String code : signsCodes) {
			model.addElement(code);
		}

		simplePalette.getSignTable().setModel(model);
		simplePalette.getSignTable().scrollRectToVisible(new Rectangle(0, 0));
		updateSubSignCombobox();
	}

	private void updateSubSignCombobox() {

		TreeSet<String> containedSigns = new TreeSet<String>(
				GardinerCode.getCodeComparator());
		// add the signs to the list model for general display.
		// add the signs parts to the list model for the sub-sign selector.
		for (String code : getDisplayedSigns()) {
			for (String subSignCode : hieroglyphsManager.getSignsIn(code)) {
				containedSigns.add(subSignCode);
			}
		}

		DefaultComboBoxModel subSignListModel = new DefaultComboBoxModel();
		// add the title as a string buffer. This way, it won't be rendered :-)
		subSignListModel
				.addElement(new StringBuffer("filter signs containing"));

		for (String oldSign : selectedSignCodes) {
			if (!oldSign.equals(""))
				subSignListModel.addElement(oldSign);
		}
		subSignListModel.addElement(new StringBuffer(" "));

		for (String signIcon : containedSigns) {
			subSignListModel.addElement(signIcon);
		}
		simplePalette.getContainsCB().setModel(subSignListModel);
	}

	/**
	 * returns the collection of the sign codes currectly displayed
	 * 
	 * @return a collection of String.
	 */
	private Collection<String> getDisplayedSigns() {
		TreeSet<String> result = new TreeSet<String>(
				GardinerCode.getCodeComparator());
		// run through the possible signs...
		ListModel listModel = simplePalette.getSignTable().getModel();
		for (int i = 0; i < listModel.getSize(); i++) {
			String code = (String) listModel.getElementAt(i);
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
	 * <p> Creates the dialog if needed.
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
	public class SimplePaletteMouseListener extends MouseAdapter {

		public void mouseClicked(MouseEvent e) {
			// Retrieve the code corresponding to the clicked cell.
			JList table = simplePalette.getSignTable();
			String code = (String) table.getSelectedValue();

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

	public void sendCode(String code) {
		if (hieroglyphPaletteListener != null) {
			hieroglyphPaletteListener.signSelected(code);
		}

		String trl = simplePalette.getTranslitterationFilterField().getText();
		// If we got the sign from a transliteration, add it...
		if (!"".equals(trl)
				&& hieroglyphsManager.getValuesFor(code).contains(trl)) {
			{
				PossibilitiesList possibilities = hieroglyphsManager
						.getPossibilityFor(trl,
								SignDescriptionConstants.KEYBOARD);
				possibilities.add(code);
				while (!possibilities.getCurrentSign().equals(code)) {
					possibilities.next();
				}
			}
		}
		lastUsed.add(code);
	}

	/**
	 * Update the various fields displaying the current sign using the available
	 * data.
	 * 
	 * @param code
	 */
	public void setSelectedCode(String code) {
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
			iconLabel.setIcon(HieroglyphicBitmapBuilder.createHieroglyphIcon(
					code, iconLabel.getHeight() - 4, 4, simplePalette));
			List<String> values = hieroglyphsManager.getValuesFor(code);
			if (values == null) {
				values = new ArrayList<String>();
			}

			infoView.setContentType("text/html");
			StringBuffer content = new StringBuffer();
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
			String fullDescription = hieroglyphsManager.getDescriptionFor(code);

			try {
				signDescriptionField.setMDCText(fullDescription);
			} catch (RuntimeException e) {
				e.printStackTrace();
				signDescriptionField.setMDCText("+lErroneous code for +s"
						+ code + "+l " + code
						+ " description. Please notice or correct.");
			}

			ShapeChar shape = DefaultHieroglyphicFontManager.getInstance().get(
					code);
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

	public void filterFromContainsCB() {
		Object selected = simplePalette.getContainsCB().getSelectedItem();
		// The list also contains text.
		if (selected instanceof String) {
			selectFilteredContaining((String) selected);
			selectNoFamily();
		}
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
		TreeSet<String> tags = new TreeSet<String>();// Strings...
		// run through the possible signs...
		Collection<String> displayedSigns = getDisplayedSigns();
		for (Iterator<String> it = displayedSigns.iterator(); it.hasNext();) {
			String code = it.next();
			tags.addAll(hieroglyphsManager.getTagsForSign(code));
		}
		DefaultComboBoxModel secondaryCBModel = new DefaultComboBoxModel(
				tags.toArray());
		simplePalette.getSecondaryTagCB().setModel(secondaryCBModel);
	}

	public void setUserPalette(Collection<String> newUserPalette) {
		userPalette.clear();
		userPalette.addAll(newUserPalette);
	}

	public Set<String> getUserPalette() {
		return userPalette;
	}

	public JMDCEditor getSignDescriptionField() {
		return signDescriptionField;
	}

	public JTextArea getGlyphDescriptionField() {
		return glyphDescriptionField;
	}

	public String getMainTag() {
		return (String) simplePalette.getTagChooserCB().getSelectedItem();
	}
}

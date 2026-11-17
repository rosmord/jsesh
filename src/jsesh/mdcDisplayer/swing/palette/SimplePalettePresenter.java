package jsesh.mdcDisplayer.swing.palette;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableModel;

import jsesh.bzr.simple.ShapeChar;
import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;
import jsesh.hieroglyphs.DefaultHieroglyphsManager;
import jsesh.hieroglyphs.GardinerCode;
import jsesh.hieroglyphs.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.HieroglyphFamily;
import jsesh.hieroglyphs.HieroglyphicBitmapBuilder;
import jsesh.hieroglyphs.HieroglyphsManager;
import jsesh.hieroglyphs.PossibilitiesList;
import jsesh.hieroglyphs.SignDescriptionConstants;
import jsesh.hieroglyphs.SignInfo;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdcDisplayer.swing.editor.JMDCEditor;

/**
 * Control and data feed for the simple palette. TODO : use a MultiLingual label
 * instead of the plain tag name...
 * 
 * @author rosmord
 * 
 */
public class SimplePalettePresenter {

	private static final String USER_PALETTE_PREF = "userPalette";

	private static final String OTHERS = "others";

	private static final String ALL = "all";

	private static final String USER_PALETTE = "1";

	private static final String LAST_USED = "0";

	public class InsertSignAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {
			String code = getSelectedCode();
			if (code != null)
				sendCode(code);
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
	 * last sign selected from the palette.
	 */

	private String selectedCode = "";

	/**
	 * Signs already got from the palette, for faster retrieval.
	 */
	private Set lastUsed = new TreeSet(GardinerCode.getCodeComparator());

	/**
	 * Signs placed in the user palette.
	 */
	private Set userPalette = new TreeSet(GardinerCode.getCodeComparator());

	private Set partSet;
	private Set variantSet;
	
	public SimplePalettePresenter() {
		hieroglyphsManager = DefaultHieroglyphsManager.getInstance();
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
		List families = hieroglyphsManager.getFamilies();
		comboModel.addElement(new HieroglyphFamily(LAST_USED, "Latest signs"));
		comboModel
				.addElement(new HieroglyphFamily(USER_PALETTE, "User Palette"));

		for (int i = 0; i < families.size(); i++) {
			comboModel.addElement(families.get(i));
		}

		simplePalette.getCategoryChooserCB().setModel(comboModel);
		simplePalette.getCategoryChooserCB().setMaximumRowCount(28);

		// activate buttons.

		simplePalette.getShowContainingButtons().addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						selectContaining();
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
						selectTranslitteration();
					}
				});

		// Sign table
		SignTableCellRenderer renderer = new SignTableCellRenderer(
				simplePalette);
		renderer.setBitmapHeight(40);

		JTable signTable = simplePalette.getSignTable();
		// Aspect and drawing related stuff.
		signTable.setDefaultRenderer(Object.class, renderer);
		signTable.setRowHeight((int) (renderer.getBitmapHeight() * 1.2));
		// Row and column selection don't mean anything in our context. Away
		// with them !
		signTable.setRowSelectionAllowed(true);
		signTable.setColumnSelectionAllowed(true);
		// Listen for selection (in fact focus) change.
		signTable.getColumnModel().addColumnModelListener(
				new PaletteColumnListener());
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

		// the table is refereshed when the selection changes, or when the
		// checkbox is checked.
		simplePalette.getCategoryChooserCB().addActionListener(
				updateToSelectedCategoryListener);
		simplePalette.getShowAllCheckBox().addActionListener(
				updateToSelectedCategoryListener);

		simplePalette.getCategoryChooserCB().setSelectedItem(families.get(0));

		// First update.
		updateAccordingToSelectedCategory();

	}

	public HieroglyphFamily getSelectedFamily() {
		HieroglyphFamily hieroglyphFamily = (HieroglyphFamily) simplePalette
				.getCategoryChooserCB().getSelectedItem();
		return hieroglyphFamily;
	}

	protected void selectTag() {
		String currentTag = (String) simplePalette.getTagChooserCB()
				.getSelectedItem();

		if (currentTag != null) {
			if (ALL.equals(currentTag)) {
				selectCategory(getSelectedFamily());
			} else if (OTHERS.equals(currentTag)) {
				Collection signs = new TreeSet(GardinerCode.getCodeComparator());
				signs.addAll(hieroglyphsManager
						.getSignsWithoutTagInFamily(getSelectedFamily()
								.getCode()));
				setTableContent(signs);
			} else {
				Collection signs = new TreeSet(GardinerCode.getCodeComparator());
				signs.addAll(hieroglyphsManager.getSignsWithTagInFamily(
						currentTag, getSelectedFamily().getCode()));

				setTableContent(signs);
			}
		}

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
		for (Iterator it = userPalette.iterator(); it.hasNext();) {
			String code = (String) it.next();
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

	protected void selectTranslitteration() {
		String trl = simplePalette.getTranslitterationFilterField().getText();
		boolean getAll = simplePalette.getShowAllCheckBox().isSelected();

		PossibilitiesList l = hieroglyphsManager.getPossibilityFor(trl,
				SignDescriptionConstants.PALETTE);
		// We only want Gardiner codes in our list (or similar stuff).
		Iterator it;
		if (l != null)
			it = l.asList().iterator();
		else
			it = Collections.EMPTY_LIST.iterator();
		ArrayList content = new ArrayList();
		while (it.hasNext()) {
			String c = (String) it.next();
			if (GardinerCode.isCorrectGardinerCode(c))
				content.add(c);
		}
		setTableContent(content);
	}

	protected void selectVariants() {
		if (variantSet == null) {
			variantSet= new TreeSet(GardinerCode.getCodeComparator());
			partSet= null;
			variantSet.add(getSelectedCode());
		}
		TreeSet tmp= new TreeSet(GardinerCode.getCodeComparator());
		tmp.addAll(variantSet);
		Iterator it= tmp.iterator();
		while (it.hasNext()) {
			String code= (String) it.next();
			variantSet.addAll(hieroglyphsManager.getVariants(code));
		}
		setTableContent(variantSet);
	}

	protected void selectContaining() {
		if (partSet == null) {
			variantSet= null;
			partSet= new TreeSet(GardinerCode.getCodeComparator());
			partSet.add(getSelectedCode());
		}
		TreeSet tmp = new TreeSet(GardinerCode.getCodeComparator());
		tmp.addAll(partSet);
		Iterator it= tmp.iterator();
		while (it.hasNext()) {
			String code= (String) it.next();
			partSet
				.addAll(hieroglyphsManager
						.getSignsContaining(code));
		}
		setTableContent(partSet);
	}

	/**
	 * Method called when a new cell is focused.
	 * 
	 */
	protected void selectedCellChanged() {
		String code = null;
		JTable signTable = simplePalette.getSignTable();

		// A) compute the selected and focused window
		int selectedRow = signTable.getSelectedRow();
		int selectedColumn = signTable.getSelectedColumn();

		// B) gets the code.
		if (selectedColumn > -1 && selectedRow > -1) {
			code = (String) signTable.getValueAt(selectedRow, selectedColumn);
		}
		if (code != null)
			updateDisplayedSignData(code);
	}

	/**
	 * Allows to use Drag and drop with the slab. Currently will copy the
	 * selected sign <em>code</em>. We will write something that also propose
	 * a MdC Fragment later.
	 * 
	 * @param dragEnabled
	 */
	public void setDragEnabled(boolean dragEnabled) {
		// Add drag and drop facilities (to move later)
		simplePalette.getSignTable().setDragEnabled(dragEnabled);
	}

	private String getSelectedCode() {
		return selectedCode;
	}

	private void updateAccordingToSelectedCategory() {
		selectCategory((HieroglyphFamily) simplePalette.getCategoryChooserCB()
				.getSelectedItem());
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
		if (family.getCode().equals(LAST_USED)) {
			setTableContent(lastUsed);
		} else if (family.getCode().equals(USER_PALETTE)) {
			setTableContent(userPalette);
		} else {

			boolean getAllSigns = simplePalette.getShowAllCheckBox()
					.isSelected();
			// Get the list of codes for the given sign family.
			String[] l = hieroglyphsManager.getCodesForFamily(family.getCode(),
					getAllSigns);
			// Filter the signs according to the selection.
			ArrayList filteredList = new ArrayList();
			if (!getAllSigns) {
				// TODO: WHEN THE SIGN INFORMATIONS ARE GOOD ENOUGH, SUPPRESS
				// THE FOLLOWING TEST.
				Pattern pattern = Pattern.compile("(Aa|Ff|[A-IK-Z])[0-9]+");

				for (int i = 0; i < l.length; i++) {
					if (pattern.matcher(l[i]).matches()) {
						filteredList.add(l[i]);
					} else if (hieroglyphsManager.isAlwaysDisplayed(l[i]))
						filteredList.add(l[i]);
				}
			} else {
				filteredList.addAll(Arrays.asList(l));
			}
			setTableContent(filteredList);

			DefaultComboBoxModel model = new DefaultComboBoxModel();
			model.addElement(ALL);
			Collection tagList = hieroglyphsManager.getTagsForFamily(family
					.getCode());
			Iterator tagListIterator = tagList.iterator();
			while (tagListIterator.hasNext()) {
				model.addElement(tagListIterator.next());
			}
			model.addElement(OTHERS);
			simplePalette.getTagChooserCB().setModel(model);
		}
	}

	private void setTableContent(Collection signsCodes) {
		String[] l = new String[signsCodes.size()];
		signsCodes.toArray(l);
		simplePalette.getSignTable().setModel(new CompactListTableModel(l));
		simplePalette.getSignTable().scrollRectToVisible(new Rectangle(0, 0));
	}

	public JSimplePalette getSimplePalette() {
		return simplePalette;
	}

	private final class PaletteRowSelectionListener implements
			ListSelectionListener {
		public void valueChanged(ListSelectionEvent e) {
			selectedCellChanged();
		}
	}

	private final class PaletteColumnListener implements
			TableColumnModelListener {
		public void columnAdded(TableColumnModelEvent e) {
			// NO OP
		}

		public void columnMarginChanged(ChangeEvent e) {
			// NO OP
		}

		public void columnMoved(TableColumnModelEvent e) {
			// NO OP
		}

		public void columnRemoved(TableColumnModelEvent e) {
			// NO OP
		}

		public void columnSelectionChanged(ListSelectionEvent e) {
			selectedCellChanged();
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
			JTable table = simplePalette.getSignTable();
			int i = table.getColumnModel().getColumnIndexAtX(e.getX());
			int j = e.getY() / table.getRowHeight();
			TableModel model = table.getModel();
			String code = (String) model.getValueAt(j, i);

			if (code == null || "".equals(code))
				return;
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
		if (hieroglyphPaletteListener != null)
			hieroglyphPaletteListener.signSelected(code);
		String trl = simplePalette.getTranslitterationFilterField().getText();
		// If we got the sign from a transliteration, add it...
		if (!"".equals(trl)
				&& hieroglyphsManager.getValuesFor(code).contains(trl)) {
			{
				PossibilitiesList possibilities = hieroglyphsManager.getPossibilityFor(trl, SignDescriptionConstants.KEYBOARD);
				possibilities.add(code);
				while (! possibilities.getCurrentSign().equals(code)) {
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
	public void updateDisplayedSignData(String code) {
		selectedCode = code;
		JLabel iconLabel = simplePalette.getGlyphPictureLabel();
		JLabel codeLabel = simplePalette.getGlyphCodeLabel();
		JEditorPane infoView = simplePalette.getGlyphInfoText();

		codeLabel.setText(code);
		iconLabel.setIcon(HieroglyphicBitmapBuilder.createHieroglyphIcon(code,
				iconLabel.getHeight() - 4, 4, simplePalette));
		List values = hieroglyphsManager.getValuesFor(code);
		if (values == null)
			values = new ArrayList();
		infoView.setContentType("text/html");
		StringBuffer content = new StringBuffer();
		content.append("<dl><dt><b>values</b></dt><dd>");
		for (int i = 0; i < values.size(); i++) {
			content.append(values.get(i));
			if (i != values.size() - 1)
				content.append(", ");
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
			signDescriptionField.setMDCText("+lErroneous code for +s" + code
					+ "+l " + code + " description. Please notice or correct.");
		}
		ShapeChar shape = DefaultHieroglyphicFontManager.getInstance()
				.get(code);
		if (shape != null)
			glyphDescriptionField.setText(shape.getDocumentation());
		else
			glyphDescriptionField.setText("");
		
		variantSet=null;
		partSet= null;
	}

	public void setUserPalette(Collection newUserPalette) {
		userPalette.clear();
		userPalette.addAll(newUserPalette);
	}

	public Set getUserPalette() {
		return userPalette;
	}

	public JMDCEditor getSignDescriptionField() {
		return signDescriptionField;
	}

	public JTextArea getGlyphDescriptionField() {
		return glyphDescriptionField;
	}
}

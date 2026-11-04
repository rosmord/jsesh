package jsesh.jhotdraw.preferences.document.ui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import jsesh.jhotdraw.utils.PanelBuilder;
import jsesh.render.style.PageTextPosition;
import jsesh.render.style.StandardPageSize;
import jsesh.resources.JSeshMessages;
import jsesh.ui.widgets.units.LengthUnit;
import net.miginfocom.swing.MigLayout;

/// The form used to choose the page size and margins of a document.
///
/// The check box at the top switches the page on and off. When it is not
/// selected, all the other controls are disabled.
public class JPageSpecificationEditor {

	/// An entry in the "standard size" combo box.
	///
	/// @param size a standard size, or null for a size which is not standard.
	public record SizeChoice(StandardPageSize size) {
		@Override
		public String toString() {
			return size == null ? JSeshMessages.getString("pagePrefs.customSize") : size.toString();
		}
	}

	/// The "custom size" entry.
	public static final SizeChoice CUSTOM = new SizeChoice(null);

	private final JPanel panel = new JPanel();

	private final JCheckBox enableBox = new JCheckBox(JSeshMessages.getString("pagePrefs.enable"));
	private final JComboBox<SizeChoice> sizeField = new JComboBox<>();
	private final JRadioButton portraitButton = new JRadioButton(JSeshMessages.getString("pagePrefs.portrait"));
	private final JRadioButton landscapeButton = new JRadioButton(JSeshMessages.getString("pagePrefs.landscape"));
	private final JFormattedTextField widthField = new JFormattedTextField();
	private final JFormattedTextField heightField = new JFormattedTextField();
	private final JFormattedTextField marginLeftField = new JFormattedTextField();
	private final JFormattedTextField marginRightField = new JFormattedTextField();
	private final JFormattedTextField marginTopField = new JFormattedTextField();
	private final JFormattedTextField marginBottomField = new JFormattedTextField();
	private final JComboBox<LengthUnit> unitField = new JComboBox<>();
	private final JComboBox<PageTextPosition> numberPositionField = new JComboBox<>(PageTextPosition.values());
	private final JComboBox<PageTextPosition> titlePositionField = new JComboBox<>(PageTextPosition.values());
	private final JTextField titleField = new JTextField(20);
	private final JCheckBox alternateBox = new JCheckBox(JSeshMessages.getString("pagePrefs.alternate"));

	// Everything but the check box
	private final List<Component> pageControls;
	private final List<Component> labels = new ArrayList<>();

	public JPageSpecificationEditor() {
		sizeField.addItem(CUSTOM);
		for (StandardPageSize size : StandardPageSize.values()) {
			sizeField.addItem(new SizeChoice(size));
		}
		ButtonGroup orientation = new ButtonGroup();
		orientation.add(portraitButton);
		orientation.add(landscapeButton);
		portraitButton.setSelected(true);

		JLabel hint = new JLabel(JSeshMessages.getString("pagePrefs.heightHint"));
		hint.setEnabled(false);

		panel.setLayout(new MigLayout());
		PanelBuilder h = new PanelBuilder(panel);
		h.add(enableBox, "span, wrap, gapbottom unrelated");
		h.addWithLabel("pagePrefs.standardSize", sizeField, "gapright unrelated");
		h.addWithLabel("pagePrefs.units", unitField, "wrap");
		h.addLabel("pagePrefs.orientation");
		h.add(portraitButton, "split 2");
		h.add(landscapeButton, "wrap");
		h.addWithLabel("pagePrefs.width", widthField, "w 60pt, gapright unrelated");
		h.addWithLabel("pagePrefs.height", heightField, "w 60pt, wrap");
		h.addWithLabel("pagePrefs.marginLeft", marginLeftField, "w 60pt, gapright unrelated");
		h.addWithLabel("pagePrefs.marginRight", marginRightField, "w 60pt, wrap");
		h.addWithLabel("pagePrefs.marginTop", marginTopField, "w 60pt, gapright unrelated");
		h.addWithLabel("pagePrefs.marginBottom", marginBottomField, "w 60pt, wrap");
		h.add(hint, "span, wrap, gapbottom unrelated");
		h.add(new JSeparator(), "span, growx, wrap");
		h.addWithLabel("pagePrefs.pageNumber", numberPositionField, "span, wrap");
		h.addWithLabel("pagePrefs.pageTitle", titleField, "span 3, growx, wrap");
		h.addWithLabel("pagePrefs.pageTitlePosition", titlePositionField, "span, wrap");
		h.add(alternateBox, "span, wrap");
		ListCellRenderer<? super PageTextPosition> renderer = new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				Object shown = value instanceof PageTextPosition position
						? JSeshMessages.getString("pagePrefs.position." + position.name())
						: value;
				return super.getListCellRendererComponent(list, shown, index, isSelected, cellHasFocus);
			}
		};
		numberPositionField.setRenderer(renderer);
		titlePositionField.setRenderer(renderer);

		pageControls = List.of(sizeField, unitField, portraitButton, landscapeButton,
				widthField, heightField, marginLeftField, marginRightField,
				marginTopField, marginBottomField, hint,
				numberPositionField, titlePositionField, titleField, alternateBox);
		// Labels follow the state of the check box too.
		for (Component c : panel.getComponents()) {
			if (c instanceof JLabel && c != hint) {
				labels.add(c);
			}
		}
	}

	/// Enable or disable all the controls which describe the page.
	///
	/// @param enabled true to enable them.
	public void setPageControlsEnabled(boolean enabled) {
		pageControls.forEach(c -> c.setEnabled(enabled));
		labels.forEach(c -> c.setEnabled(enabled));
	}

	public JPanel getPanel() {
		return panel;
	}

	public JCheckBox getEnableBox() {
		return enableBox;
	}

	public JComboBox<SizeChoice> getSizeField() {
		return sizeField;
	}

	public JRadioButton getPortraitButton() {
		return portraitButton;
	}

	public JRadioButton getLandscapeButton() {
		return landscapeButton;
	}

	public JFormattedTextField getWidthField() {
		return widthField;
	}

	public JFormattedTextField getHeightField() {
		return heightField;
	}

	public JFormattedTextField getMarginLeftField() {
		return marginLeftField;
	}

	public JFormattedTextField getMarginRightField() {
		return marginRightField;
	}

	public JFormattedTextField getMarginTopField() {
		return marginTopField;
	}

	public JFormattedTextField getMarginBottomField() {
		return marginBottomField;
	}

	public JComboBox<PageTextPosition> getNumberPositionField() {
		return numberPositionField;
	}

	public JComboBox<PageTextPosition> getTitlePositionField() {
		return titlePositionField;
	}

	public JCheckBox getAlternateBox() {
		return alternateBox;
	}

	public JTextField getTitleField() {
		return titleField;
	}

	public JComboBox<LengthUnit> getUnitField() {
		return unitField;
	}
}

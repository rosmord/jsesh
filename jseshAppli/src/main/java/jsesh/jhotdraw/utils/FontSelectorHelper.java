/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
package jsesh.jhotdraw.utils;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jsesh.jhotdraw.Messages;
import net.miginfocom.swing.MigLayout;

/**
 * Font selection composite object.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 * 
 */
public class FontSelectorHelper {

	/**
	 * Font property corresponds to the font selected in this object. 
	 */
	public static final String FONT = "Font";
	
	private PropertyChangeSupport propertyChangeSupport= new PropertyChangeSupport(this);
	private Component parent;
	/**
	 * A label to put in front of the font selection. If null, no label is set.
	 */
	private String selectorLabel;
	/**
	 * The font selected by this object.
	 */
	private Font font;

	/**
	 * A non-editable textfield to display the chosen font.
	 */
	private JTextField fontNameDisplayField;

	/**
	 * The button used to open the font selection dialog.
	 */
	private JButton fontSelectButton;

	/**
	 * The combo box for font size.
	 */
	private JComboBox fontSizeBox;

	/**
	 * Create a font selector helper.
	 * 
	 * @param label
	 *            the text to display in front of the font. May be null.
	 */
	public FontSelectorHelper(Component parent, String label) {
		this.parent = parent;
		selectorLabel = label;
		fontNameDisplayField = new JTextField("");
		fontNameDisplayField.setEditable(false);
		fontSelectButton = new JButton(
				Messages.getString("fontPreferences.browseFonts.text"));
		fontSizeBox = new JComboBox();
		animate();
		setFont(new Font("Serif", Font.PLAIN, 12));
	}

	/**
	 * Add behaviour to the objects.
	 */
	private void animate() {
		fontSizeBox.setEditable(true);
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(
				new String[] { "7", "8", "9", "10", "11", "12", "13", "14",
						"15", "16", "18", "20", "22", "24", "32", "48" });
		fontSizeBox.setModel(comboBoxModel);
		final JTextField cbTextField = (JTextField) fontSizeBox.getEditor()
				.getEditorComponent();
		cbTextField.setInputVerifier(new IntegerVerifier());
		fontSizeBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFontSize();
			}
		});
		fontSelectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseFont();
			}
		});
	}

	protected void setFontSize() {
		float fontSize = getFontSizeFromBox();
		if ((int) fontSize != font.getSize()) {
			setFont(font.deriveFont(getFontSizeFromBox()));
		}
	}

	private void chooseFont() {
		SimpleFontDialog fontDialog = new SimpleFontDialog(parent,
				Messages.getString("fontPreferences.browseFonts.text"));
		fontDialog.setFont(font);
		if (fontDialog.showDialog() == JOptionPane.OK_OPTION) {
			float size = getFontSizeFromBox();
			setFont(fontDialog.getSelectedFont().deriveFont(size));
		}
	}

	/**
	 * Use the content of the current font size box to get the font size.
	 * 
	 * @return
	 */
	private float getFontSizeFromBox() {
		String fontSizeString = (String) this.fontSizeBox.getSelectedItem();
		float size = 12;
		if (fontSizeString != null) {
			if (!fontSizeString.matches("^[0-9]+$")) {
				fontSizeString = fontSizeString.replaceAll("[^0-9]", "");
				if ("".equals(fontSizeString))
					fontSizeString = "12";
			}
			size = Float.parseFloat(fontSizeString);
		}
		return size;
	}

	/**
	 * Easy layout of the helper in a more general panel.
	 * <p>
	 * Note that the components are available for individual display too.
	 * 
	 * @param panel
	 *            the panel which will contain the objects.
	 * @param displayContraints
	 *            additional constraints to apply to the font display component.
	 *            Typically something like "sg a" to ensure that all display
	 *            have the same size.
	 *  @param constraints on the button. Typically also something like "sg b".
	 * @param finalConstraints
	 *            the mig constraints to add after the last element (the size
	 *            combobox). Typically "wrap".
	 */
	public void doMigLayout(JPanel panel, String displayContraints, String buttonConstraints,
			String finalConstraints) {
		PanelHelper helper = new PanelHelper(panel);
		if (!(panel.getLayout() instanceof MigLayout))
			throw new RuntimeException(
					"doMigLayout needs a mig layout to be set on the panel");
		if (this.selectorLabel != null) {
			helper.addWithLabel(selectorLabel, fontNameDisplayField,
					displayContraints);
		} else {
			helper.add(fontNameDisplayField, displayContraints);
		}
		helper.add(fontSelectButton, buttonConstraints);
		helper.add(fontSizeBox, finalConstraints);
	}

	public Font getSelectedFont() {
		return font;
	}

	/**
	 * Sets the selected font.
	 * 
	 * @param newFont
	 */
	public void setFont(Font newFont) {
		Font oldFont = font;
		if (oldFont == newFont)
			return;
		else {			
			font = newFont;
			fontNameDisplayField.setFont(font);
			fontNameDisplayField.setText(font.getFontName());
			fontSizeBox.setSelectedItem("" + newFont.getSize());
			propertyChangeSupport.firePropertyChange(FONT, oldFont, newFont);
		}
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * @return
	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
	 */
	public PropertyChangeListener[] getPropertyChangeListeners() {
		return propertyChangeSupport.getPropertyChangeListeners();
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(propertyName,
				listener);
	}

	/**
	 * @param propertyName
	 * @return
	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
	 */
	public PropertyChangeListener[] getPropertyChangeListeners(
			String propertyName) {
		return propertyChangeSupport.getPropertyChangeListeners(propertyName);
	}
	
	
}

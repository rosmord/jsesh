/*
 * This file is distributed under the LGPL
 * Author: Serge Rosmorduc
 */

package jsesh.swing.preferencesEditor;

import javax.swing.JComponent;

import jsesh.mdcDisplayer.clipboard.MDCClipboardPreferences;

/**
 * 
 * @author rosmord
 */
public class ClipboardChooserPresenter extends FormPresenter {

	JClipboardFormatSelector panel = new JClipboardFormatSelector();

	public ClipboardChooserPresenter(String title) {
		super(title);
	}

	public JComponent getPanel() {
		return panel;
	}

	public void updatePreferences(PreferencesFacade facade) {
		MDCClipboardPreferences prefs = new MDCClipboardPreferences()
				.withImageWanted(panel.getBitmapCheckBox().isSelected())
				.withRtfWanted(panel.getRtfCheckBox().isSelected())
				.withPdfWanted(panel.getPdfCheckBox().isSelected())
				.withTextWanted(panel.getPlainTextCheckBox().isSelected());
		facade.setClipboardPreferences(prefs);
	}

	public void loadPreferences(PreferencesFacade facade) {
		panel.getRtfCheckBox().setSelected(
				facade.getClipboardPreferences().isRtfWanted());
		panel.getPdfCheckBox().setSelected(
				facade.getClipboardPreferences().isPdfWanted());
		panel.getBitmapCheckBox().setSelected(
				facade.getClipboardPreferences().isImageWanted());
		panel.getPlainTextCheckBox().setSelected(
				facade.getClipboardPreferences().isTextWanted());

	}

}

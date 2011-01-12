/*
 * This file is distributed under the LGPL
 * Author: Serge Rosmorduc
 */

package jsesh.swing.preferencesEditor;

import javax.swing.JComponent;

/**
 *
 * @author rosmord
 */
public class ClipboardChooserPresenter extends FormPresenter {

    JClipboardFormatSelector panel= new JClipboardFormatSelector();

    public ClipboardChooserPresenter(String title) {
        super(title);
    }

    public JComponent getPanel() {
        return panel;
    }

    public void updatePreferences(PreferencesFacade facade) {
        facade.getClipboardPreferences().setImageWanted(panel.getBitmapCheckBox().isSelected());
        facade.getClipboardPreferences().setRtfWanted(panel.getRtfCheckBox().isSelected());
        facade.getClipboardPreferences().setPdfWanted(panel.getPdfCheckBox().isSelected());
        facade.getClipboardPreferences().setTextWanted(panel.getPlainTextCheckBox().isSelected());
    }

    public void loadPreferences(PreferencesFacade facade) {
        panel.getRtfCheckBox().setSelected(facade.getClipboardPreferences().isRtfWanted());
        panel.getPdfCheckBox().setSelected(facade.getClipboardPreferences().isPdfWanted());
        panel.getBitmapCheckBox().setSelected(facade.getClipboardPreferences().isImageWanted());
        panel.getPlainTextCheckBox().setSelected(facade.getClipboardPreferences().isTextWanted());
        
    }

}

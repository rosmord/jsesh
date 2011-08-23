/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.swing.preferencesEditor;

import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;
import jsesh.swing.utils.FileButtonMapper;

/**
 * @author rosmord
 * 
 */
public class FontChooserPresenter extends FormPresenter {

	FontChooserForm form;

	/**
	 * @param string
	 */
	public FontChooserPresenter(String title) {
		super(title);
		form = new FontChooserForm();

		FileButtonMapper mapper = new FileButtonMapper(form
				.getBrowseHieroglyphs(), form
				.getHieroglyphicFontDirectoryField());

		mapper.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		mapper.setApproveButtonLabel("set directory");
		mapper.setDialogTitle("select glyphs directory");
		//mapper.askForCreation();
		mapper.setMode(FileButtonMapper.SAVE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.swing.preferencesEditor.FormPresenter#getPanel()
	 */
	public JComponent getPanel() {
		return form;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.swing.preferencesEditor.FormPresenter#updatePreferences()
	 */
	public void updatePreferences(PreferencesFacade facade) {
		File f = (File) form.getHieroglyphicFontDirectoryField().getValue();
		DefaultHieroglyphicFontManager.getInstance().setDirectory(f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.swing.preferencesEditor.FormPresenter#loadPreferences(jsesh.swing.preferencesEditor.PreferencesFacade)
	 */
	public void loadPreferences(PreferencesFacade facade) {
		File glyphsPath = DefaultHieroglyphicFontManager.getInstance()
				.getDirectory();
		form.getHieroglyphicFontDirectoryField().setValue(glyphsPath);
	}

}

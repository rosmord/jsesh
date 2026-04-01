package jsesh.swing.signimportdialog;

import java.io.File;

import javax.swing.JPanel;

import jsesh.hieroglyphs.fonts.DefaultHieroglyphicFontManager;
import jsesh.swing.signimportdialog.control.ExternalSignImporterPresenter;
import jsesh.swing.signimportdialog.model.ExternalSignImporterModel;
import jsesh.swing.signimportdialog.ui.ExternalSignImporterUI;


public class ExternalSignImporter {
	
	ExternalSignImporterModel model;
	ExternalSignImporterUI ui;
	ExternalSignImporterPresenter control;
	
	public ExternalSignImporter(DefaultHieroglyphicFontManager hieroglyphicFontManager) {
		model = new ExternalSignImporterModel(hieroglyphicFontManager);
		ui = new ExternalSignImporterUI();
		control= new ExternalSignImporterPresenter(model, ui);
		ui.addEventListener(control);
	}
	
	public JPanel getPanel() {
		return ui.getPanel();
	}

	/* 
	 * gets the directory where the system will look for source files for the glyphs.
	 */
	public File getSourceDirectory() {
		return model.getSourceDirectory();
	}

	/**
	 * sets the directory where the system will look for source files for the glyph to choose.
	 * <p> This is <em>NOT</em> the user's own glyph library, it's a place where the search will start. 
	 * @param sourceDirectory : the  directory where the search will start.
	 */
	public void setSourceDirectory(File sourceDirectory) {
		model.setSourceDirectory(sourceDirectory);
	}
	
}

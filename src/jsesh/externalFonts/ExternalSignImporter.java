package jsesh.externalFonts;

import javax.swing.JFrame;
import javax.swing.JPanel;

import jsesh.externalFonts.control.ExternalSignImporterPresenter;
import jsesh.externalFonts.model.ExternalSignImporterModel;
import jsesh.externalFonts.ui.ExternalSignImporterUI;

public class ExternalSignImporter {
	
	ExternalSignImporterModel model;
	ExternalSignImporterUI ui;
	ExternalSignImporterPresenter control;
	
	public ExternalSignImporter() {
		model = new ExternalSignImporterModel();
		ui = new ExternalSignImporterUI();
		control= new ExternalSignImporterPresenter(model, ui);
		ui.addEventListener(control);
	}
	
	public JPanel getPanel() {
		return ui.getPanel();
	}
}

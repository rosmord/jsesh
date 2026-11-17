package jsesh.externalFonts.control;

import java.io.File;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import jsesh.externalFonts.model.ExternalSignImporterModel;
import jsesh.externalFonts.ui.ExternalSignImporterUI;
import jsesh.externalFonts.ui.UIEventListener;
import jsesh.hieroglyphs.HieroglyphsManager;

/**
 * Presenter for the external font importer.
 * Gets events, update GUI, modify model. 
 * This use the design pattern Model/View/Presenter.
 * @author rosmord
 *
 */

public class ExternalSignImporterPresenter implements UIEventListener {

	ExternalSignImporterModel model;
	ExternalSignImporterUI ui;
	
	public ExternalSignImporterPresenter(ExternalSignImporterModel model, ExternalSignImporterUI ui) {
		this.model = model;
		this.ui = ui;
		refreshUI();
	}

	public void actionPerformed(ActionEvent e) {
		Object src= e.getSource();
		doAction(src);
	}
	
	
	private void doAction(Object src) 
	{	
		boolean signChanged= false;
		if (src == ui.getCloseButton()) {
			
		} else if (src == ui.getCodeField()) {
			
		} else if (src == ui.getImportButton()) {
			importSigns();
			signChanged= true;
		} else if (src== ui.getInsertButton()) {
			model.insertSign(ui.getCodeField().getText());
		} else if (src== ui.getNextButton()) {
			signChanged= true;
			model.nextSign();
		} else if (src== ui.getPreviousButton()) {
			signChanged= true;
			model.previousSign();
		} else if (src== ui.getFlipHorizontallyButton()) {
			model.flipHorizontally();
		} else if (src== ui.getFlipVerticallyButton()) {
			model.flipVertically();
		} else if (src== ui.getFullCadratHeightButton()) {
			model.resizeVerticallyTo(model.getReferenceShape().getBbox().getHeight());
		}
		if (signChanged)
		{
			//ui.getCodeField().setText("");
			ui.getCodeField().setText(model.getDefaultSignCode());
		}
		refreshUI();	
	}
	private void importSigns() {
		JFileChooser fileChooser= new JFileChooser(model.getSourceDirectory());
		
		fileChooser.addChoosableFileFilter(new FileFilter() {

			public boolean accept(File f) {
				return model.fileFormatIsKnown(f);
			}

			public String getDescription() {				
				return model.getKnownFormats();
			}
			
		});
		int answer= fileChooser.showOpenDialog(ui.getPanel());
		if (answer == JFileChooser.APPROVE_OPTION) {
			try {
				File selected= fileChooser.getSelectedFile();
				if (! selected.isDirectory()) {
					model.setSourceDirectory(selected.getParentFile());
					model.loadSigns(selected);
				} else {
					model.setSourceDirectory(selected);
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(ui.getPanel(), "Could not load file " + fileChooser.getSelectedFile().getPath(),"File not loaded",JOptionPane.ERROR_MESSAGE);
			} 
		}		
	}

	/**
	 * Update all elements of the Graphic User Interface from the current model state.
	 *
	 */
	public void refreshUI() {
		// activation
		ui.getNextButton().setEnabled(model.hasNext());
		ui.getPreviousButton().setEnabled(model.hasPrevious());
		ui.getInsertButton().setEnabled(codeFieldIsComplete());
		ui.getFlipHorizontallyButton().setEnabled(model.getShapeChar() != null);
		ui.getFlipVerticallyButton().setEnabled(model.getShapeChar() != null);
		ui.getCodeField().setEnabled(model.getShapeChar() != null);
		ui.getFullCadratHeightButton().setEnabled(model.getShapeChar()!= null);
		// Fill data :
		ui.getImportedSignDisplay().setShape(model.getShapeChar());
		ui.getReferenceSignDisplay().setShape(model.getReferenceShape());		
	}

	private boolean codeFieldIsComplete() {
		String txt= ui.getCodeField().getText();
		return
		(!"".equals(txt) ) && (model.getShapeChar() != null)
			&& HieroglyphsManager.isCorrectBasicMdcCode(txt);
	}

	public void focusGained(FocusEvent e) {				
	}

	public void focusLost(FocusEvent e) {
		doAction(e.getSource());
	}
	
	
	public void resizeVerticallyTo(double y) {
		model.resizeVerticallyTo(y);
		refreshUI();
	}
	
}

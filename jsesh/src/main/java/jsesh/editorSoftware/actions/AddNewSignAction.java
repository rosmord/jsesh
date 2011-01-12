/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editorSoftware.actions;

import java.awt.event.ActionEvent;
import java.io.File;

import jsesh.editor.actionsUtils.PreferenceBoundAction;
import jsesh.editorSoftware.MDCDisplayerAppliWorkflow;
import jsesh.hieroglyphs.DefaultHieroglyphicFontManager;

public class AddNewSignAction extends PreferenceBoundAction {
	MDCDisplayerAppliWorkflow workflow;
	
	public AddNewSignAction(String name, MDCDisplayerAppliWorkflow workflow) {
		super(name);
		this.workflow= workflow;
	}

	public void actionPerformed(ActionEvent e) {
		workflow.importNewSign();
	}

	public void preferencesChanged() {
		File directory = DefaultHieroglyphicFontManager.getInstance()
				.getDirectory();
		setEnabled(directory.isDirectory());
	}
}
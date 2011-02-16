/*
 * Created on 1 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor.actions.edit;

import java.awt.event.ActionEvent;

import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.BundledActionFiller;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actions.AppDefaultFactory;
import jsesh.editor.actions.generic.EditorAction;

/**
 * @author S. Rosmorduc
 *  
 */
@SuppressWarnings("serial")
public class PasteAction extends EditorAction {
	
	public static String ID= "jsesh.edit.paste";
	
	public PasteAction(JMDCEditor editor) {
		super(editor);
		AppDefaults appDefaults= AppDefaultFactory.getAppDefaults();
		BundledActionFiller.initAction(this, appDefaults);
	}

	/**
	 * Init a new PasteAction for a given editor.
	 * <p> The AppDefault is passed as an argument to avoid creating it over and over.
	 * @param editor
	 * @param appDefaults
	 */
	public PasteAction(JMDCEditor editor, AppDefaults appDefaults) {
		super(editor);	
		BundledActionFiller.initAction(this, appDefaults);
	}

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (this.editor.isEditable())
			this.editor.paste();
	}

}
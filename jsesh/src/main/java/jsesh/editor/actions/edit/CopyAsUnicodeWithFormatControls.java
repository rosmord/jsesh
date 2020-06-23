/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.editor.actions.edit;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;

/**
 * @author rosmord
 */
@SuppressWarnings("serial")
public class CopyAsUnicodeWithFormatControls extends EditorAction {

	
	public CopyAsUnicodeWithFormatControls(JMDCEditor editor) {
		super(editor);
	}

        @Override
	public void actionPerformed(ActionEvent e) {
		editor.copyAsUnicodeWithFormatControls();
	}
}

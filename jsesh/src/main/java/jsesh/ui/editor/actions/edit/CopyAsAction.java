/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 * contributor(s) : Serge J. P. Thomas for the fonts
 * serge.rosmorduc@qenherkhopeshef.org
 */

package jsesh.ui.editor.actions.edit;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;

import jsesh.ui.editor.JMDCEditor;
import jsesh.ui.editor.actionsUtils.EditorAction;

/**
 * @author rosmord
 */
@SuppressWarnings("serial")
public class CopyAsAction extends EditorAction {

	
	private DataFlavor dataFlavor;
	
	public CopyAsAction(JMDCEditor editor, DataFlavor dataFlavor) {
		super(editor);
		this.dataFlavor = dataFlavor;
	}

	
	public void actionPerformed(ActionEvent e) {
		editor.copy(new DataFlavor[] {dataFlavor});		
	}

}

/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editor.actions.text;

import java.awt.event.ActionEvent;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;
import jsesh.editorSoftware.MDCDisplayerAppliWorkflow;
import jsesh.editorSoftware.actions.generic.BasicAction;

@SuppressWarnings("serial")
public class SizeAction extends EditorAction {
	
	public SizeAction(JMDCEditor editor) {
		super(editor);
		// TODO Auto-generated constructor stub
	}


	private int size;


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (editor.isEditable())
			editor.getWorkflow().resizeSign(size);
	}
}
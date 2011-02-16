/*
 * Created on 10 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor.actions.move;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actions.generic.EditorAction;

/**
 * @author S. Rosmorduc
 * 
 */
public class GoDownAction extends EditorAction {

	
	public GoDownAction(JMDCEditor editor, String name, Icon icon) {
		super(editor, name, icon);
	}

	public GoDownAction(JMDCEditor editor, String name) {
		super(editor, name);
	}

	public GoDownAction(JMDCEditor editor) {
		super(editor);
	}

	public void actionPerformed(ActionEvent e) {
		if (editor.getDrawingSpecifications().getTextOrientation().isHorizontal())
			editor.getWorkflow().cursorDown();
		else
			editor.getWorkflow().cursorNext();

	}

}

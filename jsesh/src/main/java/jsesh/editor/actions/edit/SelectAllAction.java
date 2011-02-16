package jsesh.editor.actions.edit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.BundledActionFiller;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actions.generic.EditorAction;

@SuppressWarnings("serial")
public class SelectAllAction extends EditorAction {

	public static final String ID= "jsesh.edit.selectAll";
	
	public SelectAllAction(JMDCEditor editor, AppDefaults appDefaults) {
		super(editor);
		BundledActionFiller.initAction(this, appDefaults);
	}
	
	public void actionPerformed(ActionEvent e) {
		editor.getWorkflow().selectAll();
	}
	
	
}

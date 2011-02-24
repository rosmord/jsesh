/**
 * 
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
public class CopyAsAction extends EditorAction {

	
	private DataFlavor dataFlavor;
	
	public CopyAsAction(JMDCEditor editor, String name, DataFlavor dataFlavor) {
		super(editor,name);
		this.dataFlavor = dataFlavor;
	}

	
	public void actionPerformed(ActionEvent e) {
		editor.copy(new DataFlavor[] {dataFlavor});		
	}

}

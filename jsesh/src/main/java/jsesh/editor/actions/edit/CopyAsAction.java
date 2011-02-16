/**
 * 
 */
package jsesh.editor.actions.edit;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actions.generic.EditorAction;

/**
 * @author rosmord
 */
@SuppressWarnings("serial")
public class CopyAsAction extends EditorAction {

	/**
	 * ID for one of the variants of copyAsAction implemented in a JMDCEditor.
	 * To get the corresponding object from a editor, use its actionMap.
	 */
	public static final String COPY_AS_MDC = "COPY_AS_MDC";

	/**
	 * ID for one of the variants of copyAsAction implemented in a JMDCEditor.
	 * To get the corresponding object from a editor, use its actionMap.
	 */

	public static final String COPY_AS_BITMAP = "COPY_AS_BITMAP";

	/**
	 * ID for one of the variants of copyAsAction implemented in a JMDCEditor.
	 * To get the corresponding object from a editor, use its actionMap.
	 */

	public static final String COPY_AS_RTF = "COPY_AS_RTF";

	/**
	 * ID for one of the variants of copyAsAction implemented in a JMDCEditor.
	 * To get the corresponding object from a editor, use its actionMap.
	 */
	
	public static final String COPY_AS_PDF = "COPY_AS_PDF";
	
	private DataFlavor dataFlavor;
	
	public CopyAsAction(JMDCEditor editor, String name, DataFlavor dataFlavor) {
		super(editor,name);
		this.dataFlavor = dataFlavor;
	}

	
	public void actionPerformed(ActionEvent e) {
		editor.copy(new DataFlavor[] {dataFlavor});		
	}

}

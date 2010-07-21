package jsesh.editor.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

import jsesh.editor.JMDCEditor;
import jsesh.swingUtils.ImageIconFactory;

/**
 * Action for shading. Applies on an editor.
 * 
 * @author rosmord
 * 
 */
public class EditorShadeAction extends EditorAction {

	private static final long serialVersionUID = 8899863336401458205L;
	private int shade;

	private static char mnemonicChars [] = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',  'F'
	};
	
	private static int mnemonicCodes [] = {
		KeyEvent.VK_0, 		KeyEvent.VK_1, 		KeyEvent.VK_2,
		KeyEvent.VK_3, 		KeyEvent.VK_4, 		KeyEvent.VK_5,
		KeyEvent.VK_6, 		KeyEvent.VK_7, 		KeyEvent.VK_8,
		KeyEvent.VK_9, 		KeyEvent.VK_A, 		KeyEvent.VK_B,
		KeyEvent.VK_C, 		KeyEvent.VK_D, 		KeyEvent.VK_E,
		KeyEvent.VK_F
	};
	
	
	public EditorShadeAction(JMDCEditor editor, int shade, String mdcLabel) {
		super(editor, ""+ mnemonicChars[shade]+ ". ", ImageIconFactory.buildImage(mdcLabel));
		this.shade = shade;
		this.putValue(EditorShadeAction.MNEMONIC_KEY, new Integer(mnemonicCodes[shade]));
	}

	public void actionPerformed(ActionEvent e) {

		if (editor.isEditable()) {
			editor.getWorkflow().doShade(shade);
		}
	}

}

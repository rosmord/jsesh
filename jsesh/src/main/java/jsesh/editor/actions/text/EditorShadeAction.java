package jsesh.editor.actions.text;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Action;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;
import jsesh.mdc.model.ShadingCode;
import jsesh.swing.ImageIconFactory;

/**
 * Action for shading. Applies on an editor.
 * 
 * @author rosmord
 * 
 */
@SuppressWarnings("serial")
public class EditorShadeAction extends EditorAction {

	public static final String ID = "text.shade_";

	private int shade;

	private static char mnemonicChars[] = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	private static int mnemonicCodes[] = { KeyEvent.VK_0, KeyEvent.VK_1,
			KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5,
			KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9,
			KeyEvent.VK_A, KeyEvent.VK_B, KeyEvent.VK_C, KeyEvent.VK_D,
			KeyEvent.VK_E, KeyEvent.VK_F };

	public EditorShadeAction(JMDCEditor editor, int shade, String mdcLabel) {
		super(editor, "" + mnemonicChars[shade] + ". ", ImageIconFactory
				.buildImage(mdcLabel));
		this.shade = shade;
		this.putValue(EditorShadeAction.MNEMONIC_KEY, new Integer(
				mnemonicCodes[shade]));
	}

	public void actionPerformed(ActionEvent e) {

		if (editor.isEditable()) {
			editor.getWorkflow().doShade(shade);
		}
	}

	/**
	 * Generate an action map for a given editor, giving to each shade action the name "text_shade_N", where N is the 
	 * numeric or-code (between 0 and 15) for the shading (not the MdC code).
	 * @see ShadingCode
	 * @param editor
	 * @return a map of action id, action.
	 */
	public static Map<String,Action> generateActionMap(JMDCEditor editor) {
		TreeMap<String, Action> result = new TreeMap<String, Action>();
		for (int i = 0; i < 16; i++) {
			String iconMdC = "G1"+ ShadingCode.toString("#", i);
			result.put(getActionName(i), new EditorShadeAction(editor, i, iconMdC));
		}
		return result;
	}

	/**
	 * Returns the possible names for shade actions.
	 * @return
	 */
	public static List<String> getActionNames() {
		ArrayList<String> names= new ArrayList<String>();
		for (int i= 0; i < 16; i++) {
			names.add(getActionName(i));
		}
		return names;
	}
	
	private static String getActionName(int i) {
		String id = ID +i;
		return id;
	}
}

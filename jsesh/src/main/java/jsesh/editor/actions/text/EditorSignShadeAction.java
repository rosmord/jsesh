/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
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
import jsesh.swing.utils.ImageIconFactory;

/**
 * Action for shading. Applies on an editor.
 * 
 * @author rosmord
 * 
 */
@SuppressWarnings("serial")
public final class EditorSignShadeAction extends EditorAction {
	public static final String ID = "sign.shade_";

	private final int shade;

	private static final char mnemonicChars [] = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',  'F'
	};
	
	private static final int mnemonicCodes [] = {
		KeyEvent.VK_0, 		KeyEvent.VK_1, 		KeyEvent.VK_2,
		KeyEvent.VK_3, 		KeyEvent.VK_4, 		KeyEvent.VK_5,
		KeyEvent.VK_6, 		KeyEvent.VK_7, 		KeyEvent.VK_8,
		KeyEvent.VK_9, 		KeyEvent.VK_A, 		KeyEvent.VK_B,
		KeyEvent.VK_C, 		KeyEvent.VK_D, 		KeyEvent.VK_E,
		KeyEvent.VK_F
	};
	
	
	public EditorSignShadeAction(JMDCEditor editor, int shade, String mdcLabel) {
		super(editor, ""+ mnemonicChars[shade]+ ". ",
                        ImageIconFactory.getInstance().buildImage(mdcLabel));
		this.shade = shade;
		this.putValue(EditorSignShadeAction.MNEMONIC_KEY, mnemonicCodes[shade]);
	}

        @Override
	public void actionPerformed(ActionEvent e) {
		if (editor.isEditable()) {
			editor.getWorkflow().doShadeSign(shade);
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
			result.put(getActionName(i), new EditorSignShadeAction(editor, i, iconMdC));
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

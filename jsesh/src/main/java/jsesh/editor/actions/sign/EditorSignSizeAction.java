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
package jsesh.editor.actions.sign;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Action;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;

/**
 * Action changing a sign size.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
@SuppressWarnings("serial")
public class EditorSignSizeAction extends EditorAction {

	public static final String ID = "sign.signSize_";

	private int percentage;

	public EditorSignSizeAction(JMDCEditor editor, int percentage) {
		super(editor);
		putValue(NAME, percentage + "%");
		this.percentage= percentage;
	}

	
	public void actionPerformed(ActionEvent e) {
		if (editor.isEditable())
			editor.getWorkflow().resizeSign(percentage);
	}

	
	private static int [] sizes= {240, 200, 144, 120, 100, 70, 50, 35, 25, 1};
	public static final String[] actionNames= new String[sizes.length];
	
	static {
		for (int i= 0; i< sizes.length; i++) {
			actionNames[i]= ID + sizes[i];
		}		
	}
	
	public static Map<String, Action> generateActionMap(JMDCEditor editor) {
		TreeMap<String, Action> map = new TreeMap<String, Action>();
		for (int i = 0; i < sizes.length; i++) {
			map.put(actionNames[i], new EditorSignSizeAction(editor,sizes[i]));
		}
		return map;
	}
}
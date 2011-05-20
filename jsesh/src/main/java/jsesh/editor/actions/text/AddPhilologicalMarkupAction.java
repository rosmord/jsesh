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
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Action;

import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.BundledActionFiller;

import jsesh.editor.JMDCEditor;
import jsesh.editor.actionsUtils.EditorAction;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.swing.ImageIconFactory;

@SuppressWarnings("serial")
public class AddPhilologicalMarkupAction extends EditorAction {

	private int code;

	public AddPhilologicalMarkupAction(JMDCEditor editor, int code) {
		super(editor);
		this.code = code;
	}

	public void actionPerformed(ActionEvent arg0) {
		editor.getWorkflow().addPhilologicalMarkup(code);
	}

	/**
	 * The list of all philology action names.
	 */
	public static final String[] philologyActionNames = {
			"text.addEditorAddition", "text.addErasedSigns",
			"text.addPreviouslyReadable", "text.addScribeAddition",
			"text.addEditorSuperfluous", "text.addMinorAddition",
			"text.addDubious" };

	/**
	 * Generate a list of actions for a specific editor.
	 * 
	 * @param editor
	 * @param appDefaults 
	 * @return
	 */
	public static Map<String, Action> generateActionMap(JMDCEditor editor, AppDefaults appDefaults) {
		int[] codes = { SymbolCodes.EDITORADDITION, SymbolCodes.ERASEDSIGNS,
				SymbolCodes.PREVIOUSLYREADABLE, SymbolCodes.SCRIBEADDITION,
				SymbolCodes.EDITORSUPERFLUOUS, SymbolCodes.MINORADDITION,
				SymbolCodes.DUBIOUS };
		TreeMap<String, Action> map = new TreeMap<String, Action>();
		for (int i = 0; i < codes.length; i++) {
			AddPhilologicalMarkupAction action = new AddPhilologicalMarkupAction(editor, codes[i]);
			map.put(philologyActionNames[i], action);
			BundledActionFiller.initActionProperties(action, philologyActionNames[i],
					appDefaults);
			String mdcText=appDefaults.getString(philologyActionNames[i]+ ".iconMdC");
			if (mdcText!= null)
				action.putValue(Action.SMALL_ICON, ImageIconFactory.buildImage(mdcText));
		}
		return map;
	}
}

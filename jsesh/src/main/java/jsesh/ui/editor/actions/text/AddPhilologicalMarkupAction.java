/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor.actions.text;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.Action;

import jsesh.ui.editor.JMDCEditor;
import jsesh.ui.editor.actionsUtils.EditorAction;
import jsesh.model.constants.SymbolCodes;
import jsesh.render.draw.MDCIconFactory;

import org.qenherkhopeshef.guiFramework.AppDefaults;
import org.qenherkhopeshef.guiFramework.BundledActionFiller;

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

	/// The markup codes (see [SymbolCodes]) matching [#philologyActionNames],
	/// in the same order.
	///
	/// For a markup code `x`, the stand-alone start and end symbols are
	/// `x * 2` and `x * 2 + 1` (see [SymbolCodes]).
	public static final int[] philologyCodes = { SymbolCodes.EDITORADDITION, SymbolCodes.ERASEDSIGNS,
			SymbolCodes.PREVIOUSLYREADABLE, SymbolCodes.SCRIBEADDITION,
			SymbolCodes.EDITORSUPERFLUOUS, SymbolCodes.MINORADDITION,
			SymbolCodes.DUBIOUS };

	/**
	 * Generate a list of actions for a specific editor.
	 * 
	 * @param editor
	 * @param appDefaults 
	 * @return
	 */
	public static Map<String, Action> generateActionMap(JMDCEditor editor, AppDefaults appDefaults, MDCIconFactory iconFactory) {
		int[] codes = philologyCodes;
		TreeMap<String, Action> map = new TreeMap<String, Action>();
		for (int i = 0; i < codes.length; i++) {
			AddPhilologicalMarkupAction action = new AddPhilologicalMarkupAction(editor, codes[i]);
			map.put(philologyActionNames[i], action);
			BundledActionFiller.initActionProperties(action, philologyActionNames[i],
					appDefaults);
			String mdcText=appDefaults.getString(philologyActionNames[i]+ ".iconMdC");
			if (mdcText!= null)
				action.putValue(Action.SMALL_ICON, 
                                        iconFactory.buildImage(mdcText));
		}
		return map;
	}
}

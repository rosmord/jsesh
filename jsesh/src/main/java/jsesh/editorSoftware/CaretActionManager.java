/*
 * Created on 7 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editorSoftware;

import java.util.ArrayList;

import javax.swing.Action;

import jsesh.editor.JMDCEditorWorkflow;
import jsesh.editor.MDCModelEditionAdapter;
import jsesh.editor.caret.MDCCaret;


/**
 * Objects of this class are mediators between carets and actions that work on them. 
 * They enable or disable the actions, depending
 * on the state of the caret.
 * @author S. Rosmorduc
 *
 */
public class CaretActionManager extends MDCModelEditionAdapter {
	private java.util.List<Action> actions;
	private JMDCEditorWorkflow workflow;
	/**
	 * Creates manager which listens and dispatches caret events on an editor. 
	 * @param workflow
	 */
	
	public CaretActionManager(JMDCEditorWorkflow workflow) {
		actions= new ArrayList<Action>();
		this.workflow= workflow;
		workflow.addMDCModelListener(this);
	}
	
	
	public void addAction(Action a) {
		actions.add(a);
		a.setEnabled(workflow.getCaret().hasMark());
	}
	
	public void removeAction(Action a) {
		actions.remove(a);
	}
	
	public void caretChanged(MDCCaret caret) {
		for (int i=0; i< actions.size(); i++) {
			Action a= actions.get(i);
			a.setEnabled(caret.hasMark());
		}
	}
}

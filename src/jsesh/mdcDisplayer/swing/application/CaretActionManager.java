/*
 * Created on 7 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.application;

import java.util.ArrayList;

import javax.swing.Action;

import jsesh.mdcDisplayer.draw.MDCCaret;
import jsesh.mdcDisplayer.swing.editor.JMDCEditorWorkflow;
import jsesh.mdcDisplayer.swing.editor.MDCModelEditionAdapter;


/**
 * Objects of this class are mediators between carets and actions that work on them. 
 * They enable or disable the actions, depending
 * on the state of the caret.
 * @author S. Rosmorduc
 *
 */
public class CaretActionManager extends MDCModelEditionAdapter {
	private java.util.List actions;
	private JMDCEditorWorkflow workflow;
	/**
	 * Creates a caretActionManager for "this", 
	 * listening to caret changes in 
	 * @param workflow
	 */
	
	public CaretActionManager(JMDCEditorWorkflow workflow) {
		actions= new ArrayList();
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
			Action a= (Action) actions.get(i);
			a.setEnabled(caret.hasMark());
		}
	}
}

/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.application.actions.generic;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppliWorkflow;

/**
 * Reflection-using action.
 * calls a specified method of the workflow.
 * This is just because I'm fed up of writing dummy actions.
 * Note that this is not that ugly. Put the actions definitions in a XML file and it would even be trendy.
 * @author rosmord
 */

public class WorkflowMethodAction extends BasicAction {

	private Method method;
	
	
	/**
	 * Create an action that will call a specific method from the workflow.
	 * @param label
	 * @param methodName
	 * @param workflow
	 */
	public WorkflowMethodAction(String label, String methodName, MDCDisplayerAppliWorkflow workflow) {
		this(label, methodName, workflow, false);
	}

	/**
	 * Create an action that will call a specific method from the workflow.
	 * if the action is only active when a selection exists, pass "true" as caretAware
	 * @param label
	 * @param methodName
	 * @param workflow
	 * @param caretAware
	 */
		
	public WorkflowMethodAction(String label, String methodName, MDCDisplayerAppliWorkflow workflow, boolean caretAware) {
		super(label,workflow);
		if (caretAware)
			workflow.registerCaretAwareAction(this);
		Class wfClass= workflow.getClass();
		try {
			method= wfClass.getMethod(methodName, new Class[] {});
		} catch (SecurityException exception) {
			exception.printStackTrace();
		} catch (NoSuchMethodException exception) {
			exception.printStackTrace();
		}
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		try {
			method.invoke(workflow, new Object[] {});
		} catch (IllegalArgumentException exception) {
			exception.printStackTrace();
		} catch (IllegalAccessException exception) {
			exception.printStackTrace();
		} catch (InvocationTargetException exception) {
			exception.printStackTrace();
		}
	}

	

}

/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.editorSoftware.actions.generic;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jsesh.editorSoftware.MDCDisplayerAppliWorkflow;

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
	 * @param workflow
	 * @param label
	 * @param methodName
	 */
	public WorkflowMethodAction(MDCDisplayerAppliWorkflow workflow, String label, String methodName) {
		this(workflow, label, methodName, false);
	}

	/**
	 * Create an action that will call a specific method from the workflow.
	 * if the action is only active when a selection exists, pass "true" as caretAware
	 * @param workflow
	 * @param label
	 * @param methodName
	 * @param caretAware
	 */
		
	public WorkflowMethodAction(MDCDisplayerAppliWorkflow workflow, String label, String methodName, boolean caretAware) {
		super(workflow,label,caretAware);
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

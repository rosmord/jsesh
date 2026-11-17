/**
 * 
 */
package jsesh.mdcDisplayer.swing.application.actions.generic;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppliWorkflow;

/**
 * Actions are forwarded to the current editor.
 * Action class for copy-type actions.
 * Normally, this action is forwarded to the current editor.
 * 
 * <p> Now, this is a rather standard way of interacting with the main editor, and hence we should 
 * probably propose some kind of generic interface here.
 * 
 * @author rosmord
 *
 */
public class ForwardedAction extends AbstractAction {
	/**
	 * 
	 */
	private final MDCDisplayerAppliWorkflow applicationWorkflow;

	public ForwardedAction(MDCDisplayerAppliWorkflow workflow, String name, String command) {
		super(name);
		putValue(ACTION_COMMAND_KEY, command);
		applicationWorkflow = workflow;
	}

	public void actionPerformed(ActionEvent e) {
		ActionEvent newEvent= new ActionEvent(applicationWorkflow.getEditor(), 0, getCommand());
		applicationWorkflow.getEditor().getActionMap().get(getCommand()).actionPerformed(newEvent);
		/*
		applicationWorkflow.getEditor().getWorkflow().copy();
		// TODO : here for test purposes. Move it to the editor ???
		TopItemList top = applicationWorkflow.getEditor().getWorkflow().getSelectionAsTopItemList();
		MDCModelTransferable transferable= new MDCModelTransferable(top);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
		*/
	}

	private String getCommand() {
		return (String)getValue(Action.ACTION_COMMAND_KEY);
	}
}
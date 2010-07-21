package jsesh.mdcDisplayer.swing.application;

import javax.swing.Action;

import jsesh.editor.JMDCEditorWorkflow;
import jsesh.editor.MDCModelEditionAdapter;
import jsesh.mdc.model.operations.ModelOperation;

/**
 * Manages undo and redo action activation. 
 * @author rosmord
 *
 */
public class UndoRedoActionManager {
	private JMDCEditorWorkflow workflow;
	private Action undoAction;
	private Action redoAction;
	
	
	public UndoRedoActionManager(JMDCEditorWorkflow workflow,
			Action undoAction, Action redoAction) {
		super();
		this.workflow = workflow;
		this.undoAction = undoAction;
		this.redoAction = redoAction;
		workflow.addMDCModelListener(myManager);
	}


	private MDCModelEditionAdapter myManager= new MDCModelEditionAdapter() {
		public void textChanged() {
			// A new text has been loaded...
			undoAction.setEnabled(false);
			redoAction.setEnabled(false);
		};
		
		public void textEdited(ModelOperation op) {
			undoAction.setEnabled(workflow.canUndo());
			redoAction.setEnabled(workflow.canRedo());
		};
	};
	
}

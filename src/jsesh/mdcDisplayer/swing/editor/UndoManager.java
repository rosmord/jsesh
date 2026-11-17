/**
 * S. Rosmorduc, 2006.
 */
package jsesh.mdcDisplayer.swing.editor;

import java.util.Stack;

/**
 * Rather standard undo manager for managing commands.
 * @author rosmord
 *
 */
class UndoManager {
	private MDCCommand redoCommand= null;
	private boolean textChanged= false;
	private Stack commands= new Stack();
	
	public void doCommand(MDCCommand newCommand) {
		commands.push(newCommand);
		newCommand.doCommand();
	}
	
	public void undoCommand() {
		redoCommand= (MDCCommand) commands.pop();
		redoCommand.undoCommand();
	}
	
	public void redo() {
		redoCommand.doCommand();
		redoCommand= null;
	}
	
	public boolean canUndo() {
		return ! commands.isEmpty();
	}
	
	public boolean canRedo() {
		return (redoCommand != null);
	}
}
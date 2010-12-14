/**
 * S. Rosmorduc, 2006.
 */
package jsesh.editor;

import java.util.Stack;

import jsesh.editor.command.MDCCommand;

/**
 * Rather standard undo manager for managing commands.
 * 
 * Basic behaviour : 
 * <ul>
 * <li> Infinite undo/redo capability.
 * <li> Some actions will clear the stack, though (opening new text?)
 * <li> As long as there are no "undo" calls, everything is simple. Just push commands.
 * <li> When starting to undo stuff, we shall have two stacks : undo and redo.
 * <li> undo or redo push the command on the "other" stack.
 * <li> we always undo or redo the last command undone or redone.
 * <li> any action different from undo/redo will clear the redo stack.
 * </ul>
 * @author rosmord
 */
class UndoManager {
	
	private boolean textChanged= false;
	private Stack commands= new Stack(); // Stack of MDCCommand
	private Stack undoneCommands= new Stack(); // Stack of MDCCommand
	
	/**
	 * Add a new command to the UndoManager.
	 * @param newCommand
	 */
	public void doCommand(MDCCommand newCommand) {
		undoneCommands.clear();
		commands.push(newCommand);
		newCommand.doCommand();
	}
	
	public void undoCommand() {
		MDCCommand command = (MDCCommand) commands.pop();
		undoneCommands.push(command);
		command.undoCommand();
	}
	
	public void redo() {
		MDCCommand command= (MDCCommand) undoneCommands.pop();
		commands.push(command);
		command.doCommand();
	}
	
	public boolean canUndo() {
		return ! commands.isEmpty();
	}
	
	public boolean canRedo() {
		return (! undoneCommands.isEmpty());
	}

	/**
	 * Clear undo/redo history.
	 */
	public void clear() {
		textChanged= false;
		commands.clear();
		undoneCommands.clear();		
	}
	
	public boolean isClean() {
		return commands.isEmpty();
	}
}
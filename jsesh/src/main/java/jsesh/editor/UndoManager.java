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
	
	//private boolean textChanged= false;
	private Stack<MDCCommand> commands= new Stack<MDCCommand>(); // Stack of MDCCommand
	private Stack<MDCCommand> undoneCommands= new Stack<MDCCommand>(); // Stack of MDCCommand
	
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
		MDCCommand command = commands.pop();
		undoneCommands.push(command);
		command.undoCommand();
	}
	
	public void redo() {
		MDCCommand command= undoneCommands.pop();
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
		//textChanged= false;
		commands.clear();
		undoneCommands.clear();		
	}
	
	public boolean isClean() {
		return commands.isEmpty();
	}
}
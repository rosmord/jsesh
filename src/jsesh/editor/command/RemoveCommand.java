package jsesh.editor.command;

import java.util.List;

import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;

/**
 * Command that deletes one or more cadrats.
 * @author rosmord
 *
 */

class RemoveCommand extends AbstractMDCCommand {
	/**
	 * Where will the deletion take place?
	 */
	private MDCPosition[] range;
	
	
	/**
	 * The deleted elements.
	 */
	private List deletedElements;
	
	/**
	 * The text which will be modified. 
	 */
	private TopItemList topItemList;
	
	/**
	 * Create a command corresponding to the erasure of text between two positions.
	 * Note that the position order is not fixed pos1 may be before or after pos2.
	 * @param topItemList
	 * @param pos1 one of the positions
	 * @param pos2 the other position.
	 * @param firstCommand
	 */
	public RemoveCommand(TopItemList topItemList, MDCPosition pos1, MDCPosition pos2, boolean firstCommand) {
		super(firstCommand);
		this.topItemList= topItemList;
		range = MDCPosition.getOrdereredPositions(pos1, pos2);		
		deletedElements= null;
	}
	
	public void doCommand() {
		deletedElements= topItemList.removeTopItems(range[0].getIndex(), range[1].getIndex());
	}
	
	public void undoCommand() {
		topItemList.addAllAt(range[0].getIndex(), deletedElements);
	}
	
}

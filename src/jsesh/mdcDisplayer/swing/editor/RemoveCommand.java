package jsesh.mdcDisplayer.swing.editor;

import java.util.List;

import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;

/**
 * Command that deletes one or more cadrats.
 * @author rosmord
 *
 */

public class RemoveCommand extends MDCCommand {
	/**
	 * Where will the deletion take place?
	 */
	private MDCPosition position;
	/**
	 * How many elements are deleted.
	 */
	private int size;
	
	/**
	 * The deleted elements.
	 */
	private List deletedElements;
	
	/**
	 * The text which will be modified. 
	 */
	private TopItemList topItemList;
	
	public RemoveCommand(TopItemList topItemList, MDCPosition position, int size, boolean firstCommand) {
		super(firstCommand);
		this.topItemList= topItemList;
		this.position= position;
		this.size= size;
		deletedElements= null;
	}
	
	public void doCommand() {
		deletedElements= topItemList.removeTopItems(position.getIndex(), position.getIndex()+ size);
	}
	
	public void undoCommand() {
		topItemList.addAllAt(position.getIndex(), deletedElements);
	}
	
}

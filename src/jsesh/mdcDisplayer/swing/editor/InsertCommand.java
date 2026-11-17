package jsesh.mdcDisplayer.swing.editor;

import java.util.List;

import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;

/**
 * Command that inserts one or more cadrats.
 * @author rosmord
 *
 */
public class InsertCommand extends MDCCommand {

	/**
	 * The cadrats to add.
	 */
	List newCadrats;
	MDCPosition position;
	TopItemList topItemList;
	
	/**
	 * Create an insertion command.
	 * @param topItemList The text which will be modified.
	 * @param newCadrats The cadrats which will be added.
	 * @param position The position where new text will be inserted.
	 * @param firstCommand Is this command the first one on a fresh text ?
	 */
	
	public InsertCommand(TopItemList topItemList, List newCadrats, MDCPosition position, boolean firstCommand) {
		super(firstCommand);
		this.topItemList= topItemList;
		this.newCadrats= newCadrats;
		this.position= position;
	}

	public void doCommand() {
		topItemList.addAllAt(position.getIndex(),newCadrats);
	}

	public void undoCommand() {
		topItemList.removeTopItems(position.getIndex(), newCadrats.size());
	}

}

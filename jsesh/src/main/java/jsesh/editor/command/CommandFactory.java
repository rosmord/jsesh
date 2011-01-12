package jsesh.editor.command;

import java.util.List;

import jsesh.editor.HieroglyphicTextModel;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;

public class CommandFactory {

	public MDCCommand buildReplaceCommand(
			TopItemList topItemList, List newTopItems,
			MDCPosition pos1, MDCPosition pos2, boolean clean) {
		CompositeCommand command = new CompositeCommand(clean);
		
		MDCPosition[] p = MDCPosition.getOrdereredPositions(pos1, pos2);
		
		MDCCommand removeCommand = buildRemoveCommand(topItemList, pos1, pos2, false);
		
		MDCCommand insertCommand = buildInsertCommand(topItemList, newTopItems, p[0], false); 
		
		command.addCommand(removeCommand);
		command.addCommand(insertCommand);
		return command;
	}

	public MDCCommand buildInsertCommand(TopItemList model,	List newCadrats, MDCPosition position, boolean firstCommand) {
		return new InsertCommand(model, newCadrats, position,
				firstCommand);
	}

	public MDCCommand buildRemoveCommand(
			TopItemList model , MDCPosition pos1,
			MDCPosition pos2, boolean clean) {
		return new RemoveCommand(model, pos1, pos2,
				clean);

	}
	
	
}

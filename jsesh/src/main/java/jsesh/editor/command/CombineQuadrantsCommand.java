package jsesh.editor.command;

import java.util.List;

import javax.swing.text.Position;

import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.model.utilities.CadratStarInserter;

/**
 * Edit for grouping the last two quadrants.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 * @deprecated 
 */
public class CombineQuadrantsCommand extends AbstractMDCCommand {

	private char separator;
	private MDCPosition pos;
	private TopItemList topItemList;
	
	protected CombineQuadrantsCommand(boolean firstCommand, TopItemList topItemList, char separator, 	MDCPosition pos) {
		super(firstCommand);
		this.pos=pos;		
		this.separator= separator;
		this.topItemList= topItemList;
		switch (separator) {
		case ' ':
		case '-':
			// No - Op !
			break;
		case '*':
			if (pos.getIndex() > 1)
				insertLastCadratIntoBeforeLast();
			break;
		case ':':
			if (pos.getIndex() > 1)
				groupLastTwoVertically();
			break;
		case '&':
			if (pos.getIndex() > 1)
				ligatureLastTwoElements();
			break;
		default: // Should not happen.
			throw new RuntimeException("bad separator " + separator);
		}
	}

	// UNDO/REDO
	private void insertLastCadratIntoBeforeLast() {
		CadratStarInserter f = new CadratStarInserter();
		List elts;
		MDCPosition pos1 = pos.getPreviousPosition(2);
		MDCPosition pos2 = pos;
		elts= topItemList.getTopItemListBetween(pos1.getIndex(), pos2.getIndex());
		//elts = hieroglyphicTextModel.getTopItemsBetween(pos1, pos2);
		Cadrat c = f.buildCadrat((TopItem) elts.get(0), (TopItem) elts.get(1));
		if (c != null) {
			
			hieroglyphicTextModel.replaceElement(pos1, pos2, c);
			clearMark();
			clearSeparator();
		}
	}
	@Override
	public void doCommand() {
		// WRITE Auto-generated method stub
		if (true) throw new RuntimeException("WRITE ME");
		
	}

	@Override
	public void undoCommand() {
		// WRITE Auto-generated method stub
		if (true) throw new RuntimeException("WRITE ME");
		
	}

}

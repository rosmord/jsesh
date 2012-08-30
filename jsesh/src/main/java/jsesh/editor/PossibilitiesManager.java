package jsesh.editor;

import java.util.ArrayList;
import java.util.List;

import jsesh.hieroglyphs.CompositeHieroglyphsManager;
import jsesh.hieroglyphs.GardinerCode;
import jsesh.hieroglyphs.PossibilitiesList;
import jsesh.hieroglyphs.SignDescriptionConstants;
import jsesh.hieroglyphs.PossibilitiesList.Possibility;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;

/**
 * Manages the choice of the text to insert for a given entry.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
class PossibilitiesManager {
	
	char separator= ' ';
	
	PossibilitiesList possibilities = null;
	
	/**
	 * The item in front of the new element (which will be combined with it if needed).
	 */
	TopItem previousItem= null;
	
	/**
	 * Must we undo the previous operation in order to insert the next possibility ?
	 * 
	 */
	boolean mustUndo= false;
	
	HieroglyphicTextModel hieroglyphicTextModel;
	
	public void clear() {
		possibilities = null;
	}

	public void init(String code) {
		// See if we have a Gardiner code or a translitteration.
		if (GardinerCode.isCorrectGardinerCode(code.toString())) {
			possibilities = CompositeHieroglyphsManager.getInstance()
					.getSuitableSignsForCode(code);
		} else {
			possibilities = CompositeHieroglyphsManager.getInstance()
					.getPossibilityFor(code, SignDescriptionConstants.KEYBOARD);
		}
		if (possibilities == null || possibilities.isEmpty()) {
			possibilities = new PossibilitiesList(code);
			possibilities.add(code);
		}
		//return possibilities.getCurrentSign();
	}

	private void insertSign() {
		if (possibilities.isSingleSign()) {
			addSign(possibilities.getCode());
		} else {			
			List<TopItem> eltsList = getPossibility().getTopItemList();
	
			hieroglyphicTextModel.insertElementsAt(caret.getInsertPosition(),
					eltsList);
		}
	}
	
	
	public boolean hasPossibilities() {
		// WRITE Auto-generated method stub
		if (true) throw new RuntimeException("WRITE ME");
		return false;
		
	}

	public Possibility getPossibility() {
		return possibilities.getCurrentSign();
	}
	/**
	 * Insert the next possibility in this text.
	 */
	
	public void next() {
		mustUndo= true;
		possibilities.next();
	}

	public char getSeparator() {
		return separator;
	}
}

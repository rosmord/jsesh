package jsesh.editor;

import java.util.List;
import jsesh.hieroglyphs.CompositeHieroglyphsManager;
import jsesh.hieroglyphs.GardinerCode;
import jsesh.hieroglyphs.PossibilitiesList;
import jsesh.hieroglyphs.PossibilitiesList.Possibility;
import jsesh.hieroglyphs.SignDescriptionConstants;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItem;

/**
 * Manages the choice of the text to insert at a given point.
 * 
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 */
class PossibilitiesManager {
	
	private char separator= ' ';
	
	private PossibilitiesList possibilities = null;
	
	/**
	 * The item in front of the new element (which will be combined with it if needed).
	 */
	private TopItem previousItem= null;
	
	/**
	 * Must we undo the previous operation in order to insert the next possibility ? 
         * (deprecated now ?)
	 */
	private boolean mustUndo= false;
	
	private HieroglyphicTextModel hieroglyphicTextModel;
        
        private MDCPosition insertPosition;
	
	public void clear() {
		possibilities = null;
                
                separator= ' ';
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

        // Back to workflow. First let's do something which works. 
        // No refactoring while changing stuff
        // (besides, this class is supposed to stay simple).
//	private void insertSign() {
//		if (getPossibility().isSingleSign()) {
//                    hieroglyphicTextModel.insertElementAt(insertPosition, sign);
//			addSign(possi.getCode());
//		} else {			
//			List<TopItem> eltsList = getPossibility().getTopItemList();
//	
//			hieroglyphicTextModel.insertElementsAt(caret.getInsertPosition(),
//					eltsList);
//		}
//	}
	
	
	public boolean hasPossibilities() {
		return possibilities!= null && ! possibilities.isEmpty();
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
                // remove the text span for the previous insert
                // 
                
	}

	public char getSeparator() {
		return separator;
	}
}

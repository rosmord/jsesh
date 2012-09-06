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
package jsesh.editor;

import jsesh.hieroglyphs.CompositeHieroglyphsManager;
import jsesh.hieroglyphs.GardinerCode;
import jsesh.hieroglyphs.PossibilitiesList;
import jsesh.hieroglyphs.PossibilitiesList.Possibility;
import jsesh.hieroglyphs.SignDescriptionConstants;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItem;

/**
 * Manages the choice of the text to insert at a given point in a given editor.
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
	}

	public char getSeparator() {
		return separator;
	}
}

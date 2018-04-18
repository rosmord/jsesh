/*
 * Created on 14 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.utils;

import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElementDeepAdapter;
import jsesh.mdc.model.TopItem;

/**
 * Knows how to find  the last hieroglyph in a top item.
 * @author S. Rosmorduc
 *
 */
public class LastHieroglyphSelector {

	/**
	 * Returns the last hieroglyph in a TopItem, or null if none.
	 * @param t a top item
	 * @return the last hieroglyph, or null if there is none.
	 */
	
	public Hieroglyph findLastHieroglyph(TopItem t) {
		FindLastAux aux1= new FindLastAux();
		t.accept(aux1);
		return aux1.last;
	}

	// Finds the last hieroglyph.
	class FindLastAux extends ModelElementDeepAdapter {
		Hieroglyph last= null;
		
		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
		 */
		public void visitHieroglyph(Hieroglyph h) {
			last= h;
		}
	}
}

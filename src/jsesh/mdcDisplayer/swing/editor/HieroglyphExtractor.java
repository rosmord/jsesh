/*
 * Created on 13 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.swing.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementDeepAdapter;

/**
 * This expert is able to extract all hieroglyphs from a list of TopItems. 
 * 
 * @author S. Rosmorduc
 *
 */
public class HieroglyphExtractor {

	/**
	 * @param elts
	 * @return
	 */
	public List extractHieroglyphs(List elts) {
		HieroglyphExtractorAux aux= new HieroglyphExtractorAux();
		for (Iterator it= elts.iterator(); it.hasNext();) {
			ModelElement e= (ModelElement) it.next();
			e.accept(aux);
		}
		return aux.result;
	}
	
	/**
	 * Visits the hierarchy, adds the hieroglyphs to result.
	 * @author S. Rosmorduc
	 *
	 */
	private class HieroglyphExtractorAux extends ModelElementDeepAdapter {
		List result= null;
		
		public HieroglyphExtractorAux() {
			result= new ArrayList();
		}
		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
		 */
		public void visitHieroglyph(Hieroglyph h) {
			result.add(h);
		}
	}

}

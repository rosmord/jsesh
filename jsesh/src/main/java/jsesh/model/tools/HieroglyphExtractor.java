/*
 * Created on 13 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.model.tools;

import java.util.ArrayList;
import java.util.List;

import jsesh.model.Hieroglyph;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementDeepAdapter;

/**
 * This expert is able to extract all hieroglyphs from a list of TopItems.
 *
 * @author S. Rosmorduc
 *
 */
public class HieroglyphExtractor {


    /**
     * Extract hieroglyphs from a list.
     *
     * @param elts
     * @return the hieroglyphs in elts.
     */
    public List<Hieroglyph> extractHieroglyphs(List<? extends ModelElement> elts) {
        HieroglyphExtractorAux aux = new HieroglyphExtractorAux();
        for (ModelElement e : elts) {
            e.accept(aux);
        }
        return aux.result;
    }

    /**
     * Visits the hierarchy, adds the hieroglyphs to result.
     *
     * @author S. Rosmorduc
     *
     */
    private class HieroglyphExtractorAux extends ModelElementDeepAdapter {

        List<Hieroglyph> result = null;

        public HieroglyphExtractorAux() {
            result = new ArrayList<>();
        }

        /* (non-Javadoc)
		 * @see jsesh.model.ModelElementDeepAdapter#visitHieroglyph(jsesh.model.Hieroglyph)
         */
        @Override
        public void visitHieroglyph(Hieroglyph h) {
            result.add(h);
        }
    }

}

/*
 * Created on 13 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.model.utilities;

import java.util.ArrayList;
import java.util.List;
import jsesh.hieroglyphs.CompositeHieroglyphsManager;
import jsesh.hieroglyphs.HieroglyphDatabaseInterface;

import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementDeepAdapter;

/**
 * This expert is able to extract all hieroglyphs codes from a list of TopItems.
 *
 * @author S. Rosmorduc
 *
 */
public class HieroglyphCodesExtractor {

    private final HieroglyphDatabaseInterface mdcInfo;
    private final boolean normalise;

    /**
     * Build the extractor.
     * If needed, replaces phonetic codes by Gardiner codes.
     * @param normalise should we normalise the codes toward Gardiner codes ?
     */
    public HieroglyphCodesExtractor(boolean normalise) {
        mdcInfo= CompositeHieroglyphsManager.getInstance();
        this.normalise = normalise;
    }
    
    
    /**
     * Extract hieroglyphs from a list.
     *
     * @param elts
     * @return the codes of the signs in elts.
     */
    public List<String> extractHieroglyphs(List<? extends ModelElement> elts) {
        HieroglyphExtractorAux aux = new HieroglyphExtractorAux();
        elts.forEach((e) -> {
            e.accept(aux);
        });
        return aux.result;
    }

    /**
     * Visits the hierarchy, adds the hieroglyphs to result.
     *
     * @author S. Rosmorduc
     *
     */
    private class HieroglyphExtractorAux extends ModelElementDeepAdapter {

        List<String> result;

        public HieroglyphExtractorAux() {
            result = new ArrayList<>();
        }

        /* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementDeepAdapter#visitHieroglyph(jsesh.mdc.model.Hieroglyph)
         */
        @Override
        public void visitHieroglyph(Hieroglyph h) {
            String code= mdcInfo.getCanonicalCode(h.getCode());
            result.add(code);
        }
    }

}

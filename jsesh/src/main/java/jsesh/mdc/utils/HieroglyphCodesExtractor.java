/*
 * Created on 13 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.utils;

import java.util.ArrayList;
import java.util.List;

import jsesh.glyphs.data.coremdc.ManuelDeCodage;
import jsesh.model.Hieroglyph;
import jsesh.model.LineBreak;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementDeepAdapter;
import jsesh.model.PageBreak;
import jsesh.model.TopItemList;

/**
 * This expert is able to extract all hieroglyphs codes from a list of TopItems.
 * It will avoid non-hieroglyphs, such as ecdotic marks and spaces.
 *
 * @author S. Rosmorduc
 *
 */
public class HieroglyphCodesExtractor {

    private final ManuelDeCodage manuelDeCodage;
    private final boolean normalise;

    /**
     * Build the extractor. If needed, replaces phonetic codes by Gardiner
     * codes.
     *
     * @param normalise should we normalise the codes toward Gardiner codes ?
     */
    public HieroglyphCodesExtractor(boolean normalise) {
        manuelDeCodage = ManuelDeCodage.getInstance();
        this.normalise = normalise;
    }

    /**
     * Extract hieroglyphs from a list of elements.
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
     * Extract hieroglyphs from TopItemList.
     *
     * @param topItemList the elements.
     * @return the codes of the signs in elts.
     */
    public List<String> extractHieroglyphs(TopItemList topItemList) {
        HieroglyphExtractorAux aux = new HieroglyphExtractorAux();
        topItemList.accept(aux);
        return aux.result;
    }

    /**
     * Extract hieroglyphs from a TopItemList, getting line-oriented
     * information.
     *
     * @param topItemList the parsed text
     * @return a list containing, for each line of the text, the list of codes
     * in this line.
     */
    public List<List<String>> extractHieroglyphLines(TopItemList topItemList) {
        HieroglyphExtractorAux aux = new HieroglyphExtractorAux();
        topItemList.accept(aux);
        return aux.lineResult;
    }

    /**
     * Visits the hierarchy, adds the hieroglyphs to result.
     *
     * @author S. Rosmorduc
     *
     */
    private class HieroglyphExtractorAux extends ModelElementDeepAdapter {

        List<String> result;
        List<List<String>> lineResult;

        public HieroglyphExtractorAux() {
            result = new ArrayList<>();
            lineResult = new ArrayList<>();
            lineResult.add(new ArrayList<>());
        }

        /* (non-Javadoc)
		 * @see jsesh.model.ModelElementDeepAdapter#visitHieroglyph(jsesh.model.Hieroglyph)
         */
        @Override
        public void visitHieroglyph(Hieroglyph h) {
            String code;
            if (normalise) {
                code = manuelDeCodage.getCanonicalCode(h.getCode()).code();
            } else {
                code = h.getCode();
            }
            if (code.matches("[a-zA-Z0-9]+")) {
                result.add(code);
                lineResult.get(lineResult.size() - 1).add(code);
            }
        }

        @Override
        public void visitLineBreak(LineBreak b) {
            lineResult.add(new ArrayList<>());
        }

        @Override
        public void visitPageBreak(PageBreak b) {
            lineResult.add(new ArrayList<>());
        }
    }

}

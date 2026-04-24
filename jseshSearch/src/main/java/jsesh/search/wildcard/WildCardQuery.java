/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search.wildcard;

import static org.qenherkhopeshef.finitestate.lazy.RegularLanguageFactory.label;
import static org.qenherkhopeshef.finitestate.lazy.RegularLanguageFactory.maxLength;
import static org.qenherkhopeshef.finitestate.lazy.RegularLanguageFactory.skip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.qenherkhopeshef.finitestate.lazy.LazyLabelIF;
import org.qenherkhopeshef.finitestate.lazy.MatchResult;
import org.qenherkhopeshef.finitestate.lazy.RegularExtractor;
import org.qenherkhopeshef.finitestate.lazy.RegularLanguageIF;

import jsesh.editor.MdCSearchQuery;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.data.VariantTypeForSearches;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.utils.HieroglyphCodesExtractor;
import jsesh.search.backingSupport.HieroglyphOccurrence;
import jsesh.search.backingSupport.OccurrenceStringBuilder;

/**
 * Wildcard implementation.
 *
 * Implementation uses my finiteState library.
 *
 *
 * @author rosmord
 */
public class WildCardQuery implements MdCSearchQuery {

    private RegularExtractor<HieroglyphOccurrence> extractor;

    private final HieroglyphDatabaseInterface hieroglyphDatabase;

    /**
     * Is this query correct or erroneous ?
     */
    private boolean correct;
    
    /**
     * Maximum length of individual results.
     */
    
    int maxLength;

    /**
     * Build a wildcard query from a top item list.
     *
     * @param items items to search
     * @param maxLength maximum length of individual results, 0 meaning "no length limit".
     * @param hieroglyphDatabase sign database, used to find variants.
     * @param variantLevel to what extend sign variants should match.
     */
    public WildCardQuery(TopItemList items, int maxLength, HieroglyphDatabaseInterface hieroglyphDatabase, VariantLevelForSearch variantLevel) {
        this.hieroglyphDatabase = hieroglyphDatabase;
        this.maxLength = maxLength;
        if (items.getNumberOfChildren() == 0) {
            correct = false;
        } else {
            try {
                correct = true;
                extractor = new QueryBuilder().buildQuery(items, variantLevel);
            } catch (IncorrectQueryException e) {
                correct = false;
            }
        }
    }

    private static int extractPosition(List<HieroglyphOccurrence> text, MatchResult m) {
        return text.get(m.getFirstPosition()).getPosition();
    }

    @Override
    public List<MDCPosition> doSearch(TopItemList items) {
        List<MDCPosition> result = Collections.emptyList();
        if (extractor != null) {
            List<HieroglyphOccurrence> text = new OccurrenceStringBuilder().analyzeQuadrat(items);
            result = extractor.search(text).stream()
                    .map(m -> extractPosition(text, m))
                    .map(pos -> new MDCPosition(items, pos))
                    .collect(Collectors.toList());
        }
        return result;
    }

    /**
     * Is this query syntactically correct ?
     *
     * @return
     */
    public boolean isCorrect() {
        return correct;
    }

    private class QueryBuilder {

        List<RegularLanguageIF<HieroglyphOccurrence>> seq;
        List<String> codes;
        int pos = -1;
        private String currentCode;
        private VariantLevelForSearch variantLevel;

        public RegularExtractor<HieroglyphOccurrence> buildQuery(TopItemList items, VariantLevelForSearch variantLevel) {
            this.variantLevel = variantLevel;
            codes = new HieroglyphCodesExtractor(true).extractHieroglyphs(items);
            codes.add(null); // null as sentinel.
            seq = new ArrayList<>();
            parseItems();
            return new RegularExtractor<>(seq);
        }

        private void parseItems() {
            nextPos();
            while (correct && currentCode != null) {
                switch (currentCode) {
                    case WildCardConstants.QUERY_SKIP:
                        processSkip();
                        break;
                    case WildCardConstants.QUERY_SET_BEGIN:
                        processSet();
                        break;
                    case WildCardConstants.QUERY_SET_END:
                        throw new IncorrectQueryException();
                    default:
                        processStandardCode(currentCode);
                        break;
                }
            }
        }

        private void processStandardCode(String code) {
            if (variantLevel == VariantLevelForSearch.EXACT_SEARCH) {
                seq.add(label(occ -> occ.getCode().equals(code)));
            } else {
                // TO MODIFY.. redundant types, in a way (but FULL != EXACT...)
                VariantTypeForSearches variantTypeForSearches = VariantTypeForSearches.UNSPECIFIED;

                Collection<String> variantCodes = hieroglyphDatabase.getTransitiveVariants(code, variantTypeForSearches);
                seq.add(label(new CodeSetLabel(variantCodes)));
            }

            nextPos();
        }

        private void processSkip() {
            if (maxLength == 0)
                seq.add(skip());
            else
                seq.add(maxLength(maxLength));            
            nextPos();
        }

        private void processSet() {
            nextPos(); // skips [...
            HashSet<String> codes = new HashSet<>();
            while (currentCode != null && !currentCode.equals(WildCardConstants.QUERY_SET_END)) {
                codes.add(currentCode);
                nextPos();
            }
            if (currentCode == null) {
                throw new IncorrectQueryException();
            } else {
                seq.add(label(new CodeSetLabel(codes)));
                nextPos();
            }

        }

        private void nextPos() {
            if (pos + 1 < codes.size()) {
                pos++;
                currentCode = codes.get(pos);
            }
        }
    }

    private static class IncorrectQueryException extends RuntimeException {
    }

    private static class CodeSetLabel implements LazyLabelIF<HieroglyphOccurrence> {

        private Set<String> codes;

        public CodeSetLabel(Collection<String> codes) {
            this.codes = new HashSet<>(codes);
        }

        @Override
        public boolean matches(HieroglyphOccurrence t) {
            return codes.contains(t.getCode());
        }

    }
}

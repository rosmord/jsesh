/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search.wildcard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jsesh.editor.MdCSearchQuery;
import jsesh.hieroglyphs.data.HieroglyphDatabaseRepository;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.data.VariantTypeForSearches;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.utils.HieroglyphCodesExtractor;
import jsesh.search.backingSupport.HieroglyphOccurrence;
import jsesh.search.backingSupport.OccurrenceStringBuilder;
import org.qenherkhopeshef.finitestate.lazy.*;
import static org.qenherkhopeshef.finitestate.lazy.RegularLanguageFactory.*;

/**
 * Wildcard implementation.
 *
 * Implementation uses my finiteState library.
 *
 * <p>
 * Note : this class has a hidden dependency on
 * {@link CompositeHieroglyphsManager} We should replace it by a more explicit
 * one through dependency injection at some point.
 *
 * @author rosmord
 */
public class WildCardQuery implements MdCSearchQuery {

    /**
     * Code in the MdC String to introduce the start of a set of searched signs.
     */
    private static final String QUERY_SET_BEGIN = "QUERYSETB";

    /**
     * Code in the MdC String to introduce the end of a set of searched signs.
     */
    private static final String QUERY_SET_END = "QUERYSETE";

    /**
     * Code in the MdC String for a skip (a undefined number of signs, possibly
     * 0).
     */
    private static final String QUERY_SKIP = "QUERYSKIP";

    private RegularExtractor<HieroglyphOccurrence> extractor;
    /**
     * Is this query correct or erroneous ?
     */
    private boolean correct;

    /**
     * Build a wildcard query from a top item list.
     *
     * @param items : items to search
     * @param maxLength : max match length. 0 = any length
     * @param variantLevel
     */
    public WildCardQuery(TopItemList items, int maxLength, VariantLevelForSearch variantLevel) {
        if (items.getNumberOfChildren() == 0) {
            correct = false;
        } else {
            try {
                correct = true;
                extractor = new QueryBuilder().buildQuery(items, maxLength, variantLevel);
            } catch (IncorrectQueryException e) {
                correct = false;
            }
        }
    }

    private static int extractPosition(List<HieroglyphOccurrence> text, List<Integer> match) {
        return text.get(match.get(0)).getPosition();
    }

    @Override
    public List<MDCPosition> doSearch(TopItemList items) {
        List<MDCPosition> result = Collections.emptyList();
        if (extractor != null) {
            List<HieroglyphOccurrence> text = new OccurrenceStringBuilder().analyzeQuadrant(items);
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

        public RegularExtractor<HieroglyphOccurrence> buildQuery(TopItemList items, int maxLength, VariantLevelForSearch variantLevel) {
            this.variantLevel = variantLevel;
            codes = new HieroglyphCodesExtractor(true).extractHieroglyphs(items);
            codes.add(null); // null as sentinel.
            seq = new ArrayList<>();
            parseItems();
            if (maxLength == 0) {
                return new RegularExtractor<>(seq);
            } else {
                return new RegularExtractor<>(maxLength(seq(seq), maxLength));
            }
        }

        private void parseItems() {
            nextPos();
            while (correct && currentCode != null) {
                switch (currentCode) {
                    case QUERY_SKIP:
                        processSkip();
                        break;
                    case QUERY_SET_BEGIN:
                        processSet();
                        break;
                    case QUERY_SET_END:
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
                HieroglyphDatabaseInterface hieroglyphsManager = HieroglyphDatabaseRepository.getHieroglyphDatabase();
                
                Collection<String> variantCodes = hieroglyphsManager.getTransitiveVariants(code, variantTypeForSearches);
                seq.add(label(new CodeSetLabel(variantCodes)));
            }

            nextPos();
        }

        private void processSkip() {
            seq.add(skip());
            nextPos();
        }

        private void processSet() {
            nextPos(); // skips [...
            HashSet<String> codes = new HashSet<>();
            while (currentCode != null && !currentCode.equals(QUERY_SET_END)) {
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

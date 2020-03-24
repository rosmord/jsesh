/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2016) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est un programme informatique servant à mettre en place 
 * une base de données linguistique pour l'égyptien ancien.

 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence [CeCILL|CeCILL-B|CeCILL-C] telle que diffusée par le CEA, le CNRS et l'INRIA 
 * sur le site "http://www.cecill.info".

 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.

 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant 
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à 
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement, 
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité. 

 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez 
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package jsesh.search.wildcard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jsesh.editor.MdCSearchQuery;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.utils.HieroglyphCodesExtractor;
import jsesh.search.backingSupport.HieroglyphOccurrence;
import jsesh.search.backingSupport.OccurrenceStringBuilder;
import org.qenherkhopeshef.finiteState.lazy.*;
import static org.qenherkhopeshef.finiteState.lazy.RegularLanguageFactory.*;
/**
 * Wildcard implementation.
 *
 * Implementation uses my finiteState library.
 * @author rosmord
 */
public class WildCardQuery implements MdCSearchQuery {

    private RegularExtractor<HieroglyphOccurrence> extractor;
    /**
     * Is this query correct or erroneous ?
     */
    private boolean correct;
    private static final String QUERYSETE = "QUERYSETE";

    /**
     * Build a wildcard query from a top item list.
     *
     * @param items : items to search
     * @param maxLength : max match length. 0 = any length
     */
    public WildCardQuery(TopItemList items, int maxLength) {
        if (items.getNumberOfChildren() == 0) {
            correct = false;
        } else {
            try {
                correct = true;
                extractor = new QueryBuilder().buildQuery(items, maxLength);
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
        

        public RegularExtractor<HieroglyphOccurrence> buildQuery(TopItemList items, int maxLength) {            
            codes = new HieroglyphCodesExtractor(true).extractHieroglyphs(items);
            codes.add(null); // null as sentinel.
            seq = new ArrayList<>();
            parseItems();
            if (maxLength == 0)
                return new RegularExtractor<>(seq);
            else {
                return new RegularExtractor<>(maxLength(seq(seq), maxLength));
            }
        }

        private void parseItems() {
            nextPos();
            while (correct && currentCode != null) {
                switch (currentCode) {
                    case "QUERYSKIP":
                        processSkip();
                        break;
                    case "QUERYSETB":
                        processSet();
                        break;
                    case "QUERYSETE":
                        throw new IncorrectQueryException();
                    default:
                        processStandardCode(currentCode);
                        break;
                }
            }
        }

        private void processStandardCode(String code) {   
            seq.add(label(occ -> occ.getCode().equals(code)));
            nextPos();
        }

        private void processSkip() {
            seq.add(skip());
            nextPos();
        }

        private void processSet() {
            nextPos(); // skips [...
            HashSet<String> codes = new HashSet<>();
            while (currentCode != null && !currentCode.equals(QUERYSETE)) {
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

        public CodeSetLabel(Set<String> codes) {
            this.codes = codes;
        }
        
        
        @Override
        public boolean matches(HieroglyphOccurrence t) {
            return codes.contains(t.getCode());
        }
        
    }
}

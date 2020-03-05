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
package jsesh.search;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import jsesh.search.quadrant.QuadrantSearchQuery;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test of searches for Specific quadrants.
 * @author rosmord
 */
public class QuadrantSearcherTest {
    
    private TopItemList parse(String mdc) {
        try {
            return new MDCParserModelGenerator().parse(mdc);
        } catch (MDCSyntaxError ex) {
            throw new RuntimeException(ex);
        }
    }
    
    /**
     * Auxiliary method for writing tests.
     * @param message
     * @param mdc
     * @param codes
     * @param expected 
     */
    private void doSearch(String message, String mdc, String mdcToSearch, Integer... expected) {
        TopItemList text = parse(mdc);
        TopItemList toSearch= parse(mdcToSearch);
        QuadrantSearchQuery searcher = new QuadrantSearchQuery(toSearch);
        List<MDCPosition> actualResult = searcher.doSearch(text);
        List<MDCPosition> expectedResult
                = Arrays.asList(expected).stream()
                        .map(pos -> new MDCPosition(text, pos))
                        .collect(Collectors.toList());
        assertEquals(message, expectedResult, actualResult);
    }

    @Test
    public void testSimpleSearchGardinerCodes() {
        doSearch("Simple search", "i-w-r:a-C1-r-ir:t", "D21", 4);
    }

    @Test
    public void testSearchQuadrant() {
        doSearch("Simple search", "i-w-r:a-C1-r-ir:t", "D21", 4);
    }
    
     @Test
    public void testNone() {
        doSearch("Simple search", "i-w-r:a-C1-Hr:r-ir:t", "D21");
    }
    
    // The following specification is not yet implemented.
    // (case pending).
//    @Test
//    public void testSubQuadrant() {
//        doSearch("testSubQuadrant", 
//                "G40-tA:N23*1-n:km:niwt*t-m-anx-DA-s",
//                "km:niwt*X1", 2
//                );
//    }
}

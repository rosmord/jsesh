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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author rosmord
 */
public class SignStringSearcherTest {

    public SignStringSearcherTest() {
    }

    @Test
    public void testSimpleSearch() {
        TopItemList text = parse("i-w-r:a-C1-r-ir:t");
        List<String> toSearch = Arrays.asList("D21");
        SignStringSearcher searcher = new SignStringSearcher(toSearch);
        List<MDCPosition> result = searcher.doSearch(text);
        List<MDCPosition> expected
                = Arrays.asList(
                        new MDCPosition(text, 2),
                        new MDCPosition(text, 4)
                );
        assertEquals("Simple search", expected, result);
    }

    @Test
    public void testInQuadrant() {
        TopItemList text = parse("i-n:n:n:k:w-m");
        List<String> toSearch = Arrays.asList("n", "n", "k");
        SignStringSearcher searcher = new SignStringSearcher(toSearch);
        List<MDCPosition> result = searcher.doSearch(text);
        List<MDCPosition> expected
                = Arrays.asList(
                        new MDCPosition(text, 1)
                );
        assertEquals("testInQuadrant", expected, result);
    }

    @Test
    public void testLast() {
        TopItemList text = parse("i-w-r:a-C1-r-ir:t");
        List<String> toSearch = Arrays.asList("t");
        SignStringSearcher searcher = new SignStringSearcher(toSearch);
        List<MDCPosition> result = searcher.doSearch(text);
        List<MDCPosition> expected
                = Arrays.asList(
                        new MDCPosition(text, 5)
                );
        assertEquals("last", expected, result);
    }

    @Test
    public void testFirst() {
        TopItemList text = parse("i-w-r:a-C1-r-ir:t");
        List<String> toSearch = Arrays.asList("i");
        SignStringSearcher searcher = new SignStringSearcher(toSearch);
        List<MDCPosition> result = searcher.doSearch(text);
        List<MDCPosition> expected
                = Arrays.asList(
                        new MDCPosition(text, 0)
                );
        assertEquals("testFirst", expected, result);
    }

    @Test
    public void testSecond() {
        TopItemList text = parse("i-w-r:a-C1-r-ir:t");
        List<String> toSearch = Arrays.asList("w", "r");
        SignStringSearcher searcher = new SignStringSearcher(toSearch);
        List<MDCPosition> result = searcher.doSearch(text);
        List<MDCPosition> expected
                = Arrays.asList(
                        new MDCPosition(text, 1)
                );
        assertEquals("testSecond", expected, result);
    }

    @Test
    public void testPartiel() {
        TopItemList text = parse("i-w-r:a-C1-r-ir:t");
        List<String> toSearch = Arrays.asList("w", "b");
        SignStringSearcher searcher = new SignStringSearcher(toSearch);
        List<MDCPosition> result = searcher.doSearch(text);
        List<MDCPosition> expected
                = Arrays.asList();
        assertEquals("testPartiel", expected, result);
    }

    @Test
    public void testSeq() {
        TopItemList text = parse("i-w-r:a-C1-r-ir:t");
        List<String> toSearch = Arrays.asList("r", "ir");
        SignStringSearcher searcher = new SignStringSearcher(toSearch);
        List<MDCPosition> result = searcher.doSearch(text);
        List<MDCPosition> expected
                = Arrays.asList(
                        new MDCPosition(text, 4)
                );
        assertEquals("testSeq", expected, result);
    }

    @Test
    public void testLongSeq() {
        TopItemList text = parse("i-w-r:a#12-C1-r-ir:t");
        List<String> toSearch = Arrays.asList("r", "a", "C1", "r", "ir");
        SignStringSearcher searcher = new SignStringSearcher(toSearch);
        List<MDCPosition> result = searcher.doSearch(text);
        List<MDCPosition> expected
                = Arrays.asList(
                        new MDCPosition(text, 2)
                );
        assertEquals("testLongSeq", expected, result);
    }

    @Test
    public void testNo() {
        TopItemList text = parse("i-w-r:a-C1-r-ir:t");
        List<String> toSearch = Arrays.asList("r", "C1");
        SignStringSearcher searcher = new SignStringSearcher(toSearch);
        List<MDCPosition> result = searcher.doSearch(text);
        List<MDCPosition> expected
                = Arrays.asList();
        assertEquals("testNo", expected, result);
    }

    private TopItemList parse(String mdc) {
        try {
            return new MDCParserModelGenerator().parse(mdc);
        } catch (MDCSyntaxError ex) {
            throw new RuntimeException(ex);
        }
    }
}

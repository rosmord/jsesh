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
package jsesh.mdc.model;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.operations.ModelOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Compare MdC constructs ignoring their ids.
 *
 * @author rosmord
 */
public class compareIgnoreIdTest {

    private TopItemList build(String mdc) {
        MDCParserModelGenerator parser = new MDCParserModelGenerator();
        try {
            return parser.parse(mdc);
        } catch (MDCSyntaxError ex) {
            throw new RuntimeException(ex);
        }
    }

    public void doCompare(String mdc1, String mdc2, boolean expected) {
        TopItemList top1 = build(mdc1);
        TopItemList top2 = build(mdc2);
        if (expected) {
            String text = mdc1 + " ~= " + mdc2;
            assertTrue(text, top1.equalsIgnoreId(top2));
        } else {
            String text = mdc1 + " =/= " + mdc2;
            assertFalse(text, top1.equalsIgnoreId(top2));
        }

    }

    @Test
    public void testVide() {
        doCompare("", "", true);
    }

    @Test
    public void testAetVide() {
        doCompare("p*t:pt", "", false);
    }

    @Test
    public void testVideEtA() {
        doCompare("", "p*t:pt", false);
    }

    @Test
    public void testSimple() {
        doCompare("i-w-r:a-m-pt:p*t", "i-w-r:a-m-pt:p*t", true);
    }

    @Test
    public void testSimpleID() {
        doCompare("i-w-r:a-m-pt\\id3:p*t", "i-w-r:a-m-pt\\id1:p*t\\id2", true);
    }

    @Test
    public void testSimpleDiff() {
        doCompare("m", "n", false);
    }

    @Test
    public void testSimpleEquals() {
        doCompare("m", "m", true);
    }

    @Test
    public void testDiffWithProps() {
        doCompare("m", "m\\r50", false);
    }

    @Test
    public void testEqualsWithProps() {
        doCompare("m\\r50", "m\\r50", true);
    }

    @Test
    public void testEqualsIgnoreOrder() {
        doCompare("m\\col50\\det", "m\\det\\col50", true);
    }

}

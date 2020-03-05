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
package jsesh.search.quadrant;

import jsesh.editor.MdCSearchQuery;
import java.util.ArrayList;
import java.util.List;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.utils.MDCNormalizer;

/**
 * Search for the exact appearance of a given quadrant.
 * @author rosmord
 */
public class QuadrantSearchQuery implements MdCSearchQuery {
    private final TopItemList search;

    public QuadrantSearchQuery(TopItemList search) {        
        this.search =search.deepCopy();
        MDCNormalizer normalizer= new MDCNormalizer();
        normalizer.normalize(this.search);
    }

    /**
     * Perform the search.
     *
     * <p>
     * Basically, we have a tree and we want to perform a sequential search on
     * it. The easiest way to do this is to linearize the entry.
     *
     * @param items the items to search into.
     * @return
     */
    public List<MDCPosition> doSearch(TopItemList items) {
        ArrayList<MDCPosition> result= new ArrayList<>();
        for (int pos= 0; pos < items.getNumberOfChildren(); pos++) {
            // Brute force search. 
            int i;
            for (i=0; i < search.getNumberOfChildren() && i + pos < items.getNumberOfChildren(); i++) {
                if (! search.getChildAt(i).equalsIgnoreId(items.getChildAt(pos+i))) {
                    break;
                }
            }
            if (i == search.getNumberOfChildren())
                result.add(new MDCPosition(items, pos));
        }
        return result;
    }

}

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
import java.util.List;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;

/**
 * Model for a search in JSesh.
 * We will start with a very simple system,
 * but improve it later on. So
 * @author rosmord
 */
public class SearchModel {
    /**
     * are we looking for the exact same quadrants, or just for a list of signs ?
     */
    private boolean exact= false;
    
    private TopItemList searchedItems= new TopItemList();

    public SearchModel() {
    }

    public boolean isExact() {
        return exact;
    }

    public void setExact(boolean exact) {
        this.exact = exact;
    }

    public TopItemList getSearchedItems() {
        return searchedItems;
    }

    public void setSearchedItems(TopItemList searchedItems) {
        this.searchedItems = searchedItems;
    }
    
    /**
     * Performs the search in a text.
     * Uses the Boyer-Moore algorithm for efficiency.
     * Returns the results of the search.
     * @param text 
     * @param startPosition start position for the search.
     * @return  
     */
    public SearchResult doSearch(TopItemList text, MDCPosition startPosition) {
        List<MDCPosition> result= new ArrayList<>();
        return null;
    }
}

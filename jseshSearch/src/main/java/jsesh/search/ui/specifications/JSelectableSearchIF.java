
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search.ui.specifications;

/**
 * Interface for searches UI where the kind of search can be chosen.
 * @author rosmord
 */
public interface JSelectableSearchIF extends JSearchFormModelIF{
    /**
     * What kind of search is currently selected ?
     * @return the current SearchType
     */
    SearchType getSearchType();
    
    /**
     * Select a kind of search.
     * @param searchType 
     */
    void setSearchType(SearchType searchType);
    
    /**
     * Access the hieroglyphic oriented part of the form.
     * @return 
     */
    JMdCSearchFormModelIF getMdcSearchForm();
    
    /**
     * Access the simple text-oriented search.
     * @return 
     */
    JTextSearchFormModelIF getTextSearchForm();
}

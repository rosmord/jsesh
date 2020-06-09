
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search.ui.specifications;

import jsesh.resources.JSeshMessages;

/**
 *
 * @author rosmord
 */
public enum SearchType {
    
    MDC_SEARCH("jsesh.search.tab.label.mdc"),
    SIMPLE_TEXT_SEARCH("jsesh.search.tab.label.simple_text")
    ;
    
    /**
     * Key for label in i18n files.
     */
    private final String key;

    private SearchType(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
       return JSeshMessages.getString(key);
    }
    
    
}

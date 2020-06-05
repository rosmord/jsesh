/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.search.clientApi;

import jsesh.editor.MdCSearchQuery;

/**
 * An object which will hold the seach state and contain the text to search.
 * <p> In some cases, a search page can "control" multiple JSesh windows... 
 * including 0. In this case, no search might be possible in some cases.
 * @author rosmord
 */
public interface SearchTarget {

    /**
     * Can a search be started or continued ?
     * @return 
     */
    public boolean isAvailable();

    /**
     * Starts a search.
     * @param query 
     */
    public void doSearch(MdCSearchQuery query);

    /**
     * Continue the most recent search.
     */
    public void nextSearch();
    
}

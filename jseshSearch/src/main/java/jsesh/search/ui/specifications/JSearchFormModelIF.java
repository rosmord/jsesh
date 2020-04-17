/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.search.ui.specifications;

import jsesh.editor.MdCSearchQuery;

/**
 * Interface for generic search interface.
 * @author rosmord
 */
public interface JSearchFormModelIF {
    
    /**
     * sets the query string.
     * @param mdcQuery 
     */
    void setMdcQuery(String mdcQuery);
    
    /**
     * Gets the query string.
     * @return 
     */
    String getMdcQueryAsText();
    
    // sets the maximum match length
    
    public MdCSearchQuery getQuery();
    
    /**
     * Sets the maximum match length.
     * @param max maximum match length. 0 means "unlimited".
     */
    void setMaxMatchLength(int max);
    
    /**
     * Returns the maximum match length.
     * 0 means unlimited.
     * @return max match length.
     */
    int getMaxMatchLength();
    
    
    /**
     * Do we match the layout of the signs.
     * If yes, no wildcards will be used, and an exact search for
     * a quadrant with the corresponding layout will be done.
     * @param matchLayout 
     */
    void setMatchLayout(boolean matchLayout);
    
    /**
     * Do we match the layout of the signs.
     * @return a boolean
     * @see #setMatchLayout(boolean) 
     */
    boolean isMatchLayout();
    
}

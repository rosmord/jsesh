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
 * Interface for MdC search interface.
 * @author rosmord
 */
public interface JMdCSearchFormModelIF extends JSearchFormModelIF {
    
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

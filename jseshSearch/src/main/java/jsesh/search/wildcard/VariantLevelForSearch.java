
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.search.wildcard;

import jsesh.resources.JSeshMessages;

/**
 * An description of variant handling in searches.
 * 
 * <p> Currently, the data available in the JSesh database for variants
 * is not very good. For most signs, variant status is "unspecified". 
 * @author rosmord
 */
public enum VariantLevelForSearch {
    /**
     * Don't consider any sign variant.
     */
    EXACT_SEARCH, 
    /*
     * not used at that point ?
     * Consider sign variants if they are said to be "full" variants. 
     * For instance, "w" and "W".
     
    FULL_VARIANTS, 
    */ 
    /**
     * Consider any kind of variants (including sometimes sign-look-alike).
     */
    EXTENDED_VARIANTS;
    
    
    @Override
    public String toString() {
       return JSeshMessages.getString("jsesh.search.variant_level."+this.name().toLowerCase());
    }
}

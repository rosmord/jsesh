
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.hieroglyphs.data;

/**
 * Types of variants usable in searches.
 * <p>Doesn't cover all types which are used by the current SignInfo
 * Software, because I feel the data is not consistent enough yet.
 * @author rosmord
 */
public enum VariantTypeForSearches {
    /**
     * Full variants : the signs are equivalent.
     */
    FULL,
    /**
     * No further information is given.
     * <p> We will look a bit further, 
     * but this relation should probably not be transitive.
     * (we will make it transitive in a first attempt, though).
     */
    UNSPECIFIED;
        
    public boolean match(SignVariantType type) {
        if (this == UNSPECIFIED)
            return true;
        else {
            // FULL...
            return type == SignVariantType.FULL;
        }
            
    }
}

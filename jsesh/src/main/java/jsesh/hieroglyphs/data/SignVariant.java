
/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 */
package jsesh.hieroglyphs.data;

import java.util.Comparator;
import java.util.Objects;

/**
 * Information about a possible variant for a sign.
 * @author rosmord
 */
public class SignVariant implements Comparable<SignVariant>{
    private final String code;
    private final SignVariantType type;

    /**
     * Build a variant for a sign.
     * @param code the code of the variant.
     * @param type the relation between the original type and its variant.
     */
    public SignVariant(String code, SignVariantType type) {
        this.code = code;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public SignVariantType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.code);
        hash = 23 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SignVariant other = (SignVariant) obj;
        if (!Objects.equals(this.code, other.code)) {
            return false;
        }
        return this.type == other.type;
    }

    @Override
    public int compareTo(SignVariant o) {        
        int result = GardinerCode.compareCodes(this.code, o.code);
        if (result != 0) return result;
        return this.type.compareTo(o.type);        
    }
}

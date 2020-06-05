
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
 * Types of variants.
 * 
 * let B be a variant of A.
 * <ul>
 * <li> "full" means that all uses of B are also possible uses of A, and all uses of A are uses of B.
 * <li> "other" means that B is more specific than A, or that the degree is unknown
 * <li> "partial" means that the uses of A and B intersect, but they have also both significantly different uses.
 *		For instance, the D36 sign (ayin) is a partial variant of D37 (di), as D36 can write "di". However,
 *		in this case, I would not consider D37 as a variant of D36, because it would cause more harm than good.
 * <li> "no" is used when the sign is not at all a linguistic variant. In this case, isSimilar is normally "y".
 * <li> "unspecified" is used when the sign creator hasn't made a choice for yet.
 * </ul>
 *
 * @author rosmord
 */
public enum SignVariantType {
    /**
     * Possible value for degree of variantOf. Means complete linguistic
     * variant. The sign has the same values as the base sign. In text searches,
     * the variant will be usually considered exactly as the base sign (e.g. Z7
     * and G43).
     */
    FULL,
    /**
     * Possible value for degree of variantOf. Means the sign can be used as a
     * variant of the base sign in some contexts. The sign will be accessible
     * from the variant button in the sign palette.
     */
    PARTIAL,
    /**
     * Possible value for degree of variantOf. Means that in some cases, the
     * sign can be considered as a variant of another one, but that is has also
     * values of its own.
     *
     * Currently won't be accessible as a variant from the palette. But will be
     * usable, <em>on request</em> for searches. e.g. D36 as a variant of D40.
     */
    OTHER,
    /**
     * Use when you don't know the exact value you want to set.
     */
    UNSPECIFIED,
    /**
     * Use when a variant has no linguistic status. It's a 
     */
    NO
    ;

    /**
     * Returns the label for this sign variant, as it will appear in XML files.
     *
     * @return
     */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}

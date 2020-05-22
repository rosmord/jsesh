/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 *
 * Created on 27 sept. 2007, 17:18:09
 */
package jsesh.hieroglyphs.data;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A repository which knows about hieroglyphic codes and signs equivalence. It
 * doesn't deal with sign shapes, which are dealt with by
 * HieroglyphicFontManager.
 *
 * @see CompositeHieroglyphsManager for default implementation.
 * @see HieroglyphicFontManager
 * @author S. Rosmorduc
 *
 */
public interface HieroglyphDatabaseInterface {

    /**
     * Gets the canonical code for a sign. In most cases, this is the Gardiner
     * code (for instance "xpr" gives "L1"). for unknown codes, the function
     * will return its argument as is.
     *
     * @param code
     * @return
     */
    public String getCanonicalCode(java.lang.String code);

    /**
     * Returns all the codes for a given family of signs.
     * <p>
     * If family is the empty string, will return all codes for all families.
     * Note that tksesh user glyph codes won't be listed. (this could count as a
     * bug).
     *
     * @param family : the Gardiner family for this sign (A,B...Aa,Ff)
     * @param userCodes : if true, also add signs which have an user code (USn+
     * Gardiner code)
     * @return all codes for a given sign family as a Collection of String.
     */
    public Collection<String> getCodesForFamily(java.lang.String family, boolean userCodes);

    /**
     * Returns the set of known codes (including phonetic codes).
     *
     * @return
     */
    public Set<String> getCodesSet();

    public String getDescriptionFor(java.lang.String code);

    public List<HieroglyphFamily> getFamilies();

    /**
     * Gets all possibilities for a certain transliteration.
     *
     * @param phoneticValue
     * @param level one of SignDescriptionConstants.KEYBOARD,
     * SignDescriptionConstants.PALETTE or SignDescriptionConstants.INFORMATIVE
     * @see SignDescriptionConstants#KEYBOARD
     * @see SignDescriptionConstants#PALETTE
     * @see SignDescriptionConstants#INFORMATIVE
     *
     * @return
     */
    public jsesh.hieroglyphs.data.PossibilitiesList getPossibilityFor(java.lang.String phoneticValue, String level);

    public Collection<String> getSignsContaining(java.lang.String code);

    /**
     * Returns all the signs which are contained in a larger sign
     *
     * @param code
     * @return
     */
    public Collection<String> getSignsIn(java.lang.String code);

    public Collection<String> getSignsWithTagInFamily(java.lang.String currentTag, java.lang.String familyName);

    public Collection<String> getSignsWithoutTagInFamily(java.lang.String familyName);

    public Collection<String> getTagsForFamily(java.lang.String familyName);

    /**
     * gets declared tags for a particular Gardiner code.
     *
     * @param gardinerCode
     * @return a collection of Strings.
     */
    public Collection<String> getTagsForSign(String gardinerCode);

    public List<String> getValuesFor(java.lang.String gardinerCode);

    /**
     * Gets directly recorded variants for a sign.
     * @param code
     * @return the signs which are recorded as variants for this one.
     */
    public Collection<String> getVariants(java.lang.String code);
    
    /**
     * Gets variants, directly or indirectly.
     * @param code
     * @return 
     */
    // public Collection<String> getAllVariants(java.lang.String code);

    /**
     * Should the sign be always displayed in the palette when its family is
     * shown.
     *
     * @param string
     * @return
     */
    public boolean isAlwaysDisplayed(String string);

    /**
     * Return all codes which start with a given string.
     *
     * @param code
     * @return
     */
    public PossibilitiesList getCodesStartingWith(String code);

    /**
     * Get the all the codes which might match a "generic gardiner code". The
     * code is considered as case insensitive (thus a1 will return A1). The code
     * given is case insensitive. So a1 and A1 will return A1 and variants, for
     * instance. In fact, there are two conceptual layers of signs : Glyphs and
     * Characters, roughly. Characters are described (more or less consistently,
     * see for example Y1 and Y2 as a counter example) by Gardiner code. Those
     * codes might correspond to user defined glyphs (US1A1 for A1, as an
     * example). Notice that this is somehow ad-hoc, as some user codes are
     * really character codes (signs ending in XT, for instance, are supposed to
     * be real new signs, not variants of existing signs, so US1A1XT would be
     * completely different from US2A1XT).
     *
     * Signs indicated as "variants" by their names will also be returned.
     *
     * So this method is not about signs values or equivalence, but mostly an
     * input/output facility.
     *
     * @param string
     * @return
     */
    public PossibilitiesList getSuitableSignsForCode(String code);
    
    
    /**
     * Currently mananaged variant levels.
     * <p> More will come, but we stick to the one which are relatively secure.
     */
    public enum VariantLevel {
        /**
         * Full variants : signs which have exactly the same value.
         */
        FULL, 
        /**
         * Any recorded variant.
         */
        ANY
    }
}

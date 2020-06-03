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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jsesh.hieroglyphs.graphics.HieroglyphicFontManager;

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
    public String getCanonicalCode(String code);

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
    public Collection<String> getCodesForFamily(String family, boolean userCodes);

    /**
     * Returns the set of known codes (including phonetic codes).
     *
     * @return
     */
    public Set<String> getCodesSet();

    public String getDescriptionFor(String code);

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
    public jsesh.hieroglyphs.data.PossibilitiesList getPossibilityFor(String phoneticValue, String level);

    public Collection<String> getSignsContaining(String code);

    /**
     * Returns all the signs which are contained in a larger sign
     *
     * @param code
     * @return
     */
    public Collection<String> getSignsIn(String code);

    public Collection<String> getSignsWithTagInFamily(String currentTag, String familyName);

    public Collection<String> getSignsWithoutTagInFamily(String familyName);

    public Collection<String> getTagsForFamily(String familyName);

    /**
     * gets declared tags for a particular Gardiner code.
     *
     * @param gardinerCode
     * @return a collection of Strings.
     */
    public Collection<String> getTagsForSign(String gardinerCode);

    public List<String> getValuesFor(String gardinerCode);

    /**
     * Gets directly recorded variants for a sign.
     * <p> This will get all variants of the signs, regardless of variant level.
     * <p> this methods is not transitive : variants of variants won't be returned.
     * @param code the code of the original sign
     * @return the signs which are recorded as variants for this one.
     */
    public Collection<SignVariant> getVariants(String code);
    
      /**
     * Gets directly recorded variants for a sign.
     * <p> this methods is not transitive : variants of variants won't be returned.
     * @param code the code of the original sign
     * @param variantTypeForSearches the kind of variants to search for.
     * @return the signs which are recorded as variants for this one.
     */
    public Collection<String> getVariants(String code, VariantTypeForSearches variantTypeForSearches);
    
    /**
     * Gets variants, directly or indirectly.
     * This method will return variants, and variants of variants, etc...
     * @param code the basic sign.
     * @param variantTypeForSearches the level of variants to search for.
     * @return a collection of sign codes.
     */
    default Collection<String> getTransitiveVariants(String code, VariantTypeForSearches variantTypeForSearches) {
        HashSet<String> result= new HashSet<>();
        Deque<String> toProcess = new ArrayDeque<>();
        toProcess.push(code);
        while (! toProcess.isEmpty()) {
            String toExpand = toProcess.pop();
            if (! result.contains(toExpand)) {
                result.add(toExpand);
                for (String variant : getVariants(toExpand, variantTypeForSearches)) {
                    if (! result.contains(variant)) {
                        toProcess.push(variant);
                    }
                }
            }
        }
        return result;    
    }

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
     * Get the all the codes which might match a "generic gardiner code".
     * 
     * <p>The
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
     * @param code the code typed by the user.
     * @return a "possibility list", a navigable set of possible choices.
     */
    public PossibilitiesList getSuitableSignsForCode(String code);
    
}

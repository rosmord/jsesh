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
    String getCanonicalCode(String code);

    /**
     * Returns the codes for a given family of signs.
     * <p>
     * If family is the empty string, will return all codes for all families.
     * <p>
     * Variants for signs can be included or excluded. The definition of a
     * variant is "a sign which is both listed as a variant for another sign,
     * and which is not marked as "basic" sign.
     *
     * <p>
     * <strong>Note:</strong> in a previous version of this method, the boolean
     * argument was used to include or exclude user-made variants of the signs.
     * I don't find this very useful, and I revert to the original version.
     *
     * @param family : the Gardiner family for this sign (A,B...Aa,Ff)
     * @param includeVariants : should we list all signs, including variants.
     * @return all codes for a given sign family as a Collection of String.
     */
    Collection<String> getCodesForFamily(
            String family, boolean includeVariants);

    /**
     * Returns the set of known codes (including phonetic codes).
     *
     * @return
     */
    Set<String> getCodesSet();

    String getDescriptionFor(String code);

    List<HieroglyphFamily> getFamilies();

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
    PossibilitiesList getPossibilityFor(String phoneticValue, String level);

    /**
     * Gets all signs which contain this one in their drawing.
     * <p>
     * E.g. A12 (mšꜥ) contains T9 (pḏ.t).
     *
     * @param code gardiner code of the sign
     * @return all signs which are currently recorded as containing sign "code".
     */
    Collection<String> getSignsContaining(String code);

    /**
     * Returns all the signs which are contained in a larger sign
     * <p>
     * (inverse of {@link #getSignsContaining(java.lang.String)}).
     *
     * @param code
     * @return all signs which are part of this sign.
     */
    Collection<String> getSignsIn(String code);

    Collection<String> getSignsWithTagInFamily(String currentTag, String familyName);

    Collection<String> getSignsWithoutTagInFamily(String familyName);

    Collection<String> getTagsForFamily(String familyName);

    /**
     * gets declared tags for a particular Gardiner code.
     *
     * @param gardinerCode
     * @return a collection of Strings.
     */
    Collection<String> getTagsForSign(String gardinerCode);

    List<String> getValuesFor(String gardinerCode);

    /**
     * Gets directly recorded variants for a sign.
     * <p>
     * This will get all variants of the signs, regardless of variant level.
     * <p>
     * this methods is not transitive : variants of variants won't be returned.
     *
     * @param code the code of the original sign
     * @return the signs which are recorded as variants for this one.
     */
    Collection<SignVariant> getVariants(String code);

    /**
     * Gets directly recorded variants for a sign.
     * <p>
     * this methods is not transitive : variants of variants won't be returned.
     *
     * @param code the code of the original sign
     * @param variantTypeForSearches the kind of variants to search for.
     * @return the signs which are recorded as variants for this one.
     */
    Collection<String> getVariants(String code, VariantTypeForSearches variantTypeForSearches);

    /**
     * Gets variants, directly or indirectly. This method will return variants,
     * and variants of variants, etc...
     *
     * @param code the basic sign.
     * @param variantTypeForSearches the level of variants to search for.
     * @return a collection of sign codes.
     */
    default Collection<String> getTransitiveVariants(String code, VariantTypeForSearches variantTypeForSearches) {
        HashSet<String> result = new HashSet<>();
        Deque<String> toProcess = new ArrayDeque<>();
        toProcess.push(code);
        while (!toProcess.isEmpty()) {
            String toExpand = toProcess.pop();
            if (!result.contains(toExpand)) {
                result.add(toExpand);
                for (String variant : getVariants(toExpand, variantTypeForSearches)) {
                    if (!result.contains(variant)) {
                        toProcess.push(variant);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Should the sign be displayed in lists, as a "main" entry for signs.
     * <p> This method name is not very good. 
     * The idea is that, when hiding variant signs,
     * this sign should be displayed.
     * @param string
     * @return
     */
    boolean isAlwaysDisplayed(String string);

    /**
     * Return all codes which start with a given string.
     *
     * @param code
     * @return
     */
    PossibilitiesList getCodesStartingWith(String code);

    /**
     * Get the all the codes which might match a "generic gardiner code".
     *
     * <p>
     * The code is considered as case insensitive (thus a1 will return A1). The
     * code given is case insensitive. So a1 and A1 will return A1 and variants,
     * for instance. In fact, there are two conceptual layers of signs : Glyphs
     * and Characters, roughly. Characters are described (more or less
     * consistently, see for example Y1 and Y2 as a counter example) by Gardiner
     * code. Those codes might correspond to user defined glyphs (US1A1 for A1,
     * as an example). Notice that this is somehow ad-hoc, as some user codes
     * are really character codes (signs ending in XT, for instance, are
     * supposed to be real new signs, not variants of existing signs, so US1A1XT
     * would be completely different from US2A1XT).
     *
     * Signs indicated as "variants" by their names will also be returned.
     *
     * So this method is not about signs values or equivalence, but mostly an
     * input/output facility.
     *
     * @param code the code typed by the user.
     * @return a "possibility list", a navigable set of possible choices.
     */
    PossibilitiesList getSuitableSignsForCode(String code);

}

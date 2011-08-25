/*
 * NewInterface.java
 * 
 * Created on 27 sept. 2007, 17:18:09
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jsesh.hieroglyphs;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A repository which knows about hieroglyphic codes and signs equivalence.
 * It doesn't deal with sign shapes, which are dealt with by HieroglyphicFontManager.
 * 
 * @see HieroglyphicFontManager
 * @author S. Rosmorduc
 * 
 */
public interface HieroglyphDatabaseInterface {

	/**
	 * Gets the canonical code for a sign.
	 * In most cases, this is the Gardiner code (for instance "xpr" gives "L1").
	 * for unknown codes, the function will return its argument as is.
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
     * @param family :
     *            the Gardiner family for this sign (A,B...Aa,Ff)
     * @param usercodes :
     *            if true, also add signs which have an user code (USn+ Gardiner
     *            code)
     * @return all codes for a given sign family as a Collection of String.
     */
    public Collection<String> getCodesForFamily(java.lang.String family, boolean userCodes);

    /**
     * Returns the set of known codes (including phonetic codes).
     * @return
     */
    public Set<String> getCodesSet();

    public String getDescriptionFor(java.lang.String code);

    public List<HieroglyphFamily> getFamilies();

    /**
     * Gets all possibilities for a certain transliteration.
     * @param phoneticValue
     * @param level one of SignDescriptionConstants.KEYBOARD, SignDescriptionConstants.PALETTE or SignDescriptionConstants.INFORMATIVE 
     * @see SignDescriptionConstants#KEYBOARD
     * @see SignDescriptionConstants#PALETTE
     * @see SignDescriptionConstants#INFORMATIVE
     * 
     * @return
     */
    public jsesh.hieroglyphs.PossibilitiesList getPossibilityFor(java.lang.String phoneticValue, String level);

    public Collection<String> getSignsContaining(java.lang.String code);

    public Collection<String> getSignsWithTagInFamily(java.lang.String currentTag, java.lang.String familyName);

    public Collection<String> getSignsWithoutTagInFamily(java.lang.String familyName);

    public Collection<String> getTagsForFamily(java.lang.String familyName);

    /**
     * gets declared tags for a particular Gardiner code.
     * @param gardinerCode
     * @return a collection of Strings.
     */
    public Collection<String> getTagsForSign(String gardinerCode);

    public List<String> getValuesFor(java.lang.String gardinerCode);

    public Collection<String> getVariants(java.lang.String code);

    /**
     * Should the sign be always displayed in the palette when its family is shown.
     * 
     * @param string
     * @return
     */
	public boolean isAlwaysDisplayed(String string);


	/**
	 * Return all codes which start with a given string.
	 * @param code
	 * @return
	 */
	public PossibilitiesList getCodesStartingWith(String code);


	/**
	 * Get the all the codes which might match a "generic gardiner code".
	 * In fact, there are two conceptual layers of signs : Glyphs and Characters, roughly.
	 * Characters are described (more or less consistently, see for example Y1 and Y2 as a counter example)
	 * by Gardiner code. Those codes might correspond to user defined glyphs (US1A1 for A1, as an example).
	 * Notice that this is somehow ad-hoc, as some user codes are really character codes (signs ending in XT, for
	 * instance, are supposed to be real new signs, not variants of existing signs, so US1A1XT would be completely 
	 * different from US2A1XT).
	 * 
	 * Signs indicated as "variants" by their names will also be returned.
	 * 
	 * So this method is not about signs values or equivalence, but mostly an input/output facility.
	 * @param string
	 * @return
	 */
	public PossibilitiesList getSuitableSignsForCode(String string);
}

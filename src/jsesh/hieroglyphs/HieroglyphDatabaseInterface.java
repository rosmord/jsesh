/*
 * NewInterface.java
 * 
 * Created on 27 sept. 2007, 17:18:09
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jsesh.hieroglyphs;

import java.util.Set;

/**
 * @author rosmord
 */
public interface HieroglyphDatabaseInterface {

    public java.util.List getBasicGardinerCodesForFamily(java.lang.String familyCode);

    public java.lang.String getCanonicalCode(java.lang.String code);

    
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
     * @return all codes for a given sign family.
     */
    public java.lang.String[] getCodesForFamily(java.lang.String family, boolean userCodes);

    public Set getCodesSet();

    public java.lang.String getDescriptionFor(java.lang.String code);

    public java.util.List getFamilies();

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

    public java.util.Collection getSignsContaining(java.lang.String code);

    public java.util.Collection getSignsWithTagInFamily(java.lang.String currentTag, java.lang.String familyName);

    public java.util.Collection getSignsWithoutTagInFamily(java.lang.String familyName);

    public java.util.Collection getTagsForFamily(java.lang.String familyName);

    public java.util.List getValuesFor(java.lang.String gardinerCode);

    public java.util.Collection getVariants(java.lang.String code);

    /**
     * Should the sign be always displayed in the palette when its family is shown.
     * 
     * @param string
     * @return
     */
	public boolean isAlwaysDisplayed(String string);
}

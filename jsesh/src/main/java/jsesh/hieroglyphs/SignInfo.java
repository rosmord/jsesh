package jsesh.hieroglyphs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Information about a sign.
 */

public class SignInfo {
	private String code= "";
	private List translitterationList= new ArrayList(); // of SignTranslitteration
	private Set determinativeValuesSet= new HashSet(); // of MultilingualLabels
	private Set tagSet= new HashSet(); // of tag names...
	private String description= "";
	private Set variants= new HashSet(); // of sign codes (Strings)
	private Set signsContainingThisOne= new HashSet(); // of sign codes (Strings)
	private boolean alwaysDisplayed= false;
                
	public SignInfo(String code) {
		this.code= code;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * returns a list of MultilingualLabels for this sign.
	 * @return
	 */
	
	public Set getDeterminativeValuesSet() {
		return determinativeValuesSet;
	}

    /**
     * Returns the set of tag names.
     * @return a set of String.
     */
	public Set getTagSet() {
		return tagSet;
	}
	
	public Set getSignsContainingThisOne() {
		return signsContainingThisOne;
	}
	
	/**
	 * Returns the list of known SignTranslitteration for this sign.
	 * @return a list of SignTranslitteration
	 */
	public List getTranslitterationList() {
		return translitterationList;
	}
	
	public Set getVariants() {
		return variants;
	}

	public void addTransliteration(SignTransliteration translitteration) {
		getTranslitterationList().add(translitteration);
	}

	public void addVariant(String sign) {
		variants.add(sign);
	}

	public void addSignContainingThisOne(String containingSign) {
		signsContainingThisOne.add(containingSign);
	}

	public void addDeterminativeValue(String category) {
		determinativeValuesSet.add(category);
	}

	public void addTag(String tag) {
		tagSet.add(tag);
	}

	/**
	 * Tells if the sign should always be displayed in the palette, or if it's an extended sign which can be ignored.
	 * @return the alwaysDisplayed
	 */
	public boolean isAlwaysDisplayed() {
		return alwaysDisplayed;
	}

	/**
	 * Tells if the sign should always be displayed in the palette, or if it's an extended sign which can be ignored.
	 * @param alwaysDisplayed the alwaysDisplayed to set
	 */
	public void setAlwaysDisplayed(boolean alwaysDisplayed) {
		this.alwaysDisplayed = alwaysDisplayed;
	}

	
}

package jsesh.hieroglyphs.data;

/**
 * Possible types for sign values
 * @author rosmord
 *
 */
public enum SignValueType {
	//phonogram|ideogram|abbreviation|typical
	
	/**
	 * Phonogram, i.e. sign whose phonetic value is used quite freely. 
	 */
	PHONOGRAM,
	/**
	 * Ideograms, includings so-called phonetic determinatives.
	 */
	IDEOGRAM,
	
	/**
	 * Possible abbreviation values for a sign ; different from ideogram in so far as they are usually written without 'Z1'.
	 * e.g. 'Sri' for G37.
	 */
	ABBREVIATION,
	
	/**
	 * The sign is so often used in this word that we can use it as mnemonic, even if the sign hasn't got this value on its own.
	 * e.g. 'bin' for G37.
	 */
	TYPICAL;
       
    /**
     * Returns the label for this sign value, as it will appear in XML files.
     * @return 
     */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
        
        
}

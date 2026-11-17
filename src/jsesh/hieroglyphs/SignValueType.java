package jsesh.hieroglyphs;

/**
 * Possible types for sign values
 * @author rosmord
 *
 */
public class SignValueType {
	//phonogram|ideogram|abbreviation|typical
	
	/**
	 * Phonogram, i.e. sign whose phonetic value is used quite freely. 
	 */
	public static final String PHONOGRAM= "phonogram";
	/**
	 * Ideograms, includings so-called phonetic determinatives.
	 */
	public static final String IDEOGRAM= "ideogram";
	
	/**
	 * Possible abbreviation values for a sign ; different from ideogram in so far as they are usually written without 'Z1'.
	 * e.g. 'Sri' for G37.
	 */
	public static final String ABBREVIATION= "abbreviation";
	
	/**
	 * The sign is so often used in this word that we can use it as mnemonic, even if the sign hasn't got this value on its own.
	 * e.g. 'bin' for G37.
	 */
	public static final String TYPICAL= "typical";
}

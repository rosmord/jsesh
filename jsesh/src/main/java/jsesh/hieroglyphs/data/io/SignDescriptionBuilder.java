package jsesh.hieroglyphs.data.io;

/**
 * Builder to use along a SignDescriptionReader.
 * Uses the Builder GoF Pattern.
 * @author rosmord
 *
 */
public interface SignDescriptionBuilder {

	void addTransliteration(String currentSign, String transliteration,
			String use, String type);

	void addVariant(String currentSign, String baseSign, String isSimilar, String degree);

	void addPartOf(String currentSign, String baseSign);

	void addDeterminativeValue(String currentSign, String category);

	void addDeterminative(String category, String lang, String label);

	void addTagToSign(String currentSign, String tag);

	/**
	 * Add a new label for a tag.
	 * The tag itself might not exist already, so we may need to create it.
	 * @param tag
	 * @param lang
	 * @param label
	 */
	void addTagLabel(String tag, String lang, String label);
	
	void addSignDescription(String currentSign, String text, String currentLang);

	/**
	 * Add a phantom sign, i.e. a sign which should be replaced by another one.
	 * This can be used for alternative and erroneous encodings.
	 * @param currentSign
	 * @param baseSign
	 * @param existsIn
	 */
	void addPhantom(String currentSign, String baseSign, String existsIn);

	/**
	 * States that this sign should be always displayed in the palette. 
	 * @param currentSign
	 */
	void setSignAlwaysDisplay(String currentSign);

	/**
	 * Declares a new tag.
	 * Names used for display will be declared as tag labels, but the tag name will be used if no label is available.
	 * @param tag
	 */
	void addTagCategory(String tag);

}

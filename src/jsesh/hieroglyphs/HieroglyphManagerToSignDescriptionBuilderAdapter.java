package jsesh.hieroglyphs;

/**
 * Adapter class for reading XML signs descriptions into HieroglyphManager.
 * The model used for storing signs information in XML is richer than the one currently used
 * by JSesh. Hence, we need this adapter.
 * @author rosmord
 *
 */
public class HieroglyphManagerToSignDescriptionBuilderAdapter implements SignDescriptionBuilder {

	private HieroglyphsManager hieroglyphsManager;
	
	
	public HieroglyphManagerToSignDescriptionBuilderAdapter(
			HieroglyphsManager hieroglyphsManager) {
		super();
		this.hieroglyphsManager = hieroglyphsManager;
	}

	public void addDeterminative(String category, String lang, String label) {
		hieroglyphsManager.addDeterminative(category, lang, label);
		
	}

	public void addDeterminativeValue(String sign, String category) {
		hieroglyphsManager.addDeterminativeValue(sign, category);
	}

	public void addPartOf(String sign, String baseSign) {
		hieroglyphsManager.addPartOf(sign, baseSign);
	}

	public void addPhantom(String sign, String baseSign, String existsIn) {
		// TODO : USE THIS INFORMATION
	}

	public void addSignDescription(String sign, String text,
			String currentLang) {
		hieroglyphsManager.addSignDescription(sign, text);
	}

	public void addSimilarTo(String sign, String baseSign) {
		hieroglyphsManager.addVariant(sign, baseSign);
	}

	public void addTagLabel(String tag, String lang, String label) {
		hieroglyphsManager.addTagLabel(tag, lang, label);
	}

	public void addTagToSign(String sign, String tag) {
		hieroglyphsManager.addTagToSign(sign, tag);
	}

	public void addTransliteration(String sign, String transliteration,
			String use, String type) {
		hieroglyphsManager.addTransliteration(sign, transliteration, use, type);
	}

	public void addVariant(String sign, String baseSign,
			String isSimilar, String degree) {
		hieroglyphsManager.addVariant(sign, baseSign);
	}

	public void setSignAlwaysDisplay(String sign) {
		hieroglyphsManager.setSignAlwaysDisplay(sign);
		
	}

	public void addTagCategory(String tag) {
		hieroglyphsManager.addTagCategory(tag);
		
	}

}

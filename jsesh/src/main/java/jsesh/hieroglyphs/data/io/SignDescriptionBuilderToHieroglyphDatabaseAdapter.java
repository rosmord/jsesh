package jsesh.hieroglyphs.data.io;

import jsesh.hieroglyphs.data.SignValueType;
import jsesh.hieroglyphs.data.SignVariantType;
import jsesh.hieroglyphs.data.SimpleHieroglyphDatabase;

/**
 * Adapter class for reading XML signs descriptions into HieroglyphDatabase. The
 * model used for storing signs information in XML is richer than the one
 * currently used by JSesh. Hence, we need this adapter.
 *
 * @author rosmord
 *
 */
public class SignDescriptionBuilderToHieroglyphDatabaseAdapter implements SignDescriptionBuilder {

    private final SimpleHieroglyphDatabase hieroglyphsManager;

    public SignDescriptionBuilderToHieroglyphDatabaseAdapter(
            SimpleHieroglyphDatabase hieroglyphsManager) {
        this.hieroglyphsManager = hieroglyphsManager;
    }

    @Override
    public void addDeterminative(String category, String lang, String label) {
        hieroglyphsManager.addDeterminative(category, lang, label);

    }

    @Override
    public void addDeterminativeValue(String sign, String category) {
        hieroglyphsManager.addDeterminativeValue(sign, category);
    }

    @Override
    public void addPartOf(String sign, String baseSign) {
        hieroglyphsManager.addPartOf(sign, baseSign);
    }

    @Override
    public void addPhantom(String sign, String baseSign, String existsIn) {
        // TODO : USE THIS INFORMATION
    }

    @Override
    public void addSignDescription(String sign, String text,
            String currentLang) {
        hieroglyphsManager.addSignDescription(sign, text);
    }

    @Override
    public void addTagLabel(String tag, String lang, String label) {
        hieroglyphsManager.addTagLabel(tag, lang, label);
    }

    @Override
    public void addTagToSign(String sign, String tag) {
        hieroglyphsManager.addTagToSign(sign, tag);
    }

    @Override
    public void addTransliteration(String sign, String transliteration,
            String use, String type) {
        hieroglyphsManager.addTransliteration(sign, transliteration, use, SignValueType.valueOf(type.toUpperCase()));
    }

    @Override
    public void addVariant(String sign, String baseSign,
            String isSimilar, String degree) {
        hieroglyphsManager.addVariant(sign, baseSign, SignVariantType.valueOf(degree.toUpperCase()));
    }

    @Override
    public void setSignAlwaysDisplay(String sign) {
        hieroglyphsManager.setSignAlwaysDisplay(sign);
    }

    @Override
    public void addTagCategory(String tag) {
        hieroglyphsManager.addTagCategory(tag);

    }

}

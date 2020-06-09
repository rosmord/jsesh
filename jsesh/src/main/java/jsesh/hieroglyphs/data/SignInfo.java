package jsesh.hieroglyphs.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Informations about a sign.
 */
public class SignInfo {

    private String code = "";
    private final List<SignTransliteration> translitterationList = new ArrayList<>(); // of SignTranslitteration
    private final Set<String> determinativeValuesSet = new HashSet<>(); // of MultilingualLabels
    private final Set<String> tagSet = new HashSet<>(); // of tag names...
    private String description = "";
    private final Set<SignVariant> variants = new HashSet<>(); // of sign codes (Strings)
    private final Set<String> signsContainingThisOne = new HashSet<>(); // of sign codes (Strings)
    private boolean alwaysDisplayed = false;
    private boolean isVariant = false;
    private final Set<String> subSigns = new HashSet<>();

    public SignInfo(String code) {
        this.code = code;
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
     *
     * @return
     */
    public Set<String> getDeterminativeValuesSet() {
        return determinativeValuesSet;
    }

    /**
     * Returns the set of tag names.
     *
     * @return a set of String.
     */
    public Set<String> getTagSet() {
        return tagSet;
    }

    public Set<String> getSignsContainingThisOne() {
        return signsContainingThisOne;
    }

    /**
     * Returns the list of known SignTranslitteration for this sign.
     *
     * @return a list of SignTranslitteration
     */
    public List<SignTransliteration> getTranslitterationList() {
        return translitterationList;
    }

    public Set<SignVariant> getVariants() {
        return variants;
    }

    public void addTransliteration(SignTransliteration translitteration) {
        getTranslitterationList().add(translitteration);
    }

    public void addVariant(String variantCode, SignVariantType type) {
        variants.add(new SignVariant(variantCode, type));
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
     * Tells if the sign should always be displayed in the palette.
     *
     * @return the alwaysDisplayed
     */
    public boolean isAlwaysDisplayed() {
        return alwaysDisplayed;
    }

    /**
     * Tells if the sign should always be displayed in the palette, or if it's
     * an extended sign which can be ignored.
     *
     * @param alwaysDisplayed the alwaysDisplayed to set
     */
    public void setAlwaysDisplayed(boolean alwaysDisplayed) {
        this.alwaysDisplayed = alwaysDisplayed;
    }

    public void addSubSign(String sign) {
        subSigns.add(sign);
    }

    public Set<String> getSubSigns() {
        return Collections.unmodifiableSet(subSigns);
    }

    public void markAsVariant(boolean isVariant) {
        this.isVariant = isVariant;
    }

    /**
     * Is this sign a (possibly subordinate) variant of another one.
     * For a sign X, it means that the database for signs has indicated that X is
     * a variant of another sign Y. the relationship is not symetrical.
     * @return true if this sign has been indicated as a variant of another one.
     */
    public boolean isVariant() {
        return isVariant;
    }

}

/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 * 
 * Created on 25 oct. 2004
 */
package jsesh.hieroglyphs.data;

import jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A repository which knows about hieroglyphic codes and signs equivalence. It
 * doesn't deal with sign shapes, which are dealt with by
 * HieroglyphicFontManager.
 *
 * TODO : merge with ManueDeCodage (relatively close meaning).
 *
 * @see HieroglyphicFontManager
 * @author S. Rosmorduc
 *
 */
public class SimpleHieroglyphDatabase implements HieroglyphDatabaseInterface {

    /**
     * Sign code to sign info
     */
    private final HashMap<String, SignInfo> signInfoMap = new HashMap<>();

    /**
     * tag code to tag labels.
     */
    private final HashMap<String, MultiLingualLabel> tagsMap = new HashMap<>();

    /**
     * category code to category labels.
     */
    private final HashMap<String, MultiLingualLabel> determinativeMap = new HashMap<>();

    /**
     * List of available families.
     */
    private ArrayList<HieroglyphFamily> families = null;

    /**
     * Deal with the core manuel de codage, which can't be extended. (will
     * probably be suppressed anyway).
     */
    private ManuelDeCodage basicManuelDeCodageManager = null;
    private final HashMap<String, ArrayList<String>> signsValues;
    private final HashMap<String, PossibilitiesList> possibilitiesLists;

    /**
     * When this boolean is true, all new information comes from the standard
     * sources for JSesh.
     *
     * When false, the data is specific to the current user.
     */
    private boolean inDistributionMode = true;

    public SimpleHieroglyphDatabase(ManuelDeCodage basicManuelDeCodageManager) {
        this.basicManuelDeCodageManager = basicManuelDeCodageManager;
        signsValues = new HashMap<>();
        possibilitiesLists = new HashMap<>();
        fillFamilyList();
    }

    private void fillFamilyList() {
        final String[] familyCodes = {"A", "B", "C", "D", "E", "F", "G", "H",
            "I", "K", "L", "M", "N", "O", "P", "Q", "" + "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z", "Aa", "Ff", "NU", "NL"};

        final String[] familyNames = {"Man and his occupations",
            "Woman and her occupations", "Anthropomorphic Deities",
            "Parts of the human body", "E. Mammals", "Parts of Mammals",
            "Birds", "Parts of Birds",
            "Amphibious animals, reptiles, etc.",
            "Fishes and parts of fishes",
            "Invertebrata and lesser animals", "Trees and plants",
            "Sky, earth, water", "Buildings, parts of buildings, etc.",
            "Ships and parts of ships", "Domestic and funerary furniture",
            "Temple furniture and sacred emblems",
            "Crowns, Dress, Staves, etc.", "Warfare, hunting, butchery",
            "Agriculture, crafts, and professions",
            "Rope, Fibre, baskets, bags, etc.",
            "Vessels of stone and earthenware", "Loaves and cakes",
            "Writings, games, music", "Strokes", "Unclassified (J)",
            "Hieratic signs, Gardiner, JEA 15 (&)", "Upper Egypt Nomes",
            "Lower Egypt Nomes"};

        assert (familyCodes.length == familyNames.length);
        families = new ArrayList<>();
        for (int i = 0; i < familyCodes.length; i++) {
            families.add(new HieroglyphFamily(familyCodes[i], familyNames[i]));
        }
    }

    /**
     * Return all known codes.
     *
     * @return an immutable set of codes.
     */
    @Override
    public Set<String> getCodesSet() {
        // NOTE THAT THE CODES SHOULD NOT BE STORED IN THE FONT MANAGER.
        // THIS IS A TEMPORARY SOLUTION.
        // Well, sort of, as the font manager knows of all the glyphs we
        // have created, and those define signs.
        return DefaultHieroglyphicFontManager.getInstance().getCodes();
    }

    /**
     * Returns the codes for a given family of signs.
     * <p>
     * If family is the empty string, will return all codes for all families.
     * <p>
     * Variants for signd can be included or excluded. The definition of a
     * variant is "a sign which is both listed as a variant for another sign,
     * and which is not marked as "basic" sign.
     *
     * @param family : the Gardiner family for this sign (A,B...Aa,Ff,NL,NU)
     * @param includeVariants : should we list all signs, including variants.
     * @return all codes for a given sign family as a Collection of String.
     */
    @Override
    public Collection<String> getCodesForFamily(String family, boolean includeVariants) {
        // Removed prefix "UG" from tksesh. I think nobody uses it.
        TreeSet<String> l = new TreeSet<>(GardinerCode.getCodeComparator());
        for (String code : getCodesSet()) {
            boolean keepCode;
            // First filter
            if (family.length() > 0) {
                keepCode = code.matches("(US[0-9]+)?" + family + "[0-9]+[a-zA-Z]*");
            } else {
                keepCode = GardinerCode.isCorrectGardinerCode(code); // No specific family needed
            }

            if (keepCode) {
                if (!includeVariants) {
                    keepCode = isAlwaysDisplayed(code);
                }
            }

            if (keepCode) {
                l.add(code);
            }
        }
        return l;
    }

    public void addValue(String gardinerCode, String value) {
        // create a value entry for the sign if necessary
        if (!signsValues.containsKey(gardinerCode)) {
            signsValues.put(gardinerCode, new ArrayList<>());
        }
        getOrCreatePossibilityList(value);
        // Now add the actual data
        signsValues.get(gardinerCode).add(value);
        possibilitiesLists.get(value).addSign(gardinerCode);
    }

    private PossibilitiesList getOrCreatePossibilityList(String value) {
        // If there is no sign list for this value, create one
        if (!possibilitiesLists.containsKey(value)) {
            PossibilitiesList l = new PossibilitiesList(value);
            // Ensure the "official" manuel de codage value comes first.
            if (basicManuelDeCodageManager.isKnownCode(value)) {
                // For readability, we want to have the "official" phonetic code
                // available.
                l.addSign(value);
            }
            possibilitiesLists.put(value, l);
        }
        return possibilitiesLists.get(value);
    }

    /**
     * Returns all possible signs for a given value.
     *
     * @param phoneticValue
     * @return a PossibilitiesList, or null if the value corresponds to nothing.
     * @see HieroglyphDatabaseInterface#getPossibilityFor(String, String)
     */
    @Override
    public PossibilitiesList getPossibilityFor(String phoneticValue,
            String level) {
        PossibilitiesList result;
        // Keyboard access...

        result = new PossibilitiesList(
                getOrCreatePossibilityList(phoneticValue));

        // Palette or informative access is not currently indexed ,
        // so we look at each registered sign. It's still reasonable.
        if (SignDescriptionConstants.PALETTE.equals(level)
                || SignDescriptionConstants.INFORMATIVE.equals(level)) {
            Iterator<SignInfo> signIter = signInfoMap.values().iterator();
            // For each sign...
            while (signIter.hasNext()) {
                SignInfo info = signIter.next();
                // Look at all the known values for the sign...
                for (int i = 0; i < info.getTranslitterationList().size(); i++) {
                    SignTransliteration trl = (SignTransliteration) info
                            .getTranslitterationList().get(i);
                    // If the value is the correct one...
                    if (trl.getTranslitteration().equals(phoneticValue)) {
                        // Depending on its level, add it to the result.
                        if (SignDescriptionConstants.PALETTE.equals(level)) {
                            if (SignDescriptionConstants.PALETTE.equals(trl
                                    .getUse())) {
                                result.addSign(info.getCode());
                            }
                        } else if (!SignDescriptionConstants.KEYBOARD
                                .equals(trl.getUse())) {
                            result.addSign(info.getCode());
                        }
                    }
                }
            }
        }

        return (PossibilitiesList) result;
    }

    @Override
    public PossibilitiesList getCodesStartingWith(String code) {
        // Temporary system...
        PossibilitiesList p = new PossibilitiesList(code);
        String protectedCode = code.replaceAll("(\\W)", "\\\\$1");
        Pattern pattern = Pattern
                .compile("^(US[0-9]+)?" + protectedCode + ".*");
        TreeSet<String> codes = new TreeSet<>();
        for (String signCode : getCodesSet()) {
            if (GardinerCode.isCorrectGardinerCode(signCode)) {
                // Protect code for insertion in a regexp.
                if (pattern.matcher(signCode).matches()) {
                    codes.add(signCode);
                }
            }
        }
        for (String signCode : codes) {
            p.addSign(signCode);
        }
        return p;
    }

    /**
     * @see HieroglyphDatabaseInterface#getSuitableSignsForCode(String)
     */
    @Override
    public PossibilitiesList getSuitableSignsForCode(String code) {
        PossibilitiesList p = new PossibilitiesList(code);
        String protectedCode = code.replaceAll("(\\W)", "\\\\$1");
        Pattern pattern = Pattern.compile("^(US[0-9]+)?" + protectedCode
                + "[a-zA-Z]*$", Pattern.CASE_INSENSITIVE);
        // Build a sorted list of codes.
        TreeSet<String> codes = new TreeSet<>();
        for (String signCode : getCodesSet()) {
            if (GardinerCode.isCorrectGardinerCode(signCode)) {
                // The exact code should come first, if possible.
                if (signCode.equalsIgnoreCase(code)) {
                    p.addSign(signCode);
                } else if (pattern.matcher(signCode).matches()) {
                    codes.add(signCode);
                }
            }
        }
        for (String signCode : codes) {
            p.addSign(signCode);
        }
        return p;
    }

    /**
     * Returns the values for a gardiner code.
     *
     * @param gardinerCode
     * @return a list of Strings, possibly empty.
     */
    @Override
    public List<String> getValuesFor(String gardinerCode) {
        List<String> result = new ArrayList<>();
        SignInfo info = getSignInfo(gardinerCode);
        for (int i = 0; i < info.getTranslitterationList().size(); i++) {
            SignTransliteration trl = (SignTransliteration) info
                    .getTranslitterationList().get(i);
            result.add(trl.getTranslitteration());
        }
        return result;
    }

    /**
     * Return the list of the families of hieroglyphs.
     *
     * @return a list of HieroglyphFamily
     * @see HieroglyphFamily
     */
    @Override
    public List<HieroglyphFamily> getFamilies() {
        return families;
    }

    private SignInfo getSignInfo(String code) {
        if (code == null) {
            return null;
        }
        if (!signInfoMap.containsKey(code)) {
            signInfoMap.put(code, new SignInfo(code));
        }
        return signInfoMap.get(code);
    }

    public void addTransliteration(String sign, String transliteration,
            String use, SignValueType type) {
        SignInfo signInfo = getSignInfo(sign);
        signInfo.addTransliteration(new SignTransliteration(transliteration,
                use, type));
        // Temporary solution.
        if (SignDescriptionConstants.KEYBOARD.equals(use)) {
            addValue(sign, transliteration);
        }
    }

    /**
     * Add sign as a variant of baseSign.
     *
     * @param sign the variant
     * @param baseSign the original sign
     * @param type the relationship between the two signs.
     */
    public void addVariant(String sign, String baseSign, SignVariantType type) {
        SignInfo baseSignInfo = getSignInfo(baseSign);
        baseSignInfo.addVariant(sign, type);
        SignInfo variantSignInfo = getSignInfo(sign);
        variantSignInfo.addVariant(baseSign, type); 
        // Not completely reflexive in theory :
        variantSignInfo.markAsVariant(true);
    }

    public void addPartOf(String sign, String baseSign) {
        SignInfo signInfo = getSignInfo(sign);
        signInfo.addSignContainingThisOne(baseSign);
        SignInfo baseSignInfo = getSignInfo(baseSign);
        baseSignInfo.addSubSign(sign);
    }

    /**
     * Sets a sign free text description. The description is a text in the
     * format of the manuel de codage. a "+l" toggle is assumed in front of the
     * description, so plain latin text is a definite possibility.
     *
     * Note that in the XML file, characters "&lt;" and &amp; will have to be
     * written "&amp;lt;" and "&amp;amp;
     *
     * @param sign
     * @param text in manuel de codage format. A "+l" toggle is assumed.
     */
    public void addSignDescription(String sign, String text) {
        SignInfo signInfo = getSignInfo(sign);
        signInfo.setDescription(text);
    }

    public void addDeterminativeValue(String sign, String category) {
        SignInfo signInfo = getSignInfo(sign);
        signInfo.addDeterminativeValue(category);
    }

    public void addTagToSign(String sign, String tag) {
        SignInfo signInfo = getSignInfo(sign);
        signInfo.addTag(tag);
    }

    public void addDeterminative(String category, String lang, String label) {
        if (!determinativeMap.containsKey(category)) {
            determinativeMap.put(category, new MultiLingualLabel(category));
        }
        MultiLingualLabel mlabel = determinativeMap.get(category);
        mlabel.addLabel(lang, label);
    }

    public void addTagLabel(String tag, String lang, String label) {
        addTagCategory(tag);
        MultiLingualLabel mlabel = tagsMap.get(tag);
        mlabel.addLabel(lang, label);
    }

    public void addTagCategory(String tag) {
        if (!tagsMap.containsKey(tag)) {
            tagsMap.put(tag, new MultiLingualLabel(tag));
        }
    }

    @Override
    public Collection<SignVariant> getVariants(String code) {
        if (code == null) {
            return Collections.emptyList();
        }
        SignInfo signInfo = getSignInfo(code);
        return signInfo.getVariants();
    }

    @Override
    public Collection<String> getVariants(String code, VariantTypeForSearches variantTypeForSearches) {
        if (code == null) {
            return Collections.emptyList();
        }
        SignInfo signInfo = getSignInfo(code);
        return signInfo.getVariants().stream()
                .filter(info -> variantTypeForSearches.match(info.getType()))
                .map(info -> info.getCode())
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<String> getSignsContaining(String code) {
        if (code == null) {
            return Collections.emptyList();
        }
        SignInfo signInfo = getSignInfo(code);
        return signInfo.getSignsContainingThisOne();
    }

    @Override
    public Collection<String> getSignsIn(String code) {
        if (code == null) {
            return Collections.emptySet();
        } else {
            SignInfo signInfo = getSignInfo(code);
            return signInfo.getSubSigns();
        }
    }

    @Override
    public Collection<String> getTagsForFamily(String familyName) {
        TreeSet<String> tagsNames = new TreeSet<>(); // Strings
        Iterator<SignInfo> iter = signInfoMap.values().iterator();
        Pattern pattern = Pattern.compile(familyName + "[0-9]+.*");
        while (iter.hasNext()) {
            SignInfo signInfo = iter.next();
            if (pattern.matcher(signInfo.getCode()).matches()) {
                tagsNames.addAll(signInfo.getTagSet());
            }
        }
        return tagsNames;
    }

    @Override
    public Collection<String> getTagsForSign(String gardinerCode) {
        TreeSet<String> tagsNames = new TreeSet<>(); // Strings
        final SignInfo signInfo = signInfoMap.get(gardinerCode);
        if (signInfo != null) {
            tagsNames.addAll(signInfo.getTagSet());
        }
        return tagsNames;
    }

    /**
     *
     * @param currentTag
     * @param familyName
     * @return a collection of sign codes.
     */
    @Override
    public Collection<String> getSignsWithTagInFamily(String currentTag,
            String familyName) {
        ArrayList<String> result = new ArrayList<>();
        Iterator<SignInfo> iter = signInfoMap.values().iterator();
        Pattern pattern = Pattern.compile("^" + familyName + "[0-9]+.*");
        while (iter.hasNext()) {
            SignInfo signInfo = iter.next();
            if (pattern.matcher(signInfo.getCode()).matches()
                    && signInfo.getTagSet().contains(currentTag)) {
                result.add(signInfo.getCode());
            }
        }
        return result;
    }

    @Override
    public Collection<String> getSignsWithoutTagInFamily(String familyName) {
        ArrayList<String> result = new ArrayList<>();
        Iterator<SignInfo> iter = signInfoMap.values().iterator();
        Pattern pattern = Pattern.compile("^" + familyName + "[0-9]+.*");
        while (iter.hasNext()) {
            SignInfo signInfo = iter.next();
            if (pattern.matcher(signInfo.getCode()).matches()
                    && signInfo.getTagSet().isEmpty()) {
                result.add(signInfo.getCode());
            }
        }
        return result;
    }

    /**
     * Return the sign description (maybe an empty string)).
     *
     * @param code the code of the sign
     * @return a textual description of the sign, in <em>Manuel de codage</em>.
     */
    @Override
    public String getDescriptionFor(String code) {
        SignInfo signInfo = signInfoMap.get(code);
        if (signInfo != null) {
            return signInfo.getDescription();
        } else {
            return "";
        }
    }

    public void setInDistributionMode(boolean inDistributionMode) {
        this.inDistributionMode = inDistributionMode;
    }

    /**
     * When this boolean is true, all new information comes from the standard
     * sources for JSesh.When false, the data is specific to the current user.
     *
     * @return a boolean
     */
    public boolean isInDistributionMode() {
        return inDistributionMode;
    }

    @Override
    public String getCanonicalCode(String code) {
        return basicManuelDeCodageManager.getCanonicalCode(code);
    }

    /**
     * Explicitly mark sign as being always displayed.
     * @param sign 
     */
    public void setSignAlwaysDisplay(String sign) {
        getSignInfo(sign).setAlwaysDisplayed(true);
    }

    @Override
    public boolean isAlwaysDisplayed(String code) {
        SignInfo signInfo = getSignInfo(code);
        return signInfo.isAlwaysDisplayed() || ! signInfo.isVariant();
    }

}

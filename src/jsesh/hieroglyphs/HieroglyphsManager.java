/*
 * Created on 25 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.hieroglyphs;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * A repository which knows about hieroglyphic codes and signs equivalence.
 * 
 * This class should be refactored as a facade to a better built database.
 * 
 * @author S. Rosmorduc
 * 
 */
public class HieroglyphsManager implements HieroglyphDatabaseInterface {

	// phonogram|ideogram|abbreviation|typical
	// private static HieroglyphsManager instance = null;
	private HashMap signInfoMap = new HashMap(); // maps sign codes to
	// signInfo.
	private HashMap tagsMap = new HashMap(); // maps tag codes to
	// MultiLingualLabels.
	private HashMap determinativeMap = new HashMap(); // maps category codes
	// to
	// MultiLingualLabels.
	private ArrayList families = null;
	/**
	 * Deal with the core manuel de codage, which can't be extended. (will
	 * probably be suppressed anyway).
	 */
	private BasicManuelDeCodageManager basicManuelDeCodageManager = null;
	private HashMap signsValues;
	private HashMap possibilitiesLists;
	/**
	 * When this boolean is true, all new information comes from the standard
	 * sources for JSesh.
	 * 
	 * When false, the data is specific to the current user.
	 */
	private boolean inDistributionMode = true;
	/**
	 * Map family codes to the signs in the corresponding Gardiner "basic"
	 * repertoire.
	 */
	private HashMap basicGardinerCodeMap;

	public HieroglyphsManager(
			BasicManuelDeCodageManager basicManuelDeCodageManager) {
		this.basicManuelDeCodageManager = basicManuelDeCodageManager;
		signsValues = new HashMap();
		possibilitiesLists = new HashMap();
		fillFamilyList();

	}

	private void fillFamilyList() {
		final String[] familyCodes = { "A", "B", "C", "D", "E", "F", "G", "H",
				"I", "K", "L", "M", "N", "O", "P", "Q", "" + "R", "S", "T",
				"U", "V", "W", "X", "Y", "Z", "Aa", "Ff" };

		final String[] familyNames = { "Man and his occupations",
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
				"Hieratic signs, Gardiner, JEA 15 (&)" };

		families = new ArrayList();
		for (int i = 0; i < familyCodes.length; i++) {
			families.add(new HieroglyphFamily(familyCodes[i], familyNames[i]));
		}
	}

	/**
	 * Return all known codes.
	 * 
	 * @return
	 */
	public Set getCodesSet() {
		// NOTE THAT THE CODES SHOULD NOT BE STORED IN THE FONT MANAGER.
		// THIS IS A TEMPORARY SOLUTION.
		// Well, sort of, as the font manager knows of all the glyphs we
		// have created, and those define signs.
		return DefaultHieroglyphicFontManager.getInstance().getCodes();
	}

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
	public String[] getCodesForFamily(String family, boolean userCodes) {
		ArrayList l = new ArrayList();
		Set s = getCodesSet();
		Iterator it = s.iterator();
		while (it.hasNext()) {
			String c = (String) it.next();
			if (family.length() > 0 && c.matches(family + "[0-9]+.*")) {
				l.add(c);
			} else if (userCodes
					&& c.matches("US" + "[0-9]+" + family + "[0-9]+.*")) {
				l.add(c);
			} else if ("".equals(family)
					&& GardinerCode.isCorrectGardinerCode(c)
					&& !c.startsWith("UG")) {
				l.add(c);
			}
		}
		Collections.sort(l, GardinerCode.getCodeComparator());
		return (String[]) l.toArray(new String[l.size()]);
	}

	/**
	 * Returns the list of basic Gardiner codes for a sign family.
	 * 
	 * @return a list of strings.
	 */
	public List getBasicGardinerCodesForFamily(String familyCode) {
		if (basicGardinerCodeMap == null) {
			basicGardinerCodeMap = new HashMap();
			try {
				// Read and build the map if necessary
				Reader in = new InputStreamReader(HieroglyphsManager.class
						.getResourceAsStream("basicGardinerCodes.txt"), "UTF-8");
				StreamTokenizer tok = new StreamTokenizer(in);
				// Read and store the codes according to their families.
				while (tok.nextToken() != StreamTokenizer.TT_EOF) {
					GardinerCode code = GardinerCode
							.createGardinerCode(tok.sval);
					if (code == null) {
						System.err.println(tok.sval);
						continue;
					}
					if (!basicGardinerCodeMap.containsKey(code.getFamily())) {
						basicGardinerCodeMap.put(code.getFamily(),
								new ArrayList());
					}
					((List) basicGardinerCodeMap.get(code.getFamily()))
							.add(tok.sval);
				}
				// Now, sort the code lists.
				Iterator it = basicGardinerCodeMap.values().iterator();
				while (it.hasNext()) {
					List l = (List) it.next();
					Collections.sort(l, GardinerCode.getCodeComparator());
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return (List) basicGardinerCodeMap.get(familyCode);
	}

	public void addValue(String gardinerCode, String value) {
		// create a value entry for the sign if necessary
		if (!signsValues.containsKey(gardinerCode)) {
			signsValues.put(gardinerCode, new ArrayList());
		}
		getOrCreatePossibilityList(value);
		// Now add the actual data
		((List) signsValues.get(gardinerCode)).add(value);
		((PossibilitiesList) possibilitiesLists.get(value)).add(gardinerCode);
	}

	private PossibilitiesList getOrCreatePossibilityList(String value) {
		// If there is no sign list for this value, create one
		if (!possibilitiesLists.containsKey(value)) {
			PossibilitiesList l = new PossibilitiesList(value);
			// Ensure the "official" manuel de codage value comes first.
			if (basicManuelDeCodageManager.isKnownCode(value)) {
				// For readability, we want to have the "official" phonetic code
				// available.
				l.add(value);
			}
			possibilitiesLists.put(value, l);
		}
		return (PossibilitiesList) possibilitiesLists.get(value);
	}

	/**
	 * Returns all possible signs for a given value.
	 * 
	 * @param phoneticValue
	 * @return a PossibilitiesList, or null if the value corresponds to nothing.
	 * @see HieroglyphDatabaseInterface#getPossibilityFor(String, String)
	 */
	public PossibilitiesList getPossibilityFor(String phoneticValue,
			String level) {
		PossibilitiesList result = null;
		// Keyboard access...

		result = new PossibilitiesList(
				getOrCreatePossibilityList(phoneticValue));

		// Palette or informative access is not currently indexed ,
		// so we look at each registered sign. It's still reasonable.
		if (SignDescriptionConstants.PALETTE.equals(level)
				|| SignDescriptionConstants.INFORMATIVE.equals(level)) {
			Iterator signIter = signInfoMap.values().iterator();
			// For each sign...
			while (signIter.hasNext()) {				
				SignInfo info = (SignInfo) signIter.next();
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
								result.add(info.getCode());
							}
						} else if (!SignDescriptionConstants.KEYBOARD
								.equals(trl.getUse()))
							result.add(info.getCode());
					}
				}
			}
		}

		return (PossibilitiesList) result;
	}

	/**
	 * Returns the values for a gardiner code.
	 * 
	 * @param gardinerCode
	 * @return a list of Strings, possibly empty.
	 */
	public List getValuesFor(String gardinerCode) {
		List result;// = (List) signsValues.get(gardinerCode);
		result = new ArrayList();
		SignInfo info = getSignInfo(gardinerCode);
		for (int i = 0; i < info.getTranslitterationList().size(); i++) {
			SignTransliteration trl = (SignTransliteration) info
					.getTranslitterationList().get(i);
			result.add(trl.getTranslitteration());
		}
		// if (result == null)
		// result = Collections.EMPTY_LIST;
		return result;
	}

	/**
	 * Return the list of the families of hieroglyphs.
	 * 
	 * @return a list of HieroglyphFamily
	 * @see HieroglyphFamily
	 */
	public List getFamilies() {
		return families;
	}

	private SignInfo getSignInfo(String code) {
		if (code == null) {
			return null;
		}
		if (!signInfoMap.containsKey(code)) {
			signInfoMap.put(code, new SignInfo(code));
		}
		return (SignInfo) signInfoMap.get(code);
	}

	public void addTransliteration(String sign, String transliteration,
			String use, String type) {
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
	 * @param sign
	 * @param baseSign
	 */
	public void addVariant(String sign, String baseSign) {
		SignInfo signInfo = getSignInfo(baseSign);
		signInfo.addVariant(sign);
	}

	public void addPartOf(String sign, String baseSign) {
		SignInfo signInfo = getSignInfo(sign);
		signInfo.addSignContainingThisOne(baseSign);
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
	 * @param text
	 *            in manuel de codage format. A "+l" toggle is assumed.
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
		MultiLingualLabel mlabel = (MultiLingualLabel) determinativeMap
				.get(category);
		mlabel.addLabel(lang, label);
	}

	public void addTagLabel(String tag, String lang, String label) {
		addTagCategory(tag);
		MultiLingualLabel mlabel = (MultiLingualLabel) tagsMap.get(tag);
		mlabel.addLabel(lang, label);
	}

	public void addTagCategory(String tag) {
		if (!tagsMap.containsKey(tag)) {
			tagsMap.put(tag, new MultiLingualLabel(tag));
		}
	}

	public Collection getVariants(String code) {
		if (code == null) {
			return Collections.EMPTY_LIST;
		}
		SignInfo signInfo = getSignInfo(code);
		return signInfo.getVariants();
	}

	public Collection getSignsContaining(String code) {
		if (code == null) {
			return Collections.EMPTY_LIST;
		}
		SignInfo signInfo = getSignInfo(code);
		return signInfo.getSignsContainingThisOne();
	}

	public Collection getTagsForFamily(String familyName) {
		TreeSet tagsNames = new TreeSet(); // Strings
		Iterator iter = signInfoMap.values().iterator();
		Pattern pattern = Pattern.compile(familyName + "[0-9]+.*");
		while (iter.hasNext()) {
			SignInfo signInfo = (SignInfo) iter.next();
			if (pattern.matcher(signInfo.getCode()).matches()) {
				tagsNames.addAll(signInfo.getTagSet());
			}
		}
		return tagsNames;
	}

	/**
	 * 
	 * @param currentTag
	 * @param familyName
	 * @return a collection of sign codes.
	 */
	public Collection getSignsWithTagInFamily(String currentTag,
			String familyName) {
		ArrayList result = new ArrayList();
		Iterator iter = signInfoMap.values().iterator();
		Pattern pattern = Pattern.compile("^" + familyName + "[0-9]+.*");
		while (iter.hasNext()) {
			SignInfo signInfo = (SignInfo) iter.next();
			if (pattern.matcher(signInfo.getCode()).matches()
					&& signInfo.getTagSet().contains(currentTag)) {
				result.add(signInfo.getCode());
			}
		}
		return result;
	}

	public Collection getSignsWithoutTagInFamily(String familyName) {
		ArrayList result = new ArrayList();
		Iterator iter = signInfoMap.values().iterator();
		Pattern pattern = Pattern.compile("^" + familyName + "[0-9]+.*");
		while (iter.hasNext()) {
			SignInfo signInfo = (SignInfo) iter.next();
			if (pattern.matcher(signInfo.getCode()).matches()
					&& signInfo.getTagSet().isEmpty()) {
				result.add(signInfo.getCode());
			}
		}
		return result;
	}

	/**
	 * Return the sign description (maybe an empty string)
	 */
	public String getDescriptionFor(String code) {
		SignInfo signInfo = (SignInfo) signInfoMap.get(code);
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
	 * sources for JSesh.
	 * 
	 * When false, the data is specific to the current user.
	 */
	public boolean isInDistributionMode() {
		return inDistributionMode;
	}

	public String getCanonicalCode(String code) {
		return basicManuelDeCodageManager.getCanonicalCode(code);
	}

	public void setSignAlwaysDisplay(String sign) {
		getSignInfo(sign).setAlwaysDisplayed(true);
	}

	public boolean isAlwaysDisplayed(String code) {
		return getSignInfo(code).isAlwaysDisplayed();
	}

}
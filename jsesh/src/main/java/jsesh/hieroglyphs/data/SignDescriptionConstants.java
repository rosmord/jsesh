package jsesh.hieroglyphs.data;

/**
 * XML tags names and property names used in sign_description.dtd.
 * @see sign_description.dtd
 * @author rosmord
 *
 */
public class SignDescriptionConstants {

	public static final String SIMILAR_TO = "similarTo";
	public static final String PHANTOM = "phantom";
	public static final String LABEL = "label";
	public static final String LANG = "lang";
	public static final String TAG = "tag";
	public static final String CATEGORY = "category";
	public static final String BASE_SIGN = "baseSign";
	public static final String TRANSLITERATION = "transliteration";
	public static final String TAG_CATEGORY = "tagCategory";
	public static final String TAG_LABEL= "tagLabel";
	public static final String DETERMINATIVE_CATEGORY = "determinativeCategory";
	public static final String HAS_TAG = "hasTag";
	public static final String IS_DETERMINATIVE = "isDeterminative";
	public static final String SIGN_DESCRIPTION = "signDescription";
	public static final String IS_PART_OF = "partOf";
	public static final String VARIANT_OF = "variantOf";
	/**
	 * Value type for a sign.
	 * possible values will be phonogram|ideogram|abbreviation|typical
	 */
	public static final String TYPE = "type";
	public static final String RELEVANCE = "relevance";
	public static final String HAS_TRANSLITERATION = "hasTransliteration";
	public static final String SIGNS = "signs";
	public static final String CONTAINS = "contains";
	public static final String PART_CODE = "partCode";
	public static final String IS_SIMILAR = "isSimilar";
	public static final String LINGUISTIC = "linguistic";
	public static final String EXISTS_IN = "existsIn";
	public static final String SIGN = "sign";
	/**
	 * Possible value for degree of variantOf.
	 * Means complete linguistic variant.
	 * The sign has the same values as the base sign.
	 * In text searches, the variant will be usually considered exactly as the base sign
	 * (e.g. Z7 and G43). 
	 */
	public static final String FULL= "full";
	/**
	 * Possible value for degree of variantOf.
	 * Means the sign can be used as a variant of the base sign in some contexts.
	 * The sign will be accessible from the variant button in the sign palette. 
	 */
	public static final String PARTIAL= "partial";
	/**
	 * Possible value for degree of variantOf.
	 * Means that in some cases, the sign can be considered as a variant of another one,
	 * but that is has also values of its own.
	 * 
	 * Currently won't be accessible as a variant from the palette.
	 * But will be usable, <em>on request</em> for searches.
	 * e.g. D36 as a variant of D40.
	 */
	public static final String OTHER= "other";
	public static final String USE = "use";
	public static final String KEYBOARD = "keyboard";
	public static final String PALETTE= "palette";
	public static final String INFORMATIVE= "informative";
	
	//phonogram|ideogram|abbreviation|typical
	
	public static final String PHONOGRAM= "phonogram";
	public static final String IDEOGRAM= "ideogram";
	public static final String ABBREVIATION= "abbreviation";
	public static final String TYPICAL= "typical";
	public static final String ALWAYS_DISPLAY = "alwaysDisplay";
	public static final String[] LANGUAGES_CODES = new String[] { "en", "ar", "de", "es",
	"fr", "he", "it", "nl", "ru" };
	public static final String NO = "no";
	public static final String UNSPECIFIED = "unspecified";
	
}

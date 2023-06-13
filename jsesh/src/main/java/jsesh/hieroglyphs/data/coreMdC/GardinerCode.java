package jsesh.hieroglyphs.data.coreMdC;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Representation of a code in "gardiner form" in our fonts. Usable for sorting
 * and the like.
 * <p>
 * The class also contains a number of helper functions which work on strings.
 *
 * @author rosmord
 */
public final class GardinerCode implements Comparable<GardinerCode> {

    /**
     * text of regular expression for "Gardiner-like" codes. (we allow for AA/FF
     * being writen instead of Aa and Ff.
     */
    private static final String GARDINER_CODE_REGEXP_STRING = "(US([0-9]+))?([A-Z]|Aa|Ff|NL|NU)([0-9]+)([A-Za-z]*)";

    /**
     * Additional numeric codes for digits.
     */
    private static final String NUMBER_REGEXP_STRING = "[23456789]|[2-9]00*";

    /**
     * Tksesh-like code
     */
    private static final String TKSESH_USER_REGEXP_STRING = "UG(\\d+)M(\\d+)N(\\d+)";

    /**
     * Pattern for testing if a code is a possible canonical code.
     */
    private static final Pattern testCanonicalPattern = Pattern.compile(GARDINER_CODE_REGEXP_STRING + "|"
            + NUMBER_REGEXP_STRING) ;

    /**
     * Pattern for testing for codes which are "Gardiner-like", i.e. with a
     * family part in them.
     */
    private static final Pattern jseshPattern = Pattern.compile(GARDINER_CODE_REGEXP_STRING);

    /**
     * The same, but the pattern is in uppercase
     */
    private static final Pattern jseshUpperCasePattern = Pattern.compile(GARDINER_CODE_REGEXP_STRING.toUpperCase(Locale.ENGLISH));

    /**
     * Should move in a "code" class
     */
    private final static List<String> FAMILIES = Arrays.asList(new String[]{
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "K", "L", "M", "N",
        "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "Aa",
        "Ff", "NU", "NL"});

    /**
     * Optional user id. 0 means no user (i.e. "standard" sign.
     */
    private int userId = 0;    
    private String family;
    private int number;
    private String variantPart;
  

    /**
     * Create a Gardiner code object from a string. Send a triplet (family,
     * number, variant) or null if the code hasn't got the proper form.
     *
     * @param code
     * @return a gardiner code, or null if the code is incorrect.
     */
    public static GardinerCode createGardinerCode(String code) {
        GardinerCode result = null;
        if (code == null) {
            code = "";
        }
        Matcher matcher = jseshPattern.matcher(code);
        int userId = 0;
        
        if (matcher.matches()) {            
            if (matcher.group(2) != null) {
            	userId = Integer.parseInt(matcher.group(2));
            }
            String family = matcher.group(3);
            int number = Integer.parseInt(matcher.group(4));
            String variantPart = matcher.group(5);
            result = new GardinerCode(userId, family, number, variantPart);
        }
        return result;
    }


    /**
     * Constructor for user defined signs.
     *
     * @param userId (0 means "standard" sign).
     * @param family : A, B... Aa, Ff, NL, NU
     * @param number : sign number in family
     * @param variantNumber variant extension (A, EXTA, VARA, etc.)
     */
    public GardinerCode(int userId, String family, int number, String variantNumber) {
        this.userId = userId;
        this.family = family;
        this.number = number;
        setVariantPart(variantNumber);
    }

    /**
     * Constructor for "standard" signs.
     * @param family
     * @param number
     * @param variantNumber
     */
    public GardinerCode(String family, int number, String variantNumber) {
    	this(0, family,  number, variantNumber);
    }
    
    private void setVariantPart(String v) {
        variantPart = v.toUpperCase(Locale.ENGLISH);
        if ("H".equals(variantPart) || "V".equals(variantPart)) {
            variantPart = variantPart.toLowerCase(Locale.ENGLISH);
        }
    }

    /**
     * @return the family
     */
    public String getFamily() {
        return family;
    }


    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    
    /**
     * @return the variantNumber
     */
    public String getVariantPart() {
        return variantPart;
    }


    /**
     * Returns the id of the user who created this sign, or 0 for standard
     * signs.
     *
     * @return
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Compare two codes.
     *
     * @param code
     */
    @Override
    public int compareTo(GardinerCode code) {
        int result;
        result = FAMILIES.indexOf(family) - FAMILIES.indexOf(code.getFamily());
        if (result == 0) {
            result = number - code.getNumber();
        }
        if (result == 0) {
            result = variantPart.toLowerCase(Locale.ENGLISH).compareTo(code.getVariantPart().toLowerCase(Locale.ENGLISH));
        }
        if (result == 0) {
            result = userId - code.userId;
        }
        return result;
    }

    /**
     * Compare two gardiner codes, given as strings.
     *
     * @param c1
     * @param c2
     * @return
     */
    public static int compareCodes(String c1, String c2) {
        GardinerCode g1 = createGardinerCode(c1);
        GardinerCode g2 = createGardinerCode(c2);
        if (g1 == null) {
            if (g2 == null) {
                return c1.compareTo(c2);
            } else {
                return 1;
            }
        } else if (g2 == null) {
            return -1;
        } else {
            return g1.compareTo(g2);
        }
    }

    /**
     * Return the names of the "Gardiner" families, in the usual order. Includes
     * the additional "Ff" family.
     *
     * @return a list of Strings.
     */
    public static List<String> getGardinerFamilies() {
        return FAMILIES;
    }

    /**
     * Returns the string version of the code (e.g. G17A).
     */
    @Override
    public String toString() {
        if (userId == 0) {
            return family + this.number + this.variantPart;
        } else {
            return "US" + this.userId + this.family + this.number + this.variantPart;
        }
    }

    /**
     * Returns a comparator able to compare two gardiner codes (as strings) and
     * to order them.
     *
     * @return a comparator able to compare two gardiner codes and to order
     * them.
     */
    public static Comparator<String> getCodeComparator() {
        return (c1, c2) -> compareCodes(c1, c2);
    }

    /**
     * Is the given code a canonical Manuel de codage code ?
     * <p>
     * We define canonical MdC codes as:
     * <ul>
     * <li> Gardiner codes (see isCorrectGardinerCode)
     * <li> numbers 2, 3, 4, 5, 6, 7, 8, 9, 20, ..., 90, 200..900, 2000..9000 etc...
     * </ul>
     *
     * "3" as a code is a new addition - I think originally an inscribe code. Originally, "3" and "Z2"
     * where the same signs, but as a version of "3" was made for groups like 3:2, we had introduced a new sign without
     * canonical code.
     *
     * @param code
     * @return
     * @see #isWellFormedGardinerCode(String)
     */
    public static boolean isCanonicalCode(String code) {
        return GardinerCode.testCanonicalPattern.matcher(code).matches();
    }

    /**
     * Returns true if the sign has the form of a "Gardiner" code, with a family and a number. 
     * <p>This is different from @{link {@link #isCanonicalCode(String)}.
     * 
     * For historical reasons, the MdC contains a number of signs
     * which have no Gardiner code.
     * 
     * <p>
     * As a result for accepting AA and FF as family names (in addition to Aa
     * and Ff), the following holds true : if <code>X</code> is a correct code,
     * then <code>X.toUpperCase(Locale.ENGLISH)</code> is also correct.
     * <p>
     * More precisely, we consider as "gardiner codes":
     * <u>
     * <li> Gardiner codes as defined in the manuel, plus the Ff family.
     * <li> JSesh extended signs, with "US" + user id in front of a gardiner
     * code
     * <li> TODO : add @ as a prefix.
     * <li> We accept signs with both upper case and lower case letters after the
     * sign number, as a variant indicator. Note that the <em>manuel</em> distinguishes
     * the two, but most software don't.
     * <li> for families Aa and Ff, the family names AA and FF are also
     * accepted.
     * 
     * </ul>
     *
     * @param code
     * @return
     */
    public static boolean isWellFormedGardinerCode(String code) {
        return Pattern.matches(GARDINER_CODE_REGEXP_STRING, code);
    }

    public static boolean isWellFormedCodeIgnoreCase(String code) {
        Pattern pattern = Pattern.compile(GARDINER_CODE_REGEXP_STRING, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(code).matches();
    }

    /**
     * Returns the Manuel de codage code corresponding to a given file name, or
     * null if the file name does not fit.
     *
     * <p> It will return null for files which corresponds to phonetic codes.
     * 
     * <p>
     * Can also be used to normalise codes which don't respect the MdC
     * capitalisation rules.
     *
     * <p>
     * This function can be used when a file name is supposed to correspond to a
     * gardiner code. The problem in this case is that not all file systems are
     * case sensitives, hence the need to process the file name.
     * <p>
     * This method understands Gardiner codes, and MdC specificities (nn and nTrw
     * codes) and Jsesh extensions.
     * 
     * @param fname a file name (with a mandatory extensions, ".svg", ".png",
     * whatever).
     * 
     * @return a code or null if no code can be created.
     */
    public static String getCodeForFileName(String fname) {
        // We put the code in upper case.
        // Note : toUpperCase was problematic for some locale (e.g. Turkish one)
        //        where a dotless "i" exists.
        // Thanks to tahacelik20181 for the suggested fix.
        fname = fname.toUpperCase(Locale.ENGLISH);
        // suppress file extensions.
        int stopIndex = fname.indexOf('.');
        if (stopIndex == -1) {
            stopIndex = fname.length();
        }
        String code = fname.substring(0, stopIndex);

        // Special codes "nTrw" and "nn" don't have a Gardiner code associated
        // to it.
        if ("NTRW".equals(code)) {
            code = "nTrw";
        } else if ("NN".equals(code.toLowerCase(Locale.ENGLISH))) {
            code = "nn";
        } else {
            // Test for a gardiner code.
            Matcher matcher;
            matcher = jseshUpperCasePattern.matcher(code);
            if (matcher.matches()) {
            	Integer userId = 0;
            	String family;
            	int number;
            	String varExtension = "";
                
                if (matcher.group(2) != null) {
                	userId = Integer.parseInt(matcher.group(2));
                }
                family = matcher.group(3);
                switch (family) {
				case "AA":
					family = "Aa";
					break;
				case "FF":
					family = "Ff";
					break;				
				default:
					break;
				}
                number = Integer.parseInt(matcher.group(4));
                varExtension = matcher.group(5);
                GardinerCode gardinerCode = new GardinerCode(userId, family, number, varExtension);
                code = gardinerCode.toString();
            } else if (code.matches("[0-9]+")) {
                // do nothing
            } else if (code.matches(TKSESH_USER_REGEXP_STRING)) {
                // do nothing
            } else {
                code = null;
            }
        }
        return code;
    }
    
}

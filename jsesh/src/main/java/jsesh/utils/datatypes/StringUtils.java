package jsesh.utils.datatypes;

/**
 * SImple String-oriented utilities.
 */
public class StringUtils {
    private StringUtils() {
        super();
    }

    /**
     * Removes any non-alphanumeric characters to avoid having hyphens in translitterations.
     * 
     * <p> When used to retrieve signs, the presence of hyphens is a problem, as 
     * they have a specific meaning in MdC. We will thus replace Hwt-Hr by HwtHr.
     * @param originalCode
     * @return the normalized phonetic code.
     */
    public static String removeHyphens(String originalCode) {
        return originalCode.replaceAll("[^\\p{IsLetter}\\p{IsDigit}]", "");
    }
}

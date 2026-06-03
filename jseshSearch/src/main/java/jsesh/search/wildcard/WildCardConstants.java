package jsesh.search.wildcard;

/**
 * String constants used in wildcard search.
 * 
 * Those are the codes used in the MdC string to introduce the different wildcard elements. 
 */
public class WildCardConstants {

    /**
     * Code in the MdC String to introduce the start of a set of searched signs.
     */
    public static final String QUERY_SET_BEGIN = "QUERYSETB";

    /**
     * Code in the MdC String to introduce the end of a set of searched signs.
     */
    public static final String QUERY_SET_END = "QUERYSETE";

    /**
     * Code in the MdC String for a skip (a undefined number of signs, possibly
     * 0).
     */
    public static final String QUERY_SKIP = "QUERYSKIP";

    /**
     * Private constructor to prevent instantiation. 
     */
    private WildCardConstants() {        
    }


    
}

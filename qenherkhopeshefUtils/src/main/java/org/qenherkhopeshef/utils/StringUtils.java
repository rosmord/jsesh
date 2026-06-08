package org.qenherkhopeshef.utils;



public class StringUtils {
    private StringUtils() {        
    }    

    /** 
     * Checks that a string is not empty in any sense, not null, not empty, not only spaces.
     * @param s the string to check
     * @return true if the string is not empty, false otherwise.
     */
    public static boolean isNotEmpty(String s) {
        return s != null && ! "".equals(s.trim());
    }
}

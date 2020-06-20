/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.utils;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compute and extract line numbers.
 * @author rosmord
 */
public class LineNumberHelper {
        
    /**
     * Returns an incremented version of lineNumber.
     * The return value will increment the last number in <code>lineNumber</code>.
     * For instance, for <code>(12,2)</code>, we will return <code>12,3</code>
     * <p>
     * If there is no line number in lineNumber, will return lineNumber.
     * @param lineNumber
     * @return the computed next line number.
     */
    public static String incrementLineNumber(String lineNumber) {
        String patternString= "^(.*?)(\\d+)(\\D*)$";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(lineNumber);
        if (matcher.matches()) {
            String start = matcher.group(1);
            String number = matcher.group(2);
            String end = matcher.group(3);
            BigInteger b = new BigInteger(number);
            b = b.add(BigInteger.ONE);
            return start + b.toString() + end;
        } else {
            return lineNumber;
        }
    }
    
}

/*
 * Created on 7 juil. 2005 by rosmord
 *
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.swing;

import java.text.ParseException;
import java.util.HashSet;

import javax.swing.text.DefaultFormatter;

/**
 * @author rosmord
 *
 * A formatter to use with JFormattedTextFields, which prevents some chars to be entered.
 */
public class RestrictedCharFormatter extends DefaultFormatter {

   
    
    // The set of forbidden chars (entered as unicode codepoints).
    HashSet codePoints;
    
    /**
     * 
     */
    public RestrictedCharFormatter() {       
        codePoints= new HashSet();
        setAllowsInvalid(false);
    }
    
    public RestrictedCharFormatter(int [] chars) {
        this();
        for(int i=0; i< chars.length; i++) {
            codePoints.add(new Integer(chars[i]));
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JFormattedTextField.AbstractFormatter#stringToValue(java.lang.String)
     */
    public Object stringToValue(String text) throws ParseException {
        //StringBuffer buffer= new StringBuffer();        
        // TODO : THIS METHOD MUST BE UPDATED WHEN FULL JDK 1.5 IS USED,
        // TO TAKE ADVANTAGE AT THE FULL UNICODE SYSTEM.
        for (int i=0; i< text.length(); i++) {
            if (codePoints.contains(new Integer(text.charAt(i)))) {
                throw new ParseException("error ",i);
            }
        }
        
        return text;
    }

    /* (non-Javadoc)
     * @see javax.swing.JFormattedTextField.AbstractFormatter#valueToString(java.lang.Object)
     */
    public String valueToString(Object value) throws ParseException {
        String result= null;
        if (value instanceof String)
            result= (String) value;        
        return result;
    }

}

package jsesh.mdc.lex;

public class MDCModifier {
    
    private String  name;
    private Integer intValue;
    
    /**
     * Analyse a string in MDC format and extracts the modifier
     * it represents
     * @param str
     * @return the modifier which corresponds to the code.
     */
     
    public static MDCModifier buildMDCModifierFromString(String str)
    {
	char c;
	int endPos= 1; // Skip the initial "\"
	// find the first char in str which is not
	// an ASCII letter nor an "?"
	while (endPos < str.length() 
	       && (Character.isLetter(c=str.charAt(endPos))
		   || c == '?'))
	    {
		endPos++;
	    }
	
	String name= str.substring(1, endPos);
	Integer value= null;
	if (endPos < str.length()) // If there is a value, capture it.
	    value= Integer.parseInt(str.substring(endPos, str.length()));
	return new MDCModifier(name, value);
    }
    
    public MDCModifier(String name, Integer intValue)
    {
	setName(name);
	setIntValue(intValue);
    }

    public MDCModifier(String name)
    {
	setName(name);
	setIntValue(null);
    }
    
    /**
     * Gets the value of name
     *
     * @return the value of name
     */
    public String getName() {
	return this.name;
    }

    /**
     * Sets the value of name
     *
     * @param argName Value to assign to this.name
     */
    public void setName(String argName){
	this.name = argName;
    }

    /**
     * Gets the value of intValue
     *
     * @return the value of intValue
     */
    public Integer getIntValue() {
	return this.intValue;
    }

    /**
     * Sets the value of intValue
     *
     * @param argIntValue Value to assign to this.intValue
     */
    public void setIntValue(Integer argIntValue){
	this.intValue = argIntValue;
    }

    public String toString()
    {
	return "\\" + getName() + "=" + getIntValue();
    }

    // Could become a test suite
//    public static void main(String a[])
//    {
//	MDCModifier m;
//	m= buildMDCModifierFromString("\\");
//	System.out.println("" + m);
//	m= buildMDCModifierFromString("\\?");
//	System.out.println("" + m);
//	m= buildMDCModifierFromString("\\80");
//	System.out.println("" + m);
//	m= buildMDCModifierFromString("\\tyut800");
//	System.out.println("" + m);
//	m= buildMDCModifierFromString("\\tyut");
//	System.out.println("" + m);
//	m= buildMDCModifierFromString("\\R-10");
//	System.out.println("" + m);
//    }
}

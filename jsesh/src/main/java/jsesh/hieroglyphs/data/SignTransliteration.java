package jsesh.hieroglyphs.data;

public class SignTransliteration {

//	sign CDATA #REQUIRED
//	translitteration CDATA #REQUIRED
//	relevance NMTOKEN '3'
//	type (phonogram|ideogram|abbreviation|typical) 'phonogram'
    private String translitteration;
    private String use;
    private SignValueType type;

    public SignTransliteration(String translitteration, String use, SignValueType type) {
        super();
        this.translitteration = translitteration;
        this.use = use;
        setType(type);
    }

    
    /**
     * Returns the uses of this transliteration in JSesh.
     * KEYBOARD: usual value, accessible through the keyboard
     * PALETTE: accessible through the palette
     * INFORMATIVE: for display only. 
     * @return
     * @see SignDescriptionConstants#KEYBOARD
     * @see SignDescriptionConstants#PALETTE
     * @see SignDescriptionConstants#INFORMATIVE
     */
    public String getUse() {
		return use;
	}



	public void setUse(String use) {
		this.use = use;
	}



	/**
     * @return the translitteration
     */
    public String getTranslitteration() {
        return translitteration;
    }

    /**
     * @param translitteration the translitteration to set
     */
    public void setTranslitteration(String translitteration) {
        this.translitteration = translitteration;
    }

    /**
     * return the type, which is one of the values in SignValueType.
     * @return the type
     * @see SignValueType
     */
    public SignValueType getType() {
        return type;
    }

    /**
     * @param type the type to set
     * @see SignValueType
     */
    public final void setType(SignValueType type) {
        this.type = type;
    }
}
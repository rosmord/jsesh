package jsesh.hieroglyphs.data;

public  class TranslationInfo {

    //private jsesh.hieroglyphs.MultiLingualLabel outer;
    private String lang = "";
    private String label = "";
 

    public TranslationInfo(String lang, String label) {
        super();
        this.label = label;
        this.lang = lang;
        
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}

package jsesh.hieroglyphs.data;

import java.util.HashMap;
import java.util.Map;

public class MultiLingualLabel {

    private String labelKey = "";
    private final Map translations = new HashMap();

    public MultiLingualLabel(String labelKey) {
        super();
        this.labelKey = labelKey;
    }

    /**
     * Add a new translation for this label.
     * 
     */
    public void addLabel(String lang, String label) {
        TranslationInfo info = new TranslationInfo(lang, label);
        translations.put(lang, info);
    }

    public String getLabel(String lang) {
        if (translations.containsKey(lang)) {
            return ((TranslationInfo) translations.get(lang)).getLabel();
        } else {
            return getDefaultLabel();
        }
    }

    public String getDefaultLabel() {
        return (String) getLabel("en");
    }

    /**
     * @return the labelKey
     */
    public String getLabelKey() {
        return labelKey;
    }

    /**
     * @param labelKey the labelKey to set
     */
    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }


    public TranslationInfo[] getAllTranslations() {
        return (TranslationInfo[]) translations.values().toArray(new TranslationInfo[translations.values().size()]);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((labelKey == null) ? 0 : labelKey.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MultiLingualLabel other = (MultiLingualLabel) obj;
        if (labelKey == null) {
            if (other.labelKey != null) {
                return false;
            }
        } else if (!labelKey.equals(other.labelKey)) {
            return false;
        }
        return true;
    }         
}
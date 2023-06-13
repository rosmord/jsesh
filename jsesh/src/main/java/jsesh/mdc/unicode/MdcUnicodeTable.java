package jsesh.mdc.unicode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import jsesh.hieroglyphs.data.HieroglyphDatabaseInterface;
import jsesh.hieroglyphs.data.coreMdC.ManuelDeCodage;
import jsesh.hieroglyphs.data.HieroglyphDatabaseFactory;
import jsesh.mdc.model.Hieroglyph;

/**
 * Link between MdC codes and Unicode codes.
 *
 * <p>
 * The class deals only with the sign codes, and is very basic, so that it can
 * be used without the JSesh library if needed.
 *
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public enum MdcUnicodeTable {
    INSTANCE;

    private HashMap<String, String> codeMap = new HashMap<>();

    private MdcUnicodeTable() {
        try {
            InputStream in = this.getClass().getResourceAsStream(
                    "mdc2unicode.txt");
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String l;
            while ((l = r.readLine()) != null) {
                l = l.trim();
                if (l.startsWith("#")) {
                    continue;
                }
                String[] fields = l.split("\t");
                String mdcCode = fields[0];
                int unicode = Integer.parseInt(fields[2].substring(2), 16);
                char[] c = Character.toChars(unicode);
                String s = "" + c[0] + c[1];
                codeMap.put(mdcCode, s);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the java UTF-16 unicode string for a given Mdc sign.
     *
     * @param signCode a code in MDC format (Gardiner or phonetic)
     * @return an unicode string, or the code itself if the sign has no unicode
     * equivalent.
     */
    public String getUnicodeFor(String signCode) {
        String canonicalCode = ManuelDeCodage.getInstance().getCanonicalCode(signCode);
        return codeMap.getOrDefault(canonicalCode, signCode);
    }

    /**
     * Does this sign have a Unicode equivalent?
     *
     * @param signCode the MdC code for the sign.
     * @return true or false.
     */
    public boolean hasUnicode(String signCode) {
    	String canonicalCode = ManuelDeCodage.getInstance().getCanonicalCode(signCode);
        return codeMap.containsKey(canonicalCode);
    }

}

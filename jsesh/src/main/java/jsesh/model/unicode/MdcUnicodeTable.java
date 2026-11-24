package jsesh.model.unicode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import jsesh.signcodes.ManuelDeCodage;

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

    private final HashMap<String, String> codeMap = new HashMap<>();

    /// Number of entries which come from `namemdc.csv` only.
    private int supplementaryCount = 0;

    private MdcUnicodeTable() {
        loadMainTable();
        loadSupplementaryTable();
    }

    /// Loads `mdc2unicode.txt`, our curated table. It has priority over the
    /// supplementary table.
    private void loadMainTable() {
        try (InputStream in = this.getClass().getResourceAsStream("mdc2unicode.txt");
                BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String l;
            while ((l = r.readLine()) != null) {
                l = l.trim();
                if (l.isEmpty() || l.startsWith("#")) {
                    continue;
                }
                String[] fields = l.split("\t");
                int unicode = Integer.parseInt(fields[2].substring(2), 16);
                codeMap.put(fields[0], Character.toString(unicode));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /// Loads `namemdc.csv` (a mapping JSesh code → Unicode string, contributed
    /// by Mark-Jan Nederhof). It covers many more signs than `mdc2unicode.txt`,
    /// including Unicode 16 signs. Entries are added only for codes which the
    /// main table doesn't know about, so the curated choices are never
    /// overridden.
    ///
    /// Entries containing Unicode format controls (U+13430-U+1345F) describe
    /// groups, that is, layout. They are ignored for now: layout will be
    /// handled separately. Variation selectors are kept, as they are part of
    /// the sign's identity.
    private void loadSupplementaryTable() {
        try (InputStream in = this.getClass().getResourceAsStream("namemdc.csv");
                BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String l;
            while ((l = r.readLine()) != null) {
                l = l.trim();
                int comma = l.indexOf(',');
                if (comma <= 0 || comma == l.length() - 1) {
                    continue;
                }
                String unicode = l.substring(comma + 1);
                if (unicode.codePoints().anyMatch(c -> c >= 0x13430 && c <= 0x1345F)) {
                    continue;
                }
                String code = ManuelDeCodage.getInstance().getCanonicalCode(l.substring(0, comma)).code();
                if (codeMap.putIfAbsent(code, unicode) == null) {
                    supplementaryCount++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /// Number of signs known only thanks to the supplementary table.
    int getSupplementaryCount() {
        return supplementaryCount;
    }

    /**
     * Gets the java UTF-16 unicode string for a given Mdc sign.
     *
     * @param signCode a code in MDC format (Gardiner or phonetic)
     * @return an unicode string, or the code itself if the sign has no unicode
     * equivalent.
     */
    public String getUnicodeFor(String signCode) {
        String canonicalCode = ManuelDeCodage.getInstance().getCanonicalCode(signCode).code();
        return codeMap.getOrDefault(canonicalCode, signCode);
    }

    /**
     * Does this sign have a Unicode equivalent?
     *
     * @param signCode the MdC code for the sign.
     * @return true or false.
     */
    public boolean hasUnicode(String signCode) {
    	String canonicalCode = ManuelDeCodage.getInstance().getCanonicalCode(signCode).code();
        return codeMap.containsKey(canonicalCode);
    }

}

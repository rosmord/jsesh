package jsesh.io.document;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jsesh.document.SignIdManager;
import jsesh.model.constants.JSeshInfoConstants;

/// Reads and writes the sign ids information of a document header.
///
/// The only property is `JSesh_max_sign_id`, the highest id ever given to a
/// sign (see [SignIdManager]). Its presence, even with the value 0, means that
/// the document is *numbered*: the editor gives and keeps the `\sid` of its
/// signs. Older versions of JSesh ignore it.
public final class SignIdsHeaderCodec {

    private static final Logger LOGGER = Logger.getLogger(SignIdsHeaderCodec.class.getName());

    /// The property holding the highest sign id used.
    public static final String MAX_ID = JSeshInfoConstants.JSESH_INFO_PREFIX + "max_sign_id";

    private SignIdsHeaderCodec() {
    }

    /// Tells the manager what the header says. Does nothing if the header has
    /// no sign ids property.
    ///
    /// @param header the header properties.
    /// @param manager the manager of the document being read.
    public static void fromHeader(Map<String, String> header, SignIdManager manager) {
        String value = header.get(MAX_ID);
        if (value == null) {
            return;
        }
        int max = 0;
        try {
            max = Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.WARNING, "Malformed " + MAX_ID + " in document header: " + value);
        }
        manager.setFromHeader(max);
    }

    /// The header properties for a document: empty unless it is numbered.
    ///
    /// @param manager the manager of the document.
    /// @return the properties to write.
    public static Map<String, String> toHeader(SignIdManager manager) {
        Map<String, String> map = new TreeMap<>();
        if (manager.isNumbered()) {
            map.put(MAX_ID, Integer.toString(manager.getMaxId()));
        }
        return map;
    }
}

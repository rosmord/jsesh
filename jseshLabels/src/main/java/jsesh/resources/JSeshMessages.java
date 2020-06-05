package jsesh.resources;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Generic access to internationalized messages for some JSesh components. Note
 * : this system is not used by all components.
 * <p>
 * At the time being, the JSesh component itself uses the BundledAction
 * framework we had devised a long time ago.
 *
 * @author rosmord
 */
public class JSeshMessages {

    private static final String BUNDLE_NAME = "jsesh.resources.labels"; //$NON-NLS-1$
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(BUNDLE_NAME);

    private JSeshMessages() {
    }

    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String format(String key, String... values) {
        String pattern = RESOURCE_BUNDLE.getString(key);        
        return MessageFormat.format(pattern, (Object[])values);
    }

}

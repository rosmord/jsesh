package jsesh.preferences;

import java.util.prefs.Preferences;

/**
 * This class is designed allow JSesh to store its preferences in a single place, and resist in case of package refactoring.
 */
public class JSeshPreferencesRoot {
    private JSeshPreferencesRoot() {}
    
    public static Preferences getPreferences() {
        return Preferences.userNodeForPackage(JSeshPreferencesRoot.class);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.jhotdraw.preferences.application.model;

import java.util.Locale;
import java.util.prefs.Preferences;

import jsesh.utils.preferences.JSeshPreferencesRoot;

/**
 * Application preferences linked to the UI.
 * <p>
 * Currently, icon height and user interface locale.
 * <p>
 * TODO: unify with other preferences ??? propose a better mechanism ??
 *
 * @author rosmord
 */
public class ApplicationUIPreferences {

    public static final String ICON_HEIGHT = "ICON_HEIGHT";

    /**
     * Preference key for the user interface locale.
     * <p>
     * The value is a language tag (as produced by {@link Locale#toLanguageTag()}).
     * An empty string means "use the system default locale".
     */
    public static final String LOCALE = "LOCALE";

    private int iconHeight = 30;

    /**
     * The chosen user interface locale, or {@code null} to use the system
     * default.
     */
    private Locale locale = null;

    public int getIconHeight() {
        return iconHeight;
    }

    public void setIconHeight(int iconHeight) {
        this.iconHeight = iconHeight;
    }

    /**
     * Returns the chosen user interface locale.
     *
     * @return the chosen locale, or {@code null} if the system default should
     * be used.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the chosen user interface locale.
     *
     * @param locale the locale to use, or {@code null} to use the system
     * default.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public void savetoPreferences() {
        Preferences preferences = JSeshPreferencesRoot.getPreferences();
        preferences.putInt(ICON_HEIGHT, iconHeight);
        preferences.put(LOCALE, locale == null ? "" : locale.toLanguageTag());
    }

    public static ApplicationUIPreferences getFromPreferences() {
        Preferences preferences = JSeshPreferencesRoot.getPreferences();
        ApplicationUIPreferences applicationPreferences = new ApplicationUIPreferences();
        applicationPreferences.setIconHeight(preferences.getInt(ICON_HEIGHT, 30));
        String languageTag = preferences.get(LOCALE, "");
        applicationPreferences.setLocale(
                languageTag.isEmpty() ? null : Locale.forLanguageTag(languageTag));
        return applicationPreferences;
    }

    /**
     * Applies the locale stored in the preferences as the JVM default locale.
     * <p>
     * This must be called very early during startup, before any resource
     * bundle (labels, messages...) is loaded, so that internationalized
     * strings are resolved with the chosen locale. Changing the locale only
     * takes effect on the next application startup.
     */
    public static void applyStoredLocale() {
        Locale locale = getFromPreferences().getLocale();
        if (locale != null) {
            Locale.setDefault(locale);
        }
    }
}

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
 * Currently, user interface locale and scale.
 * <p>
 * TODO: unify with other preferences ??? propose a better mechanism ??
 *
 * @author rosmord
 */
public class ApplicationUIPreferences {

    /**
     * Preference key for the user interface locale.
     * <p>
     * The value is a language tag (as produced by {@link Locale#toLanguageTag()}).
     * An empty string means "use the system default locale".
     */
    public static final String LOCALE = "LOCALE";

    /// Preference key for the scale of the whole user interface.
    ///
    /// The value is a factor (1.5 for 150 %); 0 means "automatic" (let the
    /// system and the look and feel decide).
    public static final String UI_SCALE = "UI_SCALE";

    /// Scale factors proposed to the user. 0 stands for "automatic".
    public static final float[] AVAILABLE_UI_SCALES = {0f, 1f, 1.25f, 1.5f, 1.75f, 2f, 2.25f, 2.5f, 2.75f, 3f};

    /**
     * The chosen user interface locale, or {@code null} to use the system
     * default.
     */
    private Locale locale = null;

    /// Scale of the user interface; 0 means "automatic".
    private float uiScale = 0f;

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

    /// Returns the scale of the user interface.
    ///
    /// @return the scale factor (1.5 for 150 %), or 0 for "automatic".
    public float getUiScale() {
        return uiScale;
    }

    /// Sets the scale of the user interface.
    ///
    /// @param uiScale the scale factor (1.5 for 150 %), or 0 for "automatic".
    public void setUiScale(float uiScale) {
        this.uiScale = uiScale;
    }

    public void savetoPreferences() {
        Preferences preferences = JSeshPreferencesRoot.getPreferences();
        preferences.putFloat(UI_SCALE, uiScale);
        preferences.put(LOCALE, locale == null ? "" : locale.toLanguageTag());
    }

    public static ApplicationUIPreferences getFromPreferences() {
        Preferences preferences = JSeshPreferencesRoot.getPreferences();
        ApplicationUIPreferences applicationPreferences = new ApplicationUIPreferences();
        float storedScale = preferences.getFloat(UI_SCALE, 0f);
        // Ignore absurd values (hand-edited preferences...).
        applicationPreferences.setUiScale(storedScale >= 0.5f && storedScale <= 4f ? storedScale : 0f);
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

    /// Applies the interface scale stored in the preferences.
    ///
    /// Sets the `flatlaf.uiScale` system property, which FlatLaf reads when it is
    /// installed: fonts, margins, borders and the look and feel's icons follow it.
    /// It must be called before any Swing component is created (including the
    /// splash screen). An explicit `-Dflatlaf.uiScale=...` on the command line wins.
    /// As for the locale, a change only takes effect on the next startup.
    public static void applyStoredUiScale() {
        float scale = getFromPreferences().getUiScale();
        if (scale > 0 && System.getProperty("flatlaf.uiScale") == null) {
            System.setProperty("flatlaf.uiScale", Float.toString(scale));
        }
    }
}

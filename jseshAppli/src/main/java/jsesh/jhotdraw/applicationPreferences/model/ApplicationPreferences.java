/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.jhotdraw.applicationPreferences.model;

import java.util.prefs.Preferences;

/**
 * Application preferences linked to the UI.
 * <p>
 * Currently, only icon height.
 * <p>
 * TODO: unify with other preferences ??? propose a better mechanism ??
 *
 * @author rosmord
 */
public class ApplicationPreferences {

    public static final String ICON_HEIGHT = "ICON_HEIGHT";

    private int iconHeight = 30;

    public int getIconHeight() {
        return iconHeight;
    }

    public void setIconHeight(int iconHeight) {
        this.iconHeight = iconHeight;
    }

    public void savetoPreferences() {
        Preferences preferences = Preferences.userNodeForPackage(ApplicationPreferences.class);
        preferences.putInt(ICON_HEIGHT, iconHeight);
    }

    public static ApplicationPreferences getFromPreferences() {
        Preferences preferences = Preferences.userNodeForPackage(ApplicationPreferences.class);
        ApplicationPreferences applicationPreferences = new ApplicationPreferences();
        applicationPreferences.setIconHeight(preferences.getInt(ICON_HEIGHT, 30));
        return applicationPreferences;
    }
}

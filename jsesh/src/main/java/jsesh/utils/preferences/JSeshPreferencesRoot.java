/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 * 
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.utils.preferences;

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

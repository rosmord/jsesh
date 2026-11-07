/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

import java.util.prefs.Preferences;

import jsesh.utils.preferences.JSeshPreferencesRoot;

/// Global user setting for the horizontal arrow keys.
///
/// The arrow keys are *logical*, not geometric: they don't depend on the
/// direction or the orientation of the text. Up and Down always go to the
/// previous and next line (or column). Left and Right move backward or forward
/// in the text; by default Right is forward, and users who are used to
/// right-to-left writing systems may want Right to be backward.
///
/// The setting is global (stored in the user preferences), and is read each time
/// a key is used, so a change takes effect immediately.
public final class ArrowKeyPreferences {

    private static final String RIGHT_ARROW_IS_FORWARD = "arrowKeys.rightIsForward";

    private ArrowKeyPreferences() {
    }

    private static Preferences preferences() {
        return JSeshPreferencesRoot.getPreferences();
    }

    /// @return true (the default) if the Right key moves forward and the Left
    /// key backward; false for the opposite.
    public static boolean isRightArrowForward() {
        return preferences().getBoolean(RIGHT_ARROW_IS_FORWARD, true);
    }

    /// @param rightArrowForward true if Right should move forward.
    public static void setRightArrowForward(boolean rightArrowForward) {
        preferences().putBoolean(RIGHT_ARROW_IS_FORWARD, rightArrowForward);
    }

    /// Converts a horizontal arrow key into a direction in the text.
    ///
    /// @param rightKey true for the Right key, false for the Left key.
    /// @return true if the key moves forward in the text.
    public static boolean isForward(boolean rightKey) {
        return rightKey == isRightArrowForward();
    }
}

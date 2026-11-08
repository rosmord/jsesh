/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor;

import java.util.prefs.Preferences;

import jsesh.model.constants.TextDirection;
import jsesh.utils.preferences.JSeshPreferencesRoot;

/// Global user setting for the arrow keys: the direction a new editor starts with.
///
/// The arrow keys are *logical*: they don't depend on the direction or the
/// orientation of the text (see [ArrowKeyMode]). Each editor has its own mode,
/// which the user changes from the toolbar. This preference only gives the
/// direction (left-to-right by default) used when an editor is created.
public final class ArrowKeyPreferences {

    private static final String DEFAULT_DIRECTION = "arrowKeys.defaultDirection";
    /// Key used before the direction was stored: false meant right-to-left.
    private static final String LEGACY_RIGHT_IS_FORWARD = "arrowKeys.rightIsForward";

    private ArrowKeyPreferences() {
    }

    private static Preferences preferences() {
        return JSeshPreferencesRoot.getPreferences();
    }

    /// @return the direction new editors start with (left-to-right by default).
    public static TextDirection getDefaultDirection() {
        Preferences prefs = preferences();
        String stored = prefs.get(DEFAULT_DIRECTION, null);
        if (stored != null) {
            try {
                return TextDirection.valueOf(stored);
            } catch (IllegalArgumentException e) {
                return TextDirection.LEFT_TO_RIGHT;
            }
        }
        return prefs.getBoolean(LEGACY_RIGHT_IS_FORWARD, true)
                ? TextDirection.LEFT_TO_RIGHT : TextDirection.RIGHT_TO_LEFT;
    }

    /// @param direction the direction new editors should start with.
    public static void setDefaultDirection(TextDirection direction) {
        preferences().put(DEFAULT_DIRECTION, direction.name());
    }
}

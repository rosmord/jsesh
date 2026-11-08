/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.editor.arrows;

import java.util.prefs.Preferences;

import jsesh.model.constants.TextDirection;
import jsesh.utils.preferences.JSeshPreferencesRoot;

/// Global user settings for the arrow keys, used when an editor is created:
/// which [ArrowKeyPolicies] it uses, and, for the logical policies, the
/// direction it starts with (left-to-right by default).
///
/// Changing them doesn't affect the editors already open.
public final class ArrowKeyPreferences {

    /// The policy used if none is stored.
    public static final ArrowKeyPolicies DEFAULT_POLICY = ArrowKeyPolicies.LOGICAL_SWITCHABLE;

    private static final String POLICY = "arrowKeys.policy";
    private static final String DEFAULT_DIRECTION = "arrowKeys.defaultDirection";
    /// Key used before the direction was stored: false meant right-to-left.
    private static final String LEGACY_RIGHT_IS_FORWARD = "arrowKeys.rightIsForward";

    private ArrowKeyPreferences() {
    }

    private static Preferences preferences() {
        return JSeshPreferencesRoot.getPreferences();
    }

    /// @return a new policy for an editor, as set in the preferences.
    public static ArrowKeyPolicy createPolicy() {
        return getPolicy().create(getDefaultDirection());
    }

    /// @return the kind of policy new editors use.
    public static ArrowKeyPolicies getPolicy() {
        String stored = preferences().get(POLICY, null);
        if (stored != null) {
            try {
                return ArrowKeyPolicies.valueOf(stored);
            } catch (IllegalArgumentException e) {
                // Policy removed since it was stored.
            }
        }
        return DEFAULT_POLICY;
    }

    /// @param policy the kind of policy new editors should use.
    public static void setPolicy(ArrowKeyPolicies policy) {
        preferences().put(POLICY, policy.name());
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

/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model.constants;

/// The axis of a kerning operator.
///
/// A kerning operator only acts in a context of the same axis: `%hkern` between
/// the elements of a horizontal group, or between quadrats in lines; `%vkern`
/// between the lines of a quadrat, or between quadrats in columns. Elsewhere,
/// it is ignored.
public enum KerningAxis {
    /// `%hkern`
    HORIZONTAL("hkern"),
    /// `%vkern`
    VERTICAL("vkern");

    private final String code;

    private KerningAxis(String code) {
        this.code = code;
    }

    /// The name of the operator in the Manuel de Codage, without the `%`.
    public String getCode() {
        return code;
    }

    /// Is this axis the main axis of the given text orientation?
    public boolean matches(TextOrientation orientation) {
        return orientation.isHorizontal() == (this == HORIZONTAL);
    }
}

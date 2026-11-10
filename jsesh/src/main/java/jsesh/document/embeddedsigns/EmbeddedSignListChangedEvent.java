/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.document.embeddedsigns;

/// Fired by an [EmbeddedSignList] when a sign is added, changed or removed.
///
/// @param code the code of the sign concerned.
public record EmbeddedSignListChangedEvent(String code) {
}

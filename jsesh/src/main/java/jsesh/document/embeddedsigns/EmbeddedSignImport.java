/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.document.embeddedsigns;

import java.util.List;
import java.util.Map;

/// How signs coming from another document (pasted text) fit in a list:
/// built by [EmbeddedSignList#planImport(java.util.Collection)], which
/// changes nothing.
///
/// The caller renames the codes of the incoming text with [#renaming()]
/// (see [jsesh.model.tools.SignCodeRenamer]), inserts it, and only then
/// calls [#commit()], so that a refused insertion adds no sign.
public final class EmbeddedSignImport {

    private final EmbeddedSignList target;
    private final int expectedMaxId;
    private final Map<String, String> renaming;
    private final List<EmbeddedSign> added;

    EmbeddedSignImport(EmbeddedSignList target, int expectedMaxId, Map<String, String> renaming,
            List<EmbeddedSign> added) {
        this.target = target;
        this.expectedMaxId = expectedMaxId;
        this.renaming = Map.copyOf(renaming);
        this.added = List.copyOf(added);
    }

    /// The new code of each incoming code which changes. Codes which keep
    /// their name are not in the map.
    public Map<String, String> renaming() {
        return renaming;
    }

    /// The signs [#commit()] adds to the list, with their new codes.
    public List<EmbeddedSign> addedSigns() {
        return added;
    }

    /// Adds the new signs to the list.
    ///
    /// @throws IllegalStateException if signs were added to the list since
    ///                               this import was planned (the codes would
    ///                               clash).
    public void commit() {
        if (target.getMaxId() != expectedMaxId) {
            throw new IllegalStateException("The embedded sign list changed since the import was planned");
        }
        for (EmbeddedSign s : added) {
            target.put(s);
        }
    }
}

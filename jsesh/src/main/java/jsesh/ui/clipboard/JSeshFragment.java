/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.ui.clipboard;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import jsesh.document.embeddedsigns.EmbeddedSign;
import jsesh.document.embeddedsigns.EmbeddedSignImport;
import jsesh.document.embeddedsigns.EmbeddedSignList;
import jsesh.model.ListOfTopItems;
import jsesh.model.tools.SignCodeRenamer;

/// Copied text which uses signs embedded in its document (`sign1`...),
/// with these signs, so that they can be added to the document it is pasted
/// in ([JSeshPasteFlavors#JSeshFragmentFlavor]).
///
/// Text which uses no embedded sign travels as a plain [ListOfTopItems].
///
/// @param items the copied elements.
/// @param signs the embedded signs the elements use.
public record JSeshFragment(ListOfTopItems items, List<EmbeddedSign> signs) implements Serializable {

    private static final long serialVersionUID = 1L;

    public JSeshFragment {
        Objects.requireNonNull(items, "items");
        signs = List.copyOf(signs);
    }

    /// Prepares the fragment to be pasted in a document whose signs are
    /// `target`: plans the import of the signs (see
    /// [EmbeddedSignList#planImport(java.util.Collection)]) and renames
    /// their codes in [#items()], **in place**.
    ///
    /// Once the items are inserted, call [EmbeddedSignImport#commit()] on the
    /// result to add the new signs to `target`.
    public EmbeddedSignImport adaptTo(EmbeddedSignList target) {
        EmbeddedSignImport plan = target.planImport(signs);
        new SignCodeRenamer(plan.renaming()).rename(items);
        return plan;
    }
}

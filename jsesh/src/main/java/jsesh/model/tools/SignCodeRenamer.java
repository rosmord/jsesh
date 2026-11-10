/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model.tools;

import java.util.Map;

import jsesh.model.Hieroglyph;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementDeepAdapter;

/// Renames sign codes in a piece of model, ligatures included.
///
/// The renaming is simultaneous: with `sign1 → sign2` and `sign2 → sign3`,
/// a `sign1` becomes `sign2`, not `sign3`. Codes which are not in the map
/// are left as they are.
///
/// The elements are changed in place (through [Hieroglyph#setCode(String)]):
/// use it on copies which are not yet in a text model, or the changes are not
/// undoable.
public final class SignCodeRenamer {

    private final Map<String, String> renaming;

    /// @param renaming the new code of each code to rename.
    public SignCodeRenamer(Map<String, String> renaming) {
        this.renaming = Map.copyOf(renaming);
    }

    /// Renames the codes in some elements and in their descendants.
    public void rename(Iterable<? extends ModelElement> elements) {
        if (renaming.isEmpty()) {
            return;
        }
        Visitor visitor = new Visitor();
        for (ModelElement e : elements) {
            e.accept(visitor);
        }
    }

    private class Visitor extends ModelElementDeepAdapter {
        @Override
        public void visitHieroglyph(Hieroglyph h) {
            String newCode = renaming.get(h.getCode());
            if (newCode != null) {
                h.setCode(newCode);
            }
        }
    }
}

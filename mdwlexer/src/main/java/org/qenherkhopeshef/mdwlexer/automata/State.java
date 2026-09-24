/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package org.qenherkhopeshef.mdwlexer.automata;

/// A state identified by a stable, builder-assigned integer.
public record State(int id) {
    public State {
        if (id < 0) {
            throw new IllegalArgumentException("State id must be non-negative");
        }
    }

    /// Short form (`q0`, `q1`...) to keep automaton dumps readable.
    @Override
    public String toString() {
        return "q" + id;
    }
}
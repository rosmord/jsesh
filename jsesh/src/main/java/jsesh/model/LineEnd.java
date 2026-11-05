/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import jsesh.sharedvocabulary.LineAlignment;

/// A top item which ends a line: a [LineBreak] or a [PageBreak]. It carries
/// the alignment of the line it ends.
public interface LineEnd {

    /// The alignment of the line which this item ends.
    LineAlignment getAlignment();

    /// A copy of this item (state included), with another alignment.
    ///
    /// @param alignment the alignment of the copy.
    /// @return the copy.
    TopItem withAlignment(LineAlignment alignment);

    /// The alignment carried by a top item.
    ///
    /// @param item a top item.
    /// @return the alignment if the item is a [LineEnd], else
    ///         [LineAlignment#START].
    static LineAlignment alignmentOf(TopItem item) {
        return item instanceof LineEnd end ? end.getAlignment() : LineAlignment.START;
    }
}

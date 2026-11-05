/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.sharedvocabulary;

/// How a line of text is placed in the width available to it: the
/// *line alignment* (see `00_Documents/documentation/technical_glossary.md`).
///
/// It is written on the line break which ends the line, as in
/// `-![align=center]` (or `-!![align=center]` for a page break). A line break
/// without the option is [#START]. The last line of a text or of a zone, when
/// it has no line break, takes the alignment of the line before it.
///
/// The names are *start* and *end*, not *left* and *right*: in right-to-left
/// text, a [#START] line is on the right.
public enum LineAlignment {
    /// On the start side (left in left-to-right text). The default.
    START("start"),
    /// On the end side (right in left-to-right text).
    END("end"),
    /// Centred.
    CENTER("center"),
    /// The space between the elements is stretched so that the line fills the
    /// whole width.
    JUSTIFY("justify");

    private final String mdcName;

    private LineAlignment(String mdcName) {
        this.mdcName = mdcName;
    }

    /// The value of the `align` option in the Manuel de Codage.
    public String mdcName() {
        return mdcName;
    }

    /// The alignment for a value of the `align` option.
    ///
    /// @param name the value, as written in the Manuel de Codage.
    /// @return the alignment, or [#START] if the name is unknown.
    public static LineAlignment fromMdc(String name) {
        for (LineAlignment a : values()) {
            if (a.mdcName.equals(name)) {
                return a;
            }
        }
        return START;
    }
}

/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.sharedvocabulary;

/// Where an inserted element is placed relative to the main element of an insertion.
///
/// Directions are given relative to the writing direction: "start" and "end" are
/// the left and right sides in left-to-right text, and the opposite in
/// right-to-left text. The corresponding codes are those of the `dir` option of
/// `%insert` in the Manuel de Codage, and of the Unicode insertion operators.
public enum InsertionDirection {
    START("s"),
    END("e"),
    TOP("t"),
    BOTTOM("b"),
    TOP_START("ts"),
    BOTTOM_START("bs"),
    TOP_END("te"),
    BOTTOM_END("be"),
    CENTER("c");

    private final String code;

    private InsertionDirection(String code) {
        this.code = code;
    }

    /// The short code for this direction (e.g. "ts").
    public String getCode() {
        return code;
    }

    /// Is this an insertion from a corner ("ts", "bs", "te", "be")?
    public boolean isOblique() {
        return switch (this) {
            case TOP_START, BOTTOM_START, TOP_END, BOTTOM_END -> true;
            default -> false;
        };
    }

    /// Does this direction have a "start" or "end" component?
    public boolean isHorizontal() {
        return this != TOP && this != BOTTOM && this != CENTER;
    }

    /// Does this direction have a "top" or "bottom" component?
    public boolean isVertical() {
        return this != START && this != END && this != CENTER;
    }

    /// Is the inserted element on the start side (as opposed to the end side)?
    ///
    /// Only meaningful if [#isHorizontal()].
    public boolean isStart() {
        return this == START || this == TOP_START || this == BOTTOM_START;
    }

    /// Is the inserted element on the top side (as opposed to the bottom side)?
    ///
    /// Only meaningful if [#isVertical()].
    public boolean isTop() {
        return this == TOP || this == TOP_START || this == TOP_END;
    }

    /// Does the horizontal enlargement of the main element's frame (`dw`) have an
    /// effect for this direction?
    ///
    /// It does for insertions with a "start" or "end" side, and for center insertions
    /// (see [jsesh.render.layout.insert.InsertionFitter#enlargedFrame]).
    public boolean usesDw() {
        return isHorizontal() || this == CENTER;
    }

    /// Does the vertical enlargement of the main element's frame (`dh`) have an
    /// effect for this direction?
    ///
    /// It does for insertions with a "top" or "bottom" side, and for center insertions.
    public boolean usesDh() {
        return isVertical() || this == CENTER;
    }

    /// Find the direction corresponding to a code.
    ///
    /// @param code one of "s", "e", "t", "b", "ts", "bs", "te", "be", "c"
    /// @return the matching direction, or null if the code is unknown.
    public static InsertionDirection fromCode(String code) {
        for (InsertionDirection d : values()) {
            if (d.code.equals(code)) {
                return d;
            }
        }
        return null;
    }
}

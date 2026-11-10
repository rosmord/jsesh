/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.util.Objects;

import jsesh.sharedvocabulary.LineAlignment;

/// A dynamic tab, `%tab` or `%tab[n=3,align=end]`.
///
/// The tabs of a zone cut its lines into *tab cells*, aligned on *tab stops*
/// shared by all the lines of the zone. The position of a stop is computed
/// from the text: it is pushed by the longest cell which ends there.
///
/// - the **index** is the number of the tab stop the tab jumps to. Stop 0 is
///   the start of the line, and is never written. An index of [#AUTO]
///   means "the index of the previous tab of the line, plus one";
/// - the **alignment** is the alignment of the cell *before* the tab. The
///   cell after the last tab of a line takes the alignment of the line.
///
/// Not to be confused with [AbsoluteTab] (`?N`), from Winglyph, which gives
/// an absolute position.
public class Tab extends TopItem {

    private static final long serialVersionUID = 4527312896154398103L;

    /// The index of a tab which follows the previous one.
    public static final int AUTO = 0;

    /// The tab stop index, ≥ 1, or [#AUTO].
    private final int index;

    /// The alignment of the cell before this tab.
    private final LineAlignment alignment;

    /// @param index the tab stop index, ≥ 1, or [#AUTO] (any value ≤ 0 is
    ///        [#AUTO]).
    /// @param alignment the alignment of the cell before the tab.
    public Tab(int index, LineAlignment alignment) {
        this.index = index > 0 ? index : AUTO;
        this.alignment = Objects.requireNonNull(alignment);
    }

    /// A tab to the next stop, with a cell aligned at the start.
    public Tab() {
        this(AUTO, LineAlignment.START);
    }

    /// @return the tab stop index, ≥ 1, or [#AUTO].
    public int getIndex() {
        return index;
    }

    /// The stop index a tab actually jumps to: its explicit index if it is
    /// greater than the one of the previous tab of the line, else the
    /// previous one plus one.
    ///
    /// @param requested the index written in the tab ([#AUTO], or any value
    ///        ≤ 0, for none).
    /// @param previous the stop index of the previous tab of the line, or 0.
    /// @return the stop index.
    public static int resolveIndex(int requested, int previous) {
        return requested > previous ? requested : previous + 1;
    }

    /// @return true if the index is explicit (`n=...`).
    public boolean hasExplicitIndex() {
        return index != AUTO;
    }

    /// @return the alignment of the cell before this tab.
    public LineAlignment getAlignment() {
        return alignment;
    }

    /// A copy of this tab, with another index.
    public Tab withIndex(int index) {
        Tab t = new Tab(index, alignment);
        copyStateTo(t);
        return t;
    }

    /// A copy of this tab, with another alignment.
    public Tab withAlignment(LineAlignment alignment) {
        Tab t = new Tab(index, alignment);
        copyStateTo(t);
        return t;
    }

    @Override
    public void accept(ModelElementVisitor v) {
        v.visitTab(this);
    }

    @Override
    public int compareToAux(ModelElement e) {
        Tab t = (Tab) e;
        int result = Integer.compare(index, t.index);
        if (result == 0) {
            result = alignment.compareTo(t.alignment);
        }
        if (result == 0) {
            result = getState().compareTo(t.getState());
        }
        return result;
    }

    @Override
    public Tab deepCopy() {
        return withIndex(index);
    }

    @Override
    public HorizontalListElement buildHorizontalListElement() {
        return null;
    }

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        Tab t = (Tab) other;
        return index == t.index && alignment == t.alignment;
    }

    @Override
    public String toString() {
        return "(%tab " + index + " " + alignment + ")";
    }

    /// It structures the line, and keeps the direction of the text.
    @Override
    public boolean followsQuadratDirection() {
        return false;
    }
}

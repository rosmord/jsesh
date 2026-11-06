/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/// The dynamic tabs ([Tab]) of one line of text: the items between two line
/// (or page) breaks of an [ItemSequence].
///
/// The *stop index* of a tab is the index it actually jumps to: its explicit
/// index if it is greater than the one of the previous tab of the line, else
/// the previous one plus one (see [Tab#resolveIndex(int, int)]). The layout
/// uses the same rule (it only differs for tabs after a soft wrap, which it
/// ignores).
public final class TabLine {

    private TabLine() {
    }

    /// The stop index of each item of a line.
    ///
    /// @param line the items of the line.
    /// @return for each item, its stop index if it is a tab, else 0.
    public static int[] stopIndexes(List<? extends ModelElement> line) {
        int[] result = new int[line.size()];
        int previous = 0;
        for (int i = 0; i < line.size(); i++) {
            if (line.get(i) instanceof Tab tab) {
                previous = Tab.resolveIndex(tab.getIndex(), previous);
                result[i] = previous;
            }
        }
        return result;
    }

    /// The stop index of a tab in the text, from the tabs before it on its
    /// line.
    ///
    /// @param tab a tab, in a text or not.
    /// @return its stop index.
    public static int stopIndexOf(Tab tab) {
        List<ModelElement> before = new ArrayList<>();
        ModelElement e = tab.getPreviousSlibing();
        while (e != null && !e.isBreak()) {
            before.add(0, e);
            e = e instanceof EmbeddedModelElement embedded ? embedded.getPreviousSlibing() : null;
        }
        before.add(tab);
        int[] indexes = stopIndexes(before);
        return indexes[indexes.length - 1];
    }

    /// Moves a tab to the next (or previous) tab stop.
    ///
    /// - the other tabs keep their stops when possible. When a tab moves
    ///   forward onto the stop of the next tab, the next one moves forward
    ///   too, and so on;
    /// - a tab can't move back onto (or before) the stop of the previous tab,
    ///   nor before stop 1.
    ///
    /// A tab is written without an index (`%tab`) when it follows the previous
    /// one and was already written so, or is the moved tab; otherwise, it
    /// gets an explicit index.
    ///
    /// @param line the items of the line; they are not modified.
    /// @param position the index of the tab to move in `line`.
    /// @param delta +1 to move it to the next stop, -1 to the previous one.
    /// @return the items of the line after the move (the unchanged items are
    ///         the same objects, the changed tabs are new ones), or empty if
    ///         the tab can't move.
    /// @throws IllegalArgumentException if the item at `position` is not a tab.
    public static Optional<List<TopItem>> moveTab(List<? extends TopItem> line, int position, int delta) {
        if (!(line.get(position) instanceof Tab)) {
            throw new IllegalArgumentException("not a tab: " + line.get(position));
        }
        int[] old = stopIndexes(line);
        int previous = 0;
        for (int i = 0; i < position; i++) {
            if (line.get(i) instanceof Tab) {
                previous = old[i];
            }
        }
        int target = old[position] + delta;
        if (target <= previous) {
            return Optional.empty();
        }
        List<TopItem> result = new ArrayList<>(line);
        for (int i = position; i < line.size(); i++) {
            if (line.get(i) instanceof Tab tab) {
                int index = i == position ? target : Math.max(old[i], previous + 1);
                boolean auto = index == previous + 1 && (i == position || !tab.hasExplicitIndex());
                int written = auto ? Tab.AUTO : index;
                if (written != tab.getIndex()) {
                    result.set(i, tab.withIndex(written));
                }
                previous = index;
            }
        }
        return Optional.of(result);
    }
}

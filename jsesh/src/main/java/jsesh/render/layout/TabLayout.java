/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout;

import java.util.ArrayList;
import java.util.List;

import jsesh.render.layout.LineAccumulator.TabCell;
import jsesh.sharedvocabulary.LineAlignment;

/// Places the tab cells of the lines (or columns) of a zone on their tab
/// stops (see [jsesh.model.Tab]). Shared by [LineLayout] and [ColumnLayout],
/// which call it at the end of the layout, once all lines are placed, and
/// before the alignment of the other lines.
///
/// 1. the stops are computed from the natural size of the cells: a cell
///    which ends at stop *i* pushes it to its start, plus its size, plus the
///    tab gap. A stop is never before the previous one. An empty cell
///    doesn't push anything.
/// 2. each cell is moved, as a block, to its interval: from its start stop to
///    its end stop minus the gap; for the last cell of a line, from its start
///    stop to the end of the table (see [#apply]).
///
/// Positions are along the x axis for lines, the y axis for columns, and are
/// always computed from the start of the line (layout coordinates are left
/// to right: right-to-left text is mirrored when drawn).
final class TabLayout {

    /// A line (or column) with tabs.
    ///
    /// @param line the line, already at its final place.
    /// @param origin the coordinate where the line starts (tab stop 0).
    /// @param lastCellAlignment the alignment of the cell after the last tab
    ///        (the alignment of the line).
    record TabbedLine(LineAccumulator line, double origin, LineAlignment lastCellAlignment) {
    }

    private final boolean vertical;
    private final double gap;

    /// @param vertical true for columns.
    /// @param gap the minimal space between a cell and the next tab stop.
    TabLayout(boolean vertical, double gap) {
        this.vertical = vertical;
        this.gap = gap;
    }

    /// Computes the tab stops of the lines and places their cells.
    ///
    /// The last cell of each line is aligned in the interval which ends at
    /// the end of the table: `availableLength` if it is positive, else the
    /// longest of the tabbed lines and of `untabbedLength`.
    ///
    /// @param lines the lines with tabs.
    /// @param availableLength the length imposed by the parent (the width of
    ///        a zone, the height of a zone in columns), or 0 or less if there
    ///        is none.
    /// @param untabbedLength the length of the longest line without tabs.
    /// @return the position of each tab stop, from the origin of the lines.
    double[] apply(List<TabbedLine> lines, double availableLength, double untabbedLength) {
        List<List<TabCell>> cells = new ArrayList<>();
        int maxIndex = 0;
        for (TabbedLine l : lines) {
            List<TabCell> lineCells = l.line().tabCells();
            cells.add(lineCells);
            for (TabCell c : lineCells) {
                maxIndex = Math.max(maxIndex, c.endTab());
            }
        }
        double[] tabPos = tabPositions(cells, maxIndex);
        double end = availableLength > 0 ? availableLength : untabbedLength;
        if (availableLength <= 0) {
            for (List<TabCell> lineCells : cells) {
                TabCell last = lineCells.get(lineCells.size() - 1);
                end = Math.max(end, tabPos[last.startTab()] + size(last));
            }
        }
        for (int i = 0; i < lines.size(); i++) {
            TabbedLine l = lines.get(i);
            for (TabCell c : cells.get(i)) {
                double start = tabPos[c.startTab()];
                double cellEnd = c.isLast() ? end : tabPos[c.endTab()] - gap;
                double length = Math.max(cellEnd - start, size(c));
                LineAlignment alignment = c.isLast() ? l.lastCellAlignment() : c.alignment();
                LineAccumulator.placeUnits(c.units(), alignment, l.origin() + start, length, vertical);
            }
            l.line().placeTabViews(l.origin(), tabPos, vertical);
        }
        return tabPos;
    }

    /// The position of each tab stop, from 0 to `maxIndex`.
    private double[] tabPositions(List<List<TabCell>> cells, int maxIndex) {
        List<List<TabCell>> endingAt = new ArrayList<>();
        for (int i = 0; i <= maxIndex; i++) {
            endingAt.add(new ArrayList<>());
        }
        for (List<TabCell> lineCells : cells) {
            for (TabCell c : lineCells) {
                if (!c.isLast()) {
                    endingAt.get(c.endTab()).add(c);
                }
            }
        }
        double[] tabPos = new double[maxIndex + 1];
        for (int i = 1; i <= maxIndex; i++) {
            double pos = tabPos[i - 1];
            for (TabCell c : endingAt.get(i)) {
                double size = size(c);
                if (size > 0) {
                    pos = Math.max(pos, tabPos[c.startTab()] + size + gap);
                }
            }
            tabPos[i] = pos;
        }
        return tabPos;
    }

    private double size(TabCell c) {
        return LineAccumulator.unitsLength(c.units(), vertical);
    }
}

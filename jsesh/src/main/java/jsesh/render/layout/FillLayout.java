/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import java.awt.geom.Rectangle2D;

import jsesh.model.RunningAnnotation;
import jsesh.model.Zone;
import jsesh.render.view.MDCView;

/// Stretches the zones which fill their line (`%zone[fill=yes]`, see
/// [jsesh.model.ZoneParameters#fill()]) in the lines (or columns) of a text or
/// a zone. Shared by [LineLayout] and [ColumnLayout].
///
/// The running annotations with `size=fill` (see [jsesh.model.RunningAnnotation])
/// are filling views too: they are simply made longer, as they have no content to
/// lay out again. Unlike zones, they are also stretched in the lines with tabs:
/// [#fillCell(List, double, double)] makes them fill their tab cell.
///
/// 1. Before a view is placed, [#prepare(MDCView)] gives a fill zone its
///    natural length: a zone stretched by a previous layout of the same views
///    (the editor lays the text out again after each edit) must not change
///    where the lines are wrapped.
/// 2. At the end of the layout, before the alignment of the lines,
///    [#fillLine(LineAccumulator, double)] shares the free length of a line
///    equally between its fill zones, and moves the views which follow them.
///    The line is then full, so its alignment has nothing left to do.
/// 3. [#finish()] gives back their natural size to the zones which had been
///    stretched by a previous layout, and were not stretched this time.
///
/// Along the lines of the parent: x for lines, y for columns.
final class FillLayout {

    /// Lengths closer than this (in layout units) are the same.
    static final double EPSILON = 1e-2;

    private final boolean vertical;

    private final ZoneFiller filler;

    /// Are the zones stretched? When the alignment of the lines is ignored,
    /// they are not, but the zones stretched by a previous layout must still
    /// get their natural size back.
    private final boolean stretch;

    /// The zones placed at their natural length by [#prepare(MDCView)], with
    /// the length at which their content is laid out.
    private final Map<MDCView, Double> laidOutLengths = new IdentityHashMap<>();

    /// @param vertical true for columns, false for lines.
    /// @param filler lays out a zone again at a new length.
    /// @param stretch should the fill zones be stretched?
    FillLayout(boolean vertical, ZoneFiller filler, boolean stretch) {
        this.vertical = vertical;
        this.filler = filler;
        this.stretch = stretch;
    }

    /// @return should the fill zones be stretched?
    boolean stretches() {
        return stretch;
    }

    /// Gives a fill zone its natural length before it is placed in its line.
    ///
    /// @param view any view of the text.
    void prepare(MDCView view) {
        if (!view.isFilling()) {
            return;
        }
        double current = length(view);
        if (Math.abs(current - view.getNaturalLength()) > EPSILON) {
            laidOutLengths.put(view, current);
            setLength(view, view.getNaturalLength());
        }
    }

    /// Is the view a running annotation which fills (`size=fill`)?
    private static boolean isFillingAnnotation(MDCView view) {
        return view.isFilling() && view.getModel() instanceof RunningAnnotation;
    }

    /// Gives a filling view a new length: a zone is laid out again, an
    /// annotation is simply made longer.
    private void resize(MDCView view, double length) {
        if (view.getModel() instanceof Zone) {
            filler.fill(view, length);
        } else {
            setLength(view, length);
        }
    }

    /// How far the end of the filling views goes beyond the visible content of
    /// the line. A zone is visible, so this is never positive for it. An
    /// annotation has no length before it is stretched, so it is invisible, and
    /// the space before it (a gap) has to be taken from what it can fill.
    private double overhang(LineAccumulator line) {
        Rectangle2D bounds = vertical ? line.visibleVerticalBounds(0, 0) : line.visibleBounds(0, 0);
        if (bounds == null) {
            return 0;
        }
        double visibleEnd = vertical ? bounds.getMaxY() : bounds.getMaxX();
        double overhang = 0;
        for (MDCView view : line.getViews()) {
            if (isFillingAnnotation(view)) {
                double start = vertical ? view.getPosition().y : view.getPosition().x;
                overhang = Math.max(overhang, start + view.getNaturalLength() - visibleEnd);
            }
        }
        return overhang;
    }

    /// Makes the running annotations which fill (`size=fill`) take the rest of
    /// a tab cell, and moves the views which follow them. The cell is first
    /// moved to its start; the free length is then what is left up to its end,
    /// shared equally between the annotations. Zones which fill are left alone
    /// (they are ignored in the lines with tabs).
    ///
    /// @param units the units of the cell, as the line accumulator holds them.
    /// @param start where the cell starts, in the coordinates of the line.
    /// @param length the length of the cell.
    /// @return true if the cell has an annotation which fills (so that it has
    ///         been placed, and needs no alignment).
    boolean fillCell(List<List<MDCView>> units, double start, double length) {
        if (!stretch) {
            return false;
        }
        List<MDCView> views = units.stream().flatMap(List::stream).toList();
        List<MDCView> filling = views.stream().filter(FillLayout::isFillingAnnotation).toList();
        if (filling.isEmpty()) {
            return false;
        }
        double first = Double.POSITIVE_INFINITY;
        for (MDCView view : views) {
            first = Math.min(first, vertical ? view.getPosition().y : view.getPosition().x);
        }
        for (MDCView view : views) {
            move(view, start - first);
        }
        double end = Double.NEGATIVE_INFINITY;
        for (MDCView view : views) {
            double viewStart = vertical ? view.getPosition().y : view.getPosition().x;
            end = Math.max(end, viewStart + length(view));
        }
        double share = Math.max(0, start + length - end) / filling.size();
        double growth = 0;
        for (MDCView view : views) {
            if (growth != 0) {
                move(view, growth);
            }
            if (filling.contains(view)) {
                laidOutLengths.remove(view);
                double target = view.getNaturalLength() + (share > EPSILON ? share : 0);
                setLength(view, target);
                growth += target - view.getNaturalLength();
            }
        }
        return true;
    }

    /// Shares the free length of a line between its fill zones.
    ///
    /// @param line a placed line (or column), in document coordinates.
    /// @param free the length of the parent's line minus the visible length
    ///        of this one.
    /// @return true if the line has changed.
    boolean fillLine(LineAccumulator line, double free) {
        List<MDCView> views = line.getViews();
        long count = views.stream().filter(MDCView::isFilling).count();
        if (count == 0) {
            return false;
        }
        double share = Math.max(0, free - overhang(line)) / count;
        double growth = 0;
        boolean changed = false;
        for (MDCView view : views) {
            if (growth != 0) {
                move(view, growth);
            }
            if (view.isFilling()) {
                double natural = view.getNaturalLength();
                double target = share > EPSILON ? natural + share : natural;
                Double laidOut = laidOutLengths.remove(view);
                if (laidOut != null && Math.abs(laidOut - target) <= EPSILON) {
                    // Already laid out at this length.
                    setLength(view, laidOut);
                } else if (laidOut != null || target > natural) {
                    resize(view, target);
                }
                double viewGrowth = length(view) - natural;
                changed |= viewGrowth != 0;
                growth += viewGrowth;
            }
        }
        return changed;
    }

    /// Gives back their natural size to the zones placed at their natural
    /// length by [#prepare(MDCView)] and not stretched since.
    void finish() {
        for (MDCView view : laidOutLengths.keySet()) {
            resize(view, view.getNaturalLength());
        }
        laidOutLengths.clear();
    }

    private double length(MDCView v) {
        return vertical ? v.getHeight() : v.getWidth();
    }

    private void setLength(MDCView v, double length) {
        if (vertical) {
            v.setHeight(length);
        } else {
            v.setWidth(length);
        }
    }

    private void move(MDCView v, double delta) {
        if (vertical) {
            v.getPosition().y += delta;
        } else {
            v.getPosition().x += delta;
        }
    }
}

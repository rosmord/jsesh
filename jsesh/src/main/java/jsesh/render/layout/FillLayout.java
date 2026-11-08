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

import jsesh.render.view.MDCView;

/// Stretches the zones which fill their line (`%zone[fill=yes]`, see
/// [jsesh.model.ZoneParameters#fill()]) in the lines (or columns) of a text or
/// a zone. Shared by [LineLayout] and [ColumnLayout].
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
    private final Map<MDCView, Float> laidOutLengths = new IdentityHashMap<>();

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
        float current = length(view);
        if (Math.abs(current - view.getNaturalLength()) > EPSILON) {
            laidOutLengths.put(view, current);
            setLength(view, view.getNaturalLength());
        }
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
        double share = Math.max(0, free) / count;
        double growth = 0;
        boolean changed = false;
        for (MDCView view : views) {
            if (growth != 0) {
                move(view, growth);
            }
            if (view.isFilling()) {
                double natural = view.getNaturalLength();
                double target = share > EPSILON ? natural + share : natural;
                Float laidOut = laidOutLengths.remove(view);
                if (laidOut != null && Math.abs(laidOut - target) <= EPSILON) {
                    // Already laid out at this length.
                    setLength(view, laidOut);
                } else if (laidOut != null || target > natural) {
                    filler.fill(view, target);
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
            filler.fill(view, view.getNaturalLength());
        }
        laidOutLengths.clear();
    }

    private float length(MDCView v) {
        return vertical ? v.getHeight() : v.getWidth();
    }

    private void setLength(MDCView v, float length) {
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

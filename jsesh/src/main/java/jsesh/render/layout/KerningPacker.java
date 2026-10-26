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
import java.util.Optional;

import jsesh.model.Cadrat;
import jsesh.model.HBox;
import jsesh.model.Kerning;
import jsesh.model.constants.KerningAxis;
import jsesh.render.layout.insert.Kerner;
import jsesh.render.view.MDCView;

/// Places the sub views of a view one after another along an axis, honouring
/// the kerning operators found among them.
///
/// Kerning operators take no room and add no space. An operator of the packing
/// axis kerns the elements around it; they then form a rigid *block*, which is
/// moved as a whole when the free space of the parent is distributed. An operator
/// of the other axis is ignored: the elements are placed as if it was not there.
///
/// When there is no kerning operator, each element is a block of its own, and the
/// result is the same as the usual layout.
final class KerningPacker {

    private final Kerner kerner;
    private final KerningAxis axis;
    private final double skip;

    /// @param kerner computes the kerning.
    /// @param axis the axis along which the views are placed.
    /// @param skip the space between two elements, and between kerned signs.
    KerningPacker(Kerner kerner, KerningAxis axis, double skip) {
        this.kerner = kerner;
        this.axis = axis;
        this.skip = skip;
    }

    /// The kerning operator a view stands for, if any: a kerning element, or a
    /// box or quadrat containing only kerning.
    static Optional<KerningAxis> kerningOf(MDCView view) {
        return switch (view.getModel()) {
            case Kerning k -> Optional.of(k.getAxis());
            case HBox b -> b.getKerningAxis();
            case Cadrat c -> c.getKerningAxis();
            default -> Optional.empty();
        };
    }

    /// Does one of the sub views of a view stand for a kerning operator?
    static boolean hasKerning(MDCView view) {
        for (MDCView sub : view.getSubViews()) {
            if (kerningOf(sub).isPresent()) {
                return true;
            }
        }
        return false;
    }

    /// The blocks formed by the sub views of a view: lists of indices of sub
    /// views. Each kerning operator belongs to the block of the element before it
    /// (or of the first element, for leading operators).
    List<List<Integer>> blocks(MDCView parent) {
        List<List<Integer>> blocks = new ArrayList<>();
        List<Integer> current = null;
        List<Integer> leading = new ArrayList<>();
        boolean kern = false;
        for (int i = 0; i < parent.getNumberOfSubviews(); i++) {
            Optional<KerningAxis> k = kerningOf(parent.getSubView(i));
            if (k.isPresent()) {
                if (current == null) {
                    leading.add(i);
                } else {
                    current.add(i);
                    kern |= k.get() == axis;
                }
            } else {
                if (current == null || !kern) {
                    current = new ArrayList<>();
                    blocks.add(current);
                }
                current.add(i);
                kern = false;
                if (!leading.isEmpty()) {
                    current.addAll(0, leading);
                    leading.clear();
                }
            }
        }
        if (!leading.isEmpty()) {
            blocks.add(leading);
        }
        return blocks;
    }

    /// Sets the positions of the sub views along the axis, starting from 0.
    ///
    /// Positions on the other axis must already be set, as the kerning depends on them.
    /// Kerning operators are put in the middle of their gap.
    ///
    /// @param parent the view whose sub views are placed.
    /// @return the extent of the sub views along the axis.
    double pack(MDCView parent) {
        List<MDCView> block = new ArrayList<>();
        List<MDCView> pendingKernings = new ArrayList<>();
        MDCView previous = null;
        double end = 0;
        boolean kern = false;
        for (MDCView v : parent.getSubViews()) {
            Optional<KerningAxis> k = kerningOf(v);
            if (k.isPresent()) {
                kern |= previous != null && k.get() == axis;
                pendingKernings.add(v);
                continue;
            }
            double start = 0;
            if (previous != null) {
                start = end + skip;
                setMain(v, start);
                if (kern) {
                    start -= kerner.shift(block, v, axis, skip);
                } else {
                    block.clear();
                }
            }
            setMain(v, start);
            for (MDCView p : pendingKernings) {
                setMain(p, previous == null ? start : (end + start) / 2);
            }
            pendingKernings.clear();
            block.add(v);
            end = Math.max(end, start + length(v));
            previous = v;
            kern = false;
        }
        for (MDCView p : pendingKernings) {
            setMain(p, end);
        }
        return end;
    }

    /// Distributes blocks over a length, as [MDCView#distributeHorizontally()]
    /// and [MDCView#distributeFromTopToBottom()] do for views: the free space is
    /// shared equally between the blocks, which keep their inner layout.
    ///
    /// @param parent the parent view.
    /// @param blocks its blocks, from [#blocks(MDCView)]; there should be more than one.
    /// @param length the length to fill, in the parent's internal coordinates.
    void distribute(MDCView parent, List<List<Integer>> blocks, double length) {
        double sum = 0;
        for (List<Integer> b : blocks) {
            sum += blockLength(parent, b);
        }
        double interSpace = (length - sum) / (blocks.size() - 1);
        double pos = 0;
        for (List<Integer> b : blocks) {
            moveBlockTo(parent, b, pos);
            pos += blockLength(parent, b) + interSpace;
        }
    }

    /// Moves a block rigidly along the axis, so that it starts at `pos`.
    void moveBlockTo(MDCView parent, List<Integer> block, double pos) {
        double delta = pos - blockStart(parent, block);
        for (int i : block) {
            MDCView v = parent.getSubView(i);
            setMain(v, main(v) + delta);
        }
    }

    /// The length of a block along the axis (that of its elements, not of its kerning operators).
    double blockLength(MDCView parent, List<Integer> block) {
        double start = Double.POSITIVE_INFINITY, end = Double.NEGATIVE_INFINITY;
        for (int i : block) {
            MDCView v = parent.getSubView(i);
            if (kerningOf(v).isEmpty()) {
                start = Math.min(start, main(v));
                end = Math.max(end, main(v) + length(v));
            }
        }
        return start > end ? 0 : end - start;
    }

    private double blockStart(MDCView parent, List<Integer> block) {
        double start = Double.POSITIVE_INFINITY;
        for (int i : block) {
            MDCView v = parent.getSubView(i);
            if (kerningOf(v).isEmpty()) {
                start = Math.min(start, main(v));
            }
        }
        return start == Double.POSITIVE_INFINITY ? main(parent.getSubView(block.get(0))) : start;
    }

    private double main(MDCView v) {
        return axis == KerningAxis.HORIZONTAL ? v.getPosition().x : v.getPosition().y;
    }

    private void setMain(MDCView v, double pos) {
        if (axis == KerningAxis.HORIZONTAL) {
            v.getPosition().x = pos;
        } else {
            v.getPosition().y = pos;
        }
    }

    private double length(MDCView v) {
        return axis == KerningAxis.HORIZONTAL ? v.getWidth() : v.getHeight();
    }
}

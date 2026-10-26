/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.render.layout.insert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jsesh.model.Hieroglyph;
import jsesh.model.ModelElement;
import jsesh.model.SubCadrat;
import jsesh.model.constants.InsertionDirection;
import jsesh.model.constants.TextDirection;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.elements.HieroglyphDrawer;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;

/// Ranks the possible ways of inserting one group into another.
///
/// Used by the editor to choose an insertion automatically: for each direction,
/// and for both choices of the main element, the inserted element is fitted as
/// the layout would do it, and the candidates are sorted, best first:
///
/// 1. candidates where the inserted element fits come first;
/// 2. then those where the main element is the expected one: one usually inserts
///    a group into a sign, and a small sign into a large one, even if the other
///    way gives a slightly larger result. So the main element is preferably the
///    group with fewer signs, or, if they have as many signs, the larger one (by
///    the area of its box);
/// 3. then the largest inserted element;
/// 4. then the groups in the order they were given;
/// 5. then a fixed order of directions: center, top, bottom, start, end, and the corners.
public final class InsertionRanker {

    /// A possible insertion.
    ///
    /// @param direction where the inserted element goes.
    /// @param swapped false if the first group is the main one, true if it's the second.
    /// @param scale the scale of the inserted element, relative to its natural size.
    /// @param fits false if the inserted element overlaps the main one.
    public record Candidate(InsertionDirection direction, boolean swapped, double scale, boolean fits) {
    }

    /// Order of preference between directions, for candidates which are otherwise equal.
    private static final List<InsertionDirection> PREFERENCE = List.of(InsertionDirection.CENTER,
            InsertionDirection.TOP, InsertionDirection.BOTTOM, InsertionDirection.START, InsertionDirection.END,
            InsertionDirection.BOTTOM_START, InsertionDirection.TOP_START, InsertionDirection.BOTTOM_END,
            InsertionDirection.TOP_END);

    /// Scales closer than this are considered equal.
    private static final double SCALE_EPSILON = 1e-6;

    /// The order of the candidates, best first.
    ///
    /// @param firstIsMain is the first group the expected main element?
    private static Comparator<Candidate> order(boolean firstIsMain) {
        Comparator<Candidate> fitsFirst = Comparator.comparing(Candidate::fits).reversed();
        return fitsFirst
                .thenComparing(c -> c.swapped() == firstIsMain)
                .thenComparing((a, b) -> Math.abs(a.scale() - b.scale()) < SCALE_EPSILON ? 0
                        : Double.compare(b.scale(), a.scale()))
                .thenComparing(Candidate::swapped)
                .thenComparingInt(c -> PREFERENCE.indexOf(c.direction()));
    }

    private final JSeshRenderContext renderContext;
    private final JSeshTechRenderContext techRenderContext;

    /// @param renderContext the context in which the text is drawn.
    /// @param techRenderContext the technical context in which the text is drawn.
    public InsertionRanker(JSeshRenderContext renderContext, JSeshTechRenderContext techRenderContext) {
        this.renderContext = renderContext;
        this.techRenderContext = techRenderContext;
    }

    /// Rank all the ways of inserting one group into the other.
    ///
    /// @param first one group.
    /// @param second the other group.
    /// @param dw horizontal enlargement of the main element's frame, see [jsesh.model.Insert#getDw()].
    /// @param dh vertical enlargement of the main element's frame, see [jsesh.model.Insert#getDh()].
    /// @return the 18 candidates (9 directions, both choices of main element), best first.
    public List<Candidate> rank(SubCadrat first, SubCadrat second, int dw, int dh) {
        ViewBuilder builder = new ViewBuilder();
        MDCView firstView = builder.buildView(first, renderContext, techRenderContext);
        MDCView secondView = builder.buildView(second, renderContext, techRenderContext);

        TextDirection textDirection = renderContext.jseshStyle().options().textDirection();
        HieroglyphDrawer drawer = new HieroglyphDrawer(renderContext.hieroglyphShapeRepository());
        ViewSilhouette silhouette = new ViewSilhouette(drawer,
                drawer.scaleFromFontToStyle(renderContext.jseshStyle()), textDirection);
        InsertPlacer placer = new InsertPlacer(silhouette, TextDirection.RIGHT_TO_LEFT.equals(textDirection));

        List<Candidate> result = new ArrayList<>();
        for (InsertionDirection direction : InsertionDirection.values()) {
            result.add(candidate(placer, direction, false, dw, dh, firstView, secondView));
            result.add(candidate(placer, direction, true, dw, dh, secondView, firstView));
        }
        int firstSigns = countSigns(first);
        int secondSigns = countSigns(second);
        boolean firstIsMain = firstSigns != secondSigns ? firstSigns < secondSigns
                : firstView.getWidth() * firstView.getHeight() >= secondView.getWidth() * secondView.getHeight();
        result.sort(order(firstIsMain));
        return result;
    }

    private static int countSigns(ModelElement element) {
        if (element instanceof Hieroglyph) {
            return 1;
        }
        int result = 0;
        for (int i = 0; i < element.getNumberOfChildren(); i++) {
            result += countSigns(element.getChildAt(i));
        }
        return result;
    }

    private static Candidate candidate(InsertPlacer placer, InsertionDirection direction, boolean swapped, int dw,
            int dh, MDCView mainView, MDCView insertedView) {
        InsertionFitter.Placement placement = placer.place(direction, dw, dh, mainView, insertedView).placement();
        return new Candidate(direction, swapped, placement.scale(), placement.fits());
    }
}

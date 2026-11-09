/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.util.Objects;

import jsesh.sharedvocabulary.RunningAnnotationKind;

/// A running annotation, `%haplography[size=200]` or `%lacuna[size=fill]`: a
/// line drawn along the text (horizontal in lines, vertical in columns) to
/// mark a stretch of it.
///
/// - an *haplography* (running variant) is a solid line with an arrow at each
///   end;
/// - a *lacuna* (running lacuna) is a dashed line, without arrows.
///
/// The old forms `^-..-..-^` and `?-..-..-?` are read as these elements.
///
/// The **size** is either a length, in percents of a quadrat (`size=200` is
/// two quadrats long), or `fill`: the annotation then takes the rest of its
/// tab cell, or of its line if there are no tabs (see
/// `jsesh.render.layout.FillLayout`).
public class RunningAnnotation extends TopItem {

    private static final long serialVersionUID = -1747090350718392045L;

    /// The size used when none, or an invalid one, is given: one quadrat.
    public static final int DEFAULT_PERCENT = 100;

    private final RunningAnnotationKind kind;

    /// True for `size=fill`.
    private final boolean fill;

    /// The length in percents of a quadrat (≥ 1). Meaningless if [#fill].
    private final int percent;

    /// A running annotation of a given length.
    ///
    /// @param kind haplography or lacuna.
    /// @param percent the length, in percents of a quadrat. A value which is
    ///        not positive is replaced by [#DEFAULT_PERCENT].
    public RunningAnnotation(RunningAnnotationKind kind, int percent) {
        this.kind = Objects.requireNonNull(kind);
        this.fill = false;
        this.percent = percent > 0 ? percent : DEFAULT_PERCENT;
    }

    private RunningAnnotation(RunningAnnotationKind kind) {
        this.kind = Objects.requireNonNull(kind);
        this.fill = true;
        this.percent = DEFAULT_PERCENT;
    }

    /// A running annotation which fills its tab cell (or its line).
    ///
    /// @param kind haplography or lacuna.
    public static RunningAnnotation filling(RunningAnnotationKind kind) {
        return new RunningAnnotation(kind);
    }

    /// @return haplography or lacuna.
    public RunningAnnotationKind getKind() {
        return kind;
    }

    /// @return true for `size=fill`.
    public boolean isFill() {
        return fill;
    }

    /// @return the length in percents of a quadrat; [#DEFAULT_PERCENT] if
    ///         the annotation fills.
    public int getPercent() {
        return percent;
    }

    @Override
    public void accept(ModelElementVisitor v) {
        v.visitRunningAnnotation(this);
    }

    @Override
    public int compareToAux(ModelElement e) {
        RunningAnnotation a = (RunningAnnotation) e;
        int result = kind.compareTo(a.kind);
        if (result == 0) {
            result = Boolean.compare(fill, a.fill);
        }
        if (result == 0) {
            result = Integer.compare(percent, a.percent);
        }
        if (result == 0) {
            result = getState().compareTo(a.getState());
        }
        return result;
    }

    @Override
    public RunningAnnotation deepCopy() {
        RunningAnnotation copy = fill ? new RunningAnnotation(kind) : new RunningAnnotation(kind, percent);
        copyStateTo(copy);
        return copy;
    }

    @Override
    public HorizontalListElement buildHorizontalListElement() {
        return null;
    }

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        RunningAnnotation a = (RunningAnnotation) other;
        return kind == a.kind && fill == a.fill && percent == a.percent;
    }

    @Override
    public String toString() {
        return "(%" + kind.mdcName() + " " + (fill ? "fill" : percent) + ")";
    }
}

/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import jsesh.model.constants.InsertionDirection;

/// A group inserted into the free space of a sign, as in
/// `%insert[dir=bs,dw=20](D36,(n:t))`.
///
/// The model is inspired by Mark-Jan Nederhof's RES and by the Unicode insertion
/// operators. An `Insert` has two children, both [SubCadrat]s, which may hold any group
/// of signs (including other insertions, as in `%insert[dir=te](%insert[dir=bs](t,w),t)`):
///
/// 1. the *main* element;
/// 2. the *inserted* element.
///
/// The inserted element is placed, and scaled, at layout time, so that it fits in
/// the free space of the main element (see `jsesh.render.layout.insert.InsertionFitter`).
///
/// The main element's frame can be enlarged on the side of the insertion, to leave
/// more room for the inserted element: `dw` and `dh` are percentages of the
/// inserted element's natural width and height.
public class Insert extends InnerGroup {

    private static final long serialVersionUID = 2864279355118206381L;

    private InsertionDirection direction;
    private int dw;
    private int dh;

    /// Create an insertion.
    ///
    /// @param direction where the inserted element goes, relative to the main element.
    /// @param main the main element.
    /// @param inserted the inserted element.
    /// @param dw enlargement of the main element's frame, horizontally, in percent
    ///        of the inserted element's width. Negative values are taken as 0.
    /// @param dh enlargement of the main element's frame, vertically, in percent
    ///        of the inserted element's height. Negative values are taken as 0.
    public Insert(InsertionDirection direction, SubCadrat main, SubCadrat inserted, int dw, int dh) {
        this.direction = direction;
        this.dw = Math.max(0, dw);
        this.dh = Math.max(0, dh);
        addChild(main);
        addChild(inserted);
    }

    /// Create an insertion without enlargement of the main element's frame.
    public Insert(InsertionDirection direction, SubCadrat main, SubCadrat inserted) {
        this(direction, main, inserted, 0, 0);
    }

    /// The main element.
    public SubCadrat getMain() {
        return (SubCadrat) getChildAt(0);
    }

    /// The inserted element.
    public SubCadrat getInserted() {
        return (SubCadrat) getChildAt(1);
    }

    public InsertionDirection getDirection() {
        return direction;
    }

    public void setDirection(InsertionDirection direction) {
        this.direction = direction;
    }

    /// Horizontal enlargement of the main element's frame, in percent of the inserted element's width.
    public int getDw() {
        return dw;
    }

    public void setDw(int dw) {
        this.dw = Math.max(0, dw);
    }

    /// Vertical enlargement of the main element's frame, in percent of the inserted element's height.
    public int getDh() {
        return dh;
    }

    public void setDh(int dh) {
        this.dh = Math.max(0, dh);
    }

    @Override
    public void accept(ModelElementVisitor v) {
        v.visitInsert(this);
    }

    @Override
    public String toString() {
        return "(insert " + direction.getCode() + " " + getChildrenAsString() + ")";
    }

    @Override
    public int compareToAux(ModelElement e) {
        Insert other = (Insert) e;
        int result = direction.compareTo(other.direction);
        if (result == 0) {
            result = Integer.compare(dw, other.dw);
        }
        if (result == 0) {
            result = Integer.compare(dh, other.dh);
        }
        if (result == 0) {
            result = compareContents(e);
        }
        return result;
    }

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        Insert o = (Insert) other;
        return direction == o.direction && dw == o.dw && dh == o.dh;
    }

    @Override
    public Insert deepCopy() {
        return new Insert(direction, getMain().deepCopy(), getInserted().deepCopy(), dw, dh);
    }
}

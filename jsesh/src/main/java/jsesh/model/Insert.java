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
/// of signs (including other insertions, as in `%insert[dir=te](%insert[dir=bs](t,w),t)`).
/// The children are kept in *reading order*, as in the text: the first and the second
/// argument. One of them is the *target* (also called the *main* element), the other one
/// is the *inserted* element. Which is which depends on the `target` option (1 or 2,
/// 1 by default): `%insert[dir=bs](A,B)` and `%insert[dir=bs,target=2](B,A)` are the
/// same insertion of `B` into `A`, but `B` is read first in the second one.
/// Using numbers rather than "before" and "after" keeps the option independent of the
/// writing direction.
///
/// The inserted element is placed, and scaled, at layout time, so that it fits in
/// the free space of the main element (see `jsesh.render.layout.insert.InsertionFitter`).
///
/// The main element's frame can be enlarged on the side of the insertion, to leave
/// more room for the inserted element: `dw` and `dh` are percentages of the
/// inserted element's natural width and height.
public class Insert extends InnerGroup {

    private static final long serialVersionUID = 2864279355118206381L;

    /// Value of the target when the first argument is the target.
    public static final int TARGET_FIRST = 1;
    /// Value of the target when the second argument is the target.
    public static final int TARGET_SECOND = 2;

    private InsertionDirection direction;
    private int target;
    private int dw;
    private int dh;

    /// Create an insertion.
    ///
    /// @param direction where the inserted element goes, relative to the target.
    /// @param first the first argument (in reading order).
    /// @param second the second argument (in reading order).
    /// @param target which argument is the target: [#TARGET_FIRST] or [#TARGET_SECOND].
    ///        The other argument is the inserted element.
    /// @param dw enlargement of the target's frame, horizontally, in percent
    ///        of the inserted element's width. Negative values are taken as 0.
    /// @param dh enlargement of the target's frame, vertically, in percent
    ///        of the inserted element's height. Negative values are taken as 0.
    /// @throws IllegalArgumentException if the target is neither 1 nor 2.
    public Insert(InsertionDirection direction, SubCadrat first, SubCadrat second, int target, int dw, int dh) {
        this.direction = direction;
        this.target = checkTarget(target);
        this.dw = Math.max(0, dw);
        this.dh = Math.max(0, dh);
        addChild(first);
        addChild(second);
    }

    /// Create an insertion where the first argument is the target.
    ///
    /// @param direction where the inserted element goes, relative to the main element.
    /// @param main the main element, which is also the first argument.
    /// @param inserted the inserted element, which is also the second argument.
    /// @param dw enlargement of the main element's frame, horizontally, in percent
    ///        of the inserted element's width. Negative values are taken as 0.
    /// @param dh enlargement of the main element's frame, vertically, in percent
    ///        of the inserted element's height. Negative values are taken as 0.
    public Insert(InsertionDirection direction, SubCadrat main, SubCadrat inserted, int dw, int dh) {
        this(direction, main, inserted, TARGET_FIRST, dw, dh);
    }

    private static int checkTarget(int target) {
        if (target != TARGET_FIRST && target != TARGET_SECOND) {
            throw new IllegalArgumentException("insert target must be 1 or 2, not " + target);
        }
        return target;
    }

    /// Create an insertion without enlargement of the main element's frame.
    public Insert(InsertionDirection direction, SubCadrat main, SubCadrat inserted) {
        this(direction, main, inserted, 0, 0);
    }

    /// The first argument, in reading order.
    public SubCadrat getFirst() {
        return (SubCadrat) getChildAt(0);
    }

    /// The second argument, in reading order.
    public SubCadrat getSecond() {
        return (SubCadrat) getChildAt(1);
    }

    /// The main element (the target): the one the other element is inserted into.
    public SubCadrat getMain() {
        return (SubCadrat) getChildAt(target - 1);
    }

    /// The inserted element.
    public SubCadrat getInserted() {
        return (SubCadrat) getChildAt(2 - target);
    }

    /// Which argument is the target: [#TARGET_FIRST] or [#TARGET_SECOND].
    public int getTarget() {
        return target;
    }

    /// Changes which argument is the target.
    ///
    /// @param target [#TARGET_FIRST] or [#TARGET_SECOND].
    /// @throws IllegalArgumentException if the target is neither 1 nor 2.
    public void setTarget(int target) {
        this.target = checkTarget(target);
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
        return "(insert " + direction.getCode() + (target == TARGET_FIRST ? "" : " target=" + target) + " "
                + getChildrenAsString() + ")";
    }

    @Override
    public int compareToAux(ModelElement e) {
        Insert other = (Insert) e;
        int result = direction.compareTo(other.direction);
        if (result == 0) {
            result = Integer.compare(target, other.target);
        }
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
        return direction == o.direction && target == o.target && dw == o.dw && dh == o.dh;
    }

    @Override
    public Insert deepCopy() {
        return new Insert(direction, getFirst().deepCopy(), getSecond().deepCopy(), target, dw, dh);
    }
}

/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 *
 * contributor(s) : Serge J. P. Thomas for the fonts
 * serge.rosmorduc@qenherkhopeshef.org
 *
 * Created on 31 oct. 2004
 */
package jsesh.document.caret;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsesh.model.CaretGranularity;
import jsesh.model.MDCMark;
import jsesh.model.ModelElement;
import jsesh.model.MDCPosition;
import jsesh.model.HieroglyphicText;
import jsesh.model.PositionNavigator;
import jsesh.model.TextScope;


/// The caret represent the editing position in a text.
/// 
/// It takes in account both
/// the *insertion point* where new signs will be inserted, and an
/// optional *mark.* The space between the mark (if any) and the
/// insertion point is the selected area.
/// 
/// The caret is an **observable** object.
/// 
/// Note that both insert and mark can be null.
/// 
/// **IMPORTANT:** we have just had a problem with a case where a text was changed, but not the caret.
/// We could a) move the HieroglyphicTextModel class to the same package as caret, or maybe even to "model"
/// and b) set up an observer system between the two. Another solution : if marks are immutable (in the sense that
/// one can't move them except by changing the text), there is no need for mark listeners. 
/// Caret can still change, but in this case, we are in control.
/// 
///  
/// TODO: use named marks, in order to avoid any memory leak problem.
/// 

public class MDCCaret {

	private MDCMark insert;

	private MDCMark mark;

	private List<MDCCaretChangeListener> listeners;

	/**
	 * @param insert
	 */
	public MDCCaret(MDCMark insert) {
		mark = null;
		listeners = new ArrayList<MDCCaretChangeListener>();
		setInsert(insert);
	}

	/**
	 * @param model
	 */
	public MDCCaret(HieroglyphicText model) {
		this(new MDCMark(model, new MDCPosition(0)));
	}

	/**
	 * Returns a caret which selects the whole text.
	 * @param model
	 * @return
	 */
	public static MDCCaret buildWholeTextCaret(HieroglyphicText model) {
		MDCCaret result= new MDCCaret(model);
		result.setMarkPosition(model.getLastPosition());
		return result;
	}
	
	public MDCMark getInsert() {
		return insert;
	}

	/// The insertion position, **as a top-level position**. 
	/// 
	/// If the insertion point lies inside a quadrat (see [#isInsertInner()]), this is the position just
	/// before this quadrat: all editing operations work at this level.
	///
	/// @return a top-level position.
	public MDCPosition getInsertPosition() {
		return insert.getTopLevelPosition();
	}

	/// The mark position, as a top-level position (see [#getInsertPosition()]).
	///
	/// @return a top-level position.
	public MDCPosition getMarkPosition() {
		return mark.getTopLevelPosition();
	}

	/// @return the exact insertion position, which may be nested if the
	///         insertion point lies inside a quadrat.
	public MDCPosition getInsertExactPosition() {
		return insert.getPosition();
	}

	/// Does the insertion point lie inside a quadrat.
	/// 
	/// @return true if the insertion point lies inside a quadrat, and not in a
	///         gap of its scope (between two top-level items, or between two
	///         quadrats of a cartouche...).
	public boolean isInsertInner() {
		return !getInsertScope().isGap(insert.getPosition());
	}

	/// Moves the insertion point by a number of stops.
	/// See [PositionNavigator]) for more details. The result is limited by the text bounds.
	///
	/// @param delta the number of stops to move by (may be negative).
	/// @param granularity the kind of stops: with
	///        [CaretGranularity#QUADRAT], the cursor stops between quadrats, in
	///        the text and in the cartouches; with [CaretGranularity#INNER],
	///        inside the quadrats too.
	public void moveInsert(int delta, CaretGranularity granularity) {
		setInsertPosition(new PositionNavigator(getModel()).move(insert.getPosition(), delta, granularity));
	}

	/// Brings the insertion point to a place where the cursor may stand in
	/// [CaretGranularity#QUADRAT] mode.
	///
	/// If the insertion point lies inside a quadrat (see [#isInsertInner()]),
	/// it moves to the gap of its scope just before this quadrat. If the scope
	/// is not reachable at this granularity (the content of a sub-quadrat), the
	/// insertion point moves out of it, to the gap before the quadrat which
	/// holds it. Does nothing otherwise.
	public void snapToScopeLevel() {
		TextScope scope = getInsertScope();
		MDCPosition target = scope.positionAt(scope.localIndexOf(insert.getPosition()));
		while (!scope.isRoot() && !isReachableScope(scope)) {
			int index = scope.enclosing(getModel()).localIndexOf(MDCPosition.of(scope.getPath()));
			scope = scope.enclosing(getModel());
			target = scope.positionAt(index);
		}
		if (!target.equals(insert.getPosition())) {
			setInsertPosition(target);
		}
	}

	private boolean isReachableScope(TextScope scope) {
		return scope.resolve(getModel()) instanceof ModelElement e && e.isCursorStop(0, CaretGranularity.QUADRAT);
	}

	// ---- scopes ----

	/// @return the innermost scope (the text, the content of a cartouche...)
	///         which contains the insertion point.
	public TextScope getInsertScope() {
		return TextScope.containing(getModel(), insert.getPosition());
	}

	/// The scope where editing takes place: the scope of the insertion point,
	/// or, if there is a selection, the innermost scope which contains both its
	/// ends. For instance, a selection from inside a cartouche to the outside
	/// of it is a selection in the text, which includes the whole cartouche.
	///
	/// @return the current scope.
	public TextScope getScope() {
		if (hasMark() && !mark.getPosition().equals(insert.getPosition())) {
			return TextScope.common(getModel(), insert.getPosition(), mark.getPosition());
		}
		return getInsertScope();
	}

	/// The insertion point, as a local position of [#getScope()].
	///
	/// Without selection, if the insertion point lies inside a quadrat, this
	/// is the gap before the quadrat. With a selection, the position is
	/// rounded outwards, so that the selection covers the whole items in
	/// which its ends lie.
	///
	/// @return a local position (one-level).
	public MDCPosition getLocalInsertPosition() {
		return localPosition(insert, mark);
	}

	/// The mark, as a local position of [#getScope()] (see
	/// [#getLocalInsertPosition()]).
	///
	/// @return a local position; the local insert position if there is no mark.
	public MDCPosition getLocalMarkPosition() {
		if (mark == null) {
			return getLocalInsertPosition();
		}
		return localPosition(mark, insert);
	}

	/// @return the start of the selection, as a local position of
	///         [#getScope()] (the local insert position if there is no
	///         selection).
	public MDCPosition getLocalMinPosition() {
		if (!hasSelection()) {
			return getLocalInsertPosition();
		}
		MDCPosition a = getLocalInsertPosition();
		MDCPosition b = getLocalMarkPosition();
		return a.compareTo(b) <= 0 ? a : b;
	}

	/// @return the end of the selection, as a local position of [#getScope()].
	public MDCPosition getLocalMaxPosition() {
		if (!hasSelection()) {
			return getLocalInsertPosition();
		}
		MDCPosition a = getLocalInsertPosition();
		MDCPosition b = getLocalMarkPosition();
		return a.compareTo(b) >= 0 ? a : b;
	}

	/// Moves the insertion point to a local position of [#getScope()].
	///
	/// @param local a local position.
	public void setLocalInsertPosition(MDCPosition local) {
		setInsertPosition(getScope().positionAt(local.getIndex()));
	}

	/// Sets the mark at a local position of [#getScope()].
	///
	/// @param local a local position.
	public void setLocalMarkPosition(MDCPosition local) {
		setMarkPosition(getScope().positionAt(local.getIndex()));
	}

	/// The local position of `end` in the current scope; rounded outwards with
	/// respect to `other` (the other end of the selection) if there is one.
	private MDCPosition localPosition(MDCMark end, MDCMark other) {
		TextScope scope = getScope();
		MDCPosition p = end.getPosition();
		boolean upper = other != null && hasSelection() && p.compareTo(other.getPosition()) > 0;
		return new MDCPosition(upper ? scope.localIndexAfter(p) : scope.localIndexOf(p));
	}
	
	public void setInsert(MDCMark insert) {
		/*
		if (this.insert != null) {
			this.insert.removeMarkChangeListener(this);
		}*/
		if (this.insert != null)
			this.insert.release();
		this.insert = insert;
		/*if (insert != null)
			insert.addMarkChangeListener(this);*/
		notifyCaretListeners();
	}

	public void changeModel(HieroglyphicText model) {
		// We want to generate only one caretChanged event. Hence, we delete the mark by hand.
		if (mark != null)
			mark.release();
		mark= null;
		setInsert(new MDCMark(model, new MDCPosition(0)));
	}
	
	public MDCMark getMark() {
		return mark;
	}

	public void setMark(MDCMark newMark) {
		/*
		if (mark != null) {
			mark.removeMarkChangeListener(this);
		}*/
		if (mark != null)
			mark.release();
		this.mark = newMark;
		/*if (mark != null)
			mark.addMarkChangeListener(this);*/
		notifyCaretListeners();
	}

	public void unsetMark() {
		setMark(null);
	}

	/** 
	 * @return the model
	 */
	public HieroglyphicText getModel() {
		return insert.getHieroglyphicText();
	}

	/// Moves the insertion location by a certain amount of elements. The
	/// result is clamped to the text bounds.
	///
	/// @param delta the number of positions to move by (may be negative).
	public void moveInsertBy(int delta) {
		setInsertPosition(insert.getNextPosition(delta));
	}

	/// Sets the mark at a given position (clamped to the text bounds).
	///
	/// @param p the new mark position.
	public void setMarkPosition(MDCPosition p) {
		setMark(new MDCMark(getModel(), p));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.model.MDCMarkChangeListener#markChanged(jsesh.model.MDCMark)
	 *
	public void markChanged(MDCMark mark) {
		notifyCaretListeners();
	}*/

	private void notifyCaretListeners() {
		for (Iterator<MDCCaretChangeListener> i = listeners.iterator(); i.hasNext();) {
			MDCCaretChangeListener l = i.next();
			l.caretChanged(this);
		}
	}

	public void addCaretChangeListener(MDCCaretChangeListener l) {
		listeners.add(l);
	}

	public void removeCaretChangeListener(MDCCaretChangeListener l) {
		listeners.remove(l);
	}

	/**
	 * @return true if a mark is set.
	 */
	public boolean hasMark() {
		return (mark != null);
	}

	/// Returns the minimal position for the caret range (the insert position
	/// if there is no selection), as a top-level position. Ends of the
	/// selection which lie inside top items are rounded outwards.
	///
	/// @return the minimal position for the caret range.
	public MDCPosition getMinPosition() {
		if (!hasSelection()) {
			return getInsertPosition();
		}
		MDCPosition a = insert.getPosition();
		MDCPosition b = mark.getPosition();
		return (a.compareTo(b) <= 0 ? a : b).getTopLevelPosition();
	}

	/// Returns the maximal position for the caret range (the insert position
	/// if there is no selection), as a top-level position (see
	/// [#getMinPosition()]).
	///
	/// @return the maximal position for the caret range.
	public MDCPosition getMaxPosition() {
		if (!hasSelection()) {
			return getInsertPosition();
		}
		MDCPosition a = insert.getPosition();
		MDCPosition b = mark.getPosition();
		return TextScope.ROOT.positionAt(TextScope.ROOT.localIndexAfter(a.compareTo(b) >= 0 ? a : b));
	}

	/// True if some text is selected. This means that
	///
	/// 1. there is a mark;
	/// 2. the selection, once its ends are brought to the scope which contains
	///    them both (see [#getScope()]), is not empty.
	///
	/// Two different positions inside the same quadrat select this quadrat.
	///
	/// @return true if some text is selected
	public boolean hasSelection() {
		if (!hasMark()) {
			return false;
		}
		MDCPosition a = insert.getPosition();
		MDCPosition b = mark.getPosition();
		if (a.equals(b)) {
			return false;
		}
		TextScope scope = TextScope.common(getModel(), a, b);
		MDCPosition min = a.compareTo(b) <= 0 ? a : b;
		MDCPosition max = a.compareTo(b) <= 0 ? b : a;
		return scope.localIndexOf(min) != scope.localIndexAfter(max);
	}

	public void setInsertPosition(MDCPosition p) {
		MDCMark newInsert= new MDCMark(getModel(), p);
		setInsert(newInsert);
	}

}
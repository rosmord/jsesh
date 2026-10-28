/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.document;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jsesh.model.HieroglyphicText;
import jsesh.model.ItemSequence;
import jsesh.model.MDCPosition;
import jsesh.model.TextScope;
import jsesh.model.TopItem;
import jsesh.parser.MDCSyntaxError;

/// A view of one scope of a [HieroglyphicTextModel] (the text itself, or the
/// content of a cartouche...) as if it were a whole text.
///
/// All positions given to, or returned by, this class are **local**: they are
/// one-level [MDCPosition]s, which designate the gaps of the scope (`(0)`
/// before its first item...). Editing commands go through the model, and are
/// undoable as usual.
///
/// This lets the editor run the same code at the top level of the text and
/// inside a cartouche. For the root scope, the methods are those of
/// [HieroglyphicTextModel] and [HieroglyphicText].
///
/// Insertions of items which the scope can't hold (a line break in a
/// cartouche, see [ItemSequence#accepts(TopItem)]) are ignored: the methods
/// return false and change nothing.
///
/// An instance is a short-lived object: create it for the current scope when
/// needed.
public final class EditingScope {

	private final HieroglyphicTextModel model;
	private final TextScope scope;

	/// @param model the text model.
	/// @param scope a scope of its text.
	public EditingScope(HieroglyphicTextModel model, TextScope scope) {
		this.model = model;
		this.scope = scope;
	}

	/// @return the scope.
	public TextScope getScope() {
		return scope;
	}

	/// @return true for the text itself.
	public boolean isRoot() {
		return scope.isRoot();
	}

	/// @return the sequence of items of the scope (not a copy).
	public ItemSequence getSequence() {
		return scope.resolve(model.getHieroglyphicText());
	}

	private int size() {
		return getSequence().getNumberOfChildren();
	}

	/// @param local a local position.
	/// @return the corresponding position in the whole text.
	public MDCPosition toTextPosition(MDCPosition local) {
		return scope.positionAt(local.getIndex());
	}

	/// @param items some items.
	/// @return true if they can all be placed in this scope.
	public boolean accepts(List<? extends TopItem> items) {
		return getSequence().acceptsAll(items);
	}

	// ---- positions ----

	/// @return the position before the first item.
	public MDCPosition getFirstPosition() {
		return new MDCPosition(0);
	}

	/// @return the position after the last item.
	public MDCPosition getLastPosition() {
		return new MDCPosition(size());
	}

	/// @param k a gap index.
	/// @return the position, clamped to the bounds of the scope.
	public MDCPosition buildPosition(int k) {
		return new MDCPosition(Math.min(Math.max(0, k), size()));
	}

	/// @param position a local position.
	/// @param delta a number of items (may be negative).
	/// @return the position `delta` gaps after `position`, clamped.
	public MDCPosition getNextPosition(MDCPosition position, int delta) {
		return buildPosition(position.getIndex() + delta);
	}

	/// @param position a local position.
	/// @return true if there is an item after it.
	public boolean hasNext(MDCPosition position) {
		return position.getIndex() < size();
	}

	/// @param position a local position.
	/// @return the item after it (not a copy), or null.
	public TopItem getElementAfter(MDCPosition position) {
		int i = position.getIndex();
		return i < size() ? getSequence().getItemAt(i) : null;
	}

	/// @param position a local position.
	/// @return the item before it (not a copy), or null.
	public TopItem getElementBefore(MDCPosition position) {
		int i = position.getIndex();
		return i > 0 && i <= size() ? getSequence().getItemAt(i - 1) : null;
	}

	/// @param position a local position.
	/// @return a copy of the item before it, or null.
	public TopItem getItemBefore(MDCPosition position) {
		TopItem item = getElementBefore(position);
		return item == null ? null : (TopItem) item.deepCopy();
	}

	/// @param pos1 one limit.
	/// @param pos2 the other limit (the order is not important).
	/// @return a **copy** of the items between the two limits.
	public List<TopItem> getTopItemsBetween(MDCPosition pos1, MDCPosition pos2) {
		int a = clamp(Math.min(pos1.getIndex(), pos2.getIndex()));
		int b = clamp(Math.max(pos1.getIndex(), pos2.getIndex()));
		return getSequence().getItemsBetween(a, b);
	}

	private int clamp(int k) {
		return Math.min(Math.max(0, k), size());
	}

	// ---- lines and pages ----

	/// @param position a local position.
	/// @return the position before the first item of the line containing it.
	///         Lines are separated by line and page breaks.
	public MDCPosition getLineFirstPosition(MDCPosition position) {
		if (isRoot()) {
			return model.getHieroglyphicText().getLineFirstPosition(position);
		}
		int i = clamp(position.getIndex());
		while (i > 0 && !getSequence().getItemAt(i - 1).isBreak()) {
			i--;
		}
		return new MDCPosition(i);
	}

	/// @param position a local position.
	/// @return the position after the last item of the line containing it.
	public MDCPosition getLineLastPosition(MDCPosition position) {
		if (isRoot()) {
			return model.getHieroglyphicText().getLineLastPosition(position);
		}
		int i = clamp(position.getIndex());
		while (i < size() && !getSequence().getItemAt(i).isBreak()) {
			i++;
		}
		return new MDCPosition(i);
	}

	/// @param position a local position.
	/// @return the first position of the previous line, or empty if
	///         `position` is on the first line of the scope.
	public Optional<MDCPosition> getUpPosition(MDCPosition position) {
		MDCPosition lineStart = getLineFirstPosition(position);
		if (lineStart.getIndex() == 0) {
			return Optional.empty();
		}
		return Optional.of(getLineFirstPosition(new MDCPosition(lineStart.getIndex() - 1)));
	}

	/// @param position a local position.
	/// @return the first position of the next line, or empty if `position` is
	///         on the last line of the scope.
	public Optional<MDCPosition> getDownPosition(MDCPosition position) {
		MDCPosition lineEnd = getLineLastPosition(position);
		if (lineEnd.getIndex() >= size()) {
			return Optional.empty();
		}
		return Optional.of(new MDCPosition(lineEnd.getIndex() + 1));
	}

	/// @param position a local position.
	/// @return the limits of the line around it.
	public List<MDCPosition> getLineLimitsAround(MDCPosition position) {
		return List.of(getLineFirstPosition(position), getLineLastPosition(position));
	}

	/// @param position a local position.
	/// @return the limits of the page around it. A nested scope is a single page.
	public List<MDCPosition> getPageLimitsAround(MDCPosition position) {
		if (isRoot()) {
			return model.getPageLimitsAround(position);
		}
		return List.of(getFirstPosition(), getLastPosition());
	}

	// ---- editing ----

	/// Inserts items.
	///
	/// @param position a local position.
	/// @param elements the items.
	/// @return false if the scope can't hold them (nothing is done then).
	public boolean insertElementsAt(MDCPosition position, List<TopItem> elements) {
		if (!accepts(elements)) {
			return false;
		}
		model.insertElementsAt(scope, position.getIndex(), elements);
		return true;
	}

	/// @param position a local position.
	/// @param item the item to insert.
	/// @return false if the scope can't hold it.
	public boolean insertElementAt(MDCPosition position, TopItem item) {
		return insertElementsAt(position, Collections.singletonList(item));
	}

	/// Inserts elements typed by the user: successive typed insertions are
	/// undone together, one word at a time.
	///
	/// @param position a local position.
	/// @param elements the typed elements.
	/// @return false if the scope can't hold them.
	public boolean insertTypedElementsAt(MDCPosition position, List<TopItem> elements) {
		if (!accepts(elements)) {
			return false;
		}
		model.insertTypedElementsAt(scope, position.getIndex(), elements);
		return true;
	}

	/// Replaces the items between two positions (in any order).
	///
	/// @param pos1 one limit.
	/// @param pos2 the other limit.
	/// @param newElements the new items.
	/// @return false if the scope can't hold them.
	public boolean replaceElement(MDCPosition pos1, MDCPosition pos2, List<TopItem> newElements) {
		if (!accepts(newElements)) {
			return false;
		}
		model.replaceElement(scope, pos1.getIndex(), pos2.getIndex(), newElements);
		return true;
	}

	/// @see #replaceElement(MDCPosition, MDCPosition, List)
	public boolean replaceElement(MDCPosition pos1, MDCPosition pos2, TopItem element) {
		return replaceElement(pos1, pos2, Collections.singletonList(element));
	}

	/// Replaces the item before a position.
	///
	/// @param position the position *after* the item to replace.
	/// @param newElement the new item.
	/// @return false if the scope can't hold it.
	public boolean replaceElementBefore(MDCPosition position, TopItem newElement) {
		return replaceElement(position.getPreviousPosition(1), position, newElement);
	}

	/// Removes the items between two positions (in any order).
	///
	/// @param pos1 one limit.
	/// @param pos2 the other limit.
	public void removeElements(MDCPosition pos1, MDCPosition pos2) {
		model.removeElements(scope, pos1.getIndex(), pos2.getIndex());
	}

	/// Inserts some Manuel de Codage text.
	///
	/// @param position a local position.
	/// @param mdcText the text.
	/// @return false if the scope can't hold the resulting items.
	/// @throws MDCSyntaxError if the text is not correct.
	public boolean insertMDCText(MDCPosition position, String mdcText) throws MDCSyntaxError {
		return insertElementsAt(position, model.buildItems(mdcText));
	}

	/// Replaces a part of the scope with some Manuel de Codage text.
	///
	/// @param start one limit.
	/// @param end the other limit.
	/// @param mdcText the new text.
	/// @return false if the scope can't hold the resulting items.
	/// @throws MDCSyntaxError if the text is not correct.
	public boolean replaceWithMDCText(MDCPosition start, MDCPosition end, String mdcText) throws MDCSyntaxError {
		return replaceElement(start, end, model.buildItems(mdcText));
	}
}

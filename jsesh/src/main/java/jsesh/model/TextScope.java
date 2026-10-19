/*
 * Copyright (c) 2026 Serge Rosmorduc/Conservatoire National des Arts et Métiers, Paris, France
 * SPDX-License-Identifier: CECILL-C
 *
 * This file is licensed under the CeCILL-C Free Software License, version 1.1.
 * Full text available at: https://cecill.info/licences/Licence_CeCILL-C_V1-en.html
 */
package jsesh.model;

import java.util.Arrays;

/// An editing scope: an [ItemSequence] of a text, designated by its path.
///
/// The root scope is the text itself. The content of a cartouche, or of a
/// sub-quadrat, is a nested scope: a text in miniature, which can be edited
/// like the top level of the text.
///
/// The path of a scope is the list of child indices leading from the text to
/// the sequence. It is empty for the root. The positions *of* a scope are then
/// this path followed by a gap index in the sequence. For instance, in
/// `<-A1-B1->`, the content of the cartouche is the scope `[0, 0, 0, 0]`
/// (quadrat 0, hbox 0, cartouche 0, its list), and `(0,0,0,0,1)` is the
/// position between `A1` and `B1`.
///
/// Like a [MDCPosition], a scope is a plain immutable value, which doesn't hold
/// a reference to the text.
public final class TextScope {

	/// The text itself.
	public static final TextScope ROOT = new TextScope(new int[0]);

	private final int[] path;

	private TextScope(int[] path) {
		this.path = path;
	}

	/// @param path the child indices leading to the sequence.
	/// @return the scope.
	public static TextScope of(int... path) {
		return path.length == 0 ? ROOT : new TextScope(path.clone());
	}

	/// The innermost scope containing a position.
	///
	/// @param text the text.
	/// @param position a position, possibly nested. As much of its path as matches
	///        the text is used.
	/// @return the innermost scope in which `position` lies, either directly (a
	///         gap of the sequence) or deeper, inside one of its items.
	public static TextScope containing(HieroglyphicText text, MDCPosition position) {
		int[] p = position.getPath();
		ModelElement current = text;
		int depth = 0;
		for (int d = 0; d < p.length - 1; d++) {
			if (p[d] >= current.getNumberOfChildren()) {
				break;
			}
			current = current.getChildAt(p[d]);
			if (current instanceof ItemSequence) {
				depth = d + 1;
			}
		}
		return of(Arrays.copyOf(p, depth));
	}

	/// @return true for the text itself.
	public boolean isRoot() {
		return path.length == 0;
	}

	/// @return the length of the path (0 for the root).
	public int depth() {
		return path.length;
	}

	/// @return a copy of the path.
	public int[] getPath() {
		return path.clone();
	}

	/// The sequence designated by this scope in a text.
	///
	/// @param text the text.
	/// @return the sequence.
	/// @throws IllegalArgumentException if the path does not lead to a sequence of
	///         `text`.
	public ItemSequence resolve(HieroglyphicText text) {
		ModelElement current = text;
		for (int i : path) {
			if (i >= current.getNumberOfChildren()) {
				throw new IllegalArgumentException("No scope " + this + " in this text");
			}
			current = current.getChildAt(i);
		}
		if (!(current instanceof ItemSequence sequence)) {
			throw new IllegalArgumentException(this + " is not a sequence of items");
		}
		return sequence;
	}

	/// @param index a gap index in the sequence.
	/// @return the (full) position of this gap in the text.
	public MDCPosition positionAt(int index) {
		int[] p = Arrays.copyOf(path, path.length + 1);
		p[path.length] = index;
		return MDCPosition.of(p);
	}

	/// Does a position lie in this scope, either in one of its gaps, or inside
	/// one of its items?
	///
	/// @param position a position.
	/// @return true if the path of `position` extends the path of this scope.
	public boolean contains(MDCPosition position) {
		int[] p = position.getPath();
		return p.length > path.length && Arrays.equals(path, 0, path.length, p, 0, path.length);
	}

	/// The gap of this scope at, or just before, a position.
	///
	/// @param position a position contained in this scope (see
	///        [#contains(MDCPosition)]). If it lies inside an item of the scope,
	///        the result is the gap before this item.
	/// @return the gap index.
	/// @throws IllegalArgumentException if the position is not in this scope.
	public int localIndexOf(MDCPosition position) {
		if (!contains(position)) {
			throw new IllegalArgumentException(position + " is not in scope " + this);
		}
		return position.getPath()[path.length];
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof TextScope s && Arrays.equals(path, s.path);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(path);
	}

	@Override
	public String toString() {
		return "scope " + Arrays.toString(path);
	}
}

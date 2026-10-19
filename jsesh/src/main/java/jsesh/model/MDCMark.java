/*
 * Created on 1 août 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.model;

import java.util.Arrays;

import jsesh.model.operations.ChildOperation;
import jsesh.model.operations.Deletion;
import jsesh.model.operations.Insertion;
import jsesh.model.operations.ModelOperation;
import jsesh.model.operations.Replacement;
import jsesh.utils.datatypes.EnumBase;

/// A long-term position marker in a text. Unlike a [MDCPosition], a mark
/// follows the modifications of the text. A mark is never deleted, except if
/// its [#release()] method is called.
///
/// The mark system is inspired by the tk text widget.
///
/// A mark may stand anywhere a [MDCPosition] can: between top items, inside a
/// quadrat, in the content of a cartouche (a nested [TextScope])... It follows
/// insertions and deletions done at any depth of the text: when items are
/// inserted or removed in some element, the marks which stand in this element,
/// or deeper inside one of its children, are shifted accordingly. A mark which
/// stood inside a removed (or replaced) element falls back to the gap where
/// this element was. The resulting position is a valid position of the text,
/// but not necessarily a cursor stop (see
/// [PositionNavigator#canonicalize(HieroglyphicText, MDCPosition)]).
///
/// There is a problem with the mark system. When text is modified, the
/// corresponding visit method are called. So far, so good. But now, the mark's
/// observers are notified. The problem is that if they observe more than one
/// mark, some of them won't have been updated by then. Hence, issues.
///
/// Solution: all marks should have been updated before telling the mark's
/// observer about anything.
///
/// @author rosmord
public class MDCMark {

	static public class Gravity extends EnumBase {

		private static final long serialVersionUID = 2830453509378805410L;

		public static Gravity BACKWARD = new Gravity(1, "BACKWARD");

		public static Gravity FORWARD = new Gravity(2, "FORWARD");

		/**
		 * @param id
		 * @param designation
		 */
		private Gravity(int id, String designation) {
			super(id, designation);
		}
	}

	/// The path of the position of this mark (see [MDCPosition]).
	private int[] path;

	/// The text this mark lives in.
	private HieroglyphicText topItemList;

	/// Will the mark move left or right when data is inserted at the mark
	/// position.
	private Gravity gravity;

	/// Create a mark corresponding to a specific position in a text. When text
	/// is inserted at the mark position, the mark will move forward.
	///
	/// @param topItemList the text the mark lives in.
	/// @param position the position of the mark in `topItemList` (clamped to its bounds).
	public MDCMark(HieroglyphicText topItemList, MDCPosition position) {
		this(topItemList, position, Gravity.FORWARD);
	}

	/// Create a mark corresponding to a specific position in a text. specify if
	/// the mark will move forward or backward when text is inserted at mark
	/// position.
	///
	/// @param topItemList the text the mark lives in.
	/// @param position the position of the mark in `topItemList` (clamped to its bounds).
	///        It may be a nested position; if it does not exist in the
	///        text, the mark falls back to the top-level position before the
	///        enclosing top item.
	/// @param gravity
	public MDCMark(HieroglyphicText topItemList, MDCPosition position, Gravity gravity) {
		this.topItemList = topItemList;
		this.path = position.getPath();
		this.gravity = gravity;
		checkPosition();
		topItemList.addMark(this);
	}

	/// @return the position of this mark. It is nested if the mark lies inside a
	///         top item (see [#isInner()]).
	public MDCPosition getPosition() {
		return MDCPosition.of(path);
	}

	/// @return the top-level position just before the top item containing this
	///         mark. For a top-level mark, the same as [#getPosition()].
	public MDCPosition getTopLevelPosition() {
		return new MDCPosition(path[0]);
	}

	/// @return true if this mark lies inside a top item (a quadrat, a
	///         cartouche...), and not between two top items.
	public boolean isInner() {
		return path.length > 1;
	}

	/**
	 * Release this mark. The mark can then be lost and forgotten. As long as a
	 * mark is not released, even if there is no handle to it, it continues to
	 * be maintained.
	 */

	public void release() {
		topItemList.removeMark(this);
		topItemList = null;
	}

	/// Updates this mark when the text is modified. Automatically called by
	/// the text, hence the "package" visibility.
	///
	/// A modification made deep inside the text reaches the text as a chain of
	/// [ChildOperation]s, one per level, ending on the actual operation. The
	/// chain gives the path of the modified element.
	void updateMark(ModelOperation op) {
		int[] container = new int[0];
		ModelOperation current = op;
		while (current instanceof ChildOperation child) {
			ModelOperation inner = child.getChildOperation();
			int index = indexOfChild(child.getElement(), inner.getElement());
			if (index < 0) {
				// Unexpected structure: just make sure the mark is still valid.
				checkPosition();
				return;
			}
			container = Arrays.copyOf(container, container.length + 1);
			container[container.length - 1] = index;
			current = inner;
		}
		if (isIn(container)) {
			if (current instanceof Insertion insertion) {
				inserted(container.length, insertion.getIndex(), insertion.getChildren().size());
			} else if (current instanceof Deletion deletion) {
				deleted(container, deletion.getStart(), deletion.getEnd());
			} else if (current instanceof Replacement replacement) {
				replaced(container, replacement.getIndex());
			}
		}
		// Other operations (modifications, shading...) don't change the structure.
		checkPosition();
	}

	/// Does this mark lie in the element with this path (in one of its gaps, or
	/// deeper)?
	private boolean isIn(int[] container) {
		return path.length > container.length
				&& Arrays.equals(path, 0, container.length, container, 0, container.length);
	}

	/// Is the mark in a gap of the element at depth `depth`, rather than inside
	/// one of its children?
	private boolean isGapAt(int depth) {
		return path.length == depth + 1;
	}

	/// Items have been inserted in the element of depth `depth` containing the mark.
	private void inserted(int depth, int index, int count) {
		int k = path[depth];
		// In a gap, the gravity decides; inside the child at `index`, the
		// insertion comes before the mark.
		if (k > index || (k == index && (!isGapAt(depth) || gravity.equals(Gravity.FORWARD)))) {
			path[depth] += count;
		}
	}

	/// Items have been removed from the element `container` containing the mark.
	private void deleted(int[] container, int start, int end) {
		int depth = container.length;
		int k = path[depth];
		if (k >= end) {
			path[depth] -= end - start;
		} else if (isGapAt(depth)) {
			path[depth] = Math.min(k, start);
		} else if (k >= start) {
			// The mark was inside a removed element.
			path = gapIn(container, start);
		}
	}

	/// A child of the element `container` containing the mark has been replaced.
	private void replaced(int[] container, int index) {
		if (!isGapAt(container.length) && path[container.length] == index) {
			path = gapIn(container, index);
		}
	}

	private static int[] gapIn(int[] container, int gap) {
		int[] result = Arrays.copyOf(container, container.length + 1);
		result[container.length] = gap;
		return result;
	}

	/// The index of a child, compared by identity.
	private static int indexOfChild(ModelElement parent, ModelElement child) {
		for (int i = 0; i < parent.getNumberOfChildren(); i++) {
			if (parent.getChildAt(i) == child) {
				return i;
			}
		}
		return -1;
	}

	/// If the position is not valid in the text anymore, falls back to the
	/// top-level position before its top item (clamped to the text bounds).
	private void checkPosition() {
		if (!topItemList.isValidPosition(MDCPosition.of(path))) {
			path = new int[] { Math.min(path[0], topItemList.getNumberOfChildren()) };
		}
	}

	/**
	 * Returns the gravity of this mark. When data at the mark position, the
	 * mark may move either to the left or to the right.
	 * 
	 * @return the gravity.
	 */

	public Gravity getGravity() {
		return gravity;
	}

	/**
	 * @return the element after the mark.
	 */
	public TopItem getElementAfter() {
		return topItemList.getElementAfter(getTopLevelPosition());
	}

	/**
	 * @return the element before the mark.
	 */
	public TopItem getElementBefore() {
		return topItemList.getElementBefore(getTopLevelPosition());
	}

	/**
	 * Get a position relatively to this mark.
	 * 
	 * @param i
	 * @return the position <em>i</em> places from this mark.
	 * @see HieroglyphicText#getNextPosition(MDCPosition, int)
	 */
	public MDCPosition getNextPosition(int i) {
		return topItemList.getNextPosition(getTopLevelPosition(), i);
	}

	/**
	 * returns the topitemlist to which this mark points.
	 * 
	 * @return the topitemlist to which this mark points.
	 */
	public HieroglyphicText getHieroglyphicText() {
		return topItemList;
	}

	public String toString() {
		return getPosition().toString();
	}

	public boolean hasNext() {
		return topItemList.hasNext(getTopLevelPosition());
	}

	public boolean hasPrevious() {
		return getTopLevelPosition().hasPrevious();
	}

}

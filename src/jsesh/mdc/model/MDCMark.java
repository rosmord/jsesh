/*
 * Created on 1 août 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.mdc.model;

import java.util.List;

import jsesh.mdc.model.operations.ChildOperation;
import jsesh.mdc.model.operations.Deletion;
import jsesh.mdc.model.operations.Insertion;
import jsesh.mdc.model.operations.ModelOperation;
import jsesh.mdc.model.operations.ModelOperationVisitor;
import jsesh.mdc.model.operations.Modification;
import jsesh.mdc.model.operations.Replacement;
import jsesh.mdc.model.operations.ZoneModification;
import jsesh.utils.EnumBase;

/**
 * A MDCMark designate a position relative to an element. It's a long-term
 * position marker in a text. A mark is never deleted, except if its release()
 * method is called.
 * 
 * <P>
 * Marks survive text modification.
 * 
 * <P>
 * The mark system is inspired by the tk text widget.
 * 
 * <p>
 * There is a problem with the mark system. When text is modified, the
 * corresponding visit method are called. So far, so good. But now, the mark's
 * observers are notified. The problem is that if they observe more than one
 * mark, some of them won't have been updated by then. Hence, issues.
 * <p>
 * Solution : all marks should have been updated before telling the mark's
 * observer about anything.
 * 
 * @author rosmord
 * 
 */

public class MDCMark {

	static public class Gravity extends EnumBase {

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

	/**
	 * The underlying text position.
	 * 
	 */

	private MDCPosition position;

	/**
	 * Will the mark move left or right when data is inserted at the mark index.
	 * When data at the mark position, the mark may move either to the left or
	 * to the right.
	 */

	private Gravity gravity;

	List listeners = null;
	
	private MarkUpdater markUpdater;

	/**
	 * Create a mark corresponding to a specific position in a text. When text
	 * is inserted at the mark position, the mark will move forward.
	 * 
	 * @param position
	 */

	public MDCMark(MDCPosition position) {
		this(position, Gravity.FORWARD);
	}

	/**
	 * Create a mark corresponding to a specific position in a text. specify if
	 * the mark will move forward or backward when text is inserted at mark
	 * position.
	 * 
	 * @param position
	 * @param gravity
	 */

	public MDCMark(MDCPosition position, Gravity gravity) {
		this.position = position;
		this.gravity = gravity;
		markUpdater= new MarkUpdater();
		position.getTopItemList().addMark(this);
	}

	/**
	 * @return Returns the position.
	 */
	public MDCPosition getPosition() {
		return position;
	}

	/**
	 * Release this mark. The mark can then be lost and forgotten. As long as a
	 * mark is not released, even if there is no handle to it, it continues to
	 * be maintained.
	 */

	public void release() {
		position.getTopItemList().removeMark(this);
		position = null;
	}

	/**
	 * a method to update this mark when the model it points to is modified.
	 * Automatically called by the model. Hence the "package" visibility.
	 */
	void updateMark(ModelOperation op) {
		op.accept(markUpdater);
	}
	
	private class MarkUpdater implements ModelOperationVisitor {
		
		/* (non-Javadoc)
		 * @see jsesh.mdc.model.ModelElementObserver#observedElementChanged(jsesh.mdc.model.operations.ModelOperation)
		 */
		public void observedElementChanged(ModelOperation operation) {
			operation.accept(this);
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitInsertion(jsesh.mdc.model.operations.Insertion)
		 */

		public void visitInsertion(Insertion insertion) {
			if (insertion.getIndex() < position.getIndex()) {
				advanceBy(insertion.getChildren().size());
			} else if (insertion.getIndex() == position.getIndex()) {
				if (gravity.equals(Gravity.FORWARD)) {
					advanceBy(insertion.getChildren().size());
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitDeletion(jsesh.mdc.model.operations.Deletion)
		 */

		public void visitDeletion(Deletion deletion) {
			// Note that deletion affects the element after the mark.
			// hence the "<" below.
			if (deletion.getEnd() <= position.getIndex()) {
				advanceBy(-deletion.getEnd() + deletion.getStart());
			} else if (deletion.getStart() < position.getIndex()) {
				// The mark falls in the deleted zone.
				setIndex(deletion.getStart());
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitReplacement(jsesh.mdc.model.operations.Replacement)
		 */

		public void visitReplacement(Replacement replacement) {
			// CURRENTLY NO-OP.
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitModification(jsesh.mdc.model.operations.Modification)
		 */

		public void visitModification(Modification modification) {
			// CURRENTLY NO-OP.
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitChildOperation(jsesh.mdc.model.operations.ChildOperation)
		 */

		public void visitChildOperation(ChildOperation operation) {
			// CURRENTLY NO-OP.
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.operations.ModelOperationVisitor#visitZoneModification(jsesh.mdc.model.operations.ZoneModification)
		 */
		public void visitZoneModification(ZoneModification modification) {
			// TODO Auto-generated method stub
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Observer#update(java.util.Observable,
		 *      java.lang.Object)
		 */

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
	 * Update the position of this mark during a text change. Marks are
	 * immutable, but they follow the text. Hence their internal representation
	 * change.
	 * 
	 * @param index
	 */
	private void setIndex(int index) {
		if (index < 0
				|| index > position.getTopItemList().getNumberOfChildren())
			return;
		if (position.getIndex() != index) {
			position = new MDCPosition(position.getTopItemList(), index);

		}
	}

	private void advanceBy(int i) {
		setIndex(position.getIndex() + i);
	}

	/**
	 * @return the element after the mark.
	 */
	public TopItem getElementAfter() {
		return position.getElementAfter();
	}

	/**
	 * @return the element before the mark.
	 */
	public TopItem getElementBefore() {
		return position.getElementBefore();
	}

	/**
	 * @return the mark index.
	 */
	public int getIndex() {
		return position.getIndex();
	}

	/**
	 * Get a position relatively to this mark.
	 * 
	 * @param i
	 * @return the position <em>i</em> places from this mark.
	 * @see MDCPosition#getNextPosition(int)
	 */
	public MDCPosition getNextPosition(int i) {
		return position.getNextPosition(i);
	}

	/**
	 * Get an absolute position in the list of items.
	 * 
	 * @param k
	 * @return an absolute position.
	 * @see MDCPosition#getPositionAt(int)
	 */
	public MDCPosition getPositionAt(int k) {
		return position.getPositionAt(k);
	}

	/**
	 * returns the topitemlist to which this mark points.
	 * 
	 * @return the topitemlist to which this mark points.
	 */
	public TopItemList getTopItemList() {
		return position.getTopItemList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return position.toString();
	}

	public boolean hasNext() {
		return position.hasNext();
	}

	public boolean hasPrevious() {
		return position.hasPrevious();
	}

}
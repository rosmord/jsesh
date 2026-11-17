/*
 * Created on 20 juil. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdc.model;

/**
 * 
 * AT THE TIME BEING, THE POSITION IS JUST A INTEGER. THE TEXT BELOW DESCRIBES
 * WHAT THE TEXT POSITION WILL PROBABLY BE.
 * 
 * A Text position represents a position in a Manuel de Codage model tree. A
 * position does not designate a <em>model element</em>; it's more like a
 * cursor <em>between</em> elements. This of course simplifies the semantics
 * of element insertion and the like. As Positions are short lived, we have
 * decided to make them <b>read-only </b>.
 * 
 * <p>
 * More precisely, a position is <em> always </em> a position
 * <em><strong>in</strong></em> an element. it designate an element, and a
 * index in this element. For instance, inserting element e1 at position 0 in
 * element e0 would make e1 the first child of e0. The first child of an element
 * stands between positions 0 and 1.
 * 
 * <p>
 * A position can be used to insert new data, as well as to implement cursors,
 * text selection, etc.
 * 
 * <p>
 * A position is not a "long term" object. It's quite likely to become invalid
 * or outdated if the text is modified. For long term marking of mutable text
 * position, one should use a MDCMark.
 * 
 * @author rosmord
 *  
 */

public class MDCPosition implements Cloneable {

	private TopItemList topItemList;

	private int index;

	// Methods :
	// getTopMostHre
	// getPrevious, getNext
	// getNextBrother, getPreviousBrother
	// getParent
	// getFirstChild
	// getLastChild

	/**
	 * TEMPORARY CONSTRUCTOR.
	 * @param container
	 * @param index
	 */

	public MDCPosition(TopItemList container, int index) {
		this.topItemList = container;
		if (index < 0)
			index = 0;
		if (index > container.getNumberOfChildren())
			index = container.getNumberOfChildren();
		this.index = index;
	}

	public TopItem getElementAfter() {
		if (topItemList == null || index >= topItemList.getNumberOfChildren()
				|| index < 0)
			return null;
		return topItemList.getTopItemAt(index);
	}

	public TopItem getElementBefore() {
		if (topItemList == null || index > topItemList.getNumberOfChildren()
				|| index <= 0)
			return null;
		return topItemList.getTopItemAt(index - 1);
	}

	/**
	 * Returns the topItemList.
	 * 
	 * @return the topItemList.
	 */
	public TopItemList getTopItemList() {
		return topItemList;
	}

	/**
	 * @return the index.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Returns the ith position after this one, at top level. if the position
	 * would fall outside of the possible bounds, it will either be the last
	 * position or the first.
	 * 
	 * @param i :
	 *            the delta between the two positions. Might be negative.
	 * @return ith position after this one.
	 */
	public MDCPosition getNextPosition(int i) {
		int k = getIndex() + i;
		return getPositionAt(k);
	}

	/**
	 * Return the k-th position in the current model. if the position would fall
	 * outside of the possible bounds, it will either be the last position or
	 * the first.
	 * 
	 * @param k :
	 *            the index of the new position.
	 * @return the kth position in this model.
	 */

	public MDCPosition getPositionAt(int k) {
		return new MDCPosition(getTopItemList(), k);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "pos " + getIndex();
	}

	/**
	 * @return true if there is a previous position.
	 */
	public boolean hasPrevious() {
		return index > 0;
	}


	/**
	 * @return true if there is a next position.
	 */
	
	public boolean hasNext() {
		return index < topItemList.getNumberOfChildren();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof MDCPosition) {
			MDCPosition p = (MDCPosition) obj;
			return topItemList == p.topItemList && index == p.index;
		} else
			return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (topItemList.hashCode() << 0xFFFF) + index; 
	}
}
/*
 * Created on 20 juil. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdc.model;

import java.io.Serializable;

/**
 * TODO MDCPosition should undergo a major rewriting. It should NOT contain 
 * a reference to the text it refers to, and should simply be a value. It would 
 * be simpler, and would avoid memory leaks.
 * 
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
 *     BEWARE: Positions are currently not suitable if you want to transfer information
 *     from one text to the other. More precisely, in the search system, two representations
 *     of the same text might be stored in memory. MDCPositions are not suitable in this
 *     case.
 * </p>
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

@SuppressWarnings("serial")
public class MDCPosition implements Cloneable, Serializable, Comparable<MDCPosition> {

	private TopItemList topItemList;

	private int index;

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
	 * Returns the ith position before this one, at top level. if the position
	 * would fall outside of the possible bounds, it will either be the first position.
	 * 
	 * @param delta :
	 *            the delta between the two positions. Must be positive.
	 * @return ith position before this one.
	 */
	public MDCPosition getPreviousPosition(int delta) {
		if (delta < 0) throw new IllegalArgumentException("delta must be positive "+ delta);
		int k = getIndex() - delta;
		return getPositionAt(k);
	}
	

	 /* Returns the last position in the current line.
	 * 
	 * @return the last position in the current line.
	 */
	public MDCPosition getLineLastPosition() {
		MDCPosition second = this;

		while (second.hasNext() && !second.getElementAfter().isBreak())
			second = second.getNextPosition(1);
		return second;
	}
	
	/**
	 * Returns the first position in the current line.
	 * 
	 * @return the first position in the current line.
	 */
	public MDCPosition getLineFirstPosition() {
		MDCPosition first = this;
		while (first.hasPrevious() && !first.getElementBefore().isBreak())
			first = first.getNextPosition(-1);
		return first;
	}
	
	/**
	 * Finds the position "up" (in a syntactic way) from us.
	 * @return
	 */
	public MDCPosition getUpPosition() {
		MDCPosition p = this;
		p = p.getNextPosition(-1);
		// Go to the last position of the previous line.
		while (p.hasPrevious() && !p.getElementAfter().isBreak())
			p = p.getNextPosition(-1);
		return p.getLineFirstPosition();	
	}
	
	/**
	 * Finds the position "down" (in a syntactic way) from us.
	 * @return
	 */
	public MDCPosition getDownPosition() {
		MDCPosition p = this;
		p = p.getNextPosition(1);
		while (p.hasNext() && !p.getElementBefore().isBreak())
			p = p.getNextPosition(1);
		return p;
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
			return topItemList.equals(p.topItemList) && index == p.index;
		} else
			return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return (topItemList.hashCode() * 31) + index; 
	}

	/**
	 * Implementation of the Comparable interface.
	 * 
	 * Comparing positions in two different top items doesn't mean much.
	 * Note: this class has a natural ordering that is inconsistent with equals, in the case where the corresponding topItemLists are differents.
	 * @see Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(MDCPosition other) {
		return (index - other.index);
	}
	
	/**
	 * Returns a couple of <em>orderered</em> text positions.
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static MDCPosition [] getOrdereredPositions(MDCPosition p1, MDCPosition p2) {
		MDCPosition result[];
		if (p1.compareTo(p2) < 0) {
			result= new MDCPosition[]{p1, p2};
		} else {
			result= new MDCPosition[]{p2, p1};	
		}
		return result;
	}

	
}
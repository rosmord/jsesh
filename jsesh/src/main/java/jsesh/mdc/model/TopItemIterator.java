package jsesh.mdc.model;

import java.util.ListIterator;

/**
 * This file is free Software
 * (c) Serge Rosmorduc
 * @author rosmord
 *
 */
public class TopItemIterator {
	private	ListIterator rep;
	
	TopItemIterator(ListIterator rep)
	{
		this.rep= rep;	
	}	
	
	public void add(TopItem o) {
		rep.add(o);
	}

	/*
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return rep.hasNext();
	}

	/*
	 * @see java.util.ListIterator#hasPrevious()
	 */
	public boolean hasPrevious() {
		return rep.hasPrevious();
	}

	/*
	 * @see java.util.Iterator#next()
	 */
	public TopItem next() {
		return (TopItem)rep.next();
	}

	/*
	 * @see java.util.ListIterator#nextIndex()
	 */
	public int nextIndex() {
		return rep.nextIndex();
	}

	/*
	 * @see java.util.ListIterator#previous()
	 */
	public TopItem previous() {
		return (TopItem)rep.previous();
	}

	/*
	 * @see java.util.ListIterator#previousIndex()
	 */
	public int previousIndex() {
		return rep.previousIndex();
	}

	/*
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		rep.remove();
	}

	/*
	 * @see java.util.ListIterator#set(java.lang.Object)
	 */
	public void set(TopItem o) {
		rep.set(o);
	}
}

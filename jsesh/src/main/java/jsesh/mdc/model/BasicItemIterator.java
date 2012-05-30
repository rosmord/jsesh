package jsesh.mdc.model;
import java.util.ListIterator;

/**
 * A typed iterator class (probably not needed anymore in jdk 1.5+).
 * This file is free Software
 * (c) Serge Rosmorduc
 * @author rosmord
 *
 */
public class BasicItemIterator<T extends BasicItem> {
	private	ListIterator<T> rep;
	

	BasicItemIterator(ListIterator<T> rep)
	{
		this.rep= rep;	
	}	
	

	public void add(T o) {
		rep.add(o);
	}


	/*
	 * (non javadoc)
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


	/**
	 * return the next basicItem and advances.
	 * @return the next basicItem.
	 * @see java.util.Iterator#next()
	 */
	public BasicItem next() {
		return rep.next();
	}


	/*
	 * @see java.util.ListIterator#nextIndex()
	 */
	public int nextIndex() {
		return rep.nextIndex();
	}


	/**
	 * return the previous basicItem and goes back.
	 * @return the previous basicItem.
	 * @see java.util.ListIterator#previous()
	 */
	public BasicItem previous() {
		return rep.previous();
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


	/**
	 * Sets current BasicItem.
	 * @param o
	 * @see java.util.ListIterator#set(java.lang.Object)
	 */
	public void set(T o) {
		rep.set(o);
	}


}

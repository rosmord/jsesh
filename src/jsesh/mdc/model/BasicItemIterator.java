package jsesh.mdc.model;
import java.util.ListIterator;

/**
 * This file is free Software
 * (c) Serge Rosmorduc
 * @author rosmord
 *
 */
public class BasicItemIterator {
	private	ListIterator rep;
	

	BasicItemIterator(ListIterator rep)
	{
		this.rep= rep;	
	}	
	

	public void add(BasicItem o) {
		rep.add(o);
	}


	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		return rep.hasNext();
	}


	/**
	 * @see java.util.ListIterator#hasPrevious()
	 */
	public boolean hasPrevious() {
		return rep.hasPrevious();
	}


	/**
	 * @see java.util.Iterator#next()
	 */
	public BasicItem next() {
		return (BasicItem)rep.next();
	}


	/**
	 * @see java.util.ListIterator#nextIndex()
	 */
	public int nextIndex() {
		return rep.nextIndex();
	}


	/**
	 * @see java.util.ListIterator#previous()
	 */
	public Object previous() {
		return (BasicItem)rep.previous();
	}


	/**
	 * @see java.util.ListIterator#previousIndex()
	 */
	public int previousIndex() {
		return rep.previousIndex();
	}


	/**
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		rep.remove();
	}


	/**
	 * @see java.util.ListIterator#set(java.lang.Object)
	 */
	public void set(BasicItem o) {
		rep.set(o);
	}


}

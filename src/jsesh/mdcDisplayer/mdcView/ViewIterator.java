package jsesh.mdcDisplayer.mdcView;

import java.util.ListIterator;

/**
 * This file is free Software
 * (c) Serge Rosmorduc
 * @author rosmord
 *
 *
 *
 */

// TODO : examine closely the interaction between a view and its subviews.
public class ViewIterator {

	private MDCView container;
	private ListIterator rep;

	ViewIterator(MDCView container, ListIterator rep) {
		this.rep= rep;
		this.container= container; 
	}
	/*
	 * @see java.util.ListIterator#add(java.lang.ContainerView)
	 */
	public void add(MDCView o) {
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
	public MDCView next() {
		return (MDCView) rep.next();
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
	public MDCView previous() {
		return (MDCView) rep.previous();
	}

	/*
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

	/*
	 * @see java.util.ListIterator#set(java.lang.ContainerView)
	 */
	public void set(MDCView o) {
		rep.set(o);
	}

}

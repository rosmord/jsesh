package jsesh.mdc.model;

import java.util.ListIterator;

/**
 * This file is free Software
 * (c) Serge Rosmorduc
 * @author rosmord
 *
 */
public class HBoxIterator {

	private ListIterator rep;
	
	HBoxIterator(ListIterator rep)
	{
		this.rep= rep;
	}
	
	/**
	 * Add an Hbox at the current position.
	 * @param o
	 * @see java.util.ListIterator#add(java.lang.Object)
	 */
	public void add(HBox o) {rep.add(o);}

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

	/**
	 * Returns the next hbox and advances.
	 * @return the next hbox.
	 * @see java.util.Iterator#next()
	 */
	public HBox next() {
		return (HBox) rep.next();
	}

	/*
	 * @see java.util.ListIterator#nextIndex()
	 */
	public int nextIndex() {
		return rep.nextIndex();
	}

	/**
	 * Returns the previous hbox and goes back.
	 * @return the previous hbox.
	 * @see java.util.ListIterator#previous()
	 */
	public HBox previous() {
		return (HBox) rep.previous();
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
	public void remove() {rep.remove();}

	/*
	 * @see java.util.ListIterator#set(java.lang.HBox)
	 */
	public void set(HBox o) {rep.set(o);}

}

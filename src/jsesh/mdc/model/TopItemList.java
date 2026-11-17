package jsesh.mdc.model;

import java.util.List;

import jsesh.mdc.interfaces.MDCFileInterface;
import jsesh.mdc.interfaces.TopItemListInterface;
import jsesh.mdc.model.operations.ZoneModification;

/**
 * 
 * TopItemList
 * @author rosmord
 *
 *This code is published under the GNU LGPL.
 */

public class TopItemList
	extends ModelElement
	implements MDCFileInterface, TopItemListInterface {

	public TopItemList() {
	}

	public void accept(ModelElementVisitor v) {
		v.visitTopItemList(this);
	}

	/**
	 * Adds a list of elements to this TopItemList.
	 * The list must contain only topitems.
	 * @param elements
	 */
	public void addAll(List elements) {
		super.addAll(elements, TopItem.class);
	}

	/**
	 * Add a list of topitems at a given position in this TopItemList.
	 * @param index
	 * @param items
	 * 
	 */
	public void addAllAt(int index, List items) {
		super.addAllAt(index, items, TopItem.class);
		
	}

	public void addTopItem(TopItem topItem) {
		addChild(topItem);
	}

	public void addTopItemAt(int id, TopItem topItem) {
		addChildAt(id, topItem);
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		return compareContents(e);
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public ModelElement deepCopy() {
		TopItemList r= new TopItemList();
		copyContentTo(r);
		return r;
	}

	/**
	 * Return the ith children as a topitem.
	 * @param i
	 * @return the ith children as a topitem.
	 */
	public TopItem getTopItemAt(int i) {
		return (TopItem) getChildAt(i);
	}

	// Iterators stuff
	public TopItemIterator iterator() {
		return new TopItemIterator(getListIterator());
	}

	public TopItemIterator iterator(int idx) {
		return new TopItemIterator(getListIterator(idx));
	}

	/**
	 * @param i
	 */
	public void removeTopItem(int i) {
		removeChildAt(i);
	}

	public void removeTopItem(TopItem topItem) {
		removeChild(topItem);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getChildrenAsString();
	}

	/**
	 * Suppress all elements between a and b.
	 * Returns the list of the suppressed elements.
	 * @param a
	 * @param b
	 * @return Returns the list of the suppressed elements.
	 */
	public List removeTopItems(int a, int b) {
		return removeChildren(a,b);
	}
	
	/* (non-Javadoc)
	 * TopItem lists are too big to enter a topitem, so the method always returns null.
	 * @see jsesh.mdc.model.ModelElement#buildTopItem()
	 */
	public TopItem buildTopItem() {
		return null;
	}

	/**
	 * Shade or unshade a whole zone.
	 * @param a
	 * @param b
	 * @param shade  if true, shade ; if false, unshade.
	 */
	public void shade(int a, int b, boolean shade) {
		int start= Math.min(a,b);
		int end= Math.max(a,b);
		
		// We don't want messages from the modified elements.
		// note that currently we won't get any, for state is not linked to its container.
		disableUpdates();
		for (int i=start; i< end; i++) {
			TopItem t= getTopItemAt(i);
			t.getState().setShaded(shade);
		}
		enableUpdates();
		notifyObservers(new ZoneModification(this, start, end));
		setChanged();
	}
	
	
	/**
	 * paint a zone in black or black.
	 * a,b are the zone limits. The system is the one used for positions. 
	 * (that is, the indexes fall between elements).
	 * @param a
	 * @param b
	 * @param red : if true, paint in red ; if false, paint in black.
	 */
	public void setRed(int a, int b, boolean red) {
		int start= Math.min(a,b);
		int end= Math.max(a,b);
		
		//	We don't want messages from the modified elements.
		disableUpdates();
		for (int i=start; i< end; i++) {
			TopItem t= getTopItemAt(i);
			t.getState().setRed(red);
		}
		enableUpdates();
		setChanged();
		notifyObservers(new ZoneModification(this, start, end));
	}

    /**
     * Returns the number of pages in this topItemList.
     * (currently not optimized).
     * @return the number of pages in this topItemList.
     */
    public int getNumberOfPages() {
        int result= 1;
        for (int i= 0; i< getNumberOfChildren(); i++)
            if (getChildAt(i) instanceof PageBreak)
                result++;
        return result;
    }
}

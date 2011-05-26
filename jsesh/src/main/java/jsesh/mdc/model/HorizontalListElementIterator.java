package jsesh.mdc.model;

import java.util.ListIterator;

/**
 * This file is free Software (c) Serge Rosmorduc
 * <p>
 * TODO : replace uses of this type by ListIterator&lt;HorizontalListElement&gt; !
 * @author rosmord
 *  
 */
public class HorizontalListElementIterator {
    private ListIterator rep;

    HorizontalListElementIterator(ListIterator rep) {
        this.rep = rep;
    }

    public void add(InnerGroup o) {
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
    public InnerGroup next() {
        return (InnerGroup) rep.next();
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
    public InnerGroup previous() {
        return (InnerGroup) rep.previous();
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
    public void set(InnerGroup o) {
        rep.set(o);
    }
}

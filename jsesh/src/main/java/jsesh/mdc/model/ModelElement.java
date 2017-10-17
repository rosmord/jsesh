package jsesh.mdc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsesh.mdc.model.operations.ChildOperation;
import jsesh.mdc.model.operations.Deletion;
import jsesh.mdc.model.operations.Insertion;
import jsesh.mdc.model.operations.ModelOperation;
import jsesh.mdc.model.operations.Modification;
import jsesh.mdc.model.operations.Replacement;

/**
 * Model Element is the parent class of all <em>Manuel de Codage</em>
 * constructs. Each model element is potentially a container for other elements,
 * and modifications to the lower levels are forwarded to the upper levels
 * (hence each element is potentially an observer for its sub-elements).
 *
 * <p>
 * Equality for this class is entity based (as inherited from Object). That is,
 * two ModelElement are the same iff they have the same address.
 * <p>
 * But this class has also a natural ordering that is <em>inconsistent</em> with
 * equals, and is content based. It can be used to compare elements. Note that,
 * as elements are mutable, if one wants to build an index based on ModelElement
 * contents, a preventive copy is needed.
 * <p>
 * TODO : The comparison operator should probably move it to a comparator.
 *
 * <p>
 * Model elements are comparable one to each other, regardless of their actual
 * class. The way this is done is :
 * <ol>
 * <li>if the elements belong to different classes, we compare the names of the
 * classes.</li>
 * <li>if the objects belong to the exact same class, then the definition
 * depends on the class itself.</li>
 * </ol>
 * To ensure that all classes implement compareTo, ModelElement doesn't provide
 * any implementation. Instead, it provides a number of protected helpful
 * functions.
 *
 * <p>
 * IMPORTANT : The current system does not ensure views are systematically
 * deleted. IMPROVE IT.
 *
 *
 * <p>
 * ModelElements used to implement the Observable class and be observers for
 * their children. It is no longer the case, and Observable has been replaced by
 * an ad-hoc system. The rationale behind this is that a) Observable doesn't
 * implement Serializable, and hence the Observable part of ModelElements could
 * not be serialized, which was quite annoying (it's an important rule about
 * Java Serialization: if a Serializable class extends a non Serializable one,
 * the data in the parent class is not copied). The second reason was that it
 * was too general to maintain a list of observers for each model element with
 * the current architecture. Multiple observers can easily be managed by
 * encapsulating a TopItemList in some Observable element.
 *
 * <p>
 * Note that a list of child elements is maintained. It is suggested to use it
 * to store the children elements.
 *
 * <p>
 * This list is somehow problematic, from a type point of view this being said.
 * <p>
 * Modelisation problem : all elements have at most one Container, which, for
 * most of them, is also a ModelElement. the only exception is TopItemList,
 * whose Container is arbitrary. In some cases, we want to know the parent
 * ModelElement of an element (for navigation purposes. Currently, it's used to
 * know if an element is an inner element or not).
 *
 * <p>
 * Now, we don't want to store the same information twice : for all elements
 * save TopItemList, the parentElement is also the ModelElementContainer. We
 * also want to avoid using instanceof, which is evil. The only possible
 * solution is to create an intermediary abstract class, EmbeddedModelElement.
 *
 * <p>
 * This file is free Software under the LGPL (c) Serge Rosmorduc
 *
 * @author rosmord
 *
 */
public abstract class ModelElement implements ModelElementObserver,
        Comparable<ModelElement>, Serializable {

    private static final long serialVersionUID = -8654450475547792198L;

    private List<EmbeddedModelElement> children = null;

    /**
     * Means that modifications to sub elements of this one will be forwarded.
     */
    private boolean updatesEnabled = true;

    public ModelElement() {
        children = buildChildrenList();
    }

    abstract public void accept(ModelElementVisitor v);

    /**
     * Add a child at the end of the children list.
     *
     * @param child the child to add.
     */
    final protected void addChild(EmbeddedModelElement child) {
        addChildAt(children.size(), child);
    }

    /**
     * add child at position idx.
     *
     * @param idx
     * @param child
     */
    final protected void addChildAt(int idx, EmbeddedModelElement child) {
        child.setParent(this);
        children.add(idx, child);
        fixSlibings(idx);
        notifyModelElementObservers(new Insertion(this, idx, child));
    }

    /**
     * Creates an empty list for children. Override it to change the children
     * list implementation.
     *
     * @return a newly built empty list of children.
     */
    private List<EmbeddedModelElement> buildChildrenList() {
        return new ArrayList<>();
    }

    /**
     * Build a <em>copy</em> of this object, embedded into a topitem. May return
     * null if this doesn't mean anything (in particular, for modifiers).
     *
     * @return a topitem or null.
     */
    public abstract TopItem buildTopItem();

    /**
     * Build a <em>copy</em> of this object, embedded in an
     * HorizontalListElement. May return null if it's not possible. The result
     * may then (if not null) be used as part of a new quadrant, in an
     * {@link HBox}.
     *
     * @return a {@link HorizontalListElement} or null.
     */
    public abstract HorizontalListElement buildHorizontalListElement();

    // Notifies observers about this element's associated values being modified.
    final protected void changeValue() {
        // IMPORTANT : think about this function
    }

    final protected void clearChildren() {
        for (EmbeddedModelElement child : children) {
            child.detachFromContainer();
        }
        children.clear();
        // FIXME : change the system for element suppression.
        notifyModelElementObservers(new Modification(this));
    }

    /**
     * Utility function that compare the classes of two ModelElements. Usable to
     * implement compareTo.
     *
     * @param o
     * @return a comparison between two classes.
     */
    private int compareClass(Object o) {
        if (getClass().equals(o.getClass())) {
            return 0;
        } else {
            return getClass().getName().compareTo(o.getClass().getName());
        }
    }

    /**
     * Compare the contents of two model elements. Usable to implement
     * compareToAux. In fact, when this and e have the same type
     * <em>and</em> are completely defined by their sub elements (i.e. in
     * practice, have no other field than their children), compareToAux is
     * simply a call to this method.
     *
     * @param e : a model element, of the exact same type as <code>this</code>.
     * @return an int, &lt; 0 if this &lt; e, 0 if this and e have the same
     * content; and &gt; 0 if this &gt; e.
     * @see ModelElement#compareToAux(ModelElement)
     */
    protected final int compareContents(ModelElement e) {
        int result = 0;
        int n = Math.min(getNumberOfChildren(), e.getNumberOfChildren());
        for (int i = 0; result == 0 && i < n; i++) {
            result = getChildAt(i).compareTo(e.getChildAt(i));
        }
        // the elements up to n are the same, compare the lengths.
        if (result == 0) {
            result = getNumberOfChildren() - e.getNumberOfChildren();
        }
        return result;
    }

    /**
     * Compare two elements for equality of content, ignoring id.
     * A possible "id" property would be ignored.
     *
     * @param other
     * @return
     */
    public boolean equalsIgnoreId(ModelElement other) {
        if (this.getClass().equals(other.getClass())) {
            if (equalsIgnoreIdAux(other)
                    && this.getNumberOfChildren() == other.getNumberOfChildren()) {
                for (int i = 0; i < this.getNumberOfChildren(); i++) {
                    if (! this.getChildAt(i).equalsIgnoreId(other.getChildAt(i))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Aux method, used to compare the non-children part of two different elements.
     * Defaut value (usable when there are no children) will always return true.
     * Ignores ids.
     * @param other
     * @return 
     */
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        return true;
    }
    
    
    /**
     * Skeleton implementation of compareTo.
     * <p>
     * Calls compareToAux when the objects have the same real classes.
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    final public int compareTo(ModelElement o) {
        int result = compareClass(o);
        if (result == 0) {
            result = compareToAux((ModelElement) o);
        }
        return result;
    }
    

    /**
     * Function called to compare two model elements that have the
     * <em>exact</em> same classes. Implementations can securely cast
     * <code>e</code> to their real type.
     * <p>
     * Implementations for this method can use compareContent().
     *
     * @param e : the element to compare
     * @return an int
     * @see Comparable#compareTo(java.lang.Object)
     * @see ModelElement#compareContents(ModelElement)
     */
    abstract protected int compareToAux(ModelElement e);

    /**
     * Copy the content of this modelElement into copy. This is an auxiliary
     * function.
     *
     * @param copy
     */
    protected void copyContentTo(ModelElement copy) {
        if (children != null) {
            copy.children = buildChildrenList();
            for (int i = 0; i < children.size(); i++) // It's important to use addChild here !
            {
                copy.addChild((EmbeddedModelElement) getChildAt(i).deepCopy());
            }
        }
    }

    /**
     * Create a copy of this ModelElement.
     *
     * @return a recursive copy of this ModelElement.
     */
    abstract public ModelElement deepCopy();

    /**
     * Disable updates forwarding. If updates are enables, modifications to sub
     * elements will be forwarded to our listeners. Disabling updates might be
     * useful when doing zone modification.
     */
    protected void disableUpdates() {
        updatesEnabled = false;
    }

    /**
     * Enable updates forwarding. If updates are enables, modifications to sub
     * elements will be forwarded to our listeners.
     *
     */
    protected void enableUpdates() {
        updatesEnabled = true;
    }

    /**
     * Fix the slibings at position id.
     *
     * @param id
     */
    private void fixSlibings(int id) {
        EmbeddedModelElement child = null;
        EmbeddedModelElement nextChild = null;
        EmbeddedModelElement previousChild = null;

        if (id >= 0 && id < children.size()) {
            child = children.get(id);
        }
        if (id + 1 >= 0 && id + 1 < children.size()) {
            nextChild = children.get(id + 1);
        }
        if (id - 1 >= 0 && id - 1 < children.size()) {
            previousChild = children.get(id - 1);
        }
        // Fix previous element, if any :
        if (child != null) {
            child.previous = previousChild;
            child.next = nextChild;
        }
        if (previousChild != null) {
            previousChild.next = child;
        }
        if (nextChild != null) {
            nextChild.previous = child;
        }
    }

    /**
     * Returns the ith child of this element. Indexes start at 0.
     *
     * @param id
     *
     * @return null if there is no such child.
     */
    final public EmbeddedModelElement getChildAt(int id) {
        return children.get(id);
    }

    final protected String getChildrenAsString() {
        return children.toString();
    }

    /**
     * Returns an iterator to the elements included in this one.
     *
     * @return ModelElementIterator
     */
    final public Iterator<EmbeddedModelElement> getModelElementIterator() {
        return children.iterator();
    }

    /**
     * Get the next element at the same level as this one. If there is no such
     * element, return null.
     *
     * @return the next element at the same level as this one.
     */
    public abstract ModelElement getNextSlibing();

    /**
     * Return the number of children of this element. Elements who have no child
     * by nature should return 0.
     *
     * @return the number of childs.
     */
    final public int getNumberOfChildren() {
        return children.size();
    }

    /**
     * Get the previous element at the same level as this one. If there is no
     * such element, return null.
     *
     * @return the previous element at the same level as this one.
     */
    public abstract ModelElement getPreviousSlibing();

    /**
     * @return true for elements that break the current structure (line breaks,
     * page breaks).
     */
    public boolean isBreak() {
        return false;
    }

    /**
     * Warns listeners that a small modification has been done to this element.
     * (it might be colour change, shading...)
     */
    protected void notifyModification() {
        notifyModelElementObservers(new Modification(this));
    }

    final protected void removeChild(ModelElement child) {
        int id = children.indexOf(child);
        removeChildAt(id);
    }

    /**
     * Removes the child after position id
     *
     * @param id
     */
    final protected void removeChildAt(int id) {
        if (id < 0 || id >= children.size()) {
            return;
        }
        EmbeddedModelElement child;
        child = children.get(id);
        children.remove(id);
        fixSlibings(id);
        child.detachFromContainer();
        notifyModelElementObservers(new Deletion(this, id, id + 1));
    }

    /**
     * Suppress all elements between indexes a and b. Returns the list of the
     * suppressed elements.
     *
     * @param a first position
     * @param b second position (b > a).
     * @return the list of the suppressed elements.
     */
    protected List<EmbeddedModelElement> removeChildren(int a, int b) {
        List<EmbeddedModelElement> l = new ArrayList<>(b - a);
        l.addAll(children.subList(a, b));
        for (int i = b - 1; i >= a; i--) {
            EmbeddedModelElement child = children.get(i);
            children.remove(i);
            child.detachFromContainer();
        }
        fixSlibings(a);
        notifyModelElementObservers(new Deletion(this, a, b));
        return l;
    }

    final protected void setChildAt(int idx, EmbeddedModelElement child) {
        EmbeddedModelElement oldChild = children.get(idx);
        oldChild.detachFromContainer();
        child.setParent(this);
        children.set(idx, child);
        fixSlibings(idx);
        notifyModelElementObservers(new Replacement(this, idx));
    }

    @Override
    public void observedElementChanged(ModelOperation operation) {
        if (updatesEnabled) {
            notifyModelElementObservers(new ChildOperation(this, operation));
        }
    }

    abstract protected void notifyModelElementObservers(ModelOperation op);

    /**
     * Returns this element's parent, or null if none.
     *
     * @return this element's parent, or null if none.
     */
    public abstract ModelElement getParent();

    /**
     * Detaches an element from its container, be it another modelElement or
     * something else.
     *
     */
    protected abstract void unsetContainers();

    /**
     * Returns true if the element contains exactly one sign and nothing else. A
     * quadrant might contain a single sign, but a cartouche is not considered
     * as containing a single sign, as it also contains the cartouche lines.
     *
     * <p>
     * Implementation note: the default implementation does the right thing for
     * most container elements. One needs to overwrite the method for more
     * specific elements.
     *
     * @return a boolean.
     */
    public boolean containsOnlyOneSign() {
        return (getNumberOfChildren() == 1 && getChildAt(0).containsOnlyOneSign());
    }

    /**
     * If the group contains only one sign, return it, else return null. It's an
     * error to call this method in other cases.
     *
     * <p>
     * Implementation note: the default implementation does the right thing for
     * most container elements. One needs to overwrite the method for more
     * specific elements.
     *
     * @return the unique hieroglyph contained in this element.
     * @throws IllegalStateException if called when
     * {@link #containsOnlyOneSign()} is false.
     */
    public Hieroglyph getLoneSign() {
        if (containsOnlyOneSign()) {
            return getChildAt(0).getLoneSign();
        } else {
            throw new IllegalStateException("Wrong call to getLoneSign.");
        }
    }


}

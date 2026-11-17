package jsesh.mdc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;
import java.util.Observer;

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
 * Model elements are comparable one to each other. The way this is done is :
 * <ol>
 * <li>if the elements belong to different classes, we compare the names of the
 * classes.</li>
 * <li>if the objects belong to the exact same class, then the definition
 * depends on the class itself.</li>
 * </ol>
 * To ensure that all classes implement compareTo, ModelElement doesn't provide
 * any implementation. Instead, it provides a number of protected helpful
 * functions : IMPORTANT : The current system does not ensure views are
 * systematically deleted. IMPROVE IT.
 * 
 * This file is free Software (c) Serge Rosmorduc
 * 
 * @author rosmord
 *  
 */
public abstract class ModelElement extends Observable implements Observer,
		Comparable, Serializable {

	private List children = null;

	private boolean fillX = false;

	private boolean fillY = false;

	private ModelElement next, previous;

	private ModelElement parent = null;

	
	/**
	 * Means that modifications to sub elements of this one will be forwarded.
	 */
	private boolean updatesEnabled = true;

	public ModelElement() {
		children = buildChildrenList();
		next = null;
		previous = null;
	}

	abstract public void accept(ModelElementVisitor v);

	/**
	 * Add all the elements in the list elementList. The compatibility of these
	 * elements with this structure is up to the programmer.
	 * 
	 * @param elementList
	 * @param eltClass the base class of the legal elements.
	 */
	final protected void addAll(List elementList, Class eltClass) {
		int i = getNumberOfChildren();
		int pos = i;

		children.addAll(elementList);
		for (Iterator iter = elementList.iterator(); iter.hasNext();) {
			ModelElement child = (ModelElement) iter.next();
			if (!eltClass.isInstance(child)) {
				throw new ClassCastException("Bad element in list " + child
						+ " for " + eltClass);
			}
			child.setParent(this);
			fixSlibings(i);
			i++;
		}
		setChanged();
		notifyObservers(new Insertion(this, pos, elementList));
	}

	protected void addAllAt(int index, List elementList, Class eltClass) {
		int i = getNumberOfChildren();

		children.addAll(index, elementList);
		for (Iterator iter = elementList.iterator(); iter.hasNext();) {
			ModelElement child = (ModelElement) iter.next();
			if (!eltClass.isInstance(child)) {
				throw new ClassCastException("Bad element in list " + child
						+ " for " + eltClass);
			}
			child.setParent(this);
			fixSlibings(i);
			i++;
		}
		setChanged();
		notifyObservers(new Insertion(this, index, elementList));
	}

	/**
	 * Add a child at the end of the children list.
	 * 
	 * @param child
	 *            the child to add.
	 */
	final protected void addChild(ModelElement child) {
		child.setParent(this);
		children.add(child);
		fixSlibings(children.size() - 1);
		setChanged();
		notifyObservers(new Insertion(this, children.size() - 1, child));
	}

	/**
	 * add child at position idx.
	 * 
	 * @param idx
	 * @param child
	 */

	final protected void addChildAt(int idx, ModelElement child) {
		child.setParent(this);
		children.add(idx, child);
		fixSlibings(idx);
		setChanged();
		notifyObservers(new Insertion(this, idx, child));
	}

	/**
	 * Creates an empty list for children. Override it to change the children
	 * list implementation.
	 * @return a newly built empty list of children.
	 */

	protected List buildChildrenList() {
		return new ArrayList();
	}

	/**
	 * Build a <em>copy</em> of this object, embedded into a topitem. May
	 * return null if this doesn't mean a thing (in particular, for modifiers).
	 * 
	 * @return a topitem or null.
	 */
	public abstract TopItem buildTopItem();

	// Notifies observers about this element's associated values being modified.
	final protected void changeValue() {
		// IMPORTANT : think about this function
	}

	final protected void clearChildren() {
		ListIterator i = getListIterator();
		while (i.hasNext()) {
			ModelElement child = (ModelElement) i.next();
			child.detachModelElement();
		}
		children.clear();
		// IMPORTANT : change the system for element suppression.
		setChanged();
		notifyObservers(new Modification(this));
	}

	/**
	 * Utility function that compare the classes of two ModelElements. Usable to
	 * implement compareTo.
	 * 
	 * @param o
	 * @return a comparison between two classes.
	 */
	private int compareClass(Object o) {
		if (getClass().equals(o.getClass()))
			return 0;
		else
			return getClass().getName().compareTo(o.getClass().getName());
	}

	/**
	 * Compare the contents of two model elements. Usable to implement
	 * compareToAux. In fact, when this and e have the same type
	 * <em>and</and> are completely defined by their sub elements (i.e. in practice, have no other
	 * field than their children), compareToAux is simply a call to this method.
	 * @param e : a model element, of the exact same type as <code>this</code>.
	 * @return an int, &lt; 0 if this &lt; e, 0 if this and e have the same content; and &gt; 0 if this &gt; e.
	 * @see ModelElement#compareToAux(ModelElement)
	 */
	protected final int compareContents(ModelElement e) {
		int result = 0;
		int n = Math.min(getNumberOfChildren(), e.getNumberOfChildren());
		for (int i = 0; result == 0 && i < n; i++) {
			result = getChildAt(i).compareTo(e.getChildAt(i));
		}
		// the elements up to n are the same, compare the lengthes.
		if (result == 0) {
			result = getNumberOfChildren() - e.getNumberOfChildren();
		}
		return result;
	}

	/**
	 * Skeleton implementation of compareTo.
	 * <p>
	 * Calls compareToAux when the objects have the same real classes.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	final public int compareTo(Object o) {
		int result = compareClass(o);
		if (result == 0)
			result = compareToAux((ModelElement) o);
		return result;
	}

	/**
	 * Function called to compare two model elements that have the
	 * <em>exact</em> same classes. Implementations can securely cast
	 * <code>e</code> to their real type.
	 * 
	 * @param e
	 * @return an int 
	 * @see Comparable#compareTo(java.lang.Object)
	 */
	abstract public int compareToAux(ModelElement e);

	/**
	 * Copy the content of this modelElement into copy. This is an auxiliary
	 * function.
	 * 
	 * @param copy
	 */
	protected void copyContentTo(ModelElement copy) {
		if (children != null) {
			copy.children = buildChildrenList();
			for (int i = 0; i < children.size(); i++)
				// It's important to use addChild here !
				copy.addChild(getChildAt(i).deepCopy());
		}
	}

	/**
	 * Create a copy of this ModelElement.
	 * 
	 * @return a recursive copy of this ModelElement.
	 */
	abstract public ModelElement deepCopy();

	/**
	 * Deconnect an element which was previously a child of this one. This is
	 * cleanup after actual removal.
	 * 
	 * @param child
	 */
	private void detachElement(ModelElement child) {
		// Fix the current element for possible reuse elsewhere :
		child.previous = null;
		child.next = null;
		child.parent = null;
		child.deleteObserver(this);
	}

	/**
	 * Separate a model element from its container.
	 */

	final protected void detachModelElement() {
		if (parent != null)
			deleteObserver(parent);
		parent = null;
		previous = null;
		next = null;
	}

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
		ModelElement child = null;
		ModelElement nextChild = null;
		ModelElement previousChild = null;

		if (id >= 0 && id < children.size())
			child = (ModelElement) children.get(id);
		if (id + 1 >= 0 && id + 1 < children.size())
			nextChild = (ModelElement) children.get(id + 1);
		if (id - 1 >= 0 && id - 1 < children.size())
			previousChild = (ModelElement) children.get(id - 1);
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
	 * @param id
	 * 
	 * @return null if there is no such child.
	 */

	final public ModelElement getChildAt(int id) {
		return (ModelElement) children.get(id);
	}

	final protected String getChildrenAsString() {
		return children.toString();
	}

	final protected ListIterator getListIterator() {
		return children.listIterator();
	}

	final protected ListIterator getListIterator(int idx) {
		return children.listIterator(idx);
	}

	/**
	 * Returns an iterator to the elements included in this one.
	 * 
	 * @return ModelElementIterator
	 */
	final public ModelElementIterator getModelElementIterator() {
		return new ModelElementIterator(children.iterator());
	}

	/**
	 * Get the next element at the same level as this one. If there is no such
	 * element, return null.
	 * 
	 * @return the next element at the same level as this one.
	 */
	public ModelElement getNextSlibing() {
		return next;
	}

	/**
	 * Return the number of children of this element. Elements who have no child
	 * by nature should return 0.
	 * 
	 * @return the number of childs.
	 */

	final public int getNumberOfChildren() {
		return children.size();
	}

	/*public TextDirection getDirection() {
		return TextDirection.LEFT_TO_RIGHT;
	}*/

	/**
	 * Returns the containing element. Might be null.
	 * 
	 * @return the parent element.
	 */
	public ModelElement getParent() {
		return parent;
	}

	/**
	 * Get the previous element at the same level as this one. If there is no
	 * such element, return null.
	 * 
	 * @return the previous element at the same level as this one.
	 */
	public ModelElement getPreviousSlibing() {
		return previous;
	}

	/**
	 * @return true for elements that break the current structure (line breaks,
	 *         page breaks).
	 */
	public boolean isBreak() {
		return false;
	}

	/**
	 * @return true if the element can be streched horizontally.
	 */
	public boolean isFillX() {
		return fillX;
	}

	/**
	 * @return true if the element can be streched vertically.
	 */
	public boolean isFillY() {
		return fillY;
	}

	/**
	 * Warns listeners that a small modification has been done to this element.
	 * (it might be colour change, shading...)
	 */
	protected void notifyModification() {
		setChanged();
		notifyObservers(new Modification(this));
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
		if (id < 0 || id >= children.size())
			return;
		ModelElement child;
		child = (ModelElement) children.get(id);
		children.remove(id);
		fixSlibings(id);
		detachElement(child);
		setChanged();
		notifyObservers(new Deletion(this, id, id + 1));
	}

	/**
	 * Suppress all elements between indexes a and b. Returns the list of the
	 * suppressed elements.
	 * 
	 * @param a
	 * @param b
	 * @return the list of the suppressed elements.
	 */
	protected List removeChildren(int a, int b) {
		List l = new ArrayList(b - a);
		l.addAll(children.subList(a, b));
		for (int i = b - 1; i >= a; i--) {
			ModelElement child = (ModelElement) children.get(i);
			children.remove(i);
			detachElement(child);

		}
		fixSlibings(a);
		setChanged();
		notifyObservers(new Deletion(this, a, b));
		return l;
	}

	final protected void setChildAt(int idx, ModelElement child) {
		ModelElement oldChild = (ModelElement) children.get(idx);
		oldChild.detachModelElement();
		child.setParent(this);
		children.set(idx, child);
		fixSlibings(idx);
		setChanged();
		notifyObservers(new Replacement(this, idx));
	}

	/**
	 * Set the element which contains this one.
	 * 
	 * @param element
	 */
	private void setParent(ModelElement element) {
		parent = element;
		if (parent != null)
			addObserver(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		if (updatesEnabled && arg instanceof ModelOperation) {
			setChanged();
			notifyObservers(new ChildOperation(this, (ModelOperation) arg));
		}
	}
}
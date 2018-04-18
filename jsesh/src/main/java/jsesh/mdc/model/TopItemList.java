package jsesh.mdc.model;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsesh.mdc.interfaces.MDCFileInterface;
import jsesh.mdc.interfaces.TopItemListInterface;
import jsesh.mdc.model.operations.ModelOperation;
import jsesh.mdc.model.operations.ZoneModification;
import jsesh.mdc.output.MdCModelWriter;
import jsesh.mdc.utils.HieroglyphCodesExtractor;
import jsesh.mdc.utils.MDCCodeExtractor;

/**
 * 
 * TopItemList
 * 
 * @author rosmord
 * 
 *         This code is published under the GNU LGPL.
 */

public class TopItemList extends ModelElement implements MDCFileInterface,
		TopItemListInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8950824272164138323L;

	/**
	 * List of observers for this topItemList. Currently, the associated
	 * HieroglyphicTextModel
	 */
	transient List<ModelElementObserver> topItemListObservers;

	/**
	 * The list of MDCMark on this topItemList. To avoid problems with outdated
	 * marks, we have separated them from the usual observers.
	 */
	transient List<MDCMark> marks;

	public TopItemList() {
	}

	public void accept(ModelElementVisitor v) {
		v.visitTopItemList(this);
	}

	/**
	 * Adds a list of elements to this TopItemList. The list must contain only
	 * topitems.
	 * 
	 * @param elements
	 */
	public void addAll(List<TopItem> elements) {
		for (TopItem e : elements) {
			addChild(e);
		}
	}

	/**
	 * Add a list of topitems at a given position in this TopItemList.
	 * 
	 * @param index
	 * @param items
	 * 
	 */
	public void addAllAt(int index, List<? extends TopItem> items) {
		int pos = index;
		for (TopItem e : items) {
			super.addChildAt(pos++, e);
		}
	}

	public void addTopItem(TopItem topItem) {
		addChild(topItem);
	}

	public void addTopItemAt(int id, TopItem topItem) {
		addChildAt(id, topItem);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		return compareContents(e);
	}

	@Override
	public TopItemList deepCopy() {
		TopItemList r = new TopItemList();
		copyContentTo(r);
		return r;
	}

	/**
	 * Return the ith children as a topitem.
	 * 
	 * @param i
	 * @return the ith children as a topitem.
	 */
	public TopItem getTopItemAt(int i) {
		return (TopItem) getChildAt(i);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getChildrenAsString();
	}

	/**
	 * Suppress all elements between a and b. Returns the list of the suppressed
	 * elements. (The list should be given as argument, in order to avoid typing
	 * problems ???).
	 * 
	 * @param a
	 * @param b
	 *            (b > a).
	 * @return Returns the list of the suppressed elements.
	 */
	public List removeTopItems(int a, int b) {
		return removeChildren(a, b);
	}

	/*
	 * (non-Javadoc) TopItem lists are too big to enter a topitem, so the method
	 * always returns null.
	 * 
	 * @see jsesh.mdc.model.ModelElement#buildTopItem()
	 */

	public TopItem buildTopItem() {
		return null;
	}

	/**
	 * Shade or unshade a whole zone.
	 * 
	 * @param a
	 * @param b
	 * @param shade
	 *            if true, shade ; if false, unshade.
	 */
	public void shade(int a, int b, boolean shade) {
		int start = Math.min(a, b);
		int end = Math.max(a, b);

		// We don't want messages from the modified elements.
		// note that currently we won't get any, for state is not linked to its
		// container.
		disableUpdates();
		for (int i = start; i < end; i++) {
			TopItem t = getTopItemAt(i);
			t.getState().setShaded(shade);
		}
		enableUpdates();
		notifyModelElementObservers(new ZoneModification(this, start, end));
	}

	/**
	 * Apply a partial shading (for instance shade the top) to a whole zone.
	 * 
	 * @param a
	 * @param b
	 * @param shadeCode
	 *            value in {@link ShadingCode}
	 * @see #shade(int, int, boolean) for another kind of shading.
	 */
	public void shade(int a, int b, int shadeCode) {
		int start = Math.min(a, b);
		int end = Math.max(a, b);

		// We don't want messages from the modified elements.
		// note that currently we won't get any, for state is not linked to its
		// container.
		disableUpdates();
		for (int i = start; i < end; i++) {
			TopItem t = getTopItemAt(i);
			if (t instanceof Cadrat) {
				// No "zone shading" in this case...
				t.getState().setShaded(false);
				Cadrat c = (Cadrat) t;
				c.setShading(shadeCode);
			}
		}
		enableUpdates();
		notifyModelElementObservers(new ZoneModification(this, start, end));
	}

	/**
	 * paint a zone in black or black. a,b are the zone limits. The system is
	 * the one used for positions. (that is, the indexes fall between elements).
	 * 
	 * @param a
	 * @param b
	 * @param red
	 *            : if true, paint in red ; if false, paint in black.
	 */
	public void setRed(int a, int b, boolean red) {
		int start = Math.min(a, b);
		int end = Math.max(a, b);

		// We don't want messages from the modified elements.
		disableUpdates();
		for (int i = start; i < end; i++) {
			TopItem t = getTopItemAt(i);
			t.getState().setRed(red);
		}
		enableUpdates();
		notifyModelElementObservers(new ZoneModification(this, start, end));
	}

	/**
	 * Returns the number of pages in this topItemList. (currently not
	 * optimized).
	 * 
	 * @return the number of pages in this topItemList.
	 */
	public int getNumberOfPages() {
		int result = 1;
		for (int i = 0; i < getNumberOfChildren(); i++)
			if (getChildAt(i) instanceof PageBreak)
				result++;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdc.model.ModelElement#notifyContainers(jsesh.mdc.model.operations
	 * .ModelOperation)
	 */
	protected void notifyModelElementObservers(ModelOperation op) {
		// First notify marks
		if (marks != null) {
			for (Iterator<MDCMark> it = marks.iterator(); it.hasNext();) {
				MDCMark mark = it.next();
				mark.updateMark(op);
			}
		}
		// Then notify regular observers
		if (topItemListObservers != null) {
			for (Iterator<ModelElementObserver> it = topItemListObservers
					.iterator(); it.hasNext();) {
				ModelElementObserver observer = it.next();
				observer.observedElementChanged(op);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.ModelElement#getParent()
	 */
	public ModelElement getParent() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.ModelElement#unsetContainers()
	 */
	protected void unsetContainers() {
		topItemListObservers = null;
	}

	public ModelElement getNextSlibing() {
		return null;
	}

	public ModelElement getPreviousSlibing() {
		return null;
	}

	/**
	 * @param obs
	 */
	public void deleteObserver(ModelElementObserver obs) {
		if (topItemListObservers != null) {
			topItemListObservers.remove(obs);
			if (topItemListObservers.isEmpty())
				topItemListObservers = null;
		}
	}

	/**
	 * @param obs
	 */
	public void addObserver(ModelElementObserver obs) {
		if (topItemListObservers == null) {
			topItemListObservers = new ArrayList<ModelElementObserver>();
		}
		topItemListObservers.add(obs);
	}

	public void addMark(MDCMark mdcMark) {
		if (marks == null)
			marks = new ArrayList<MDCMark>();
		marks.add(mdcMark);
	}

	public void removeMark(MDCMark mdcMark) {
		if (marks != null) {
			marks.remove(mdcMark);
			if (marks.isEmpty())
				marks = null;
		}
	}

	/**
	 * Returns a MdC Representation for this object.
	 * 
	 * @return
	 */
	public String toMdC() {
		return toMdC(0, getNumberOfChildren());
	}

	/**
	 * Returns a MdC Representation for the cadrats between positions start and
	 * end.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public String toMdC(int start, int end) {
		return toMdC(start, end, false);
	}

	/**
	 * Gets a MdC representation for the quadrants between start and end, with possible normalization.
	 * @param start
	 * @param end
	 * @param normalized if true, phonetic codes will be replaced by their Gardiner counterpart.
	 * @return
	 */
	public String toMdC(int start, int end, boolean normalized) {
		// As requested by the IFAO, we save the Manuel de codage content in
		// the picture as a comment.
		MdCModelWriter mdCModelWriter = new MdCModelWriter();
		mdCModelWriter.setNormalized(normalized);
		StringWriter comment = new StringWriter();
		mdCModelWriter.write(comment, this, start, end);

		String mdcText = comment.toString();
		return mdcText;
	}
	
	/**
	 * Returns a MdC Representation for this object, with possible normalization.
	 * 
	 * @return
	 */
	public String toMdC(boolean normalized) {
		return toMdC(0, getNumberOfChildren(), normalized);
	}
	
	/**
	 * Returns the list of top items between two points.
	 * 
	 * @param min
	 * @param max
	 * @return a <strong>copy</strong> of the items between the two limits.
	 */
	public List<TopItem> getTopItemListBetween(int min, int max) {
		ArrayList<TopItem> result = new ArrayList<TopItem>();
		for (int i = min; i < max; i++) {
			result.add(getTopItemAt(i).deepCopy());
		}
		return result;
	}

	@Override
	public HorizontalListElement buildHorizontalListElement() {		
		// Weird... check if reasonable.
		BasicItemList list= new BasicItemList();
		for (int i = 0; i < getNumberOfChildren(); i++) {
			TopItem item = getTopItemAt(i);
			HorizontalListElement hElt = item.buildHorizontalListElement();
			Cadrat c= new Cadrat();
			HBox box= new HBox();
			box.addHorizontalListElement(hElt);
			c.addHBox(box);
			if (hElt != null) {
				list.addBasicItem(c);
			}
		}
		return new SubCadrat(list);
	}

	/**
	 * Returns a list view of the top item list.
	 * (the elements are copies of the original ones).
	 * @return
	 */
	public List<TopItem> asList() {
		return getTopItemListBetween(0, getNumberOfChildren());
	}


	/**
	 * Returns a list with the gardiner codes of all signs in this text.
	 * @return  a list of Strings.
	 */
	public List<String> getCodes() {
		HieroglyphCodesExtractor extractor= new HieroglyphCodesExtractor(true);
		return extractor.extractHieroglyphs(this);
	}
}

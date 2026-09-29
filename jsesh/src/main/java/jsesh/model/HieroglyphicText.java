package jsesh.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsesh.model.operations.ModelOperation;
import jsesh.model.operations.ZoneModification;
import jsesh.model.tools.HieroglyphCodesExtractor;

/// A complete hieroglyphic text: the root of the model tree.
///
/// Its children are [TopItem]s ([Cadrat]s, [Cartouche]s, line and page
/// breaks, alphabetic text...). Positions in the text ([MDCPosition]) fall
/// between these children.
///
/// This class is the plain text, usable without any editor. For editing
/// (undo/redo, change events), wrap it in a
/// [jsesh.document.HieroglyphicTextModel]; for file metadata, see
/// [jsesh.document.MDCDocument]. To build one from MdC code, use
/// [jsesh.mdcreader.MDCParserModelGenerator].
///
/// Formerly named `TopItemList`.
///
/// @author rosmord
///
/// This code is published under the GNU LGPL.
public class HieroglyphicText extends ModelElement {

    /**
     *
     */
    private static final long serialVersionUID = 8950824272164138323L;

    /**
     * List of observers for this text. Currently, the associated
     * HieroglyphicTextModel
     */
    transient List<ModelElementObserver> textObservers;

    /**
     * The list of MDCMark on this text. To avoid problems with outdated
     * marks, we have separated them from the usual observers.
     */
    transient List<MDCMark> marks;

    public HieroglyphicText() {
    }

    public void accept(ModelElementVisitor v) {
        v.visitHieroglyphicText(this);
    }

    /**
     * Adds a list of elements to this text. The list must contain only
     * topitems.
     *
     * @param elements
     */
    public void addAll(List<? extends TopItem> elements) {
        for (TopItem e : elements) {
            addChild(e);
        }
    }

    /// Adds a list of top items at a given position in this text.
    ///
    /// @param position where to insert the items.
    /// @param items the items to insert.
    public void addAllAt(MDCPosition position, List<? extends TopItem> items) {
        int pos = position.getIndex();
        for (TopItem e : items) {
            super.addChildAt(pos++, e);
        }
    }

    public void addTopItem(TopItem topItem) {
        addChild(topItem);
    }

    /// Adds a top item at a given position in this text.
    ///
    /// @param position where to insert the item.
    /// @param topItem the item to insert.
    public void addTopItemAt(MDCPosition position, TopItem topItem) {
        addChildAt(position.getIndex(), topItem);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.model.ModelElement#compareToAux(jsesh.model.ModelElement)
     */
    public int compareToAux(ModelElement e) {
        return compareContents(e);
    }

    @Override
    public HieroglyphicText deepCopy() {
        HieroglyphicText r = new HieroglyphicText();
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

    /// Removes all elements between two positions.
    ///
    /// @param start the first position.
    /// @param end the last position (`start <= end`).
    /// @return the list of the removed elements.
    public List<TopItem> removeTopItems(MDCPosition start, MDCPosition end) {
        List<TopItem> result = new ArrayList<>();
        for (EmbeddedModelElement e : removeChildren(start.getIndex(), end.getIndex())) {
            result.add((TopItem) e);
        }
        return result;
    }

    /*
	 * (non-Javadoc) TopItem lists are too big to enter a topitem, so the method
	 * always returns null.
	 * 
	 * @see jsesh.model.ModelElement#buildTopItem()
     */
    public TopItem buildTopItem() {
        return null;
    }

    /// Shades or unshades a whole zone. The order of `a` and `b` is not
    /// important.
    ///
    /// @param a one limit of the zone.
    /// @param b the other limit of the zone.
    /// @param shade if true, shade ; if false, unshade.
    public void shade(MDCPosition a, MDCPosition b, boolean shade) {
        int start = Math.min(a.getIndex(), b.getIndex());
        int end = Math.max(a.getIndex(), b.getIndex());

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

    /// Applies a partial shading (for instance shade the top) to a whole zone.
    /// The order of `a` and `b` is not important.
    ///
    /// @param a one limit of the zone.
    /// @param b the other limit of the zone.
    /// @param shadeCode value in [ShadingCode]
    /// @see #shade(MDCPosition, MDCPosition, boolean) for another kind of shading.
    public void shade(MDCPosition a, MDCPosition b, int shadeCode) {
        int start = Math.min(a.getIndex(), b.getIndex());
        int end = Math.max(a.getIndex(), b.getIndex());

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

    /// Paints a zone in red or black. The order of `a` and `b` is not
    /// important.
    ///
    /// @param a one limit of the zone.
    /// @param b the other limit of the zone.
    /// @param red if true, paint in red ; if false, paint in black.
    public void setRed(MDCPosition a, MDCPosition b, boolean red) {
        int start = Math.min(a.getIndex(), b.getIndex());
        int end = Math.max(a.getIndex(), b.getIndex());

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
     * Returns the number of pages in this text. (currently not
     * optimized).
     *
     * @return the number of pages in this text.
     */
    public int getNumberOfPages() {
        int result = 1;
        for (int i = 0; i < getNumberOfChildren(); i++) {
            if (getChildAt(i) instanceof PageBreak) {
                result++;
            }
        }
        return result;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.model.ModelElement#notifyContainers(jsesh.model.operations
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
        if (textObservers != null) {
            for (Iterator<ModelElementObserver> it = textObservers
                    .iterator(); it.hasNext();) {
                ModelElementObserver observer = it.next();
                observer.observedElementChanged(op);
            }
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.model.ModelElement#getParent()
     */
    public ModelElement getParent() {
        return null;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.model.ModelElement#unsetContainers()
     */
    protected void unsetContainers() {
        textObservers = null;
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
        if (textObservers != null) {
            textObservers.remove(obs);
            if (textObservers.isEmpty()) {
                textObservers = null;
            }
        }
    }

    /**
     * @param obs
     */
    public void addObserver(ModelElementObserver obs) {
        if (textObservers == null) {
            textObservers = new ArrayList<>();
        }
        textObservers.add(obs);
    }

    public void addMark(MDCMark mdcMark) {
        if (marks == null) {
            marks = new ArrayList<MDCMark>();
        }
        marks.add(mdcMark);
    }

    public void removeMark(MDCMark mdcMark) {
        if (marks != null) {
            marks.remove(mdcMark);
            if (marks.isEmpty()) {
                marks = null;
            }
        }
    }

    /// Returns a *copy* of the top items between two positions. The order of
    /// `pos1` and `pos2` is not important.
    ///
    /// @param pos1 one limit.
    /// @param pos2 the other limit.
    /// @return a **copy** of the items between the two limits.
    public List<TopItem> getTopItemsBetween(MDCPosition pos1, MDCPosition pos2) {
        int min = Math.min(pos1.getIndex(), pos2.getIndex());
        int max = Math.max(pos1.getIndex(), pos2.getIndex());
        ArrayList<TopItem> result = new ArrayList<TopItem>();
        for (int i = min; i < max; i++) {
            result.add(getTopItemAt(i).deepCopy());
        }
        return result;
    }

    @Override
    public HorizontalListElement buildHorizontalListElement() {
        // Weird... check if reasonable.
        BasicItemList list = new BasicItemList();
        for (int i = 0; i < getNumberOfChildren(); i++) {
            TopItem item = getTopItemAt(i);
            HorizontalListElement hElt = item.buildHorizontalListElement();
            Cadrat c = new Cadrat();
            HBox box = new HBox();
            box.addHorizontalListElement(hElt);
            c.addHBox(box);
            if (hElt != null) {
                list.addBasicItem(c);
            }
        }
        return new SubCadrat(list);
    }

    /**
     * Returns a list view of this text. (the elements are copies of the
     * original ones).
     *
     * @return
     */
    public List<TopItem> asList() {
        return getTopItemsBetween(new MDCPosition(0), getLastPosition());
    }

    /**
     * Returns a list with the gardiner codes of all signs in this text.
     *
     * @return a list of Strings.
     */
    public List<String> getCodes() {
        HieroglyphCodesExtractor extractor = new HieroglyphCodesExtractor(true);
        return extractor.extractHieroglyphs(this);
    }

    /// Returns the line limits for the line around a given position. More
    /// precisely, will return a list of two positions `[pos1, pos2]` around
    /// `pos`, with `pos1 <= pos <= pos2`, where:
    ///
    /// - `pos1` is the position in front of the first element in the line
    ///   containing `pos`;
    /// - `pos2` is the position after the last element in the line containing
    ///   `pos`;
    ///
    /// a line being a list of elements which are not page or line break.
    ///
    /// @param pos a position in the line.
    /// @return a list of two positions.
    public List<MDCPosition> getLineLimitsAround(MDCPosition pos) {
        return List.of(getLineFirstPosition(pos), getLineLastPosition(pos));
    }

    private int getLineEndAfter(int pos) {
        int res = pos;
        while (res < getNumberOfChildren() && !getChildAt(res).isBreak()) {
            res++;
        }
        return res;
    }

    private int getLineStartBefore(int pos) {
        int res = pos - 1; // The element BEFORE position pos is at index pos - 1 in the array
        while (res >= 0 && !getChildAt(res).isBreak()) {
            res--;
        }
        // so, now, we have the index of the new line (or -1 if we fell on the beginning of the text)
        // the position we see is just after that.
        return res + 1;
    }

    /// Returns the k-th position in this text.
    ///
    /// If the position would fall outside of the possible bounds, it will
    /// either be the last position or the first.
    ///
    /// @param k the index of the position.
    /// @return the kth position in this text.
    public MDCPosition getPositionAt(int k) {
        return new MDCPosition(clampIndex(k));
    }

    /// @return the position after the last element of this text.
    public MDCPosition getLastPosition() {
        return new MDCPosition(getNumberOfChildren());
    }

    /// Returns the ith position after a given one. If the position would fall
    /// outside the possible bounds, it will either be the last position or the
    /// first.
    ///
    /// @param position the original position.
    /// @param delta the delta between the two positions. Might be negative.
    /// @return ith position after `position`.
    public MDCPosition getNextPosition(MDCPosition position, int delta) {
        return getPositionAt(position.getIndex() + delta);
    }

    /// @param position a position in this text.
    /// @return the element just after `position`, or null if there is none.
    public TopItem getElementAfter(MDCPosition position) {
        int index = position.getIndex();
        if (index >= getNumberOfChildren()) {
            return null;
        }
        return getTopItemAt(index);
    }

    /// @param position a position in this text.
    /// @return the element just before `position`, or null if there is none.
    public TopItem getElementBefore(MDCPosition position) {
        int index = position.getIndex();
        if (index > getNumberOfChildren() || index == 0) {
            return null;
        }
        return getTopItemAt(index - 1);
    }

    /// @param position a position in this text.
    /// @return true if there is a position after `position` in this text.
    public boolean hasNext(MDCPosition position) {
        return position.getIndex() < getNumberOfChildren();
    }

    /// Returns the first position of the line containing a given position.
    ///
    /// @param position a position in this text.
    /// @return the first position in the line.
    /// @see #getLineLimitsAround(MDCPosition)
    public MDCPosition getLineFirstPosition(MDCPosition position) {
        return new MDCPosition(getLineStartBefore(clampIndex(position.getIndex())));
    }

    /// Returns the last position of the line containing a given position
    /// (i.e. the position just before the line break, if any).
    ///
    /// @param position a position in this text.
    /// @return the last position in the line.
    /// @see #getLineLimitsAround(MDCPosition)
    public MDCPosition getLineLastPosition(MDCPosition position) {
        return new MDCPosition(getLineEndAfter(clampIndex(position.getIndex())));
    }

    /// Finds the position "up" (in a syntactic way) from a given one, that is,
    /// the first position of the previous line.
    ///
    /// @param position a position in this text.
    /// @return the first position of the previous line (or of the current
    /// line, if it's the first one).
    public MDCPosition getUpPosition(MDCPosition position) {
        int p = Math.max(0, clampIndex(position.getIndex()) - 1);
        // Go to the last position of the previous line.
        while (p > 0 && !getTopItemAt(p).isBreak()) {
            p--;
        }
        return new MDCPosition(getLineStartBefore(p));
    }

    /// Finds the position "down" (in a syntactic way) from a given one, that
    /// is, the first position of the next line.
    ///
    /// @param position a position in this text.
    /// @return the first position of the next line (or the last position of
    /// the text, if there is no next line).
    public MDCPosition getDownPosition(MDCPosition position) {
        int n = getNumberOfChildren();
        int p = Math.min(n, clampIndex(position.getIndex()) + 1);
        while (p < n && !getTopItemAt(p - 1).isBreak()) {
            p++;
        }
        return new MDCPosition(p);
    }

    private int clampIndex(int k) {
        return Math.max(0, Math.min(k, getNumberOfChildren()));
    }

    /// Returns the page limits for the page around a given position. More
    /// precisely, will return a list of two positions `[pos1, pos2]` around
    /// `pos`, with `pos1 <= pos <= pos2`, where:
    ///
    /// - `pos1` is the position in front of the first element in the page
    ///   containing `pos`;
    /// - `pos2` is the position after the last element in the page containing
    ///   `pos`;
    ///
    /// a page being a list of elements which are not page break.
    ///
    /// TODO : generalize this. A getLimitsAround method, taking as argument a
    /// boolean test, or specific types of elements, would be ok.
    ///
    /// @param pos a position in the page.
    /// @return a list of two positions.
    public List<MDCPosition> getPageLimitsAround(MDCPosition pos) {
        int index = clampIndex(pos.getIndex());
        return List.of(new MDCPosition(getPageStartBefore(index)),
                new MDCPosition(getPageEndAfter(index)));
    }

    private int getPageEndAfter(int pos) {
        int res = pos;
        while (res < getNumberOfChildren() && !(getChildAt(res) instanceof PageBreak)) {
            res++;
        }
        return res;
    }

    private int getPageStartBefore(int pos) {
        int res = pos - 1; // The element BEFORE position pos is at index pos - 1 in the array
        while (res >= 0 && !(getChildAt(res) instanceof PageBreak)) {
            res--;
        }
        // so, now, we have the index of the new line (or -1 if we fell on the beginning of the text)
        // the position we see is just after that.
        return res + 1;
    }

    /// Gets original line number coordinates of a certain point in the text.
    ///
    /// If the document contains line-number indications, like (vo, 3) which
    /// reference the actual source document (ostracon, papyrus...), this
    /// function will return the coordinates for a given point in text.
    ///
    /// @param position technical position in the JSesh document.
    /// @return the position in the original document, or the empty string if
    /// none is found.
    public String getOriginalDocumentCoordinates(MDCPosition position) {
        int res = clampIndex(position.getIndex()) - 1; // The element BEFORE position pos is at index pos - 1 in the array
        while (res >= 0 && !(getChildAt(res) instanceof Superscript)) {
            res--;
        }
        if (res == -1)
            return "";
        else {
            Superscript superscript = (Superscript) getChildAt(res);
            return superscript.getText();
        }     
    }
}

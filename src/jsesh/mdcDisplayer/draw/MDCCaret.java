/*
 * Created on 31 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdcDisplayer.draw;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsesh.mdc.model.MDCMark;
import jsesh.mdc.model.MDCMarkChangeListener;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;

/**
 * The caret represent the editing position in a text. It takes in account both
 * the <em>insertion point</em> where new signs will be inserted, and an
 * optional <em>mark.</em> The space between the mark (if ant) and the
 * insertion point is the selected area.
 * 
 * <p>
 * Note that both insert and mark can be null.
 * 
 * IMPORTANT : we have just had a problem with a case where a text was changed, but not the caret.
 * We should a) move the HieroglyphicTextModel class to the same package as caret, or maybe even to "model"
 * and b) set up an observer system between the two. 
 * @author S. Rosmorduc
 *  
 */

public class MDCCaret implements MDCMarkChangeListener {

	private MDCMark insert;

	private MDCMark mark;

	private List listeners;

	/**
	 * @param insert
	 */
	public MDCCaret(MDCMark insert) {
		mark = null;
		listeners = new ArrayList();
		setInsert(insert);
	}

	/**
	 * @param model
	 */
	public MDCCaret(TopItemList model) {
		this(new MDCMark(new MDCPosition(model, 0)));
	}

	public MDCMark getInsert() {
		return insert;
	}

	public void setInsert(MDCMark insert) {
		if (this.insert != null) {
			this.insert.removeMarkChangeListener(this);
		}
		this.insert = insert;
		if (insert != null)
			insert.addMarkChangeListener(this);
		notifyCaretListeners();
	}

	public void changeModel(TopItemList model) {
		if (mark != null)
			mark.removeMarkChangeListener(this);
		if (insert != null)
			insert.removeMarkChangeListener(this);
		mark= null;
		setInsert(new MDCMark(new MDCPosition(model, 0)));
	}
	
	public MDCMark getMark() {
		return mark;
	}

	public void setMark(MDCMark newMark) {
		if (mark != null) {
			mark.removeMarkChangeListener(this);
		}
		this.mark = newMark;
		if (mark != null)
			mark.addMarkChangeListener(this);
		notifyCaretListeners();
	}

	public void unsetMark() {
		setMark(null);
	}

	/** 
	 * @return the model
	 */
	public TopItemList getModel() {
		return (TopItemList) insert.getPosition().getTopItemList();
	}

	/**
	 * @param i
	 */
	public void moveInsert(int i) {
		insert.advanceBy(i);
	}

	public void moveInsertTo(int i) {
		insert.setIndex(i);
	}

	public void setMarkAt(int i) {
		setMark(new MDCMark(insert.getPosition().getPositionAt(i)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.MDCMarkChangeListener#markChanged(jsesh.mdc.model.MDCMark)
	 */
	public void markChanged(MDCMark mark) {
		notifyCaretListeners();
	}

	private void notifyCaretListeners() {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			MDCCaretChangeListener l = (MDCCaretChangeListener) i.next();
			l.caretChanged(this);
		}
	}

	public void addCaretChangeListener(MDCCaretChangeListener l) {
		listeners.add(l);
	}

	public void removeCaretChangeListener(MDCCaretChangeListener l) {
		listeners.remove(l);
	}

	/**
	 * @return true if a mark is set.
	 */
	public boolean hasMark() {
		return (mark != null);
	}

	/**
	 * Returns the minimal index for the caret range.
	 * 
	 * @return the minimal index for the caret range.
	 */
	public int getMin() {
		if (hasMark()) {
			return Math.min(getInsert().getIndex(), getMark().getIndex());
		} else {
			return getInsert().getIndex();
		}
	}

	/**
	 * Returns the maximal index for the caret range.
	 * @return the maximal index for the caret range.
	 */
	public int getMax() {
		if (hasMark()) {
			return Math.max(getInsert().getIndex(), getMark().getIndex());
		} else {
			return getInsert().getIndex();
		}
	}

    /**
     * true if some text is selected. 
     * <p> This means that
     * <ol>
     * <li> There is a mark
     * <li> the mark is different from the current position.
     * </ol>
     * @return  true if some text is selected
     * 
     */
    public boolean hasSelection() {        
        return hasMark() && (getMin() != getMax());
    }
}
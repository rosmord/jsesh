/*
 * Created on 31 oct. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.editor.caret;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsesh.mdc.model.MDCMark;
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
 * We could a) move the HieroglyphicTextModel class to the same package as caret, or maybe even to "model"
 * and b) set up an observer system between the two. Another solution : if marks are immutable (in the sense that
 * one can't move them except by changing the text), there is no need for mark listeners. Caret can still change, but in this case, we are in control.
 * 
 *  
 * // TODO : use named marks, in order to avoid any memory leak problem.
 * @author S. Rosmorduc
 *  
 */

public class MDCCaret {

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

	/**
	 * Returns a caret which selects the whole text.
	 * @param model
	 * @return
	 */
	public static MDCCaret buildWholeTextCaret(TopItemList model) {
		MDCCaret result= new MDCCaret(model);
		result.moveInsertTo(0);
		result.setMarkAt(model.getNumberOfChildren());
		return result;
	}
	
	public MDCMark getInsert() {
		return insert;
	}

	public MDCPosition getInsertPosition() {
		return insert.getPosition();
	}
	
	public MDCPosition getMarkPosition() {
		return mark.getPosition();
	}
	
	public void setInsert(MDCMark insert) {
		/*
		if (this.insert != null) {
			this.insert.removeMarkChangeListener(this);
		}*/
		if (this.insert != null)
			this.insert.release();
		this.insert = insert;
		/*if (insert != null)
			insert.addMarkChangeListener(this);*/
		notifyCaretListeners();
	}

	public void changeModel(TopItemList model) {
		// We want to generate only one caretChanged event. Hence, we delete the mark by hand.
		if (mark != null)
			mark.release();
		mark= null;
		setInsert(new MDCMark(new MDCPosition(model, 0)));
	}
	
	public MDCMark getMark() {
		return mark;
	}

	public void setMark(MDCMark newMark) {
		/*
		if (mark != null) {
			mark.removeMarkChangeListener(this);
		}*/
		if (mark != null)
			mark.release();
		this.mark = newMark;
		/*if (mark != null)
			mark.addMarkChangeListener(this);*/
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
	 * move the insertion location by a certain amount of elements
	 * @param i
	 */
	public void moveInsertBy(int i) {
		setInsertPosition(insert.getNextPosition(i));
	}

	/**
	 * move the insertion location to a certain place.
	 * @param i
	 */
	public void moveInsertTo(int i) {
		setInsertPosition(insert.getPositionAt(i));
	}

	public void setMarkAt(int i) {
		setMark(new MDCMark(insert.getPosition().getPositionAt(i)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdc.model.MDCMarkChangeListener#markChanged(jsesh.mdc.model.MDCMark)
	 *
	public void markChanged(MDCMark mark) {
		notifyCaretListeners();
	}*/

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
	 * Returns the maximal position for the caret range.
	 * @return the maximal position for the caret range.
	 */
	public MDCPosition getMaxPosition() {
		return new MDCPosition(insert.getTopItemList(), getMax());
	}
	
	/**
	 * Returns the minimal position for the caret range.
	 */
	
	/**
	 * Returns the maximal position for the caret range.
	 * @return the maximal position for the caret range.
	 */
	public MDCPosition getMinPosition() {
		return new MDCPosition(insert.getTopItemList(), getMin());
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

	public void setInsertPosition(MDCPosition p) {
		MDCMark newInsert= new MDCMark(p);
		setInsert(newInsert);
	}

	public void advanceInsertBy(int dir) {
		MDCPosition pos= insert.getNextPosition(dir);
		setInsertPosition(pos);
	}
	
}
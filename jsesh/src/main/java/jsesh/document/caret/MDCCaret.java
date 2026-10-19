/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
/*
 * Created on 31 oct. 2004
 */
package jsesh.document.caret;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jsesh.model.CaretGranularity;
import jsesh.model.MDCMark;
import jsesh.model.MDCPosition;
import jsesh.model.HieroglyphicText;
import jsesh.model.PositionNavigator;

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

	private List<MDCCaretChangeListener> listeners;

	/**
	 * @param insert
	 */
	public MDCCaret(MDCMark insert) {
		mark = null;
		listeners = new ArrayList<MDCCaretChangeListener>();
		setInsert(insert);
	}

	/**
	 * @param model
	 */
	public MDCCaret(HieroglyphicText model) {
		this(new MDCMark(model, new MDCPosition(0)));
	}

	/**
	 * Returns a caret which selects the whole text.
	 * @param model
	 * @return
	 */
	public static MDCCaret buildWholeTextCaret(HieroglyphicText model) {
		MDCCaret result= new MDCCaret(model);
		result.setMarkPosition(model.getLastPosition());
		return result;
	}
	
	public MDCMark getInsert() {
		return insert;
	}

	/// The insertion position, as a top-level position. If the insertion point
	/// lies inside a quadrat (see [#isInsertInner()]), this is the position just
	/// before this quadrat: all editing operations work at this level.
	///
	/// @return a top-level position.
	public MDCPosition getInsertPosition() {
		return insert.getTopLevelPosition();
	}

	/// The mark position, as a top-level position (see [#getInsertPosition()]).
	///
	/// @return a top-level position.
	public MDCPosition getMarkPosition() {
		return mark.getTopLevelPosition();
	}

	/// @return the exact insertion position, which may be nested if the
	///         insertion point lies inside a quadrat.
	public MDCPosition getInsertExactPosition() {
		return insert.getPosition();
	}

	/// @return true if the insertion point lies inside a quadrat, and not between
	///         two top-level items.
	public boolean isInsertInner() {
		return insert.isInner();
	}

	/// Moves the insertion point by a number of stops, taking into account the
	/// positions inside quadrats (see [jsesh.model.CaretGranularity#INNER]). The
	/// result is clamped to the text bounds.
	///
	/// @param delta the number of stops to move by (may be negative).
	public void moveInsertInner(int delta) {
		setInsertPosition(PositionNavigator.move(getModel(), insert.getPosition(), delta, CaretGranularity.INNER));
	}

	/// If the insertion point lies inside a quadrat, moves it to the position
	/// just before this quadrat. Does nothing otherwise.
	public void snapToTopLevel() {
		if (insert.isInner()) {
			setInsertPosition(insert.getTopLevelPosition());
		}
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

	public void changeModel(HieroglyphicText model) {
		// We want to generate only one caretChanged event. Hence, we delete the mark by hand.
		if (mark != null)
			mark.release();
		mark= null;
		setInsert(new MDCMark(model, new MDCPosition(0)));
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
	public HieroglyphicText getModel() {
		return insert.getHieroglyphicText();
	}

	/// Moves the insertion location by a certain amount of elements. The
	/// result is clamped to the text bounds.
	///
	/// @param delta the number of positions to move by (may be negative).
	public void moveInsertBy(int delta) {
		setInsertPosition(insert.getNextPosition(delta));
	}

	/// Sets the mark at a given position (clamped to the text bounds).
	///
	/// @param p the new mark position.
	public void setMarkPosition(MDCPosition p) {
		setMark(new MDCMark(getModel(), p));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.model.MDCMarkChangeListener#markChanged(jsesh.model.MDCMark)
	 *
	public void markChanged(MDCMark mark) {
		notifyCaretListeners();
	}*/

	private void notifyCaretListeners() {
		for (Iterator<MDCCaretChangeListener> i = listeners.iterator(); i.hasNext();) {
			MDCCaretChangeListener l = i.next();
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

	/// Returns the minimal position for the caret range (the insert position
	/// if there is no mark).
	///
	/// @return the minimal position for the caret range.
	public MDCPosition getMinPosition() {
		MDCPosition p = getInsertPosition();
		if (hasMark() && getMarkPosition().compareTo(p) < 0) {
			p = getMarkPosition();
		}
		return p;
	}

	/// Returns the maximal position for the caret range (the insert position
	/// if there is no mark).
	///
	/// @return the maximal position for the caret range.
	public MDCPosition getMaxPosition() {
		MDCPosition p = getInsertPosition();
		if (hasMark() && getMarkPosition().compareTo(p) > 0) {
			p = getMarkPosition();
		}
		return p;
	}

    /// True if some text is selected. This means that
    ///
    /// 1. there is a mark;
    /// 2. the mark is different from the current position.
    ///
    /// @return true if some text is selected
    public boolean hasSelection() {
        return hasMark() && !getMarkPosition().equals(getInsertPosition());
    }

	public void setInsertPosition(MDCPosition p) {
		MDCMark newInsert= new MDCMark(getModel(), p);
		setInsert(newInsert);
	}

}
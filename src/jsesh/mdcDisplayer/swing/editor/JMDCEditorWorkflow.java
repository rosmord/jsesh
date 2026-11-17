/*
 * Created on 30 sept. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 * 
 * TODO: find a better inner representation, perhaps a state-oriented one, to manage data like the "currentSeparator".
 *       The same kind of information might be used to keep the horizontal position for up/down movement.
 * 		 Basically: we always clear the states, except in some particular transitions 
 * 2005/05/03 : now, most moves and significant changes clear the "currentSeparator".
 * 	We will get a more intuitive behaviour.
 * 
 **/
package jsesh.mdcDisplayer.swing.editor;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import jsesh.hieroglyphs.HieroglyphsManager;
import jsesh.hieroglyphs.PossibilitiesList;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.constants.WordEndingCode;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.BasicItemList;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.Cartouche;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.Ligature;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.MDCMark;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelWriter;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.Philology;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.model.operations.ModelOperation;
import jsesh.mdc.utils.MDCSyntaxError;
import jsesh.mdcDisplayer.draw.DrawUtilities;
import jsesh.mdcDisplayer.draw.MDCCaret;
import jsesh.mdcDisplayer.draw.MDCCaretChangeListener;
import jsesh.mdcDisplayer.draw.MDCEditorKit;

/**
 * An abstract represention of the editing process of a hieroglyphic text.
 * <p>
 * far too large a class.
 * 
 * @author rosmord
 */

public class JMDCEditorWorkflow implements Observer, MDCCaretChangeListener {

	/**
	 * The data from the last cut or copy. Should move somewhere else, and be
	 * integrated in the normal java copy/paste scheme;
	 */

	static private List copyBuffer;

	private MDCCaret caret;

	private StringBuffer currentCode;

	private char currentSeparator = ' ';

	private HieroglyphicTextModel hieroglyphicTextModel;

	private List listeners;

	/**
	 * Mode is one of 's', 'l', 'i', 'b', 't', for respectively "hieroglyphs",
	 * "latin", "italic", "bold", "transliteration".
	 */

	private char mode;

	private PossibilitiesList possibilities;

	public JMDCEditorWorkflow(HieroglyphicTextModel data) {
		hieroglyphicTextModel = data;
		hieroglyphicTextModel.addObserver(this);
		caret = new MDCCaret(data.getModel());
		caret.addCaretChangeListener(this);
		currentCode = new StringBuffer("");
		listeners = new ArrayList();
		mode = 's';
	}

	/**
	 * @param key
	 */
	private void addAlphabeticChar(char key) {
		possibilities = null;
		// First, deal with backspace.
		if (key == 8) {
			if (caret.getInsert().hasPrevious()) {
				TopItem t = caret.getInsert().getElementBefore();
				if (t instanceof AlphabeticText) {
					AlphabeticText txt = (AlphabeticText) t;
					// If t is now empty, suppress t
					if (txt.getText().length() == 0) {
						doBackspace();
					} else {
						// Suppress only one char.

						txt.setText(txt.getText().substring(0,
								txt.getText().length() - 1));
						// Now, t might become empty, and it's better to supress
						// it anyway.
						if (txt.getText().length() == 0)
							doBackspace();
					}
				} else {
					doBackspace();
				}
			}
		} else {
			boolean addNew = false;
			if (!caret.getInsert().hasPrevious())
				addNew = true;
			else {
				TopItem t = caret.getInsert().getElementBefore();
				if (t instanceof AlphabeticText
						&& ((AlphabeticText) t).getScriptCode() == mode) {
					AlphabeticText txt = (AlphabeticText) t;
					txt.setText(txt.getText() + key);
				} else {
					addNew = true;
				}
			}
			if (addNew)
				insertElement(new AlphabeticText(mode, "" + key));
		}
		clearSeparator();
	}

	/**
	 * @param type
	 * @param start
	 * @param end
	 * @return true if success.
	 */
	public boolean addCartouche(int type, int start, int end) {
		boolean result;
		BasicItemListGrouper grouper = new BasicItemListGrouper();
		List elts = suppressSelection();
		BasicItemList l = grouper.extractBasicItemList(elts);
		if (l != null) {
			Cartouche c = new Cartouche(type, start, end, l);
			hieroglyphicTextModel.getModel().addTopItemAt(
					caret.getInsert().getIndex(), c);
			result = true;
		} else {
			pasteList(elts);
			result = false;
		}
		clearSeparator();
		return result;
	}

	/**
	 * public void addCursorChangeListener(MDCCaretChangeListener l) {
	 * caret.addCaretChangeListener(l); }
	 */

	/**
	 * Add the sign with the current code at the lower level of the cadrat
	 * before the insertion point.
	 */
	public void addLowerLevel() {
		TopItemList l = hieroglyphicTextModel.getModel();
		ModelElement elt = l.getChildAt(getIndexPosition() - 1);
		if (elt instanceof Cadrat) {
			Cadrat c = (Cadrat) elt;
			HBox hbox = new HBox();
			Hieroglyph hiero = buildHieroglyphFromCode(currentCode.toString());
			hbox.addInnerGroup(hiero);
			c.addHBox(hbox);
			clearCode();
			clearSeparator();
		}
	}

	/**
	 * @param l
	 */
	public void addMDCModelListener(MDCModelEditionListener l) {
		listeners.add(l);
	}

	/**
	 * Adds a philological markup around the selected zone. Respects the current
	 * model's view of philological markup. If markup considered as parenthesis,
	 * it will be add so. Else, two signs will be added.
	 * 
	 * @param type
	 *            one of SymbolCodes.ERASEDSIGNS, EDITORADDITION,
	 *            EDITORSUPERFLUOUS, PREVIOUSLYREADABLE or SCRIBEADDITION.
	 * @return true if success, false if failure.
	 */
	public boolean addPhilologicalMarkup(int type) {
		boolean result;
		possibilities = null;
		if (getHieroglyphicTextModel().isPhilologyIsSign()) {
			int max = caret.getMax();
			int min = caret.getMin();
			// See the SymbolCodes for the explanation of the magic *2 and *2+1
			// below.
			hieroglyphicTextModel.getModel().addTopItemAt(max,
					new Hieroglyph(type * 2 + 1).buildTopItem());
			hieroglyphicTextModel.getModel().addTopItemAt(min,
					new Hieroglyph(type * 2).buildTopItem());
			result = true;
		} else {
			BasicItemListGrouper grouper = new BasicItemListGrouper();
			List elts = suppressSelection();
			BasicItemList l = grouper.extractBasicItemList(elts);
			if (l != null) {
				Philology c = new Philology(type, l);
				hieroglyphicTextModel.getModel().addTopItemAt(
						caret.getInsert().getIndex(), c.buildTopItem());
				result = true;
			} else {
				pasteList(elts);
				result = false;
			}
		}
		clearSeparator();
		return result;
	}

	/**
	 * 
	 */
	public void addSameLevel() {
		TopItemList l = hieroglyphicTextModel.getModel();
		ModelElement elt = l.getChildAt(getIndexPosition() - 1);
		if (elt instanceof Cadrat) {
			Cadrat c = (Cadrat) elt;
			// get last hbox.
			HBox hbox = c.getHBox(c.getNumberOfChildren() - 1);
			// add sign :
			Hieroglyph hiero = buildHieroglyphFromCode(currentCode.toString());
			hbox.addInnerGroup(hiero);
			clearCode();
			clearSeparator();
		}
	}

	/**
	 * Add a separator to the code.
	 * <p>
	 * The separator is used to groups new signs ; it can be '*', '-', ' ', ':'
	 * or '&'. The exact effect depends on currentCode and on the presence of a
	 * previous separator.
	 * 
	 * <p>
	 * if currentCode is empty and there is no current separator, the separator
	 * becomes the current separator. if there is a current separator, the
	 * current separator is used to group the last two signs before the
	 * separator, and the new separator becomes the current separator.
	 * 
	 * If currentCode is not empty, add the corresponding glyph to the model,
	 * according to the current separator, and then make sep the current
	 * separator.
	 * 
	 * @param sep
	 */
	public void addSeparator(char sep) {
		if (currentCode.length() != 0) {
			addSign();
		}
		groupBy(currentSeparator);
		currentSeparator = sep;		
		notifyCodeChangeListeners();
		notifySeparatorChangeListeners();
	}

	/**
	 * Adds the sign whose code is "currentCode" at the cursor position.
	 */
	public void addSign() {
		// No empty sign.
		if (getCurrentCode().length() == 0)
			return;		
		possibilities = HieroglyphsManager.getInstance().getPossibilityFor(
				currentCode.toString());
		if (possibilities == null)
			addSign(currentCode.toString());
		else
			addSign(possibilities.getCurrentSign());
	}

	/**
	 * @param code
	 */
	public void addSign(String code) {
		TopItemList t = hieroglyphicTextModel.getModel();
		Cadrat cadrat = new Cadrat();
		HBox hbox = new HBox();
		cadrat.addHBox(hbox);
		hbox.addInnerGroup(buildHieroglyphFromCode(code));
		t.addTopItemAt(getIndexPosition(), cadrat);
		clearCode();
	}

	/**
	 * @param c
	 */
	public void addToCode(char c) {
		possibilities = null;
		currentCode.append(c);
		notifyCodeChangeListeners();
	}

	/**
	 * Build a "plain" absolute group with the selected text, or return an
	 * absolute group from last cadrat. Returns a <em>copy</em> of this group.
	 * 
	 * @return Returns a <em>working copy</em> of the created group, or null
	 *         if nothing was created.
	 */
	public AbsoluteGroup buildAbsoluteGroup() {
		AbsoluteGroup result = null;
		if (!caret.hasMark() && getIndexPosition() >= 1)
			caret.setMarkAt(getIndexPosition() - 1);
		if (caret.hasMark()) {
			List elts = suppressSelection();
			HieroglyphExtractor extractor = new HieroglyphExtractor();

			// FIXME : drawing specifications alert !!!
			AbsoluteGroup g = DrawUtilities.createAbsoluteGroupFrom(elts,
					MDCEditorKit.getBasicMDCEditorKit()
							.getDrawingSpecifications());
			if (g.getNumberOfChildren() != 0) {
				hieroglyphicTextModel.getModel().addTopItemAt(
						caret.getInsert().getIndex(), g.buildTopItem());
				result = (AbsoluteGroup) g.deepCopy();
			} else {
				pasteList(elts);
			}
		}
		return result;
	}

	private Hieroglyph buildHieroglyphFromCode(String code) {
		Hieroglyph hiero;
		if ("o".equals(code))
			hiero= new Hieroglyph(SymbolCodes.REDPOINT);
		else
			hiero = new Hieroglyph(code);
		return hiero;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.MDCCaretChangeListener#caretChanged(jsesh.mdcDisplayer.draw.MDCCaret)
	 */
	public void caretChanged(MDCCaret caret) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			MDCModelEditionListener l = (MDCModelEditionListener) i.next();
			l.caretChanged(caret);
			// clearSeparator();
		}
	}

	/**
	 * Set the angle for the last hieroglyph.
	 * 
	 * @param angle
	 */
	public void changeAngle(int angle) {
		Hieroglyph h = getLastHieroglyph();
		if (h != null)
			h.setAngle(angle);
	}

	/**
	 * Clear the text in the current model.
	 */
	public void clear() {
		try {
			possibilities = null;
			setMDCCode("");
			clearSeparator();
		} catch (MDCSyntaxError e) {
			// Should not happen
			throw new RuntimeException(e);
		}
		currentCode.setLength(0);
		clearSeparator();
	}

	public void clearCode() {
		currentCode.setLength(0);
		notifyCodeChangeListeners();
	}

	/**
	 * 
	 */
	public void clearMark() {
		possibilities = null;
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * Resets the current separator so that next sign appears on a cadrat of his
	 * own.
	 * 
	 */
	public void clearSeparator() {
		currentSeparator = ' ';
		notifySeparatorChangeListeners();
	}

	/**
	 * 
	 */
	public void copy() {
		possibilities = null;
		if (caret.hasMark()) {
			int a = Math.min(caret.getInsert().getIndex(), caret.getMark()
					.getIndex());
			int b = Math.max(caret.getInsert().getIndex(), caret.getMark()
					.getIndex());
			copyBuffer = new ArrayList();
			for (int i = a; i < b; i++) {
				copyBuffer.add(hieroglyphicTextModel.getModel().getChildAt(i)
						.deepCopy());
			}
		}
	}

	/**
	 * Moves the cursor down one line.
	 */
	public void cursorDown() {
		possibilities = null;
		MDCPosition p = caret.getInsert().getPosition();
		p = p.getNextPosition(1);
		while (p.hasNext() && !p.getElementBefore().isBreak())
			p = p.getNextPosition(1);
		caret.getInsert().setPosition(p);
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * Moves the cursor to the next cadrat.
	 */
	public void cursorNext() {
		possibilities = null;
		caret.moveInsert(1);
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * Moves the cursor to the previous cadrat.
	 * 
	 */
	public void cursorPrevious() {
		possibilities = null;
		caret.moveInsert(-1);
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * move to cursor to the beginning of the current line.
	 */
	public void cursorToBeginningOfLine() {
		possibilities = null;
		MDCPosition p = getLineFirstPosition();
		caret.getInsert().setPosition(p);
		clearSeparator();
	}

	/**
	 * move to the end of the current line.
	 */
	public void cursorToEndOfLine() {
		possibilities = null;
		MDCPosition p = getLineLastPosition();
		caret.getInsert().setPosition(p);
	}

	/**
	 * Moves the cursor up one line.
	 * <p>
	 * Currently, we move to the beginning of the line. In the future, it might
	 * be interesting to retain the horizontal position and to try to keep it.
	 */
	public void cursorUp() {
		possibilities = null;
		MDCPosition p = caret.getInsert().getPosition();
		p = p.getNextPosition(-1);
		// Go to the last position of the previous line.
		while (p.hasPrevious() && !p.getElementAfter().isBreak())
			p = p.getNextPosition(-1);
		caret.getInsert().setPosition(p);
		cursorToBeginningOfLine();
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * 
	 */
	public void cut() {
		List l = suppressSelection();
		if (l != null) {
			copyBuffer = l;
		}
		clearSeparator();
	}

	public void deleteCodeChangeListener(MDCModelEditionListener l) {
		listeners.remove(l);
	}

	public void deleteCursorChangeListener(MDCCaretChangeListener l) {
		caret.removeCaretChangeListener(l);
	}

	/**
	 * Erase the last character of the current code.
	 */
	public void doBackspace() {
		possibilities = null;
		if (currentCode.length() > 0) {
			currentCode.replace(currentCode.length() - 1, currentCode.length(),
					"");
			notifyCodeChangeListeners();
		} else if (currentSeparator != ' ') {
			clearSeparator();
		}
		else {
			removeTopItem();
		}
		clearSeparator();
	}

	/**
	 * @param shade
	 */
	public void doShade(int shade) {
		possibilities = null;
		if (caret.getInsert().hasPrevious()) {
			TopItem t = caret.getInsert().getElementBefore();
			if (t instanceof Cadrat) {
				Cadrat c = (Cadrat) t;
				c.setShading(shade);
			}
		}
	}

	/**
	 * Expand selection left (dir= -1) or right (dir=1).
	 * 
	 * @param dir
	 */
	public void expandSelection(int dir) {
		if (!caret.hasMark()) {
			caret.setMark(new MDCMark(caret.getInsert().getPosition()));
		}
		caret.getInsert().advanceBy(dir);
	}

	/**
	 * separate the current group into smaller ones.
	 */
	public void explodeGroup() {
		boolean success = false;
		if (caret.getInsert().getIndex() < 1)
			return;
		GroupExploder f = new GroupExploder();
		List elts = getHieroglyphicTextModel().getModel().removeTopItems(
				caret.getInsert().getIndex() - 1, caret.getInsert().getIndex());
		if (elts.size() == 1) {
			List result = f.explode((TopItem) elts.get(0));
			pasteList(result);
		} else {
			pasteList(elts);
		}
		clearSeparator();
	}

	/**
	 * Replaces the previous cadrat with g.
	 * 
	 * @param g
	 */
	public void fixAbsoluteGroup(AbsoluteGroup g) {
		clearSeparator();
		TopItem t = getCurrentItem();
		if (t != null && t instanceof Cadrat) {
			removeTopItem();
			// IMPORTANT : We should decide where (and if) we compact the group.
			g.compact();
			hieroglyphicTextModel.getModel().addTopItemAt(getIndexPosition(),
					g.buildTopItem());
		}
	}

	/**
	 * 
	 */
	public void focusGained() {
		for (int i = 0; i < listeners.size(); i++) {
			MDCModelEditionListener listener = (MDCModelEditionListener) listeners
					.get(i);
			listener.focusGained(currentCode);
		}

	}

	/**
	 * 
	 */
	public void focusLost() {
		for (int i = 0; i < listeners.size(); i++) {
			MDCModelEditionListener listener = (MDCModelEditionListener) listeners
					.get(i);
			listener.focusLost();
		}

	}

	public MDCCaret getCaret() {
		return caret;
	}

	/**
	 * @return the current glyph code being entered.
	 * 
	 */
	public StringBuffer getCurrentCode() {
		return currentCode;
	}

	/**
	 * Returns the topitem at index position, or null if none.
	 * 
	 * @return Returns the topitem at index position, or null if none.
	 */
	public TopItem getCurrentItem() {
		TopItem t = null;
		if (caret.getInsert().hasPrevious()) {
			t = caret.getInsert().getElementBefore();
		}
		return t;
	}

	/**
	 * Returns the content of the current line as manuel de codage code.
	 * 
	 * @return the content of the current line as manuel de codage code.
	 */
	public String getCurrentLineAsString() {
		int idx = getIndexPosition();
		TopItemList t = getHieroglyphicTextModel().getModel();
		int[] limits = getLineLimits();

		StringWriter sw = new StringWriter();
		ModelWriter w = new ModelWriter();
		w.write(sw, t, limits[0], limits[1]);
		return sw.toString();
	}

	/**
	 * @return Returns the currentSeparator.
	 */
	public char getCurrentSeparator() {
		return currentSeparator;
	}

	public HieroglyphicTextModel getHieroglyphicTextModel() {
		return hieroglyphicTextModel;
	}

	private int getIndexPosition() {
		return caret.getInsert().getPosition().getIndex();
	}

	/**
	 * Returns the last hieroglyph element in the topitem before index mark, or
	 * null.
	 * 
	 * @return the last hieroglyph element in the topitem before index mark, or
	 *         null.
	 */
	public Hieroglyph getLastHieroglyph() {
		Hieroglyph result = null;
		TopItem t = getCurrentItem();
		if (t != null) {
			LastHieroglyphSelector selector = new LastHieroglyphSelector();
			result = selector.findLastHieroglyph(t);
		}
		return result;
	}

	/**
	 * Returns the first position in the current line.
	 * 
	 * @return the first position in the current line.
	 */
	private MDCPosition getLineFirstPosition() {
		MDCPosition first = caret.getInsert().getPosition();
		while (first.hasPrevious() && !first.getElementBefore().isBreak())
			first = first.getNextPosition(-1);
		return first;
	}

	/**
	 * Returns the last position in the current line.
	 * 
	 * @return the last position in the current line.
	 */
	private MDCPosition getLineLastPosition() {
		MDCPosition second = caret.getInsert().getPosition();

		while (second.hasNext() && !second.getElementAfter().isBreak())
			second = second.getNextPosition(1);
		return second;
	}

	/**
	 * Returns the limits of the current line, including the final -! or -!! if
	 * any.
	 * 
	 * @return the limits of the current line.
	 */
	private int[] getLineLimits() {
		int[] result = new int[2];
		int a, b;
		int idx = getIndexPosition();
		TopItemList t = getHieroglyphicTextModel().getModel();

		MDCPosition first = getLineFirstPosition();

		MDCPosition second = getLineLastPosition();

		if (second.hasNext())
			second = second.getNextPosition(1);

		result[0] = first.getIndex();
		result[1] = second.getIndex();
		return result;
	}

	/**
	 * Return the manuel de codage text for the current model.
	 * 
	 * @return the manuel de codage text for the current model.
	 */
	public String getMDCCode() {
		StringWriter sw = new StringWriter();
		ModelWriter w = new ModelWriter();
		w.write(sw, getHieroglyphicTextModel().getModel());
		return sw.toString();
	}

	/**
	 * @return Returns the mode.
	 */
	public char getMode() {
		return mode;
	}

	/**
	 * Group the two groups before the insert mark according to this separator
	 * 
	 * @param key
	 *            one of '-', ' ' ; '*' , ':' or '&'.
	 */
	private void groupBy(char key) {
		switch (key) {
		case ' ':
		case '-':
			// No - Op !
			break;
		case '*':
			if (caret.getInsert().getIndex() > 1)
				insertLastCadratIntoBeforeLast();
			break;
		case ':':
			if (caret.getInsert().getIndex() > 1)
				groupLastTwoVertically();
			break;
		case '&':
			if (caret.getInsert().getIndex() > 1)
				ligatureLastTwoElements();
			break;
		default: // Should not happen.
			throw new RuntimeException("bad separator " + key);
		}
	}

	/**
	 * Group the elements in the selection in an hbox (and then in a cadrat).
	 * Can only work if it's possible to fetch innergroups from these elements.
	 * 
	 * @return true if success.
	 */
	public boolean groupHorizontal() {
		boolean result;
		HorizontalGrouper f = new HorizontalGrouper();
		List elts = suppressSelection();
		Cadrat c = f.buildCadrat(elts);
		if (c != null) {
			hieroglyphicTextModel.getModel().addTopItemAt(
					caret.getInsert().getIndex(), c);
			result = true;
		} else {
			pasteList(elts);
			result = false;
		}
		clearSeparator();
		return result;
	}

	/**
	 * stack the last two cadrats.
	 */

	private void groupLastTwoVertically() {
		VerticalGrouper f = new VerticalGrouper();
		List elts = getHieroglyphicTextModel().getModel().removeTopItems(
				caret.getInsert().getIndex() - 2, caret.getInsert().getIndex());
		Cadrat c = f.buildCadrat(elts);
		if (c != null) {
			hieroglyphicTextModel.getModel().addTopItemAt(
					caret.getInsert().getIndex(), c);
		} else {
			pasteList(elts);
		}
		clearSeparator();
	}

	/**
	 * Stack the elements in the selection on top of each other.
	 * <p>
	 * All these elements should be basic items. For cadrats, the hboxes will be
	 * directly copied into the new cadrat. AlphabeticText will be stored in
	 * SubCadrat and stacked.
	 * 
	 * @return true if success.
	 */
	public boolean groupVertical() {
		boolean result;
		VerticalGrouper v = new VerticalGrouper();
		List elts = suppressSelection();
		Cadrat c = v.buildCadrat(elts);
		if (c != null) {
			hieroglyphicTextModel.getModel().addTopItemAt(
					caret.getInsert().getIndex(), c);
			result = true;
		} else {
			pasteList(elts);
			result = false;
		}
		clearSeparator();
		return result;
	}

	/**
	 * Insert a modelElement in the text. This element will be duplicated, so
	 * the base element can be considered as a prototype. This will only work on
	 * elements which can be inserted in a text, that is, elements which are not
	 * TopItemLists or modifiers.
	 * 
	 * @param e
	 */
	public void insertElement(ModelElement e) {
		possibilities = null;
		getHieroglyphicTextModel().getModel().addTopItemAt(
				caret.getInsert().getIndex(), e.buildTopItem());
	}

	/**
	 * 
	 */
	public void insertHalfSpace() {
		possibilities = null;
		Hieroglyph h = new Hieroglyph(SymbolCodes.HALFSPACE);
		hieroglyphicTextModel.getModel().addTopItemAt(
				caret.getInsert().getIndex(), h.buildTopItem());
	}

	/**
	 * group the last two cadrat horizontally, inserting the second in the
	 * <em>lower</em> row of the first.
	 * 
	 */
	private void insertLastCadratIntoBeforeLast() {
		CadratStarInserter f = new CadratStarInserter();
		List elts = getHieroglyphicTextModel().getModel().removeTopItems(
				caret.getInsert().getIndex() - 2, caret.getInsert().getIndex());
		Cadrat c = f.buildCadrat((TopItem) elts.get(0), (TopItem) elts.get(1));
		if (c != null) {
			hieroglyphicTextModel.getModel().addTopItemAt(
					caret.getInsert().getIndex(), c);
		} else {
			pasteList(elts);
		}
	}

	/**
	 * @param mdcText
	 */
	public void insertMDC(String mdcText) {
		try {
			possibilities = null;
			getHieroglyphicTextModel().insertMDCText(
					caret.getInsert().getIndex(), mdcText);
		} catch (MDCSyntaxError e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 */
	public void insertNewLine() {
		possibilities = null;
		addSeparator(' ');
		getHieroglyphicTextModel().getModel().addTopItemAt(getIndexPosition(),
				new LineBreak());

	}

	/**
	 * 
	 */
	public void insertPageBreak() {
		possibilities = null;
		addSeparator(' ');
		getHieroglyphicTextModel().getModel().addTopItemAt(getIndexPosition(),
				new PageBreak());
	}

	/**
	 * 
	 */
	public void insertSpace() {
		possibilities = null;
		Hieroglyph h = new Hieroglyph(SymbolCodes.FULLSPACE);
		hieroglyphicTextModel.getModel().addTopItemAt(
				caret.getInsert().getIndex(), h.buildTopItem());
	}

	/**
	 * @param key
	 * @return true if the typed key correspond to a possible code character..
	 */
	public boolean isCodeChar(char key) {
		return (Character.isLetterOrDigit(key) || key == '\'');
	}

	/**
	 * @param key
	 * @return true if key corresponds to a MDC separator.
	 */
	public boolean isSeparatorChar(char key) {
		return (key == ' ') || (key == '-') || (key == ':') || (key == '*')
				|| (key == '&');
	}

	/**
	 * Send a char to the current editing process. The char will be processed
	 * according to the current state of the workflow.
	 * 
	 * @param key
	 * 
	 */
	public void keyTyped(char key) {
		if (mode == 's') {
			if (currentSeparator == ' ' && key == ' '
					&& currentCode.length() == 0) {
				nextPossibility();
			} else if (key == 8) {
				doBackspace();
			} else if (key == ',') {
				// Shortcut for Ff1. There should be a more general system, but
				// right now, I want my Ff1 sign !
				currentCode.replace(0, currentCode.length(), "Ff1");
			} else if (isSeparatorChar(key)) {
				if (key == '-')
					key = ' ';
				addSeparator(key);
			} else if (isCodeChar(key)) {
				{
					addToCode(key);
				}
			}
		} else {
			addAlphabeticChar(key);
		}
	}

	/**
	 * Ligature all the elements in the selection.
	 * 
	 * @return true if success
	 */
	public boolean ligatureElements() {
		boolean result = false;
		List elts = suppressSelection();
		HieroglyphExtractor extractor = new HieroglyphExtractor();
		List hieros = extractor.extractHieroglyphs(elts);
		if (hieros != null && hieros.size() > 1) {
			Ligature lig = new Ligature();
			for (Iterator it = hieros.iterator(); it.hasNext();) {
				Hieroglyph h = (Hieroglyph) it.next();
				lig.addHieroglyph((Hieroglyph) h.deepCopy());
			}
			hieroglyphicTextModel.getModel().addTopItemAt(
					caret.getInsert().getIndex(), lig.buildTopItem());
			result = true;
		} else {
			pasteList(elts);
			result = false;
		}
		clearSeparator();
		return result;
	}

	/**
	 * Build a ligature with the last two elements. IMPORTANT : improve this.
	 * The ligature should take place with the last element of the previous
	 * cadrat.
	 */
	private void ligatureLastTwoElements() {
		// bored now. I use the mark !
		caret.setMarkAt(caret.getInsert().getIndex() - 2);
		// If enough elements are selected :
		if (caret.getMax() - caret.getMin() == 2) {
			ligatureElements();
		}
		clearSeparator();
	}

	public void nextPossibility() {
		if (possibilities != null) {
			Hieroglyph h = getLastHieroglyph();
			if (h != null) {
				possibilities.next();
				h.setCode(possibilities.getCurrentSign());
			}
		}
	}

	private void notifyCodeChangeListeners() {
		for (int i = 0; i < listeners.size(); i++) {
			MDCModelEditionListener listener = (MDCModelEditionListener) listeners
					.get(i);
			listener.codeChanged(currentCode);
		}

	}

	private void notifySeparatorChangeListeners() {
		for (int i = 0; i < listeners.size(); i++) {
			MDCModelEditionListener listener = (MDCModelEditionListener) listeners
					.get(i);
			listener.separatorChanged();
		}
	}
	
	/**
	 * 
	 */
	public void paste() {
		possibilities = null;
		if (copyBuffer != null) {
			pasteList(copyBuffer);
			clearMark();
		}
		clearSeparator();
	}

	/**
	 * Utility function. Paste a list of TopItems into the model.
	 * 
	 * @param elts
	 */
	private void pasteList(List elts) {
		List l = new ArrayList(elts.size());
		for (Iterator i = elts.iterator(); i.hasNext();)
			l.add(((TopItem) i.next()).deepCopy());
		hieroglyphicTextModel.getModel().addAllAt(caret.getInsert().getIndex(),
				l);
		clearSeparator();
	}

	/**
	 * @param b
	 */
	public void redZone(boolean b) {
		possibilities = null;
		if (caret.hasMark())
			hieroglyphicTextModel.getModel()
					.setRed(caret.getInsert().getIndex(),
							caret.getMark().getIndex(), b);
		clearSeparator();

	}

	public void removeTopItem() {
		possibilities = null;
		TopItemList l = hieroglyphicTextModel.getModel();
		l.removeTopItem(getIndexPosition() - 1);
		clearSeparator();
	}

	/**
	 * Changes the size of the current sign
	 * 
	 * @param size
	 */
	public void resizeSign(int size) {
		Hieroglyph h = getLastHieroglyph();
		if (h != null) {
			h.setRelativeSize(size);
		}
	}

	/**
	 * 
	 */
	public void reverseSign() {
		Hieroglyph h = getLastHieroglyph();
		if (h != null)
			h.setReversed(!h.isReversed());
	}

	/**
	 * 
	 */
	public void selectAll() {
		possibilities = null;
		caret.moveInsertTo(0);
		caret.setMarkAt(hieroglyphicTextModel.getModel().getNumberOfChildren());
		clearSeparator();
	}

	/**
	 * Sets the content of the current line from a manuel de codage encoding.
	 * 
	 * @param text
	 * @return true if success.
	 */
	public boolean setCurrentLineTo(String text) {
		possibilities = null;
		boolean success = true;
		// ok. This is an awful cut and paste from getCurrentMDCLine.
		TopItemList t = getHieroglyphicTextModel().getModel();
		int limits[] = getLineLimits();

		try {
			List items = getHieroglyphicTextModel().buildItems(text);
			getHieroglyphicTextModel().getModel().removeTopItems(limits[0],
					limits[1]);
			getHieroglyphicTextModel().getModel().addAllAt(limits[0], items);
			caret.getInsert().setIndex(limits[0]);
		} catch (MDCSyntaxError e) {
			success = false;
		}
		clearSeparator();
		return success;

	}

	/**
	 * @param position
	 */
	public void setCursor(MDCPosition position) {
		if (position != null) {
			possibilities = null;
			caret.setInsert(new MDCMark(position));
			clearSeparator();
		}
	}

	/**
	 * Set mark at position.
	 * 
	 * @param position
	 */
	public void setMark(MDCPosition position) {
		if (position != null) {
			possibilities = null;
			caret.setMark(new MDCMark(position));
			clearSeparator();
		}
	}

	/**
	 * 
	 */
	public void setMarkToCursor() {
		possibilities = null;
		caret.setMarkAt(caret.getInsert().getIndex());
		clearSeparator();
	}

	// Set the data of the current model to String
	public void setMDCCode(String txt) throws MDCSyntaxError {
		possibilities = null;
		getHieroglyphicTextModel().setMDCCode(txt);
		clearSeparator();
	}

	/**
	 * Sets the writing mode : hieroglyphs, latin, etc.
	 * 
	 * @param mode
	 *            one of 's', 'l', 'i', 'b', 't', for respectively
	 *            "hieroglyphs", "latin", "italic", "bold", "transliteration".
	 */
	public void setMode(char mode) {
		possibilities = null;
		this.mode = mode;
		clearSeparator();
	}

	/**
	 * 
	 */
	public void setSignIsAtSentenceEnd() {
		Hieroglyph h = getLastHieroglyph();
		if (h != null)
			h.setEndingCode(WordEndingCode.SENTENCE_END);
	}

	/**
	 * 
	 */
	public void setSignIsAtWordEnd() {
		Hieroglyph h = getLastHieroglyph();
		if (h != null)
			h.setEndingCode(WordEndingCode.WORD_END);
	}

	/**
	 * 
	 */
	public void setSignIsInsideWord() {
		Hieroglyph h = getLastHieroglyph();
		if (h != null)
			h.setEndingCode(WordEndingCode.NONE);
	}

	/**
	 * shade or unshade selected zone, depending the value of <code>shade</code>.
	 * 
	 * @param shade
	 * 
	 */
	public void shadeZone(boolean shade) {
		possibilities = null;
		if (caret.hasMark())
			hieroglyphicTextModel.getModel().shade(
					caret.getInsert().getIndex(), caret.getMark().getIndex(),
					shade);
		clearSeparator();
	}

	/**
	 * Auxilliary function used for manipulating the selection.
	 * 
	 * @return the list of the elements formerly in the selection, or null if no
	 *         selection was available.
	 */
	private List suppressSelection() {
		possibilities = null;
		List result = null;
		if (caret.hasMark()) {
			int a = Math.min(caret.getInsert().getIndex(), caret.getMark()
					.getIndex());
			int b = Math.max(caret.getInsert().getIndex(), caret.getMark()
					.getIndex());
			result = hieroglyphicTextModel.getModel().removeTopItems(a, b);
			clearMark();
		}
		clearSeparator();
		return result;
	}

	/**
	 * 
	 */
	public void toggleGrammar() {
		Hieroglyph h = getLastHieroglyph();
		if (h != null)
			h.setGrammar(!h.isGrammar());
	}

	public void toggleIgnoredSign() {
		Hieroglyph h = getLastHieroglyph();
		if (h != null)
			h.getModifiers().setBoolean("i", !h.getModifiers().getBoolean("i"));
	}

	/**
	 * 
	 */
	public void toggleRedSign() {
		Hieroglyph h = getLastHieroglyph();
		if (h != null)
			h.getModifiers().setBoolean("red",
					!h.getModifiers().getBoolean("red"));
	}

	public void toggleWideSign() {
		Hieroglyph h = getLastHieroglyph();
		if (h != null)
			h.getModifiers().setBoolean("l", !h.getModifiers().getBoolean("l"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		if (arg == null) {
			// A new Text has been loaded.
			// We set up the caret, but this should be placed in the caret
			// itself !
			caret.changeModel(hieroglyphicTextModel.getModel());

			for (Iterator i = listeners.iterator(); i.hasNext();) {
				MDCModelEditionListener l = (MDCModelEditionListener) i.next();
				l.textChanged();
			}
			clearSeparator();
		} else if (arg instanceof ModelOperation) {
			ModelOperation op = (ModelOperation) arg;
			for (Iterator i = listeners.iterator(); i.hasNext();) {
				MDCModelEditionListener l = (MDCModelEditionListener) i.next();
				l.textEdited(op);
			}
		} else {
			throw new RuntimeException("should not happen.");
		}
	}
}
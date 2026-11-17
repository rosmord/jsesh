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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import jsesh.hieroglyphs.DefaultHieroglyphsManager;
import jsesh.hieroglyphs.PossibilitiesList;
import jsesh.hieroglyphs.SignDescriptionConstants;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.SymbolCodes;
import jsesh.mdc.constants.WordEndingCode;
import jsesh.mdc.model.AbsoluteGroup;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.BasicItemList;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.Cartouche;
import jsesh.mdc.model.ComplexLigature;
import jsesh.mdc.model.HBox;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.InnerGroup;
import jsesh.mdc.model.Ligature;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.MDCMark;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.Philology;
import jsesh.mdc.model.Superscript;
import jsesh.mdc.model.TextContainer;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.model.operations.ModelOperation;
import jsesh.mdc.model.utilities.BasicItemListGrouper;
import jsesh.mdc.model.utilities.CadratStarInserter;
import jsesh.mdc.model.utilities.ComplexLigatureExtractor;
import jsesh.mdc.model.utilities.GroupExploder;
import jsesh.mdc.model.utilities.HieroglyphExtractor;
import jsesh.mdc.model.utilities.HorizontalGrouper;
import jsesh.mdc.model.utilities.InnerGroupBuilder;
import jsesh.mdc.model.utilities.LastHieroglyphSelector;
import jsesh.mdc.model.utilities.VerticalGrouper;
import jsesh.mdc.output.MdCModelWriter;
import jsesh.mdcDisplayer.layout.MDCEditorKit;
import jsesh.mdcDisplayer.mdcView.AbsoluteGroupBuilder;
import jsesh.mdcDisplayer.secondaryData.MDCCaret;
import jsesh.mdcDisplayer.secondaryData.MDCCaretChangeListener;

/**
 * An abstract represention of the editing process of a hieroglyphic text.
 * <p>
 * far too large a class.
 * <p>We should separate parts of it, and move the actual manipulations in the model. We would 
 * only keep what is relevant for us (e.g. possibility lists for signs, undo, etc...)
 * 
 * TODO : cleanup. find unused methods from previous versions, and suppress them.
 * 
 * @author rosmord
 */

public class JMDCEditorWorkflow implements Observer, MDCCaretChangeListener {

	/**
	 * The data from the last cut or copy. Should move somewhere else, and be
	 * integrated in the normal java copy/paste scheme;
	 */

	//static private List copyBuffer;

	private MDCCaret caret;

	private StringBuffer currentCode;

	private char currentSeparator = ' ';

	private HieroglyphicTextModel hieroglyphicTextModel;

	private List listeners;
	
	/**
	 * Is it possible to modify this text, or is this simply a view?
	 */
	
	private boolean readWrite= true;

	/**
	 * Mode is one of 's', 'l', 'i', 'b', 't', '|' for respectively
	 * "hieroglyphs", "latin", "italic", "bold", "transliteration", and
	 * "page/line number".
	 */

	private char mode;

	private PossibilitiesList possibilities;

	private UndoManager undoManager;
	
	public JMDCEditorWorkflow(HieroglyphicTextModel data) {
		hieroglyphicTextModel = data;
		hieroglyphicTextModel.addObserver(this);
		caret = new MDCCaret(data.getModel());
		caret.addCaretChangeListener(this);
		currentCode = new StringBuffer("");
		listeners = new ArrayList();
		mode = 's';
		undoManager= new UndoManager();
	}

	/**
	 * Method called when a regular letter is added to the text.
	 * 
	 * @param key
	 */
	private void addAlphabeticChar(char key) {
		possibilities = null;
		// First, deal with backspace.
		if (key == 8) {
			doBackspace();
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
				} else if (t instanceof Superscript && mode == '|') {
					Superscript txt= (Superscript) t;
					txt.setText(txt.getText() + key);
				} else {
					addNew = true;
				}
			}
			if (addNew) {
				if (mode != '|')
					insertElement(new AlphabeticText(mode, "" + key));
				else
					insertElement(new Superscript(""+ key));
			}
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
			getHieroglyphicTextModel().getModel().addTopItemAt(
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
			getHieroglyphicTextModel().getModel().addTopItemAt(max,
					new Hieroglyph(type * 2 + 1).buildTopItem());
			getHieroglyphicTextModel().getModel().addTopItemAt(min,
					new Hieroglyph(type * 2).buildTopItem());
			result = true;
		} else {
			BasicItemListGrouper grouper = new BasicItemListGrouper();
			List elts = suppressSelection();
			BasicItemList l = grouper.extractBasicItemList(elts);
			if (l != null) {
				Philology c = new Philology(type, l);
				getHieroglyphicTextModel().getModel().addTopItemAt(
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
	 * Adds a sign coorresponding to currentCode to the cadrat before the
	 * insertion point.
	 */
	public void addSameLevel() {
		TopItemList l = getHieroglyphicTextModel().getModel();
		ModelElement elt = l.getChildAt(getInsertPosition() - 1);
		if (elt instanceof Cadrat) {
			Cadrat c = (Cadrat) elt;
			// get last hbox.
			HBox hbox = c.getHBox(c.getNumberOfChildren() - 1);
			// add sign :
			Hieroglyph hiero = buildHieroglyphFromCode(currentCode.toString());
			hbox.addHorizontalListElement(hiero);
			clearCode();
			clearSeparator();
		}
	}

	/**
	 * Add a separator to the code.
	 * <p>
	 * The separator is used to groups new signs ; it can be '*', ' ', ':'
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
		possibilities = DefaultHieroglyphsManager.getInstance().getPossibilityFor(
				currentCode.toString(), SignDescriptionConstants.KEYBOARD);
		if (possibilities == null || possibilities.isEmpty())
			addSign(currentCode.toString());
		else
			addSign(possibilities.getCurrentSign());
	}

	/**
	 * @param code
	 */
	public void addSign(String code) {
		TopItemList t = getHieroglyphicTextModel().getModel();
		Cadrat cadrat = new Cadrat();
		HBox hbox = new HBox();
		cadrat.addHBox(hbox);
		hbox.addHorizontalListElement(buildHieroglyphFromCode(code));
		t.addTopItemAt(getInsertPosition(), cadrat);
		clearCode();
	}

	/**
	 * Add the sign with the current code at the lower level of the cadrat
	 * before the insertion point.
	 * @deprecated
	 */
	public void addSignInLowerLevel() {
		TopItemList l = getHieroglyphicTextModel().getModel();
		ModelElement elt = l.getChildAt(getInsertPosition() - 1);
		if (elt instanceof Cadrat) {
			Cadrat c = (Cadrat) elt;
			HBox hbox = new HBox();
			Hieroglyph hiero = buildHieroglyphFromCode(currentCode.toString());
			hbox.addHorizontalListElement(hiero);
			c.addHBox(hbox);
			clearCode();
			clearSeparator();
		}
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
		if (!caret.hasMark() && getInsertPosition() >= 1)
			caret.setMarkAt(getInsertPosition() - 1);
		if (caret.hasMark()) {
			List elts = suppressSelection();

			// FIXME : drawing specifications alert !!!
			AbsoluteGroup g = AbsoluteGroupBuilder.createAbsoluteGroupFrom(elts,
					MDCEditorKit.getBasicMDCEditorKit()
							.getDrawingSpecifications());
			if (g.getNumberOfChildren() != 0) {
				getHieroglyphicTextModel().getModel().addTopItemAt(
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
			hiero = new Hieroglyph(SymbolCodes.REDPOINT);
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
	 * 
	 * public void copy() { possibilities = null; if (caret.hasMark()) { int a =
	 * Math.min(caret.getInsert().getIndex(), caret.getMark() .getIndex()); int
	 * b = Math.max(caret.getInsert().getIndex(), caret.getMark() .getIndex());
	 * copyBuffer = new ArrayList(); for (int i = a; i < b; i++) {
	 * copyBuffer.add(hieroglyphicTextModel.getModel().getChildAt(i)
	 * .deepCopy()); } } }
	 */

	/**
	 * Moves the cursor down one line.
	 */
	public void cursorDown() {
		possibilities = null;
		MDCPosition p = caret.getInsertPosition();
		p = p.getNextPosition(1);
		while (p.hasNext() && !p.getElementBefore().isBreak())
			p = p.getNextPosition(1);
		caret.setInsertPosition(p);
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * Moves the cursor to the next cadrat.
	 */
	public void cursorNext() {
		possibilities = null;
		caret.moveInsertBy(1);
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * Moves the cursor to the previous cadrat.
	 * 
	 */
	public void cursorPrevious() {
		possibilities = null;
		caret.moveInsertBy(-1);
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * move to cursor to the beginning of the current line.
	 */
	public void cursorToBeginningOfLine() {
		possibilities = null;
		MDCPosition p = getLineFirstPosition();
		caret.setInsertPosition(p);
		clearSeparator();
	}

	/**
	 * move to the end of the current line.
	 */
	public void cursorToEndOfLine() {
		possibilities = null;
		MDCPosition p = getLineLastPosition();
		caret.setInsertPosition(p);
	}

	/**
	 * Moves the cursor up one line.
	 * <p>
	 * Currently, we move to the beginning of the line. In the future, it might
	 * be interesting to retain the horizontal position and to try to keep it.
	 */
	public void cursorUp() {
		possibilities = null;
		MDCPosition p = caret.getInsertPosition();
		p = p.getNextPosition(-1);
		// Go to the last position of the previous line.
		while (p.hasPrevious() && !p.getElementAfter().isBreak())
			p = p.getNextPosition(-1);
		caret.setInsertPosition(p);
		cursorToBeginningOfLine();
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * Deletes the current selection.
	 */
	public void cut() {
		suppressSelection();
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
		} else {
			
			// Code to deal with text elements. may disappear one day if we decide that letters are first-class citizens.
			if (caret.getInsert().hasPrevious() && caret.getInsert().getElementBefore() instanceof TextContainer)
			{
				removeSingleLetter();
			}
			else
				removeTopItem();
		}
		clearSeparator();
	}

	/**
	 * Ligature one or two group(s) and a hieroglyph.
	 * <p>
	 * The group before signPost is inserted in the first ligature position, the
	 * group after signPos in the second.
	 * 
	 * @param signPos
	 *            position of the hieroglyph. -1 stands for "last position".
	 */
	public void doComplexLigature(int signPos) {
		boolean success = false;
		List elts = suppressSelection();
		if (elts == null)
			return;
		if (elts.size() > 1 && signPos < elts.size()) {
			InnerGroup group1 = null;
			InnerGroup group2 = null;
			Hieroglyph h = null;

			if (signPos == -1)
				signPos = elts.size() - 1;

			// See if the main element is already a ligature
			ComplexLigatureExtractor ligatureExtractor = new ComplexLigatureExtractor();
			ligatureExtractor.extract((ModelElement) elts.get(signPos));
			if (!ligatureExtractor.foundOtherElements()
					&& ligatureExtractor.getComplexLigature() != null) {
				ComplexLigature c1 = ligatureExtractor.getComplexLigature();
				if ((InnerGroup) c1.getBeforeGroup() != null)
					group1 = (InnerGroup) c1.getBeforeGroup().deepCopy();
				if ((InnerGroup) c1.getAfterGroup() != null)
					group2 = (InnerGroup) c1.getAfterGroup().deepCopy();
				h = (Hieroglyph) c1.getHieroglyph().deepCopy();
			}

			// If the element is not already a ligature, it might be a
			// hieroglyph.

			if (h == null) {
				// The element should be a lone hieroglyph or a complexLigature,
				// in which the
				// previous elements will be ligatured.
				HieroglyphExtractor extractor = new HieroglyphExtractor();
				List hieros = extractor.extractHieroglyphs(elts.subList(
						signPos, signPos + 1));
				if (hieros.size() == 1) {
					h = (Hieroglyph) hieros.get(0);
				}
			}

			// We should not ligature if the ligature slot is already occupied.
			boolean error = false;
			if (h != null) {
				// Build first group with elements in front of the sign
				if (signPos > 0) {
					if (group1 == null) {

						InnerGroupBuilder innerGroupBuilder = new InnerGroupBuilder();
						innerGroupBuilder.buildHorizontalElement(elts.subList(
								0, signPos));
						group1 = innerGroupBuilder.getGroup();
					} else {
						error = true;
					}
				}
				// Build second group with elements after the sign
				if (signPos < elts.size() - 1) {
					if (group2 == null) {
						InnerGroupBuilder innerGroupBuilder = new InnerGroupBuilder();
						innerGroupBuilder.buildHorizontalElement(elts.subList(
								signPos + 1, elts.size()));
						group2 = innerGroupBuilder.getGroup();
					} else {
						error = true;
					}
				}
				if (!error && (group1 != null || group2 != null)) {
					ComplexLigature ligature = new ComplexLigature(group1, h,
							group2);
					getHieroglyphicTextModel().getModel().addTopItemAt(
							caret.getInsert().getIndex(),
							ligature.buildTopItem());
					success = true;
				}

			}
		}

		if (!success) {
			pasteList(elts);
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
			caret.setMark(new MDCMark(caret.getInsertPosition()));
		}
		caret.advanceInsertBy(dir);
	}

	/**
	 * separate the current group into smaller ones.
	 */
	public void explodeGroup() {
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
			getHieroglyphicTextModel().getModel().addTopItemAt(getInsertPosition(),
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
		TopItemList t = getHieroglyphicTextModel().getModel();
		int[] limits = getLineLimits();

		StringWriter sw = new StringWriter();
		MdCModelWriter w = new MdCModelWriter();
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

	private int getInsertPosition() {
		return caret.getInsertPosition().getIndex();
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
		MDCPosition first = caret.getInsertPosition();
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
		MDCPosition second = caret.getInsertPosition();

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
		MdCModelWriter w = new MdCModelWriter();
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
	 * @return
	 */
	public TopItemList getSelectionAsTopItemList() {
		TopItemList topItemList = new TopItemList();
		if (caret.hasMark()) {
			int a = Math.min(caret.getInsert().getIndex(), caret.getMark()
					.getIndex());
			int b = Math.max(caret.getInsert().getIndex(), caret.getMark()
					.getIndex());
			for (int i = a; i < b; i++) {
				topItemList.addTopItem((TopItem) (getHieroglyphicTextModel()
						.getModel().getChildAt(i).deepCopy()));
			}
		}
		return topItemList;
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
			getHieroglyphicTextModel().getModel().addTopItemAt(
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
			getHieroglyphicTextModel().getModel().addTopItemAt(
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
			getHieroglyphicTextModel().getModel().addTopItemAt(
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
		insertElementAt(e, caret.getInsert().getIndex());
	}
	
	/**
	 * Insert a new element at a given place in the text.
	 * @param e
	 * @param position
	 */
	public void insertElementAt(ModelElement e, int position) {
		possibilities = null;
		getHieroglyphicTextModel().getModel().addTopItemAt(position, e.buildTopItem());
	}

	
	/**
	 * Insert a list of TopItems in the text.
	 * 
	 * @param elements
	 *            a
	 */
	public void insertElements(List elements) {
		possibilities = null;
		getHieroglyphicTextModel().getModel().addAllAt(getInsertPosition(),
				elements);
	}

	/**
	 * 
	 */
	public void insertHalfSpace() {
		possibilities = null;
		Hieroglyph h = new Hieroglyph(SymbolCodes.HALFSPACE);
		insertElement(h);
	}

	/**
	 * group the last two cadrat horizontally, inserting the second in the
	 * <em>lower</em> row of the first.
	 */
	private void insertLastCadratIntoBeforeLast() {
		CadratStarInserter f = new CadratStarInserter();
		List elts = getHieroglyphicTextModel().getModel().removeTopItems(
				caret.getInsert().getIndex() - 2, caret.getInsert().getIndex());
		Cadrat c = f.buildCadrat((TopItem) elts.get(0), (TopItem) elts.get(1));
		if (c != null) {
			getHieroglyphicTextModel().getModel().addTopItemAt(
					caret.getInsert().getIndex(), c);
			// insertElement(c); InsertElement is used to insert a new element. We want to change an existing element.
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
			getHieroglyphicTextModel().insertMDCText( getInsertPosition(), mdcText);
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
		insertElement(new LineBreak());
	}

	/**
	 * 
	 */
	public void insertPageBreak() {
		possibilities = null;
		addSeparator(' ');
		insertElement(new PageBreak());
	}

	/**
	 * 
	 */
	public void insertSpace() {
		possibilities = null;
		Hieroglyph h = new Hieroglyph(SymbolCodes.FULLSPACE);
		insertElement(h);
	}

	/**
	 * @param key
	 * @return true if the typed key correspond to a possible code character..
	 */
	public boolean isCodeChar(char key) {
		return (Character.isLetterOrDigit(key) || key == '\'' || key == '-');
	}

	/**
	 * Is it possible to modify this text, or is this simply a view?
	 * @return the readWrite
	 */
	public boolean isReadWrite() {
		return readWrite;
	}

	/**
	 * @param key
	 * @return true if key corresponds to a MDC separator.
	 */
	public boolean isSeparatorChar(char key) {
		return (key == ' ') ||  (key == ':') || (key == '*')
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
	 * 
	 */
	public void ligatureAfter() {
		doComplexLigature(0);
	}

	/**
	 * 
	 */
	public void ligatureBefore() {
		doComplexLigature(-1);

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
			insertElement(lig);
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
		// TODO: avoid creating empty possibility lists.
		// TODO: avoid creating null possibility lists
		if (possibilities != null && ! possibilities.isEmpty()) {
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
	 * Utility function. Paste a list of TopItems into the model.
	 * 
	 * @param elts
	 */
	private void pasteList(List elts) {
		List l = new ArrayList(elts.size());
		for (Iterator i = elts.iterator(); i.hasNext();)
			l.add(((TopItem) i.next()).deepCopy());
		insertElements(l);
		clearSeparator();
	}

	public void redo() {
		if (undoManager.canRedo())
			undoManager.redo();
	}

	/**
	 * @param b
	 */
	public void redZone(boolean b) {
		possibilities = null;
		if (caret.hasMark())
			getHieroglyphicTextModel().getModel()
					.setRed(caret.getInsert().getIndex(),
							caret.getMark().getIndex(), b);
		clearSeparator();

	}

	/**
	 * Remove a single letter from the text element in front of the cursor.
	 * Erase the element if it becomes empty.
	 */
	private void removeSingleLetter() {
		if (caret.getInsert().hasPrevious()) {
			TopItem t = caret.getInsert().getElementBefore();
			// TODO : FIND A BETTER OO ORGANIZATION.
			if (t instanceof TextContainer) {
				TextContainer txt = (TextContainer) t;
				// If t is or would be  empty, suppress t
				if (txt.getText().length() <= 1) {
					removeTopItem();
				} else {			
					TextContainer newText= (TextContainer) txt.deepCopy();
					// Suppress only one char.
					newText.setText(txt.getText().substring(0,
							txt.getText().length() - 1));
					// Replace the old text with the new one.
					replaceElement(caret.getInsert().getNextPosition(-1), (TopItem)newText);
				}
			}
		}
	}

	private void replaceElement(MDCPosition pos1, TopItem newElt) {
		replaceElements(pos1, pos1.getNextPosition(1), Collections.singletonList(newElt));
	}

	/**
	 * Replaces old elements between pos1 and pos2 by the ones in list.
	 * @param pos1
	 * @param pos2
	 * @param newTopItems
	 */
	private void replaceElements(MDCPosition pos1, MDCPosition pos2, List newTopItems) {
		boolean clean= getHieroglyphicTextModel().isClean();
		CompositeCommand command= new CompositeCommand(clean);
		RemoveCommand removeCommand= new RemoveCommand(getHieroglyphicTextModel().getModel(), pos1, pos2.getIndex() - pos1.getIndex(), false);
		InsertCommand insertCommand= new InsertCommand(getHieroglyphicTextModel().getModel(),newTopItems, pos1, false);
		
		command.addCommand(removeCommand);
		command.addCommand(insertCommand);
		undoManager.doCommand(command);
	}
	
	public void removeTopItem() {
		possibilities = null;
		TopItemList l = getHieroglyphicTextModel().getModel();
		l.removeTopItem(getInsertPosition() - 1);
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
		caret.setMarkAt(getHieroglyphicTextModel().getModel().getNumberOfChildren());
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
			// caret.getInsert().setIndex(limits[0]);
			caret.setInsertPosition(new MDCPosition(t, limits[0]));
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
	 *            one of 's', 'l', 'i', 'b', 't', '|' for respectively
	 *            "hieroglyphs", "latin", "italic", "bold", "transliteration", "line number".
	 */
	public void setMode(char mode) {
		possibilities = null;
		this.mode = mode;
		clearSeparator();
	}

	/**
	 * Choose between readonly and readwrite object.
	 * @param readWrite the readWrite to set
	 */
	public void setReadWrite(boolean readWrite) {
		this.readWrite = readWrite;
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
			getHieroglyphicTextModel().getModel().shade(
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
			result = getHieroglyphicTextModel().getModel().removeTopItems(a, b);
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

	public void undo() {
		if (undoManager.canUndo())
			undoManager.undoCommand();
		
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
			caret.changeModel(getHieroglyphicTextModel().getModel());

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
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
 * Created on 30 sept. 2004 by rosmord
 * TODO: find a better inner representation, perhaps a state-oriented one, to manage data like the "currentSeparator".
 *       The same kind of information might be used to keep the horizontal position for up/down movement.
 * 		 Basically: we always clear the states, except in some particular transitions
 * 2005/05/03 : now, most moves and significant changes clear the "currentSeparator".
 * 	We will get a more intuitive behaviour.
 *
 **/
package jsesh.editor;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Consumer;

import jsesh.editor.caret.MDCCaret;
import jsesh.editor.caret.MDCCaretChangeListener;
import jsesh.hieroglyphs.data.Possibility;
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
import jsesh.mdc.model.ShadingCode;
import jsesh.mdc.model.Superscript;
import jsesh.mdc.model.TextContainer;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdc.model.operations.ModelOperation;
import jsesh.mdc.utils.*;
import jsesh.mdcDisplayer.layout.MDCEditorKit;
import jsesh.mdcDisplayer.mdcView.AbsoluteGroupBuilder;

/**
 * An abstract represention of the editing process of a hieroglyphic text.
 * <p>
 * far too large a class.
 * <p>
 * We should separate parts of it, and move the actual manipulations in the
 * model. We would only keep what is relevant for us (e.g. possibility lists for
 * signs, undo, etc...)
 * <p>
 * TODO : current version: sign insertion should be done in two steps: a) find
 * what text needs to be written b) replace the "old text" by this new text TODO
 * : in a second step, try to implement the "events contain undo commands"
 * system. It's likely that the current system is too precise. We always go
 * through TextModel, and the events can be generated at that level (and not at
 * the model element actual level, which is probably too fine ?)
 * <p>
 * TODO : cleanup. find unused methods from previous versions, and suppress
 * them.
 * <p>
 * TODO: the editor should be able to work on MDCDocuments, in order to capture
 * the text orientation IN the model (or move the orientation in the
 * HieroglyphicTextModel ?? (LOGICAL THING TO DO!!!)
 *
 * @author rosmord
 */
public class JMDCEditorWorkflow implements Observer, MDCCaretChangeListener {


	private interface TopItemModifier {

		void modifyTopItem(TopItem topItem);
	}


	// NOTE : each method which has been reread for undo/redo capability will be
	// associated with a // UNDO/REDO comment
	/**
	 * The data from the last cut or copy. Should move somewhere else, and be
	 * integrated in the normal java copy/paste scheme;
	 */
	// static private List copyBuffer;
	private MDCCaret caret;
	private StringBuffer currentCode;
	private char currentSeparator = ' ';
	private HieroglyphicTextModel hieroglyphicTextModel;
	private List<MDCModelEditionListener> listeners;
	/**
	 * Is it possible to modify this text, or is this simply a view?
	 */
	private boolean readWrite = true;
	/**
	 * Mode is one of 's', 'l', 'i', 'b', 't', '|' for respectively
	 * "hieroglyphs", "latin", "italic", "bold", "transliteration", and
	 * "page/line number".
	 */
	private char mode;
	private PossibilitiesHandler possibilitiesHandler = new PossibilitiesHandler();

	// UNDO/REDO
	public JMDCEditorWorkflow() {
		this(new HieroglyphicTextModel());
	}

	// UNDO/REDO
	public JMDCEditorWorkflow(HieroglyphicTextModel data) {
		listeners = new ArrayList<MDCModelEditionListener>();
		setHieroglyphicTextModel(data);
		currentCode = new StringBuffer("");
		mode = 's';
	}

	// UNDO/REDO
	public final void setHieroglyphicTextModel(
			HieroglyphicTextModel newHieroglyphicTextModel) {

		if (this.hieroglyphicTextModel == newHieroglyphicTextModel) {
			return;
		}

		if (hieroglyphicTextModel != null) {
			hieroglyphicTextModel.deleteObserver(this);
			caret.removeCaretChangeListener(this);
		}
		this.hieroglyphicTextModel = newHieroglyphicTextModel;
		hieroglyphicTextModel.addObserver(this);
		caret = hieroglyphicTextModel.buildCaret();
		setCursor(hieroglyphicTextModel.buildFirstPosition());
		caret.addCaretChangeListener(this);
		possibilitiesHandler.clear();
	}

	/**
	 * Method called when a regular letter is added to the text.
	 *
	 * @param key
	 */
	// UNDO/REDO
	private void addAlphabeticChar(char key) {
		possibilitiesHandler.clear();
		// First, deal with backspace.
		if (key == 8) {
			doBackspace();
		} else {
			boolean addNew = false;
			if (!caret.getInsert().hasPrevious()) {
				addNew = true;
			} else {
				// TODO : PROBLEM HERE. The method getElementBefore should
				// return
				// a copy of the original element. But this might be expensive
				// (when we will use zones() and the like.
				// Hence, the test might be done elsewhere.
				// Or we might have a Readonly variant of getElementBefore
				// (getElementInfoBefore)
				// Which is used to ensure no write operation occur.
				TopItem t = caret.getInsert().getElementBefore();
				if (t instanceof AlphabeticText
						&& ((AlphabeticText) t).getScriptCode() == mode) {
					// THIS WILL CHANGE...
					AlphabeticText txt = (AlphabeticText) t.deepCopy();
					txt.setText(txt.getText() + key);
					hieroglyphicTextModel.replaceElementBefore(
							caret.getInsertPosition(), txt);
				} else if (t instanceof Superscript && mode == '|') {
					Superscript txt = (Superscript) t.deepCopy();
					txt.setText(txt.getText() + key);
					hieroglyphicTextModel.replaceElementBefore(
							caret.getInsertPosition(), txt);
				} else {
					addNew = true;
				}
			}
			if (addNew) {
				if (mode != '|') {
					hieroglyphicTextModel.insertElementAt(caret
							.getInsertPosition(), new AlphabeticText(mode, ""
							+ key).buildTopItem());
				} else {
					hieroglyphicTextModel.insertElementAt(
							caret.getInsertPosition(),
							new Superscript("" + key).buildTopItem());
				}
			}
		}
		clearSeparator();
	}

	/**
	 * Replace a text span with a cartouche.
	 * <p>
	 * The text originally there will move in the cartouche.
	 * <p>
	 * If the text span is empty, we simply create an empty cartouche.
	 *
	 * @param type
	 * @param start
	 * @param end
	 * @return true if success.
	 */
	// UNDO/REDO
	public boolean addCartouche(int type, int start, int end) {
		boolean result;
		List<TopItem> elts = getSelection();
		BasicItemList l = new BasicItemListGrouper().extractBasicItemList(elts);
		if (l != null) {
			Cartouche c = new Cartouche(type, start, end, l);
			MDCPosition insertPos = getCaret().getInsertPosition();
			MDCPosition markPos = insertPos;
			if (getCaret().hasMark()) {
				markPos = getCaret().getMarkPosition();
			}
			clearMark();
			hieroglyphicTextModel.replaceElement(insertPos, markPos,
					c.buildTopItem());
			result = true;
		} else {
			result = false;
		}
		clearSeparator();
		return result;
	}

	/**
	 * @param l
	 */
	// UNDO/REDO
	public void addMDCModelListener(MDCModelEditionListener l) {
		listeners.add(l);
	}

	/**
	 * Adds a philological markup around the selected zone. Respects the current
	 * model's view of philological markup. If markup considered as parenthesis,
	 * it will be add so. Else, two signs will be added.
	 * <p>
	 * Note : to spare ourselves the problem of creating very specific
	 * multi-commands here, we have decided that the undo operation would remove
	 * one element at a time here.
	 *
	 * @param type one of SymbolCodes.ERASEDSIGNS, EDITORADDITION,
	 *             EDITORSUPERFLUOUS, PREVIOUSLYREADABLE or SCRIBEADDITION.
	 * @return true if success, false if failure.
	 */
	// UNDO/REDO
	public boolean addPhilologicalMarkup(int type) {
		boolean result;
		possibilitiesHandler.clear();
		if (hieroglyphicTextModel.isPhilologyIsSign()) {
			int maxIndex = caret.getMax();
			MDCPosition max = caret.getMaxPosition();
			MDCPosition min = caret.getMinPosition();
			// See the SymbolCodes for the explanation of the magic *2 and *2+1
			// below.
			hieroglyphicTextModel.insertElementAt(max, new Hieroglyph(
					type * 2 + 1).buildTopItem());

			hieroglyphicTextModel.insertElementAt(min,
					new Hieroglyph(type * 2).buildTopItem());
			caret.moveInsertTo(maxIndex + 1);
			result = true;
		} else {
			BasicItemListGrouper grouper = new BasicItemListGrouper();
			List<TopItem> elts = getSelection();
			BasicItemList l = grouper.extractBasicItemList(elts);
			if (l != null) {
				Philology c = new Philology(type, l);
				hieroglyphicTextModel.replaceElement(caret.getInsertPosition(),
						caret.getMarkPosition(), c.buildTopItem());
				clearMark();
				result = true;
			} else {
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
	// UNDO/REDO
	public void addSameLevel() {
		ModelElement elt = caret.getInsertPosition().getElementBefore();
		if (elt instanceof Cadrat) {
			Cadrat c = (Cadrat) elt.deepCopy(); // Won't be needed in the future
			// if getElementBefore sends
			// copies too.
			// get last hbox.
			HBox hbox = c.getHBox(c.getNumberOfChildren() - 1);
			// add sign :
			Hieroglyph hiero = buildHieroglyphFromCode(currentCode.toString());
			hbox.addHorizontalListElement(hiero);
			hieroglyphicTextModel.replaceElementBefore(
					caret.getInsertPosition(), c);
			clearCode();
			clearSeparator();
		}
	}

	/**
	 * Accept a separator, and possibly insert the current code in the text.
	 * <p>
	 * The separator is used to groups new signs ; it can be ' ', '*', ':' or
	 * '&'. The exact effect depends on currentCode and on the presence of a
	 * previous separator.
	 * <p>
	 * if currentCode is empty and there is no current separator, the separator
	 * becomes the current separator. if there is a current separator, the
	 * current separator is used to group the last two signs before the
	 * separator, and the new separator becomes the current separator.
	 *
	 * <p>
	 * If currentCode is not empty, add the corresponding glyph to the model,
	 * according to the current separator, and then make sep the current
	 * separator.
	 *
	 * @param sep the next separator.
	 */
	// UNDO/REDO
	// New implementation in two steps
	// Step a) build the new text fragment
	// Step b) replace the old text fragment (possibly empty) with the new one.
	public void acceptSeparator(char sep) {
		if (currentCode.length() != 0) {
			possibilitiesHandler.init(getCurrentCode().toString(), currentSeparator);
			addCodeAndThenGroup();
		} else {
			groupTwoPreviousBySeparator();
		}
		clearMark();
		currentSeparator = sep;
		notifyCodeChangeListeners();
		notifySeparatorChangeListeners();
	}

	/**
	 * Add the element(s) corresponding to the current code, and group them
	 * using the current separator.
	 */
	private void addCodeAndThenGroup() {
		// Build a list of items to add
		List<TopItem> items;
		if (possibilitiesHandler.hasPossibilities()) {
			Possibility s = possibilitiesHandler.getPossibility();
			if (!s.isSingleSign()) {
				items = s.getTopItemList().asList();
			} else {
				items = Collections.singletonList(buildItemForSignCode(s
						.getCode()));
			}
		} else {
			items = Collections.singletonList(buildItemForSignCode(currentCode
					.toString()));
		}
		// Now, add the item.
		MDCPosition pos1, pos2;
		pos1 = caret.getInsertPosition();
		pos2 = pos1.getPreviousPosition(1);
		TopItem head = hieroglyphicTextModel.getItemBefore(pos1);

		List<TopItem> newElements = groupBy(head, items, possibilitiesHandler.getSeparator());
		hieroglyphicTextModel.replaceElement(pos1, pos2, newElements);
		clearCode();
	}

	/**
	 * Group the two elements in front of the cursor according to the
	 */
	private void groupTwoPreviousBySeparator() {
		// group only.
		MDCPosition pos1 = caret.getInsertPosition().getPreviousPosition(2);
		MDCPosition pos2 = caret.getInsertPosition();
		List<TopItem> items = hieroglyphicTextModel.getTopItemsBetween(pos1,
				pos2);
		if (items.size() == 2) {
			// Only grouping is done.
			List<TopItem> newElements = groupBy(items.get(0),
					items.subList(1, items.size()), currentSeparator);
			hieroglyphicTextModel.replaceElement(pos1, pos2, newElements);
		}
	}

	// UNDO/REDO

	/**
	 * Use next possible choice for the previously input code.
	 */
	public void nextPossibility() {
		if (possibilitiesHandler.hasPossibilities()) {
			hieroglyphicTextModel.undo();
			possibilitiesHandler.next();
			addCodeAndThenGroup();
		}
	}

	/**
	 * Adds a sign whose code is "code" to the text.
	 *
	 * @param code the sign code (which has the form of an identifier).
	 */
	// UNDO/REDO
	public void addSign(String code) {
		hieroglyphicTextModel.insertElementAt(caret.getInsertPosition(),
				buildItemForSignCode(code));
		clearCode();
	}

	private TopItem buildItemForSignCode(String code) {
		// Avoid codes which would create unreadable files.
		if (!code.matches("[@0-9a-zA-Z]+")) {
			code = "illegalCode";
		}

		Cadrat cadrat = new Cadrat();
		HBox hbox = new HBox();
		cadrat.addHBox(hbox);
		hbox.addHorizontalListElement(buildHieroglyphFromCode(code));
		return cadrat.buildTopItem();
	}

	/**
	 * @param c
	 */
	public void addToCode(char c) {
		possibilitiesHandler.clear();
		currentCode.append(c);
		notifyCodeChangeListeners();
	}

	/**
	 * Build a "plain" absolute group with the selected text, or return an
	 * absolute group from last cadrat. Returns a <em>copy</em> of this group.
	 *
	 * <p>
	 * Side effect: if there is no selection, select the previous cadrat.
	 *
	 * <p>
	 * Note that this method doesn't change the text itself.
	 *
	 * @return Returns a <em>working copy</em> of the created group, or null if
	 * nothing was created.
	 */
	// UNDO/REDO
	public AbsoluteGroup buildAbsoluteGroup() {
		AbsoluteGroup result = null;
		// ensure there is a selection if possible.
		if (!caret.hasSelection() && getInsertPosition() >= 1) {
			caret.setMarkAt(getInsertPosition() - 1);
		}
		if (caret.hasSelection()) {
			List<TopItem> elts = getSelection();
			// FIXME : drawing specifications alert !!!
			AbsoluteGroup g = AbsoluteGroupBuilder.createAbsoluteGroupFrom(
					elts, MDCEditorKit.getBasicMDCEditorKit()
							.getDrawingSpecifications());
			result = g;
		}
		return result;
	}

	/**
	 * Replaces the selection with g.
	 *
	 * @param g
	 */
	// UNDO/REDO
	public void replaceSelectionByAbsoluteGroup(AbsoluteGroup g) {
		clearSeparator();
		possibilitiesHandler.clear();
		if (caret.hasSelection() && !g.containsOnlyOneSign()) {
			g.compact();
			hieroglyphicTextModel.replaceElement(caret.getInsertPosition(),
					caret.getMarkPosition(), g.buildTopItem());
		}
	}

	// UNDO/REDO
	private Hieroglyph buildHieroglyphFromCode(String code) {
		Hieroglyph hiero;
		if ("o".equals(code)) {
			hiero = new Hieroglyph(SymbolCodes.REDPOINT);
		} else {
			hiero = new Hieroglyph(code);
		}
		return hiero;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seejsesh.mdcDisplayer.draw.MDCCaretChangeListener#caretChanged(jsesh.
	 * mdcDisplayer.draw.MDCCaret)
	 */
	// UNDO/REDO
	public void caretChanged(MDCCaret caret) {
		for (Iterator<MDCModelEditionListener> i = listeners.iterator(); i
				.hasNext(); ) {
			MDCModelEditionListener l = i.next();
			l.caretChanged(caret);
		}
	}


	/**
	 * Replaces the last sign (and the last sign only).
	 *
	 * @param hieroglyphConsumer
	 */

	// UNDO/REDO
	private void modifyLastSign(Consumer<Hieroglyph> hieroglyphConsumer) {
		if (getCurrentItem() != null) {
			TopItem item = (TopItem) getCurrentItem().deepCopy();
			if (item != null) {
				Hieroglyph h = getLastHieroglyph(item);
				if (h != null) {
					hieroglyphConsumer.accept(h);
				}
				hieroglyphicTextModel.replaceElementBefore(
						caret.getInsertPosition(), item);
			}
		}
	}

	/**
	 * Perform a modification either on the last sign, or on a selection.
	 * <p>Handles the UNDO/REDO framework.</p>
	 *
	 * @param hieroglyphConsumer
	 */
	private void processLastSignOrSelection(Consumer<Hieroglyph> hieroglyphConsumer) {
		possibilitiesHandler.clear();
		if (!caret.hasSelection()) {
			modifyLastSign(hieroglyphConsumer);
		} else {
			clearSeparator();
			List<TopItem> newItems = hieroglyphicTextModel.getTopItemsBetween(
					caret.getInsertPosition(), caret.getMarkPosition());
			DeepHieroglyphsProcessor processor = new DeepHieroglyphsProcessor();
			processor.processItems(newItems, hieroglyphConsumer);
			hieroglyphicTextModel.replaceElement(
					caret.getInsertPosition(),
					caret.getMarkPosition(), newItems);
		}
	}

	/**
	 * Set the angle for either the last hieroglyph or all glyphs in selection, if there is a selection.
	 *
	 * @param angle
	 */
	// UNDO/REDO
	public void setAngle(final int angle) {
		processLastSignOrSelection(h -> h.setAngle(angle));
	}


	/**
	 * Clear the text in the current model.
	 */
	// UNDO/REDO
	public void clear() {
		try {
			possibilitiesHandler.clear();
			setMDCCode("");
			clearSeparator();
		} catch (MDCSyntaxError e) {
			// Should not happen
			throw new RuntimeException(e);
		}
		currentCode.setLength(0);
		clearSeparator();
	}

	// UNDO/REDO
	public void clearCode() {
		currentCode.setLength(0);
		notifyCodeChangeListeners();
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void clearMark() {
		// possibilitiesHandler.clear();
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * Resets the current separator so that next sign appears on a cadrat of his
	 * own.
	 */
	// UNDO/REDO
	public void clearSeparator() {
		currentSeparator = ' ';
		notifySeparatorChangeListeners();
	}

	/**
	 * Moves the cursor down one line.
	 */
	// UNDO/REDO
	public void cursorDown() {
		caret.setInsertPosition(caret.getInsertPosition().getDownPosition());
		possibilitiesHandler.clear();
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * Moves the cursor to the next cadrat.
	 */
	// UNDO/REDO
	public void cursorNext() {
		possibilitiesHandler.clear();
		caret.moveInsertBy(1);
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * Moves the cursor to the previous cadrat.
	 */
	// UNDO/REDO
	public void cursorPrevious() {
		possibilitiesHandler.clear();
		caret.moveInsertBy(-1);
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * move to cursor to the beginning of the current line.
	 */
	// UNDO/REDO
	public void cursorToBeginningOfLine() {
		possibilitiesHandler.clear();
		MDCPosition p = getLineFirstPosition();
		caret.setInsertPosition(p);
		clearSeparator();
	}

	/**
	 * move to the end of the current line.
	 */
	// UNDO/REDO
	public void cursorToEndOfLine() {
		possibilitiesHandler.clear();
		caret.setInsertPosition(getLineLastPosition());
	}

	/**
	 * Moves the cursor up one line.
	 * <p>
	 * Currently, we move to the beginning of the line. In the future, it might
	 * be interesting to retain the horizontal position and to try to keep it.
	 */
	// UNDO/REDO
	public void cursorUp() {
		caret.setInsertPosition(caret.getInsertPosition().getUpPosition());
		possibilitiesHandler.clear();
		clearSeparator();
		caret.unsetMark();
	}

	/**
	 * Deletes the current selection.
	 */
	// UNDO/REDO
	public void removeSelectedText() {
		possibilitiesHandler.clear();
		hieroglyphicTextModel.removeElements(caret.getMinPosition(),
				caret.getMaxPosition());
		clearSeparator();
	}

	// UNDO/REDO
	public void deleteCodeChangeListener(MDCModelEditionListener l) {
		listeners.remove(l);
	}

	// UNDO/REDO
	public void deleteCursorChangeListener(MDCCaretChangeListener l) {
		caret.removeCaretChangeListener(l);
	}

	/**
	 * Erase text according to the context.
	 * <p>
	 * The erased text might be : an incomplete code (if any), a separator (if
	 * any) a whole selection (if any) or the item just in front of the cursor
	 * (still if any).
	 */
	// UNDO/REDO
	public void doBackspace() {

		possibilitiesHandler.clear();

		if (currentCode.length() > 0) {
			currentCode.replace(currentCode.length() - 1, currentCode.length(),
					"");
			notifyCodeChangeListeners();
		} else if (currentSeparator != ' ') {
			clearSeparator();
		} else {
			// Code to deal with text elements. may disappear one day if we
			// decide that letters are first-class citizens.
			if (caret.hasSelection()) {
				removeSelectedText();
			} else if (caret.getInsert().hasPrevious()
					&& caret.getInsert().getElementBefore() instanceof TextContainer) {
				removeSingleLetter();
			} else {
				removeTopItem();
			}
		}
		clearSeparator();
	}

	/**
	 * Ligature one or two group(s) and a hieroglyph.
	 * <p>
	 * The group before signPost is inserted in the first ligature position, the
	 * group after signPos in the second.
	 *
	 * @param signPos position of the hieroglyph. -1 stands for "last position".
	 */
	// UNDO/REDO
	public void doComplexLigature(int signPos) {
		boolean success = false;
		List<TopItem> elts = getSelection();
		MDCPosition minPos = caret.getMinPosition();
		MDCPosition maxPos = caret.getMaxPosition();
		if (elts == null) {
			return;
		}
		if (elts.size() > 1 && signPos < elts.size()) {
			InnerGroup group1 = null;
			InnerGroup group2 = null;
			Hieroglyph h = null;

			if (signPos == -1) {
				signPos = elts.size() - 1;
			}

			// See if the main element is already a ligature,
			// in which case we will ligature the rest in either positions
			// group1 or group2, one of which is supposed to be free.
			ComplexLigatureExtractor ligatureExtractor = new ComplexLigatureExtractor();
			ligatureExtractor.extract(elts.get(signPos));
			if (!ligatureExtractor.foundOtherElements()
					&& ligatureExtractor.getComplexLigature() != null) {
				ComplexLigature c1 = ligatureExtractor.getComplexLigature();
				if ((InnerGroup) c1.getBeforeGroup() != null) {
					group1 = (InnerGroup) c1.getBeforeGroup().deepCopy();
				}
				if ((InnerGroup) c1.getAfterGroup() != null) {
					group2 = (InnerGroup) c1.getAfterGroup().deepCopy();
				}
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
					// The awful cast below would disapear completely in java
					// 1.5
					h = (Hieroglyph) ((Hieroglyph) hieros.get(0)).deepCopy();
					;
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
					hieroglyphicTextModel.replaceElement(minPos, maxPos,
							ligature.buildTopItem());
					clearMark();
					success = true;
				}

			}
		}
		clearSeparator();
		possibilitiesHandler.clear();
	}

	/**
	 * Shade the selection, or the cadrat in front of the cursor.
	 * <p>
	 * the integer constant is the sum of the shading codes for the selected
	 * areas.
	 * <p>
	 * If there is a selection, all groups in the selection are shaded. If there
	 * is none, the shading is applied to the group in front of the cursor. If
	 * there is nothing to shade, nothing will be shaded.
	 *
	 * @param shade shading specifications (see {@link ShadingCode}
	 */
	// UNDO/REDO
	public void doShade(final int shade) {
		possibilitiesHandler.clear();
		applyChangeToSelection((TopItem t) -> {
			if (t instanceof Cadrat) {
				Cadrat c = (Cadrat) t;
				c.setShading(shade);
			}
		});
	}

	/**
	 * Apply a modification to the whole selection, or to the previous item if
	 * there is no previous selection.
	 *
	 * @param topItemModifier
	 */
	// UNDO/REDO
	private void applyChangeToSelection(TopItemModifier topItemModifier) {
		List<TopItem> modified = new ArrayList<TopItem>();
		if (caret.hasSelection()) {
			List<TopItem> sel = getSelection();
			for (int i = 0; i < sel.size(); i++) {
				TopItem t = (TopItem) ((TopItem) sel.get(i)).deepCopy();
				topItemModifier.modifyTopItem(t);
				modified.add(t);
			}
			if (modified.size() > 0) {
				hieroglyphicTextModel.replaceElement(caret.getMinPosition(),
						caret.getMaxPosition(), modified);
			}
		} else if (caret.getInsert().hasPrevious()) {
			TopItem t = (TopItem) caret.getInsert().getElementBefore()
					.deepCopy();
			topItemModifier.modifyTopItem(t);
			modified.add(t);
			if (modified.size() > 0) {
				hieroglyphicTextModel.replaceElementBefore(
						caret.getInsertPosition(), modified);
			}
		}

	}

	/**
	 * Expand/restrict selection. dir controls the way expansion goes :
	 * <ul>
	 * <li>backward (dir= -1) or forward (dir=1).
	 * <li>one "line" backward (-2) or forward (2)
	 * </ul>
	 *
	 * @param dir
	 */
	// UNDO/REDO
	public void expandSelection(int dir) {
		if (!caret.hasMark()) {
			caret.setMark(new MDCMark(caret.getInsertPosition()));
		}
		switch (dir) {
			case 1:
			case -1:
				caret.advanceInsertBy(dir);
				break;
			case 2:
				caret.setInsertPosition(caret.getInsertPosition().getDownPosition());
				break;
			case -2:
				caret.setInsertPosition(caret.getInsertPosition().getUpPosition());
				break;
			default:
				throw new RuntimeException("Unexpected dir " + dir);
		}
	}

	/**
	 * separate the current group into smaller ones.
	 */
	// UNDO/REDO
	public void explodeGroup() {
		if (caret.getInsert().getIndex() < 1) {
			return;
		}
		GroupExploder f = new GroupExploder();
		TopItem topItem = getCurrentItem();
		if (topItem != null) {
			List<TopItem> result = f.explode((TopItem) topItem.deepCopy());
			hieroglyphicTextModel.replaceElementBefore(
					caret.getInsertPosition(), result);
		}
		clearSeparator();
		possibilitiesHandler.clear();
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void focusGained() {
		for (int i = 0; i < listeners.size(); i++) {
			MDCModelEditionListener listener = listeners.get(i);
			listener.focusGained(currentCode);
		}

	}

	/**
	 *
	 */
	// UNDO/REDO
	public void focusLost() {
		for (int i = 0; i < listeners.size(); i++) {
			MDCModelEditionListener listener = listeners.get(i);
			listener.focusLost();
		}

	}

	// UNDO/REDO
	public MDCCaret getCaret() {
		return caret;
	}

	/**
	 * @return the current glyph code being entered.
	 */
	// UNDO/REDO
	public StringBuffer getCurrentCode() {
		return currentCode;
	}

	/**
	 * Returns the topitem at index position, or null if none.
	 *
	 * @return Returns the topitem at index position, or null if none.
	 */
	// UNDO/REDO
	private TopItem getCurrentItem() {
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
	// UNDO/REDO
	public String getCurrentLineAsString() {
		int[] limits = getLineLimits();
		StringWriter sw = new StringWriter();
		hieroglyphicTextModel.writeAsMDC(sw, limits[0], limits[1]);
		return sw.toString();
	}

	/**
	 * @return Returns the currentSeparator.
	 */
	// UNDO/REDO
	public char getCurrentSeparator() {
		return currentSeparator;
	}

	// UNDO/REDO
	private int getInsertPosition() {
		return caret.getInsertPosition().getIndex();
	}

	/**
	 * Returns the last hieroglyph element in the topitem before index mark, or
	 * null.
	 * <p>
	 * This modifies the element.
	 *
	 * @param t the topitem from which the hieroglyph is taken.
	 * @return the last hieroglyph element in the topitem before index mark, or
	 * null.
	 */
	// UNDO/REDO
	private Hieroglyph getLastHieroglyph(TopItem t) {
		Hieroglyph result = null;
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
	// UNDO/REDO
	private MDCPosition getLineFirstPosition() {
		return caret.getInsertPosition().getLineFirstPosition();
	}

	/**
	 * Returns the last position in the current line.
	 *
	 * @return the last position in the current line.
	 */
	// UNDO/REDO
	private MDCPosition getLineLastPosition() {
		return caret.getInsertPosition().getLineLastPosition();
	}

	/**
	 * Returns the limits of the current line, including the final -! or -!! if
	 * any.
	 *
	 * @return the limits of the current line.
	 */
	// UNDO/REDO
	private int[] getLineLimits() {
		int[] result = new int[2];
		MDCPosition first = getLineFirstPosition();

		MDCPosition second = getLineLastPosition();

		if (second.hasNext()) {
			second = second.getNextPosition(1);
		}

		result[0] = first.getIndex();
		result[1] = second.getIndex();
		return result;
	}

	/**
	 * Return the manuel de codage text for the current model.
	 *
	 * @return the manuel de codage text for the current model.
	 */
	// UNDO/REDO
	public String getMDCCode() {
		StringWriter sw = new StringWriter();
		hieroglyphicTextModel.writeAsMDC(sw, 0, hieroglyphicTextModel
				.getLastPosition().getIndex());
		return sw.toString();
	}

	/**
	 * @return Returns the mode.
	 */
	// UNDO/REDO
	public char getMode() {
		return mode;
	}

	/**
	 * Returns a <em>copy</em> of the selection, as a new TopItemList.
	 *
	 * @return
	 */
	// UNDO/REDO
	public TopItemList getSelectionAsTopItemList() {
		TopItemList topItemList = new TopItemList();
		if (caret.hasMark()) {
			int a = caret.getMin();
			int b = caret.getMax();
			List<TopItem> l = hieroglyphicTextModel.getTopItemsBetween(a, b);
			topItemList.addAll(l);
		}
		return topItemList;
	}

	/**
	 * Group the two groups before the insert mark according to this separator
	 * All parameters should be copies of the original elements.
	 *
	 * @param head the item just before the group which should be inserted (may
	 *             be null)
	 * @param tail the 'new' items (usually, one, but glossary entry might be
	 *             whole words).
	 * @param key  one of ' ' ; '*' , ':' or '&'.
	 * @return the list of items which should replace head (or head and tail, it
	 * depends).
	 */
	// UNDO/REDO
	private List<TopItem> groupBy(TopItem head, List<TopItem> tail, char sep) {
		// No need for grouping when separator is '-'
		if (sep == ' ') {
			ArrayList<TopItem> result = new ArrayList<TopItem>();
			if (head != null) {
				result.add(head);
			}
			result.addAll(tail);
			return result;
		} else {
			// A) try to group tail as one quadrant.
			//
			if (head == null) {
				return tail;
			}
			if (tail.isEmpty()) {
				return Collections.singletonList(head);
			}
			TopItem secondElement;
			if (tail.size() == 1) {
				secondElement = tail.get(0);
			} else {
				HorizontalGrouper grouper = new HorizontalGrouper();
				ArrayList<ModelElement> tmpList = new ArrayList<ModelElement>();
				//tmpList.add(head);
				tmpList.addAll(tail);
				secondElement = grouper.buildCadrat(tmpList);
			}
			// we now have two elements.
			switch (sep) {
				case '*':
					return buildCombinedQuadrant(head, secondElement);
				case ':':
					return buildVerticalStack(head, secondElement);
				case '&': {
					ArrayList<TopItem> list = new ArrayList<TopItem>();
					list.add(head);
					list.addAll(tail);
					return buildLigature(list);
				}
				default: // Should not happen.
					throw new RuntimeException("bad separator " + sep);
			}
		}
	}

	/**
	 * Group the elements in the selection in an hbox (and then in a cadrat).
	 * Can only work if it's possible to fetch innergroups from these elements.
	 *
	 * @return true if success.
	 */
	// UNDO/REDO
	public boolean groupHorizontally() {
		boolean result;
		HorizontalGrouper f = new HorizontalGrouper();
		List<TopItem> elts = getSelection();

		Cadrat c = f.buildCadrat(elts);
		if (c != null) {
			hieroglyphicTextModel.replaceElement(caret.getMinPosition(),
					caret.getMaxPosition(), c);
			result = true;
		} else {
			result = false;
		}
		clearSeparator();
		clearMark();
		return result;
	}

	/**
	 * stack the last two cadrats.
	 */
	// UNDO/REDO
	private List<TopItem> buildVerticalStack(TopItem head, TopItem secondElement) {
		ArrayList<TopItem> result = new ArrayList<TopItem>();
		VerticalGrouper f = new VerticalGrouper();
		List<TopItem> elts = new ArrayList<TopItem>();
		elts.add(head);
		elts.add(secondElement);
		if (elts.size() == 2) {
			Cadrat c = f.buildCadrat(elts);
			if (c != null) {
				result.add(c);
			}
		}
		if (result.isEmpty()) {
			result.add(head);
			result.add(secondElement);
		}
		return result;
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
	// UNDO/REDO
	public boolean groupVertically() {
		boolean result;
		VerticalGrouper v = new VerticalGrouper();
		List<TopItem> elts = getSelection();
		if (elts.isEmpty()) {
			return false;
		}
		Cadrat c = v.buildCadrat(elts);
		if (c != null) {
			hieroglyphicTextModel.replaceElement(caret.getMinPosition(),
					caret.getMaxPosition(), c);
			clearMark();
			result = true;
		} else {
			result = false;
		}
		clearSeparator();
		return result;
	}

	/**
	 * Insert a list of TopItems in the text.
	 *
	 * @param elements a
	 */
	// UNDO/REDO
	public void insertElements(List elements) {
		possibilitiesHandler.clear();
		hieroglyphicTextModel.insertElementsAt(caret.getInsertPosition(),
				elements);
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void insertHalfSpace() {
		possibilitiesHandler.clear();
		Hieroglyph h = new Hieroglyph(SymbolCodes.HALFSPACE);
		hieroglyphicTextModel.insertElementAt(caret.getInsertPosition(),
				h.buildTopItem());
	}

	/**
	 * group two quadrants, inserting the second in the <em>lower</em> row of
	 * the first.
	 */
	private List<TopItem> buildCombinedQuadrant(TopItem head,
												TopItem secondElement) {
		ArrayList<TopItem> result = new ArrayList<TopItem>();
		CadratStarInserter f = new CadratStarInserter();
		Cadrat c = f.buildCadrat(head, secondElement);
		if (c != null) {
			result.add(c);
		} else {
			result.add(head);
			result.add(secondElement);
		}
		return result;
	}

	/**
	 * @param mdcText
	 */
	// UNDO/REDO
	public void insertMDC(String mdcText) {
		try {
			possibilitiesHandler.clear();
			hieroglyphicTextModel.insertMDCText(getInsertPosition(), mdcText);
		} catch (MDCSyntaxError e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void insertNewLine() {
		possibilitiesHandler.clear();
		acceptSeparator(' ');
		hieroglyphicTextModel.insertElementAt(caret.getInsertPosition(),
				new LineBreak().buildTopItem());
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void insertPageBreak() {
		possibilitiesHandler.clear();
		acceptSeparator(' ');
		hieroglyphicTextModel.insertElementAt(caret.getInsertPosition(),
				new PageBreak().buildTopItem());
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void insertSpace() {
		possibilitiesHandler.clear();
		Hieroglyph h = new Hieroglyph(SymbolCodes.FULLSPACE);
		hieroglyphicTextModel.insertElementAt(caret.getInsertPosition(),
				h.buildTopItem());
	}

	/**
	 * @param key
	 * @return true if the typed key correspond to a possible code character..
	 */
	// UNDO/REDO
	public boolean isCodeChar(char key) {
		return (Character.isLetterOrDigit(key) || key == '\'' || key == '-');
	}

	/**
	 * Is it possible to modify this text, or is this simply a view?
	 *
	 * @return the readWrite
	 */
	// UNDO/REDO
	public boolean isReadWrite() {
		return readWrite;
	}

	/**
	 * @param key
	 * @return true if key corresponds to a MDC separator.
	 */
	// UNDO/REDO
	public boolean isSeparatorChar(char key) {
		return (key == ' ') || (key == ':') || (key == '*') || (key == '&');
	}

	/**
	 * Send a char to the current editing process. The char will be processed
	 * according to the current state of the workflow.
	 *
	 * @param key
	 */
	// UNDO/REDO
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
				acceptSeparator(key);
			} else if (isCodeChar(key)) {
				addToCode(key);
			}
		} else {
			addAlphabeticChar(key);
		}
	}

	/**
	 * Ligature a hieroglyph and a following group.
	 */
	// UNDO/REDO
	public void ligatureHieroglyphWithGroup() {
		doComplexLigature(0);
	}

	/**
	 * Insert a group in a following hieroglyph.
	 */
	// UNDO/REDO
	public void ligatureGroupWithHieroglyph() {
		doComplexLigature(-1);

	}

	/**
	 * Ligature all the elements in the selection.
	 *
	 * @return true if success
	 */
	// UNDO/REDO
	public boolean ligatureElements() {
		List<TopItem> elts = getSelection();
		if (getSelection().isEmpty()) {
			return false;
		}
		MDCPosition minPos = caret.getMinPosition();
		MDCPosition maxPos = caret.getMaxPosition();
		List<TopItem> toInsert = buildLigature(elts);
		hieroglyphicTextModel.replaceElement(minPos, maxPos, toInsert);
		clearMark();
		clearSeparator();
		return true;
	}

	/**
	 * Build a ligature from a number of elements. The parameters are not copied
	 * here, so you should be sure they are already copies, or not already parts
	 * of an existing construct.
	 *
	 * @param elts the elements, whose glyphs will be used IMPORTANT : improve
	 *             this. The ligature should take place with the last element of the
	 *             previous cadrat.
	 */
	private List<TopItem> buildLigature(List<? extends ModelElement> elts) {
		ArrayList<TopItem> result = new ArrayList<TopItem>();
		// Copy of the corresponding code from ligatureElements.
		// We will change ligatureElements so that it uses this method
		HieroglyphExtractor extractor = new HieroglyphExtractor();
		List<Hieroglyph> hieros = extractor.extractHieroglyphs(elts);
		if (hieros != null && hieros.size() > 1) {
			Ligature lig = new Ligature();
			for (Hieroglyph h : hieros) {
				lig.addHieroglyph(h.deepCopy());
			}
			result.add(lig.buildTopItem());
		} else {
			for (ModelElement e : elts) {
				result.add(e.buildTopItem());
			}
		}
		return result;
	}

	// UNDO/REDO
	private void notifyCodeChangeListeners() {
		for (int i = 0; i < listeners.size(); i++) {
			MDCModelEditionListener listener = listeners.get(i);
			listener.codeChanged(currentCode);
		}

	}

	// UNDO/REDO
	private void notifySeparatorChangeListeners() {
		for (int i = 0; i < listeners.size(); i++) {
			MDCModelEditionListener listener = listeners.get(i);
			listener.separatorChanged();
		}
	}

	// UNDO/REDO
	public void redo() {
		hieroglyphicTextModel.redo();
	}

	/**
	 * Paint a zone in red if b is true, black otherwise. Will become private in
	 * favour of paintZoneInRed() and paintZoneInBlack ?
	 *
	 * @param b
	 */
	// UNDO/REDO
	public void redZone(final boolean b) {
		possibilitiesHandler.clear();
		applyChangeToSelection((TopItem t) -> {
			if (t instanceof Cadrat) {
				Cadrat c = (Cadrat) t;
				c.setRed(b);

			}
		});
		clearSeparator();
	}

	/**
	 * Colour the selection in red.
	 */
	public void paintZoneInRed() {
		redZone(true);
	}

	/**
	 * Colour the selection in black.
	 */
	public void paintZoneInBlack() {
		redZone(false);
	}

	/**
	 * Remove a single letter from the text element in front of the cursor.
	 * Erase the element if it becomes empty. Things would be waayyyy simpler if
	 * we had "one letter = one element".
	 */
	// UNDO/REDO
	private void removeSingleLetter() {
		if (caret.getInsert().hasPrevious()) {
			TopItem t = caret.getInsert().getElementBefore();
			// TODO : FIND A BETTER OO ORGANIZATION.
			if (t instanceof TextContainer) {
				TextContainer txt = (TextContainer) t;
				// If t is or would be empty, suppress t
				if (txt.getText().length() <= 1) {
					removeTopItem();
				} else {
					TextContainer newText = (TextContainer) txt.deepCopy();
					// Suppress only one char.
					newText.setText(txt.getText().substring(0,
							txt.getText().length() - 1));
					// Replace the old text with the new one.
					// NOW, THIS IS A UGLY CAST.
					// Normally, we would need some kind of
					// "TextContainer + TopItem" class.
					hieroglyphicTextModel.replaceElementBefore(
							caret.getInsertPosition(), (TopItem) newText);
				}
			}
		}
	}

	/**
	 * Remove the element in front of the cursor.
	 */
	// UNDO/REDO
	public void removeTopItem() {
		possibilitiesHandler.clear();
		hieroglyphicTextModel.removeElements(caret.getInsertPosition()
				.getPreviousPosition(1), caret.getInsertPosition());
		clearSeparator();
	}

	/**
	 * Changes the size of the current sign
	 *
	 * @param size
	 */
	// UNDO/REDO
	public void resizeSign(final int size) {
		processLastSignOrSelection(
				hieroglyph -> hieroglyph.setRelativeSize(size));
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void reverseSign() {
		processLastSignOrSelection(
				hieroglyph ->
						hieroglyph.setReversed(!hieroglyph.isReversed()));
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void selectAll() {
		possibilitiesHandler.clear();
		caret.moveInsertTo(0);
		caret.setMarkAt(hieroglyphicTextModel.getLastPosition().getIndex());
		clearSeparator();
	}

	// UNDO/REDO ?
	public void selectCurrentLine() {
		possibilitiesHandler.clear();
		int pos = getInsertPosition();
		List<MDCPosition> limits = hieroglyphicTextModel.getLineLimitsAround(caret.getInsertPosition());
		caret.setInsertPosition(limits.get(0));
		caret.setMarkAt(limits.get(1).getIndex());
	}

	public void selectCurrentPage() {
		possibilitiesHandler.clear();
		int pos = getInsertPosition();
		List<MDCPosition> limits = hieroglyphicTextModel.getPageLimitsAround(caret.getInsertPosition());
		caret.setInsertPosition(limits.get(0));
		caret.setMarkAt(limits.get(1).getIndex());
	}

	public void clearSelection() {
		possibilitiesHandler.clear();
		caret.unsetMark();
		clearSeparator();
	}

	/**
	 * Sets the content of the current line from a manuel de codage encoding.
	 *
	 * @param text
	 * @return true if success.
	 */
	// UNDO/REDO
	public boolean setCurrentLineTo(String text) {
		possibilitiesHandler.clear();
		boolean success = true;
		try {
			int limits[] = getLineLimits();
			hieroglyphicTextModel
					.replaceWithMDCText(limits[0], limits[1], text);
			caret.setInsertPosition(hieroglyphicTextModel
					.buildPosition(limits[0]));
			clearSeparator();
		} catch (MDCSyntaxError e) {
			success = false;
		}
		return success;
	}

	/**
	 * @param position
	 */
	// UNDO/REDO
	public void setCursor(MDCPosition position) {
		if (position != null) {
			possibilitiesHandler.clear();
			caret.setInsert(new MDCMark(position));
			clearSeparator();
		}
	}

	/**
	 * Set mark at position.
	 *
	 * @param position
	 */
	// UNDO/REDO
	public void setMark(MDCPosition position) {
		if (position != null) {
			possibilitiesHandler.clear();
			caret.setMark(new MDCMark(position));
			clearSeparator();
		}
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void setMarkToCursor() {
		possibilitiesHandler.clear();
		caret.setMarkAt(caret.getInsert().getIndex());
		clearSeparator();
	}

	// Set the data of the current model to String
	// UNDO/REDO
	public void setMDCCode(String txt) throws MDCSyntaxError {
		possibilitiesHandler.clear();
		hieroglyphicTextModel.setMDCCode(txt);
		clearSeparator();
	}

	/**
	 * Sets the writing mode : hieroglyphs, latin, etc.
	 *
	 * @param mode one of 's', 'l', 'i', 'b', 't', '|' for respectively
	 *             "hieroglyphs", "latin", "italic", "bold", "transliteration", "line
	 *             number".
	 */
	// UNDO/REDO
	public void setMode(char mode) {
		possibilitiesHandler.clear();
		this.mode = mode;
		clearSeparator();
	}

	/**
	 * Choose between readonly and readwrite object.
	 *
	 * @param readWrite the readWrite to set
	 */
	// UNDO/REDO
	public void setReadWrite(boolean readWrite) {
		this.readWrite = readWrite;
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void setSignIsAtSentenceEnd() {
		processLastSignOrSelection(hieroglyph ->
				hieroglyph.setEndingCode(WordEndingCode.SENTENCE_END));
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void setSignIsAtWordEnd() {
		processLastSignOrSelection(hieroglyph ->
				hieroglyph.setEndingCode(WordEndingCode.WORD_END)
		);
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void setSignIsInsideWord() {
		processLastSignOrSelection(hieroglyph ->
				hieroglyph.setEndingCode(WordEndingCode.NONE)
			);
	}

	/**
	 * shade or unshade selected zone, depending the value of
	 * <code>shade</code> . This method will be made private in favour of
	 * {@link #shadeZone()} and {@link #unshadeZone()}.
	 *
	 * @param shade
	 */
	// UNDO/REDO
	public void shadeZone(final boolean shade) {
		clearSeparator();
		possibilitiesHandler.clear();
		applyChangeToSelection((TopItem t) -> {
			t.setShaded(shade);
		});
	}

	/**
	 * Shade selected zone.
	 */
	public void shadeZone() {
		shadeZone(true);
	}

	/**
	 * Remove shading for selected zone.
	 */
	public void unshadeZone() {
		shadeZone(false);
	}

	/**
	 * Returns a <em>copy</em> of the list of selected elements, or null if none
	 * is selected.
	 *
	 * @return a copy of the list of TopItems, empty if there is no selection.
	 */
	// UNDO/REDO
	private List<TopItem> getSelection() {
		// possibilitiesHandler.clear(); WHY WAS THAT ??
		List<TopItem> result = null;
		if (caret.hasMark()) {
			result = hieroglyphicTextModel.getTopItemsBetween(caret.getMin(),
					caret.getMax());
		}
		return result;
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void toggleGrammar() {
		processLastSignOrSelection(hieroglyph ->
				hieroglyph.setGrammar(!hieroglyph.isGrammar())
			);
	}

	// UNDO/REDO
	public void toggleIgnoredSign() {
		processLastSignOrSelection(hieroglyph ->
				hieroglyph.getModifiers().setBoolean("i",
						!hieroglyph.getModifiers().getBoolean("i"))
		);
	}

	/**
	 *
	 */
	// UNDO/REDO
	public void toggleRedSign() {
		processLastSignOrSelection(hieroglyph ->
				hieroglyph.getModifiers().setBoolean("red",
						!hieroglyph.getModifiers().getBoolean("red"))

		);
	}

	/**
	 * Shade the last selected sign.
	 *
	 * @param shade the shade, expressed as a sum of Shading codes.
	 * @see ShadingCode
	 */
	public void doShadeSign(int shade) {
		// The sign shading system is somehow awkward.
		final StringBuffer code = new StringBuffer();
		if ((shade & ShadingCode.TOP_START) != 0) {
			code.append('1');
		}
		if ((shade & ShadingCode.TOP_END) != 0) {
			code.append('2');
		}
		if ((shade & ShadingCode.BOTTOM_START) != 0) {
			code.append('3');
		}
		if ((shade & ShadingCode.BOTTOM_END) != 0) {
			code.append('4');
		}
		if (code.toString().equals("")) {
			code.append("0");
		}
		processLastSignOrSelection(hieroglyph ->
				hieroglyph.getModifiers().setInteger("shading",
						Integer.parseInt(code.toString()))
		);
	}

	// UNDO/REDO
	public void toggleWideSign() {
		processLastSignOrSelection( hieroglyph ->
				hieroglyph.getModifiers().setBoolean("l",
						!hieroglyph.getModifiers().getBoolean("l"))

		);
	}

	// UNDO/REDO
	public void undo() {
		hieroglyphicTextModel.undo();
	}

	/**
	 * Receive a notification from the inner text model. Update our data, and
	 * notify our own observers.
	 */
	// UNDO/REDO
	public void update(Observable o, Object arg) {
		if (arg == null) {
			// A new Text has been loaded.
			// We set up the caret, but this should be placed in the caret
			// itself !
			caret.changeModel(hieroglyphicTextModel.getModel());

			for (Iterator<MDCModelEditionListener> i = listeners.iterator(); i
					.hasNext(); ) {
				MDCModelEditionListener l = i.next();
				l.textChanged();
			}
			clearSeparator();
		} else if (arg instanceof ModelOperation) {
			ModelOperation op = (ModelOperation) arg;
			for (Iterator<MDCModelEditionListener> i = listeners.iterator(); i
					.hasNext(); ) {
				MDCModelEditionListener l = i.next();
				l.textEdited(op);
			}
		} else {
			throw new RuntimeException("should not happen.");
		}
	}

	public HieroglyphicTextModel getHieroglyphicTextModel() {
		return hieroglyphicTextModel;
	}

	/**
	 * Add an element specified by an external layer.
	 *
	 * @param element
	 */
	public void insertElement(ModelElement element) {
		possibilitiesHandler.clear();
		hieroglyphicTextModel.insertElementAt(caret.getInsertPosition(),
				element.buildTopItem());
	}

	public boolean canUndo() {
		return hieroglyphicTextModel.canUndo();
	}

	public boolean canRedo() {
		return hieroglyphicTextModel.canRedo();
	}

	public boolean mustSave() {
		return hieroglyphicTextModel.mustSave();
	}
}

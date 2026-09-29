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
package jsesh.document;

import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import org.qenherkhopeshef.observable.ObservableEventListener;
import org.qenherkhopeshef.observable.ObservableEventPublisher;
import org.qenherkhopeshef.observable.ObservableEventSupport;

import jsesh.document.command.CommandFactory;
import jsesh.document.command.MDCCommand;
import jsesh.document.events.NewTextEvent;
import jsesh.document.events.TextEvent;
import jsesh.document.events.TextOperationEvent;
import jsesh.mdcreader.MDCParserModelGenerator;
import jsesh.parser.MDCSyntaxError;
import jsesh.model.constants.Dialect;
import jsesh.model.MDCPosition;
import jsesh.model.ModelElementObserver;
import jsesh.model.TopItem;
import jsesh.model.HieroglyphicText;
import jsesh.model.operations.ModelOperation;

/**
 * The edition model of a hieroglyphic text.
 * <p>
 * This model represents a edited text and all operations which can be made on
 * it. (including changing the text completely).
 * <p>
 * As such, it handles undo/redo operations and the like.
 * <p>
 * The advantages of using this class is that the text can be easily loaded,
 * cleared, and so on.
 * <p>
 * TODO : express this class in terms of patterns, and modify it accordingly.
 * We should probably define the HieroglyphicText and the HieroglyphicTextModel in terms 
 * of interfaces, and have HieroglyphicTextModel extend or implement HieroglyphicText.
 * <p>
 * FIXME : currently, the Observable pattern used is misleading.
 * <p>
 * For instance, the workflow and the Carets are all understood as Observers.
 * Now, the workflow uses the carets, and hence should be warned <em>after</em>
 * them, because some of them may be out of date at the time they are used.
 * <p>
 * <p>
 * In a few cases, these caused a problem with the code of getCurrentMDCLine,
 * but it's now fixed.
 * <p>
 * <p>
 * The carets would probably deserve a better treatment (and we might use a more
 * precise class than observer). This file is free Software (c) Serge Rosmorduc
 *
 * @author rosmord
 */
public class HieroglyphicTextModel implements ObservableEventPublisher<TextEvent> {

	private HieroglyphicText text;
	private boolean philologyIsSign;
	private boolean debug;
	private UndoManager undoManager;
	private ObservableEventSupport<TextEvent> eventSupport = new ObservableEventSupport<>();

	/**
	 * Internal observer for model components.
	 */
	private final ModelElementObserver modelElementObserver = this::internalElementChanged;

	public HieroglyphicTextModel() {
		undoManager = new UndoManager();
		setHieroglyphicText(new HieroglyphicText());
		philologyIsSign = true;
		debug = false;
	}

	/// Returns the edited text, for READ-ONLY PURPOSES ONLY.
	///
	/// Currently, nothing enforces this rule. We might either decide to hide
	/// the text behind some kind of specific interface, or send a copy.
	///
	/// @return the edited text.
	public HieroglyphicText getHieroglyphicText() {
		return text;
	}

	/// Replaces the edited text. Clears the undo history.
	///
	/// @param text the new text.
	public void setHieroglyphicText(HieroglyphicText text) {
		undoManager.clear();
		// Detach the ancient text.
		if (this.text != null) {
			this.text.deleteObserver(modelElementObserver);
		}
		this.text = text;
		if (text != null) {
			text.addObserver(modelElementObserver);
		}
		eventSupport.fireEvent(new NewTextEvent());		
	}

	public void clear() {
            // There was a bug, documented by the small demo ViewUpdateBug
            // if we simply clear the view by calling setHieroglyphicText
            // can't reproduce it ??!!??
            setHieroglyphicText(new HieroglyphicText());
            
            
            // The following two lines are annoying, because they generate
            // an edition in the text, and this edition should leave the text in
            // a "clean" state, which is not normally the case. Hence, complications.
            
            // removeElements(buildFirstPosition(), getLastPosition());
            // undoManager.clear(); // This is somehow a patch. The behaviour of the undo manager should be clearer.
            
            // This line is buggy, but I can't remember when it is so...
            // It triggers a NullPointerException in some context...
            
            
	}

	public void readHieroglyphicText(Reader in) throws MDCSyntaxError {
		// long start= System.currentTimeMillis();
		HieroglyphicText l = createGenerator(Dialect.OTHER).parse(in);
		// System.err.println("model created in "+ (System.currentTimeMillis() -
		// start));
		clear();
                insertElementsAt(buildFirstPosition(), l.asList());
	}

	/**
	 * read data into the model. If the data is incorrect, a line-by-line
	 * reading system will be used, and incorrect lines will be transformed into
	 * plain text, for possible correction.
	 *
	 * @param in
	 * @param dialect a MDC dialect identifier from Dialect
	 * @throws MDCSyntaxError
	 * @see Dialect
	 */
	public void readHieroglyphicText(Reader in, Dialect dialect)
			throws MDCSyntaxError {
		if (Dialect.TKSESH.equals(dialect)) {
			this.philologyIsSign = false;
		} else {
			this.philologyIsSign = true;
		}
		HieroglyphicText l = createGenerator(dialect).parse(in);
		setHieroglyphicText(l);
	}

	public void setMDCCode(String text) throws MDCSyntaxError {
		StringReader r = new StringReader(text);
		readHieroglyphicText(r);
	}

	private MDCParserModelGenerator createGenerator(Dialect dialect) {
		MDCParserModelGenerator generator = new MDCParserModelGenerator(dialect);
		generator.setPhilologyAsSigns(philologyIsSign);
		generator.setDebug(debug);
		return generator;
	}

	/**
	 * Returns the debug.
	 *
	 * @return boolean
	 */
	public boolean isDebug() {
		return debug;
	}

	/**
	 * Returns the philologyIsSign.
	 *
	 * @return boolean
	 */
	public boolean isPhilologyIsSign() {
		return philologyIsSign;
	}

	/**
	 * Sets the debug.
	 *
	 * @param debug The debug to set
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Sets the philologyIsSign.
	 *
	 * @param philologyIsSign The philologyIsSign to set
	 */
	public void setPhilologyIsSign(boolean philologyIsSign) {
		this.philologyIsSign = philologyIsSign;
	}

	/**
	 * Called when a sub element of the model is changed.
	 * @param operation
	 */
	private void internalElementChanged(ModelOperation operation) {
		eventSupport.fireEvent(new TextOperationEvent(operation));		
	}

	/**
	 * Build a list of topitems from a String.
	 *
	 * @param text
	 * @return the corresponding list of items.
	 * @throws MDCSyntaxError
	 */
	public List<TopItem> buildItems(String text) throws MDCSyntaxError {
		MDCParserModelGenerator generator = createGenerator(Dialect.OTHER);
		StringReader r = new StringReader(text);
		HieroglyphicText t = generator.parse(r);
		// We take advantage of the return value of removeTopItems here :
		return t.removeTopItems(new MDCPosition(0), t.getLastPosition());
	}

	/// A simple method for adding text at a given place. Note that this method
	/// is really not the most efficient one. It's quite error prone. Yet it
	/// allows a to write simple stuff fast.
	///
	/// @param position where to insert the text.
	/// @param mdcText the text to insert, in Manuel de Codage.
	/// @throws MDCSyntaxError if `mdcText` is not correct.
	public void insertMDCText(MDCPosition position, String mdcText) throws MDCSyntaxError {
		List<TopItem> items = buildItems(mdcText);
		insertElementsAt(position, items);
	}

	/**
	 * Is the model clean (i.e. has it been modified since last loaded or saved
	 * ?).
	 */
	public boolean isClean() {
		return undoManager.isClean();
	}

	public MDCPosition buildFirstPosition() {
		return new MDCPosition(0);
	}

	/**
	 * Replaces the element at a certain position by a new one.
	 *
	 * @param position   : the cursor position <em>after</em> the element to replace.
	 * @param newElement : the new element.
	 */
	public void replaceElementBefore(MDCPosition position, TopItem newElement) {
		replaceElementBefore(position, Collections.singletonList(newElement));
	}

	public void replaceElementBefore(MDCPosition position,
									 List<TopItem> newElements) {
		MDCCommand command = new CommandFactory().buildReplaceCommand(text,
				newElements, position.getPreviousPosition(1), position,
				isFirstCommand());
		undoManager.doCommand(command);
	}

	/**
	 * Insert a list of elements at a certain point.
	 *
	 * @param position
	 * @param elements a list of TopItems
	 */
	public void insertElementsAt(MDCPosition position, List<TopItem> elements) {
		MDCCommand command = new CommandFactory().buildInsertCommand(text,
				elements, position, isFirstCommand());
		undoManager.doCommand(command);
	}

	/**
	 * replace text.
	 * Note that the order of pos1 and pos2 is not important.
	 * @param pos1 one of the limits of the text to replace
	 * @param pos2 the other limit  of the text to replace
	 * @param newElements the text to insert instead of the current text.
	 */
	public void replaceElement(MDCPosition pos1, MDCPosition pos2,
							   List<TopItem> newElements) {
		MDCCommand command = new CommandFactory().buildReplaceCommand(
				getHieroglyphicText(), newElements, pos1, pos2, isClean());
		undoManager.doCommand(command);
	}

	/**
	 * Replace the text between pos1 and pos2 by a single element.
	 * @param pos1
	 * @param pos2
	 * @param element
	 */
	public void replaceElement(MDCPosition pos1, MDCPosition pos2,
							   TopItem element) {
		replaceElement(pos1, pos2, Collections.singletonList(element));
	}

	/// Inserts elements typed by the user (typically, one alphabetic
	/// character). Unlike [#insertElementsAt(MDCPosition, List)], successive
	/// typed insertions are undone together, one word at a time.
	///
	/// @param position where to insert.
	/// @param elements the typed elements.
	public void insertTypedElementsAt(MDCPosition position, List<TopItem> elements) {
		MDCCommand command = new CommandFactory().buildTypingCommand(text,
				elements, position, isFirstCommand());
		undoManager.doCommand(command);
	}

	public void insertElementAt(MDCPosition position, TopItem item) {
		insertElementsAt(position, Collections.singletonList(item));
	}

	// I Don't know if I will use this "first command" stuff.
	// Meanwhile, I have moved the responsability to make the choice to this
	// method.
	private boolean isFirstCommand() {
		// TODO Auto-generated method stub
		return false;
	}

	/// Returns a *copy* of the items between two positions. There are no
	/// conditions on the order of `pos1` and `pos2`.
	///
	/// @param pos1 one limit.
	/// @param pos2 the other limit.
	/// @return a copy of the items between the two limits.
	/// @see HieroglyphicText#getTopItemsBetween(MDCPosition, MDCPosition)
	public List<TopItem> getTopItemsBetween(MDCPosition pos1, MDCPosition pos2) {
		return text.getTopItemsBetween(pos1, pos2);
	}

	public void removeElements(MDCPosition minPosition, MDCPosition maxPosition) {
		MDCCommand command = new CommandFactory().buildRemoveCommand(text,
				minPosition, maxPosition, isClean());
		undoManager.doCommand(command);
	}

	public MDCPosition getLastPosition() {
		return text.getLastPosition();
	}

	public MDCPosition buildPosition(int index) {
		return text.getPositionAt(index);
	}

	public void redo() {
		if (undoManager.canRedo()) {
			undoManager.redo();
		}

	}

	public void undo() {
		if (undoManager.canUndo()) {
			undoManager.undoCommand();
		}

	}

	/// Replaces a part of the text with a text in MdC.
	/// The order of `start` and `end` is not important.
	///
	/// @param start one limit of the text to replace.
	/// @param end the other limit of the text to replace.
	/// @param text the new text, in Manuel de Codage.
	/// @throws MDCSyntaxError if `text` is not correct.
	public void replaceWithMDCText(MDCPosition start, MDCPosition end, String text)
			throws MDCSyntaxError {
		List<TopItem> items = buildItems(text);
		replaceElement(start, end, items);
	}

	public boolean canUndo() {
		return undoManager.canUndo();
	}

	/**
	 * Return true if an operation can be redone.
	 *
	 * @return
	 * @see jsesh.document.UndoManager#canRedo()
	 */
	public boolean canRedo() {
		return undoManager.canRedo();
	}

	public boolean mustSave() {
		return !undoManager.isClean();
	}

	public void setClean() {
		undoManager.clear();
	}

	/**
	 * Returns <em>a copy of</em> the item just before a given position, or null
	 * if the position is the first one.
	 *
	 * @param position
	 * @return an item or null
	 */
	public TopItem getItemBefore(MDCPosition position) {
		MDCPosition pos1 = position.getPreviousPosition(1);
		TopItem head = null;
		List<TopItem> previousElement = getTopItemsBetween(pos1, position);
		if (!previousElement.isEmpty()) {
			head = previousElement.get(0);
		}
		return head;

	}

	/**
	 * Returns all positions where query matches were found.
	 * @param query
	 * @return a list of positions.
	 */
	public List<MDCPosition> doSearch(MdCSearchQuery query) {
		return query.doSearch(getHieroglyphicText());
	}

	/**
	 * Returns the line limits for the line around a given position.
	 * More precisely, will return a list of two positions [pos1, pos2] around pos, with :
	 * <p> pos1 &le; pos &le; pos2 </p>
	 * <ul>
	 *     <li>pos1 is the position in front of the first element in the line containing pos;</li>
	 *	   <li>pos2 is the position after the last element in the line containing pos;</li>
	 * </ul>
	 * <p> a line being a list of elements which are not page or line break.</p>
	 * @param pos a position in the line.
	 * @return a list of two elements.
	 */

	public List<MDCPosition> getLineLimitsAround(MDCPosition pos) {
		return text.getLineLimitsAround(pos);
	}
        
        /**
	 * Returns the page limits for the page around a given position.
	 * More precisely, will return a list of two positions [pos1, pos2] around pos, with :
	 * <p> pos1 &le; pos &le; pos2 </p>
	 * <ul>
	 *     <li>pos1 is the position in front of the first element in the page containing pos;</li>
	 *	   <li>pos2 is the position after the last element in the page containing pos;</li>
	 * </ul>
	 * <p> a page being a list of elements which are not page break.</p>
	 * @param pos a position in the line.
	 * @return a list of two elements.
	 */

	public List<MDCPosition> getPageLimitsAround(MDCPosition pos) {
		return text.getPageLimitsAround(pos);
	}
        
        
    /**
     * Gets original line number coordinates of a certain point in the text.
     * <p>
     * If the document contains line-number indications, like (vo, 3) which
     * reference the actual source document (ostracon, papyrus...), this
     * function will return the coordinates for a given point in text.
     *
     * @param position technical position in the JSesh document.
     * @return the position in the original document, or the empty string if
     * none is found.
     */
  
    public String getOriginalDocumentCoordinates(MDCPosition position) {
        return text.getOriginalDocumentCoordinates(position);
    }

	@Override
	public void addListener(ObservableEventListener<TextEvent> listener) {
		eventSupport.addListener(listener);				
	}

	@Override
	public void removeListener(ObservableEventListener<TextEvent> listener) {
		eventSupport.removeListener(listener);
	}
}

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
package jsesh.ui.editor;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.qenherkhopeshef.observable.ObservableEventListener;

import jsesh.document.events.NewTextEvent;
import jsesh.document.events.TextEvent;
import jsesh.document.events.TextOperationEvent;
import jsesh.glyphs.signdata.Possibility;
import jsesh.glyphs.signdata.PossibilityRepository;
import jsesh.parser.MDCSyntaxError;
import jsesh.sharedvocabulary.CartouchePart;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.sharedvocabulary.InsertionDirection;
import jsesh.sharedvocabulary.CartoucheType;
import jsesh.model.constants.SymbolCodes;
import jsesh.sharedvocabulary.WordEnding;
import jsesh.model.AbsoluteGroup;
import jsesh.model.AlphabeticCharacter;
import jsesh.model.constants.ScriptCode;
import jsesh.model.BasicItemList;
import jsesh.model.Cadrat;
import jsesh.model.CaretGranularity;
import jsesh.model.Cartouche;
import jsesh.model.ComplexLigature;
import jsesh.model.HBox;
import jsesh.model.Hieroglyph;
import jsesh.model.InnerEdit;
import jsesh.model.InnerGroup;
import jsesh.model.Insert;
import jsesh.model.ItemSequence;
import jsesh.model.Ligature;
import jsesh.model.LineBreak;
import jsesh.model.MDCMark;
import jsesh.model.MDCPosition;
import jsesh.model.PositionNavigator;
import jsesh.model.ModelElement;
import jsesh.model.PageBreak;
import jsesh.model.Philology;
import jsesh.model.ShadingCode;
import jsesh.model.SubCadrat;
import jsesh.model.Superscript;
import jsesh.model.TopItem;
import jsesh.model.HieroglyphicText;
import jsesh.model.TextScope;
import jsesh.model.Zone;
import jsesh.model.ZoneContent;
import jsesh.model.ZoneParameters;
import jsesh.model.operations.ModelOperation;
import jsesh.model.tools.BasicItemListGrouper;
import jsesh.model.tools.CadratStarInserter;
import jsesh.model.tools.ComplexLigatureExtractor;
import jsesh.model.tools.DeepHieroglyphsProcessor;
import jsesh.model.tools.GroupExploder;
import jsesh.model.tools.HieroglyphExtractor;
import jsesh.model.tools.HorizontalGrouper;
import jsesh.model.tools.InnerGroupBuilder;
import jsesh.model.tools.LastHieroglyphSelector;
import jsesh.model.tools.VerticalGrouper;
import jsesh.model.tools.VerticallyCenteredGrouper;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.layout.insert.InsertionRanker;
import jsesh.render.view.AbsoluteGroupBuilder;
import jsesh.document.EditingScope;
import jsesh.document.HieroglyphicTextModel;
import jsesh.document.caret.MDCCaret;
import jsesh.document.caret.MDCCaretChangeListener;
import jsesh.io.mdc.MdCModelWriter;
import jsesh.io.mdc.MdcTextHelper;

/**
 * An abstract representation of the editing process of a hieroglyphic text.
 * <p>
 * far too large a class.
 * <p>
 * We should separate parts of it, and move the actual manipulations in the
 * model. We would only keep what is relevant for us (e.g. possibility lists for
 * signs, undo, etc...)
 * <p>
 * TODO : current version: sign insertion should be done in two steps.
 * <ol>
 * <li>find what text needs to be written
 * <li>replace the "old text" by this new text
 * <li>in a second step, try to implement the "events contain undo commands"
 * system. It's likely that the current system is too precise. We always go
 * through TextModel, and the events can be generated at that level (and not at
 * the model element actual level, which is probably too fine ?)
 * </ol>
 * TODO : check if we didn't implement the previous TODO !!!
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

public class JMDCEditorWorkflow implements MDCCaretChangeListener {

  
    // NOTE : each method which has been reread for undo/redo capability will be
    // associated with a comment

    /**
     * Cursor position and selection.
     */
    private MDCCaret caret;

    /// Can the cursor stand inside quadrats, or only between top-level items?
    private CaretGranularity caretGranularity = CaretGranularity.QUADRAT;

    /// Where the last sign was inserted by [#insertSignAtInnerPosition(String)],
    /// so that [#nextPossibility()] can replace it. Null if the last sign was
    /// added at top level.
    private MDCPosition innerRetryPosition = null;

    /// Largest enlargement of the frame of an insertion's main group, in percent.
    public static final int MAX_INSERTION_ENLARGEMENT = 200;

    /// The choices prepared by [#prepareChoiceOfInsertion(InsertionRanker)].
    private List<InsertionChoice> insertionChoices = Collections.emptyList();

    /// Index of the insertion currently in the text.
    private int selectedInsertionIndex = -1;

    /// The insertion which was in the text when the choices were prepared.
    private Insert insertionOriginal = null;

    /// Computes the choices again when the enlargement changes.
    private InsertionRanker insertionRanker = null;

    /// Enlargement of the frame for the choices, in percent (see [Insert#getDw()]).
    private int insertionDw = 0;

    /// Enlargement of the frame for the choices, in percent (see [Insert#getDh()]).
    private int insertionDh = 0;

    /// True if the original insertion has been replaced (and the replacement can be undone).
    private boolean insertionReplaced = false;

    /// The (local) position after the insertion being chosen.
    private MDCPosition insertionCaretPosition = null;

    /**
     * The code being typed
     */
    private final StringBuffer currentCode;

    private char currentSeparator = ' ';

    /**
     * The current hieroglyphic text.
     */
    private HieroglyphicTextModel hieroglyphicTextModel;

    /**
     * Is it possible to modify this text, or is this simply a view?
     */
    private boolean readWrite = true;

    /**
     * What typed keys produce: hieroglyphs, latin text, transliteration...
     */
    private InputMode mode;

    /**
     * System for handling multiple choices of glyphs.
     */
    private final PossibilitiesHandler possibilitiesHandler;

    /**
     * Internal event listener.
     */
    private ObservableEventListener<TextEvent> textChangeListener = e -> textChanged(e);

    /**
     * External list of listeners.
     */
     private final List<MDCModelEditionListener> listeners;


    public JMDCEditorWorkflow(PossibilityRepository possibilityRepository) {
        this(new HieroglyphicTextModel(), possibilityRepository);
    }

    public JMDCEditorWorkflow(HieroglyphicTextModel data, PossibilityRepository possibilityRepository) {
        listeners = new ArrayList<>();
        possibilitiesHandler = new PossibilitiesHandler(possibilityRepository);
        setHieroglyphicTextModel(data);
        currentCode = new StringBuffer();
        mode = InputMode.HIEROGLYPHS;
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

    public boolean addCartouche(int type, int start, int end) {
        boolean result;
        List<TopItem> elts = getSelection();
        BasicItemList l = new BasicItemListGrouper().extractBasicItemList(elts);
        if (l != null) {
            Cartouche c = new Cartouche(CartoucheType.forCode((char) type),
                    CartouchePart.forDigit(start), CartouchePart.forDigit(end), l);
            EditingScope scope = scope();
            MDCPosition insertPos = caret.getLocalInsertPosition();
            MDCPosition markPos = caret.getLocalMarkPosition();
            clearMark();
            scope.replaceElement(insertPos, markPos, c.buildTopItem());
            result = true;
        } else {
            result = false;
        }
        clearSeparator();
        return result;
    }

    /// Puts the selection in a new zone (`%zone(...)`), with default settings,
    /// or inserts an empty zone if nothing is selected. The cursor is then
    /// placed at the end of the zone's content.
    ///
    /// Zones can't be placed in a cartouche: there, nothing is done.
    ///
    /// @return true if the zone was created.
    public boolean addZone() {
        return addZone(ZoneParameters.DEFAULT);
    }

    /// Puts the selection in a new zone with the given settings, or inserts an
    /// empty zone if nothing is selected. The cursor is then placed at the end
    /// of the zone's content.
    ///
    /// Zones can't be placed in a cartouche: there, nothing is done.
    ///
    /// @param parameters the settings of the new zone.
    /// @return true if the zone was created.
    public boolean addZone(ZoneParameters parameters) {
        EditingScope scope = scope();
        MDCPosition minPos = caret.getLocalMinPosition();
        MDCPosition maxPos = caret.getLocalMaxPosition();
        List<TopItem> content = getSelection();
        Zone zone = new Zone(parameters, new ZoneContent(content));
        if (!scope.accepts(List.of(zone))) {
            return false;
        }
        clearMark();
        scope.replaceElement(minPos, maxPos, zone);
        // Inside the zone (its only child is its content), after its last item.
        int[] zonePath = scope.toTextPosition(minPos).getPath();
        int[] path = Arrays.copyOf(zonePath, zonePath.length + 2);
        path[zonePath.length] = 0;
        path[zonePath.length + 1] = content.size();
        caret.setInsertPosition(MDCPosition.of(path));
        clearSeparator();
        return true;
    }

    /// The settings of the innermost zone around the cursor.
    ///
    /// @return the settings, or empty if the cursor is not in a zone.
    public Optional<ZoneParameters> getZoneParametersAtCaret() {
        List<Zone> zones = enclosingZones();
        return zones.isEmpty() ? Optional.empty() : Optional.of(zones.get(0).getParameters());
    }

    /// Changes the settings of the innermost zone around the cursor. The change
    /// can be undone, and the cursor stays where it was.
    ///
    /// @param parameters the new settings.
    /// @return false if the cursor is not in a zone.
    public boolean setZoneParametersAtCaret(ZoneParameters parameters) {
        if (enclosingZones().isEmpty()) {
            return false;
        }
        MDCPosition cursor = caret.getInsertExactPosition();
        HieroglyphicText text = caret.getModel();
        TextScope contentScope = innermostZoneContentScope();
        // The zone's content is [... zoneIndex, 0]: the zone sits in the scope above.
        int[] contentPath = contentScope.getPath();
        int zoneIndex = contentPath[contentPath.length - 2];
        TextScope zoneScope = TextScope.of(Arrays.copyOf(contentPath, contentPath.length - 2));
        ZoneContent oldContent = (ZoneContent) contentScope.resolve(text);
        Zone newZone = new Zone(parameters, new ZoneContent(oldContent.getItemsBetween(0, oldContent.getNumberOfChildren())));
        EditingScope scope = new EditingScope(hieroglyphicTextModel, zoneScope);
        clearMark();
        scope.replaceElement(new MDCPosition(zoneIndex), new MDCPosition(zoneIndex + 1), newZone);
        // The structure is unchanged: the cursor is still valid.
        caret.setInsertPosition(cursor);
        clearSeparator();
        return true;
    }

    /// Removes the innermost zone around the cursor, and puts its content in
    /// the zone's place, in the zone's parent: the reverse of [#addZone()]. The
    /// change can be undone, and the cursor stays at the same place in the
    /// content.
    ///
    /// @return false if the cursor is not in a zone.
    public boolean unwrapZone() {
        if (enclosingZones().isEmpty()) {
            return false;
        }
        int[] cursorPath = caret.getInsertExactPosition().getPath();
        HieroglyphicText text = caret.getModel();
        TextScope contentScope = innermostZoneContentScope();
        // The zone's content is [... zoneIndex, 0]: the zone sits in the scope above.
        int[] contentPath = contentScope.getPath();
        int zoneIndex = contentPath[contentPath.length - 2];
        TextScope zoneScope = TextScope.of(Arrays.copyOf(contentPath, contentPath.length - 2));
        ZoneContent content = (ZoneContent) contentScope.resolve(text);
        List<TopItem> items = content.getItemsBetween(0, content.getNumberOfChildren());
        EditingScope scope = new EditingScope(hieroglyphicTextModel, zoneScope);
        if (!scope.accepts(items)) {
            return false;
        }
        clearMark();
        scope.replaceElement(new MDCPosition(zoneIndex), new MDCPosition(zoneIndex + 1), items);
        // [... zoneIndex, 0, k, rest] becomes [... zoneIndex + k, rest].
        int[] path = new int[cursorPath.length - 2];
        System.arraycopy(cursorPath, 0, path, 0, zoneScope.depth());
        path[zoneScope.depth()] = zoneIndex + cursorPath[contentPath.length];
        System.arraycopy(cursorPath, contentPath.length + 1, path, zoneScope.depth() + 1,
                cursorPath.length - contentPath.length - 1);
        caret.setInsertPosition(MDCPosition.of(path));
        clearSeparator();
        return true;
    }

    /**
     * @param l
     */

    public void addMDCModelListener(MDCModelEditionListener l) {
        listeners.add(l);
    }

    
    /// Adds a philological markup around the selected zone. 
    /// 
    /// Respects the current model system for philological markup: 
    ///  - if markup is considered as parenthesis, it will add a structure;
    ///  - if it's considered as signs, it will merely add two signs.
    ///
    /// If the selection is empty, it will add the markup and leave the cursor 
    /// between the two markup elements, for it is likely that the user wants to
    /// add something between the two balanced elements.
    /// 
    /// Note : to spare ourselves the problem of creating very specific
    /// multi-commands here, we have decided that the undo operation would remove
    /// one element at a time here.
    /// @param type one of SymbolCodes.ERASEDSIGNS, EDITORADDITION,
    ///             EDITORSUPERFLUOUS, PREVIOUSLYREADABLE or SCRIBEADDITION.
    /// @return true if success, false if failure.
    /// 

    public boolean addPhilologicalMarkup(int type) {
        boolean result;
        possibilitiesHandler.clear();
        EditingScope scope = scope();
        if (hieroglyphicTextModel.isPhilologyIsSign()) {
            MDCPosition max = caret.getLocalMaxPosition();
            MDCPosition min = caret.getLocalMinPosition();
            // See the SymbolCodes for the explanation of the magic *2 and *2+1
            // below.
            scope.insertElementAt(max, new Hieroglyph(
                    type * 2 + 1).buildTopItem());

            scope.insertElementAt(min,
                    new Hieroglyph(type * 2).buildTopItem());
            caret.setInsertPosition(scope.toTextPosition(scope.getNextPosition(max, 1)));
            result = true;
        } else {
            BasicItemListGrouper grouper = new BasicItemListGrouper();
            List<TopItem> elts = getSelection();
            BasicItemList l = grouper.extractBasicItemList(elts);
            if (l != null) {
                Philology c = new Philology(type, l);
                scope.replaceElement(caret.getLocalInsertPosition(),
                        caret.getLocalMarkPosition(), c.buildTopItem());
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

    public void addSameLevel() {
        EditingScope scope = scope();
        ModelElement elt = scope.getElementBefore(caret.getLocalInsertPosition());
        if (elt instanceof Cadrat) {
            Cadrat c = (Cadrat) elt.deepCopy(); // Won't be needed in the future
            // if getElementBefore sends
            // copies too.
            // get last hbox.
            HBox hbox = c.getHBox(c.getNumberOfChildren() - 1);
            // add sign :
            Hieroglyph hiero = buildHieroglyphFromCode(currentCode.toString());
            hbox.addHorizontalListElement(hiero);
            scope.replaceElementBefore(caret.getLocalInsertPosition(), c);
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
     * Use next possible choice for the previously input code.
     */
    public void nextPossibility() {
        if (possibilitiesHandler.hasPossibilities()) {
            replaceLastPossibility(possibilitiesHandler::next);
        }
    }

    /// Prepares the choice of a possibility in a list, as an alternative to
    /// cycling with [#nextPossibility()].
    ///
    /// Like the space key, this accepts the code being typed (if any), using its
    /// first possibility. The choices are then available through
    /// [#getPossibilityChoices()] and [#selectPossibility(int)].
    ///
    /// @return true if the sign just entered has several possibilities to choose from.
    public boolean prepareChoiceOfPossibility() {
        if (mode != InputMode.HIEROGLYPHS) {
            return false;
        }
        if (currentCode.length() != 0) {
            acceptSeparator(' ');
        }
        return currentSeparator == ' ' && currentCode.length() == 0
                && possibilitiesHandler.getChoices().size() > 1;
    }

    /// The possibilities for the sign just entered.
    ///
    /// @return an immutable list, empty when there is nothing to choose from.
    public List<Possibility> getPossibilityChoices() {
        return possibilitiesHandler.getChoices();
    }

    /// Index, in [#getPossibilityChoices()], of the possibility currently in the text.
    public int getSelectedPossibilityIndex() {
        return possibilitiesHandler.hasPossibilities() ? possibilitiesHandler.getSelectedIndex() : -1;
    }

    /// Replaces the sign just entered by another of its possibilities.
    ///
    /// Does nothing if the cursor has moved since the sign was entered.
    ///
    /// @param index index in [#getPossibilityChoices()].
    public void selectPossibility(int index) {
        if (possibilitiesHandler.hasPossibilities()
                && index >= 0 && index < possibilitiesHandler.getChoices().size()) {
            replaceLastPossibility(() -> possibilitiesHandler.select(index));
        }
    }

    /// Undoes the last insertion of a possibility, changes the selected
    /// possibility with `selector`, and inserts the new one.
    private void replaceLastPossibility(Runnable selector) {
        hieroglyphicTextModel.undo();
        if (innerRetryPosition != null) {
            // The undo has put the caret back at top level.
            caret.setInsertPosition(innerRetryPosition);
        }
        selector.run();
        addCodeAndThenGroup();
    }

    /**
     * Adds a sign whose code is "code" to the text.
     *
     * @param code the sign code (which has the form of an identifier).
     */

    public void addSign(String code) {
        scope().insertElementAt(caret.getLocalInsertPosition(),
                buildItemForSignCode(code));
        clearCode();
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
     * Replaces the selection with g.
     *
     * @param g
     */
    public void replaceSelectionByAbsoluteGroup(AbsoluteGroup g) {
        clearSeparator();
        possibilitiesHandler.clear();
        if (caret.hasSelection() && !g.containsOnlyOneSign()) {
            g.compact();
            scope().replaceElement(caret.getLocalInsertPosition(),
                    caret.getLocalMarkPosition(), g.buildTopItem());
        }
    }

    @Override
    public void caretChanged(MDCCaret caret) {
        for (Iterator<MDCModelEditionListener> i = listeners.iterator(); i
                .hasNext();) {
            MDCModelEditionListener l = i.next();
            l.caretChanged(caret);
        }
    }

    /**
     * Set the angle for either the last hieroglyph or all glyphs in selection,
     * if there is a selection.
     *
     * @param angle
     */

    public void setAngle(final int angle) {
        applySignLevelModifications(h -> h.setAngle(angle));
    }

    /**
     * Clear the text in the current model.
     */
    public void clear() {
        possibilitiesHandler.clear();
        hieroglyphicTextModel.clear();
        clearSeparator();

        currentCode.setLength(0);
        clearSeparator();
    }

    public void clearCode() {
        currentCode.setLength(0);
        notifyCodeChangeListeners();
    }

    public void clearMark() {
        caret.unsetMark();
        clearSeparator();
    }

    /**
     * Resets the current separator so that next sign appears on a cadrat of his
     * own.
     */
    public void clearSeparator() {
        currentSeparator = ' ';
        notifySeparatorChangeListeners();
    }

    /**
     * Moves the cursor down one line.
     */
    public void cursorDown() {
        caret.setInsertPosition(verticalPosition(true));
        possibilitiesHandler.clear();
        caret.unsetMark();
        clearSeparator();
        followCaret();
    }

    /**
     * Moves the cursor to the next cadrat.
     */
    public void cursorNext() {
        possibilitiesHandler.clear();
        moveCursorBy(1);
        caret.unsetMark();
        clearSeparator();
        followCaret();
    }

    /**
     * Moves the cursor to the previous cadrat.
     */
    public void cursorPrevious() {
        possibilitiesHandler.clear();
        moveCursorBy(-1);
        caret.unsetMark();
        clearSeparator();
        followCaret();
    }

    /// Moves the cursor one stop to the left or to the right.
    ///
    /// In text of a single direction, this is [#cursorNext()] or
    /// [#cursorPrevious()], depending on the direction of the text at the cursor.
    /// At the boundary of a zone whose direction differs from the one around it,
    /// the move follows what is seen on screen instead of the text order: from
    /// the right edge of a right-to-left zone in a left-to-right text, going
    /// right puts the cursor *after* the zone.
    ///
    /// @param toRight true to move right, false to move left.
    /// @param fallback the direction of the text outside any zone setting one.
    public void cursorHorizontally(boolean toRight, TextDirection fallback) {
        boolean forward = toRight == getTextDirectionAtCaret(fallback).isLeftToRight();
        MDCPosition from = caret.getInsertExactPosition();
        MDCPosition normal = new PositionNavigator(caret.getModel()).move(from, forward ? 1 : -1, caretGranularity);
        Optional<MDCPosition> target = zoneBoundaryTarget(from, normal, toRight, forward, fallback);
        if (target.isEmpty()) {
            if (forward) {
                cursorNext();
            } else {
                cursorPrevious();
            }
            return;
        }
        possibilitiesHandler.clear();
        caret.setInsertPosition(target.get());
        caret.unsetMark();
        clearSeparator();
        followCaret();
    }

    /// Moves the cursor by one stop, according to the current granularity.
    private void moveCursorBy(int delta) {
        caret.moveInsert(delta, caretGranularity);
    }

    /// @return the kind of positions the cursor can stand on.
    public CaretGranularity getCaretGranularity() {
        return caretGranularity;
    }

    /// Sets the kind of positions the cursor can stand on. Going back to
    /// [CaretGranularity#QUADRAT] moves a cursor which lies inside a quadrat
    /// back to the position before this quadrat.
    ///
    /// With both granularities, the cursor enters cartouches and sub-quadrats:
    /// their content is an editing *scope* (see [jsesh.model.TextScope]), where
    /// the editor works exactly as at the top level of the text. With
    /// [CaretGranularity#INNER], {@link #cursorNext()} and {@link #cursorPrevious()}
    /// (hence the arrow keys moving along the text) also stop inside the
    /// quadrats. There, the insertion of signs, {@link #doBackspace()} and
    /// {@link #doDelete()} edit the quadrat around the cursor (see [InnerEdit]),
    /// and the cursor stays inside it when possible. All other commands
    /// (selection, word movements, sign modifications...) work on the gap of
    /// the scope just before the quadrat which contains the cursor.
    ///
    /// @param granularity the new granularity.
    public void setCaretGranularity(CaretGranularity granularity) {
        this.caretGranularity = granularity;
        if (granularity == CaretGranularity.QUADRAT) {
            caret.snapToScopeLevel();
        }
    }

    /**
     * move to cursor to the beginning of the current line.
     */
    public void cursorToBeginningOfLine() {
        possibilitiesHandler.clear();
        EditingScope scope = scope();
        caret.setInsertPosition(scope.toTextPosition(scope.getLineFirstPosition(caret.getLocalInsertPosition())));
        caret.unsetMark();
        clearSeparator();
        followCaret();
    }

    /**
     * move to the end of the current line.
     */
    public void cursorToEndOfLine() {
        possibilitiesHandler.clear();
        EditingScope scope = scope();
        caret.setInsertPosition(scope.toTextPosition(scope.getLineLastPosition(caret.getLocalInsertPosition())));
        caret.unsetMark();
        clearSeparator();
        followCaret();
    }

    /// Moves the cursor to the end of the current (or next) word of
    /// alphabetic text; outside of text, moves to the next position.
    public void cursorNextWord() {
        possibilitiesHandler.clear();
        moveToWordBoundary(1);
        caret.unsetMark();
        clearSeparator();
        followCaret();
    }

    /// Moves the cursor to the start of the current (or previous) word of
    /// alphabetic text; outside of text, moves to the previous position.
    public void cursorPreviousWord() {
        possibilitiesHandler.clear();
        moveToWordBoundary(-1);
        caret.unsetMark();
        clearSeparator();
        followCaret();
    }

    /// Extends the selection to the next (`dir > 0`) or previous (`dir < 0`)
    /// word boundary.
    public void expandSelectionByWord(int dir) {
        if (!caret.hasMark()) {
            caret.setMarkPosition(caret.getInsertExactPosition());
        }
        moveToWordBoundary(dir);
    }

    /// Moves the insertion point to the next (`dir > 0`) or previous word
    /// boundary, in the current scope.
    private void moveToWordBoundary(int dir) {
        EditingScope scope = scope();
        ItemSequence items = scope.getSequence();
        MDCPosition current = caret.getLocalInsertPosition();
        MDCPosition target = dir > 0 ? nextWordPosition(items, current) : previousWordPosition(items, current);
        caret.setInsertPosition(scope.toTextPosition(target));
    }

    /// The position after the next word: skips spaces, then letters, staying
    /// in the same run of text.
    private static MDCPosition nextWordPosition(ItemSequence items, MDCPosition position) {
        int n = items.getNumberOfChildren();
        int index = position.getIndex();
        if (index >= n) {
            return new MDCPosition(n);
        }
        if (!(items.getItemAt(index) instanceof AlphabeticCharacter first)) {
            return new MDCPosition(index + 1);
        }
        int i = index;
        while (i < n && items.getItemAt(i) instanceof AlphabeticCharacter c
                && c.isSameScriptAs(first) && c.isSpace()) {
            i++;
        }
        while (i < n && items.getItemAt(i) instanceof AlphabeticCharacter c
                && c.isSameScriptAs(first) && !c.isSpace()) {
            i++;
        }
        return new MDCPosition(i);
    }

    /// The position before the previous word: skips spaces, then letters,
    /// staying in the same run of text.
    private static MDCPosition previousWordPosition(ItemSequence items, MDCPosition position) {
        int index = position.getIndex();
        if (index <= 0) {
            return new MDCPosition(0);
        }
        if (!(items.getItemAt(index - 1) instanceof AlphabeticCharacter last)) {
            return new MDCPosition(index - 1);
        }
        int i = index;
        while (i > 0 && items.getItemAt(i - 1) instanceof AlphabeticCharacter c
                && c.isSameScriptAs(last) && c.isSpace()) {
            i--;
        }
        while (i > 0 && items.getItemAt(i - 1) instanceof AlphabeticCharacter c
                && c.isSameScriptAs(last) && !c.isSpace()) {
            i--;
        }
        return new MDCPosition(i);
    }

    /**
     * Moves the cursor up one line.
     * <p>
     * Currently, we move to the beginning of the line. In the future, it might
     * be interesting to retain the horizontal position and to try to keep it.
     */
    public void cursorUp() {
        caret.setInsertPosition(verticalPosition(false));
        possibilitiesHandler.clear();
        clearSeparator();
        caret.unsetMark();
        followCaret();
    }

    /**
     * Deletes the current selection.
     */
    public void removeSelectedText() {
        possibilitiesHandler.clear();
        scope().removeElements(caret.getLocalMinPosition(),
                caret.getLocalMaxPosition());
        clearSeparator();
    }

    public void deleteCodeChangeListener(MDCModelEditionListener l) {
        listeners.remove(l);
    }

    public void deleteCursorChangeListener(MDCCaretChangeListener l) {
        caret.removeCaretChangeListener(l);
    }

    /// Erases the element after the cursor, when the cursor stands inside a
    /// quadrat (see [CaretGranularity#INNER]); erases the selection, if any.
    /// Does nothing otherwise.
    public void doDelete() {
        possibilitiesHandler.clear();
        if (currentCode.length() > 0 || currentSeparator != ' ') {
            return;
        }
        if (caret.hasSelection()) {
            removeSelectedText();
        } else if (caret.isInsertInner()) {
            applyInnerEdit(new InnerEdit(getHieroglyphicTextModel().getHieroglyphicText(),
                    caret.getInsertExactPosition()).deleteAfter());
        }
    }

    /// Inserts a sign where the cursor stands, inside a quadrat. The container
    /// of the cursor decides how the sign is combined with its neighbours (see
    /// [InnerEdit]).
    ///
    /// @param code the code of the sign.
    /// @return true if the sign has been inserted. False if this kind of
    ///         container does not support insertion yet.
    private boolean insertSignAtInnerPosition(String code) {
        MDCPosition where = caret.getInsertExactPosition();
        boolean done = applyInnerEdit(new InnerEdit(getHieroglyphicTextModel().getHieroglyphicText(), where)
                .insertSign(buildHieroglyphFromCode(code)));
        innerRetryPosition = done ? where : null;
        return done;
    }

    /// Replaces the top item which holds the cursor by its edited version,
    /// and moves the cursor to the place computed by the edit.
    ///
    /// @param edit the result of an [InnerEdit] operation.
    /// @return true if there was an edit to apply.
    private boolean applyInnerEdit(Optional<InnerEdit.Result> edit) {
        if (edit.isEmpty()) {
            return false;
        }
        InnerEdit.Result result = edit.get();
        MDCPosition start = caret.getInsertPosition();
        MDCPosition end = new MDCPosition(start.getIndex() + 1);
        List<TopItem> replacement = result.replacement().map(List::of).orElse(List.of());
        hieroglyphicTextModel.replaceElement(start, end, replacement);
        caret.setInsertPosition(result.caret());
        caret.unsetMark();
        return true;
    }

    /**
     * Erase text according to the context.
     * <p>
     * The erased text might be : an incomplete code (if any), a separator (if
     * any) a whole selection (if any) or the item just in front of the cursor
     * (still if any).
     * <p>
     * When the cursor stands inside a quadrat (see {@link CaretGranularity#INNER}),
     * the item just in front of the cursor is the element of the quadrat which
     * precedes it.
     */
    public void doBackspace() {

        possibilitiesHandler.clear();

        if (currentCode.length() > 0) {
            currentCode.replace(currentCode.length() - 1, currentCode.length(),
                    "");
            notifyCodeChangeListeners();
        } else if (currentSeparator != ' ') {
            clearSeparator();
        } else {
            // Line numbers (superscripts) are edited letter by letter;
            // alphabetic characters are elements of their own.
            if (caret.hasSelection()) {
                removeSelectedText();
            } else if (caret.isInsertInner()) {
                applyInnerEdit(new InnerEdit(getHieroglyphicTextModel().getHieroglyphicText(),
                        caret.getInsertExactPosition()).deleteBefore());
            } else if (scope().getElementBefore(caret.getLocalInsertPosition()) instanceof Superscript) {
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
    public void doComplexLigature(int signPos) {
        List<TopItem> elts = getSelection();
        EditingScope scope = scope();
        MDCPosition minPos = caret.getLocalMinPosition();
        MDCPosition maxPos = caret.getLocalMaxPosition();
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
                if (c1.getBeforeGroup() != null) {
                    group1 = (InnerGroup) c1.getBeforeGroup().deepCopy();
                }
                if (c1.getAfterGroup() != null) {
                    group2 = (InnerGroup) c1.getAfterGroup().deepCopy();
                }
                h = c1.getHieroglyph().deepCopy();
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
                    h = ((Hieroglyph) hieros.get(0)).deepCopy();
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
                    scope.replaceElement(minPos, maxPos,
                            ligature.buildTopItem());
                    clearMark();
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

    public void doShade(final int shade) {
        possibilitiesHandler.clear();
        applyGroupLevelModifications((TopItem t) -> {
            if (t instanceof Cadrat) {
                Cadrat c = (Cadrat) t;
                c.setShading(shade);
            }
            return Collections.singletonList(t);
        });
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

    public void expandSelection(int dir) {
        if (!caret.hasMark()) {
            caret.setMarkPosition(caret.getInsertExactPosition());
        }
        switch (dir) {
            case 1:
            case -1:
                caret.moveInsert(dir, caretGranularity);
                break;
            case 2:
                caret.setInsertPosition(verticalPosition(true));
                break;
            case -2:
                caret.setInsertPosition(verticalPosition(false));
                break;
            default:
                throw new RuntimeException("Unexpected dir " + dir);
        }
    }

    /**
     * separate the current group into smaller ones.
     */

    public void explodeGroup() {
        final GroupExploder groupExploder = new GroupExploder();
        applyGroupLevelModifications(
                topItem -> groupExploder.explode(topItem));
        clearSeparator();
        possibilitiesHandler.clear();
    }

    /**
     * vertically center some text.
     * <p>
     * it will add zero-sized spacing on top and below of any selected text (or of
     * the text at previous position).
     */

    public void centerGroup() {
        VerticallyCenteredGrouper grouper = new VerticallyCenteredGrouper();
        applyGroupLevelModifications(
                topItem -> Collections.singletonList(grouper.centerText(topItem)));
        clearSeparator();
        possibilitiesHandler.clear();
    }

    /**
     *
     */

    public void focusGained() {
        for (int i = 0; i < listeners.size(); i++) {
            MDCModelEditionListener listener = listeners.get(i);
            listener.focusGained(currentCode);
        }
    }

    /**
     *
     */

    public void focusLost() {
        for (int i = 0; i < listeners.size(); i++) {
            MDCModelEditionListener listener = listeners.get(i);
            listener.focusLost();
        }

    }

    public MDCCaret getCaret() {
        return caret;
    }

    /**
     * @return the current glyph code being entered.
     */

    public StringBuffer getCurrentCode() {
        return currentCode;
    }

    /**
     * Returns the content of the current line as manuel de codage code.
     *
     * @return the content of the current line as manuel de codage code.
     */

    public String getCurrentLineAsString() {
        List<MDCPosition> limits = getLineLimits();
        StringWriter sw = new StringWriter();
        new MdCModelWriter().write(sw, hieroglyphicTextModel.getHieroglyphicText(),
                limits.get(0), limits.get(1));
        return sw.toString();
    }

    /**
     * @return Returns the currentSeparator.
     */

    public char getCurrentSeparator() {
        return currentSeparator;
    }

    /**
     * Return the manuel de codage text for the current model.
     *
     * @return the manuel de codage text for the current model.
     */

    public String getMDCCode() {
        StringWriter sw = new StringWriter();
        new MdCModelWriter().write(sw, hieroglyphicTextModel.getHieroglyphicText(),
                hieroglyphicTextModel.buildFirstPosition(), hieroglyphicTextModel.getLastPosition());
        return sw.toString();
    }

    // Set the data of the current model to String

    public void setMDCCode(String txt) throws MDCSyntaxError {
        possibilitiesHandler.clear();
        hieroglyphicTextModel.setMDCCode(txt);
        clearSeparator();
    }

    /**
     * @return Returns the mode.
     */

    public InputMode getMode() {
        return mode;
    }

    /// Sets the writing mode : hieroglyphs, latin, etc.
    ///
    /// @param mode the new mode.
    public void setMode(InputMode mode) {
        possibilitiesHandler.clear();
        changeMode(mode);
        clearSeparator();
    }

    /// Changes the mode, and warns the listeners.
    private void changeMode(InputMode newMode) {
        if (mode != newMode) {
            mode = newMode;
            for (MDCModelEditionListener listener : new ArrayList<>(listeners)) {
                listener.inputModeChanged(newMode);
            }
        }
    }

    /// After a caret move (not while typing), adapts the mode to the text
    /// around the caret: typing after a word continues the word, typing after
    /// hieroglyphs types hieroglyphs.
    ///
    /// The element before the caret is used; at the beginning of a line, the
    /// element after the caret. Comments, tabulations, etc. don't change the
    /// mode.
    private void followCaret() {
        EditingScope scope = scope();
        MDCPosition p = caret.getLocalInsertPosition();
        TopItem reference = scope.getElementBefore(p);
        if (reference == null || reference.isBreak()) {
            reference = scope.getElementAfter(p);
        }
        if (reference instanceof AlphabeticCharacter c) {
            InputMode.forScript(c.getScript()).ifPresent(this::changeMode);
        } else if (reference instanceof Cadrat) {
            changeMode(InputMode.HIEROGLYPHS);
        } else if (reference instanceof Superscript) {
            changeMode(InputMode.LINE_NUMBER);
        }
    }

    /**
     * Returns a <em>copy</em> of the selection, as a new HieroglyphicText.
     *
     * @return
     */

    public HieroglyphicText getSelectionAsHieroglyphicText() {
        HieroglyphicText topItemList = new HieroglyphicText();
        if (caret.hasMark()) {
            List<TopItem> l = scope().getTopItemsBetween(
                    caret.getLocalMinPosition(), caret.getLocalMaxPosition());
            topItemList.addAll(l);
        }
        return topItemList;
    }

    /**
     * Group the elements in the selection in an hbox (and then in a cadrat).
     * Can only work if it's possible to fetch innergroups from these elements.
     *
     * @return true if success.
     */

    public boolean groupHorizontally() {
        boolean result;
        HorizontalGrouper f = new HorizontalGrouper();
        List<TopItem> elts = getSelection();

        Cadrat c = f.buildCadrat(elts);
        if (c != null) {
            scope().replaceElement(caret.getLocalMinPosition(),
                    caret.getLocalMaxPosition(), c);
            result = true;
        } else {
            result = false;
        }
        clearSeparator();
        clearMark();
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

    public boolean groupVertically() {
        boolean result;
        VerticalGrouper v = new VerticalGrouper();
        List<TopItem> elts = getSelection();
        if (elts.isEmpty()) {
            return false;
        }
        Cadrat c = v.buildCadrat(elts);
        if (c != null) {
            scope().replaceElement(caret.getLocalMinPosition(),
                    caret.getLocalMaxPosition(), c);
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

    @SuppressWarnings("unchecked")
    public void insertElements(List elements) {
        possibilitiesHandler.clear();
        scope().insertElementsAt(caret.getLocalInsertPosition(),
                (List<TopItem>) elements);
    }

    /**
     *
     */

    public void insertHalfSpace() {
        possibilitiesHandler.clear();
        Hieroglyph h = new Hieroglyph(SymbolCodes.HALFSPACE);
        scope().insertElementAt(caret.getLocalInsertPosition(),
                h.buildTopItem());
    }

    /**
     * @param mdcText
     */

    public void insertMDC(String mdcText) {
        try {
            possibilitiesHandler.clear();
            scope().insertMDCText(caret.getLocalInsertPosition(), mdcText);
        } catch (MDCSyntaxError e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */

    public void insertNewLine() {
        possibilitiesHandler.clear();
        acceptSeparator(' ');
        scope().insertElementAt(caret.getLocalInsertPosition(),
                new LineBreak().buildTopItem());
    }

    /**
     *
     */

    public void insertPageBreak() {
        possibilitiesHandler.clear();
        acceptSeparator(' ');
        scope().insertElementAt(caret.getLocalInsertPosition(),
                new PageBreak().buildTopItem());
    }

    /**
     *
     */

    public void insertSpace() {
        possibilitiesHandler.clear();
        Hieroglyph h = new Hieroglyph(SymbolCodes.FULLSPACE);
        scope().insertElementAt(caret.getLocalInsertPosition(),
                h.buildTopItem());
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
     *
     * @return the readWrite
     */

    public boolean isReadWrite() {
        return readWrite;
    }

    /**
     * Choose between readonly and readwrite object.
     *
     * @param readWrite the readWrite to set
     */

    public void setReadWrite(boolean readWrite) {
        this.readWrite = readWrite;
    }

    /**
     * @param key
     * @return true if key corresponds to a MDC separator.
     */

    public boolean isSeparatorChar(char key) {
        return (key == ' ') || (key == ':') || (key == '*') || (key == '&');
    }

    /**
     * Send a char to the current editing process. The char will be processed
     * according to the current state of the workflow.
     *
     * @param key
     */

    public void keyTyped(char key) {
        if (mode == InputMode.HIEROGLYPHS) {
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

    public void ligatureHieroglyphWithGroup() {
        doComplexLigature(0);
    }

    /**
     * Insert a group in a following hieroglyph.
     */

    public void ligatureGroupWithHieroglyph() {
        doComplexLigature(-1);

    }

    /**
     * Ligature all the elements in the selection.
     *
     * @return true if success
     */

    public boolean ligatureElements() {
        List<TopItem> elts = getSelection();
        if (getSelection().isEmpty()) {
            return false;
        }
        MDCPosition minPos = caret.getLocalMinPosition();
        MDCPosition maxPos = caret.getLocalMaxPosition();
        List<TopItem> toInsert = buildLigature(elts);
        scope().replaceElement(minPos, maxPos, toInsert);
        clearMark();
        clearSeparator();
        return true;
    }

    /// Groups two items into an insertion (`%insert`), choosing the best way automatically.
    ///
    /// The two items are the selection, if any (it must contain exactly two
    /// items), or else the two items before the cursor. All directions, and both
    /// choices of the main group, are tried, and the one where the inserted group
    /// is the largest is kept (see [InsertionRanker]). The user can then choose
    /// another way with [#prepareChoiceOfInsertion(InsertionRanker)].
    ///
    /// @param ranker computes the possible insertions.
    /// @return true if an insertion was built.
    public boolean groupAsInsertion(InsertionRanker ranker) {
        possibilitiesHandler.clear();
        clearSeparator();
        EditingScope scope = scope();
        MDCPosition p1, p2;
        if (caret.hasSelection()) {
            p1 = caret.getLocalMinPosition();
            p2 = caret.getLocalMaxPosition();
        } else {
            p2 = caret.getLocalInsertPosition();
            if (p2.getLastIndex() < 2) {
                return false;
            }
            p1 = p2.getPreviousPosition(2);
        }
        List<TopItem> items = scope.getTopItemsBetween(p1, p2);
        if (items.size() != 2) {
            return false;
        }
        SubCadrat first = toSubCadrat(items.get(0));
        SubCadrat second = toSubCadrat(items.get(1));
        if (first == null || second == null) {
            return false;
        }
        List<InsertionChoice> choices = buildInsertionChoices(ranker, first, second, true, 0, 0);
        boolean result = scope.replaceElement(p1, p2, choices.get(0).insert().buildTopItem());
        clearMark();
        return result;
    }

    /// Is the item before the cursor an insertion, whose layout could be chosen
    /// with [#prepareChoiceOfInsertion(InsertionRanker)]?
    public boolean hasInsertionBeforeCaret() {
        return !caret.hasSelection() && getInsertionBeforeCaret() != null;
    }

    /// Prepares the choice of another way of inserting, for the insertion before the cursor.
    ///
    /// The choices are all the directions with the current main group, and the
    /// directions where the other group fits, if it is used as the main group.
    /// They are available through [#getInsertionChoices()], and
    /// [#selectInsertion(int)] puts one of them in the text. The direction and the
    /// enlargement of the frame can also be changed directly, with
    /// [#selectInsertionDirection(InsertionDirection)] and
    /// [#changeInsertionEnlargement(int, int)].
    ///
    /// @param ranker computes the possible insertions.
    /// @return true if there is an insertion before the cursor.
    public boolean prepareChoiceOfInsertion(InsertionRanker ranker) {
        possibilitiesHandler.clear();
        clearSeparator();
        insertionChoices = Collections.emptyList();
        selectedInsertionIndex = -1;
        Insert current = getInsertionBeforeCaret();
        if (current == null) {
            return false;
        }
        insertionRanker = ranker;
        insertionOriginal = current.deepCopy();
        insertionDw = current.getDw();
        insertionDh = current.getDh();
        insertionReplaced = false;
        insertionCaretPosition = caret.getLocalInsertPosition();
        insertionChoices = buildInsertionChoices(ranker, current.getMain(), current.getInserted(),
                current.getTarget() == Insert.TARGET_FIRST, insertionDw, insertionDh);
        // The choice may differ from the text by an enlargement which has no effect.
        selectedInsertionIndex = indexOfInsertionChoice(current.getMain(), current.getInserted(),
                current.getTarget(), current.getDirection());
        return selectedInsertionIndex >= 0;
    }

    /// The choices prepared by [#prepareChoiceOfInsertion(InsertionRanker)], best first.
    ///
    /// @return an immutable list.
    public List<InsertionChoice> getInsertionChoices() {
        return insertionChoices;
    }

    /// Index, in [#getInsertionChoices()], of the insertion currently in the text.
    public int getSelectedInsertionIndex() {
        return selectedInsertionIndex;
    }

    /// The insertion currently in the text, among [#getInsertionChoices()], or null if there are no choices.
    public InsertionChoice getSelectedInsertion() {
        return selectedInsertionIndex < 0 ? null : insertionChoices.get(selectedInsertionIndex);
    }

    /// The choice with the same groups as the current insertion, and another direction.
    ///
    /// @return the choice, or null if there are no choices.
    public InsertionChoice getInsertionChoice(InsertionDirection direction) {
        InsertionChoice current = getSelectedInsertion();
        if (current == null) {
            return null;
        }
        Insert insert = current.insert();
        int index = indexOfInsertionChoice(insert.getMain(), insert.getInserted(), insert.getTarget(), direction);
        return index < 0 ? null : insertionChoices.get(index);
    }

    /// The horizontal enlargement of the frame used for the choices, in percent.
    ///
    /// For directions where it has no effect, the choices have no enlargement.
    public int getInsertionDw() {
        return insertionDw;
    }

    /// The vertical enlargement of the frame used for the choices, in percent.
    ///
    /// For directions where it has no effect, the choices have no enlargement.
    public int getInsertionDh() {
        return insertionDh;
    }

    /// Replaces the insertion before the cursor by one of the choices.
    ///
    /// Successive choices replace each other in the undo history: only the last
    /// one remains, and coming back to the original insertion leaves no trace.
    ///
    /// @param index index in [#getInsertionChoices()].
    public void selectInsertion(int index) {
        if (index < 0 || index >= insertionChoices.size() || index == selectedInsertionIndex) {
            return;
        }
        applyInsertion(insertionChoices.get(index).insert());
        selectedInsertionIndex = index;
    }

    /// Changes the direction of the insertion before the cursor, keeping its groups.
    public void selectInsertionDirection(InsertionDirection direction) {
        InsertionChoice current = getSelectedInsertion();
        if (current != null) {
            Insert insert = current.insert();
            selectInsertion(indexOfInsertionChoice(insert.getMain(), insert.getInserted(), insert.getTarget(),
                    direction));
        }
    }

    /// Changes the enlargement of the frame of the main group.
    ///
    /// The choices are computed again, and the insertion before the cursor is
    /// replaced by the one with the same groups and direction. Enlargements are
    /// between 0 and [#MAX_INSERTION_ENLARGEMENT].
    ///
    /// @param deltaDw change of the horizontal enlargement, in percent.
    /// @param deltaDh change of the vertical enlargement, in percent.
    public void changeInsertionEnlargement(int deltaDw, int deltaDh) {
        InsertionChoice current = getSelectedInsertion();
        if (current == null) {
            return;
        }
        insertionDw = Math.clamp(insertionDw + deltaDw, 0, MAX_INSERTION_ENLARGEMENT);
        insertionDh = Math.clamp(insertionDh + deltaDh, 0, MAX_INSERTION_ENLARGEMENT);
        Insert insert = current.insert();
        insertionChoices = buildInsertionChoices(insertionRanker, insert.getMain(), insert.getInserted(),
                insert.getTarget() == Insert.TARGET_FIRST, insertionDw, insertionDh);
        int index = indexOfInsertionChoice(insert.getMain(), insert.getInserted(), insert.getTarget(),
                insert.getDirection());
        selectedInsertionIndex = -1;
        selectInsertion(index);
    }

    /// Puts back the insertion which was in the text when the choices were prepared.
    public void cancelChoiceOfInsertion() {
        if (insertionOriginal != null) {
            applyInsertion(insertionOriginal);
        }
        insertionChoices = Collections.emptyList();
        selectedInsertionIndex = -1;
    }

    /// Replaces the insertion before the cursor, undoing the previous replacement if any.
    private void applyInsertion(Insert insert) {
        if (insertionReplaced) {
            hieroglyphicTextModel.undo();
            caret.setLocalInsertPosition(insertionCaretPosition);
            insertionReplaced = false;
        }
        if (!insert.equalsIgnoreId(insertionOriginal)) {
            insertionReplaced = scope().replaceElementBefore(insertionCaretPosition,
                    insert.deepCopy().buildTopItem());
            caret.setLocalInsertPosition(insertionCaretPosition);
        }
    }

    /// Index of the choice with the given groups, target and direction (whatever its enlargement), or -1.
    private int indexOfInsertionChoice(SubCadrat main, SubCadrat inserted, int target,
            InsertionDirection direction) {
        for (int i = 0; i < insertionChoices.size(); i++) {
            Insert candidate = insertionChoices.get(i).insert();
            if (candidate.getDirection() == direction && candidate.getTarget() == target
                    && candidate.getMain().equalsIgnoreId(main)
                    && candidate.getInserted().equalsIgnoreId(inserted)) {
                return i;
            }
        }
        return -1;
    }

    /// The insertion which is the only content of the item before the cursor, or null.
    private Insert getInsertionBeforeCaret() {
        TopItem item = scope().getElementBefore(caret.getLocalInsertPosition());
        if (item == null) {
            return null;
        }
        InnerGroupBuilder builder = new InnerGroupBuilder();
        builder.buildHorizontalElement(List.of(item));
        return builder.getGroup() instanceof Insert insert ? insert : null;
    }

    /// The ranked ways of inserting one group into the other.
    ///
    /// Keeps all the candidates where `main` is the main group, and those where
    /// `other` is the main group and something fits. The enlargements are only
    /// kept for the directions where they have an effect.
    ///
    /// The groups stay in reading order in the choices: choosing the other group
    /// as the main one changes the `target`, not the order of the arguments.
    ///
    /// @param mainIsFirst true if `main` comes first in reading order, false if it comes second.
    private static List<InsertionChoice> buildInsertionChoices(InsertionRanker ranker, SubCadrat main,
            SubCadrat other, boolean mainIsFirst, int dw, int dh) {
        List<InsertionChoice> result = new ArrayList<>();
        SubCadrat first = mainIsFirst ? main : other;
        SubCadrat second = mainIsFirst ? other : main;
        for (InsertionRanker.Candidate c : ranker.rank(main, other, dw, dh)) {
            if (!c.swapped() || c.fits()) {
                boolean firstIsTarget = mainIsFirst != c.swapped();
                InsertionDirection direction = c.direction();
                Insert insert = new Insert(direction, first.deepCopy(), second.deepCopy(),
                        firstIsTarget ? Insert.TARGET_FIRST : Insert.TARGET_SECOND,
                        direction.usesDw() ? dw : 0, direction.usesDh() ? dh : 0);
                result.add(new InsertionChoice(insert, c.scale(), c.fits()));
            }
        }
        return Collections.unmodifiableList(result);
    }

    /// A sub-cadrat with the content of an item, for use in an insertion.
    ///
    /// @return a sub-cadrat which is not part of the text, or null if the item can't be grouped.
    private static SubCadrat toSubCadrat(TopItem item) {
        InnerGroupBuilder builder = new InnerGroupBuilder();
        builder.buildHorizontalElement(List.of(item));
        // Avoid ((n:t)) when the item is a single sub-cadrat.
        if (builder.getGroup() instanceof SubCadrat subCadrat) {
            return subCadrat.deepCopy();
        }
        BasicItemList list = new BasicItemListGrouper().extractBasicItemList(List.of(item));
        return list.getNumberOfChildren() == 0 ? null : new SubCadrat(list);
    }

    public void redo() {
        hieroglyphicTextModel.redo();
    }

    /**
     * Paint a zone in red if b is true, black otherwise. Will become private in
     * favour of paintZoneInRed() and paintZoneInBlack ?
     *
     * @param b
     */

    public void redZone(final boolean b) {
        possibilitiesHandler.clear();
        applyGroupLevelModifications((TopItem t) -> {
            if (t instanceof Cadrat) {
                Cadrat c = (Cadrat) t;
                c.setRed(b);

            }
            return Collections.singletonList(t);
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
     * Remove the element in front of the cursor.
     */

    public void removeTopItem() {
        possibilitiesHandler.clear();
        MDCPosition position = caret.getLocalInsertPosition();
        scope().removeElements(position.getPreviousPosition(1), position);
        clearSeparator();
    }

    /**
     * Changes the size of the current sign
     *
     * @param size
     */

    public void resizeSign(final int size) {
        applySignLevelModifications(
                hieroglyph -> hieroglyph.setRelativeSize(size));
    }

    /**
     *
     */

    public void reverseSign() {
        applySignLevelModifications(
                hieroglyph -> hieroglyph.setReversed(!hieroglyph.isReversed()));
    }

    /**
     *
     */

    public void selectAll() {
        possibilitiesHandler.clear();
        caret.setInsertPosition(hieroglyphicTextModel.buildFirstPosition());
        caret.setMarkPosition(hieroglyphicTextModel.getLastPosition());
        clearSeparator();
    }

    public void selectCurrentLine() {
        possibilitiesHandler.clear();
        EditingScope scope = scope();
        List<MDCPosition> limits = scope.getLineLimitsAround(caret.getLocalInsertPosition());
        caret.setInsertPosition(scope.toTextPosition(limits.get(0)));
        caret.setMarkPosition(scope.toTextPosition(limits.get(1)));
    }

    public void selectCurrentPage() {
        possibilitiesHandler.clear();
        EditingScope scope = scope();
        List<MDCPosition> limits = scope.getPageLimitsAround(caret.getLocalInsertPosition());
        caret.setInsertPosition(scope.toTextPosition(limits.get(0)));
        caret.setMarkPosition(scope.toTextPosition(limits.get(1)));
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

    public boolean setCurrentLineTo(String text) {
        possibilitiesHandler.clear();
        boolean success = true;
        try {
            List<MDCPosition> limits = getLineLimits();
            hieroglyphicTextModel
                    .replaceWithMDCText(limits.get(0), limits.get(1), text);
            caret.setInsertPosition(limits.get(0));
            clearSeparator();
        } catch (MDCSyntaxError e) {
            success = false;
        }
        return success;
    }

    /**
     * @param position
     */

    public void setCursor(MDCPosition position) {
        if (position != null) {
            possibilitiesHandler.clear();
            caret.setInsertPosition(position);
            clearSeparator();
            followCaret();
        }
    }

    /**
     * Set mark at position.
     *
     * @param position
     */

    public void setMark(MDCPosition position) {
        if (position != null) {
            possibilitiesHandler.clear();
            caret.setMark(new MDCMark(caret.getModel(), position));
            clearSeparator();
        }
    }

    /**
     *
     */

    public void setMarkToCursor() {
        possibilitiesHandler.clear();
        caret.setMarkPosition(caret.getInsertExactPosition());
        clearSeparator();
    }

    /**
     *
     */

    public void setSignIsAtSentenceEnd() {
        applySignLevelModifications(hieroglyph -> hieroglyph.setEndingCode(WordEnding.SENTENCE_END));
    }

    /**
     *
     */

    public void setSignIsAtWordEnd() {
        applySignLevelModifications(hieroglyph -> hieroglyph.setEndingCode(WordEnding.WORD_END));
    }

    /**
     *
     */

    public void setSignIsInsideWord() {
        applySignLevelModifications(hieroglyph -> hieroglyph.setEndingCode(WordEnding.NONE));
    }

    /**
     * shade or unshade selected zone, depending the value of <code>shade</code>
     * . This method will be made private in favour of {@link #shadeZone()} and
     * {@link #unshadeZone()}.
     *
     * @param shade
     */

    public void shadeZone(final boolean shade) {
        clearSeparator();
        possibilitiesHandler.clear();
        applyGroupLevelModifications((TopItem t) -> {
            t.setShaded(shade);
            return Collections.singletonList(t);
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
     *
     */

    public void toggleGrammar() {
        applySignLevelModifications(hieroglyph -> hieroglyph.setGrammar(!hieroglyph.isGrammar()));
    }

    public void toggleIgnoredSign() {
        applySignLevelModifications(hieroglyph -> hieroglyph.getModifiers().setBoolean("i",
                !hieroglyph.getModifiers().getBoolean("i")));
    }

    /**
     *
     */

    public void toggleRedSign() {
        applySignLevelModifications(hieroglyph -> hieroglyph.getModifiers().setBoolean("red",
                !hieroglyph.getModifiers().getBoolean("red")));
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
        applySignLevelModifications(hieroglyph -> hieroglyph.getModifiers().setInteger("shading",
                Integer.parseInt(code.toString())));
    }

    public void toggleWideSign() {
        applySignLevelModifications(hieroglyph -> hieroglyph.getModifiers().setBoolean("l",
                !hieroglyph.getModifiers().getBoolean("l")));
    }

    public void undo() {
        hieroglyphicTextModel.undo();
    }

    /**
     * Process a notification of text change from the text model.
     * <p>
     * Update our data, and notify our own observers.
     * 
     * @param e
     */
    private void textChanged(TextEvent e) {

        switch (e) {
            case NewTextEvent _tmp_ -> {
                // A new Text has been loaded.
                // We set up the caret, but this should be placed in the caret
                // itself !
                caret.changeModel(hieroglyphicTextModel.getHieroglyphicText());

                for (MDCModelEditionListener l : listeners) {
                    l.textChanged();
                }
                clearSeparator();
            }
            case TextOperationEvent(ModelOperation operation) -> {
                for (MDCModelEditionListener l : listeners) {
                    l.textEdited(operation);
                }
            }
        }        
    }

    public HieroglyphicTextModel getHieroglyphicTextModel() {
        return hieroglyphicTextModel;
    }

    public final void setHieroglyphicTextModel(
            HieroglyphicTextModel newHieroglyphicTextModel) {

        if (this.hieroglyphicTextModel == newHieroglyphicTextModel) {
            return;
        }

        if (hieroglyphicTextModel != null) {
            hieroglyphicTextModel.removeListener(textChangeListener);
            caret.removeCaretChangeListener(this);
        }
        this.hieroglyphicTextModel = newHieroglyphicTextModel;
        hieroglyphicTextModel.addListener(textChangeListener);
        caret = new MDCCaret(hieroglyphicTextModel.getHieroglyphicText());
        setCursor(hieroglyphicTextModel.buildFirstPosition());
        caret.addCaretChangeListener(this);
        possibilitiesHandler.clear();
        textChanged(new NewTextEvent()); // We have changed the whole text.
    }

    /**
     * Add an element specified by an external layer.
     *
     * @param element
     */
    public void insertElement(ModelElement element) {
        possibilitiesHandler.clear();
        scope().insertElementAt(caret.getLocalInsertPosition(),
                element.buildTopItem());
    }

    /// Inserts plain text (e.g. pasted from another application) at the
    /// caret, as alphabetic characters in the script of the current mode
    /// (latin in hieroglyphic mode). Line breaks in the text become line
    /// breaks in the document.
    ///
    /// @param text the text.
    public void insertPlainText(String text) {
        possibilitiesHandler.clear();
        char scriptCode = mode.getScript().orElse(ScriptCode.LATIN).getMdcCode();
        List<TopItem> items = new ArrayList<>();
        String[] lines = text.split("\r\n|\n|\r", -1);
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) {
                items.add(new LineBreak());
            }
            items.addAll(AlphabeticCharacter.fromMdcText(scriptCode, lines[i]));
        }
        if (!items.isEmpty()) {
            scope().insertElementsAt(caret.getLocalInsertPosition(), items);
        }
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

    public void insertLineNumber(String line) {
        possibilitiesHandler.clear();
        Superscript superscript = new Superscript(line);
        scope().insertElementAt(caret.getLocalInsertPosition(), superscript);
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
     * @param jseshRenderContext the render context to use for building the group.
     * @param jseshTechRenderContext the technical render context to use for building the group.
     * 
     * @return Returns a <em>working copy</em> of the created group, or null if
     *         nothing was created.
     */

    public AbsoluteGroup buildAbsoluteGroup(
            JSeshRenderContext jseshRenderContext,
			JSeshTechRenderContext jseshTechRenderContext
    ) {
        AbsoluteGroupBuilder groupBuilder = new AbsoluteGroupBuilder();
        AbsoluteGroup result = null;
        // ensure there is a selection if possible.
        if (!caret.hasSelection() && caret.getLocalInsertPosition().hasPrevious()) {
            caret.setLocalMarkPosition(caret.getLocalInsertPosition().getPreviousPosition(1));
        }
        if (caret.hasSelection()) {
            List<TopItem> elts = getSelection();
			AbsoluteGroup g = groupBuilder.createAbsoluteGroupFrom(
                    elts, jseshRenderContext, jseshTechRenderContext);
            result = g;
        }
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------
    // Private methods
    // --------------------------------------------------------------------------------------------------------------

    /// The text orientation in force at the insertion point: the one of the
    /// innermost enclosing zone which sets one, or `fallback`.
    ///
    /// @param fallback the orientation of the text outside any zone setting it.
    public TextOrientation getTextOrientationAtCaret(TextOrientation fallback) {
        for (Zone zone : enclosingZones()) {
            if (zone.getParameters().orientation() != null) {
                return zone.getParameters().orientation();
            }
        }
        return fallback;
    }

    /// The text direction in force at the insertion point: the one of the
    /// innermost enclosing zone which sets one, or `fallback`.
    ///
    /// @param fallback the direction of the text outside any zone setting it.
    public TextDirection getTextDirectionAtCaret(TextDirection fallback) {
        for (Zone zone : enclosingZones()) {
            if (zone.getParameters().direction() != null) {
                return zone.getParameters().direction();
            }
        }
        return fallback;
    }

    /// The zones around the insertion point, innermost first.
    private List<Zone> enclosingZones() {
        return enclosingZones(caret.getInsertExactPosition());
    }

    /// The scope of the content of the innermost zone around the insertion
    /// point, which must be in a zone.
    private TextScope innermostZoneContentScope() {
        HieroglyphicText text = caret.getModel();
        TextScope contentScope = TextScope.containing(text, caret.getInsertExactPosition());
        while (!(contentScope.resolve(text) instanceof ZoneContent)) {
            contentScope = contentScope.enclosing(text);
        }
        return contentScope;
    }

    /// The zones around a position, innermost first.
    private List<Zone> enclosingZones(MDCPosition position) {
        HieroglyphicText text = caret.getModel();
        List<Zone> zones = new ArrayList<>();
        TextScope textScope = TextScope.containing(text, position);
        while (!textScope.isRoot()) {
            if (textScope.resolve(text) instanceof ZoneContent content
                    && content.getParent() instanceof Zone zone) {
                zones.add(zone);
            }
            textScope = textScope.enclosing(text);
        }
        return zones;
    }

    /// The text direction in force at a position.
    private TextDirection directionAt(MDCPosition position, TextDirection fallback) {
        for (Zone zone : enclosingZones(position)) {
            if (zone.getParameters().direction() != null) {
                return zone.getParameters().direction();
            }
        }
        return fallback;
    }

    /// The position the cursor must go to, when a move along the text crosses
    /// the boundary of a zone whose direction differs from the one outside.
    ///
    /// Such a zone is a visual box: the arrow keys follow the *visual*
    /// direction, in the text which surrounds the zone. Leaving the zone through
    /// its right edge puts the cursor after it (if the text around is
    /// left-to-right), and entering it from the right lands on its right edge,
    /// whatever the direction of its own content.
    ///
    /// @param from the current position.
    /// @param normal the position a move by one stop along the text (in the
    ///        direction of the text at `from`) leads to.
    /// @param toRight true if the key moves to the right.
    /// @param forward true if the move goes forward in the text order.
    /// @param fallback the direction of the text outside any zone setting one.
    /// @return the position to go to, or empty if `normal` is the right one.
    private Optional<MDCPosition> zoneBoundaryTarget(MDCPosition from, MDCPosition normal, boolean toRight,
            boolean forward, TextDirection fallback) {
        HieroglyphicText text = caret.getModel();
        TextScope source = TextScope.containing(text, from);
        TextScope destination = TextScope.containing(text, normal);
        if (source.equals(destination)) {
            return Optional.empty();
        }
        if (source.resolve(text) instanceof ZoneContent && !source.contains(normal)) {
            // Leaving a zone.
            TextScope outer = source.enclosing(text);
            int index = outer.localIndexOf(MDCPosition.of(source.getPath()));
            boolean innerLtr = directionAt(from, fallback).isLeftToRight();
            boolean outerLtr = directionAt(outer.positionAt(index), fallback).isLeftToRight();
            if (innerLtr == outerLtr) {
                return Optional.empty();
            }
            boolean outerForward = toRight == outerLtr;
            return Optional.of(outer.positionAt(outerForward ? index + 1 : index));
        }
        if (!destination.contains(from)) {
            // Entering a zone.
            TextScope entered = destination;
            while (!entered.enclosing(text).contains(from)) {
                entered = entered.enclosing(text);
            }
            if (entered.resolve(text) instanceof ZoneContent content) {
                boolean innerLtr = directionAt(entered.positionAt(0), fallback).isLeftToRight();
                boolean outerLtr = directionAt(from, fallback).isLeftToRight();
                if (innerLtr != outerLtr) {
                    return Optional.of(entered.positionAt(forward ? content.getNumberOfChildren() : 0));
                }
            }
        }
        return Optional.empty();
    }

    /// The scope where the editing commands work: the text itself, or the
    /// content of the cartouche where the cursor stands (see
    /// [MDCCaret#getScope()]). Positions in it are the caret's *local*
    /// positions ([MDCCaret#getLocalInsertPosition()]...).
    private EditingScope scope() {
        return new EditingScope(hieroglyphicTextModel, caret.getScope());
    }

    /// The start of the next (or previous) line, from the insertion point.
    ///
    /// Lines are those of the innermost text with lines around the insertion
    /// point: the content of a zone, or the text itself (a cartouche is a single
    /// line). From the last (or first) line of a zone, the move goes on in the
    /// enclosing text, from the zone.
    ///
    /// @param down true to move down, false to move up.
    /// @return the new insertion position.
    private MDCPosition verticalPosition(boolean down) {
        HieroglyphicText text = caret.getModel();
        MDCPosition position = caret.getInsertExactPosition();
        TextScope textScope = TextScope.containing(text, position);
        while (true) {
            while (!textScope.isRoot() && !(textScope.resolve(text) instanceof ZoneContent)) {
                textScope = textScope.enclosing(text);
            }
            EditingScope scope = new EditingScope(hieroglyphicTextModel, textScope);
            MDCPosition local = new MDCPosition(textScope.localIndexOf(position));
            Optional<MDCPosition> result = down ? scope.getDownPosition(local) : scope.getUpPosition(local);
            if (result.isPresent()) {
                return scope.toTextPosition(result.get());
            }
            if (textScope.isRoot()) {
                return scope.toTextPosition(down ? scope.getLastPosition() : scope.getLineFirstPosition(local));
            }
            // First or last line of a zone: go on from the zone, in the
            // enclosing text.
            position = MDCPosition.of(textScope.getPath());
            textScope = textScope.enclosing(text);
        }
    }

    /**
     * Returns a <em>copy</em> of the list of selected elements, or null if none
     * is selected.
     *
     * @return a copy of the list of TopItems, empty if there is no selection.
     */
    private List<TopItem> getSelection() {
        // possibilitiesHandler.clear(); WHY WAS THAT ??
        List<TopItem> result = null;
        if (caret.hasMark()) {
            result = scope().getTopItemsBetween(caret.getLocalMinPosition(),
                    caret.getLocalMaxPosition());
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Apply a modification to the whole selection, or to the previous item if
     * there is no previous selection.
     *
     * @param topItemModifier : the modification to apply.
     */

    private void applyGroupLevelModifications(TopItemModifier topItemModifier) {
        List<TopItem> modified = new ArrayList<>();
        EditingScope scope = scope();
        MDCPosition p1, p2;
        if (caret.hasSelection()) {
            p1 = caret.getLocalMinPosition();
            p2 = caret.getLocalMaxPosition();
        } else if (caret.getLocalInsertPosition().hasPrevious()) {
            p2 = caret.getLocalInsertPosition();
            p1 = p2.getPreviousPosition(1);
        } else {
            return; // DO NOTHING.
        }
        for (TopItem currentItem : scope.getTopItemsBetween(p1, p2)) {
            modified.addAll(topItemModifier.modifyTopItem(currentItem));
        }
        // NOT SO SURE THAT THIS IS RELEVANT...
        if (modified.size() > 0) {
            scope.replaceElement(p1,
                    p2, modified);
        }
    }

    /**
     * Perform a modification either on the last sign, or on a selection.
     * <p>
     * Handles the UNDO/REDO framework.
     * </p>
     *
     * @param hieroglyphConsumer
     */
    private void applySignLevelModifications(Consumer<Hieroglyph> hieroglyphConsumer) {
        possibilitiesHandler.clear();
        if (!caret.hasSelection()) {
            modifyLastSign(hieroglyphConsumer);
        } else {
            clearSeparator();
            EditingScope scope = scope();
            MDCPosition min = caret.getLocalMinPosition();
            MDCPosition max = caret.getLocalMaxPosition();
            List<TopItem> newItems = scope.getTopItemsBetween(min, max);
            DeepHieroglyphsProcessor processor = new DeepHieroglyphsProcessor();
            processor.processItems(newItems, hieroglyphConsumer);
            scope.replaceElement(min, max, newItems);
        }
    }

    /**
     * Replaces the last sign (and only the last sign ).
     *
     * @param hieroglyphConsumer
     */
    private void modifyLastSign(Consumer<Hieroglyph> hieroglyphConsumer) {
        if (getCurrentItem() != null) {
            TopItem item = getCurrentItem().deepCopy();
            if (item != null) {
                Hieroglyph h = getLastHieroglyph(item);
                if (h != null) {
                    hieroglyphConsumer.accept(h);
                }
                scope().replaceElementBefore(
                        caret.getLocalInsertPosition(), item);
            }
        }
    }

    /**
     * Method called when a regular letter is added to the text.
     *
     * @param key
     */

    private void addAlphabeticChar(char key) {
        possibilitiesHandler.clear();
        if (key == 8) {
            doBackspace();
        } else {
            insertOrExtendAlphabeticText(key);
        }
        clearSeparator();
    }

    /**
     * Inserts key at the caret: a new alphabetic character, or, in line number
     * mode, a letter added to the Superscript before the caret.
     */
    private void insertOrExtendAlphabeticText(char key) {
        EditingScope scope = scope();
        MDCPosition position = caret.getLocalInsertPosition();
        TopItem elementBefore = scope.getElementBefore(position);

        if (mode == InputMode.LINE_NUMBER) {
            String toAdd = Character.toString(key);
            if (elementBefore instanceof Superscript) {
                Superscript txt = ((Superscript) elementBefore).deepCopy();
                txt.setText(txt.getText() + toAdd);
                scope.replaceElementBefore(position, txt);
            } else {
                scope.insertElementAt(position,
                        new Superscript(toAdd).buildTopItem());
            }
        } else if (mode == InputMode.TRANSLITERATION && key == '^') {
            // MdC notation for "next letter is uppercase".
            changeMode(InputMode.UPPERCASE_TRANSLITERATION);
        } else {
            char scriptCode = mode.getScript().orElseThrow().getMdcCode();
            String mdcText = Character.toString(key);
            if (mode == InputMode.UPPERCASE_TRANSLITERATION) {
                // Most of the time, only one uppercase char will be needed !!!
                changeMode(InputMode.TRANSLITERATION);
                mdcText = "^" + key;
            }
            // (for transliteration, fromMdcText also handles the Utrecht font
            // conventions, e.g. "!" for uppercase h).
            List<TopItem> characters = new ArrayList<>(AlphabeticCharacter.fromMdcText(scriptCode, mdcText));
            if (!characters.isEmpty()) {
                scope.insertTypedElementsAt(position, characters);
            }
        }
    }

    /**
     * Returns the last hieroglyph element in the topitem before index mark, or
     * null.
     * <p>
     * This modifies the element.
     *
     * @param t the topitem from which the hieroglyph is taken.
     * @return the last hieroglyph element in the topitem before index mark, or
     *         null.
     */

    private Hieroglyph getLastHieroglyph(TopItem t) {
        Hieroglyph result = null;
        if (t != null) {
            LastHieroglyphSelector selector = new LastHieroglyphSelector();
            result = selector.findLastHieroglyph(t);
        }
        return result;
    }

    /// Returns the limits of the current line of the text, including the
    /// final -! or -!! if any. These are top-level positions: inside a
    /// cartouche, the line is the line of the text which contains it.
    ///
    /// @return the limits of the current line.
    private List<MDCPosition> getLineLimits() {
        HieroglyphicText text = caret.getModel();
        MDCPosition first = text.getLineFirstPosition(caret.getInsertPosition());
        MDCPosition second = text.getLineLastPosition(caret.getInsertPosition());
        if (text.hasNext(second)) {
            second = text.getNextPosition(second, 1);
        }
        return List.of(first, second);
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
        ArrayList<TopItem> result = new ArrayList<>();
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

    private void notifyCodeChangeListeners() {
        for (int i = 0; i < listeners.size(); i++) {
            MDCModelEditionListener listener = listeners.get(i);
            listener.codeChanged(currentCode);
        }

    }

    private void notifySeparatorChangeListeners() {
        for (int i = 0; i < listeners.size(); i++) {
            MDCModelEditionListener listener = listeners.get(i);
            listener.separatorChanged();
        }
    }

    /**
     * Remove a single letter from the line number (superscript) in front of
     * the cursor. Erase it if it becomes empty.
     */
    private void removeSingleLetter() {
        EditingScope scope = scope();
        if (scope.getElementBefore(caret.getLocalInsertPosition()) instanceof Superscript txt) {
            // If txt is or would be empty, suppress it
            if (txt.getText().length() <= 1) {
                removeTopItem();
            } else {
                Superscript newText = txt.deepCopy();
                // Suppress only one char.
                newText.setText(txt.getText().substring(0,
                        txt.getText().length() - 1));
                scope.replaceElementBefore(
                        caret.getLocalInsertPosition(), newText);
            }
        }
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
                String mdc = s.getCode();
                try {
					items = MdcTextHelper.parse(mdc).asList();
				} catch (MDCSyntaxError e) {
                    items = Collections.singletonList(buildItemForSignCode("erroringlossary"));
					e.printStackTrace();
				}                
            } else {
                items = Collections.singletonList(buildItemForSignCode(s
                        .getCode()));
            }
        } else {
            items = Collections.singletonList(buildItemForSignCode(currentCode
                    .toString()));
        }
        // Inside a quadrat, the container tells how the sign is combined with its neighbours.
        innerRetryPosition = null;
        boolean singleSign = !possibilitiesHandler.hasPossibilities()
                || possibilitiesHandler.getPossibility().isSingleSign();
        if (caret.isInsertInner() && !caret.hasSelection() && singleSign) {
            String signCode = possibilitiesHandler.hasPossibilities() ? possibilitiesHandler.getPossibility().getCode()
                    : currentCode.toString();
            if (insertSignAtInnerPosition(signCode)) {
                clearCode();
                return;
            }
        }
        // Now, add the item. If the cursor is inside a quadrat which can't take
        // the sign, the sign goes before the quadrat, and so does the cursor.
        caret.snapToScopeLevel();
        EditingScope scope = scope();
        MDCPosition pos1, pos2;
        pos1 = caret.getLocalInsertPosition();
        pos2 = pos1.getPreviousPosition(1);
        TopItem head = scope.getItemBefore(pos1);

        List<TopItem> newElements = groupBy(head, items, possibilitiesHandler.getSeparator());
        scope.replaceElement(pos1, pos2, newElements);
        clearCode();
    }

    /**
     * Group the two elements in front of the cursor according to the
     */
    private void groupTwoPreviousBySeparator() {
        if (caret.isInsertInner()) {
            // Inside a quadrat, signs are combined as they are inserted.
            return;
        }
        // group only.
        EditingScope scope = scope();
        MDCPosition pos2 = caret.getLocalInsertPosition();
        MDCPosition pos1 = pos2.getPreviousPosition(2);
        List<TopItem> items = scope.getTopItemsBetween(pos1,
                pos2);
        if (items.size() == 2) {
            // Only grouping is done.
            List<TopItem> newElements = groupBy(items.get(0),
                    items.subList(1, items.size()), currentSeparator);
            scope.replaceElement(pos1, pos2, newElements);
        }
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
     * Returns the topitem at index position, or null if none.
     *
     * @return Returns the topitem at index position, or null if none.
     */

    private TopItem getCurrentItem() {
        return scope().getElementBefore(caret.getLocalInsertPosition());
    }

    /**
     * Group the two groups before the insert mark according to this separator
     * All parameters should be copies of the original elements.
     *
     * @param head the item just before the group which should be inserted (may
     *             be null)
     * @param tail the 'new' items (usually, one, but glossary entry might be
     *             whole words).
     * @param sep  one of ' ' ; '*' , ':' or '&'.
     * @return the list of items which should replace head (or head and tail, it
     *         depends).
     */

    private List<TopItem> groupBy(TopItem head, List<TopItem> tail, char sep) {
        // No need for grouping when separator is '-'
        if (sep == ' ') {
            ArrayList<TopItem> result = new ArrayList<>();
            if (head != null) {
                result.add(head);
            }
            result.addAll(tail);
            return result;
        } else {
            // A) try to group tail as one quadrat.
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
                ArrayList<ModelElement> tmpList = new ArrayList<>();
                // tmpList.add(head);
                tmpList.addAll(tail);
                secondElement = grouper.buildCadrat(tmpList);
            }
            // we now have two elements.
            switch (sep) {
                case '*':
                    return buildCombinedQuadrat(head, secondElement);
                case ':':
                    return buildVerticalStack(head, secondElement);
                case '&': {
                    ArrayList<TopItem> list = new ArrayList<>();
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
     * stack the last two cadrats.
     */

    private List<TopItem> buildVerticalStack(TopItem head, TopItem secondElement) {
        ArrayList<TopItem> result = new ArrayList<>();
        VerticalGrouper f = new VerticalGrouper();
        List<TopItem> elts = new ArrayList<>();
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

    private Hieroglyph buildHieroglyphFromCode(String code) {
        Hieroglyph hiero;
        if ("o".equals(code)) {
            hiero = new Hieroglyph(SymbolCodes.REDPOINT);
        } else {
            hiero = new Hieroglyph(code);
        }
        return hiero;
    }

    /**
     * group two quadrats, inserting the second in the <em>lower</em> row of
     * the first.
     */
    private List<TopItem> buildCombinedQuadrat(TopItem head,
            TopItem secondElement) {
        ArrayList<TopItem> result = new ArrayList<>();
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
     * Generic system for transformation of elements, allowing easy creation of
     * undoable processing.
     */
    private interface TopItemModifier {

        /**
         * Takes a topItem (normally already a copy), and returns what will replace it.
         *
         * @param topItem
         * @return a list of new topitems.
         */
        List<TopItem> modifyTopItem(TopItem topItem);
    }

}

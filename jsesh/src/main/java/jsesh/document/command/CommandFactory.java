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
package jsesh.document.command;

import java.util.List;

import jsesh.model.HieroglyphicText;
import jsesh.model.ItemSequence;
import jsesh.model.MDCPosition;
import jsesh.model.MDCRange;
import jsesh.model.TextScope;
import jsesh.model.TopItem;

/**
 * A factory to build the main edit commands used by the hieroglyphic editor.
 * <p> tech detail : Basically build composite commands using deletion and insertion. 
 * <p> other tech detail : the list of available commands needs not be known, as the Factory hides them.
 * <p> The commands work on a scope of the text ({@link TextScope}): the text
 * itself (the methods with top-level positions), or the content of a
 * cartouche... (the methods with a scope and gap indices).
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public class CommandFactory {

	/**
	 * Build a replace element command.
	 * @param topItemList the text where replacement will occur.
	 * @param newTopItems the items to insert
	 * @param pos1 the start position for replacement
	 * @param pos2 the end element for replacement
	 * @param clean is the original text clean (i.e. not modified).
	 * @return
	 */
	public MDCCommand buildReplaceCommand(HieroglyphicText topItemList,
			List<TopItem> newTopItems, MDCPosition pos1, MDCPosition pos2,
			boolean clean) {
		return buildReplaceCommand(topItemList, TextScope.ROOT, newTopItems, pos1.getIndex(), pos2.getIndex(),
				clean);
	}

	/// Builds the replacement of the items between two gaps of a scope.
	///
	/// @param text the text where replacement will occur.
	/// @param scope the sequence of the text which holds the items.
	/// @param newItems the items to insert.
	/// @param gap1 one limit of the items to replace.
	/// @param gap2 the other limit (the order of `gap1` and `gap2` is not important).
	/// @param clean is the original text clean (i.e. not modified)?
	/// @return the command.
	/// @throws IllegalArgumentException if the scope can't hold one of the new
	///         items (see [ItemSequence#accepts(TopItem)]).
	public MDCCommand buildReplaceCommand(HieroglyphicText text, TextScope scope, List<TopItem> newItems, int gap1,
			int gap2, boolean clean) {
		checkAccepted(text, scope, newItems);
		int start = Math.min(gap1, gap2);
		int end = Math.max(gap1, gap2);
		CompositeCommand command = new CompositeCommand(clean);
		command.addCommand(new RemoveCommand(text, scope, start, end, false));
		command.addCommand(new InsertCommand(text, scope, newItems, start, false, false));
		return command;
	}

	public MDCCommand buildInsertCommand(HieroglyphicText model,
			List<TopItem> newCadrats, MDCPosition position, boolean firstCommand) {
		return buildInsertCommand(model, TextScope.ROOT, newCadrats, position.getIndex(), firstCommand);
	}

	/// Builds the insertion of items in a scope.
	///
	/// @param text the text to modify.
	/// @param scope the sequence of the text which will receive the items.
	/// @param newItems the items to insert.
	/// @param index the gap where they will be inserted.
	/// @param firstCommand is this command the first one on a fresh text?
	/// @return the command.
	/// @throws IllegalArgumentException if the scope can't hold one of the new
	///         items (see [ItemSequence#accepts(TopItem)]).
	public MDCCommand buildInsertCommand(HieroglyphicText text, TextScope scope, List<TopItem> newItems, int index,
			boolean firstCommand) {
		checkAccepted(text, scope, newItems);
		return new InsertCommand(text, scope, newItems, index, firstCommand, false);
	}

	/// Builds the insertion of text typed by the user; successive typing
	/// insertions are merged in the undo history, word by word.
	public MDCCommand buildTypingCommand(HieroglyphicText model,
			List<TopItem> newItems, MDCPosition position, boolean firstCommand) {
		return buildTypingCommand(model, TextScope.ROOT, newItems, position.getIndex(), firstCommand);
	}

	/// Same as [#buildTypingCommand(HieroglyphicText, List, MDCPosition, boolean)],
	/// in a scope.
	public MDCCommand buildTypingCommand(HieroglyphicText text, TextScope scope, List<TopItem> newItems, int index,
			boolean firstCommand) {
		checkAccepted(text, scope, newItems);
		return new InsertCommand(text, scope, newItems, index, firstCommand, true);
	}

	public MDCCommand buildRemoveCommand(HieroglyphicText model, MDCPosition pos1,
			MDCPosition pos2, boolean clean) {
		MDCRange range = MDCRange.ofUnordered(pos1, pos2);
		return buildRemoveCommand(model, TextScope.ROOT, range.start().getIndex(), range.end().getIndex(), clean);
	}

	/// Builds the removal of the items between two gaps of a scope.
	///
	/// @param text the text to modify.
	/// @param scope the sequence of the text which holds the items.
	/// @param gap1 one limit.
	/// @param gap2 the other limit (the order of `gap1` and `gap2` is not important).
	/// @param clean is the original text clean (i.e. not modified)?
	/// @return the command.
	public MDCCommand buildRemoveCommand(HieroglyphicText text, TextScope scope, int gap1, int gap2,
			boolean clean) {
		return new RemoveCommand(text, scope, Math.min(gap1, gap2), Math.max(gap1, gap2), clean);
	}

	private static void checkAccepted(HieroglyphicText text, TextScope scope, List<TopItem> items) {
		ItemSequence sequence = scope.resolve(text);
		if (!sequence.acceptsAll(items)) {
			throw new IllegalArgumentException("Some items can't be placed in " + scope + ": " + items);
		}
	}
}

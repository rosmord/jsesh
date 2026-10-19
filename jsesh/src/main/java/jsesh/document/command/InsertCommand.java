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

import java.util.ArrayList;
import java.util.List;

import jsesh.model.AlphabeticCharacter;
import jsesh.model.HieroglyphicText;
import jsesh.model.TextScope;
import jsesh.model.TopItem;

/// Command that inserts one or more items (quadrats...) in a scope of a text:
/// the text itself, or the content of a cartouche...
///
/// The scope is kept as a path, and found again in the text each time the
/// command is done or undone.
///
/// @author rosmord
class InsertCommand extends AbstractMDCCommand {

	/// The items to add.
	private final List<TopItem> newCadrats;
	private final HieroglyphicText text;
	private final TextScope scope;
	/// The gap of the scope where the items are inserted.
	private final int index;

	private final boolean typing;

	/// Create an insertion command.
	///
	/// @param text the text which will be modified.
	/// @param scope the sequence of the text where the items will be inserted.
	/// @param newCadrats the items to add.
	/// @param index the gap of the scope where they will be inserted.
	/// @param firstCommand is this command the first one on a fresh text?
	/// @param typing true if the elements are text typed by the user:
	/// successive typing insertions are then undone one word at a time.
	InsertCommand(HieroglyphicText text, TextScope scope, List<TopItem> newCadrats, int index, boolean firstCommand,
			boolean typing) {
		super(firstCommand);
		this.text = text;
		this.scope = scope;
		this.newCadrats = new ArrayList<>(newCadrats);
		this.index = index;
		this.typing = typing;
	}

	/// A typing insertion absorbs the next one if it continues it (inserted
	/// right after it), until a space has been typed and a new word begins.
	@Override
	public boolean absorb(MDCCommand next) {
		if (!typing || !(next instanceof InsertCommand other) || !other.typing
				|| other.text != text
				|| !other.scope.equals(scope)
				|| other.index != index + newCadrats.size()
				|| newCadrats.isEmpty() || other.newCadrats.isEmpty()) {
			return false;
		}
		if (isSpace(newCadrats.get(newCadrats.size() - 1)) && !isSpace(other.newCadrats.get(0))) {
			return false;
		}
		newCadrats.addAll(other.newCadrats);
		return true;
	}

	private static boolean isSpace(TopItem item) {
		return item instanceof AlphabeticCharacter c && c.isSpace();
	}

	public void doCommand() {
		scope.resolve(text).insertItemsAt(index, newCadrats);
	}

	public void undoCommand() {
		scope.resolve(text).removeItems(index, index + newCadrats.size());
	}

}

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
import jsesh.model.TextScope;
import jsesh.model.TopItem;

/// Command that deletes one or more items (quadrats...) of a scope of a text:
/// the text itself, or the content of a cartouche...
///
/// @author rosmord
class RemoveCommand extends AbstractMDCCommand {

	private final HieroglyphicText text;
	private final TextScope scope;
	/// The removed items lie between these two gaps of the scope.
	private final int start, end;

	/// The deleted elements.
	private List<TopItem> deletedElements;

	/// Create a command corresponding to the erasure of the items between two
	/// gaps of a scope.
	///
	/// @param text the text which will be modified.
	/// @param scope the sequence of the text which holds the items.
	/// @param start the first gap.
	/// @param end the last gap (`start <= end`).
	/// @param firstCommand is this command the first one on a fresh text?
	RemoveCommand(HieroglyphicText text, TextScope scope, int start, int end, boolean firstCommand) {
		super(firstCommand);
		this.text = text;
		this.scope = scope;
		this.start = start;
		this.end = end;
		deletedElements = null;
	}

	public void doCommand() {
		deletedElements = scope.resolve(text).removeItems(start, end);
	}

	public void undoCommand() {
		scope.resolve(text).insertItemsAt(start, deletedElements);
	}

}

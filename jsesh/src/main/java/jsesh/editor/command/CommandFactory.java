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
package jsesh.editor.command;

import java.util.List;

import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;

/**
 * A factory to build the main edit commands used by the hieroglyphic editor.
 * <p> tech detail : Basically build composite commands using deletion and insertion. 
 * <p> other tech detail : the list of available commands needs not be known, as the Factory hides them.
 * @author Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)
 *
 */
public class CommandFactory {

	public MDCCommand buildReplaceCommand(TopItemList topItemList,
			List<TopItem> newTopItems, MDCPosition pos1, MDCPosition pos2,
			boolean clean) {
		CompositeCommand command = new CompositeCommand(clean);
		MDCPosition[] p = MDCPosition.getOrdereredPositions(pos1, pos2);
		MDCCommand removeCommand = buildRemoveCommand(topItemList, pos1, pos2,
				false);
		MDCCommand insertCommand = buildInsertCommand(topItemList, newTopItems,
				p[0], false);
		command.addCommand(removeCommand);
		command.addCommand(insertCommand);
		return command;
	}

	public MDCCommand buildInsertCommand(TopItemList model,
			List<TopItem> newCadrats, MDCPosition position, boolean firstCommand) {
		return new InsertCommand(model, newCadrats, position, firstCommand);
	}

	public MDCCommand buildRemoveCommand(TopItemList model, MDCPosition pos1,
			MDCPosition pos2, boolean clean) {
		return new RemoveCommand(model, pos1, pos2, clean);

	}	
}

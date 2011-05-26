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
import jsesh.mdc.model.TopItemList;

/**
 * Command that deletes one or more cadrats.
 * @author rosmord
 *
 */

class RemoveCommand extends AbstractMDCCommand {
	/**
	 * Where will the deletion take place?
	 */
	private MDCPosition[] range;
	
	
	/**
	 * The deleted elements.
	 */
	private List deletedElements;
	
	/**
	 * The text which will be modified. 
	 */
	private TopItemList topItemList;
	
	/**
	 * Create a command corresponding to the erasure of text between two positions.
	 * Note that the position order is not fixed pos1 may be before or after pos2.
	 * @param topItemList
	 * @param pos1 one of the positions
	 * @param pos2 the other position.
	 * @param firstCommand
	 */
	public RemoveCommand(TopItemList topItemList, MDCPosition pos1, MDCPosition pos2, boolean firstCommand) {
		super(firstCommand);
		this.topItemList= topItemList;
		range = MDCPosition.getOrdereredPositions(pos1, pos2);		
		deletedElements= null;
	}
	
	public void doCommand() {
		deletedElements= topItemList.removeTopItems(range[0].getIndex(), range[1].getIndex());
	}
	
	public void undoCommand() {
		topItemList.addAllAt(range[0].getIndex(), deletedElements);
	}
	
}

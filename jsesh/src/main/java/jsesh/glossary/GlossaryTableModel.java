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
package jsesh.glossary;

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class GlossaryTableModel extends AbstractTableModel {

	private JSeshGlossary glossary = GlossaryManager.getInstance()
			.getGlossary();

	public int getColumnCount() {
		return 3;
	}

	public int getRowCount() {
		return glossary.getNumberOfEntries();
	}

	public Object getValueAt(int row, int col) {
		if (row < 0 || row >= glossary.getNumberOfEntries())
			return null;
		switch (col) {
		case 0:
			return glossary.getEntry(row).getKey();
		case 1:
			return glossary.getEntry(row).getMdc();
		case 2:
			return ""; // "remove" button.
		default:
			return null;
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 2;
	}

	public void delete(int pos) {
		if (0 <= pos && pos < glossary.getNumberOfEntries()) {
			glossary.remove(glossary.getEntry(pos));
			GlossaryManager.getInstance().save();
			fireTableRowsDeleted(pos, pos);
		}
	}

	public void addEntry(String name, String mdc) {
		glossary.add(name, mdc);
		GlossaryManager.getInstance().save();		
		fireTableStructureChanged(); // as elements are sorted, it's easier to redraw everything.
	}


}

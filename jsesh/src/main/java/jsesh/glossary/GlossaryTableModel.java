package jsesh.glossary;

import javax.swing.table.AbstractTableModel;

import jsesh.mdc.model.TopItemList;

@SuppressWarnings("serial")
public class GlossaryTableModel extends AbstractTableModel {
	
	private JSeshGlossary glossary;

	public int getColumnCount() {
		return 2;		
	}

	public int getRowCount() {
		return glossary.getNumberOfEntries();
	}
 
	public Object getValueAt(int arg0, int arg1) {
				throw new RuntimeException("Write me");
	}
	
	public void delete(int pos) {
		throw new RuntimeException("Write me");		
	}
	
	public void addEntry(String name, TopItemList data) {
		
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// WRITE Auto-generated method stub
		if (true) throw new RuntimeException("WRITE ME");
		super.setValueAt(aValue, rowIndex, columnIndex);
	}
}

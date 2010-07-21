package jsesh.swing.signPalette;

import javax.swing.table.AbstractTableModel;

/**
 * A table model which in fact represent a list which should be displayed in a compact way.
 * @author rosmord
 */

public class CompactListTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -445002616606156708L;

	/**
	 * The number of columns to display.
	 */
	private int columnNumber= 3;

	private String [] data;
	
	
	public CompactListTableModel(String[] data) {
		super();
		this.data = data;
	}

	public int getColumnCount() {
		return columnNumber;
	}

	public int getRowCount() {
		int length= data.length;
		if (length % columnNumber == 0)
			return length / columnNumber;
		else
			return length / columnNumber + 1; 
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		int index= rowIndex * columnNumber + columnIndex;
		if (index < data.length) {
			return data [index];
		} else
			return null;
	}

	public String getColumnName(int column) {
		return null;
	}
	
	
	
	
}

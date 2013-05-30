/**
 * 
 */
package org.qenherkhopeshef.viewToolKit.drawing.tabularDrawing;

public class ElementInfo {
	private int firstColumn;
	private int firstRow;
	private int horizontalSpan;
	private int verticalSpan= 1;

	public ElementInfo(int firstColumn, int firstRow, int horizontalSpan) {
		this.setFirstRow(firstRow);
		this.setFirstColumn(firstColumn);
		this.setHorizontalSpan(horizontalSpan);
	}

	void setFirstColumn(int firstColumn) {
		this.firstColumn = firstColumn;
	}

	public int getFirstColumn() {
		return firstColumn;
	}

	void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public int getFirstRow() {
		return firstRow;
	}

	/**
	 * Sets the number of columns this element occupies (1 for 1 column).
	 * @param span
	 */
	void setHorizontalSpan(int span) {
		this.horizontalSpan = span;
	}

	/**
	 * 
	 * @param verticalSpan
	 */
	public void setVerticalSpan(int verticalSpan) {
		this.verticalSpan = verticalSpan;
	}
	
	public int getHorizontalSpan() {
		return horizontalSpan;
	}

	public int getVerticalSpan() {
		return verticalSpan;
	}
	
	
	public int getLastRow() {
		return firstRow + verticalSpan -1;
	}

}
package org.qenherkhopeshef.viewToolKit.drawing.tabularDrawing;

import java.awt.Color;
import java.awt.geom.Dimension2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import org.qenherkhopeshef.algo.ReversibleMultiHashMap;
import org.qenherkhopeshef.viewToolKit.drawing.AbstractDrawing;
import org.qenherkhopeshef.viewToolKit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewToolKit.drawing.element.RectangleElement;
import org.qenherkhopeshef.viewToolKit.drawing.event.CursorChangedEvent;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingChangedEvent;
import org.qenherkhopeshef.viewToolKit.drawing.event.DrawingEvent;
import org.qenherkhopeshef.viewToolKit.drawing.event.ElementAdded;
import org.qenherkhopeshef.viewToolKit.drawing.event.ElementRemoved;

/**
 * A drawing organized in a tabular way. Elements added to the drawing will try
 * to fill their square.
 * 
 * <p>
 * An optional cursor may be drawn too. The current version is vertical.
 * 
 * @author rosmord
 * 
 */
public class TabularDrawing extends AbstractDrawing {

	// Layout maintainer algorithm.
	// When new data is added :
	// all items may change (the available grid space may change in fact).
	//
	// But in fact, we are mostly interested by the item's "maximal" space.
	//
	// Possible operations :
	//
	// Element(s) addition(s) :
	// Compute the width and height of the new elements.
	// change their square's dimensions accordingly.
	// let a be the minimal x index of a modified square (a square whose width
	// changed)
	// change (translate) all elements and entries after a.
	// let b be the minimal y index... (same).
	//
	// Element suppression: for this application, save in the case of complete
	// suppression of all text, we consider that element suppression is not
	// likely.
	// In this case, the other element keep their current coordinate.
	//
	// Element modification is very much the same as element addition.
	//
	// "On demand" Computation:
	// When display is asked we check that :
	// a) the drawing dimensions are lower or equal than the square which
	// contains them
	// b) that they are roughly equals to those of their square.
	// for each element which doesn't fit, a complete re-layout is in order.
	//

	/**
	 * Data about the drawing's columns.
	 * 
	 * @author rosmord
	 */
	private static class Column {
		double width;
		double xPos;

		@Override
		public String toString() {
			return "column pos " + xPos + " width " + width;
		}
	}

	/**
	 * Data about the drawing's rows.
	 * 
	 * @author rosmord
	 * 
	 */
	private static class Row {
		double height;
		double yPos;

		@Override
		public String toString() {
			return "row pos " + yPos + " height " + height;
		}

	}

	/**
	 * Is the drawing being modified. This is used to mark large modifications.
	 * When beingChanged is true, layout and other operations normally done when
	 * something is changed will be postponed until beingChanged become false.
	 */
	private boolean beingChanged;

	/**
	 * Geometric information about the columns.
	 */
	private ArrayList<Column> columns = new ArrayList<Column>();

	/**
	 * Geometric information about each element.
	 */
	private HashMap<GraphicalElement, ElementInfo> elementInfoMap = new HashMap<GraphicalElement, ElementInfo>();

	/**
	 * Information about the individual grid square contents.
	 */

	private ReversibleMultiHashMap<Coordinates, GraphicalElement> grid = new ReversibleMultiHashMap<Coordinates, GraphicalElement>();

	/**
	 * Geometric information about the rows.
	 */
	private ArrayList<Row> rows = new ArrayList<Row>();

	private TabularDrawingCursor cursor = null;

	/**
	 * Building advancement listener.
	 */

	private AdvancementListener advancementListener = null;

	/**
	 * Add an element to the drawing.
	 * 
	 * @param graphicalElement
	 *            the element
	 * @param i
	 *            the column.
	 * @param j
	 *            the line.
	 * @param colSpan
	 *            the number of column spanned by this element.
	 * @param rowSpan
	 *            the number of rows spanned by this element (<b>currently
	 *            ignored</b>)
	 */
	public void add(GraphicalElement graphicalElement, int i, int j,
			int colSpan, int rowSpan) {
		manage(graphicalElement);
		putInSquare(i, j, graphicalElement, colSpan, rowSpan);
		if (!isBeingChanged()) {
			setBeingChanged(true);
			graphicalElement.setOrigin(getColumnLeftPosition(i),
					getRowPosition(j));
			relayout(graphicalElement);
			fireDrawingEvent(new ElementAdded(graphicalElement));
			setBeingChanged(false);
		}
	}

	private double getRowPosition(int j) {
		if (j < 0 || j >= rows.size()) {
			if (j >= rows.size() && rows.size() > 0) {
				Row lastRow = getRow(rows.size() - 1);
				return lastRow.yPos + lastRow.height;
			} else
				return 0;
		} else
			return getRow(j).yPos;
	}

	private double getColumnLeftPosition(int i) {
		if (i < 0 || i >= columns.size()) {
			if (i >= columns.size() && columns.size() > 0) {
				Column lastCol = getColumn(columns.size() - 1);
				return lastCol.xPos + lastCol.width;
			} else
				return 0;
		} else
			return getColumn(i).xPos;
	}

	private double getColumnRightPosition(int i) {
		if (i < 0 || i >= columns.size()) {
			if (i >= columns.size() && columns.size() > 0) {
				Column lastCol = getColumn(columns.size() - 1);
				return lastCol.xPos + lastCol.width;
			} else
				return 0;
		} else
			return getColumn(i).xPos + getColumn(i).width;
	}

	@Override
	/*
	 * * Called when an event occurs on a child element.
	 */
	protected void eventOccurredInDrawing(DrawingEvent ev) {
		// Avoid being called when in layout (else, infinite recursion !)
		if (!beingChanged) {
			beingChanged = true;
			// Find the culprit, and re-layout...
			relayout(ev.getSource());
			// If the layout has changed, we should probably INVALIDATE IT.
			beingChanged = false;
			fireDrawingEvent(ev);
		}
	}

	/**
	 * Find the column containing a certain position.
	 * <p>
	 * Special cases:
	 * <ul>
	 * <li>returns 0 if there is no column at all.
	 * <li>returns -1 if the position is in front of column 0
	 * <li>returns 1 + last column index (that is, the number of columns) if the
	 * position is after the last column.
	 * </ul>
	 * <p>
	 * If no existing column contains the position, we will return 0 if the
	 * position is before all columns, and columns.size() if the position is
	 * after the last column.
	 * 
	 * @param x
	 * @return
	 */
	private int findColumnFor(double x) {
		if (columns.size() == 0)
			return 0;
		int a = 0;
		int b = columns.size() - 1;
		// Special cases (x out of bounds).
		if (getColumn(0).xPos > x)
			return -1;
		if (getColumnRightPosition(b) <= x)
			return b + 1;
		// Ok, now we know that colA.xPos <= x < colB.xPos + colB.width.
		// We keep this as an invariant until b-a <= 1 :
		while (b - a > 1) {
			int c = (a + b) / 2;
			double m = getColumnLeftPosition(c);
			if (m > x)
				b = c;
			else
				a = c;
		}
		// Now, x might be either in a or b.
		if (x > getColumnLeftPosition(b))
			return b;
		else
			return a;
	}

	/**
	 * Find the first row for a certain position. Follows the same principles as
	 * {@see #findColumnFor(double)}.
	 * 
	 * @param y
	 * @return
	 */
	private int findRowFor(double y) {
		if (rows.size() == 0)
			return 0;
		int a = 0;
		int b = rows.size() - 1;
		// Special cases (x out of bounds).
		if (getRow(0).yPos > y)
			return -1;
		if (getRowPosition(b) <= y)
			return b + 1;
		// Ok, now we know that colA.xPos <= x < colB.xPos.
		// We keep this as an invariant until b-a <= 1 :
		while (b - a > 1) {
			int c = (a + b) / 2;
			double m = getRowPosition(c);
			if (m > y)
				b = c;
			else
				a = c;
		}
		return a;
	}

	/**
	 * Gets the grid coordinates for a particular point.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Coordinates getPointCoordinates(double x, double y) {
		return new Coordinates(findColumnFor(x), findRowFor(y));
	}

	public Coordinates getGridCoordinates(Point2D p) {
		return getPointCoordinates(p.getX(), p.getY());
	}

	private Column getColumn(int i) {
		while (columns.size() <= i)
			columns.add(new Column());
		return columns.get(i);
	}

	public Collection<GraphicalElement> getElementsInZone(Rectangle2D rectangle) {
		Collection<GraphicalElement> result = new LinkedHashSet<GraphicalElement>();

		// TODO : deal with depth if needed.

		// Compute the columns we want.
		int firstCol, lastCol;

		// First column: currently, find the first column after
		// rectangle.getMinX();
		// The first column is the previous one (or 0).
		firstCol = findColumnFor(rectangle.getMinX());

		// Last column : currently, find the last column after
		// rectangle.getMaxX().
		// The last column is the previous one (or 0).

		lastCol = findColumnFor(rectangle.getMaxX());
		// Compute the lines we want
		// Currently, all lines...
		int firstLine, lastLine;
		firstLine = 0;
		lastLine = rows.size() - 1;
		if (lastLine < 0)
			lastLine = 0;

		// Return their content
		for (int i = firstCol; i <= lastCol + 1; i++) {
			for (int j = firstLine; j <= lastLine; j++) {
				for (GraphicalElement elt : getSquareContent(i, j)) {
					if (elt.getBounds().intersects(rectangle))
						result.add(elt);
				}
			}
		}
		return result;
	}

	@Override
	public Collection<GraphicalElement> getDecorations() {
		if (cursor == null)
			return Collections.emptySet();
		else
			return Collections.singleton(getCursorRectangle());
	}

	/**
	 * Returns a rectangle for the cursor (it should be possible to configure
	 * this).
	 * <p>
	 * precondition : getCursor() != null.
	 * 
	 * @return
	 */
	private GraphicalElement getCursorRectangle() {
		if (getCursor() == null)
			throw new NullPointerException();
		double height = getRow(getCursor().row).height;
		double xpos;
		if (getCursor().getCol() < getNumberOfColumns()) {
			xpos = getColumnLeftPosition(getCursor().getCol());
		} else if (getCursor().getCol() > 0) {
			xpos = getColumnRightPosition(getCursor().getCol() - 1);
		} else {
			xpos = 0;
		}
		double ypos = getRowPosition(getCursor().getRow());
		RectangleElement result = new RectangleElement();
		Color color = new Color(255, 0, 0, 100);
		result.setBackground(color);
		result.setLineColor(color);

		double cursorWidth = 20;
		result.setBounds(xpos - cursorWidth / 2, ypos, cursorWidth, height);
		result.setLayer(0);
		return result;
	}

	public double getHeight() {
		if (rows.size() == 0)
			return 0;
		else {
			Row lastRow = getRow(rows.size() - 1);
			return lastRow.yPos + lastRow.height;
		}
	}

	public Rectangle2D getPreferredSize() {
		return new Rectangle2D.Double(0, 0, getWidth(), getHeight());
	}

	private Row getRow(int j) {
		while (rows.size() <= j) {
			rows.add(new Row());
		}
		return rows.get(j);
	}

	public Collection<GraphicalElement> getSquareContent(int i, int j) {
		if (i < 0 || j < 0 || i >= getNumberOfColumns()
				|| j >= getNumberOfRows())
			return Collections.emptySet();
		return Collections.unmodifiableCollection(grid
				.get(new Coordinates(i, j)));
	}

	public Collection<GraphicalElement> getColumnContent(int i) {
		if (i < 0 || i >= getNumberOfColumns())
			return Collections.emptySet();
		HashSet<GraphicalElement> result = new HashSet<GraphicalElement>();
		for (int j = 0; j < getNumberOfRows(); j++)
			result.addAll(getSquareContent(i, j));
		return result;
	}

	public int getNumberOfRows() {
		return rows.size();
	}

	public int getNumberOfColumns() {
		return columns.size();
	}

	public double getWidth() {
		if (columns.size() == 0)
			return 0;
		else {
			Column lastCol = getColumn(columns.size() - 1);
			return lastCol.xPos + lastCol.width;
		}
	}

	/**
	 * Is the drawing being changed. In this case, no event will be sent until
	 * setBeingChanged(false) is called.
	 */
	public boolean isBeingChanged() {
		return beingChanged;
	}

	/**
	 * Initial Lay out for the whole drawing.
	 */
	private void layout() {
		boolean oldBeingChanged = beingChanged;
		beingChanged = true;

		int numberOfSquares = grid.keySet().size();
		int numberDone = 0;
		fireDone(0);
		// Compute the width and height of all lines and columns.
		for (Coordinates c : grid.keySet()) {
			Row row = getRow(c.getRow());
			Column column = getColumn(c.getColumn());

			for (GraphicalElement elt : grid.get(c)) {
				ElementInfo square = elementInfoMap.get(elt);
				// Divide the element over all the available columns.
				double eltWidth = elt.getPreferredSize().getWidth()
						/ square.getHorizontalSpan();
				if (row.height < elt.getPreferredSize().getHeight()) {
					row.height = elt.getPreferredSize().getHeight();
				}
				if (column.width < eltWidth) {
					column.width = eltWidth;
				}
			}
			numberDone++;
			fireDone((numberDone * 50) / numberOfSquares);
		}
		// Now, compute coordinates:
		fixColumnsCoordinates();
		fixRowsCoordinates();
		fitCells();
		beingChanged = oldBeingChanged;
		fireFinished();
	}

	private void fireFinished() {
		if (advancementListener != null)
			advancementListener.notifyCompletion();
	}

	private void fireDone(int percent) {
		if (advancementListener != null)
			advancementListener.notifyCompletion(percent);
	}

	private void fixRowsCoordinates() {
		double y = 0;
		for (Row row : rows) {
			row.yPos = y;
			y += row.height;
		}
	}

	private void fixColumnsCoordinates() {
		double x = 0;
		for (Column col : columns) {
			col.xPos = x;
			x += col.width;
		}
	}

	/**
	 * Fit the cell content to their respective cells.
	 */
	private void fitCells() {
		int total = grid.keySet().size();
		int nbrDone = 0;
		for (Coordinates c : grid.keySet()) {
			if (!grid.get(c).isEmpty()) {
				Row row = getRow(c.getRow());
				Column column = getColumn(c.getColumn());
				for (GraphicalElement elt : grid.get(c)) {
					ElementInfo square = elementInfoMap.get(elt);
					if (c.getColumn() == square.getFirstColumn()) {
						double eltWidth = 0;
						for (int i = 0; i < square.getHorizontalSpan(); i++) {
							eltWidth += getColumn(c.getColumn() + i).width;
						}
						Rectangle2D oldBounds = elt.getBounds();
						if (oldBounds.getWidth() != eltWidth
								|| oldBounds.getHeight() != row.height) {
							elt.setBounds(column.xPos, row.yPos, eltWidth,
									row.height);
						} else {
							elt.setOrigin(column.xPos, row.yPos);
						}
					}
				}
			}
			nbrDone++;
			fireDone(50 + (nbrDone * 50) / total);
		}
	}

	/**
	 * Put an element in the relevant(s) square(s). An element will be placed in
	 * squares i, i+1 ... i+hspan -1.
	 * 
	 * @param i
	 * @param j
	 * @param g
	 * @param hspan
	 * @param vspan
	 *            (vertical span, not currently used).
	 */
	private void putInSquare(int i, int j, GraphicalElement g, int hspan,
			int vspan) {
		ElementInfo squareContent = new ElementInfo(i, j, hspan);
		// Store the square content :
		elementInfoMap.put(g, squareContent);
		// Store the element in the grid.
		for (int a = i; a < i + hspan; a++) {
			grid.put(new Coordinates(a, j), g);
		}
	}

	/**
	 * Change the layout of one element (whose size has changed). Modify its
	 * line and column data. Change the other elements too.
	 * 
	 * @param elt
	 */
	private void relayout(GraphicalElement elt) {

		ElementInfo info = elementInfoMap.get(elt);

		// Find the element preferred size...
		Dimension2D preferred = elt.getPreferredSize();

		// If needed (that is, the new size is larger than the current one)
		// (Note: one should compute the total space available instead)
		boolean widthChanged = preferred.getWidth() > elt.getBounds()
				.getWidth();

		boolean heightChanged = preferred.getHeight() > elt.getBounds()
				.getHeight();

		Column column = getColumn(info.getFirstColumn());

		Row row = getRow(info.getFirstRow());

		if (widthChanged || heightChanged) {
			// enlarge the element
			elt.setBounds(elt.getBounds().getMinX(), elt.getBounds().getMinY(),
					preferred.getWidth(), preferred.getHeight());

			// Now, distribute the element increase in width among all columns.
			if (elt.getBounds().getWidth() > column.width) {
				column.width = elt.getBounds().getWidth();
				// Move the following columns
				fixColumnsCoordinates();
			}

			if (elt.getBounds().getHeight() > row.height) {
				row.height = elt.getBounds().getHeight();
				fixRowsCoordinates();
			}
			// If a vertical change occurred, move all elements below the
			// current one.
			if (heightChanged) {

			}
			// If an horizontal change occurred, move all elements after the
			// current one.
			if (widthChanged || heightChanged) {
				fitCells();
				// fixRowsCoordinates();
				// fixColumnsCoordinates();
				/*
				 * int count = 0; double x = column.width + column.xPos; for
				 * (int i = info.firstColumn + 1; i < columns.size(); i++) {
				 * Column currentColumn = getColumn(i); currentColumn.xPos = x;
				 * for (int j = 0; j < rows.size(); j++) { for (GraphicalElement
				 * gelt : grid.get(new Coordinates( i, j))) { ElementInfo gInfo
				 * = elementInfoMap.get(gelt); if (gInfo.firstColumn == i) {
				 * double y = gelt.getBounds().getMinY(); gelt.setOrigin(x, y);
				 * count++; } } } x += currentColumn.width; }
				 * System.out.println("Changed " + count + " elements");
				 */

			}

		}

	}

	/**
	 * Sets the advancement listener for this drawing.
	 * 
	 * @param advancementListener
	 */
	public void setAdvancementListener(AdvancementListener advancementListener) {
		this.advancementListener = advancementListener;
	}

	public void removeAdvancementListener() {
		this.advancementListener = null;
	}

	public void setBeingChanged(boolean beingChanged) {
		// BEWARE... THIS SHOULD BE MORE SUBTLE THAN WHAT I HAVE WRITEN.
		// if elements were added, one should probably use layout() (or add
		// elements separatly ?)

		if (this.beingChanged && !beingChanged) {
			layout();
		}
		this.beingChanged = beingChanged;
		fireDrawingEvent(new DrawingChangedEvent());
	}

	public ElementInfo getElementCell(GraphicalElement elt) {
		ElementInfo info = elementInfoMap.get(elt);
		return info;
	}

	public void removeElement(GraphicalElement element) {
		elementInfoMap.remove(element);
		// if (info != null) {
		grid.removeValue(element);
		// }
		fireDrawingEvent(new ElementRemoved(element));
	}

	/**
	 * Completely clear the drawing.
	 */

	public void clear() {
		columns = new ArrayList<Column>();
		rows = new ArrayList<Row>();
		elementInfoMap = new HashMap<GraphicalElement, ElementInfo>();
		grid = new ReversibleMultiHashMap<Coordinates, GraphicalElement>();

		if (!beingChanged)
			fireDrawingEvent(new DrawingChangedEvent());
	}

	public boolean isEmpty() {
		return elementInfoMap.isEmpty();
	}

	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		boolean first = true;
		HashSet<GraphicalElement> done = new HashSet<GraphicalElement>();
		for (Coordinates coords : grid.keySet()) {
			buff.append("[" + coords.toString());
			for (GraphicalElement e : grid.get(coords)) {
				if (!done.contains(e)) {
					if (!first) {
						buff.append(", ");
					}
					buff.append(e.toString());
					first = false;
					done.add(e);
				}
			}
			buff.append("]");
		}
		return buff.toString();
	}

	public void setCursor(TabularDrawingCursor cursor) {
		this.cursor = cursor;
		fireDrawingEvent(new CursorChangedEvent());
	}

	public TabularDrawingCursor getCursor() {
		return cursor;
	}

	/**
	 * translate all columns from firstColumn to the end by the specified number
	 * of columns. Actually, the new columns will be empty, so no coordinate
	 * change. The display of the drawing itself won't change as a result of
	 * this translation.
	 * 
	 * @param firstColumn
	 *            first column to translate
	 * @param numberOfColumns
	 *            the number of column added (if positive) or removed (if
	 *            negative).
	 */
	public void translate(int firstColumn, int numberOfColumns) {
		setBeingChanged(true);
		// compute the starting graphical position
		double startPos = 0;
		if (firstColumn > 0) {
			Column previousColumn = columns.get(firstColumn - 1);
			startPos = previousColumn.xPos + previousColumn.width;
		}

		if (numberOfColumns > 0) {
			// insert empty columns at the correct position...
			// Add new columns if needed (nope, empty list !!!)
			ArrayList<Column> newCols = new ArrayList<Column>(numberOfColumns); // Pre-sized
																				// arraylist.
			for (int i = firstColumn; i < firstColumn + numberOfColumns; i++) {
				Column column = new Column();
				column.width = 0;
				column.xPos = startPos;
				newCols.add(column);
			}
			// actual insertion.
			columns.addAll(firstColumn, newCols);
		} else {
			for (int i = 0; i < -numberOfColumns; i++) {
				columns.remove(firstColumn);
			}
		}
		// update element info (just to be sure, we get a copy, as putInSquare
		// might change elementInfoMap) -- this is probably NOT necessary.
		HashMap<GraphicalElement, ElementInfo> entries = new HashMap<GraphicalElement, ElementInfo>();
		entries.putAll(elementInfoMap);
		for (Entry<GraphicalElement, ElementInfo> entry : entries.entrySet()) {
			ElementInfo info = entry.getValue();
			if (info.getFirstColumn() >= firstColumn) {
				grid.removeValue(entry.getKey());
				putInSquare(info.getFirstColumn() + numberOfColumns,
						info.getFirstRow(), entry.getKey(),
						info.getHorizontalSpan(), info.getVerticalSpan());
			}
		}
		setBeingChanged(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see grammaticalBase.viewToolkit.drawing.Drawing#getCursorBounds()
	 */
	@Override
	public Rectangle2D getCursorBounds() {
		if (cursor != null) {
			Rectangle2D bounds = getCursorRectangle().getBounds();
			double w= bounds.getWidth();
			double minx= Math.max(0, bounds.getMinX() - 10*w);
			double newW= bounds.getMaxX() + 10*w - minx;
			return new Rectangle2D.Double(minx, bounds.getMinY(), newW, bounds.getHeight());
		} else
			return null;
	}

}

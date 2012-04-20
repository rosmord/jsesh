package org.qenherkhopeshef.viewToolKit.drawing.tabularDrawing;

/**
* Cursor position.
* <p>Currently, the cursor is "vertical", i.e. suppose to stand between two elements standing on the same
 * horizontal level.
* User: rosmord
* Date: 20/03/11
* Time: 19:22
*/
public class TabularDrawingCursor {
    int row;
    int col;

    public TabularDrawingCursor(int row, int col) {
        super();
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}

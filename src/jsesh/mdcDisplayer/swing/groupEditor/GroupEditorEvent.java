/*
 * Created on 30 nov. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jsesh.mdcDisplayer.swing.groupEditor;

import java.awt.geom.Point2D;

import jsesh.mdc.model.AbsoluteGroup;

/**
 * Represent a mouse action over a group editor.
 * @author rosmord
 */

public class GroupEditorEvent {
    public static final int TOP= 1;
    public static final int BOTTOM= 2;
    public static final int LEFT= 3;
    public static final int RIGHT= 4;
    public static final int MIDDLE= 0;
    
    private Point2D point;
	private AbsoluteGroup group;
	private int elementIndex;
	private boolean onHandle;
	private int horizontalHandlePosition;
	private int verticalHandlePosition;
	
	
	/**
	 * Create an event for a mouse click or down on an absolute group.
	 * @param group : the group being edited
	 * @param point
	 * @param elementIndex : the element in the group
	 */
	public GroupEditorEvent(AbsoluteGroup group, Point2D point, int elementIndex) {
		super();
		this.group = group;
		this.point= point;
		this.elementIndex = elementIndex;
		this.onHandle = false;
	}
	
    public int getHorizontalHandlePosition() {
        return horizontalHandlePosition;
    }
    
    /**
     * Get the mouse event point, in model coordinates.
     * @return the mouse event point, in model coordinates
     */
    public Point2D getPoint() {
        return point;
    }
    
    public int getVerticalHandlePosition() {
        return verticalHandlePosition;
    }
    
    /**
     * Event for mouse events on an handle.
     * @param group
     * @param point
     * @param elementIndex
     * @param handleHorizontalPosition
     * @param handleVerticalPosition
     */
	
    public GroupEditorEvent(AbsoluteGroup group, Point2D point, int elementIndex,
            int handleHorizontalPosition, int handleVerticalPosition) {
        this.group = group;
        this.point= point;
        this.elementIndex = elementIndex;
        this.onHandle= true;
        this.horizontalHandlePosition = handleHorizontalPosition;
        this.verticalHandlePosition = handleVerticalPosition;
    }
    
    /**
     * Return the element on which this event occured, or -1 if none. 
     * @return the index of the element on which this event occured, or -1 if none
     */
	public int getElementIndex() {
		return elementIndex;
	}
	
	public AbsoluteGroup getGroup() {
		return group;
	}
	
	public boolean isOnHandle() {
		return onHandle;
	}
}

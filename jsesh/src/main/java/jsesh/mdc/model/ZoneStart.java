/*
 * Created on 25 nov. 2004
 *
 * This file is distributed under the LGPL.
 */
package jsesh.mdc.model;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.interfaces.ZoneStartInterface;

/**
 * Zone markers introduce new drawing zones in the text.
 * A zone gives information on the following points :
 * <ul>
 * <li> zone position;
 * <li> zone dimensions;
 * <li> zone text orientation and direction.
 * </ul>
 * 
 * The layout of the text is changed by zones.
 * 
 * Suppose we are in left-to-right, line-oriented text and 
 * that we start a zone in right-to-left columns.
 * <p>
 * The layout will be done accordingly : signs will be reverted
 * (including parenthesis !), hboxes will be built from right to left,
 * and cadrats will be stacked, ends of lines will bring the element back to the reference line.
 * <p> The zone dimensions will be computed, and the zone origin will be set using those.
 * 
 * <p> The possible problem is 
 * 
 * @author S. Rosmorduc
 *
 */
public class ZoneStart extends TopItem implements ZoneStartInterface {

	private static final long serialVersionUID = -6568578915390082362L;
	/**
	 * Zone width. If set to "0", the width will be computed.
	 */
	private int width;
	/**
	 * Zone Height. If set to "0", will be computed.
	 */
	private int height;
	
	private int refx;
	private int refy;
	
	/**
	 * The reference point may correspond to the top, to the bottom of the zone, or to the current position. 
	 */
	private int refHorizontalReference;
	
	/**
	 * The reference point may correspond to the top, to the bottom of the zone, or to the current position.
	 */
	private int refVerticalReference;
	
	private TextDirection writingDirection;
	/**
	 * 
	 */
	private TextOrientation writingOrientation;
	private int lineSkip;
	/**
	 * spaces between columns.
	 */
	private int columnSkip;
	/**
	 * drawn lines between (lines or columns).
	 * if 0, nothing is drawn.
	 */
	private int lineWidth; 
	
	public ZoneStart() {
		
	}
	
	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#accept(jsesh.mdc.model.ModelElementVisitor)
	 */
	public void accept(ModelElementVisitor v) {
		v.visitZoneStart(this);
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#compareToAux(jsesh.mdc.model.ModelElement)
	 */
	public int compareToAux(ModelElement e) {
		return compareContents(e);
	}

	/* (non-Javadoc)
	 * @see jsesh.mdc.model.ModelElement#deepCopy()
	 */
	public ZoneStart deepCopy() {
		ZoneStart result= new ZoneStart();		
		return result;
	}

	/**
	 * Fills the data for this elements from an optionMap.
	 * @param l
	 */
	public void setOptions(OptionsMap l) {
		
	}
	
	/**
	 * Returns the columnSkip.
	 * @return Returns the columnSkip.
	 */
	public int getColumnSkip() {
		return columnSkip;
	}
	
	/**
	 * Set the distance which separates columns of text.
	 * If the text orientation is horizontal, this is simply the space between cadrats.
	 * @param columnSkip The columnSkip to set.
	 */
	
	public void setColumnSkip(int columnSkip) {
		this.columnSkip = columnSkip;
	}
	
	/**
	 * 
	 * @return Returns the height.
	 */
	
	public int getHeight() {
		return height;
	}
	/**
	 * @param height The height to set.
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	/**
	 * @return Returns the lineSkip.
	 */
	public int getLineSkip() {
		return lineSkip;
	}
	/**
	 * @param lineSkip The lineSkip to set.
	 */
	public void setLineSkip(int lineSkip) {
		this.lineSkip = lineSkip;
	}
	/**
	 * @return Returns the lineWidth.
	 */
	public int getLineWidth() {
		return lineWidth;
	}
	/**
	 * @param lineWidth The lineWidth to set.
	 */
	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}
	/**
	 * @return Returns the width.
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * @param width The width to set.
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	/**
	 * @return Returns the writingDirection.
	 */
	public TextDirection getWritingDirection() {
		return writingDirection;
	}
	/**
	 * @param writingDirection The writingDirection to set.
	 */
	public void setWritingDirection(TextDirection writingDirection) {
		this.writingDirection = writingDirection;
	}
	/**
	 * @return Returns the writingOrientation.
	 */
	public TextOrientation getWritingOrientation() {
		return writingOrientation;
	}
	/**
	 * @param writingOrientation The writingOrientation to set.
	 */
	public void setWritingOrientation(TextOrientation writingOrientation) {
		this.writingOrientation = writingOrientation;
	}
	
	@Override
	public HorizontalListElement buildHorizontalListElement() {
		return null;	
	}

    @Override
    protected boolean equalsIgnoreIdAux(ModelElement other) {
        ZoneStart o= (ZoneStart) other;
        return this.columnSkip == o.columnSkip
                && this.height== o.height
                && this.lineSkip== o.lineSkip
                && this.lineWidth == o.lineWidth
                && this.refHorizontalReference== o.refHorizontalReference
                && this.refVerticalReference== o.refVerticalReference
                && this.refx== o.refx
                && this.refy== o.refy
                && this.width== o.width
                && this.writingDirection == o.writingDirection
                && this.writingOrientation == o.writingOrientation
                ;
    }
        
        
}

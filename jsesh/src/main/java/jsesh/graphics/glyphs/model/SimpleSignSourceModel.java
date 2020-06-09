package jsesh.graphics.glyphs.model;

import jsesh.hieroglyphs.graphics.ShapeChar;

/**
 * Simple model for a source of hieroglyphic signs drawing.
 * <p> The navigation system is a bit like the one used in resultsets. That is, there are two special position without associated data, 
 * called beforeFirst and afterLast.
 * <p> In a new SignSource, the position should be "beforeFirst". If we want to point to some data, we should call next() if possible. 
 * <p>
 * Modification : take into account the possibility of an empty source. 
 * @author rosmord
 *
 */
public interface SimpleSignSourceModel {
	/**
	 * Returns the shape associated with the current sign.
	 * <p>Should only be called after next() or previous().
	 * @return the shape associated with the current sign.
	 */
	public ShapeChar getCurrentShape();
	
	/**
	 * Returns the code for this shape, if the source defines it.
	 * @return a code, or the empty string (not null).
	 */
	public String getCurrentCode();
	
	/**
	 * Can we call next().
	 * @return
	 */
	public boolean hasNext();
	
	/**
	 * Can we call previous().
	 * @return
	 */
	public boolean hasPrevious();
	
	/**
	 * Advance to next position.
	 */
	public void next();
	
	/**
	 * Advance to previous position.
	 */
	public void previous();
	
	/**
	 * Move after the last position.
	 * <p> A call to previous() will place us on the last item.
	 */
	public void afterLast();
	
	/**
	 * Move before the first position.
	 */
	public void beforeFirst();
}

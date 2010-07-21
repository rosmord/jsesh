package jsesh.mdcDisplayer.drawingElements;

/**
 * Geometric Informations useful when drawing a sign.
 * Currently implemented by View.
 * 
 * <p> Should probably be done in a standalone class.
 * For the most complex cases, we need information irrelevant in view.
 * 
 * @author rosmord
 *
 */
public interface ViewBox {

	/**
	 * Returns the height the box will have.
	 * @return a float
	 */
	float getHeight();

	/**
	 * Returns the width the box will have.
	 * @return a float
	 */

	float getWidth();

	/**
	 * Returns the xScale.
	 * 
	 * @return float
	 */
	public float getXScale();

	/**
	 * Returns the yScale.
	 * 
	 * @return float
	 */
	public float getYScale();

	
}

package jsesh.render.view;

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
	 * @return a double
	 */
	double getHeight();

	/**
	 * Returns the width the box will have.
	 * @return a double
	 */

	double getWidth();

	/**
	 * Returns the xScale.
	 * 
	 * @return double
	 */
	public double getXScale();

	/**
	 * Returns the yScale.
	 * 
	 * @return double
	 */
	public double getYScale();

	
}

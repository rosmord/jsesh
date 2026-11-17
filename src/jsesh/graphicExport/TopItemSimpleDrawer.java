package jsesh.graphicExport;

import java.awt.Graphics2D;

import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.layout.ViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;

/**
 * Base utility class which can be extended when one needs to draw a specific element.
 * <p> This class is usable to implement all export filter in which some elements are exported as drawings and others are not.
 * 
 * <p> Typical use of this class involves creating a subclass of it with the appropriate methods,
 * and calling drawElement on it, and, if needed, consulting the resulting view and dimensions for layout purposes.
 * 
 * <p> It would probably be better not to keep instances of TopItemSimpleDrawer. 
 * @author rosmord
 */
public abstract class TopItemSimpleDrawer {
	
	private MDCView currentView;
	private float scaledWidth, scaledHeight;
	private double cadratHeight;
	private ViewBuilder viewBuilder;
	private DrawingSpecifications drawingSpecifications;
	
	
	/**
	 * @param cadratHeight
	 */
	protected TopItemSimpleDrawer(ViewBuilder viewBuilder, DrawingSpecifications drawingSpecifications, double cadratHeight) {
		this.viewBuilder= viewBuilder;
		this.drawingSpecifications= drawingSpecifications;
		this.cadratHeight = cadratHeight;
	}

	/**
	 * Utility method to draw a list of model elements.
	 */
	
	public void drawTopItemList(TopItemList list) {
		//		 scale Compute

		double scale = (double) getCadratHeight()
				/ getDrawingSpecifications().getMaxCadratHeight();

		 currentView = getBuilder().buildView(list,getDrawingSpecifications());
		
		if (currentView.getWidth() == 0 || currentView.getHeight() == 0)
			return;

		ViewDrawer drawer = new ViewDrawer();

		// Compute the size
		scaledWidth = (float) (currentView.getWidth() * scale);
		scaledHeight = (float) (currentView.getHeight() * scale);

	
		Graphics2D g = buildGraphics();
		
		g.setColor(getDrawingSpecifications().getBlackColor());
		g.setBackground(getDrawingSpecifications().getBackgroundColor());
		g.scale(scale, scale);
		drawer.draw(g, currentView, getDrawingSpecifications());
		g.dispose();

	}
	
	/**
	 * Utility method to draws a model element.
	 * 
	 * This method uses an auxiliary class, GraphicsGenerator.
	 * @param elt
	 */

	public void drawElement(TopItem elt) {
		TopItemList smallModel = new TopItemList();
		smallModel.addTopItem((TopItem) elt.deepCopy());
		drawTopItemList(smallModel);
	}

	protected abstract Graphics2D buildGraphics();

	public ViewBuilder getBuilder() {
		return viewBuilder;
	}

	public DrawingSpecifications getDrawingSpecifications() {
		return drawingSpecifications;
	}
	
	/**
	 * @param cadratHeight
	 *            The cadratHeight to set.
	 */
	public void setCadratHeight(double cadratHeight) {
		this.cadratHeight = cadratHeight;
	}

	/**
	 * @return Returns the cadratHeight.
	 */
	public double getCadratHeight() {
		return cadratHeight;
	}
	
	/**
	 * @return Returns the currentView.
	 */
	public MDCView getCurrentView() {
		return currentView;
	}
	
	/**
	 * @return Returns the scaledHeight.
	 */
	public float getScaledHeight() {
		return scaledHeight;
	}

	/**
	 * @return Returns the scaledWidth.
	 */
	public float getScaledWidth() {
		return scaledWidth;
	}

}

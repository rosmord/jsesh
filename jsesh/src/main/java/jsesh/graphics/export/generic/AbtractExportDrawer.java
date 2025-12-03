package jsesh.graphics.export.generic;

import java.awt.Graphics2D;

import jsesh.drawingspecifications.JSeshStyle;
import jsesh.drawingspecifications.PaintingSpecifications;
import jsesh.mdc.model.TopItem;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.context.JSeshRenderContext;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.layout.Layout;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.ViewBuilder;

/**
 * Base utility class which can be extended when one needs to draw a specific
 * element.
 * <p>
 * This class is usable to implement all export filter in which some elements
 * are exported as drawings and others are not.
 *
 * <p> It uses the template method pattern.</p>
 *
 * <p>
 * Typical use of this class involves creating a subclass of it with the
 * appropriate methods, calling drawElement on it, and, if needed,
 * consulting the resulting view and dimensions for layout purposes.
 *
 * <p> It would probably be better not to keep instances of AbtractExportDrawer.</p>
 *
 * <p> Note that by default, AbtractExportDrawer assumes that the scale of the
 * device used to draw the items is 1 graphical point = 1 device point.
 * <b>This is fixed in the copy of DrawingSpecifications used by the drawer.</b>
 *
 * @author rosmord
 */
public abstract class AbtractExportDrawer {

    private MDCView currentView;
    private float scaledWidth, scaledHeight;
    private double cadratHeight;
    private JSeshRenderContext renderContext;
    /**
     * Ask for the shading operations to be performed after all drawings.
     */
    private boolean shadeAfter;

    /**
     * @param viewBuilder the builder used to create views.
     * @param renderContext the general drawing specifications
     * @param cadratHeight the actual quadrat height we want.
     */
    protected AbtractExportDrawer(JSeshRenderContext renderContext, double cadratHeight) {
        this.renderContext = renderContext.copy().graphicDeviceScale(1.0).build();        
        this.cadratHeight = cadratHeight;
        this.shadeAfter = true;
    }

    /**
     * Utility method to draw a list of model elements.
     * <p>
     * This method uses buildGraphics to create the drawing device.
     * @param list
     */
    public void drawTopItemList(TopItemList list) {
        JSeshStyle style = renderContext.jseshStyle();

        double scale = (double) getCadratHeight()
                / style.geometry().maxCadratHeight();


        ViewBuilder builder = new ViewBuilder();
        currentView = builder.buildView(list, renderContext);

        if (currentView.getWidth() == 0 || currentView.getHeight() == 0) {
            return;
        }

        ViewDrawer drawer = new ViewDrawer();
        drawer.setShadeAfter(shadeAfter);
        // Compute the size
        scaledWidth = (float) (currentView.getWidth() * scale);
        scaledHeight = (float) (currentView.getHeight() * scale);

        Graphics2D g = buildGraphics();

        g.setColor(style.painting().blackColor());
        g.setBackground(style.painting().backgroundColor());
        g.scale(scale, scale);
        drawer.draw(g, renderContext, currentView);
        g.dispose();
    }

    /**
     * Utility method to draws a model element.
     *
     * <p>
     * This method uses buildGraphics to create the drawing device.
     *
     * @param elt
     */
    public void drawElement(TopItem elt) {
        TopItemList smallModel = new TopItemList();
        smallModel.addTopItem((TopItem) elt.deepCopy());
        drawTopItemList(smallModel);
    }

    /**
     * Method to create the actual drawing device.
     * <p>
     * You need to write it when extending this class.
     * <p>
     * Note that when this method is called, the layout is already done. So one
     * can call getCurrentView, getScaledHeight(), getScaledWidth...
     *
     * @return
     */
    protected abstract Graphics2D buildGraphics();

    /**
     * @param cadratHeight The cadratHeight to set.
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

    /**
     * @return the shadeAfter
     */
    public boolean isShadeAfter() {
        return shadeAfter;
    }

    /**
     * Select if shading should be drawn before or <em>after</em> the signs. If
     * your output can't handle transparency, set to false (defaults to true).
     *
     * @param shadeAfter the shadeAfter to set
     */
    public final void setShadeAfter(boolean shadeAfter) {
        this.shadeAfter = shadeAfter;
    }


    public void setRenderContext(JSeshRenderContext renderContext) {
        this.renderContext = renderContext;
    }

    public JSeshRenderContext getRenderContext() {
        return renderContext;
    }

}

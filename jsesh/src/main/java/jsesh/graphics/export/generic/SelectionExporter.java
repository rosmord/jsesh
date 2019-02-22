/*
 * Created on 5 juil. 2005 by rosmord
 *
 * TODO document the file SelectionExporter.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.graphics.export.generic;

import jsesh.graphics.export.generic.ExportData;
import jsesh.graphics.export.generic.BaseGraphics2DFactory;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.io.IOException;

import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.layout.SimpleViewBuilder;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.PageLayout;
import jsesh.utils.DoubleDimensions;

/**
 * An expert which is able to export manuel de codage data to some kind of
 * graphics.
 *
 * @author rosmord
 *
 */
public class SelectionExporter {

    private final ExportData exportData;

    private BaseGraphics2DFactory graphicsFactory;

    private Color background;

    private boolean clearBeforeDrawing = true;

    private DrawingSpecification actualDrawingSpecifications;

    /**
     * @param exportData
     * @param graphicsFactory
     */
    public SelectionExporter(ExportData exportData, BaseGraphics2DFactory graphicsFactory) {
        this.exportData = exportData;
        this.graphicsFactory = graphicsFactory;
        this.background = Color.WHITE;
    }

    /**
     * @throws IOException
     */
    public void exportSelection() throws IOException {
        // Build the view :
        SimpleViewBuilder builder = new SimpleViewBuilder();
        actualDrawingSpecifications = exportData.getDrawingSpecifications().copy();
        PageLayout pageLayout = actualDrawingSpecifications.getPageLayout();
        pageLayout.setLeftMargin(0f);
        pageLayout.setTopMargin(0f);
        actualDrawingSpecifications.setPageLayout(pageLayout);
        exportZone(builder, exportData.getStart().getIndex(), exportData.getEnd().getIndex());
        actualDrawingSpecifications = null;
    }

    /**
     * Export to a system where pages actually exist.
     * @throws IOException 
     */
    public void exportToPages() throws IOException {
        // Build the view :
        SimpleViewBuilder builder = new SimpleViewBuilder();
        actualDrawingSpecifications = exportData.getDrawingSpecifications().copy();
        actualDrawingSpecifications.setPaged(true);

        int start = 0;
        TopItemList l = exportData.getTopItemList();
        // export all pages.
        while (start < l.getNumberOfChildren()) {
            //          Now, loop through the model to find page limits	
            int end = start + 1;
            while (end < l.getNumberOfChildren() && !(l.getChildAt(end) instanceof PageBreak)) {
                end++;
            }
            exportZone(builder, start, end);
            graphicsFactory.newPage();
            start = end;
        }
        actualDrawingSpecifications = null;
    }

    /**
     * @param builder
     * @param start
     * @param end
     * @throws IOException
     */
    private void exportZone(SimpleViewBuilder builder, int start, int end) throws IOException {
        MDCView view = builder.buildView(exportData.getTopItemList(), start, end, actualDrawingSpecifications);
        graphicsFactory.setDimension(getScaledDimensions(view));
        // Build the graphic file and initialize it :
        Graphics2D g = graphicsFactory
                .buildGraphics();

        g.setColor(actualDrawingSpecifications.getBlackColor());
        g.setBackground(background);
        if (clearBeforeDrawing) {
            g.clearRect(0, 0, (int) getScaledDimensions(view).getWidth() + 1, (int) getScaledDimensions(view).getHeight() + 1);
        }
        g.scale(exportData.getScale(), exportData.getScale());
        // Prepare to draw.
        ViewDrawer drawer = new ViewDrawer();
        drawer.setShadeAfter(false);

        // draw !
        drawer.draw(g, view, actualDrawingSpecifications);
        g.dispose();
        graphicsFactory.writeGraphics();
    }

    /**
     * returns the scaled dimensions for the view.
     *
     * @param view
     * @return the scaled dimensions for the view.
     */
    private Dimension2D getScaledDimensions(MDCView view) {
        double w = view.getWidth() * exportData.getScale();
        double h = view.getHeight() * exportData.getScale();
        //return new Dimension((int) Math.ceil(w), (int) Math.ceil(h));
        return new DoubleDimensions(w, h);
    }

    public BaseGraphics2DFactory getGraphicsFactory() {
        return graphicsFactory;
    }

    public void setGraphicsFactory(BaseGraphics2DFactory graphicsFactory) {
        this.graphicsFactory = graphicsFactory;
    }

    /**
     * Get the background color.
     *
     * @return the background color
     */
    public Color getBackground() {
        return background;
    }

    /**
     * Sets the background color.
     *
     * @param background
     */
    public void setBackground(Color background) {
        this.background = background;
    }

    /**
     * @param transparency
     */
    public void setTransparency(boolean transparency) {
    }

    /**
     * Should the drawing surface be cleared before drawing.
     *
     * @return true if we need to clear first.
     */
    public boolean isClearBeforeDrawing() {
        return clearBeforeDrawing;
    }

    /**
     * Selects if the drawing surface should be cleared before drawing. Usually,
     * this is true for bitmap pictures, and false for vector.
     *
     * @param clearBeforeDrawing the clearBeforeDrawing to set
     */
    public void setClearBeforeDrawing(boolean clearBeforeDrawing) {
        this.clearBeforeDrawing = clearBeforeDrawing;
    }

}

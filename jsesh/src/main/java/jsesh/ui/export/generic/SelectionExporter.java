/*
 * Created on 5 juil. 2005 by rosmord
 *
 * TODO document the file SelectionExporter.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.ui.export.generic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import jsesh.render.style.JSeshStyle;
import jsesh.render.style.PageSpecification;
import jsesh.model.PageBreak;
import jsesh.model.HieroglyphicText;
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.context.JSeshTechRenderContext;
import jsesh.render.draw.PageDecorationDrawer;
import jsesh.render.draw.ViewDrawer;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewBuilder;
import jsesh.utils.datatypes.DoubleDimensions;

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
        ViewBuilder builder = new ViewBuilder();
        JSeshRenderContext renderContext = exportData.getRenderContext()
                .copy().jseshStyle(s -> s.geometry(
                        g -> g.leftMargin(0f).topMargin(0f).withoutPagination()))
                .build();
        exportZone(builder, renderContext, exportData.getStart().getIndex(), exportData.getEnd().getIndex());        
    }

    /**
     * Export to a system where pages actually exist.
     * 
     * @throws IOException
     */
    public void exportToPages() throws IOException {
        // Build the view :
        ViewBuilder builder = new ViewBuilder();
        JSeshRenderContext renderContext = exportData.getRenderContext()
                .copy().jseshStyle(style -> style.options(opts -> opts.paged(true))).build();

        PageSpecification page = renderContext.jseshStyle().geometry().page();
        if (page.isPaginated()) {
            exportPaginatedPages(builder, renderContext, page);
            return;
        }

        int start = 0;
        HieroglyphicText l = exportData.getHieroglyphicText();
        // export all pages.
        while (start < l.getNumberOfChildren()) {
            // Now, loop through the model to find page limits
            int end = start + 1;
            while (end < l.getNumberOfChildren() && !(l.getChildAt(end) instanceof PageBreak)) {
                end++;
            }
            exportZone(builder, renderContext, start, end);
            graphicsFactory.newPage();
            start = end;
        }
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

    // -------------------------------------------------------------------------------------------------
    // Private methods.

    /// Exports a text which is cut into pages by the layout: the whole text is
    /// laid out once, with its pages stacked, and each page is drawn separately.
    private void exportPaginatedPages(ViewBuilder builder, JSeshRenderContext renderContext, PageSpecification page)
            throws IOException {
        JSeshStyle style = renderContext.jseshStyle();
        JSeshTechRenderContext techRenderContext = JSeshTechRenderContext.APPROXIMATIVE_CONTEXT;
        HieroglyphicText text = exportData.getHieroglyphicText();
        // The stacked layout is not the "one page at a time" one.
        JSeshRenderContext stacked = renderContext.copy()
                .jseshStyle(s -> s.options(o -> o.paged(false))).build();
        MDCView view = builder.buildView(text, 0, text.getNumberOfChildren(), stacked, techRenderContext);
        int pages = page.pageCount(view.getHeight());
        double scale = exportData.getScale();
        for (int i = 0; i < pages; i++) {
            graphicsFactory.setDimension(new DoubleDimensions(page.pageWidth() * scale, page.pageHeight() * scale));
            Graphics2D g2d = graphicsFactory.buildGraphics();
            g2d.setColor(style.painting().blackColor());
            g2d.setBackground(background);
            if (clearBeforeDrawing) {
                g2d.clearRect(0, 0, (int) (page.pageWidth() * scale) + 1, (int) (page.pageHeight() * scale) + 1);
            }
            g2d.scale(scale, scale);
            g2d.clip(new Rectangle2D.Double(0, 0, page.pageWidth(), page.pageHeight()));
            g2d.translate(0, -i * (double) page.pageStride());
            ViewDrawer drawer = new ViewDrawer();
            drawer.setShadeAfter(false);
            drawer.draw(g2d, renderContext, techRenderContext, view);
            // Page number and title, in the margins.
            g2d.setColor(style.painting().blackColor());
            PageDecorationDrawer.draw(g2d, style, i + 1, i * (double) page.pageStride());
            g2d.dispose();
            graphicsFactory.writeGraphics();
            graphicsFactory.newPage();
        }
    }

    /**
     * @param builder
     * @param start
     * @param end
     * @throws IOException
     */
    private void exportZone(ViewBuilder builder, JSeshRenderContext renderContext, int start, int end) throws IOException {
        JSeshStyle style = renderContext.jseshStyle();
        JSeshTechRenderContext techRenderContext = JSeshTechRenderContext.APPROXIMATIVE_CONTEXT;
        MDCView view = builder.buildView(exportData.getHieroglyphicText(), start, end, renderContext, techRenderContext);
        graphicsFactory.setDimension(getScaledDimensions(view));
        // Build the graphic file and initialize it :
        Graphics2D g2d = graphicsFactory
                .buildGraphics();

        g2d.setColor(style.painting().blackColor());
        g2d.setBackground(background);
        if (clearBeforeDrawing) {
            g2d.clearRect(0, 0, (int) getScaledDimensions(view).getWidth() + 1,
                    (int) getScaledDimensions(view).getHeight() + 1);
        }
        g2d.scale(exportData.getScale(), exportData.getScale());
        // Prepare to draw.
        ViewDrawer drawer = new ViewDrawer();
        drawer.setShadeAfter(false);

        // draw !
        drawer.draw(g2d, renderContext, techRenderContext, view);
        g2d.dispose(); // In some cases, dispose will add some metadata to the graphical file (e.g. for EMF).
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
        return new DoubleDimensions(w, h);
    }

}

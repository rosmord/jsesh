/*
 * Created on 5 juil. 2005 by rosmord
 *
 * TODO document the file SelectionExporter.java
 * 
 * This file is distributed along the GNU Lesser Public License (LGPL)
 * author : rosmord
 */
package jsesh.graphicExport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.io.IOException;

import jsesh.graphics.generic.BaseGraphics2D;
import jsesh.graphics.generic.BaseGraphics2DException;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.draw.MDCCaret;
import jsesh.mdcDisplayer.draw.SimpleViewBuilder;
import jsesh.mdcDisplayer.draw.ViewDrawer;
import jsesh.mdcDisplayer.draw.ViewPrinter;
import jsesh.mdcDisplayer.mdcView.MDCView;

/**
 * @author rosmord
 * 
 * TODO document the type SelectionExporter
 */
public class SelectionExporter {

    private ExportData exportData;

    private BaseGraphics2DFactory graphicsFactory;

    private Color background;
    
    private boolean transparency;
    
    /**
     * @param exportData
     * @param gf
     */
    public SelectionExporter(ExportData exportData, BaseGraphics2DFactory gf) {
        this.exportData = exportData;
        this.graphicsFactory = gf;
        this.background= Color.WHITE;
        this.transparency= false;
    }

    /**
     * @throws IOException
     */
    public void exportSelection() throws IOException {
        // Build the view :
        SimpleViewBuilder builder = new SimpleViewBuilder();
        builder.setDrawingSpecifications(exportData.getDrawingSpecifications().copy());
        builder.getDrawingSpecifications().setLeftMargin(0f);
        builder.getDrawingSpecifications().setTopMargin(0f);        
        MDCCaret caret = exportData.getCaretProvider().getCaret();
        exportZone(builder,caret.getMin(), caret.getMax());
        /*
        MDCView view = builder.buildView(exportData.getData().getModel(), caret
                .getMin(), caret.getMax());

        graphicsFactory.setDimension(getScaledDimensions(view));
        // Build the wmf file and initialize it :
        BaseGraphics2D g = graphicsFactory
                .buildGraphics();

        //g.setBackground(Color.WHITE);
        g.setBackground(background);
        g.clearRect(0,0,(int)getScaledDimensions(view).getWidth(), (int)getScaledDimensions(view).getHeight());
        g.scale(exportData.getScale(), exportData.getScale());
        // Prepare to draw.
        ViewDrawer drawer = new ViewDrawer();
        drawer.setShadeAfter(false);

        // draw !
        drawer.draw(g, view, exportData.getDrawingSpecifications());
        g.dispose();*/        
    }


	public void exportToPages() throws IOException {
	    // Build the view :
        SimpleViewBuilder builder = new SimpleViewBuilder();
        builder.setDrawingSpecifications(exportData.getDrawingSpecifications().copy());
        
        
        int start=0;
        TopItemList l= exportData.getData().getModel();
        // export all pages.
        while (start < l.getNumberOfChildren()) {
        	//          Now, loop through the model to find page limits	
            int end= start + 1;
            while (end < l.getNumberOfChildren() && ! (l.getChildAt(end) instanceof PageBreak))
            	end++;
            exportZone(builder, start, end);        
   
            start= end;
        }		
	}

	/**
	 * @param builder
	 * @param start
	 * @param end
	 * @throws IOException
	 */
	private void exportZone(SimpleViewBuilder builder, int start, int end) throws IOException {
		MDCView view = builder.buildView(exportData.getData().getModel(), start, end);
		graphicsFactory.setDimension(getScaledDimensions(view));
		// Build the graphic file and initialize it :
		BaseGraphics2D g = graphicsFactory
		        .buildGraphics();

		//g.setBackground(Color.WHITE);
		g.setBackground(background);
		g.clearRect(0,0,(int)getScaledDimensions(view).getWidth(), (int)getScaledDimensions(view).getHeight());
		g.scale(exportData.getScale(), exportData.getScale());
		// Prepare to draw.
		ViewDrawer drawer = new ViewDrawer();
		drawer.setShadeAfter(false);

		// draw !
		drawer.draw(g, view, exportData.getDrawingSpecifications());
		g.dispose();
	}
	
    public void exportToPagesOLD(Dimension dimensions) throws IOException {

        PageFormat pageFormat = new PageFormat();
        Paper p = new Paper();
        p.setSize(dimensions.getWidth(), dimensions.getHeight());
        p.setImageableArea(0, 0, dimensions.getWidth(), dimensions.getHeight());
        pageFormat.setPaper(p);

        ViewPrinter printer = new ViewPrinter();
        printer.setModel(exportData.getData().getModel());
       
        //printer.setPageformat(pageFormat);
        printer.setDrawingSpecifications(exportData.getDrawingSpecifications());

        Dimension dimension = new Dimension((int) pageFormat
                .getImageableWidth(), (int) pageFormat.getImageableHeight());

        graphicsFactory.setDimension(dimension);
        BaseGraphics2D g = graphicsFactory.buildGraphics();
        g.scale(exportData.getScale(), exportData.getScale());
        for (int i = 0; i < printer.getNumberOfPages(); i++) {
            try {
            	printer.setPageformat(null);
            	PageFormat currentPageFormat= printer.getPageFormat(i);
            	Dimension d= new Dimension((int)currentPageFormat.getWidth(), (int)currentPageFormat.getHeight());
                g.openPage(d, "page " + (i + 1));
                printer.print(g, pageFormat, i);
            } catch (PrinterException e2) {
                e2.printStackTrace();
            } catch (BaseGraphics2DException e1) {
                e1.printStackTrace();
            } finally {
                try {
                    g.closePage();
                } catch (BaseGraphics2DException e3) {
                    e3.printStackTrace();
                }
            }

        }
        g.dispose();       
    }

    /**
     * returns the scaled dimensions for the view.
     * 
     * @param view
     * @return the scaled dimensions for the view.
     */
    private Dimension getScaledDimensions(MDCView view) {
        double w = view.getWidth() * exportData.getScale();
        double h = view.getHeight() * exportData.getScale();
        return new Dimension((int) Math.ceil(w), (int) Math.ceil(h));
    }

    public BaseGraphics2DFactory getGraphicsFactory() {
        return graphicsFactory;
    }

    public void setGraphicsFactory(BaseGraphics2DFactory graphicsFactory) {
        this.graphicsFactory = graphicsFactory;
    }
    
    /**
     * Get the background color.
     * @return the background color
     */
    public Color getBackground() {
        return background;
    }
    
    /**
     * Sets the background color.
     * @param background
     */
    public void setBackground(Color background) {
        this.background = background;
    }

    /**
     * @param transparency
     */
    public void setTransparency(boolean transparency) {
        this.transparency= transparency;        
    }

}

/*
 * Created on 14 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdcDisplayer.draw;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.layout.MDCPagedEditorKit;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;

/**
 * 
 * Expert for printing pages.
 * @deprecated
 * @author rosmord
 */
public class ViewPrinter implements Pageable, Printable {

	private TopItemList model;

	private ViewDrawer drawer;

	private PagedViewBuilder builder;

	private MDCView currentView = null;
	
	private DrawingSpecifications drawingSpecifications;

	/**
	 * Page format for drawing to a printer of similar device.
	 */

	private PageFormat pageformat;

	public ViewPrinter() {
		MDCPagedEditorKit kit = MDCPagedEditorKit.getDefaultMDCPagedEditorKit();
		pageformat = null;
		drawer = new ViewDrawer();
		drawer.setPaged(true);
		builder = new PagedViewBuilder();
		model = null;
		drawingSpecifications= new DrawingSpecifications();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.Pageable#getNumberOfPages()
	 */

	public int getNumberOfPages() {
		return getView().getNumberOfSubviews();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.Pageable#getPageFormat(int)
	 */
	public PageFormat getPageFormat(int pageIndex)
			throws IndexOutOfBoundsException {
		if (pageformat == null) {			
			MDCView v= getPageView(pageIndex);
			if (v!= null) {
				PageFormat format= new PageFormat();
				format.getPaper().setSize(v.getWidth(), v.getHeight());
				format.getPaper().setImageableArea(0, 0, v.getWidth(), v.getHeight());
				return format;
			}
		}
		return pageformat;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.Pageable#getPrintable(int)
	 */
	public Printable getPrintable(int pageIndex)
			throws IndexOutOfBoundsException {
		if (pageIndex < 0 || pageIndex >= getNumberOfPages())
			throw new IndexOutOfBoundsException("bad page index " + pageIndex);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.print.Printable#print(java.awt.Graphics,
	 *      java.awt.print.PageFormat, int)
	 */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {

		builder.getPagedLayout().setPageFormat(pageFormat);
		// From experience, view building is not very long when compared to
		// printing.
		// So, we can build a complete view for each page.

		MDCView pageView = getPageView(pageIndex);
		if (pageView != null) {
			graphics.translate((int) pageFormat.getImageableX(),
					(int) pageFormat.getImageableY());

			drawer.draw((Graphics2D) graphics, pageView, getDrawingSpecifications());
		} else
			return Printable.NO_SUCH_PAGE;

		return Printable.PAGE_EXISTS;
	}

	/**
	 * Builds a view for printing.
	 * <P>
	 * we create a view just for printing purposes, because it's more than
	 * likely that an already existing view doesn't take into account notions
	 * such as page dimensions, nor the actual available fonts for the device
	 * used.
	 * 
	 * @return the view we have built.
	 */

	private MDCView getView() {
		if (currentView == null) {
			currentView = builder.buildView(model,getDrawingSpecifications());
		}
		return currentView;

	}

	private MDCView getPageView(int pageIndex) {
		MDCView view = getView();
		if (pageIndex >= getNumberOfPages()) {
			currentView = null;
			return null;
		}
		return view.getSubView(pageIndex);
	}

	
	/**
	 * @return DrawingSpecifications
	 */
	public DrawingSpecifications getDrawingSpecifications() {
		return drawingSpecifications;
	}

	/**
	 * @return the model to draw.
	 */
	public TopItemList getModel() {
		return model;
	}

	/**
	 * @return the page format.
	 */
	public PageFormat getPageformat() {
		// FIXME : store page format in builder.
		return pageformat;
	}

	/**
	 * @param specification
	 */
	public void setDrawingSpecifications(DrawingSpecifications specification) {
		this.drawingSpecifications= drawingSpecifications; 
	}

	/**
	 * @param list
	 */
	public void setModel(TopItemList list) {
		model = list;
	}

	/**
	 * @param format
	 */
	public void setPageformat(PageFormat format) {
		pageformat = format;
		builder.getPagedLayout().setPageFormat(pageformat);
	}

}
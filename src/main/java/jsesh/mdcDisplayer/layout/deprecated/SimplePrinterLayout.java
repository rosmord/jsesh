/*
 * Created on 14 oct. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdcDisplayer.layout.deprecated;

import java.awt.print.PageFormat;
import java.util.ArrayList;

import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.layout.SimpleLayout;
import jsesh.mdcDisplayer.mdcView.MDCView;

/**
 * Layouter specialized for printing.
 * Will be suppressed, as its capabilities will be moved to SimpleLayout.
 * @author rosmord
 * @deprecated
 */
public class SimplePrinterLayout extends SimpleLayout implements PagedLayout
{

	private PageFormat pageFormat;

	/**
	 * Build a SimplePrinterLayout.
	 */
	public SimplePrinterLayout()
	{
		super();
		pageFormat = null;
	}

	/**
	 * Layout the list of items.
	 * IMPORTANT : change SimplePrinterLayout. We should probably incorporate it in the "normal" SimpleLayout.
	 * @param t
	 */

	public void visitTopItemList(TopItemList t)
	{
		double pageWidth= 0;
		double pageHeight= 0;
		
		// The page total dimensions
		if (pageFormat != null) {
		 pageWidth = pageFormat.getImageableWidth();
		 pageHeight = pageFormat.getImageableHeight();
		}
		
		// List of views representing the pages.
		ArrayList pages = new ArrayList();

		// List of views for the currentLine.
		ArrayList currentLineView = new ArrayList();

		float currentLineWidth = 0f;
		float currentlineHeight = 0f;
		float currentPageHeight = 0f;


		MDCView v = null;

		MDCView currentPageView = new MDCView(null);

		// To be sure that all views are flushed into a page,
		// we use a small trick : we add an artificial last view, representing a page break.
		// The artificial last new page created is empty, so we just forget it.
		
		for (int i= 0; i<= currentView.getNumberOfSubviews(); i++)
		{
			boolean flushPage = false;
			boolean flushLine = false;

			if (i== currentView.getNumberOfSubviews()) {
				v = new MDCView(new PageBreak());
			} else {
				v= currentView.getSubView(i);
			}
			// Base choice depending on model element :

			if (v.getModel() instanceof LineBreak)
				flushLine = true;

			if (v.getModel() instanceof PageBreak) {
				flushPage = true;
				flushLine = true;
			}

			// Test if the element fits on the line
			if (pageFormat != null && currentLineWidth + v.getWidth() + v.getPosition().x > pageWidth)
			{
				flushLine = true;
			}

			// Test if the line fit on this page.
			// (implicit page skip)
			if (pageFormat != null && flushLine
				&& currentPageHeight + currentlineHeight > pageHeight)
			{
				// We add the current page to the list, and we start a new one :
				pages.add(currentPageView);
				currentPageView = new MDCView(null);
				currentPageHeight = 0;				
			}

			if (flushLine)
			{
				// We add the current line to the page :
				for (int k = 0; k < currentLineView.size(); k++)
				{
					currentPageView.add((MDCView) currentLineView.get(k));
				}
				// We clear the line : 
				currentLineView.clear();
				// We change the page height : 
				currentPageHeight += currentlineHeight
					+ drawingSpecifications.getLineSkip();

			
				

				// For printing, we consider the element as the first of its line.
				/*v.getStartPoint().setValues(
					0,
					RelativePosition.WEST,
					currentlineHeight + drawingSpecifications.getLineSkip(),
					RelativePosition.PREVIOUS);*/
				// We reset the dimensions  :
				currentLineWidth = 0;					
				currentlineHeight = 0;
			}

			// Explicit page skip.
			if (flushPage)
			{
				// We add the current page to the list, and we start a new one :
				pages.add(currentPageView);
				currentPageView = new MDCView(null);
				currentPageHeight = 0;
			}

			// In all cases, we add the current item to the current line.

			if (v.getWidth() != 0)
			{
				// Next element position.
				/*v.getNextViewPosition().setValues(
					v.getWidth() + drawingSpecifications.getSmallSkip(),
					RelativePosition.PREVIOUS,
					0,
					RelativePosition.PREVIOUS);
				// Update data :
				currentLineWidth += v.getWidth()
					+ drawingSpecifications.getSmallSkip();*/
			} else
			{
				/*v.getNextViewPosition().clear();*/
			}
			if (v.getHeight() > currentlineHeight)
				currentlineHeight = v.getHeight();

			// Add to current line :
			currentLineView.add(v);
		}
		
		// Now, all real pages's views are in pages. 
		currentView.clear();
		
		for (int i= 0; i< pages.size(); i++) {
			currentView.add((MDCView) pages.get(i));
		}
		
		currentView.setWidth((float) pageWidth);
		currentView.setHeight((float) pageHeight);
	}

	/**
	 * @param format
	 */
	public void setPageFormat(PageFormat format)
	{
		pageFormat = format;
	}

}

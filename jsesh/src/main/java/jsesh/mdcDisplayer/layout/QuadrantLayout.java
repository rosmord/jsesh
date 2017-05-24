package jsesh.mdcDisplayer.layout;

import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.Cadrat;
import jsesh.mdc.model.Hieroglyph;
import jsesh.mdc.model.HorizontalListElement;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.mdcView.ViewIterator;
import jsesh.mdcDisplayer.mdcView.ViewSide;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

/**
 * Expert for laying out a Cadrat content.
 * 
 * Not really needed yet from an OO point of view (well, it would, but in this
 * case the structure of the class would probably be different).
 * 
 * But really needed because the Cadrat layout is tricky and full of special
 * cases. Moving the corresponding code to a specific class simplifies the
 * understanding of the process.
 * 
 * @author rosmord
 */

public class QuadrantLayout {

	// Note : drawingSpecifications, centerSigns, and currentTextOrientation
	// should probably be stored in an object. "LayoutSpecification" might
	// be a good class name for this one.

	private DrawingSpecification drawingSpecifications;
	private MDCView currentView;
	private boolean centerSigns;
	private TextOrientation currentTextOrientation;

	public QuadrantLayout(DrawingSpecification drawingSpecifications,
			boolean centerSigns, TextOrientation currentTextOrientation) {
		super();
		this.drawingSpecifications = drawingSpecifications;
		this.centerSigns = centerSigns;
		this.currentTextOrientation = currentTextOrientation;
	}

	// WE MUST REALLY DO SOMETHING PRETTIER... THIS CODE IS UGLY...

	public void layout(MDCView currentView, Cadrat c) {
		this.currentView = currentView;
		ViewIterator i;
		currentView.setYStretchable(true);
		currentView.setXStretchable(true);

		// Cadrats's dimensions depend on the context.

		/*
		 * Do we set cadrats to their maximal width. This should be set in the
		 * toplevel of columns.
		 */

		boolean honourMaxWidth;

		/*
		 * Do we set cadrats to their maximal height. This should be set in the
		 * toplevel of lines. If the cadrat is bigger, it will grow smaller. If
		 * it's smaller, it will be left as such (for smaller cadrats, the
		 * "centering" attribute is the key).
		 */
		boolean honourMaxHeight;

		if (currentTextOrientation.isHorizontal()) {
			honourMaxHeight = true;
			honourMaxWidth = false;
		} else {
			honourMaxHeight = false;
			honourMaxWidth = true;
		}

		// See if the cadrat is a sub-cadrat

		boolean embedded = c.isEmbedded();

		if (embedded) {
			honourMaxHeight = false;
			honourMaxWidth = false;
		}

		float maxWidth = drawingSpecifications.getMaxCadratWidth();

		// First, we make sure that no hbox is too wide.
		// Except when there is only one hbox ; in this case,
		// the cadrat is "flat" and it's better not to scale it.
		// We don't scale, either, if one of the hboxes contains
		// a "large" sign (a sign which can grow horizontally).

		boolean wide = c.isWide();

		if ((honourMaxWidth)
				|| (currentView.getNumberOfSubviews() != 1 && !wide)) {
			i = currentView.iterator();
			while (i.hasNext()) {
				i.next().scaleForMaxWidth(maxWidth);
			}
		}

		// Streched cadrats
		// Scale lines so that they fit the limits.
		if (wide) {
			float maxw;
			if (honourMaxWidth)
				maxw = maxWidth;
			else
				maxw = currentView.getMaximalWidthOfSubView();
			for (int k = 0; k < currentView.getNumberOfSubviews(); k++) {
				if (c.getHBox(k).isWide()) {
					currentView.getSubView(k).scaleToWidth(maxw);
				}
			}
		} else {
			// Keep the current width as the cadrat width,
			// but scale wide lone signs so that they will fill their line.
			// This is useful in columns, because you want, for instance, a
			// "n" alone on a line to fill the whole line.
			float width = currentView.getMaximalWidthOfSubView();
			if (honourMaxWidth && (!embedded) && width < maxWidth) {
				width = maxWidth;
			}

			fitLoneSignsToColumnWidth(c, width);
		}

		// CADRAT HEIGHT
		// A cadrat is a list of horizontal boxes.
		// Its height is basically the sum of the heights of these boxes.
		float h = currentView.getSumOfSubViewsHeights();
		// add the interline spaces
		h += (currentView.getNumberOfSubviews() - 1)
				* drawingSpecifications.getSmallSkip();

		// If the cadrat is not high enough, we add some space to set its height
		// (if it's too large, it will be scaled anyway).
		// Note :
		// This should be done in a number of cases :
		// a) generally in line composition

		if ((honourMaxHeight) && h < drawingSpecifications.getMaxCadratHeight())
			h = drawingSpecifications.getMaxCadratHeight();

		currentView.setHeight(h);

		// CADRAT WIDTH
		// We now proceed to the actual cadrat's width (after fixing the
		// height).
		// Compute "natural" width
		float w = currentView.getMaximalWidthOfSubView();

		if (honourMaxWidth && (!embedded) && w < maxWidth) {
			w = maxWidth;
		}

		currentView.setWidth(w);

		// We redistribute views.
		if (currentView.getNumberOfSubviews() > 1) {
			currentView.distributeFromTopToBottom();
		} else {
			if (centerSigns)
				currentView.centerSubViewsVertically();
			else
				currentView.stickSubViewsTo(ViewSide.BOTTOM);
		}

		// Ok. Now, we are going to modify the hboxes' views so
		// that the cadrats are squared.
		i = currentView.iterator();

		while (i.hasNext()) {
			MDCView sub = i.next();
			if (sub.getWidth() < w) {
				// enlarge the box.
				sub.setWidth(w);
			}
			if (sub.getNumberOfSubviews() == 1) {
				// Currently, we simply center the element.
				sub.centerSubViewHorizontally();
			} else {
				sub.distributeHorizontally();
				// We align the bottom lines of subviews.
				sub.stickSubViewsTo(ViewSide.BOTTOM);
			}
		}
		// Now, if needed, we scale the cadrat :
		if (honourMaxHeight && h > drawingSpecifications.getMaxCadratHeight()) {
			currentView.reScale(drawingSpecifications.getMaxCadratHeight() / h);
		}
	}


	private void fitLoneSignsToColumnWidth(Cadrat c, float width) {
		for (int k = 0; k < currentView.getNumberOfSubviews(); k++) {
			// If there is only one element in the HBox...
			if (currentView.getSubView(k).getSubViews().size() == 1) {
				// get the element
				HorizontalListElement elt = c.getHBox(k)
						.getHorizontalListElementAt(0);
				// If the element contains only one sign...
				if (elt.containsOnlyOneSign()) {
					Hieroglyph hiero = elt.getLoneSign();
					MDCView subSubView = currentView.getSubView(k)
							.getSubView(0);
					// Ok. Now we have the lone sign. Is it an unscaled wide
					// narrow sign ?
					// (large "square" signs can probably be small and
					// centered).
					if (hiero.getRelativeSize() == 100
							&& subSubView.getInternalWidth() >= drawingSpecifications
									.getLargeSignSizeRatio()
									* drawingSpecifications
											.getHieroglyphsDrawer()
											.getHeightOfA1()
							&& subSubView.getInternalHeight() <= drawingSpecifications
									.getSmallSignSizeRatio()
									* drawingSpecifications
											.getHieroglyphsDrawer()
											.getHeightOfA1()) {
						// The sign is a narrow broad sign. Good. Enlarge
						// it, and enlarge the hbox view.
						subSubView.reScale(width / subSubView.getWidth());
						currentView.getSubView(k).fitToSubViews(true);
					}
				}
			}
		}

	}

}

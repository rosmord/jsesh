package jsesh.render.layout;

import java.util.List;

import jsesh.render.style.JSeshStyle;
import jsesh.model.constants.KerningAxis;
import jsesh.model.constants.TextOrientation;
import jsesh.model.Cadrat;
import jsesh.model.Hieroglyph;
import jsesh.model.HorizontalListElement;
import jsesh.render.elements.HieroglyphDrawer;
import jsesh.render.layout.insert.Kerner;
import jsesh.render.view.MDCView;
import jsesh.render.view.ViewIterator;
import jsesh.render.view.ViewSide;

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

public class QuadratLayout {

	// Note : drawingSpecifications, centerSigns, and currentTextOrientation
	// should probably be stored in an object. "LayoutSpecification" might
	// be a good class name for this one.

	private JSeshStyle jseshStyle;
	private MDCView currentView;
	private boolean centerSigns;
	private TextOrientation currentTextOrientation;
	private HieroglyphDrawer hieroglyphsDrawer;
	private Kerner kerner;

	public QuadratLayout(JSeshStyle jseshStyle, HieroglyphDrawer hieroglyphsDrawer, Kerner kerner,
			boolean centerSigns, TextOrientation currentTextOrientation) {
		super();
		this.jseshStyle = jseshStyle;
		this.hieroglyphsDrawer = hieroglyphsDrawer;
		this.kerner = kerner;
		this.centerSigns = centerSigns;
		this.currentTextOrientation = currentTextOrientation;
	}

	// WE MUST REALLY DO SOMETHING PRETTIER... THIS CODE IS UGLY...

	public void layout(MDCView currentView, Cadrat c) {
		this.currentView = currentView;
		if (c.getKerningAxis().isPresent()) {
			layoutKerningOnly(c);
			return;
		}
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

		float maxWidth = jseshStyle.geometry().maxCadratWidth();

		// First, we make sure that no hbox is too wide.
		// Except when there is only one hbox ; in this case,
		// the cadrat is "flat" and it's better not to scale it.
		// We don't scale, either, if one of the hboxes contains
		// a "large" sign (a sign which can grow horizontally).

		boolean wide = c.isWide();

		// Lines made only of kerning operators (as in p:%vkern:q) don't count.
		int lineCount = 0;
		for (int k = 0; k < c.getNumberOfHBoxes(); k++) {
			if (c.getHBox(k).getKerningAxis().isEmpty()) {
				lineCount++;
			}
		}
		boolean kerningLines = lineCount != c.getNumberOfHBoxes();

		if ((honourMaxWidth)
				|| (lineCount != 1 && !wide)) {
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

		if (kerningLines) {
			layoutWithKerningLines(c, honourMaxWidth, honourMaxHeight, embedded, maxWidth);
		} else {
			// CADRAT HEIGHT
			// A cadrat is a list of horizontal boxes.
			// Its height is basically the sum of the heights of these boxes.
			float h = currentView.getSumOfSubViewsHeights();
			// add the interline spaces
			h += (currentView.getNumberOfSubviews() - 1)
					* jseshStyle.geometry().smallSkip();

			// If the cadrat is not high enough, we add some space to set its height
			// (if it's too large, it will be scaled anyway).
			// Note :
			// This should be done in a number of cases :
			// a) generally in line composition

			if ((honourMaxHeight) && h < jseshStyle.geometry().maxCadratHeight())
				h = jseshStyle.geometry().maxCadratHeight();

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

			layoutLines(c, w);
		}
		// Now, if needed, we scale the cadrat :
		float h = currentView.getHeight();
		if (honourMaxHeight && h > jseshStyle.geometry().maxCadratHeight()) {
			currentView.reScale(jseshStyle.geometry().maxCadratHeight() / h);
		}
	}

	/// Places the elements of each line of the quadrat, once its width is known.
	private void layoutLines(Cadrat c, float w) {
		for (int k = 0; k < currentView.getNumberOfSubviews(); k++) {
			MDCView sub = currentView.getSubView(k);
			if (sub.getWidth() < w) {
				// enlarge the box.
				sub.setWidth(w);
			}
			if (c.getHBox(k).containsKerning()) {
				// Kerned elements are moved together; vertical positions are already set.
				KerningPacker packer = packer(KerningAxis.HORIZONTAL);
				List<List<Integer>> blocks = packer.blocks(sub);
				if (blocks.size() > 1) {
					packer.distribute(sub, blocks, sub.getInternalWidth());
				} else if (!blocks.isEmpty()) {
					List<Integer> block = blocks.get(0);
					packer.moveBlockTo(sub, block, (sub.getInternalWidth() - packer.blockLength(sub, block)) / 2);
				}
			} else if (sub.getNumberOfSubviews() == 1) {
				// Currently, we simply center the element.
				sub.centerSubViewHorizontally();
			} else {
				sub.distributeHorizontally();
				// We align the bottom lines of subviews.
				sub.stickSubViewsTo(ViewSide.BOTTOM);
			}
		}
	}

	/// Lays out a quadrat with lines made only of kerning operators, as in `p:%vkern:q`.
	///
	/// Such lines take no room. A `%vkern` line kerns the lines around it, which then
	/// move together; they are only known once the content of the lines is placed.
	private void layoutWithKerningLines(Cadrat c, boolean honourMaxWidth, boolean honourMaxHeight,
			boolean embedded, float maxWidth) {
		float w = currentView.getMaximalWidthOfSubView();
		if (honourMaxWidth && (!embedded) && w < maxWidth) {
			w = maxWidth;
		}
		currentView.setWidth(w);
		layoutLines(c, w);

		for (MDCView sub : currentView.getSubViews()) {
			sub.getPosition().x = 0;
		}
		KerningPacker packer = packer(KerningAxis.VERTICAL);
		float h = (float) packer.pack(currentView);
		if (honourMaxHeight && h < jseshStyle.geometry().maxCadratHeight()) {
			h = jseshStyle.geometry().maxCadratHeight();
		}
		currentView.setHeight(h);

		List<List<Integer>> blocks = packer.blocks(currentView);
		if (blocks.size() > 1) {
			packer.distribute(currentView, blocks, currentView.getInternalHeight());
		} else if (!blocks.isEmpty()) {
			List<Integer> block = blocks.get(0);
			double free = currentView.getInternalHeight() - packer.blockLength(currentView, block);
			packer.moveBlockTo(currentView, block, centerSigns ? free / 2 : free);
		}
	}

	/// Lays out a quadrat made only of kerning operators, as in `p-%hkern-q`.
	///
	/// It takes no room along the text, but at the top level it gets the size of a quadrat
	/// across it, so that kerning marks can be drawn in the middle of the line.
	private void layoutKerningOnly(Cadrat c) {
		float w = 0, h = 0;
		if (!c.isEmbedded()) {
			if (currentTextOrientation.isHorizontal()) {
				h = jseshStyle.geometry().maxCadratHeight();
			} else {
				w = jseshStyle.geometry().maxCadratWidth();
			}
		}
		currentView.setWidth(w);
		currentView.setHeight(h);
		for (MDCView sub : currentView.getSubViews()) {
			sub.getPosition().setLocation(w / 2, h / 2);
		}
	}

	private KerningPacker packer(KerningAxis axis) {
		return new KerningPacker(kerner, axis, jseshStyle.geometry().smallSkip());
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
					// Ok. Now we have a lone sign. Is it an unscaled wide
					// narrow sign ?
					// (large "square" signs can probably be small and
					// centered).
					double heightOfA1 = hieroglyphsDrawer
							.getHeightOfA1();
					if (hiero.getRelativeSize() == 100
							&& subSubView.getInternalWidth() >= jseshStyle
									.geometry().largeSignSizeRatio()

									* heightOfA1
							&& subSubView.getInternalHeight() <= jseshStyle
									.geometry()
									.smallSignSizeRatio()
									* heightOfA1) {
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

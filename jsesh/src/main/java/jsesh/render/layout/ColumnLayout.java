/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 23 d�c. 2004
 *
 */
package jsesh.render.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import jsesh.render.style.JSeshStyle;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.model.LineBreak;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementAdapter;
import jsesh.model.PageBreak;
import jsesh.model.TabStop;
import jsesh.render.view.MDCView;

/**
 * Expert for laying out text organised in columns.
 * 
 * <p>
 * We suppose that:
 * <ul>
 * <li>Apart from the first element, the others can be placed absolutely.
 * <li>
 * </ul>
 * General organization of the code :
 * 
 * 
 * @author Serge Rosmorduc
 */

public class ColumnLayout extends TopItemLayout {

	MDCView documentView;

	Rectangle2D documentArea;

	Point2D insertionPoint;

	Zone zone;

	Point2D.Double zoneStart;

	JSeshStyle jseshStyle;

	TextDirection currentTextDirection;

	// End of zone to move

	private MDCView subView;

	private final LayoutAux aux = new LayoutAux();

	/**
	 * @param documentView
	 * @param jseshStyle
	 */
	public ColumnLayout(MDCView documentView,
			JSeshStyle jseshStyle) {
		this.documentView = documentView;
		documentArea = new Rectangle2D.Double();
		insertionPoint = new Point2D.Double();
		this.jseshStyle = jseshStyle;
		currentTextDirection = jseshStyle.options().textDirection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.render.draw.TopItemLayout#layoutElement(jsesh.mdcDisplayer
	 * .mdcView.MDCView)
	 */
	@Override
	public void layoutElement(MDCView subView) {
		this.subView = subView;
		subView.getModel().accept(aux);
		this.subView = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.render.draw.TopItemLayout#endLayout()
	 */
	/// A run of text is one horizontal line in the column (it is not
	/// aligned with the hieroglyphs' baseline).
	@Override
	public void layoutTextRun(TextRun run, List<MDCView> views) {
		boolean mirrored = !currentTextDirection.isLeftToRight();
		double height = 0;
		for (MDCView v : views) {
			v.setDeltaBaseY(0); // No y alignment required !
			height = Math.max(height, v.getHeight());
		}
		zone.addRun(views, run.layoutOffsets(mirrored));
		if (height != 0) {
			zone.moveCurrentPoint(0, height + jseshStyle.geometry().lineSkip());
		}
	}

	@Override
	public void endLayout() {
		if (!zone.isEmpty()) {
			flushZone();
		}
		// add a margin
		documentArea.add(new Point2D.Double(documentArea.getMaxX()
				+ jseshStyle.geometry().leftMargin(),
				documentArea.getMinY()));
	}

	/**
	 * Returns the total area for the document, after computation.
	 * 
	 * @return the area for the complete document.
	 */

	@Override
	public Rectangle2D getDocumentArea() {
		return this.documentArea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.render.draw.TopItemLayout#initState()
	 */
	@Override
	public void startLayout() {
		documentView.reset();
		// Pseudo relative position for first element.
		// Note : this is somehow problematic...
		jseshStyle = jseshStyle.copy()
				.options(opt -> opt.textOrientation(TextOrientation.VERTICAL)).build();
		documentView.setDirection(currentTextDirection);
		zone = createNewZone();
		zoneStart = new Point2D.Double(jseshStyle.geometry()
				.leftMargin(), 0);
	}

	private Zone createNewZone() {
		Zone z = new Zone(0, 0);
		z.moveCurrentPoint(0, jseshStyle.geometry().topMargin());
		return z;
	}

	/**
	 * 
	 */
	private void flushZone() {
		// Compute the start point relative position for this line
		// RIght-to-left is :
		// zoneStart.setValues((float) 0, RelativePosition.WEST,
		// -(float) zone.getMinY(), RelativePosition.PREVIOUS);
		// zoneStart.setValues((float) 0, RelativePosition.WEST,
		// (float) zone.getHeight(), RelativePosition.PREVIOUS);

		// Normally, the zone upper left point is (0,0). It can change if the
		// zone
		// is highter than expected. Correct this position :
		zoneStart.y += -zone.getMinY();

		// Should it change for right-to-left orientation ?
		zoneStart.x += -zone.getMinX();

		// update the zone
		zone.translateBy(zoneStart);

		// flush the zone
		documentArea.add(new Rectangle2D.Double(zoneStart.x, zoneStart.y, zone
				.getWidth(), zone.getHeight()));
	}

	private class LayoutAux extends ModelElementAdapter {
		/**
		 * Called when a new line is met.
		 * <ul>
		 * <li>add the current zone to the page
		 * <li>prepare a new zone.
		 * </ul>
		 * 
		 * @see jsesh.model.ModelElementAdapter#visitLineBreak(jsesh.model.LineBreak)
		 */

		@Override
		public void visitLineBreak(LineBreak b) {
			subView.setHeight(0.1f);
			subView.setWidth(jseshStyle.geometry().maxCadratWidth());
			// Add a minimal line break height to the current zone
			// (else empty lines would be 0 height)
			// Write
			// Add the line break to the current line.
			zone.add(subView);
			flushZone();

			zoneStart.x += zone.getWidth()
					+ jseshStyle.geometry().columnSkip();

			// create a new zone ?
			zone = createNewZone();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.model.ModelElementAdapter#visitPageBreak(jsesh.model
		 * .PageBreak)
		 */
		@Override
		public void visitPageBreak(PageBreak b) {
			// TEMPORARY.
			if (!jseshStyle.options().paged())
				visitLineBreak(new LineBreak());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.model.ModelElementAdapter#visitTabStop(jsesh.model.
		 * TabStop)
		 */
		@Override
		public void visitTabStop(TabStop t) {
			visitDefault(t);
		}

		/**
		 * Lay out generic top items.
		 * 
		 * <ul>
		 * <li>compute next element position
		 * <li>update the current zone shape
		 * </ul>
		 * 
		 * @see jsesh.model.ModelElementAdapter#visitDefault(jsesh.model.ModelElement)
		 */

		@Override
		public void visitDefault(ModelElement t) {
			// add the new view's bounding box to the current column.
			zone.add(subView);
			// Compute the position of the following view.
			// prepare a small skip after v, if necessary
			// the skip is "integrated" in v,
			if (subView.getHeight() != 0) {
				zone.moveCurrentPoint(0, subView.getHeight()
						+ jseshStyle.geometry().lineSkip());
			}
		}
	}

}
/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 23 d�c. 2004
 *
 */
package jsesh.mdcDisplayer.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdc.model.AlphabeticText;
import jsesh.mdc.model.LineBreak;
import jsesh.mdc.model.ModelElement;
import jsesh.mdc.model.ModelElementAdapter;
import jsesh.mdc.model.PageBreak;
import jsesh.mdc.model.TabStop;
import jsesh.mdcDisplayer.mdcView.MDCView;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;

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

	DrawingSpecification drawingSpecifications;

	TextDirection currentTextDirection;

	// End of zone to move

	private MDCView subView;

	private LayoutAux aux = new LayoutAux();

	/**
	 * @param documentView
	 * @param drawingSpecifications
	 */
	public ColumnLayout(MDCView documentView,
			DrawingSpecification drawingSpecifications) {
		super();
		this.documentView = documentView;
		documentArea = new Rectangle2D.Double();
		insertionPoint = new Point2D.Double();
		this.drawingSpecifications = drawingSpecifications;
		currentTextDirection = drawingSpecifications.getTextDirection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jsesh.mdcDisplayer.draw.TopItemLayout#layoutElement(jsesh.mdcDisplayer
	 * .mdcView.MDCView)
	 */
	public void layoutElement(MDCView subView) {
		this.subView = subView;
		subView.getModel().accept(aux);
		this.subView = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.TopItemLayout#endLayout()
	 */
	public void endLayout() {
		if (!zone.isEmpty()) {
			flushZone();
		}
		// add a margin
		documentArea.add(new Point2D.Double(documentArea.getMaxX()
				+ drawingSpecifications.getPageLayout().getLeftMargin(),
				documentArea.getMinY()));
	}

	/**
	 * Returns the total area for the document, after computation.
	 * 
	 * @return the area for the complete document.
	 */

	public Rectangle2D getDocumentArea() {
		return this.documentArea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.mdcDisplayer.draw.TopItemLayout#initState()
	 */
	public void startLayout() {
		documentView.reset();
		// Pseudo relative position for first element.
		drawingSpecifications.setTextOrientation(TextOrientation.VERTICAL);
		documentView.setDirection(currentTextDirection);
		zone = createNewZone();
		zoneStart = new Point2D.Double(drawingSpecifications.getPageLayout()
				.getLeftMargin(), 0);
	}

	private Zone createNewZone() {
		Zone z = new Zone(0, 0);
		z.moveCurrentPoint(0, drawingSpecifications.getPageLayout().getTopMargin());
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
		 * @see jsesh.mdc.model.ModelElementAdapter#visitLineBreak(jsesh.mdc.model.LineBreak)
		 */

		public void visitLineBreak(LineBreak b) {
			subView.setHeight(0.1f);
			subView.setWidth(drawingSpecifications.getMaxCadratWidth());
			// Add a minimal line break height to the current zone
			// (else empty lines would be 0 height)
			// Write
			// Add the line break to the current line.
			zone.add(subView);
			flushZone();

			zoneStart.x += zone.getWidth()
					+ drawingSpecifications.getColumnSkip();

			// create a new zone ?
			zone = createNewZone();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdc.model.ModelElementAdapter#visitPageBreak(jsesh.mdc.model
		 * .PageBreak)
		 */
		public void visitPageBreak(PageBreak b) {
			// TEMPORARY.
			if (!drawingSpecifications.isPaged())
				visitLineBreak(new LineBreak());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.mdc.model.ModelElementAdapter#visitTabStop(jsesh.mdc.model.
		 * TabStop)
		 */
		public void visitTabStop(TabStop t) {
			visitDefault(t);
		}

		/**
		 * Layout alphabetic text.
		 * <p>
		 * Note that alphabetic text is special, because a sequence of element
		 * will always be laid out in a given orientation which depends</em>
		 * only</em> on the text writing system. (currently, always
		 * left-to-right, but if arabic and hebrew are added, this will change).
		 * 
		 * @param t
		 */
		public void visitAlphabeticText(AlphabeticText t) {
			subView.setDeltaBaseY(0); // No y alignment required !
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
		 * @see jsesh.mdc.model.ModelElementAdapter#visitDefault(jsesh.mdc.model.ModelElement)
		 */

		public void visitDefault(ModelElement t) {
			// add the new view's bounding box to the current column.
			zone.add(subView);
			// Compute the position of the following view.
			// prepare a small skip after v, if necessary
			// the skip is "integrated" in v,
			if (subView.getHeight() != 0) {
				zone.moveCurrentPoint(0, subView.getHeight()
						+ drawingSpecifications.getSmallSkip());
			}
		}
	}

}
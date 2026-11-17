/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 23 déc. 2004
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
import jsesh.mdcDisplayer.mdcView.RelativePosition;
import jsesh.mdcDisplayer.preferences.DrawingSpecifications;

/**
 * TEMPORARY FILE. Expert for laying out text organised in columns.
 * <p>
 * Currently works only for left-to-right text.
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

	RelativePosition zoneStart;

	DrawingSpecifications drawingSpecifications;

	TextDirection currentTextDirection;

	RelativePosition nextViewPosition;

	// End of zone to move

	private MDCView subView;

	private LayoutAux aux = new LayoutAux();

	/**
	 * @param documentView
	 * @param drawingSpecifications
	 */
	public ColumnLayout(MDCView documentView,
			DrawingSpecifications drawingSpecifications) {
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
	 * @see jsesh.mdcDisplayer.draw.TopItemLayout#layoutElement(jsesh.mdcDisplayer.mdcView.MDCView)
	 */
	public void layoutElement(MDCView subView) {
		this.subView = subView;
		// Point2D refPoint = state.getReferencePoint();
		// Compute the new reference point ?
		// Don't think so...
		// state.getNextItemPosition().translate(subView.getParent(), subView,
		// refPoint);
		subView.getModel().accept(aux);
		// state.setNextItemPosition(subView.getNextViewPosition());

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

	
			nextViewPosition = new RelativePosition(0, RelativePosition.WEST,
					0, RelativePosition.NORTH);

		zone = new Zone(0, 0);
		zoneStart = documentView.getFirstSubViewPosition();
		if (! currentTextDirection.isLeftToRight())
			zoneStart.setValues(0,RelativePosition.EAST, 0, RelativePosition.NORTH) ;
		else
			zoneStart.setValues(0,RelativePosition.WEST, 0, RelativePosition.NORTH) ;
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

		zoneStart.setDy(zoneStart.getDy() + (float) -zone.getMinY());

		// Should it change for right-to-left orientation ?
		if (currentTextDirection.isLeftToRight()) {
			zoneStart.setDx(zoneStart.getDx() + (float) -zone.getMinX());
		} else {
			//zoneStart.setDx(zoneStart.getDx() +(float) -zone.getWidth());
			zoneStart.setDx(zoneStart.getDx() +(float) -zone.getMaxX());
		}
		// Compute the actual start point for the line
		zoneStart.translate(documentView, insertionPoint);
		// update the zone

		// flush the zone
		documentArea.add(new Rectangle2D.Double(insertionPoint.getX(), 0, zone
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
			// Add a minimal line break height to the current zone
			// (else empty lines would be 0 height)
			// Write
			flushZone();

			if (currentTextDirection.isLeftToRight()) {
				subView.getNextViewPosition().setValues(
						(float) zone.getWidth()
								+ drawingSpecifications.getColumnSkip(),
						RelativePosition.PREVIOUS, 0, RelativePosition.NORTH);
			} else {
				subView.getNextViewPosition().setValues(
						(float) -(drawingSpecifications
								.getColumnSkip()), RelativePosition.PREVIOUS,
						0, RelativePosition.NORTH);
			}

			// create a new zone ?
			zone = new Zone(0, 0);
			zoneStart = subView.getNextViewPosition();
			// Next view position is computed relatively to the zone.
			nextViewPosition = new RelativePosition(0, RelativePosition.WEST,
					0, RelativePosition.NORTH);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementAdapter#visitPageBreak(jsesh.mdc.model.PageBreak)
		 */
		public void visitPageBreak(PageBreak b) {
			// TEMPORARY.
			visitLineBreak(new LineBreak());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jsesh.mdc.model.ModelElementAdapter#visitTabStop(jsesh.mdc.model.TabStop)
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

			// Compute the position of the following view.
			// prepare a small skip after v, if necessary
			// the skip is "integrated" in v,
			if (subView.getWidth() != 0) {
				// IMPORTANT : here difference with right to left code
				subView.getNextViewPosition().setDy(
						subView.getHeight()
								+ drawingSpecifications.getSmallSkip());
			} else
				subView.getNextViewPosition().setDy(0);
			zone.add(subView);
			nextViewPosition = subView.getNextViewPosition();

		}
	}

}
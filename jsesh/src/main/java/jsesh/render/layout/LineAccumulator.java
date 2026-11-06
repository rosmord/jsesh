/*
Copyright Serge Rosmorduc
contributor(s) : Serge J. P. Thomas for the fonts
serge.rosmorduc@qenherkhopeshef.org

This software is a computer program whose purpose is to edit ancient egyptian hieroglyphic texts.

This software is governed by the CeCILL license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.
 */
/*
 * Created on 21 fevr. 2005 by rosmord
 */
package jsesh.render.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import jsesh.render.view.MDCView;
import jsesh.sharedvocabulary.LineAlignment;

/// A line or a column being built by [LineLayout] or [ColumnLayout]: views
/// are added to it, and it keeps track of its size and content.
///
/// It has a size, contains a number of views (with a position), and holds an
/// "insertion" point where new views would be placed. Moving the insertion
/// point is the responsibility of the user of the class.
///
/// Formerly named `Zone`; not to be confused with [jsesh.model.Zone], the
/// `%zone(...)` element of the text.
///
/// @author rosmord
public class LineAccumulator {

	/**
	 * Current insertion position for views in zone.
	 */
	private Point2D.Double currentPoint;

	private Rectangle2D zoneArea;

	private ArrayList<MDCView> views;

	/**
	 * The views, grouped in units which are moved as a whole when the zone
	 * is justified. A unit is usually a single view, but a run of alphabetic
	 * text is one unit.
	 */
	private final ArrayList<List<MDCView>> units = new ArrayList<>();

	/// The view of the line break which ends a column, if any: it gives the
	/// column a minimal size, but is not part of its visible content.
	private MDCView columnEnd;

	/**
	 * build a new zone.
	 * 
	 * @param minWidth
	 * @param minHeight
	 */
	public LineAccumulator(double minWidth, double minHeight) {
		currentPoint = new Point2D.Double();
		zoneArea = new Rectangle2D.Double(0f, 0f, minWidth, minHeight);
		views = new ArrayList<MDCView>();
	}

	/**
	 * Add a view's bounding box to the current zone. All the parameters for
	 * this view should be set. In particular, <em>both</em> the startPoint and
	 * the nextViewPosition should have their values. These values will be
	 * relative to the zone's first point, and all views in the zone will be
	 * translated when the zone is finally added to the document.
	 * 
	 * @param view
	 *            : the view to add.
	 */
	public void add(MDCView view) {
		// Sets the view position
		view.getPosition().setLocation(currentPoint.x + view.getDeltaBaseX(),
				currentPoint.y + view.getDeltaBaseY());
		// Update the zone size
		zoneArea.add(new Rectangle2D.Double(view.getPosition().x, view
				.getPosition().y, view.getWidth(), view.getHeight()));
		views.add(view);
		units.add(List.of(view));
	}

	/// Adds the view of the line break which ends a column. It is placed like
	/// in [#add(MDCView)], but the alignment of the column ignores it (it is
	/// moved with the column, as the line break of a line, which has no
	/// width).
	///
	/// @param view the view of the line break.
	public void addColumnEnd(MDCView view) {
		view.getPosition().setLocation(currentPoint.x + view.getDeltaBaseX(),
				currentPoint.y + view.getDeltaBaseY());
		zoneArea.add(new Rectangle2D.Double(view.getPosition().x, view
				.getPosition().y, view.getWidth(), view.getHeight()));
		views.add(view);
		columnEnd = view;
	}

	/// Adds a view kerned with the previous one: they will be moved together
	/// when the zone is justified.
	///
	/// @param view the view to add; it is placed like in [#add(MDCView)].
	public void addKerned(MDCView view) {
		if (units.isEmpty()) {
			add(view);
			return;
		}
		view.getPosition().setLocation(currentPoint.x + view.getDeltaBaseX(),
				currentPoint.y + view.getDeltaBaseY());
		zoneArea.add(new Rectangle2D.Double(view.getPosition().x, view
				.getPosition().y, view.getWidth(), view.getHeight()));
		views.add(view);
		List<MDCView> last = new ArrayList<>(units.remove(units.size() - 1));
		last.add(view);
		units.add(last);
	}

	/// Adds a run of views which must stay together (a run of alphabetic
	/// text). Justification will move them as one unit.
	///
	/// @param runViews the views.
	/// @param xOffsets the position of each view, relative to the current
	/// point.
	public void addRun(List<MDCView> runViews, double[] xOffsets) {
		for (int k = 0; k < runViews.size(); k++) {
			MDCView view = runViews.get(k);
			view.getPosition().setLocation(currentPoint.x + xOffsets[k] + view.getDeltaBaseX(),
					currentPoint.y + view.getDeltaBaseY());
			zoneArea.add(new Rectangle2D.Double(view.getPosition().x, view
					.getPosition().y, view.getWidth(), view.getHeight()));
			views.add(view);
		}
		units.add(List.copyOf(runViews));
	}

	/**
	 * Computes the size the zone would be if a given view was added to it. Does
	 * not actually add the view.
	 * 
	 * @param view
	 * @return the size the zone would be if it included the view.
	 */
	public Rectangle2D computeNewSizeWith(MDCView view) {
		Rectangle2D newArea = (Rectangle2D) zoneArea.clone();
		// computes the view position
		double x = currentPoint.x + view.getDeltaBaseX();
		double y = currentPoint.y + view.getDeltaBaseY();
		// Update the zone size
		newArea.add(new Rectangle2D.Double(x, y, view.getWidth(), view
				.getHeight()));
		return newArea;
	}

	public double getWidth() {
		return zoneArea.getWidth();
	}

	public double getHeight() {
		return zoneArea.getHeight();
	}

	/**
	 * @return true if zone is empty.
	 */
	public boolean isEmpty() {
		return this.zoneArea.isEmpty();
	}

	public double getMinY() {
		return zoneArea.getMinY();
	}

	public double getMinX() {
		return zoneArea.getMinX();
	}

	public double getMaxX() {
		return zoneArea.getMaxX();
	}

	/**
	 * @return
	 */
	public Point2D getCurrentPoint() {
		return currentPoint;
	}

	/**
	 * @param dx
	 * @param dy
	 */
	public void moveCurrentPoint(double dx, double dy) {
		currentPoint.setLocation(currentPoint.x + dx, currentPoint.y + dy);
	}

	/**
	 * Translates all views in a zone by a certain amount.
	 * 
	 * @param zoneStart
	 */
	public void translateBy(Point2D zoneStart) {
		for (int i = 0; i < views.size(); i++) {
			MDCView v = (MDCView) views.get(i);
			v.getPosition().x += zoneStart.getX();
			v.getPosition().y += zoneStart.getY();
		}
	}

	/// Places the content of the line in the width given to it, as an
	/// alignment requires. The line is supposed to be at the start (its first
	/// view at `startMargin`), which is where [LineLayout] puts it.
	///
	/// @param alignment the alignment.
	/// @param startMargin the margin to leave on the left (in layout
	///        coordinates, which are always left to right).
	/// @param width the width to use (the start margin is not covered by this
	///        width).
	public void alignTo(LineAlignment alignment, double startMargin, double width) {
		alignTo(false, alignment, startMargin, width);
	}

	/// Places the content of a column in the height given to it, as an
	/// alignment requires: [LineAlignment#START] is the top. The column is
	/// supposed to be at the top (its first view at `topMargin`), which is
	/// where [ColumnLayout] puts it.
	///
	/// @param alignment the alignment.
	/// @param topMargin the margin to leave at the top.
	/// @param height the height to use (the top margin is not covered by this
	///        height).
	public void alignVerticallyTo(LineAlignment alignment, double topMargin, double height) {
		alignTo(true, alignment, topMargin, height);
	}

	/// @param vertical true to align along the y axis (a column), false
	///        along the x axis (a line).
	private void alignTo(boolean vertical, LineAlignment alignment, double start, double length) {
		switch (alignment) {
			case START -> {
			}
			case END -> {
				double[] extent = visibleExtent(vertical);
				if (extent != null) {
					shift(vertical, start + length - extent[1]);
				}
			}
			case CENTER -> centerIn(vertical, start, length);
			case JUSTIFY -> justifyTo(vertical, start, length);
		}
	}

	/// Justifies the line to a certain width, by distributing the free space
	/// between its units (a unit is a view, a run of alphabetic text, or a
	/// group of kerned views).
	///
	/// The line stays as it is if it has only one unit (there is nothing to
	/// distribute), or if it is already wider than `width`.
	///
	/// @param startMargin the margin to leave on the left.
	/// @param width the width to use (the start margin is not covered by this
	///        width).
	public void justifyWidthTo(double startMargin, double width) {
		justifyTo(false, startMargin, width);
	}

	/// Justification along an axis: see [#justifyWidthTo(double, double)].
	private void justifyTo(boolean vertical, double start, double length) {
		// In the current system, some views can be empty (which is not a good
		// idea, BTW).
		// Hence, we need to actually count the number of "real" units.
		double minimalLength = 0;
		List<List<MDCView>> nonEmptyUnits = new ArrayList<>();
		for (List<MDCView> unit : units) {
			double unitLength = unitLength(unit, vertical);
			minimalLength += unitLength;
			if (unitLength != 0)
				nonEmptyUnits.add(unit);
		}
		double space = length - minimalLength;
		if (nonEmptyUnits.size() > 1 && space > 0) {
			double skip = space / (nonEmptyUnits.size() - 1);
			double pos = start;
			for (List<MDCView> unit : nonEmptyUnits) {
				moveUnitTo(unit, pos, vertical);
				pos += unitLength(unit, vertical) + skip;
			}
		}
	}

	/// The first and last coordinate, along an axis, of the non-empty views
	/// (a reversed run of text means the list order is not always the
	/// left-to-right order).
	///
	/// @param vertical true for the y axis, false for the x axis.
	/// @return `{min, max}`, or null if there are no non-empty views.
	private double[] visibleExtent(boolean vertical) {
		double currentStart = Double.POSITIVE_INFINITY;
		double currentEnd = Double.NEGATIVE_INFINITY;
		for (MDCView v : views) {
			double length = v == columnEnd ? 0 : length(v, vertical);
			if (length > 0f) {
				currentStart = Math.min(currentStart, coordinate(v, vertical));
				currentEnd = Math.max(currentEnd, coordinate(v, vertical) + length);
			}
		}
		return currentStart > currentEnd ? null : new double[] { currentStart, currentEnd };
	}

	/// The width of the visible content of the line (its non-empty views).
	public double getVisibleWidth() {
		double[] extent = visibleExtent(false);
		return extent == null ? 0 : extent[1] - extent[0];
	}

	/// The visible area of the line, in its current coordinates: the
	/// x extent of its non-empty views, and the given vertical extent.
	///
	/// @return the bounds, or null if the line has no non-empty views.
	Rectangle2D visibleBounds(double minY, double height) {
		double[] extent = visibleExtent(false);
		return extent == null ? null : new Rectangle2D.Double(extent[0], minY, extent[1] - extent[0], height);
	}

	/// The visible area of a column, in its current coordinates: the
	/// y extent of its non-empty views, and the given horizontal extent.
	///
	/// @return the bounds, or null if the column has no non-empty views.
	Rectangle2D visibleVerticalBounds(double minX, double width) {
		double[] extent = visibleExtent(true);
		return extent == null ? null : new Rectangle2D.Double(minX, extent[0], width, extent[1] - extent[0]);
	}

	private static double coordinate(MDCView v, boolean vertical) {
		return vertical ? v.getPosition().y : v.getPosition().x;
	}

	private static double length(MDCView v, boolean vertical) {
		return vertical ? v.getHeight() : v.getWidth();
	}

	private static double unitMin(List<MDCView> unit, boolean vertical) {
		double min = Double.POSITIVE_INFINITY;
		for (MDCView v : unit) {
			min = Math.min(min, coordinate(v, vertical));
		}
		return min;
	}

	private static double unitLength(List<MDCView> unit, boolean vertical) {
		if (unit.size() == 1) {
			return length(unit.get(0), vertical);
		}
		double max = Double.NEGATIVE_INFINITY;
		for (MDCView v : unit) {
			max = Math.max(max, coordinate(v, vertical) + length(v, vertical));
		}
		return max - unitMin(unit, vertical);
	}

	/// Moves a unit rigidly so that its start (left or top) is at `pos`.
	private static void moveUnitTo(List<MDCView> unit, double pos, boolean vertical) {
		double delta = pos - unitMin(unit, vertical);
		for (MDCView v : unit) {
			if (vertical) {
				v.getPosition().y += delta;
			} else {
				v.getPosition().x += delta;
			}
		}
	}

	/// Moves all the views along an axis.
	private void shift(boolean vertical, double delta) {
		translateBy(vertical ? new Point2D.Double(0, delta) : new Point2D.Double(delta, 0));
	}

	/// Centres the views in a given length.
	///
	/// @param start a margin to add before (on the left or at the top).
	/// @param length the length of the centering area (the margin is not accounted for).
	private void centerIn(boolean vertical, double start, double length) {
		double[] extent = visibleExtent(vertical);
		if (extent == null)
			return;
		double currentLength = extent[1] - extent[0];
		double centerMargin = start + (length - currentLength) / 2.0;
		shift(vertical, centerMargin - extent[0]);
	}

}

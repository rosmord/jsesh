/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 23 déc. 2004
 *
 */
package jsesh.render.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jsesh.render.style.JSeshStyle;
import jsesh.sharedvocabulary.KerningAxis;
import jsesh.model.constants.TextDirection;
import jsesh.model.constants.TextOrientation;
import jsesh.model.LineBreak;
import jsesh.model.ModelElement;
import jsesh.model.ModelElementAdapter;
import jsesh.model.PageBreak;
import jsesh.model.AbsoluteTab;
import jsesh.model.Tab;
import jsesh.render.layout.insert.Kerner;
import jsesh.render.view.MDCView;
import jsesh.sharedvocabulary.LineAlignment;

/// Expert for laying out text organised in columns.
///
/// Columns are never wrapped. They are aligned vertically, according to the
/// `align` option of the line break which ends them (see "Line alignment" in
/// `00_Documents/documentation/technical_glossary.md`): [LineAlignment#START]
/// is the top.
///
/// @author Serge Rosmorduc

public class ColumnLayout extends TopItemLayout {

	MDCView documentView;

	Rectangle2D documentArea;

	Point2D insertionPoint;

	LineAccumulator zone;

	Point2D.Double zoneStart;

	JSeshStyle jseshStyle;

	TextDirection currentTextDirection;

	// End of zone to move

	private MDCView subView;

	private final LayoutAux aux = new LayoutAux();

	private final Kerner kerner;

	/// The horizontal extent (left, right) of each column, for the separators.
	private final List<double[]> columnExtents = new ArrayList<>();

	/// A column placed in the document: its views, the y where its content
	/// starts, and its horizontal extent, in document coordinates.
	private record PlacedColumn(LineAccumulator column, double top, double left, double width) {
	}

	/// The columns placed so far, with the alignment they will get in
	/// [#endLayout()].
	private final List<PlacedColumn> placedColumns = new ArrayList<>();

	/// Their alignments (same indexes as [#placedColumns]).
	private final List<LineAlignment> alignments = new ArrayList<>();

	/// The columns whose cells were placed by [TabLayout]: [#alignColumns()]
	/// leaves them alone.
	private final Set<LineAccumulator> tabbedColumns = Collections.newSetFromMap(new IdentityHashMap<>());

	/// The alignment of the last column ended by a line break: the last
	/// column of the text, if it has no line break, takes it.
	private LineAlignment previousAlignment = LineAlignment.START;

	/// The elements of the current kerned group (since the last element which was not kerned).
	private final List<MDCView> kernedGroup = new ArrayList<>();

	/// Is there a `%vkern` operator after the current kerned group?
	private boolean pendingKerning;

	/// The kerning operators met since the last element; they are moved to the middle of the gap.
	private final List<MDCView> kerningViews = new ArrayList<>();

	/**
	 * @param documentView
	 * @param jseshStyle
	 */
	public ColumnLayout(MDCView documentView,
			JSeshStyle jseshStyle, Kerner kerner) {
		this.kerner = kerner;
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
		if (fillLayout() != null) {
			fillLayout().prepare(subView);
		}
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
		resetKerning();
		boolean mirrored = !currentTextDirection.isLeftToRight();
		double height = 0;
		for (MDCView v : views) {
			v.setDeltaBaseY(0); // No y alignment required !
			height = Math.max(height, v.getHeight());
		}
		zone.addRun(views, run.layoutOffsets(mirrored));
		if (height != 0) {
			zone.moveCurrentPoint(0, height + jseshStyle.geometry().columnQuadratSkip());
		}
	}

	@Override
	public void endLayout() {
		if (!zone.isEmpty()) {
			flushZone(previousAlignment);
		}
		layoutTabs();
		fillZones();
		alignColumns();
		// add a margin
		documentArea.add(new Point2D.Double(documentArea.getMaxX()
				+ jseshStyle.geometry().leftMargin(),
				documentArea.getMinY()));
	}

	@Override
	public double[] getSeparatorPositions() {
		return separatorsBetween(columnExtents);
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
		placedColumns.clear();
		alignments.clear();
		tabbedColumns.clear();
		previousAlignment = LineAlignment.START;
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

	private void resetKerning() {
		kernedGroup.clear();
		pendingKerning = false;
		kerningViews.clear();
	}

	private LineAccumulator createNewZone() {
		LineAccumulator z = new LineAccumulator(0, 0);
		z.moveCurrentPoint(0, jseshStyle.geometry().topMargin());
		return z;
	}

	/// Places the cells of the columns which have tabs on their tab stops
	/// (see [TabLayout]). The cell after the last tab takes the alignment of
	/// the column, in the height of the parent if it has one, else in the
	/// height of the longest column.
	private void layoutTabs() {
		if (!tabsEnabled()) {
			return;
		}
		List<TabLayout.TabbedLine> columns = new ArrayList<>();
		double untabbedHeight = 0;
		for (int i = 0; i < placedColumns.size(); i++) {
			PlacedColumn c = placedColumns.get(i);
			if (c.column().hasTabs()) {
				columns.add(new TabLayout.TabbedLine(c.column(), c.top(), alignments.get(i)));
				tabbedColumns.add(c.column());
			} else {
				Rectangle2D bounds = c.column().visibleVerticalBounds(0, 0);
				if (bounds != null) {
					untabbedHeight = Math.max(untabbedHeight, bounds.getMaxY() - c.top());
				}
			}
		}
		if (columns.isEmpty()) {
			return;
		}
		new TabLayout(true, tabGap(), fillLayout()).apply(columns, jseshStyle.geometry().maxColumnHeight(), untabbedHeight);
		for (PlacedColumn c : placedColumns) {
			if (tabbedColumns.contains(c.column())) {
				Rectangle2D bounds = c.column().visibleVerticalBounds(c.left(), c.width());
				if (bounds != null) {
					documentArea.add(bounds);
				}
			}
		}
	}

	@Override
	protected boolean isVertical() {
		return true;
	}

	/// Stretches the zones which fill their column (see [FillLayout]): each
	/// column is filled down to the height of the parent if it has one (a page
	/// with a height, or a zone with a height), else down to the height of the
	/// longest column. The columns with tabs are left alone.
	private void fillZones() {
		FillLayout fill = fillLayout();
		if (fill == null) {
			return;
		}
		if (fill.stretches()) {
			double height = jseshStyle.geometry().maxColumnHeight();
			if (height <= 0) {
				for (PlacedColumn c : placedColumns) {
					Rectangle2D bounds = c.column().visibleVerticalBounds(0, 0);
					if (bounds != null) {
						height = Math.max(height, bounds.getMaxY() - c.top());
					}
				}
			}
			for (PlacedColumn c : placedColumns) {
				if (tabbedColumns.contains(c.column())) {
					continue;
				}
				Rectangle2D bounds = c.column().visibleVerticalBounds(0, 0);
				double used = bounds == null ? 0 : bounds.getMaxY() - c.top();
				if (fill.fillLine(c.column(), height - used)) {
					Rectangle2D filled = c.column().visibleVerticalBounds(c.left(), c.width());
					if (filled != null) {
						documentArea.add(filled);
					}
				}
			}
		}
		fill.finish();
	}

	/// Places the columns according to their alignment, in the height of the
	/// parent if it has one (a page with a height, or a zone with a height),
	/// else in the height of the longest column.
	///
	/// Unlike lines, columns keep their alignment when the text is wrapped to
	/// the width of the editor (their height doesn't depend on it).
	private void alignColumns() {
		if (alignments.stream().allMatch(a -> a == LineAlignment.START)) {
			return;
		}
		double height = jseshStyle.geometry().maxColumnHeight();
		if (height <= 0) {
			// The bottom of the visible content of the longest column.
			for (PlacedColumn c : placedColumns) {
				Rectangle2D bounds = c.column().visibleVerticalBounds(0, 0);
				if (bounds != null) {
					height = Math.max(height, bounds.getMaxY() - c.top());
				}
			}
		}
		for (int i = 0; i < placedColumns.size(); i++) {
			PlacedColumn c = placedColumns.get(i);
			if (alignments.get(i) != LineAlignment.START && !tabbedColumns.contains(c.column())) {
				c.column().alignVerticallyTo(alignments.get(i), c.top(), height);
				Rectangle2D bounds = c.column().visibleVerticalBounds(c.left(), c.width());
				if (bounds != null) {
					documentArea.add(bounds);
				}
			}
		}
	}

	/// Adds the current column to the document.
	///
	/// @param alignment the alignment of the column.
	private void flushZone(LineAlignment alignment) {
		// Compute the start point relative position for this line
		// RIght-to-left is :
		// zoneStart.setValues(0, RelativePosition.WEST,
		// -zone.getMinY(), RelativePosition.PREVIOUS);
		// zoneStart.setValues(0, RelativePosition.WEST,
		// zone.getHeight(), RelativePosition.PREVIOUS);

		// Normally, the zone upper left point is (0,0). It can change if the
		// zone
		// is highter than expected. Correct this position :
		zoneStart.y += -zone.getMinY();

		// Should it change for right-to-left orientation ?
		zoneStart.x += -zone.getMinX();

		// update the zone
		zone.translateBy(zoneStart);

		columnExtents.add(new double[] { zoneStart.x, zoneStart.x + zone.getWidth() });
		placedColumns.add(new PlacedColumn(zone, zoneStart.y + jseshStyle.geometry().topMargin(),
				zoneStart.x, zone.getWidth()));
		alignments.add(alignment);

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
			endColumn(b.getAlignment());
		}

		/// Ends the current column (at a line break, or a page break).
		///
		/// @param alignment the alignment written on the break.
		private void endColumn(LineAlignment alignment) {
			resetKerning();
			subView.setHeight(0.1);
			subView.setWidth(jseshStyle.geometry().maxCadratWidth());
			// Add a minimal line break height to the current zone
			// (else empty lines would be 0 height)
			// Add the line break to the current line.
			zone.addColumnEnd(subView);
			flushZone(alignment);
			previousAlignment = alignment;

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
				endColumn(b.getAlignment());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jsesh.model.ModelElementAdapter#visitAbsoluteTab(jsesh.model.
		 * AbsoluteTab)
		 */
		@Override
		public void visitAbsoluteTab(AbsoluteTab t) {
			visitDefault(t);
		}

		/// A dynamic tab ends the current tab cell, if tabs are laid out.
		/// Otherwise, it is an empty element.
		@Override
		public void visitTab(Tab t) {
			resetKerning();
			if (tabsEnabled()) {
				zone.addTab(subView, t.getIndex(), t.getAlignment());
			} else {
				visitDefault(t);
			}
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
			Optional<KerningAxis> kerning = KerningPacker.kerningOf(subView);
			if (kerning.isPresent()) {
				// Takes no room. In columns, only %vkern is meaningful.
				pendingKerning |= !kernedGroup.isEmpty() && kerning.get() == KerningAxis.VERTICAL;
				// In the unit of the previous element, so that justification keeps kerned elements together.
				zone.addKerned(subView);
				kerningViews.add(subView);
				return;
			}
			// add the new view's bounding box to the current column.
			if (pendingKerning) {
				// Place the view where it would go, and move it back.
				subView.getPosition().setLocation(zone.getCurrentPoint().getX() + subView.getDeltaBaseX(),
						zone.getCurrentPoint().getY() + subView.getDeltaBaseY());
				double shift = kerner.shift(kernedGroup, subView, KerningAxis.VERTICAL,
						jseshStyle.geometry().smallSkip());
				zone.moveCurrentPoint(0, -shift);
				zone.addKerned(subView);
			} else {
				zone.add(subView);
			}
			if (!kernedGroup.isEmpty()) {
				MDCView last = kernedGroup.get(kernedGroup.size() - 1);
				double middle = (last.getPosition().y + last.getHeight() + subView.getPosition().y) / 2;
				for (MDCView k : kerningViews) {
					k.getPosition().y = middle - k.getHeight() / 2;
				}
			}
			kerningViews.clear();
			if (!pendingKerning) {
				kernedGroup.clear();
			}
			pendingKerning = false;
			kernedGroup.add(subView);
			// Compute the position of the following view.
			// prepare a small skip after v, if necessary
			// the skip is "integrated" in v,
			// A view which fills has no height yet (a running annotation), but takes its gap.
			if (subView.getHeight() != 0 || subView.isFilling()) {
				zone.moveCurrentPoint(0, subView.getHeight()
						+ jseshStyle.geometry().columnQuadratSkip());
			}
		}
	}

}
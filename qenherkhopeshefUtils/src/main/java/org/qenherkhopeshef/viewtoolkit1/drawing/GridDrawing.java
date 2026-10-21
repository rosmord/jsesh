package org.qenherkhopeshef.viewtoolkit.drawing;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.qenherkhopeshef.viewtoolkit.drawing.element.GraphicalElement;
import org.qenherkhopeshef.viewtoolkit.drawing.event.DrawingChangedEvent;
import org.qenherkhopeshef.viewtoolkit.drawing.event.DrawingEvent;
import org.qenherkhopeshef.viewtoolkit.drawing.event.ElementAdded;
import org.qenherkhopeshef.viewtoolkit.drawing.event.ElementRemoved;

/// A free position drawing, in which element access is speeded by a grid-like
/// indexation.
///
/// The plane is divided into cells of a fixed size; each element is registered
/// in the cells its (decorated) bounds intersect. [#getElementsInZone(Rectangle2D)]
/// only looks at the cells covered by the zone, so the cost of drawing a
/// small part of a large drawing doesn't depend on the total number of
/// elements. Elements which would span too many cells are kept in a separate
/// list, which is always searched.
///
/// The index follows the elements: when an element managed by this drawing
/// changes (moves, grows...), it is re-indexed automatically.
///
/// Elements are returned in drawing order: by increasing [GraphicalElement#getLayer()],
/// then by order of insertion (see [#bringToFront(GraphicalElement)] and
/// [#sendToBack(GraphicalElement)]).
///
/// @author rosmord
public class GridDrawing extends AbstractDrawing implements FreePlacementDrawing {

    /// Elements spanning more cells than this are not stored in the grid.
    private static final int MAX_CELLS_PER_ELEMENT = 64;

    private final double gridWidth;
    private final double gridHeight;

    /// Grid cells, indexed by [#cellKey(int, int)].
    private final Map<Long, Set<GraphicalElement>> cells = new HashMap<>();

    /// Elements too large to be put in the grid.
    private final Set<GraphicalElement> largeElements = new LinkedHashSet<>();

    private final Map<GraphicalElement, Entry> entries = new HashMap<>();

    private long nextFrontOrder = 0;
    private long nextBackOrder = -1;

    /// Cached union of the elements' bounds (null when it must be recomputed).
    private Rectangle2D extent = null;

    private double minimalWidth = 0;
    private double minimalHeight = 0;

    /// When true, events from elements are ignored (used while we are
    /// changing them ourselves).
    private boolean muted = false;

    /// Index information about one element.
    private static class Entry {
        long order;
        /// Indexed cell range (meaningful only when inGrid is true).
        int minI, minJ, maxI, maxJ;
        boolean inGrid = false;

        Entry(long order) {
            this.order = order;
        }
    }

    /// Creates a drawing with 100x100 cells.
    public GridDrawing() {
        this(100, 100);
    }

    /// Creates a drawing.
    ///
    /// @param gridWidth the cell width; should be in the order of the size of
    ///  typical elements.
    /// @param gridHeight the cell height.
    public GridDrawing(double gridWidth, double gridHeight) {
        if (gridWidth <= 0 || gridHeight <= 0)
            throw new IllegalArgumentException("grid cells must have a positive size");
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
    }

    /// Adds (or moves) an element, placing its origin at (x,y).
    @Override
    public void putAt(GraphicalElement elt, double x, double y) {
        boolean isNew = register(elt);
        muted = true;
        try {
            elt.setOrigin(x, y);
        } finally {
            muted = false;
        }
        reindex(elt);
        extent = null;
        fireDrawingEvent(isNew ? new ElementAdded(elt) : new DrawingChangedEvent());
    }

    /// Adds an element at its current position.
    public void addElement(GraphicalElement elt) {
        boolean isNew = register(elt);
        reindex(elt);
        extent = null;
        if (isNew)
            fireDrawingEvent(new ElementAdded(elt));
    }

    private boolean register(GraphicalElement elt) {
        if (entries.containsKey(elt))
            return false;
        entries.put(elt, new Entry(nextFrontOrder++));
        manage(elt);
        return true;
    }

    @Override
    public void remove(GraphicalElement elt) {
        Entry entry = entries.remove(elt);
        if (entry != null) {
            unindex(elt, entry);
            elt.setManager(null);
            extent = null;
            fireDrawingEvent(new ElementRemoved(elt));
        }
    }

    /// Removes all elements.
    public void clear() {
        for (GraphicalElement elt : entries.keySet())
            elt.setManager(null);
        entries.clear();
        cells.clear();
        largeElements.clear();
        extent = null;
        fireDrawingEvent(new DrawingChangedEvent());
    }

    public boolean containsElement(GraphicalElement elt) {
        return entries.containsKey(elt);
    }

    /// All elements, in drawing order.
    public List<GraphicalElement> getElements() {
        List<GraphicalElement> result = new ArrayList<>(entries.keySet());
        result.sort(drawingOrder());
        return result;
    }

    /// Puts an element over the others of its layer.
    public void bringToFront(GraphicalElement elt) {
        Entry entry = entries.get(elt);
        if (entry != null) {
            entry.order = nextFrontOrder++;
            fireDrawingEvent(new DrawingChangedEvent());
        }
    }

    /// Puts an element under the others of its layer.
    public void sendToBack(GraphicalElement elt) {
        Entry entry = entries.get(elt);
        if (entry != null) {
            entry.order = nextBackOrder--;
            fireDrawingEvent(new DrawingChangedEvent());
        }
    }

    /// Sets a minimal size for the drawing (useful for an initially empty canvas).
    public void setMinimalSize(double width, double height) {
        this.minimalWidth = width;
        this.minimalHeight = height;
        fireDrawingEvent(new DrawingChangedEvent());
    }

    private Comparator<GraphicalElement> drawingOrder() {
        return Comparator.comparingInt(GraphicalElement::getLayer)
                .thenComparingLong(e -> entries.get(e).order);
    }

    @Override
    protected void eventOccurredInDrawing(DrawingEvent ev) {
        if (muted)
            return;
        GraphicalElement source = ev.getSource();
        if (source != null && entries.containsKey(source)) {
            reindex(source);
        }
        extent = null;
        fireDrawingEvent(ev);
    }

    @Override
    public Collection<GraphicalElement> getElementsInZone(Rectangle2D rectangle) {
        Set<GraphicalElement> candidates = new HashSet<>();
        int minI = cellColumn(rectangle.getMinX());
        int maxI = cellColumn(rectangle.getMaxX());
        int minJ = cellRow(rectangle.getMinY());
        int maxJ = cellRow(rectangle.getMaxY());
        long zoneCells = ((long) maxI - minI + 1) * ((long) maxJ - minJ + 1);
        if (zoneCells > cells.size()) {
            // Large zone: cheaper to look at the existing cells.
            for (Set<GraphicalElement> cell : cells.values())
                candidates.addAll(cell);
        } else {
            for (int i = minI; i <= maxI; i++) {
                for (int j = minJ; j <= maxJ; j++) {
                    Set<GraphicalElement> cell = cells.get(cellKey(i, j));
                    if (cell != null)
                        candidates.addAll(cell);
                }
            }
        }
        candidates.addAll(largeElements);
        List<GraphicalElement> result = new ArrayList<>();
        for (GraphicalElement elt : candidates) {
            if (elt.getDecoratedBounds().intersects(rectangle))
                result.add(elt);
        }
        result.sort(drawingOrder());
        return result;
    }

    /// The size of the drawing. The rectangle always starts at (0,0), and
    /// covers all elements and the minimal size.
    @Override
    public Rectangle2D getPreferredSize() {
        if (extent == null) {
            double maxX = 0, maxY = 0;
            for (GraphicalElement elt : entries.keySet()) {
                Rectangle2D b = elt.getDecoratedBounds();
                maxX = Math.max(maxX, b.getMaxX());
                maxY = Math.max(maxY, b.getMaxY());
            }
            extent = new Rectangle2D.Double(0, 0, maxX, maxY);
        }
        return new Rectangle2D.Double(0, 0, Math.max(minimalWidth, extent.getWidth()),
                Math.max(minimalHeight, extent.getHeight()));
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /// No cursor in this kind of drawing.
    @Override
    public Rectangle2D getCursorBounds() {
        return null;
    }

    private int cellColumn(double x) {
        return (int) Math.floor(x / gridWidth);
    }

    private int cellRow(double y) {
        return (int) Math.floor(y / gridHeight);
    }

    private static long cellKey(int i, int j) {
        return (((long) i) << 32) ^ (j & 0xffffffffL);
    }

    private void reindex(GraphicalElement elt) {
        Entry entry = entries.get(elt);
        Rectangle2D b = elt.getDecoratedBounds();
        int minI = cellColumn(b.getMinX());
        int maxI = cellColumn(b.getMaxX());
        int minJ = cellRow(b.getMinY());
        int maxJ = cellRow(b.getMaxY());
        if (entry.inGrid && minI == entry.minI && maxI == entry.maxI
                && minJ == entry.minJ && maxJ == entry.maxJ)
            return; // nothing changed.
        unindex(elt, entry);
        long count = ((long) maxI - minI + 1) * ((long) maxJ - minJ + 1);
        if (count > MAX_CELLS_PER_ELEMENT) {
            largeElements.add(elt);
        } else {
            for (int i = minI; i <= maxI; i++)
                for (int j = minJ; j <= maxJ; j++)
                    cells.computeIfAbsent(cellKey(i, j), k -> new HashSet<>()).add(elt);
            entry.inGrid = true;
            entry.minI = minI;
            entry.maxI = maxI;
            entry.minJ = minJ;
            entry.maxJ = maxJ;
        }
    }

    private void unindex(GraphicalElement elt, Entry entry) {
        if (entry.inGrid) {
            for (int i = entry.minI; i <= entry.maxI; i++) {
                for (int j = entry.minJ; j <= entry.maxJ; j++) {
                    long key = cellKey(i, j);
                    Set<GraphicalElement> cell = cells.get(key);
                    if (cell != null) {
                        cell.remove(elt);
                        if (cell.isEmpty())
                            cells.remove(key);
                    }
                }
            }
            entry.inGrid = false;
        }
        largeElements.remove(elt);
    }
}

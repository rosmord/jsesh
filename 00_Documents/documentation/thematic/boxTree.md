# Box tree and zones (design, not implemented)

*Written 2026-10-20. Nothing in this document is implemented yet; the
long-term todo [0127](../../tracking/TODO/03_longterm/todo20261020-0127.md)
tracks the phases (section F).*

This document proposes a new view structure for JSesh, the *box tree*, and
the model for `%zone(content of the zone)`.

The starting point is the GUI structure of the Ramsès syntax editor. Its
toolkit also lives in this repository, as `org.qenherkhopeshef.viewtoolkit`
(`qenherkhopeshefUtils`). The structure has three parts:

- a dumb displayer, which paints the elements of a drawing that fall in the
  clip, then a separate layer of *decorations* (cursor, selection);
- a presenter, which keeps the drawing in sync with the model;
- an n‑n mapping between view and model elements.

The goals for JSesh are:

1. a view model which is trivial to draw, with fast lookups;
2. an n‑n mapping between view and model, so that layout objects which are
   not model elements (**lines and columns**, frames, zones, fragments) can
   have views of their own;
3. an explicit representation of the **possible cursor positions** in the
   view, which simplifies cursor drawing, mouse clicks and geometric
   navigation (up/down, Home/End);
4. **zones**: nested areas with their own layout (columns and lines, other
   algorithms, possibly free placement and pictures).

See also [Cursor and edition](advancedCursor.md) for the current cursor,
position and scope machinery, on which this design builds.

## A. Findings

### A.1 `org.qenherkhopeshef.viewtoolkit` (qenherkhopeshefUtils)

| Class | What it does | Keep for JSesh? |
|---|---|---|
| `JGraphicalElementDisplayer<T extends Drawing>` | A `JPanel` that paints `drawing.getElementsInZone(clip)`, then `getDecorations()`. Scrolls to `getCursorBounds()`, has a busy interface, and repaints on `DrawingEvent`s. | **Idea kept**: a dumb displayer, a decorations layer and scroll-to-cursor. Rewritten, because it has no zoom and no point conversion, and paints an editor `JComponent` itself. |
| `Drawing` / `AbstractDrawing` | `getElementsInZone`, `getElementsAt` (a 1×1 rectangle), decorations, preferred size, cursor bounds, listeners. Elements report to the drawing through a `GraphicalElementManager`. | **Interface idea kept** (zone and point queries, decorations). The event plumbing is dropped. |
| `GraphicalElement` | A mutable element with origin, dimension, margin, layer, alignment, background and decorator. Its properties (`ObjectProperty`) fire `PropertyChangedEvent`s. `draw()` translates to the origin, then calls `drawElement`. | **Not as is.** Placement is translation only, while JSesh needs scale, rotation and right-to-left mirroring (`MDCView.getAffineTransform`, `layoutToDrawing`). Mutable properties with events don't scale to 10⁴ glyphs, and push state (such as selection colour) into the view. |
| `CompositeElement` + `CompositeLayout` (`VerticalCompositeLayout`) | A composite that repacks itself when a child changes, with a layout strategy. | **Strategy idea kept** (`FlowLayout`, below). Self-repacking through events is dropped. |
| `TabularDrawing` | A grid of columns and rows. Lookup is a **binary search on columns** (`findColumnFor`) with a row scan. `translate(firstColumn, n)` shifts columns after an insertion. The cursor is a decoration. | **Lessons kept**: lookups can rely on the layout's own order, without a quadtree, and absolute coordinates force a `translate()` on each insertion, which relative boxes avoid. |
| `PlainDrawing` (linear scan), `FreePlacementDrawing` (interface), `GridDrawing` (stub) | Experiments. | `PlainDrawing` maps to `LinearBoxIndex`, and `FreePlacementDrawing` to `FreeFlowLayout`. |
| `textOriented/package-info.java` | An empty package, meant for "a desktop publishing" drawing. | **This is where the generic part goes** (renamed `box`, see C). |

In Ramses itself (`IndexedDrawing`, `SyntaxViewPresenter`), the n‑n map is a
global `ReversibleMultiHashMap<TaggedElement, GraphicalElement>`. The
presenter colours elements to show selection. `graphicalToModel` throws
"Normally, only one model for a graphical element". **Avoid both**: store
links on the boxes, and draw the selection as a decoration.

Nothing in this repository imports `org.qenherkhopeshef.viewtoolkit`. The old
classes stay untouched. Deprecating them is a separate decision.

### A.2 JSesh's current pipeline

- `ViewBuilder.buildView(element)` builds an `MDCView` tree that mirrors the
  model **1‑1**: subview *i* = model child *i*, with relative coordinates and
  per-view scale, angle and direction. `Layout` (a visitor, with
  `QuadratLayout` etc.) lays out each element.
- For the whole text, `Layout.compositeVisitHieroglyphicText` picks a
  `TopItemLayout` (`LineLayout` or `ColumnLayout`, depending on the
  orientation). It groups alphabetic runs (`TextRun.layoutRuns`), and feeds
  each top-item view to the strategy.
- `LineLayout` builds one `jsesh.render.layout.Zone` per line. Lines break
  **only at explicit `LineBreak`/`PageBreak`**: there is no automatic
  wrapping. Tabs are relative to the zone. `flushZone()` translates the views
  into page coordinates and then **drops the zone**. Justification takes the
  maximum width of all zones, a **global** dependency, and spreads each zone's
  units.
- `MDCViewUpdater` (`jsesh.ui.editor`) handles model operations:
  - `ChildOperation`, `Insertion`, `Deletion`, `ZoneModification`: rebuild
    the affected top-item views, then `reLayout` the whole text;
  - `Modification`, `Replacement`: invalidate everything.
- `ViewDrawer` draws the tree recursively, with clipping, through
  `ElementDrawer`/`DefaultElementDrawer` (a visitor on the model). It also
  draws the top-level selection.
- `TextGeometry` is public, rebuilt for **each** paint and click. It uses
  `PlacedView` (a view + `toRoot` transform + path, checking
  `sub.getModel() == model.getChildAt(i)`), `CursorLocator`,
  `SelectionLocator` and `PositionLocator`. Its results are `CursorShape` and
  `SelectionShape`, records that paint themselves.
- `PositionNavigator.stops(topIndex, granularity)`, `isCursorStop`,
  `canonicalize`: the model decides which stops exist.
- `ItemSequence`, `TextScope`, `EditingScope`: nested sequences (cartouche
  content = `BasicItemList`) are full editing scopes.
- `cursorUp`/`cursorDown` use `getDownPosition`, which works on the model.
  `END_OF_LINE` exists. Up/down inside stacked quadrats is still to do
  (todo 0072).

### A.3 Zones today

- **Lexer**: `MdcLexicon` has `ZONE` (`%zone`).
- **Parser**: `MDCParser.parseZone()` accepts `ZONE optionList? '(' topitems ')'`,
  and the AST has `AstZone(AstOptionList options, AstTopItemList list)`. So
  the target syntax `%zone(…)`, and `%zone[opts](…)`, **already parses**.
- **`AstModelBuilder.visitZone`**: `push(null)`, so the zone is discarded.
- **Model**: `ZoneStart` is a dead stub (a flat marker with size, reference
  point, orientation, skips and rules). The parser never produces it.
  `MdCModelWriter.visitZoneStart` writes `"zone"` (TODO), and
  `DefaultElementDrawer.visitZoneStart` is empty.
- `ModelElement.equals` is not overridden, so model references compare by
  identity. `getParent()` exists, and `getDescendant(int...)` follows a path.

## B. Design principles

1. **The view follows the layout, not the model.** The view is a *box tree*:
   page → flow (a sequence laid out by a strategy) → lines/columns → items →
   (later) glyphs. Boxes that have no single model counterpart (lines, zones,
   frames, pictures, fragments) are normal.
2. **n‑n links live on the boxes, relative to their parent.** There is no
   global map. Links are relative, so that an insertion only renumbers the
   line boxes that follow, and their item boxes are shared as they are.
3. **Boxes are immutable, and the tree is persistent.** An update builds a
   new tree that shares the unchanged subtrees, as Ramses's `translate()` did
   for its columns. There are no property events. A tree can be built off the
   Swing thread, as Ramses's `LayoutWorker` did.
4. **Each box is placed relative to its parent by an `AffineTransform`**
   (translation, scale, rotation, mirroring), as `MDCView` already is.
5. **Each container indexes its own children** with a strategy-specific
   `BoxIndex`. There is no global quadtree.
6. **The model decides which cursor stops exist, and the view says where they
   are.** `CaretStop`s are cached per flow, and computed from
   `PositionNavigator` and the cursor rules that exist today.
7. **Cursor and selection are decorations**, computed at paint time from
   the caret and the tree.
8. **Migrate in phases, so that each phase is verifiable on its own.**
   Existing `MDCView` subtrees first live *inside* item boxes (a bridge), so
   drawing and quadrat layout don't change until late.

## C. Generic layer: `org.qenherkhopeshef.viewtoolkit.box` (qenherkhopeshefUtils)

This layer has no JSesh dependency and is Swing-free, except `box.swing`. It
replaces the empty `drawing.textOriented` package, which is deleted. The
older `drawing.*` classes are untouched.

### C.1 `Box` (abstract class)
```java
public abstract class Box {
    protected Box(Rectangle2D bounds, AffineTransform toParent);
    public final Rectangle2D bounds();          // own local system (usually x=y=0)
    public final AffineTransform toParent();    // local → parent (defensive copy)
    public Rectangle2D boundsInParent();        // toParent applied to bounds (cached)
    public List<? extends Box> children();      // empty for leaves
    public BoxIndex index();                    // default: LinearBoxIndex over children()
    public abstract Box withTransform(AffineTransform toParent); // copy; children shared
    /// Template: paintSelf, then the children intersecting clip (each with its
    /// transform), then paintAfterChildren. Final, so that clipping is uniform.
    public final void paint(Graphics2D g, PaintRequest request);
    protected void paintSelf(Graphics2D g, PaintRequest request) {}
    protected void paintAfterChildren(Graphics2D g, PaintRequest request) {}
}
```
- It is immutable after construction. Subclasses add their own final fields.
- `paintAfterChildren` covers today's `ElementDrawer` *postfix* pass and
  `ViewDrawer.isShadeAfter`.

### C.2 `PaintRequest` (record)
`record PaintRequest(Rectangle2D clip, boolean debug)`. `clip` is in the local
system of the box being painted. `Box.paint` transforms it for each child,
using the inverse of `toParent`. `debug` draws the bounds, replacing
`ViewDrawer.drawDebug`. The application captures everything else at build
time.

### C.3 `BoxIndex` (interface) and its implementations
```java
public interface BoxIndex {
    List<Box> intersecting(Rectangle2D local);   // paint order
    List<Box> containing(Point2D local);         // topmost last
    Optional<Box> nearest(Point2D local);        // distance to boundsInParent
}
```
- `LinearBoxIndex`: a scan, the default for small containers and free zones.
- `AxisSortedBoxIndex(axis)`: children sorted by their minimum along `axis`,
  with a running maximum, so that overlaps (cartouches higher than the line)
  stay correct. Queries are binary searches. It is used by lines (along the
  line) and by flows (along the block axis).
- `QuadTreeBoxIndex`: **only if needed** (many boxes in a free zone). The
  interface leaves room for it.

### C.4 `PlacedBox` (final class)
It generalises `PlacedView`:
`(Box box, AffineTransform toRoot, int[] childPath, PlacedBox parent)`.
Methods:
- `child(int)`, `children()`;
- `toLocal(Point2D)`, `toRoot(Point2D | Line2D | Rectangle2D | Shape)`;
- `static List<PlacedBox> hitChain(Box root, Point2D rootPoint)`: the chain
  of the deepest boxes containing the point, from the root, using each
  `index().containing`.
- `static Optional<PlacedBox> follow(Box root, int[] childPath)`.

### C.5 `Decoration` (interface) and `DecorationLayer`
```java
public interface Decoration {
    DecorationLayer layer();          // BELOW_CONTENT (selection), ABOVE_CONTENT (cursor, hover)
    Rectangle2D bounds();             // root system, for repaint and scroll
    void paint(Graphics2D g);         // root system
}
```

### C.6 `Scene` (interface), `SceneListener`, `SceneChange`
```java
public interface Scene {
    Box root();
    Rectangle2D bounds();                       // preferred size
    void addSceneListener(SceneListener l); void removeSceneListener(SceneListener l);
}
record SceneChange(Optional<Rectangle2D> dirtyArea /* empty = everything */, boolean boundsChanged)
```
A scene is a *holder*. Updating it replaces `root`, then fires one
`SceneChange`.

### C.7 `ScenePainter` (final, static)
`paint(Graphics2D g, Box root, Rectangle2D clipRoot, PaintRequest base,
Collection<Decoration> decorations)` paints the `BELOW_CONTENT` decorations,
then the root, then the `ABOVE_CONTENT` decorations. It works on any
`Graphics2D`: screen, print, PDF, SVG, clipboard.

### C.8 `box.swing.JSceneDisplayer` (extends `JComponent`)
- A `Scene`, a `Supplier<Collection<Decoration>>`, a display `scale` (zoom)
  and margins.
- `paintComponent` calls `ScenePainter` with the clip converted to scene
  coordinates.
- Conversions: `toScene(Point)`, `toScreen(Rectangle2D)`.
- `scrollSceneRectToVisible(Rectangle2D)`, replacing `showCursor`.
- On a `SceneChange`, it repaints only the dirty area (converted) and calls
  `revalidate()` if the bounds changed.
- It has no busy interface and doesn't paint embedded editor components:
  those stay the application's job.

Tests (qenherkhopeshefUtils): the index results equal a linear scan on random
rectangles, with and without overlaps. `PlacedBox` point round trips work
through scale, rotation and mirroring. `ScenePainter` respects the order of
the layers.

## D. JSesh layer: `jsesh.render.box` (new, Swing-free)

Its dependencies: `jsesh.model`, `jsesh.render.{view,layout,draw,style,context}`
and `org.qenherkhopeshef.viewtoolkit.box`. It is used by `jsesh.ui.editor`
and `jsesh.ui.export`.
**Avoid a cycle**: `TextGeometry` and the locators move from
`jsesh.render.draw` to `jsesh.render.box`, so that `box → draw`
(`CursorShape`, `SelectionShape`, `ViewDrawer`) is one-way. API
compatibility is not a goal (CLAUDE.md).

### D.1 Model links
```java
/// The part of the model a box stands for, relative to the anchor of its parent box.
public record ModelSpan(int[] path, int start, int end) {
    public static final ModelSpan NONE;               // frames, rules, pictures with no model
    public static ModelSpan item(int index);          // ([], index, index+1)
    public boolean isNone();
}
/// The model position that a box's children's spans are relative to.
public record ModelAnchor(int[] containerPath, int offset) {}
```
- **Absolute resolution**: `BoxPaths.absoluteSpan(PlacedBox)` walks up the
  chain, adding each parent's anchor (`containerPath` concatenated, and
  `offset` added to the first index). It returns an `MDCRange`, or a
  `(TextScope, start, end)` triple.
- **Anchors**:
  - a `FlowBox` anchors at its scope, with offset 0;
  - a `LineBox` anchors at `(scope, startGap)`, so its items use local indices
    `0..n`;
  - an `ItemBox` anchors at its item (`path = [k]` in the line's frame).
- **Consequence**: inserting *n* items before a line only rebuilds that line
  box with `startGap + n` and a new transform. Its item boxes are shared.
- **n‑n examples**:

  | Case | How it is represented |
  |---|---|
  | `TextRunBox` (alphabetic run) | one box, span `[k, k+m)` |
  | Line | span `[startGap, endGap)` |
  | Item split over two lines (later) | two `ItemBox` *fragments* with the same span, `fragment = (i, n)` |
  | Ligature glyph (phase 10) | span over the ligature's children |
  | Cartouche frame | `NONE`; its content flow anchors at the `BasicItemList` |

- **Debug check**: an optional `ModelElement expected` reference on
  `ItemBox`, compared by identity, as `PlacedView.child` does today. It
  detects a tree that is out of sync with the model.

### D.2 Box classes
All extend `Box` and implement
```java
public interface MdcBox { ModelSpan span(); ModelAnchor anchor(); <R> R accept(MdcBoxVisitor<R> v); }
```

| Class | Children | Role |
|---|---|---|
| `DocumentBox` | `FlowBox` (or `PageBox`s when paged) | The root. It holds the `JSeshStyle` used, and is the `Scene.root()`. |
| `PageBox` | lines of the root flow (a *fragment* of it) | Paged mode and printing: `startLine..endLine` of the root `FlowBox`. Not before phase 6. |
| `FlowBox` | `LineBox`es (line or column strategies) or `ItemBox`es (free) | A sequence (`TextScope scope`) laid out by one `FlowLayout`. It owns a `CaretStopTable` (D.3) and an `AxisSortedBoxIndex` (block axis) or a `LinearBoxIndex` (free). Fields: `scope`, `FlowParameters params`, `FlowKind kind`. |
| `LineBox` | `ItemBox`, `TextRunBox`, `BreakBox` | A line or column: `startGap`, `endGap`, `axis`, `baselineOffset`, `justified` (boolean), `AxisSortedBoxIndex` along the line. |
| `ItemBox` (abstract) | depends on the subclass | A top item of the flow. |
| `ViewItemBox` extends `ItemBox` | none (leaf) | **Bridge**: holds the item's `MDCView` subtree, built by `ViewBuilder.buildView(item)`. `paintSelf` calls `ViewDrawer` on that subtree. Cursor geometry inside the item reuses `CursorLocator` (D.3). |
| `TextRunBox` | none | A run of alphabetic items laid out as one unit (`TextRun`), covering several items. |
| `BreakBox` | none | `LineBreak`, `PageBreak`, `TabStop`: zero width, but with a span and stops. |
| `ZoneBox` extends `ItemBox` | `FlowBox` (+ `RuleBox`es and a frame) | A `%zone` item. Its content flow uses the zone's own strategy. |
| `CartoucheBox` extends `ItemBox` | `ShapeBox` frame + `FlowBox` content | Phase 8. It makes cartouche content a flow like any other. |
| `ShapeBox` | none | Frames, rules, philology brackets (a filled or stroked `Shape`, colour, stroke). |
| `GlyphBox` | none | Phase 10: one sign (a `HieroglyphDrawer` reference, code, options captured at build time). |
| `PictureBox` | none | Phase 9: an image. |

`MdcBoxVisitor<R>` has one method per class, plus an adapter with a default.

### D.3 Cursor stops
```java
public record CaretStop(MDCPosition position,      // absolute, at the time of the table's construction
                        CursorShape shape,         // in the FlowBox's local system
                        Rectangle2D hitArea,       // click area (gap between neighbours), local
                        boolean quadratLevel) {}   // reachable in QUADRAT mode / by a click
public final class CaretStopTable {                // one per FlowBox, built lazily, then cached
    Optional<CaretStop> at(MDCPosition p);
    List<CaretStop> inReadingOrder();
    List<CaretStop> inLine(int lineIndex);
    Optional<CaretStop> nearest(Point2D local, Predicate<CaretStop> filter);
    OptionalInt lineOf(MDCPosition p);
}
```
- **Which stops**: those whose innermost scope is the flow's scope. Gaps of
  the flow are `quadratLevel`. Inner stops of its items come from
  `PositionNavigator.stops(i, INNER)` restricted to that scope. Stops inside
  a nested flow (zone, cartouche content) belong to the nested flow's table.
- **Where**:
  - Gaps between items of a line: the rules of
    `CursorLocator.locateInText` (merged line, breaks), rewritten on
    `LineBox` children. They become `LineCursorRules`, package-private.
  - Inner stops of a `ViewItemBox`: `CursorLocator.locateInContainer` on a
    `PlacedView` rooted at the item. This needs a new factory
    `PlacedView.at(MDCView view, AffineTransform toRoot, int[] modelPath)`.
- **Positions are absolute only inside a table built for one tree.** After
  any update, the table belongs to the new tree, since tables are cached per
  `FlowBox`. Unchanged flows are shared, but their `MDCPosition`s may have
  shifted. **Decision**: tables store positions *relative to their flow's
  anchor* (as `ModelSpan`s do), and absolutise them on output. This is the
  same rule as D.1.

### D.4 Layout strategies
```java
public interface FlowLayout {
    FlowBox layout(FlowInput in);
    /// Default: layout(in). LineFlowLayout overrides it (D.5).
    default FlowBox relayout(FlowBox previous, FlowEdit edit, FlowInput in) { return layout(in); }
}
public record FlowInput(TextScope scope, List<ItemBox> items, FlowParameters params, LayoutEnvironment env) {}
public record FlowParameters(TextOrientation orientation, TextDirection direction,
        double lineSkip, double columnSkip, boolean justified, boolean paged,
        double width, double height /* 0 = computed */, double ruleWidth) {
    static FlowParameters fromStyle(JSeshStyle style);       // the root text
    static FlowParameters forZone(ZoneParameters zp, JSeshStyle style);
}
public record LayoutEnvironment(JSeshRenderContext rc, JSeshTechRenderContext trc, ItemBoxFactory factory) {}
public record FlowEdit(int firstChangedItem, int oldCount, int newCount) {}
```
Implementations:
- `LineFlowLayout`: a port of `LineLayout`. It handles line breaks
  (spacing %), page breaks (paged or not), tabs (relative to the line), top
  margin, small skip, and **justification as a separate pass**
  (`Justifier`): the maximum width over the lines, then spreading units.
  `jsesh.render.layout.Zone` becomes its private helper `LineAccumulator`.
- `ColumnFlowLayout`: a port of `ColumnLayout`.
- `FreeFlowLayout`: items placed at explicit coordinates (see G.3), with
  `LinearBoxIndex`.
- Mixed layouts ("columns + lines") come from **nesting**: a horizontal flow
  containing a vertical zone, and so on. No combined strategy is needed.

`TopItemLayout`, `LineLayout`, `ColumnLayout` and
`Layout.compositeVisitHieroglyphicText` are retired at the end of phase 6.
`Layout`'s quadrat-level code (`QuadratLayout`, ligatures, cartouche
internals) is **unchanged**: it builds the `MDCView`s inside items.

### D.5 Construction and update
- `ItemBoxFactory`: `ItemBox create(TopItem item, LayoutEnvironment env)`.
  - `Zone` gives a `ZoneBox`: it recursively builds the content `FlowBox`
    with the zone's strategy.
  - Otherwise it gives a `ViewItemBox(ViewBuilder.buildView(item))`.
  - Alphabetic runs give `TextRunBox`es. `TextRun.layoutRuns` needs an
    overload taking a `List<MDCView>`, since today it takes the parent view.
- `BoxTreeBuilder`:
  `DocumentBox build(HieroglyphicText text, JSeshRenderContext rc, JSeshTechRenderContext trc)`.
  It is a public entry point and replaces `ViewBuilder.buildView(text)` for
  the whole text.
- `BoxTreeUpdater`:
  `UpdateResult apply(DocumentBox old, ModelOperation op, LayoutEnvironment env)`,
  where `record UpdateResult(DocumentBox tree, Optional<Rectangle2D> dirty)`.

  | Operation | What the updater does |
  |---|---|
  | `Insertion` (index, children) | Build the new `ItemBox`es. `LineFlowLayout.relayout` re-splits from the line containing `index` up to the next unchanged `LineBreak`, since lines only break at explicit breaks. Following lines get `startGap + n` and a translated transform. Re-justify if the maximum width changed. |
  | `Deletion` | Same as `Insertion`. |
  | `ChildOperation` | Rebuild that item's box (nested-scope edits inside a cartouche or zone arrive here too), then relayout its line. |
  | `ZoneModification` (start, end) | Rebuild those items, then relayout their lines. |
  | `Modification`, `Replacement` | Full `build` (as today's `invalidateView`). |

  The dirty area is the union of the old and new bounds of the changed lines
  and of every line that moved.

### D.6 Geometry facade: `TextGeometry` (moved to `jsesh.render.box`)
It is built on a `DocumentBox` and is cheap, since the caches live in the
tree.

Kept methods: `cursorAt(MDCCaret)`, `cursorAt(MDCPosition)`,
`selection(TextScope, int, int)`, `positionAt(Point2D)`,
`areaAround(MDCPosition)`.

New methods:
- `Optional<MDCPosition> move(MDCPosition from, Direction d, CaretGranularity g, OptionalDouble goal)`
- `Optional<MDCPosition> lineStart(MDCPosition)`, `lineEnd(MDCPosition)`
- `List<PlacedBox> boxesFor(MDCPosition element)`: model → view (n‑n)
- `List<MDCRange> spansAt(Point2D)`: view → model (n‑n), for hover, ligature
  components and "select this word"
- `Rectangle2D cursorArea(MDCCaret)`: for scrolling (replaces the
  `areaAround` fallback in todo 0072)

Internally:
- `ClickResolver` replaces `PositionLocator`. It descends the
  `PlacedBox.hitChain` to the deepest `FlowBox` that a click may enter. The
  rule is unchanged: `isCursorStop(0, QUADRAT)` on its scope. Then:
  - inside an item that hosts no enterable flow: the gap on the nearer side
    (today's rule), or the cartouche frame treated as a unit;
  - inside a line: the nearest `quadratLevel` stop of that line;
  - in no line: the end of the nearest line along the block axis (today's
    "after the end of a line").
- `CaretNavigator`, for `move`:
  - along the line axis: `PositionNavigator`, the logical order, unchanged;
  - across lines: the stop of the previous or next line (in the same flow)
    nearest to `goal`, the line-axis coordinate remembered by the editor
    (standard "goal column" behaviour, reset by any non-vertical move);
  - with `INNER` granularity, it first tries the nearest inner stop in that
    direction inside the current item (up/down between the rows of a stacked
    quadrat, a todo 0072 item);
  - at a flow's edge: it continues in the parent flow, from the zone box's
    edge.
- `SelectionLocator` is rewritten on stops: the union, per line, of the item
  bounds between two stops. It is used for top-level and nested selections
  alike, so `ViewDrawer.drawSelection` and `drawNestedSelection` disappear.

### D.7 Decorations and painting
- `CursorDecoration(CursorShape shape, JSeshStyle style)`, `ABOVE_CONTENT`.
- `SelectionDecoration(SelectionShape shape, Color color)`, `BELOW_CONTENT`.
- `HoverDecoration`, later.
- **Phase 5**: `JMDCEditor.paintComponent` calls `ScenePainter` with
  `DocumentBox` and the decorations. `ViewItemBox.paintSelf` calls a new
  `ViewDrawer.drawSubtree(Graphics2D, rc, trc, MDCView)`, which draws without
  selection, since selection is now a decoration. Export
  (`MDCDrawingFacade`, `jsesh.ui.export`) switches to `ScenePainter` later,
  in the same phase or after.

## E. JSesh UI changes (`jsesh.ui.editor`)

- `JMDCEditor` keeps one `DocumentBox` (in a `MdcScene implements Scene`)
  instead of the root `MDCView`. `TextGeometry` is built from it on demand,
  which is cheap. Display scale handling moves to `JSceneDisplayer`
  conversions. The editor can delegate to one, or keep extending its current
  base class and call `ScenePainter`.
- `MDCViewUpdater` becomes a thin adapter: model operation →
  `BoxTreeUpdater.apply` → `scene.setRoot` → repaint of the dirty area.
- `JMDCEditorWorkflow`:
  - `cursorUp`/`cursorDown` call `TextGeometry.move(…, goal)` and keep a
    `goal` field;
  - new `cursorLineStart`/`cursorLineEnd` (`END_OF_LINE` exists, a Home
    action is added);
  - labels go in `jseshLabels`.
- `JMDCField` (single line): same scene, with a `LineFlowLayout` flow and no
  breaks.

## F. Phases

Each phase leaves the application working and is tested on its own.
Phases 1–5 change no visible behaviour, except up/down.

| # | Phase | Done when |
|---|---|---|
| 1 | Generic toolkit `org.qenherkhopeshef.viewtoolkit.box` (C.1–C.7; C.8 can wait for phase 5). Delete the empty `textOriented`. | Unit tests in C pass. |
| 2 | **Lines kept**, with no change to drawing. `LineLayout`/`ColumnLayout` record each flushed zone as a `LineRecord(startGap, endGap, bounds)` on the root view. `BoxTreeBuilder` wraps the *already positioned* top-level `MDCView`s into `DocumentBox → FlowBox → LineBox → ViewItemBox`, with transforms taken from their positions. | Every top item is in exactly one line, in order. Each item box's bounds in root coordinates equal its `MDCView`'s, for a corpus with breaks, tabs, justification, RTL and columns. |
| 3 | **Geometry on boxes**: `CaretStopTable`, `ClickResolver`, stop-based `SelectionLocator`, and `TextGeometry` moved to `jsesh.render.box` and built on `DocumentBox`. The old locators are kept as test oracles, then deleted. | `CursorLocatorTest`, `TextGeometryTest`, `JMDCEditorMouseTest`, `InnerCursorRenderingTest` and `PositionNavigatorTest` are green. For every stop of the corpus, the new and old results are equal. |
| 4 | **Geometric navigation**: `CaretNavigator` with a goal coordinate, workflow up/down, Home/End, and INNER up/down in stacked quadrats. | Tests on multi-line texts in the four orientation/direction combinations. Manual check in the application. |
| 5 | **Painting through the scene**: `ScenePainter`, decorations, `ViewDrawer.drawSubtree`, `JMDCEditor` painting, and optionally `JSceneDisplayer`. | **Pixel-identical** `BufferedImage` comparison against the old `ViewDrawer` path for the corpus, with and without selection. |
| 6 | **Layout through strategies**: `FlowLayout`, `LineFlowLayout`, `ColumnFlowLayout`, `Justifier`, `ItemBoxFactory`, and incremental `BoxTreeUpdater` replacing `MDCViewUpdater` internals. The `TopItemLayout` family and `layout.Zone` (→ `LineAccumulator`) are retired. | Pixel-identical again, and an incremental update gives the same tree as a full build (structural comparison after random edits). Edit latency on a long text (e.g. Horus and Seth) is no worse. |
| 7 | **Zones** (G): model, `AstModelBuilder.visitZone`, writer, `ZoneStart` removed, `ZoneBox` with inline placement, navigation and editing as a scope, "insert zone" action. | `%zone(...)` round trips (nested, with options). Click, typing, arrows and selection inside a zone behave as inside a cartouche. A vertical zone inside horizontal text renders correctly. |
| 8 | **Cartouche content as a flow** (`CartoucheBox`). Optional: long cartouches split over lines (fragments). | Cartouche tests unchanged, and pixel-identical rendering. |
| 9 | **Free placement, pictures, anchored zones**: `FreeFlowLayout`, `PictureBox`, out-of-flow zones (`QuadTreeBoxIndex` if measurements call for it). | Depends on the decisions in H. |
| 10 | **Fine-grained leaves** (`GlyphBox`, `ShapeBox`) for hit-testing signs inside ligatures, hover, "select this word". On screen, the `ElementDrawer` dispatch retires in favour of self-drawing boxes. | Pixel-identical rendering, and n‑n hit tests on complex ligatures. |

## G. The zone model (phase 7)

### G.1 Classes (`jsesh.model`)
- `Zone extends TopItem`. It is not a `BasicItem`, so it can't go in a
  cartouche, at least at first. It has one child, `ZoneContent`, and a
  `ZoneParameters` value. `accept(v)` calls `v.visitZone(this)`.
  `isNavigable()` is true.
- `ZoneContent extends ModelElement implements ItemSequence`. It accepts
  every `TopItem` except `PageBreak`, including `LineBreak`, `TabStop` and
  nested `Zone`. `edgeGapsAreStops()` is true, and `isCursorStop(0, QUADRAT)`
  is true, so clicks and QUADRAT mode enter it, as `BasicItemList` does today.
  Positions inside are `(z, 0, k)`.
- `record ZoneParameters(ZoneLayoutKind layout, TextOrientation orientation,
  TextDirection direction, int width, int height, int lineSkip, int columnSkip,
  int ruleWidth, ZoneFrame frame, ZonePlacement placement, int refX, int refY)`:
  - `ZoneLayoutKind { LINES, COLUMNS, FREE }`;
  - `ZoneFrame { NONE, RECTANGLE }`;
  - `ZonePlacement { INLINE, ANCHORED }`;
  - `DEFAULT` is inline, lines, and the orientation and direction inherited
    from the parent. The fields come from `ZoneStart`.
- `ModelElementVisitor`, `ModelElementAdapter`, `ModelElementDeepAdapter`:
  `visitZone` replaces `visitZoneStart`. **`ZoneStart` is deleted**, together
  with the empty `DefaultElementDrawer.visitZoneStart` and the TODO in
  `MdCModelWriter`.
- `deepCopy`, `compareToAux`, `equalsIgnoreIdAux`, following `Cartouche`'s
  pattern.

### G.2 Reading and writing
- `AstModelBuilder.visitZone(AstZone)` builds a `Zone` from `node.list()`
  (the same path as the top items of the text), and `ZoneParameters` from
  `node.options()`. The parser and AST are already done.
- Proposed option keys, to confirm (see H): `layout=lines|columns|free`,
  `o=h|v`, `d=lr|rl`, `width`, `height`, `lineskip`, `columnskip`, `rule`,
  `frame`, `x`, `y`.
- `MdCModelWriter.visitZone` writes `%zone[…](…)`, with only the non-default
  options.
- Tests: parse → write → parse round trip, nested zones, an empty zone, and a
  zone at the end of the text.

### G.3 Layout
- An `INLINE` zone is an `ItemBox` in its parent's line, like a big quadrat.
  Its size is the content flow's size plus frame and rules. Its baseline
  alignment follows the cartouche's `deltaBase` logic.
- `COLUMNS` inside a horizontal parent (and the reverse) is what gives
  "columns + lines".
- `ANCHORED` (phase 9) is out of flow. It doesn't advance the parent's
  current point. The parent `FlowBox` keeps a separate list of out-of-flow
  boxes in a `LinearBoxIndex`.
- `FREE` (phase 9) needs per-item coordinates, possibly with the existing
  `&&` absolute-group mechanism as a model (see H).

### G.4 Editing
- `ZoneContent` is an `ItemSequence`, so `TextScope`, `EditingScope`,
  `InnerEdit` and the scoped commands work unchanged.
- A new workflow action, "Insert zone" (around the selection or empty),
  with labels in `jseshLabels`.
- Deleting a zone as a whole uses the same mechanism as for cartouches.

## H. Open questions (to decide before the relevant phase)

1. Zone option names and short forms (G.2). Does a zone inherit the
   orientation of its parent by default?
2. Can zones appear inside cartouches (making `Zone` a `BasicItem`)?
3. Free placement: where do coordinates live? On each item of a `FREE`
   zone (new item options?), or does a free zone only accept absolute
   groups?
4. Pictures: what is the model element and syntax? How are files embedded or
   referenced? This also affects `jsesh.io`.
5. Paged mode: should the page split be a `PageBox` over line ranges (as
   proposed), or stay a separate pass for printing only?
6. Should the old `org.qenherkhopeshef.viewtoolkit.drawing.*` classes be
   deprecated once `box` exists?
7. Does `JMDCEditor` extend or delegate to `JSceneDisplayer` (E)?

## I. Layering impact (to record in `jsesh-package-dependencies.md` as the phases land)

- New: `jsesh.render.box`, above `render.view`, `render.layout` and
  `render.draw`, and below `jsesh.ui.*`. `TextGeometry` moves there, so that
  there is no `draw ↔ box` cycle.
- `jsesh → qenherkhopeshefUtils` already exists (an `implementation`
  dependency in `jsesh/build.gradle.kts`), so no new module edge is added.
- `jsesh.model` gains `Zone`, `ZoneContent` and `ZoneParameters`, and loses
  `ZoneStart`. There is no new outgoing dependency.

## J. Files concerned by the phases

- Generic: `qenherkhopeshefUtils/src/main/java/org/qenherkhopeshef/viewtoolkit/**` (new `box/`).
- Layout: `jsesh/src/main/java/jsesh/render/layout/{Layout,TopItemLayout,LineLayout,ColumnLayout,Zone,TextRun}.java`.
- Views and drawing: `jsesh/src/main/java/jsesh/render/view/{MDCView,ViewBuilder}.java`, `render/draw/{TextGeometry,PlacedView,CursorLocator,PositionLocator,SelectionLocator,ViewDrawer,CursorShape,SelectionShape,MDCDrawingFacade}.java`.
- Model: `jsesh/src/main/java/jsesh/model/{ZoneStart,Cartouche,BasicItemList,ItemSequence,PositionNavigator,ModelElementVisitor,ModelElementAdapter,ModelElementDeepAdapter}.java`.
- Reading and writing: `jsesh/src/main/java/jsesh/mdcreader/AstModelBuilder.java` (`visitZone`), `jsesh/io/mdc/MdCModelWriter.java`. Already done: `parser/MDCParser.parseZone`, `parser/ast/AstZone`.
- UI: `jsesh/src/main/java/jsesh/ui/editor/{JMDCEditor,JMDCField,MDCViewUpdater,JMDCEditorWorkflow,MDCEditorKeyManager}.java`.

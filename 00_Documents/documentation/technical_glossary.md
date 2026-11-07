# JSesh technical glossary

JSesh is old, and its vocabulary comes from several sources: Egyptology, the Manuel de
Codage, typesetting, and thirty years of code. Some words are used for two different
things (*wrap*, *scope*, *ligature*, *glossary*). This file defines the words which
are easy to misunderstand when reading the code.

It is a reference for **human** programmers. For how to *use* the library, see
[programmer_documentation.md](programmer_documentation.md); for the syntax of the
encoding, see [manuel-de-codage.md](manuel-de-codage.md); for the packages which may
depend on which, see [jsesh-package-dependencies.md](jsesh-package-dependencies.md).

**Conventions.** Class names are given without their package when the package is the
obvious one (`jsesh.model` for the model, `jsesh.render.*` for rendering). Each entry
says, when it matters, **where the information lives**:

- *document*: in the model, so saved, exported and copied;
- *style*: in `JSeshStyle`, so a rendering parameter (the document may save part of it);
- *editor only*: a view option of the Swing editor, not saved, exported or copied.

**Adding an entry.** Keep it short (2-4 lines), name the classes, and say what the term
is *not* when there is a trap. Entries are alphabetical inside each section.

## Index by domain

- [Line wrapping and pages](#line-wrapping-and-pages): hard line break, soft wrap, wrap
  width, wrap to component width, pagination, line alignment, logical line, justification,
  wrap in zone
- [Text and model](#text-and-model): quadrat, hbox, inner group, ligature, insert,
  overwrite, absolute group, philology, kerning, absolute tab, tab (tab stop, tab cell, tab
  column), zone, layout settings, unit, document header, scope, position, range,
  mark, caret granularity, alphabetic text, superscript
- [Parsing and writing MdC](#parsing-and-writing-mdc): MdC, literal AST, model builder, lexer
- [Signs and fonts](#signs-and-fonts): Gardiner code, phonetic code, shape repository,
  hieroglyph database, glossary, hieroglyph resources
- [Rendering](#rendering): style, render context, layout unit, view, layout, lone sign
  fitting, text geometry, device
- [Editor and user interface](#editor-and-user-interface): document, text model, workflow,
  action id, labels
- [Project and build](#project-and-build): modules, JS port, fixtures, genuine singletons

---

## Line wrapping and pages

The word *wrap* names two unrelated things in the code. The first one is about lines of
text (this section). The second one, *Wrap in Zone*, is about zones (see below). Do not
mix them up.

### Hard line break

An explicit end of line (`!` in MdC; end of column in vertical text). It is a
`TopItem` of the model, `LineBreak`: it is part of the document, and it is saved,
exported and copied. Compare with [soft wrap](#soft-wrap).

The *end of page* (`!!`) is the same kind of thing: `PageBreak`. In a zone, a page break is
laid out as a line break. Both implement `LineEnd`: they carry the
[alignment](#line-alignment) of the line they end.

### Soft wrap

A line end which is **not** in the text: the layout cuts the line because the next
element would not fit in the available width. Nothing is added to the model, and no
view is added either, so that the index of a subview is still the index of the model
child (carets, selections and `MDCViewUpdater` are not affected).

- Done by `LineLayout`, for horizontal text only. **Columns are never wrapped.**
- Active when `GeometrySpecification.isWrapping()` is true, i.e. when a page is defined
  or when `wrapWidth` is positive.
- Never cut: an alphabetic run is cut only after a space or punctuation (or anywhere in
  a word longer than a line), and an element kerned to the previous one stays with it.
  An element wider than the line is placed alone on its line.
- Where it lives: *style* (the width), never *document* (the cuts themselves).

### Wrap width

The width at which lines are cut (`GeometrySpecification.wrapWidth`, in
[layout units](#layout-unit), margins included; 0 means "no wrapping"). It comes
from one of the following, from the strongest to the weakest:

1. **the page** (`PageSpecification`): when a page is defined, its usable width is used
   and `wrapWidth` is ignored. *Document* (it is saved in the document header);
2. **the width of a zone** (`%zone[width=…]`): a horizontal zone with a width wraps its own
   content at that width, and never inherits the wrapping of the enclosing text
   (`Layout.zoneRenderContext`). *Document*;
3. **`wrapWidth` in the style**: set by the code which embeds JSesh. *Style*;
4. **the width of the editor window**, see next entry. *Editor only*. The page prevails
   over it (`JMDCEditor.getDisplayRenderContext`).

`GeometrySpecification.maxLineWidth()` is the width available for the *content* of a line.

### Wrap to component width

The **only** kind of wrapping which depends on the current state of the editor. When
`JMDCEditor.setWrapToComponentWidth(true)` (menu *View > Wrap Lines*,
`ToggleWrapLinesAction`, remembered in the user preferences), the lines are cut at the
width of the component (in pixels, divided by the editor's scale). The editor tracks the
width of its viewport (`Scrollable`) and lays the text out again when it is resized.

It is a **view option**: it is added to the render context of the editor's own view by
`getDisplayRenderContext()`, and nowhere else. So the document and its style do not change,
and printing, exporting and copying are not affected. The sign palette uses it for sign
descriptions.

In this mode, the display is optimised for reading, not WYSIWYG: the
[alignment of the lines](#line-alignment) is ignored (`LayoutOptions.ignoreLineAlignment`,
set by `getDisplayRenderContext()`). The content of a zone keeps its alignment, as it has its
own width (`Layout.zoneRenderContext` resets the option).

*JS port*: `<jsesh-editor wrap-lines>` (`EditorView.wrapLines`); `<jsesh-text>` has no
such attribute (use `jseshStyle.geometry.wrapWidth`).

### Pagination

Cutting the text into pages: a line which does not fit in the page height goes to the top
of the next page. It needs a page *with a height* (`PageSpecification.isPaginated()`);
a page without height only wraps lines. `PageSpecification.withoutHeight()` is used to draw
a fragment (a selection copied as a picture) which should not be as tall as a page. In the
view, pages are stacked, `pageStride()` apart (page height plus `PAGE_GAP`).

### Line alignment

How a line is placed in the width available to it: `LineAlignment` (`jsesh.sharedvocabulary`),
`START` (the default), `END`, `CENTER` or `JUSTIFY`. Written on the [hard break](#hard-line-break)
which ends the line: `-![align=center]`, `-!=150%[align=end]`, `-!![align=justify]`. *Start*
and *end*, not *left* and *right*: the layout works in left-to-right coordinates, and
right-to-left text is drawn mirrored, so a *start* line is on the right there.

- A line break without `align` is `START`. The last line of a text or zone, when it has no
  line break, takes the alignment of the line before it.
- The width: the page's usable width or the zone's width if there is one
  (`GeometrySpecification.maxLineWidth()`), else the visible width of the longest line of the
  text (or zone).
- Applied by `LineLayout` at the end of the layout (`LineAccumulator.alignTo`).
- Ignored when [wrapping to the component width](#wrap-to-component-width).
- **Columns** (vertical text) use the same `align` option and the same enum, along the y axis:
  `START` is the top, `END` the bottom, `JUSTIFY` shares the free height between the units of
  the column. The height: the usable height of the page if it has one, or the height of a
  `%zone[layout=columns,height=…]`, or `GeometrySpecification.columnHeight` (minus the top margins)
  (`GeometrySpecification.maxColumnHeight()`), else the visible height of the longest column.
  Applied by `ColumnLayout` (`LineAccumulator.alignVerticallyTo`). The view of the line break,
  which gives a column its minimal size, is not part of the aligned content
  (`LineAccumulator.addColumnEnd`). Columns are never wrapped, so their alignment is **not**
  ignored when wrapping to the component width (their height doesn't depend on the window).
  The direction of the text doesn't change it (the mirroring is horizontal).
- Editor: `JMDCEditorWorkflow.getLineAlignmentAtCaret()` (empty when the selected lines differ:
  the *mixed* state) and `setLineAlignment(...)`, shown by the combo box of the editor's
  toolbar (`JSeshViewComponent`; its label and icons follow the orientation and direction of
  the text at the caret). A new line break copies the alignment of the line where it is
  inserted.

### Logical line

The text between two [hard breaks](#hard-line-break). When it is [soft-wrapped](#soft-wrap),
it is drawn as several lines, which all get its alignment, except for `JUSTIFY`: the last one
is always placed at the start (the end of a paragraph).

### Justification

The `JUSTIFY` [alignment](#line-alignment): the free space is shared between the units of the
line (a sign, a run of alphabetic text, or a group of kerned elements:
`LineAccumulator.justifyWidthTo`). It always applies, however large the spaces, except to the
last part of a [soft-wrapped](#logical-line) line. A line with a single unit, which has nothing
to distribute, stays at the start. The global
`LayoutOptions.justified` boolean, which justified the whole text, has been removed.

### Wrap in Zone / Unwrap Zone

*Not* a line wrap. *Wrap in Zone* (Text > Zone) puts the selection inside a `%zone(...)`;
*Unwrap Zone* (`ActionsID.UNWRAP_ZONE`, `JMDCEditorWorkflow.unwrapZone`) replaces the
innermost zone around the cursor with its content. See [zone](#zone).

### Known limitations of wrapping

- Up, Down, Home and End are model-based: a wrapped paragraph counts as one line.
- *View > Show Line Breaks* draws a return arrow on hard line breaks only; a soft wrap has
  none, which is how you tell the two apart. Editor only; not ported to the JS port.
- Any change to wrapping or layout must be mirrored in `jsesh-js/packages/core`
  (`render/layout.ts`), with the fixtures re-exported (see [fixtures](#fixtures-js-export)).

---

## Text and model

The model is a tree: `HieroglyphicText` → `TopItem`s (`Cadrat`, `Cartouche`, `LineBreak`...)
→ `HBox` → `HorizontalListElement`s (`Hieroglyph`, `InnerGroup`s, `ComplexLigature`).

### Absolute group

`AbsoluteGroup` (`a**b`): signs placed at explicit coordinates (`{{x,y,scale}}`, in
thousandths of the height of A1). It needs at least two signs.

### Absolute tab

`AbsoluteTab` (`?N`, formerly `TabStop`): the next item is placed at an absolute position
from the start of the line, in tab units (`tabUnitWidth`); from Winglyph. `HRule`
(`{lA,B}`) is a thin horizontal rule. Its positions `A,B` are read and written but no
longer used: the rule fills the width available (at least one quadrat; else the width of its
zone, or the usable width of the page; else the width of the text, see `LineLayout.fitRules`).
Not to be confused with the dynamic [tab](#tab-tab-stop-tab-cell-tab-column) `%tab`.

### Alphabetic text

Latin, transliteration, Greek, Coptic... written as a block in MdC (`+lsome text+s`). In the
model each character is one `AlphabeticCharacter`, so that the caret can move in the text;
writers gather them back into runs (`jsesh.model.tools.AlphabeticRuns`). MdC `^x` is *one*
character (an uppercase `x`).

### Caret granularity

`CaretGranularity`: `QUADRAT` (the caret stands between top-level items) or `INNER` (it
also stands between the rows and elements inside a quadrat). `PositionNavigator` lists the
places where the caret may stand for a given granularity.

### Hbox

`HBox`: one row of a quadrat, a horizontal list of elements. A `Cadrat` is a vertical
list of `HBox`es (`a:b` is two rows, `a*b` is one row of two elements).

### Inner group

`InnerGroup`: base class of the things which can stand in a row: `Hieroglyph`,
`Ligature`, `Cartouche`, `SubCadrat`, `Philology`, `Overwrite`, `Insert`, `AbsoluteGroup`,
`Kerning`. Not to be confused with a `TopItem` (which stands in the text itself).

### Insert

`Insert` (`%insert[dir=bs](D36,(n:t))`): a group placed in the free space of a sign,
computed from the actual shapes. Its two children are `SubCadrat`s in reading order; the
`target` option says which one is the main element. Compare with [ligature](#ligature).

### Kerning

`Kerning` (`%hkern`, `%vkern`): an operator which moves the groups before and after it
closer. It has no content, and acts only on its own axis (`KerningAxis`); elsewhere it is
ignored. *View > Show Kerning Marks* is an *editor only* option.

### Ligature

Three things share the word:

- `Ligature` (`a&b`): signs joined, the smaller one placed in a *ligature zone* of the
  larger one (or a predefined ligature, if one exists). Only signs, not groups;
- `ComplexLigature` (`t^^^w`, `w&&&t`): a whole group placed in the ligature zones of a
  sign. It is a `HorizontalListElement`, not an `InnerGroup`;
- the **legacy ligatures** of the *Text* menu: the old operators, kept in their own submenu.

For placing a group using the real shapes, use an [insert](#insert).

### Mark, position, range

- `MDCPosition`: a place in the text. A plain immutable value, **a path of integers** in the
  tree (all but the last are child indices, the last is a *gap* index). It holds no
  reference to the text: ask the `HieroglyphicText`.
- `MDCRange`: two ordered positions. Use `MDCRange.ofUnordered` for a user selection.
- `MDCMark`: a long-term position which **follows the modifications** of the text, like a
  mark of the Tk text widget. Must be released.

### Overwrite

`Overwrite` (`a##b`): one sign drawn over another.

### Philology

`Philology`: philological brackets. In current JSesh each mark (`[[`, `]]`...) is **a sign**;
a `Philology` element exists only when reading `.hie` files (tksesh), where the marks are
parentheses.

### Quadrat (cadrat)

`Cadrat`: the "ideal square" of hieroglyphic writing, a group of signs written as a unit.
The French spelling stayed in the code: *cadrat* in English texts means *quadrat*.
`SubCadrat` is a quadrat-like group inside a row (`n*(t:r)`).

A sub-quadrat is a *syntactic* device, not a layout one. It is needed when a stack or a
sequence must sit inside a row (`n:(x:t)*nxt`, `(i-w)`), but a *useless* one — the only
content of the only row of its quadrat, as in `i-(mn:n)` — is dropped when reading
(`AstModelBuilder`) and not written (`MdCModelWriter`); `i-((mn:n))` is just `i-mn:n`.
See `Cadrat.uselessSubCadratContent()`.

Likewise, a sub-quadrat which only adds parentheses is flattened: `x*(a*b)` is `x*a*b`
(single-row sub-quadrat inserted in the row), `a:(b:c)` is `a:b:c` (sub-quadrat alone in its
row: its rows replace that row). Shaded sub-quadrats and ones with kerning operators are kept.
The model may keep the nested form while editing (`a:(b:c)` before adding `*t`); the flattening
happens when reading and writing MdC. See `Cadrat.withoutUselessSubCadrats()`.

### Scope (text scope)

`TextScope`: a sequence of quadrat-level items that can be edited like a whole text. The
text itself is the root scope; the content of a cartouche or of a `SubCadrat` are nested
scopes, designated by a path. `ItemSequence` is the interface; `EditingScope` (in
`jsesh.document`) presents a scope of a `HieroglyphicTextModel` as if it was a whole text.
Here *scope* has nothing to do with zones, though a `ZoneContent` is a scope as well.

### Superscript

`Superscript` (`|a-`): a text above the line, used for line numbers. A `TopItem`, allowed
at top level only.

### Tab, tab stop, tab cell, tab column

`Tab` (`%tab`, `%tab[n=3,align=end]`): a *dynamic* tab, as in a LaTeX `tabular`. The tabs
of a zone cut its lines into **tab cells**; a **tab stop** is a position, number *i*,
shared by all the lines of the zone (its **tab scope**), and pushed by the longest cell
which ends there. Stop 0 is the start of the line, never written. A **tab column** is the
interval between two consecutive stops; a cell may span several columns when indexes are
skipped.

- `n`: the absolute index of the stop (default: the previous tab of the line, plus 1);
- `align`: the alignment of the cell **before** the tab (default `start`). The cell after
  the last tab takes the alignment of the line.
- `%zone[tabgap=N]` (`ZoneParameters.tabGap`): the minimal space between a cell and the
  next stop; the quadrat gap by default.

Laid out only in zones (`TabLayout`, called by `LineLayout` and `ColumnLayout` at the end of
the layout, before line alignment); ignored in the main text, and after the first soft
wrap of a logical line. Not yet in the JS port. `TabLine` gives the stop index of a tab in
the model, and moves a tab to another stop (editor: Text > Tabs; View > Show Tabs). Spec and progress in todo 0131. Not to be
confused with the absolute tab `?N`.

### Top item

`TopItem`: an item of the text itself, between two `-`: a quadrat, a cartouche, a line or
page break, a zone, an absolute tab, a tab, a superscript, a rule, a comment.

### Zone

`Zone` (`%zone[options](content)`): an area of the text with its own layout (lines or
columns), direction and optional size. It is placed in the line like a large quadrat and
may contain anything a text contains, including other zones. Its only child is a
`ZoneContent` (a scope). The whole text plays the role of the outermost zone; its
settings are the `DocumentPreferences`.

The options of a zone are of two sorts (`ZoneParameters`): the *layout settings*, shared
with the document (see below), and the settings proper to a zone: `scale`, `width`,
`height`, `tabgap`, `separator`, `frame`. Lengths are percents of the *unit* of the
document. With `scale=N`, the content is laid out at full size and its view is scaled by
N % (signs, gaps, lines and all); the lengths of the zone are then in its own quadrats.

### Layout settings

`LayoutSetting` (enum, the only place where their names are written) and
`TextLayoutSettings` (a set of values; unset ones are inherited): the settings of the
layout of a text, with the **same names and values** in zone options
(`%zone[linegap=50]`) and in the document header (`++JSesh_linegap 50 +s`):

| name | values | document default | meaning |
|---|---|---|---|
| `layout` | `lines`/`columns` | lines | orientation |
| `dir` | `lr`/`rl` | lr | direction |
| `quadratheight` | % | 100 | maximal height of a quadrat |
| `quadratwidth` | % | 122.22 | maximal width of a quadrat |
| `signheight` | % | 100 | height of A1 |
| `linegap` | % | 33.33 | space between two lines |
| `columngap` | % | 44.44 | space between two columns |
| `quadratgap` | % | 11.11 | space between the quadrats of a line (and the signs of a quadrat) |
| `columnquadratgap` | % | 33.33 | space between the quadrats of a column |
| `cartouchelinewidth` | % | 5.56 | line of cartouches and zone frames |
| `textbaseline` | % | 22.22 | baseline of alphabetic text above the quadrats' bottom |
| `fitsigns` | `yes`/`no` | yes | [lone sign fitting](#lone-sign-fitting) |
| `limitquadratwidth` | `yes`/`no` | yes | in lines, scale quadrats down to `quadratwidth` |
| `centresmallsigns` | `yes`/`no` | no | centre small signs |

The gaps are named by **role**, never by axis: `linegap` is always between lines, even in a
zone in columns. (In `GeometrySpecification`, the fields are still named by axis:
`lineSkip`, `columnSkip`, `smallSkip`, `columnQuadratSkip`; `LayoutSettingsApplier` is the
only correspondence.)

### Unit (of a document)

`DocumentPreferences.unit` (`++JSesh_unit`, `GeometrySpecification.unit`): the height of a
quadrat, in points, 18 by default. It is the **only absolute length of the text**: the
lengths of the layout settings are percents of it, so changing it scales the whole
document. The page and the small body limit stay in points. Not to be confused with the
[layout unit](#layout-unit) (the unit of all the lengths of the geometry, in points).

### Document header

The `++JSesh_xxx value +s` lines at the start of a `.gly` file, read and written by
`DocumentHeaderCodec`. Since `JSesh_Info 2.0`: `JSesh_unit`, the layout settings
(`JSesh_linegap`...), `JSesh_smallbodylimit`, `JSesh_shading`, and the page. A header
without `JSesh_unit` is an old one (all lengths in points, `JSesh_line_skip`...), converted
without loss; the old keys are also written for older versions of JSesh
(`DocumentHeaderCodec.WRITE_LEGACY_KEYS`). The user's default preferences use the same
format.

---

## Parsing and writing MdC

### MdC (Manuel de Codage)

The plain-text encoding of hieroglyphic texts (`i-w-r:a-ra-m-p*t:pt`). See
[manuel-de-codage.md](manuel-de-codage.md).

### Literal AST and model

Two trees, with **no common types**, and neither package imports the other:

- the *literal AST* (`jsesh.parser.ast`, `AstDocument`) is what was written;
- the *model* (`jsesh.model`) is its interpretation.

`jsesh.mdcreader` bridges them (`AstModelBuilder`, `MDCParserModelGenerator`). The AST
stores the lexer's own enums, and their mapping to the model constants is done there.
`jsesh.parser` must not import `jsesh.model`.

### Lexer

`jsesh.parser.lexer.MdcLexer` and `MdcLexicon`, hand-written, on the generic scanner
framework of the `mdwlexer` module. `MDCParser` (recursive descent) drives it directly.
The JFlex and CUP generated versions are retired.

---

## Signs and fonts

### Gardiner code and phonetic code

A *Gardiner code* names a sign by its place in Gardiner's list (`A1`, `G17`, `Aa1`). A
*phonetic code* is a short name (`n`, `nfr`; case matters: `A` is G1, `a` is D36). Both are
sign codes in MdC. `GardinerCode` is a value usable for sorting; `ManuelDeCodage` knows
which codes are standard (a genuine [singleton](#genuine-singletons)).

### Glossary

Two unrelated things:

- `Glossary` (`jsesh.glossary`): the user's map from a transliteration to **precomposed MdC
  text**, used for completion (a key may have several entries);
- the vocabulary of this file.

### Hieroglyph database

`HieroglyphDatabase`: what is known about signs (values, descriptions, variants) through
`SignInfo`. It knows **nothing about shapes**; they belong to a shape repository.

### Hieroglyph resources

`HieroglyphResources`: a record bundling a shape repository, a database and the
possibility repository (completion). Build it with `HieroglyphResourcesBuilder`
(`buildFull`, or `buildFullFromUserPreferences` for "use it like JSesh itself").

### Shape repository

`HieroglyphShapeRepository`: gives the drawing of a sign from its code. Implementations
are composable (`CompositeHieroglyphShapeRepository`: first font that has the sign wins),
or read a directory (`DirectoryHieroglyphShapeRepository`: the file name is the code), or
the built-in fonts (`PredefinedFonts`).

---

## Rendering

### Device

The output target: screen, PDF (iText), RTF, SVG... described by a
`JSeshTechRenderContext`.

### Layout unit

The unit of `GeometrySpecification`: by default, the height of an A1 sign is **18**
(`standardSignHeight`). Page sizes, margins and `wrapWidth` are in layout units. The
*scale* of the device is a magnifying glass on top: it does not change the design.

### Layout

`Layout`, `LineLayout`, `ColumnLayout`: compute the positions and sizes of the views. They
do **not** create views (that is `ViewBuilder`'s job). Short-lived objects.

### Lone sign fitting

In a quadrat, a low broad sign (as `n`, `N35`) alone on its line, at its normal size, is
enlarged to the width of the quadrat (`QuadratLayout.fitLoneSignsToColumnWidth`). It shows
in columns, where a lone `n` fills the column. Not to be confused with the *wide* quadrats
of `%insert` or with `limitQuadratWidthInLines` (which scales quadrats *down*). Option
`LayoutOptions.fitLoneSigns` (layout setting `fitsigns`: document header `JSesh_fitsigns`,
`yes` by default), overridden per zone with `%zone[fitsigns=yes|no]`.

### Render context

Two objects, to be told apart: `JSeshRenderContext` says *what* to draw with (the style and
the shape repository); `JSeshTechRenderContext` says *where* (the `Graphics2D` font context
and the device scale).

### Style

`JSeshStyle`: all the parameters of rendering: `geometry` (`GeometrySpecification`, from
line width to page size, including `wrapWidth`), colours, fonts for non-hieroglyphic text,
rendering options. Immutable; changed through a builder (`copy()`).

### Text geometry

`TextGeometry`, `CursorLocator`, `PositionLocator`: map model positions to points of the
drawing (where to paint the caret, which position is under a click), working from the view
tree only, independently of the drawing recursion of `ViewDrawer`.

### View

`MDCView`: the geometric view of a model element (position, size, subviews), built by
`ViewBuilder` and drawn by `ViewDrawer` through an `ElementDrawer`. y axis is oriented
downwards. A view is deliberately *not* intelligent: layout is somewhere else.

---

## Editor and user interface

### Action ID

`ActionsID`: the identifier of every action in the editor's action map. Menus are built
elsewhere (`JSeshMenuBuilder`, in `jseshAppli`).

### Document, text, and text model

Three levels, see §4 of [programmer_documentation.md](programmer_documentation.md):
`HieroglyphicText` (the text), `MDCDocument` (text plus file metadata), and
`HieroglyphicTextModel` (a live, observable, undoable wrapper used by the editor).

### Labels

All user-visible strings live in the `jseshLabels` module, one `labels_xx.properties` per
language. Add text there, not inline.

### Workflow

`JMDCEditorWorkflow`: the editing process of a text (sign insertion, possibilities, undo...)
behind `JMDCEditor`. A large class, to be split.

---

## Project and build

### Fixtures (JS export)

Test data exported by the `jseshJsExport` module (`./gradlew :jseshJsExport:exportAll`):
ASTs, written MdC, and view trees. The tests of `jsesh-js` compare the port with them.
Re-export after any change of layout or drawing.

### Genuine singletons

`MdcUnicodeTable`, `ManuelDeCodage` and `GnutraceHieroglyphShapeRepository` are singletons
on purpose: each wraps one immutable global piece of data. Do not turn them into
instance-per-caller objects.

### JS port

`jsesh-js/`: a TypeScript port (core, glyphs, elements) which mirrors the Java parser,
model, writer, layout and drawing (to SVG), with the Java names. See `jsesh-js/README.md`
and `jsesh-js/documentation/`.

### Modules

`jsesh` (the library), `jseshAppli` (the application, entry point `jsesh.jhotdraw.Main`),
`jseshLabels`, `mdwlexer` (generic scanner framework), `jseshJsExport`, `jseshSearch`,
`jseshTests` (demo programs, *not* unit tests); the full list is in `settings.gradle.kts`.

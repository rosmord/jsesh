# JSesh `jsesh` module — Programmer Documentation

This document describes how to *use* the `jsesh` core library from your own Java
code. It is organised around concrete use cases rather than around the package
layout. For the internal architecture and package roots, see [jsesh packages dependencies](jsesh-package-dependencies.html) and the package-info files inside the module.

The demo applications in [JSeshDemos](https://github.com/rosmord/jseshDemos) give examples of how to use the library.

The `jsesh` module is a **library**: it has no `main`. The end-user application
lives in `jseshAppli`. Everything below can be embedded in a servlet, a batch
tool, a desktop app, or a test.

## Contents

- [Contents](#contents)
- [1. Core concepts and vocabulary](#1-core-concepts-and-vocabulary)
- [2. Use case: parse Manuel de Codage into a model](#2-use-case-parse-manuel-de-codage-into-a-model)
- [3. Use case: render hieroglyphs to an image or a `Graphics2D`](#3-use-case-render-hieroglyphs-to-an-image-or-a-graphics2d)
  - [Quickest possible — a PNG from MdC](#quickest-possible--a-png-from-mdc)
  - [Drawing onto an existing `Graphics2D`](#drawing-onto-an-existing-graphics2d)
  - [Building the facade explicitly](#building-the-facade-explicitly)
- [4. Use case: read and write `.gly` documents](#4-use-case-read-and-write-gly-documents)
  - [Loading](#loading)
  - [Saving](#saving)
- [5. Use case: turn a model back into MdC text](#5-use-case-turn-a-model-back-into-mdc-text)
- [6. Use case: embed the interactive editor in a Swing app](#6-use-case-embed-the-interactive-editor-in-a-swing-app)
- [7. Use case: query the sign database](#7-use-case-query-the-sign-database)
- [8. Use case: convert hieroglyphs to Unicode](#8-use-case-convert-hieroglyphs-to-unicode)
- [9. Use case: export to PDF, SVG, RTF, EMF…](#9-use-case-export-to-pdf-svg-rtf-emf)
- [10. Use case: walk or transform a model](#10-use-case-walk-or-transform-a-model)
- [11. Resources, fonts and the sign database — how to build them](#11-resources-fonts-and-the-sign-database--how-to-build-them)
- [12. Quick reference: key entry points](#12-quick-reference-key-entry-points)

---

## 1. Core concepts and vocabulary

**Manuel de Codage (MdC)** is the plain-text encoding for Egyptian hieroglyphs
(e.g. `i-w-r:a-ra-m-p*t:pt`). JSesh reads, edits, renders and writes MdC.

Three data types represent hieroglyphic texts, with increasing richness:

| Type | What it is | Where |
|---|---|---|
| `TopItemList` | The **actual hieroglyphic text**: a tree of hieroglyphs, cadrats, cartouches, line breaks… This is the in-memory representation of a text. | `jsesh.model.TopItemList` |
| `MDCDocument` | A `TopItemList` **plus** file metadata (path, encoding, dialect, preferences). What you load from / save to disk. | `jsesh.document.MDCDocument` |
| `HieroglyphicTextModel` | A **live, observable, undoable** wrapper around a `TopItemList`, used by the editor. | `jsesh.document.HieroglyphicTextModel` |

Rule of thumb:

- Batch / server code that only transforms text → work with `TopItemList`.
- Loading and saving files → `MDCDocument`.
- Anything interactive (an editor, undo/redo, change notification) → `HieroglyphicTextModel`.

**Rendering** always needs two contexts:

- a `JSeshRenderContext` — *what* to draw with (style + a font/shape repository),
- a `JSeshTechRenderContext` — *where* to draw (the `Graphics2D`, device scale).

The `MDCDrawingFacade` (see §3) hides both for the common cases.

---

## 2. Use case: parse Manuel de Codage into a model

The entry point is `jsesh.parser.MDCParserModelGenerator`. It returns a
`TopItemList`.

```java
import jsesh.parser.MDCParserModelGenerator;
import jsesh.parser.MDCSyntaxError;
import jsesh.model.TopItemList;

MDCParserModelGenerator generator = new MDCParserModelGenerator();
try {
    TopItemList text = generator.parse("i-w-r:a-ra-m-p*t:pt");
    // ... use the model ...
} catch (MDCSyntaxError e) {
    // e.getLine() / e.getColumn() locate the problem in the input
    System.err.println("Bad MdC: " + e.getMessage());
}
```

Notes:

- `parse(String)` and `parse(Reader)` are both available.
- Pass a `Dialect` to the constructor (`new MDCParserModelGenerator(Dialect.TKSESH)`)
  to read legacy encodings. For plain modern MdC the default is fine.
- `setPhilologyAsSigns(true)` treats `[[`, `]]`, `(` … as ordinary signs instead
  of philological constructs — only needed for very old TkSesh texts.
- **Lower level:** if you don't want a `TopItemList` but want to drive your own
  builder, use `MDCParserFacade` with an `MDCBuilder` implementation
  (see `jsesh.model.api.MDCBuilder` / `MDCBuilderAdapter`). The model generator is
  just `MDCParserFacade` wired to the built-in `MDCModelBuilder`.

---

## 3. Use case: render hieroglyphs to an image or a `Graphics2D`

`jsesh.render.draw.MDCDrawingFacade` is the "one class for programmers who just
want to draw hieroglyphs". It accepts either an MdC `String` or a `TopItemList`.

### Quickest possible — a PNG from MdC

```java
import jsesh.render.draw.MDCDrawingFacade;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

MDCDrawingFacade facade = MDCDrawingFacade.buildDefault();
facade.setCadratHeight(40);                 // approx. height of a quadrat, in px
BufferedImage img = facade.createImage("i-w-r:a-ra-m-p*t:pt");
ImageIO.write(img, "png", new File("word.png"));
```

`buildDefault()` uses the embedded font and the default style. **Caveat:** it does
*not* include the user's own signs — for that, build the facade from a
`JSeshRenderContext` you assemble yourself (see §11).

### Drawing onto an existing `Graphics2D`

Useful when compositing hieroglyphs into a larger drawing (a report, a custom
component, another exporter):

```java
// g is a Graphics2D; x, y the top-left target point.
Rectangle2D bounds = facade.draw("nfr-nfr-nfr", g, x, y);
// bounds tells you the box that was drawn, for layout.
```

`getBounds(...)` computes the same box **without** drawing, so you can size a
component or lay out a page first.

### Building the facade explicitly

```java
import jsesh.render.context.JSeshRenderContext;
import jsesh.render.style.JSeshStyle;
import jsesh.glyphs.fonts.PredefinedFonts;

JSeshRenderContext ctx =
    new JSeshRenderContext(JSeshStyle.DEFAULT, PredefinedFonts.buildAllEmbeddedFonts());
MDCDrawingFacade facade = new MDCDrawingFacade(ctx);
```

Swap `JSeshStyle.DEFAULT` for a customised `JSeshStyle` to change sign spacing,
line thickness, shading, etc. Use `facade.setStyle(...)` to change it later.

Other knobs: `setDeviceScale(double)` (pixels per typographic point — raise it for
print resolution), `setMaxSize(w, h)` (caps bitmap size).

There is a runnable example in
[`jseshTests/.../MdcDrawingFacadeDemo.java`](../../jseshTests/src/main/java/jsesh/demo/drawing/MdcDrawingFacadeDemo.java).

---

## 4. Use case: read and write `.gly` documents

Files carry more than the model: an encoding, a dialect, and document
preferences (orientation, direction…). Use `MDCDocument` with the reader/writer
in `jsesh.io.document`.

### Loading

```java
import jsesh.io.document.MDCDocumentReader;
import jsesh.document.MDCDocument;

MDCDocumentReader reader = new MDCDocumentReader();
MDCDocument doc = reader.loadFile(new File("text.gly"));
TopItemList model = doc.getTopItemList();
```

The reader **guesses the encoding and dialect** from the file (BOM, `.hie`
extension, WinGlyph `@` header, MacScribe header, JSesh `++JSeshInfo` header…),
and is *forgiving*: a line it can't parse is kept verbatim as red text rather than
aborting the load. Call `reader.failFast()` beforehand if instead you want a
malformed file to throw `MDCSyntaxError` — e.g. when validating input.

`readString(mdc, file)` builds a document straight from an MdC string, associating
it with a (future) file.

### Saving

```java
import jsesh.io.document.MDCDocumentWriter;

new MDCDocumentWriter().write(doc);        // saves to doc.getFile()
```

Beware: `write(...)` calls `prepareForSaving(doc)`, which **normalises the
document in place** — it forces the modern JSesh dialect, UTF-8 encoding, and a
`.gly` extension (or `.hie`/iso-8859-1 when philology-as-signs is on). Variants:

- `write(doc, OutputStream)` / `write(doc, Writer)` — flush but do not close the
  stream (it stays the caller's).
- `toMdC(doc)` — returns the full document (header included) as a `String`.
- `toMdC(TopItemList, DocumentPreferences)` — convenience when you have a bare
  text and preferences but no real document.

---

## 5. Use case: turn a model back into MdC text

If you only need the MdC source of a `TopItemList` (no file header), use
`jsesh.io.mdc.MdCModelWriter` directly:

```java
import jsesh.io.mdc.MdCModelWriter;
import java.io.StringWriter;

StringWriter out = new StringWriter();
new MdCModelWriter().write(out, topItemList);
String mdc = out.toString();
```

It also has `write(File, TopItemList)` and `write(String fileName, TopItemList)`
overloads. When you want the header too, go through `MDCDocumentWriter.toMdC(...)`
(§4).

---

## 6. Use case: embed the interactive editor in a Swing app

`jsesh.ui.editor.JMDCEditor` is a ready-to-use Swing component (a `JComponent`).
Drop it in a `JScrollPane`:

```java
import jsesh.ui.editor.JMDCEditor;

JMDCEditor editor = new JMDCEditor();
frame.add(new JScrollPane(editor), BorderLayout.CENTER);

editor.setMDCText("i-mn:n-ra");          // load text (ignores syntax errors)
String current = editor.getMDCText();     // read it back
```

Key collaborators (you rarely touch them directly, but they exist for advanced
control):

| Class | Role |
|---|---|
| `JMDCEditorWorkflow` | The editing **state machine** — `getMDCCode()`/`setMDCCode()`, caret, undo/redo, insertion. Get it with `editor.getWorkflow()`. |
| `HieroglyphicTextModel` | The observable/undoable model behind the editor (`editor.getHieroglyphicTextModel()`). Register listeners here to react to edits. |
| `MDCEditorKeyManager` | Keyboard handling. |
| `MDCCaret` | Selection / cursor position. |

Text direction and orientation are set on the editor:
`editor.setTextDirection(TextDirection.RIGHT_TO_LEFT)`,
`editor.setTextOrientation(TextOrientation.HORIZONTAL)`.

The three-argument constructor
`JMDCEditor(HieroglyphicTextModel, JSeshStyle, HieroglyphResources)` lets you
share a model, apply a custom style, and supply your own fonts/signs (§11).

Runnable example:
[`MDCEditorDemo.java`](../../jseshTests/src/main/java/jsesh/demo/swingdemos/MDCEditorDemo.java).
For a single-line input field, see `JMDCField`.

---

## 7. Use case: query the sign database

`jsesh.glyphs.signdata.HieroglyphDatabase` answers questions about the ~6500
signs: transliterations, variants, families, tags, code completion. Get an
instance from the resources (§11):

```java
HieroglyphResources resources = HieroglyphResourcesBuilder.buildWithUserDefinitions();
HieroglyphDatabase db = resources.database();

List<String> values      = db.getValuesFor("G17");          // phonetic values of a sign
String description        = db.getDescriptionFor("A1");
Collection<SignVariant> v = db.getVariants("N35");
PossibilitiesList byValue = db.getPossibilityFor("mn", null); // signs for a phonetic value
PossibilitiesList byCode  = db.getCodesStartingWith("G1");    // code completion
Collection<String> tagged = db.getSignsWithTagInFamily("tall", "A");
```

Highlights of the interface:

- `getValuesFor`, `getDescriptionFor`, `getFamilies`, `getTagsForSign`.
- Variant navigation: `getVariants(code)`, `getVariants(code, VariantTypeForSearches)`,
  and the transitive-closure default `getTransitiveVariants(...)`.
- Search / completion: `getPossibilityFor`, `getCodesStartingWith`,
  `getSuitableSignsForCode`, `getSignsContaining`, `getSignsIn`.

The Gardiner code system itself (parsing `A1`, `G17`, phantom codes…) lives in
`jsesh.glyphs.data.coremdc` (`GardinerCode`, `ManuelDeCodage`).

---

## 8. Use case: convert hieroglyphs to Unicode

`jsesh.model.unicode.MdCToUnicodeConverter` maps a model to the Unicode
hieroglyph block (plus optional format/control characters for grouping):

```java
import jsesh.model.unicode.MdCToUnicodeConverter;

MdCToUnicodeConverter converter = new MdCToUnicodeConverter();
converter.setIncludeFormatControlChars(true);   // emit grouping controls
String unicode = converter.convertToPlainUnicode(topItemList);
```

Combine with the parser (§2) to go straight from MdC text to Unicode.

---

## 9. Use case: export to PDF, SVG, RTF, EMF…

Format exporters live under `jsesh.ui.export.*`. Each format has its own
package; they share the machinery in `jsesh.ui.export.generic`
(`GraphicalExporter`, `AbstractGraphicalExporter`, `SelectionExporter`,
`ExportData`).

| Format | Package / class |
|---|---|
| PDF (iText 2.1.5) | `jsesh.ui.export.pdfExport.PDFExporter` |
| SVG | `jsesh.ui.export.svg.SVGExporter` |
| RTF (with embedded pictures) | `jsesh.ui.export.rtf` |
| EMF / WMF (Windows metafiles) | `jsesh.ui.export.emf`, `jsesh.ui.export.wmf` |
| EPS | `jsesh.ui.export.eps` |
| Mac PICT | `jsesh.ui.export.macpict` |
| Bitmaps (PNG/JPEG) | `jsesh.ui.export.bitmaps` |
| HTML | `jsesh.ui.export.html` |

These classes mix rendering with some UI (option panels), so they are less
"pure" than the drawing facade. For a **headless bitmap** you almost always want
`MDCDrawingFacade` (§3) instead. Reach for these exporters when you need the
specific vector/clipboard format. The generic drawer walks the same
`MDCView`/`ViewDrawer` pipeline the screen uses, so output matches the editor.

The rendering pipeline underneath (should you need it directly) is:
`ViewBuilder.buildView(topItemList, renderContext, techContext)` → `MDCView`,
then `new ViewDrawer().draw(graphics, renderContext, techContext, view)`
(all in `jsesh.render.view` / `jsesh.render.draw`).

---

## 10. Use case: walk or transform a model

The model is a tree of `ModelElement`s with a **visitor** and an **observer**
pattern (see the tree in `CLAUDE.md`). To inspect or transform it, implement a
visitor rather than instanceof-chains.

- Base class: `jsesh.model.ModelElement` (every node accepts a visitor).
- Container root: `TopItemList` → `TopItem`s (`Cadrat`, `Cartouche`,
  `LineBreak`, `ZoneStart`, …); a `Cadrat` holds `HBox`es of
  `HorizontalListElement`s (`Hieroglyph`, `InnerGroup`, `ComplexLigature`).
- The read-only "shape" of these nodes is also published as interfaces in
  `jsesh.model.api` (`HieroglyphInterface`, `CadratInterface`, …) — useful when
  you want to consume the model without depending on concrete classes, and it is
  the same vocabulary the `MDCBuilder` speaks.
- Structural edits and higher-level operations live in `jsesh.model.operations`.
- To be notified of changes, register on the `HieroglyphicTextModel` (§6) or on
  individual elements' observers.

`MdCToUnicodeConverter` (§8) and `MdCModelWriter` (§5) are themselves good,
readable examples of model visitors.

---

## 11. Resources, fonts and the sign database — how to build them

"Resources" bundle the three things rendering and querying need: the sign
**shapes** (fonts), the sign **database** (metadata), and the **possibilities**
(completion). Assemble them with `jsesh.defaults.HieroglyphResourcesBuilder`,
which returns a `HieroglyphResources` record
(`shapes()`, `database()`, `possibilities()`).

Ready-made factory methods cover the usual needs:

```java
import jsesh.defaults.HieroglyphResourcesBuilder;
import jsesh.defaults.HieroglyphResources;

// Only what ships inside the jar — no user signs, no user fonts:
HieroglyphResources embedded = HieroglyphResourcesBuilder.buildEmbedded();

// Embedded fonts + the user's SignInfo definitions (signs_definition.xml):
HieroglyphResources withUser = HieroglyphResourcesBuilder.buildWithUserDefinitions();

// Everything: user font folder + user definitions + a glossary for completion.
// Use this form when you keep the DirectoryHolder/GlossaryManager yourself
// (e.g. because the app also needs to edit and save them back):
HieroglyphResources full =
    HieroglyphResourcesBuilder.buildFull(userFontsDirectoryHolder, glossary);

// Same, but reading the user's own JSesh preferences for you (font directory
// from UserFontDirectoryManager, glossary from GlossaryManager) — the
// convenient one-liner for embedders who just want to reuse the user's setup
// without managing those objects themselves:
HieroglyphResources fromPrefs = HieroglyphResourcesBuilder.buildFullFromUserPreferences();
```

Or build one piece at a time (font order matters — earlier fonts win):

```java
HieroglyphResources res = new HieroglyphResourcesBuilder()
        .addFontDirectory(myFontsDir)                  // user signs override…
        .addFont(PredefinedFonts.buildStandardJSeshFont())
        .addFont(PredefinedFonts.buildGnuTraceFont())  // …fallback last
        .useUserDefinitions(true)
        .glossary(myGlossary)
        .build();
```

For the database alone, `jsesh.defaults.HieroglyphDatabaseFactory` has
`buildPlainDefault(codesSource)` and `buildWithUserDefinitions(codesSource)`.

To feed resources into rendering, wrap the shape repository in a
`JSeshRenderContext` together with a `JSeshStyle` (§3). To feed them into an
editor, pass the `HieroglyphResources` to the `JMDCEditor` constructor (§6).

---

## 12. Quick reference: key entry points

| I want to… | Use | Package |
|---|---|---|
| Parse MdC → model | `MDCParserModelGenerator.parse` | `jsesh.parser` |
| Model → MdC text | `MdCModelWriter.write` | `jsesh.io.mdc` |
| Draw hieroglyphs (image / `Graphics2D`) | `MDCDrawingFacade` | `jsesh.render.draw` |
| Load a `.gly` file | `MDCDocumentReader.loadFile` | `jsesh.io.document` |
| Save a document | `MDCDocumentWriter.write` | `jsesh.io.document` |
| Full document (model + metadata) | `MDCDocument` | `jsesh.document` |
| Editable/observable/undoable model | `HieroglyphicTextModel` | `jsesh.document` |
| Interactive Swing editor | `JMDCEditor` | `jsesh.ui.editor` |
| Query signs / variants / completion | `HieroglyphDatabase` | `jsesh.glyphs.signdata` |
| Hieroglyphs → Unicode | `MdCToUnicodeConverter` | `jsesh.model.unicode` |
| Build fonts + database + completion | `HieroglyphResourcesBuilder` | `jsesh.defaults` |
| Export PDF/SVG/RTF/EMF… | `jsesh.ui.export.*` | `jsesh.ui.export` |
| Low-level render pipeline | `ViewBuilder` + `ViewDrawer` | `jsesh.render.view` / `.draw` |

Runnable examples for several of these live in the `jseshTests` module under
`jsesh.demo.*` (they are demo programs, not unit tests).

---

*Build reminder:* the `jsesh` module contains generated parser/lexer sources
(`MDCParse`, `MDCLex`, `MDCSymbols`). Run `./gradlew build` from the repository
root before working in an IDE, or those classes will be missing.

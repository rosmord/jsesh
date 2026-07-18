# Package Refactoring — `master` → `repackage`

## Overview

The `repackage` branch reorganises the package layout of the `jsesh` core
module, and updates every consumer module accordingly. It is a **structural
change only**: no feature was added or removed, no algorithm was rewritten,
and the full test suite (159 tests) passes on both branches.

Scale of the change:

|                                    |                                                                     |
| ---------------------------------- | ------------------------------------------------------------------- |
| Commits                            | 12                                                                  |
| Files touched                      | 399                                                                 |
| Files moved (git-detected renames) | 265                                                                 |
| Modules affected                   | `jsesh`, `jseshAppli`, `jseshSearch`, `signInfoAppli`, `jseshTests` |
| Classes renamed                    | 1 (`GardinerCodeTest` → `GardinerCodeCanonicalCodeTest`)            |
| Behavioural changes                | 2 (see *Boundary fixes*)                                            |

The old layout had grown historically rather than by design. Names described
either the *file format* (`jsesh.mdc`, `jsesh.mdcDisplayer`) or an
implementation accident (`jsesh.drawingspecifications`, `jsesh.preferences`,
`jsesh.resources` all sitting at top level), and several packages had become
landing buckets that mixed layers — most visibly, core rendering and glyph
code imported `jsesh.swing`.

The top-level package roots of the `jsesh` module before and after:

```
before:  clipboard defaults drawingspecifications editor glossary graphics
         hieroglyphs io mdc mdcDisplayer preferences resources swing utils

after:   clipboard defaults editor glossary glyphs graphics io model
         parser platform render swing utils
```

---

## Principles

Four rules drove the reorganisation.

**1. Domain-first naming.** Packages are named after what they *are about*,
not after the encoding or the class that happens to live there.
`mdcDisplayer` → `render`, `hieroglyphs` → `glyphs`, `mdc.model` → `model`.
Rendering is rendering whatever the source syntax; the sign database is about
glyphs, not about the Manuel de Codage.

**2. Dependencies run downwards, and Swing sits at the bottom of nothing.**
The layering is:

```
       jsesh.swing        (the only UI layer in the module)
            │
   jsesh.editor ── jsesh.io ── jsesh.graphics.export
            │
        jsesh.render
            │
   jsesh.glyphs   jsesh.parser
            │
        jsesh.model
            │
       jsesh.platform
```

Nothing below `jsesh.swing` may import it. After the branch,
**`jsesh.render` and `jsesh.glyphs` import no Swing package at all** — which
was not true before.

**3. One top-level root per concern, subpackages inside it.** Rather than a
flat spread of `drawingspecifications`, `preferences`, `resources`,
`hieroglyphs`, each concern gets a root with intention-revealing
subpackages: `render.{view,layout,draw,elements,context,style}`,
`platform.{preferences,resources,metadata,graphics}`.

**4. Break the API cleanly rather than leave forwarding shims.** JSesh is
used as an embedded library, so this *is* a breaking change for downstream
users. The decision was to take the break in one branch, with no deprecated
forwarding classes left behind, rather than carry a compatibility layer
indefinitely. Migration is mechanical — an import rewrite.

### Migration method

Work proceeded in vertical slices, each kept compile-green
(`./gradlew classes`) before starting the next: fix the boundary leaks first,
then move glyphs, then model/parser, then io, then catch up the consumer
modules. Fixing leaks before the mass moves avoided reproducing the unclear
dependencies in the new layout.

---

## Per-subsystem changes

### Document model — `jsesh.mdc.model` → `jsesh.model`

The largest single move (46 files). The model tree (`TopItemList`, `Cadrat`,
`HBox`, `Hieroglyph`, `Cartouche`, …) is the module's foundation and now
sits directly under `jsesh.model`, with the satellites regrouped as
subpackages:

| Before                       | After                         |
| ---------------------------- | ----------------------------- |
| `jsesh.mdc.model`            | `jsesh.model`                 |
| `jsesh.mdc.model.operations` | `jsesh.model.operations`      |
| `jsesh.mdc.constants`        | `jsesh.model.constants`       |
| `jsesh.mdc.transliteration`  | `jsesh.model.transliteration` |
| `jsesh.mdc.unicode`          | `jsesh.model.unicode`         |
| `jsesh.mdc.interfaces`       | `jsesh.model.api`             |
| `jsesh.mdc.utils`            | `jsesh.model.tools`           |

`jsesh.mdc.interfaces` → `jsesh.model.api` renames the builder/visitor
interfaces used by the parser to what they actually are: the model's public
abstraction surface.

`jsesh.mdc.utils` → `jsesh.model.tools` is an honest rename, **not** a
cleanup — it was a grab-bag before and remains one. See *What was
deliberately left alone*.

### Parser — `jsesh.mdc` → `jsesh.parser`

The parser was living in the same package as the model constants, which made
the model appear to depend on the parser. It now has its own root:

| Before                                    | After              |
| ----------------------------------------- | ------------------ |
| `jsesh.mdc` (facade, `MDCSyntaxError`, …) | `jsesh.parser`     |
| `jsesh.mdc.lex`                           | `jsesh.parser.lex` |

Because the CUP and JFlex sources are code-generated at build time, the
generator configuration in [jsesh/build.gradle.kts](jsesh/build.gradle.kts)
moved with them:

```kotlin
parserPackage.set("jsesh.parser")     // was "jsesh.mdc.parser"
lexerPackage.set("jsesh.parser.lex")  // was "jsesh.mdc.lex"
```

Note this also collapses `jsesh.mdc.parser` and `jsesh.mdc` into one
`jsesh.parser` package — the generated `MDCParse` / `MDCSymbols` / `MDCLex`
now sit beside the handwritten `MDCParserFacade` and
`MDCParserModelGenerator` they are used by.

### Rendering — `jsesh.mdcDisplayer` → `jsesh.render`

The rename that motivated much of the rest. `mdcDisplayer` was
camel-cased, format-named, and had absorbed the drawing specifications from
a separate top-level package.

| Before                                                      | After                                          |
| ----------------------------------------------------------- | ---------------------------------------------- |
| `jsesh.mdcDisplayer.mdcView`                                | `jsesh.render.view`                            |
| `jsesh.mdcDisplayer.layout`                                 | `jsesh.render.layout`                          |
| `jsesh.mdcDisplayer.draw`                                   | `jsesh.render.draw`                            |
| `jsesh.mdcDisplayer.drawingElements`                        | `jsesh.render.elements`                        |
| `jsesh.mdcDisplayer.drawingElements.cartouche`              | `jsesh.render.elements.cartouche`              |
| `jsesh.mdcDisplayer.drawingElements.internal.symboldrawers` | `jsesh.render.elements.internal.symboldrawers` |
| `jsesh.mdcDisplayer.context`                                | `jsesh.render.context`                         |
| `jsesh.drawingspecifications` (top level)                   | `jsesh.render.style`                           |

Folding `jsesh.drawingspecifications` into `jsesh.render.style` puts
`JSeshStyle`, `ShadingMode` and friends where they belong: they are
rendering configuration, not a peer of the renderer.

### Glyph / sign database — `jsesh.hieroglyphs` → `jsesh.glyphs`

| Before                               | After                       |
| ------------------------------------ | --------------------------- |
| `jsesh.hieroglyphs.data`             | `jsesh.glyphs.data`         |
| `jsesh.hieroglyphs.data.coremdc`     | `jsesh.glyphs.data.coremdc` |
| `jsesh.hieroglyphs.data.io`          | `jsesh.glyphs.io`           |
| `jsesh.hieroglyphs.fonts`            | `jsesh.glyphs.fonts`        |
| `jsesh.hieroglyphs.signshape`        | `jsesh.glyphs.shape`        |
| `jsesh.hieroglyphs.utils`            | `jsesh.glyphs.tools`        |
| `jsesh.hieroglyphs.resources`        | `jsesh.glyphs.resources`    |
| `jsesh.swing.signimportdialog.model` | `jsesh.glyphs.signsource`   |

Two things beyond the rename:

- `data.io` was promoted out of `data` to `glyphs.io`: sign-database
  serialisation is a sibling of the database, not part of it.
- `signsource` is a **boundary fix**, described below.

### Boundary fixes

Three leaks were repaired before the mass moves, since moving them unchanged
would have baked the wrong dependency direction into the new layout.

**Glyph repositories → sign-import UI.** The SVG/TTF/BZR/TML sign parsers
lived under `jsesh.swing.signimportdialog.model`, and the glyph repositories
depended on them — so the domain depended on a dialog's package. Inspection
showed those parsers import nothing from Swing at all: they were simply in
the wrong place. Moved to `jsesh.glyphs.signsource`; the dialog now depends
on `glyphs`, reversing the arrow. No interface indirection was needed.

**Render/glyphs → Swing utilities.** `ShapeHelper`, `GraphicsUtils` and
`GeometryHelper` sat in `jsesh.swing.utils` but are pure `java.awt.geom`
mathematics with no Swing involvement. They forced `ShapeChar`,
`HieroglyphPictureBuilder`, `SpecialSymbolDrawer` and `MDCDrawingFacade` to
import a UI package. Moved to `jsesh.platform.graphics`.

**Core → editor, in `MDCDocument`.** This one changed behaviour, and is the
only place where the branch is not a pure move.
[MDCDocument](jsesh/src/main/java/jsesh/io/document/MDCDocument.java)
exposed its internal `HieroglyphicTextModel` to IO callers. It now offers
document-level facade methods instead — `getTopItemList()`,
`setTopItemList()`, `appendTopItems()`, `readTopItemList()`,
`isPhilologyIsSign()` / `setPhilologyIsSign()`.

It also no longer throws the Swing-derived
`org.qenherkhopeshef.swingUtils.errorHandler.UserMessage` from `save()`:

```java
// before
throw new UserMessage("THIS METHOD CAN NOT SAVE PDF");
// after
throw new IOException("THIS METHOD CAN NOT SAVE PDF");
```

`save()` already declared `throws IOException`, so callers that caught
`IOException` are unaffected — but any caller that specifically caught
`UserMessage` needs updating.

### Input/output — `jsesh.io`

`jsesh.io` previously held only `importer`. It is now the single IO root:

| Before                 | After               |
| ---------------------- | ------------------- |
| `jsesh.mdc.file`       | `jsesh.io.document` |
| `jsesh.mdc.output`     | `jsesh.io.mdc`      |
| `jsesh.mdc.output.dom` | `jsesh.io.mdc.dom`  |
| `jsesh.io.importer`    | unchanged           |

Document-level read/write (`MDCDocument`) is separated from MDC-syntax
serialisation (`MdCModelWriter` and the DOM writer), which used to be split
across two unrelated branches of `jsesh.mdc`.

### Platform — new root `jsesh.platform`

Collects everything that touches the environment rather than the domain,
replacing three top-level packages and one stray:

| Before                                 | After                        |
| -------------------------------------- | ---------------------------- |
| `jsesh.preferences`                    | `jsesh.platform.preferences` |
| `jsesh.resources`                      | `jsesh.platform.resources`   |
| `jsesh.mdc.jseshInfo`                  | `jsesh.platform.metadata`    |
| `jsesh.swing.utils` (geometry helpers) | `jsesh.platform.graphics`    |

`platform.graphics` is deliberately Swing-free: it is `java.awt.geom` maths
that anything above may use.

### Resources — moved in step with their classes

`EmbeddedGlyphsPathResources` and `MdcUnicodeTable` load their data with
`getResourceAsStream("bare_name")`, which resolves **relative to the class's
own package**. Moving the classes without moving the resource directories
compiled cleanly and then failed at runtime, with the sign database silently
not loading. The resource trees were moved to match:

```
jsesh/hieroglyphs/resources/{signs_description.xml, sign_description.dtd,
    basicGardinerCodes.txt}          →  jsesh/glyphs/resources/
jsesh/mdc/unicode/mdc2unicode.txt    →  jsesh/model/unicode/
```

**Any future move of a resource-loading class needs the same treatment.**
This is the single most dangerous class of error in this kind of refactoring,
because nothing in the build catches it.

### Consumer modules

These modules contain no package moves of their own — only import updates
following the `jsesh` core reorganisation:

| Module          | Files touched | Notes                                            |
| --------------- | ------------- | ------------------------------------------------ |
| `jseshAppli`    | 19            | plus the Gradle `application` plugin, below      |
| `jseshSearch`   | 15            | search queries, UI forms, tests                  |
| `signInfoAppli` | 12            | sign-info editor model, presenters, table models |
| `jseshTests`    | 5             | demo programs                                    |

An earlier round of moves had only updated `jsesh/src/main`; `jsesh/src/test`
and these four modules were left uncompilable for several commits and were
caught up in `701370e7`.

### Build — unrelated convenience change

[jseshAppli/build.gradle.kts](jseshAppli/build.gradle.kts) gained the Gradle
`application` plugin, so the app can be started with `./gradlew :jseshAppli:run`
(2 GB heap, and on macOS the dock name and screen menu bar are set). The jar
manifest now derives its `Main-Class` from `application.mainClass` instead of
repeating the literal. This rides along with the branch but is independent of
the repackaging.

---

## What was deliberately left alone

Several areas were identified as needing work but were left untouched, to
keep the branch reviewable as a pure structural change:

- **`jsesh.model.tools`** is still a landing bucket inherited from
  `jsesh.mdc.utils`. Splitting it by responsibility — the plan proposed
  `model.transform` / `model.query` / `model.normalize` — is the natural next
  step and needs a naming decision.
- **`jsesh.graphics.export`** still mixes format encoders with Swing
  presenters and file dialogs. It is the last large mixed-responsibility
  package; the intent is `io.export` plus UI presenters in the UI layer.
- **`jsesh.defaults` and `jsesh.glossary`** are unchanged. `glossary` still
  needs an ownership decision (domain service vs. editor feature) with its UI
  classes split out.
- **`jsesh.swing` vs. `jsesh.ui`.** The plan named the UI root `jsesh.ui`,
  but the widgets still live under `jsesh.swing`, and `CLAUDE.md` documents
  that as the UI layer. Rename or keep — undecided.
- **No automated architecture check exists.** Every layering rule above is
  enforced by convention only. A grep-based forbidden-import check in CI is
  the cheap first version; ArchUnit is the fuller one.

Inert leftovers not removed: orphaned `package.html` javadoc stubs under
`src/main/resources/jsesh/{mdc,mdcDisplayer,hieroglyphs,…}` for packages that
no longer exist, an unread
`mdcDisplayer/preferences/defaultProperties.properties`,
`graphics/export/pdfExport/oldCode`, and `mdc/lex/README.txt`.

`jsesh.newmdc`, an empty experimental package, is gone from the tree.

---

## Migrating downstream code

For code embedding JSesh as a library, migration is an import rewrite. The
mapping in the tables above is complete; the common cases are:

```
jsesh.mdc.model.*            → jsesh.model.*
jsesh.mdc.constants.*        → jsesh.model.constants.*
jsesh.mdc.interfaces.*       → jsesh.model.api.*
jsesh.mdc.utils.*            → jsesh.model.tools.*
jsesh.mdc.MDCParserFacade    → jsesh.parser.MDCParserFacade
jsesh.mdcDisplayer.*         → jsesh.render.*
jsesh.drawingspecifications.*→ jsesh.render.style.*
jsesh.hieroglyphs.*          → jsesh.glyphs.*
jsesh.mdc.file.MDCDocument   → jsesh.io.document.MDCDocument
jsesh.preferences.*          → jsesh.platform.preferences.*
```

Beyond imports, only two things can actually break a compile:
`MDCDocument.getHieroglyphicTextModel()`-based code should move to the new
facade methods, and `catch (UserMessage …)` around `MDCDocument.save()` must
become `catch (IOException …)`.

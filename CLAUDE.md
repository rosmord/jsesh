# CLAUDE.md

## Project Overview

JSesh is a Java hieroglyphic editor for Egyptian texts, using the Manuel de Codage (MDC) encoding standard.

- it can be used as a standalone application (in `jseshAppli`),
- but it's also a library which can be embbeded in other applications.


Developer reflections and a daily log are written in `00_Documents/journal/DevelopperJournal.md`; open todos live under `00_Documents/TODO/` (`01_easy`, `02_important`, `03_longterm`).

For the `jsesh` module's internal package layering (which package may depend on which, generated from actual imports), see `00_Documents/documentation/jsesh-package-dependencies.md`. For how to *use* the library from outside code, see `00_Documents/documentation/programmer_documentation.md`.

## Build Commands

```bash
./gradlew clean build
```

**Important:** The `jsesh` module has generated sources (CUP parser + JFlex lexer). Always run `./gradlew build` from the root before working in an IDE, or the generated classes (`MDCParse`, `MDCLex`, `MDCSymbols`) will be missing.

After a fresh `./gradlew build`, if the IDE still shows errors on generated sources, run **Java: Clean Java Language Server Workspace** in VS Code.

## Module Structure

| Module | Role |
|---|---|
| `jsesh` | Core library: MDC parser, document model, rendering, editor components |
| `jseshAppli` | Main GUI application — entry point: `jsesh.jhotdraw.Main` |
| `jhotdrawfw` | Adapted JHotDraw 7 application framework |
| `jseshGlyphs` | Hieroglyphic font resources |
| `jseshLabels` | i18n labels/resources for all modules |
| `jseshSearch` | Hieroglyphic search/query functionality |
| `qenherkhopeshefUtils` | Shared utilities and Swing helpers |
| `cupAndlex` | CUP (parser generator) and JFlex (lexer). Still carries the old Maven Mojos, compiled `compileOnly`; the Gradle build drives it through `buildSrc` instead |
| `cupruntime` | CUP parser runtime |
| `signInfoAppli` | Sign information editor — entry point: `jsesh.utilitysoftwares.signinfoeditor.Main` |
| `jseshTests` | Demo programs showing library usage (not formal unit tests) |

## Architecture

### Package roots (`jsesh` module)

The module is organised domain-first, bottom to top: base → core → document →
middle → config → UI. Dependencies run downwards; nothing below `jsesh.ui.*`
may import it. Full generated dependency diagrams (with edge counts, and a
running log of layering fixes) live in
`00_Documents/documentation/jsesh-package-dependencies.md` — treat that file,
not this table, as the source of truth when it disagrees.

| Layer | Root | Role |
|---|---|---|
| base | `jsesh.utils` | Shared utilities, incl. `.io` (`DirectoryHolder`) |
| base | `jsesh.platform` | Preferences, resources, metadata (pure leaf, no outgoing deps) |
| core | `jsesh.signcodes` | Gardiner-code identity: `GardinerCode`, `ManuelDeCodage`, `CanonicalCode`, `HieroglyphCodesSource` (pure leaf) |
| core | `jsesh.model` | Document model, plus `.constants`, `.operations`, `.transliteration`, `.unicode`, `.tools`, `.api` |
| core | `jsesh.parser` | MDC parser and lexer (`.lex`), generated and handwritten |
| core | `jsesh.glyphs` | Sign database and shapes: `.signdata`, `.fonts`, `.shape`, `.signsource`, `.tools`, `.resources` |
| document | `jsesh.document` | `MDCDocument`, `DocumentPreferences`, `HieroglyphicTextModel`, undo machinery |
| middle | `jsesh.io` | Document and MDC import/export (`.document`, `.mdc`, `.importer`) |
| middle | `jsesh.render` | Rendering: `.view`, `.layout`, `.draw`, `.elements`, `.context`, `.style` |
| config | `jsesh.glossary` | `Glossary`, `GlossaryManager` — Swing-free; the editor dialog lives in `jsesh.ui.glossary` |
| config | `jsesh.defaults` | App-scoped assembly: `HieroglyphResourcesBuilder`, `HieroglyphResources`, `UserFontDirectoryManager` |
| UI | `jsesh.ui.widgets` | Swing widgets and dialogs (was `jsesh.swing`) |
| UI | `jsesh.ui.palette` | Sign palette |
| UI | `jsesh.ui.clipboard` | Vector clipboard support (was `jsesh.clipboard`) |
| UI | `jsesh.ui.export` | Format exporters (was `jsesh.graphics.export`; still mixes UI presenters with encoders) |
| UI | `jsesh.ui.editor` | `JMDCEditor`, `JMDCField`, and the editing state machine (was `jsesh.editor`) |
| UI | `jsesh.ui.glossary` | Glossary editor dialog/table model |

**Embedding note:** `jsesh.defaults.UserFontDirectoryManager` is the only class
in `jsesh.defaults` that touches `java.util.prefs`. A library embedder that
doesn't want JSesh's own preference tree can ignore it and call
`HieroglyphResourcesBuilder.buildFull(DirectoryHolder, Glossary)` directly with
a bare `DirectoryHolder`/`Glossary`. Conversely, `buildFullFromUserPreferences()`
is the one-line convenience for embedders who *do* want "use it like JSesh
itself" (reads the user's font directory and glossary from prefs for you). See
`00_Documents/documentation/programmer_documentation.md` §11.

### Document Model (`jsesh.model`)

The document model is a tree with visitor and observer patterns:

```
TopItemList                  ← document root (implements MDCFileInterface)
  └─ TopItem subclasses:
       ├─ Cadrat              ← one hieroglyphic "square" (grid cell)
       │    └─ HBox(es)
       │         └─ HorizontalListElement(s)
       │              ├─ Hieroglyph      ← one sign (Gardiner code + modifiers)
       │              ├─ InnerGroup
       │              └─ ComplexLigature
       ├─ Cartouche
       ├─ LineBreak / PageBreak
       ├─ ZoneStart
       └─ TabStop / Tabbing
```

- `MDCFileInterface` is an interface which comes from the parser/builder pattern used by JSesh. It might not be such a great idea in the long run. When we wrote it, the idea was to avoid building a stack to create the final model.
- `ModelElement` is the abstract base; all elements support an observer pattern for change notification.
- `MDCModelBuilder` / `MDCBuilder` — builder interface used by the parser to construct the model incrementally.

### MDC Parser (generated, in `jsesh` module)

- Grammar source: `jsesh/src/jcup/MDCParse.y` (CUP grammar)
- Lexer source: `jsesh/src/jlex/MDCLexAux.l` (JFlex spec)
- Generated into: `jsesh/build/generated-sources/cup` and `.../lex`, in package `jsesh.parser` / `jsesh.parser.lex`
- Generated by the `CupTask` / `LexTask` defined in `buildSrc`
- High-level entry point: `jsesh.parser.MDCParserModelGenerator` (returns a `TopItemList`)
- Low-level entry point: `jsesh.parser.MDCParserFacade` (accepts any `MDCBuilder`)

### Sign/Glyph Database (`jsesh.glyphs`)

- `jsesh.glyphs.signdata.HieroglyphDatabase` — abstraction for querying ~6500 Egyptian signs
- `ManuelDeCodage` / `GardinerCode` (in `jsesh.signcodes`, a separate top-level package, not under `jsesh.glyphs`) — Gardiner sign code system (standard Egyptology codes like `A1`, `G17`)
- `jsesh.glyphs.fonts` — shape repositories (`PredefinedFonts`, `DirectoryHieroglyphShapeRepository`, `CompositeHieroglyphShapeRepository`, `ResourcesHieroglyphicShapeRepository`)
- `jsesh.glyphs.signsource` — importers reading sign shapes from SVG, TTF, BZR and TML

**Resources caveat:** `EmbeddedGlyphsPathResources` and `MdcUnicodeTable` load
their data with `getResourceAsStream("name")`, resolved *relative to the
class's package*. Moving those classes means moving the matching directory
under `src/main/resources` too, or sign loading breaks at runtime only.

### Editor Architecture (`jseshAppli` + `jsesh.ui.editor`)

- `jsesh.jhotdraw.Main` bootstraps the app (loads prefs, icons, glyph DB, creates `JSeshApplication`)
- `JMDCEditor` — core Swing editor component; `JMDCField` — single-line variant, same package
- `JMDCEditorWorkflow` — editing state machine
- `MDCEditorKeyManager` — keyboard input handling
- `MDCViewUpdater` — triggers view refresh after model changes
- `HieroglyphicTextModel` (`jsesh.document`) — wraps `TopItemList` for Swing data binding
- Undo/redo via `UndoManager`

### Rendering (`jsesh.render`)

- Specification-driven rendering via `jsesh.render.style.JSeshStyle`
- Custom device contexts support PDF (iText 2.1.5), RTF, SVG, and screen output
- Vector clipboard support via [JVectClipboard](https://github.com/rosmord/jvectclipboard) also by Serge Rosmorduc

### i18n

All user-visible strings live in `jseshLabels`. When adding UI text, add it there rather than inline.

## Key IDE / Compilation Quirk

If incremental builds complain that `MDCParse` doesn't exist after an initial successful build, delete the `.classpath` and `.project` files in the `jsesh` folder and rebuild. This is a known Eclipse/VS Code artifact from the generated-sources path.

## Bash commands 

- note that we run on a mac.
- macOS sed doesn't support `\b`, so those substitutions silently no-op'd. Use python instead.


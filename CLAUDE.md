# CLAUDE.md

## Project Overview

JSesh is a Java hieroglyphic editor for Egyptian texts, using the Manuel de Codage (MDC) encoding standard.

- it can be used as a standalone application (in `jseshAppli`),
- but it's also a library which can be embbeded in other applications.


Developer reflections and a daily log are written in `00_Documents/journal/DevelopperJournal.md`; open todos live under `00_Documents/tracking/TODO/` (`01_easy`, `02_important`, `03_longterm`).

For the `jsesh` module's internal package layering (which package may depend on which, generated from actual imports), see `00_Documents/documentation/jsesh-package-dependencies.md`. For how to *use* the library from outside code, see `00_Documents/documentation/programmer_documentation.md`.

## Various notes

- The current development is moving towards an improved and more logical API. API compatibility with older code is not a goal.

- older code may contain latin-1 characters (mostly in comments and javadoc). We are currently using UTF-8 everywhere, so if you meet a latin-1 character in a comment, place replace it with its UTF-8 equivalent. You should also signal it.

## Build Commands

```bash
./gradlew clean build
```

The `jsesh` module's MDC lexer is hand-written (`jsesh.parser.lexer`,
built on the generic scanner framework in the separate `mdwlexer` module) —
no generated sources or codegen step remain for it. The JFlex-generated
`MDCLexAux` and its spec (`jsesh/src/jlex/MDCLexAux.l`) were retired in favour
of `jsesh.parser.lexer.MdcLexer`/`MdcLexicon`.

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
| `mdwlexer` | Generic hand-written scanner framework (`org.qenherkhopeshef.mdwlexer`: expressions, DFAs, `Lexicon`/`LexiconBuilder`) that `jsesh.parser.lexer.MdcLexer`/`MdcLexicon` (in `jsesh`) are built on |
| `signInfoAppli` | Sign information editor — entry point: `jsesh.utilitysoftwares.signinfoeditor.Main` |
| `jseshTests` | Demo programs showing library usage (not formal unit tests) |
| `jseshJsExport` | Exports data and golden fixtures for the TypeScript port in `jsesh-js/` (`./gradlew :jseshJsExport:exportAll`) |

## JavaScript port (`jsesh-js/`)

TypeScript port (npm workspaces; `cd jsesh-js && npm test`, `npm run dev` for the examples). `packages/core` mirrors the Java parser, model, writer, layout (`jsesh.render.layout`, `jsesh.render.view`) and drawing (to SVG), keeping Java names; `packages/glyphs` loads the font data exported from Java; `packages/elements` holds the Web Components. Tests compare the port with fixtures exported by `jseshJsExport` (ASTs, written MdC, view trees). When changing the Java layout or drawing, re-run `./gradlew :jseshJsExport:exportAll` and the JS tests. See `jsesh-js/README.md` (build), and `jsesh-js/documentation/` (`user-guide.md`, `customisation.md`, and `maintainer-guide.md` for the architecture).

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
| base | `jsesh.utils` | Shared utilities, incl. `.io` (`DirectoryReference`) |
| base | `jsesh.platform` | Preferences, resources, metadata (pure leaf, no outgoing deps) |
| core | `jsesh.signcodes` | Gardiner-code identity: `GardinerCode`, `ManuelDeCodage`, `CanonicalCode`, `HieroglyphCodesSource` (pure leaf) |
| core | `jsesh.sharedvocabulary` | Constants shared by parser AST and model: `InsertionDirection`, `KerningAxis`, `CartouchePart`, `CartoucheType`, `WordEnding` (pure leaf) |
| core | `jsesh.model` | Document model, plus `.constants`, `.operations`, `.transliteration`, `.unicode`, `.tools` |
| core | `jsesh.parser` | MDC parser (`MDCParser`), lexer (`.lexer`) and literal AST (`.ast`); no dependency on `jsesh.model` |
| reader | `jsesh.mdcreader` | `MDCParserModelGenerator`, `AstModelBuilder`: interprets the parser's AST into the model (the only package depending on both) |
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
`HieroglyphResourcesBuilder.buildFull(DirectoryReference, Glossary)` directly with
a bare `DirectoryReference`/`Glossary`. Conversely, `buildFullFromUserPreferences()`
is the one-line convenience for embedders who *do* want "use it like JSesh
itself" (reads the user's font directory and glossary from prefs for you). See
`00_Documents/documentation/programmer_documentation.md` §11.

### Document Model (`jsesh.model`)

The document model is a tree with visitor and observer patterns:

```
HieroglyphicText             ← document root: the whole text (formerly TopItemList)
  └─ TopItem subclasses:
       ├─ Cadrat              ← one hieroglyphic "square" (grid cell)
       │    └─ HBox(es)
       │         └─ HorizontalListElement(s)
       │              ├─ Hieroglyph      ← one sign (Gardiner code + modifiers)
       │              ├─ InnerGroup
       │              └─ ComplexLigature
       ├─ Cartouche
       ├─ LineBreak / PageBreak
       ├─ Zone                ← %zone[...](...): area with its own layout
       │    └─ ZoneContent    ← an ItemSequence of TopItems (nested zones too)
       └─ TabStop / Tabbing
```

- The interpreted model (`jsesh.model`) and the literal parse AST (`jsesh.parser.ast`) share no types, and neither package imports the other; `jsesh.mdcreader` bridges them.
- `ModelElement` is the abstract base; all elements support an observer pattern for change notification.

### MDC Parser (`jsesh` module)

- Grammar: hand-written recursive descent parser, `jsesh.parser.MDCParser`, which builds a literal AST (`jsesh.parser.ast.AstDocument`). It replaced an earlier CUP-generated parser (`jsesh/src/jcup/MDCParse.y`, retired once checked equivalent construct-by-construct).
- Lexer: hand-written, `jsesh.parser.lexer.MdcLexer`/`MdcLexicon`, built on the generic scanner framework in the `mdwlexer` module (replacement for the retired JFlex-generated `MDCLexAux`, formerly `jsesh/src/jlex/MDCLexAux.l`). `MDCParser` drives `MdcLexer` directly and switches on its `MdcSymbolCode` enum.
- `jsesh.parser` (lexer, parser, AST) does not depend on `jsesh.model`. The AST stores the lexer's own enums (`SignSubType`, `PhilologyKind`, `jsesh.parser.lexer.symbols.ToggleType`, `jsesh.sharedvocabulary.WordEnding`); their mapping to `jsesh.model.constants` codes lives in `jsesh.mdcreader.AstModelBuilder`. Don't add `jsesh.model` imports to `jsesh.parser`.
- High-level entry point: `jsesh.mdcreader.MDCParserModelGenerator` (returns a `HieroglyphicText`)
- Lower-level entry point: `jsesh.parser.MDCParser` itself (returns the literal `AstDocument`, walkable with `jsesh.parser.ast.AstVisitor`)

### Sign/Glyph Database (`jsesh.glyphs`)

- `jsesh.glyphs.signdata.HieroglyphDatabase` — abstraction for querying ~6500 Egyptian signs
- `ManuelDeCodage` / `GardinerCode` (in `jsesh.signcodes`, a separate top-level package, not under `jsesh.glyphs`) — Gardiner sign code system (standard Egyptology codes like `A1`, `G17`)
- `jsesh.glyphs.fonts` — shape repositories (`PredefinedFonts`, `DirectoryHieroglyphShapeRepository`, `CompositeHieroglyphShapeRepository`, `ResourcesHieroglyphicShapeRepository`)
- `jsesh.glyphs.signsource` — importers reading sign shapes from SVG, TTF, BZR and TML

**Resources caveat:** `EmbeddedGlyphsPathResources` and `MdcUnicodeTable` load
their data with `getResourceAsStream("name")`, resolved *relative to the
class's package*. Moving those classes means moving the matching directory
under `src/main/resources` too, or sign loading breaks at runtime only.

**Genuine singletons:** `jsesh.model.unicode.MdcUnicodeTable`,
`jsesh.signcodes.ManuelDeCodage`, and
`jsesh.glyphs.fonts.GnutraceHieroglyphShapeRepository` are logical, not
accidental, singletons — each wraps one immutable, global piece of data (the
MDC↔Unicode table, the Manuel de Codage sign-code table, the Gnutrace shape
repository) that has exactly one valid instance for the whole JVM. Don't
"fix" their singleton pattern into instance-per-caller.

### Editor Architecture (`jseshAppli` + `jsesh.ui.editor`)

- `jsesh.jhotdraw.Main` bootstraps the app (loads prefs, icons, glyph DB, creates `JSeshApplication`)
- `JMDCEditor` — core Swing editor component; `JMDCField` — single-line variant, same package
- `JMDCEditorWorkflow` — editing state machine
- `MDCEditorKeyManager` — keyboard input handling
- `MDCViewUpdater` — triggers view refresh after model changes
- `HieroglyphicTextModel` (`jsesh.document`) — wraps `HieroglyphicText` (`jsesh.model`) with undo/redo and change events, for Swing data binding
- Undo/redo via `UndoManager`

### Rendering (`jsesh.render`)

- Specification-driven rendering via `jsesh.render.style.JSeshStyle`
- Custom device contexts support PDF (iText 2.1.5), RTF, SVG, and screen output
- Vector clipboard support via [JVectClipboard](https://github.com/rosmord/jvectclipboard) also by Serge Rosmorduc

### i18n

All user-visible strings live in `jseshLabels`. When adding UI text, add it there rather than inline.

## Documentation comments

- Write javadoc as Markdown documentation comments (`///`, JEP 467), not `/** ... */`. Existing `/** */` comments are being converted gradually: convert one when you rewrite it, but don't mass-convert unrelated files.
- Links in Markdown comments use reference syntax: `[Foo]`, `[Foo#bar(int)]`, `[text][jsesh.x.Foo]`.
- Link direction versus the package layering (see `00_Documents/documentation/jsesh-package-dependencies.md`):
  - **Downwards or same layer:** a short name is fine if the class is already imported for code.
  - **Upwards** (a lower package pointing to a higher one, e.g. `jsesh.parser` → `jsesh.mdcreader`): allowed, and useful as "see the high-level API", but always write the **fully qualified** name (`[jsesh.mdcreader.MDCParserModelGenerator]`). **Never add an `import` just for a comment**: it creates a real source dependency that the dependency diagrams count.
  - **Non-public targets:** link to a package-private (or private) class or member only from its own package. From another package, public javadoc can't resolve the link. Link to the nearest public entry point instead (e.g. `[jsesh.mdcreader.MDCParserModelGenerator]` rather than the package-private `AstModelBuilder`), or write the name as code.
  - **Outside the current Gradle module** (e.g. from `jsesh` to `jseshAppli` or `jseshSearch`): don't link. Write the name as code, `` `jsesh.search.SomeClass` ``, because javadoc can't resolve it when documenting this module.

## UI changes

Claude can't run the Swing app (or the web editor) interactively, and a green build doesn't show that a UI change works. So after any change to the UI (dialogs, popups, menus, shortcuts, focus, caret/keyboard navigation, clipboard, the web editor), end the answer with:

- a statement that the change was **not tried in the running app** (unless it really was), and
- a short **manual test checklist**: numbered steps, with the exact MdC to type, the keys or menu items to use, and the expected result. Include the edge cases that tend to differ: first window at startup vs a window opened with Cmd-N, left-to-right vs right-to-left and horizontal vs vertical text/zones, an empty document, and (for the web editor) resizing the page.

### Keyboard shortcuts

- Before proposing a menu or key shortcut, grep **all** the action and menu definitions for existing bindings (Shift and Alt variants included) ;
- check the macOS system shortcuts too (e.g. Cmd+Shift+Space is the input-source selector; Cmd+I is italic; Cmd+Shift+C is the cartouche command). 
- List the conflicts you checked in your answer. If every sensible binding clashes, say so and let me decide, rather than reshuffling existing ones.

## Bash commands

- note that we run on a mac.
- macOS sed doesn't support `\b`, so those substitutions silently no-op'd. Use python instead.
- This is macOS: BSD `sed` differs from GNU `sed`. Do not use `sed -i` for edits. Use the Edit tool, or Python for scripted multi-line or indentation-sensitive changes.

## git management

Git will be managed manually. **Never** commit or push. It's up to me. You can of course use git to look at history - temporary local worktrees are fine, but it should never impact the current archive.

## Testing

- Run the tests for ALL modules (the full Gradle build plus the JS port tests), not just the module you touched.
- Any change to Java layout or rendering (line wrapping, kerning, justification, zones, @insert) must be mirrored in the JS port. Run the JS fixture tests to confirm the view trees still match Java. If the JS port can't support a feature yet, say so explicitly.
- Before writing an MdC test fixture, check the MdC syntax in existing tests or the parser. Don't guess.
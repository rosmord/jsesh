Updated todo list

## Plan: Reorganize jsesh Main Packages

Restructure jsesh/src/main/java toward domain-first packages that separate core model/parsing, rendering, editor, UI widgets, and import/export adapters. Keep behavior unchanged while reducing cross-layer leakage (core importing Swing/editor), clarifying ownership, and enabling future module boundaries.

**Steps**
1. Phase 0 - Freeze Target Architecture and Rules:
Define package boundaries and dependency direction rules before moving files.
- Core domain has no Swing/UI dependencies.
- Rendering has no editor command dependencies.
- UI widgets depend on editor/render/core, not inverse.
- Import/export adapters depend on render/core and optional UI presenters.

2. Phase 1 - Establish New Top-Level Domains (parallel with step 3 where independent):
Create domain-first roots (logical target map):
- jsesh.model (current jsesh.mdc.model + related stable interfaces/constants)
- jsesh.parser (current jsesh.mdc parser facade/lexer/parser utilities)
- jsesh.render (current jsesh.mdcDisplayer + drawingspecifications)
- jsesh.editor (retain editor-centric code, but remove UI utility leakage over time)
- jsesh.glyphs (current hieroglyphs data/fonts/signshape/resources)
- jsesh.io (current mdc.file + io.importer + mdc.output writer responsibilities split clearly)
- jsesh.ui (current jsesh.swing reusable widgets/dialogs)
- jsesh.platform (preferences/resources/default wiring that touches environment)

3. Phase 2 - Resolve Immediate Boundary Leaks (depends on 1):
Prioritize leak fixes before mass moves to avoid reproducing unclear dependencies:
- Replace direct core->editor dependency in MDCDocument by introducing a model DTO/adapter in io/editor boundary.
- Remove glyph/render dependencies on jsesh.swing.utils by moving pure geometry/shape helpers into neutral util/render helpers and leaving Swing-only wrappers in ui.
- Decouple glyph font repositories from signimportdialog model types via an interface in glyphs/io.

4. Phase 3 - Split Monolithic/Unclear Buckets (depends on 1,2):
Break ambiguous packages into intention-revealing subdomains:
- jsesh.mdc.utils -> model.transform, model.query, model.normalize (based on class responsibility).
- jsesh.defaults -> platform.resources + platform.fontconfig (builder vs preference-backed manager separated).
- jsesh.graphics.export -> io.export (+ format-specific subpackages), separate presenter/UI forms under ui.export.
- jsesh.glossary -> editor.glossary (if editor feature) or glyphs.lexicon (if domain service); choose one owner and move UI table/editor classes to ui.

5. Phase 4 - Migrate Package-by-Package with Compatibility Window (depends on 2,3):
Move in vertical slices with compile-green checkpoints:
- 4A: render + drawingspecifications consolidation under jsesh.render
- 4B: glyphs domain consolidation under jsesh.glyphs
- 4C: model/parser split under jsesh.model and jsesh.parser
- 4D: io import/export/document read-write consolidation under jsesh.io
- 4E: ui widgets/dialogs under jsesh.ui
During each slice:
- Update imports and package declarations.
- Keep temporary forwarding classes only where needed for staged compile.
- Remove forwarding classes at end of slice (since package breaks are allowed).

6. Phase 5 - Address Legacy/Experimental Zones (depends on 4):
- Evaluate jsesh.newmdc (currently near-empty/experimental): archive to dedicated experimental namespace or remove if unused.
- Decide fate of jsesh.mdc.interfaces vs model public API interfaces (retain only externally meaningful abstractions).
- Normalize naming inconsistencies (e.g., mdcDisplayer -> render).

7. Phase 6 - Enforce Architecture and Verify (depends on 4,5):
Add architectural checks (build-time or test-time) for forbidden dependencies, and verify no behavior regression:
- core/model/parser must not import jsesh.ui or Swing helper packages.
- render must not import ui dialog/presenter classes.
- glyphs domain must not import ui packages.

**Relevant files**
- MDCDocument.java — currently imports editor model type; primary boundary-fix candidate.
- DirectoryHieroglyphShapeRepository.java — currently coupled to sign-import UI model.
- ResourcesHieroglyphicShapeRepository.java — same coupling pattern as above.
- ShapeChar.java — depends on swing shape helper.
- HieroglyphPictureBuilder.java — depends on swing graphics helper.
- SpecialSymbolDrawer.java — depends on swing shape helper.
- MDCDrawingFacade.java — depends on swing graphics helper.
- package-info.java — already documents layering; good basis for platform split.
- package-info.java — should move with render domain.
- DevelopperJournal.md — records prior intent for model/component package strategy and dependency concerns.

**Verification**
1. Build after each migration slice: run ./gradlew :jsesh:classes (or root ./gradlew classes) and keep each slice compile-green.
2. Add/execute architectural import checks (e.g., ArchUnit or lightweight forbidden-import tests) for core/model/parser/render/glyphs/ui boundaries.
3. Run smoke scenarios for editor rendering and import/export flows in jseshAppli after each major slice.
4. Confirm generated parser classes remain resolved by running full root build once near completion.

**Decisions**
- Use domain-first naming (model/parser/render/editor/glyphs/io/ui/platform).
- Package/API breaks are acceptable for this reorganization.
- Prefer phased migration with temporary bridges only when they reduce risk inside a slice; remove quickly.

**Further Considerations**
1. Glossary ownership choice: place domain objects under glyphs.lexicon or editor.glossary; recommendation is split domain vs UI (domain in glyphs, UI in ui/editor).
2. Tooling support: if introducing ArchUnit is too heavy, start with grep-based forbidden import checks in CI, then upgrade later.
3. Experimental package policy: define explicit rule for jsesh.newmdc (active roadmap vs archived prototype) to prevent future drift.
---

## Status — 2026-07-18

`./gradlew clean build` is green across all modules: 159 tests, 0 failures.

### Done

- **Phase 1 roots established.** `jsesh.model` (+ `.constants` `.operations`
  `.transliteration` `.unicode` `.tools` `.api`), `jsesh.parser` (+ `.lex`),
  `jsesh.render`, `jsesh.glyphs`, `jsesh.io`, `jsesh.platform`. No package
  declaration for `jsesh.mdc`, `jsesh.mdcDisplayer`, `jsesh.hieroglyphs`,
  `jsesh.drawingspecifications` or `jsesh.preferences` remains anywhere.
- **Phase 2 leak: core → editor.** `MDCDocument` exposes document-level
  facade methods; `save()` throws `IOException` rather than the Swing
  `UserMessage`.
- **Phase 2 leak: render/glyphs → Swing.** `ShapeHelper`, `GraphicsUtils`
  and `GeometryHelper` were pure `java.awt.geom` math sitting in
  `jsesh.swing.utils`; moved to `jsesh.platform.graphics`.
- **Phase 2 leak: glyph repositories → sign-import UI.** The SVG/TTF/BZR/TML
  sign parsers in `jsesh.swing.signimportdialog.model` import nothing from
  Swing; moved to `jsesh.glyphs.signsource`. The dialog UI now depends on
  glyphs rather than the reverse. No interface indirection was needed —
  the types were simply in the wrong package.
- **`jsesh.render` and `jsesh.glyphs` now import no Swing package at all.**
- **Consumers caught up.** The earlier moves had only covered `jsesh`
  main sources; `jsesh/src/test`, `jseshTests`, `jseshSearch`, `jseshAppli`
  and `signInfoAppli` were left uncompilable and are now migrated.
- **Package-relative resources moved with their classes** (see below).

### Trap worth remembering

`EmbeddedGlyphsPathResources` and `MdcUnicodeTable` load data with
`getResourceAsStream("bare_name")`, which resolves against the *class's own
package*. Their classes had moved but `src/main/resources` had not, so sign
database loading failed at runtime while everything still compiled. Moved:

    jsesh/hieroglyphs/resources/{signs_description.xml,sign_description.dtd,
      basicGardinerCodes.txt}   ->  jsesh/glyphs/resources/
    jsesh/mdc/unicode/mdc2unicode.txt  ->  jsesh/model/unicode/

Any further move of a resource-loading class needs the same treatment.

### Pending

- **Phase 3 — `jsesh.model.tools` is still a landing bucket** (from the old
  `jsesh.mdc.utils`). Splitting it by responsibility is the natural next
  step, and needs a naming decision: the plan proposed
  `model.transform` / `model.query` / `model.normalize`.
- **Phase 3 — `jsesh.graphics.export`** still mixes encoders with Swing
  presenters and file dialogs. It is the last big mixed-responsibility
  bucket; the plan calls for `io.export` plus UI presenters under the UI
  layer.
- **Phase 3 — `jsesh.defaults` and `jsesh.glossary`** untouched.
- **Phase 1 — `jsesh.ui`.** The plan named this root, but the Swing widgets
  still live under `jsesh.swing`. Decide whether to rename or keep
  `jsesh.swing` as the UI layer name; `CLAUDE.md` currently documents it
  as the UI layer.
- **Phase 6 — no automated architecture check exists yet.** Everything above
  is enforced only by convention. A grep-based forbidden-import check is
  the cheap first version.
- **`jsesh.newmdc` is gone** from the tree, so the "experimental package
  policy" question is moot.

### Orphaned package.html — resolved

The old `package.html` javadoc files were **not** empty stubs; each held
authored prose. They were sorted into two groups:

- **Package renamed, docs still accurate** — converted to `package-info.java`
  at the new location, which is the mechanism the project already uses
  elsewhere and which keeps the text next to the code:
  `mdc/model`→`jsesh.model`, `mdc/interfaces`→`jsesh.model.api`,
  `mdc/utils`→`jsesh.model.tools`, `mdc`→`jsesh.parser`,
  `mdc/lex`→`jsesh.parser.lex`, `mdc/file`→`jsesh.io.document`,
  `mdc/output/dom`→`jsesh.io.mdc.dom`, `mdcDisplayer/draw`→`jsesh.render.draw`,
  `mdcDisplayer/clipboard`→`jsesh.clipboard`,
  `mdcDisplayer/swing/units`→`jsesh.swing.units`,
  `hieroglyphs`→`jsesh.glyphs`.
  `mdcDisplayer/drawingElements` was merged into the existing
  `jsesh.render.elements` package-info.
  A misfiled `org/qenherkhopeshef/swingUtils/portableFileDialog/package.html`
  living in the `jsesh` module moved to `qenherkhopeshefUtils` as a
  package-info.
- **Package genuinely gone** — deleted, with their now-empty directories:
  `editorSoftware`(+`actions`), `mdcDisplayer/viewToolkit`(+`elements/properties`,
  `temp`), `swing/preferencesEditor`, `xml`, `mdc/jseshInfo`,
  `org/qenherkhopeshef/{json,qpreference}`, plus the dead parent namespaces
  `mdc`, `mdcDisplayer`, `hieroglyphs`.

Not carried over: `mdcDisplayer/preferences/package.html` described
`DrawingPreferences`/`DrawingSpecifications`, neither of which still exists;
the current `jsesh.render.style` package-info supersedes it.

### Leftovers not cleaned up

- `jsesh/mdcDisplayer/preferences/defaultProperties.properties` is a single
  comment line and is read by no code.
- `jsesh/graphics/export/pdfExport/oldCode` and `jsesh/mdc/lex/README.txt`.

These three are the only reason the dead `jsesh/mdc/lex` and
`jsesh/mdcDisplayer/preferences` resource directories still exist. They are
inert; left in place rather than deleted on my own initiative.

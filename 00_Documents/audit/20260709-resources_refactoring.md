---
title: "Resource-access architecture — analysis & proposed refactoring"
date: 2026-07-09
supersedes: "20260709-resources_creation.md (open TODOs)"
status: proposal
---

# JSesh resource-access architecture — analysis & proposed refactoring

## Context

JSesh is both a hieroglyphic editor **and** an embeddable library. To render or edit
Manuel-de-Codage text, callers need three kinds of resource, along two source axes:

| Resource | What it is | Comes from |
|---|---|---|
| **Shapes** (`HieroglyphShapeRepository`) | drawable glyphs (`ShapeChar`) keyed by `CanonicalCode` | embedded fonts + user SVG folder |
| **Database** (`HieroglyphDatabase`) | sign codes, values, families, tags, variants, descriptions | embedded XML + user `signs_definition.xml` |
| **Glossary / possibilities** (`Glossary`, `PossibilityRepository`) | transliteration → sign-sequence, for autocompletion | user `glossary.txt` |

These three are bundled into an aggregate (`HieroglyphToolkit`) so a `JMDCEditor` / `JMDCField`
can be handed "everything it needs" at once. The previous refactoring successfully removed most
of the JSesh v7 singletons, but — as the audits in `20260709-resources.md` and
`20260709-resources_creation.md` note — the **creation** side was left ad-hoc: too many factories,
inconsistent names, and unclear singleton semantics, confusing even to the authors.

**Scope of this document:** written analysis + a target design. No code changes are made here.
The decisions already taken for the target design are:

- the unified aggregate is named **`HieroglyphResources`**;
- `JSeshRenderContext` stays a **separate** drawing concern, joined to the resources by a bridge
  method (not fused);
- flexibility — pluggable shape / database sources, and future "sign libraries" (Old Kingdom vs
  Ptolemaic, …) — must be preserved.

The findings below were checked against the current source tree (all types live in the `jsesh`
module unless noted; `jseshGlyphs` is a resource-only module).

---

## Problems in the current architecture

### P1 — Six overlapping ways to build the aggregate

To obtain a `HieroglyphToolkit`, a caller can use any of:

1. `HieroglyphToolkit.standardHieroglyphToolKit()` — static default, delegates to (2)
2. `SimpleHieroglyphToolkit.embeddedOnlyInstance()` — lazy-holder **singleton**
3. `SimpleHieroglyphToolkit.buildFontWithoutUserDefinitions(font, glossary)`
4. `SimpleHieroglyphToolkit.buildFontWithUserDescriptions(fonts, glossary)`
5. `new SimpleHieroglyphToolkit(shapes, possibilities, database)` — public 3-arg constructor
6. `new JSeshUserSignLibraryConfiguration()` — the user-preferences path

There is no single obvious entry point, and nothing in the names signals which calls return a
**shared** object versus a **fresh** one. The user-preferences path (6) has no counterpart on
`SimpleHieroglyphToolkit`, so the two implementations are reached through unrelated APIs.

### P2 — One interface, two implementations that add no polymorphism

`HieroglyphToolkit` has exactly two implementors. Both merely *hold and return* the three
resources; they differ only in **how they are assembled** (embedded vs. user preferences), not in
runtime behaviour. The interface is not earning its keep — the variability that matters lives one
level down, in `HieroglyphShapeRepository` / `HieroglyphDatabase`, which genuinely have many
implementations.

`JSeshUserSignLibraryConfiguration` also carries a member the interface cannot express —
`glossaryManager()`, needed to *save* the user glossary — so callers downcast to the concrete type
anyway (`JSeshApplicationCore`, `signInfoAppli/Main`). The interface abstraction leaks.

### P3 — Inconsistent, sometimes misleading names

- `standardHieroglyphToolKit()` — stray capital K (`ToolKit` vs. the type `Toolkit`).
- `buildFontWithoutUserDefinitions` / `buildFontWithUserDescriptions` — they build a **whole
  toolkit**, not a font; and "Definitions" vs. "Descriptions" is used for the same concept. The
  parameter is named `font` (singular) while its Javadoc says `@param fonts … a list of fonts`.
- `JSeshUserSignLibraryConfiguration` — long, used at few sites; it *is* "the resources built from
  user preferences," not a generic "configuration."
- `PredefinedFonts.compositeFont()` — "not a real predefined font"; returns a stripped composite
  that silently omits the user font. It duplicates `StandardFontShapeRepository`'s wiring, and
  `PredefinedFonts.standardJSeshFont()` re-reads `list.txt` on every call, while the singleton path
  caches — two loading paths for the same font.
- `GlossaryManager` — the name hides that it is three things at once (see P5).
- Accessor style is mixed: record-style `hieroglyphShapeRepository()` next to `getGlossary()` /
  `getValuesFor(…)`.
- `HieroglyphCodesSource.getCodes()` returns `Set<String>` even though the rest of the shape API
  migrated to the `CanonicalCode` value type — a leftover `String` / `CanonicalCode` seam.

### P4 — I/O and hidden singletons in constructors → poor testability

Constructors do real disk / `Preferences` work, so the objects cannot be built in a unit test
without a populated user environment:

- `JSeshUserSignLibraryConfiguration()` → glossary file + user sign XML + full shape repo.
- `GlossaryManager()` → reads `glossary.txt`.
- `JSeshFullHieroglyphShapeRepository()` → scans a folder **and** reads `java.util.prefs.Preferences`.
- `DirectoryHieroglyphShapeRepository(File)` → `refresh()` scans the directory at construction.

Additionally, `StandardFontShapeRepository.INSTANCE` and `GnutraceHieroglyphShapeRepository.INSTANCE`
are **eager** static singletons that read resources at class-load — hidden global state with
class-init ordering hazards. The no-arg `JMDCEditor()` and `JMDCField(w, h)` constructors bind
callers to these singletons implicitly.

### P5 — Muddled responsibilities & unclear singleton semantics

- `GlossaryManager` = in-memory holder + file reader + file writer + path resolver, all in one
  class.
- The audits themselves admit it is unclear "what should *normally* be a singleton vs. what is
  *structurally* one." `embeddedOnlyInstance()` reads like a factory but is a singleton; the named
  constructors read like singleton accessors. No naming convention separates the two cases.

### P6 — Scattered package layout

The subsystem has no home. Its pieces are spread across five packages:
`jsesh` (`JSeshUserSignLibraryConfiguration`), `jsesh.defaults` (`HieroglyphToolkit`,
`SimpleHieroglyphToolkit`, `PredefinedFonts`), `jsesh.hieroglyphs.fonts` (shape repositories),
`jsesh.hieroglyphs.data` (database + factory), `jsesh.glossary`, and `jsesh.editor`
(`PossibilityRepository`, which depends only on database + glossary and does not belong with the
editor).

### P7 — Two overlapping aggregates

`JSeshRenderContext` = (style, shapes) and `HieroglyphToolkit` = (shapes, database, possibilities)
both carry the shape repository. `JMDCEditor` holds *both* and stitches them by hand
(`new JSeshRenderContext(getJSeshStyle(), hieroglyphShapeRepository())`). It is unclear which
aggregate to pass where.

### Minor bugs surfaced while mapping the code

(Not the main point, but worth recording.)

- `GlossaryManager`: `read()` / `save()` use `glossary.txt`, but the static
  `getUserSignDefinitionFile()` returns `jsesh_glossary.txt` — two different filenames, and the
  method is misnamed (it is a glossary path, not a sign-definition path).
- `DefaultHieroglyphDatabase` constructor calls `fillFamilyList()` twice (second result discarded).
- The earlier note that `buildPlainDefault` "loads user signs" appears stale: on that path the
  factory adds only the embedded descriptions.

---

## Target design

The shape is: **one immutable aggregate + one loader**, a clear naming convention for shared vs.
fresh, and a thin bridge to the drawing concern. The pluggable interfaces underneath are left
untouched, so all current flexibility (custom fonts, custom databases, future "sign libraries") is
preserved.

### 1. `HieroglyphResources` — the aggregate, as a record

Replaces the `HieroglyphToolkit` interface **and** both implementations.

```java
public record HieroglyphResources(
        HieroglyphShapeRepository shapes,
        HieroglyphDatabase        database,
        PossibilityRepository     possibilities,
        GlossaryManager           glossary) {   // carries the glossary gateway the old interface couldn't

    /** Bridge to the drawing concern — keeps JSeshRenderContext separate (P7). */
    public JSeshRenderContext renderContext(JSeshStyle style) {
        return new JSeshRenderContext(style, shapes);
    }
}
```

- A **record**, because the two old implementations added no behaviour (P2). Variability stays in
  `shapes` / `database`, which remain interfaces.
- `renderContext(style)` is the single place that joins resources to a style, so no consumer
  hand-stitches the pair again (P7). Drawing / export code keeps taking `JSeshRenderContext` and
  stays free of database / glossary dependencies.
- Consistent record-style accessors (P3).
- `glossary` could instead be `Optional<GlossaryManager>` if an embedded-only kit should
  legitimately have none — a small open choice.

### 2. `HieroglyphResourcesLoader` — the single construction entry point

Collapses the six creation paths (P1) into one fluent builder plus a couple of clearly-named
presets:

```java
public final class HieroglyphResourcesLoader {
    // fluent, orthogonal configuration along the two axes
    public HieroglyphResourcesLoader addFont(HieroglyphShapeRepository font);
    public HieroglyphResourcesLoader userFontDirectory(File dir);
    public HieroglyphResourcesLoader userSignDefinitions(File xml);
    public HieroglyphResourcesLoader userGlossary(File txt);
    public HieroglyphResources build();               // <-- all disk I/O happens HERE, explicitly

    // named presets — the only two most callers need
    public static HieroglyphResources embedded();            // was standardHieroglyphToolKit() / embeddedOnlyInstance()
    public static HieroglyphResources fromUserPreferences(); // was new JSeshUserSignLibraryConfiguration()
}
```

- All I/O is centralised in the loader / `build()`; the resource objects themselves get plain
  data-in constructors and become unit-testable (P4). (Physically moving I/O out of the existing
  constructors is a later implementation step; the design target is what is fixed here.)
- The builder keeps the **shape axis** and the **description axis** independent — exactly what
  "sign libraries" (Old Kingdom vs. Ptolemaic) will need.

Resulting call sites:

```java
// embedded (tests, servers, demos)
HieroglyphResources res = HieroglyphResourcesLoader.embedded();

// standard JSesh application
HieroglyphResources res = HieroglyphResourcesLoader.fromUserPreferences();
JMDCEditor editor = new JMDCEditor(model, JSeshStyle.DEFAULT, res);

// fine-grained control (tests, specific deployments)
HieroglyphResources res = new HieroglyphResourcesLoader()
        .userFontDirectory(tempDir)
        .userSignDefinitions(customXml)
        .build();
```

### 3. Naming convention for shared vs. fresh (P5)

Adopt and document one rule across the subsystem:

- `getXxx()` / `xxxInstance()` → returns a **process-wide shared singleton** (reserved for the
  immutable, expensive embedded font / gnutrace resources).
- `build()` / `embedded()` / `fromUserPreferences()` / `new` → returns a **fresh** object.

Under this rule `embedded()` returns a fresh (or explicitly-cached-and-documented) kit rather than
the current surprising singleton, and the genuine singletons (`StandardFontShapeRepository`,
`GnutraceHieroglyphShapeRepository`) keep `getInstance()` and are documented as intentional.

### 4. Renames (P3)

| Now | Target |
|---|---|
| `HieroglyphToolkit` (interface) + `SimpleHieroglyphToolkit` + `JSeshUserSignLibraryConfiguration` | one `HieroglyphResources` record |
| `standardHieroglyphToolKit()` | `HieroglyphResources.embedded()` |
| `embeddedOnlyInstance()` | `HieroglyphResources.embedded()` |
| `buildFontWithoutUserDefinitions(…)` / `buildFontWithUserDescriptions(…)` | loader methods; drop "Font", unify "Descriptions" |
| `new JSeshUserSignLibraryConfiguration()` | `HieroglyphResources.fromUserPreferences()` |
| `PredefinedFonts.compositeFont()` | remove (fold into loader / the standard singleton) |

### 5. GlossaryManager (P5) — split responsibilities

Separate the in-memory `Glossary` (already clean) from persistence. Rename `GlossaryManager` to a
storage-gateway role (e.g. `GlossaryStore`) exposing explicit `load()` / `save()` rather than
reading in its constructor, and fix the `glossary.txt` vs. `jsesh_glossary.txt` filename bug and the
mis-named `getUserSignDefinitionFile()`.

### 6. A home package (P6)

Give the aggregate + loader a single coherent package (e.g. `jsesh.resources`) rather than
`jsesh` + `jsesh.defaults`. `PossibilityRepository` belongs with the resources it depends on, not in
`jsesh.editor`.

### What stays flexible (explicitly preserved)

- `HieroglyphShapeRepository`, `HieroglyphCodesSource`, `HieroglyphDatabase` remain interfaces with
  multiple implementations. Custom fonts (`addFont`, `CompositeHieroglyphShapeRepository`,
  `DirectoryHieroglyphShapeRepository`) and custom databases are unaffected.
- The observer mechanism on shape repositories
  (`addListener` / `HieroglyphShapeRepositoryChangedEvent`) is untouched.
- "Sign libraries" become natural: a preset on the loader = a named recipe of shape + description
  sources.

---

## Impact sketch (for when/if this is implemented)

The concrete-type footprint is small and the churn is mechanical:

- `HieroglyphToolkit` — ~22 files, mostly `jseshSearch`; these take the aggregate as a parameter and
  only change type name.
- `JSeshUserSignLibraryConfiguration` — only the two application bootstraps plus their immediate
  wiring (`JSeshStartup`, `JSeshApplicationCore`, `JSeshApplicationModel`, `signInfoAppli/Main`).
- The two editor/field constructor families (`JMDCEditor`, `JMDCField`).
- `JSeshRenderContext` / `JSeshStyle` are pervasive (~55 files) but **untouched** by this design —
  the bridge method is purely additive.

## Verification (for a future implementation phase)

- `./gradlew clean build` from the root (regenerates the CUP / JFlex sources) must stay green across
  all 13 modules.
- Launch `jsesh.jhotdraw.Main` and confirm the user font directory, user sign definitions, and
  glossary still load (they flow through `fromUserPreferences()`); autocompletion still fires from
  the glossary.
- Launch `signInfoAppli` (`jsesh.utilitysoftwares.signinfoeditor.Main`) — its second
  `fromUserPreferences()` bootstrap.
- New unit tests build a `HieroglyphResources` via the loader with a temp font dir + temp XML,
  proving the objects construct without touching the real user preferences (the P4 payoff).

---

## Suggested next step

Turn the target design into a short ADR (e.g. `00_Documents/ADR/0004.md`, "Unify resource access
into `HieroglyphResources`") once the direction is agreed, and treat this file as the analysis it
records. This proposal supersedes the open TODOs in `20260709-resources_creation.md`.

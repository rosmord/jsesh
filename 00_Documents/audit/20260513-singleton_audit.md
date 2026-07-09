# JSesh Singleton Audit
**Date:** 2026-05-13  
**Context:** Refines the earlier tech-debt audit with input from `DeveloperJournal.md` and direct inspection of every `getInstance()` call site.

---

## Summary

The raw audit flagged 116 files calling `getInstance()`. After applying the two filters you raised — *jhotdrawfw is frozen* and *truly static resources are fine* — the number of singletons that actually need attention shrinks to **four**, one of which (`MDCEditorKit`) can be deleted right now.

---

## Axis 1 — jhotdrawfw singletons: ignore

Roughly two-thirds of the `getInstance()` call count lives inside `jhotdrawfw` (color spaces, palette UI classes, font chooser nodes). These are all internal to the framework and not touched by JSesh code proper. They are not actionable and should be excluded from any refactoring count.

**Action required: none.**

---

## Axis 2 — Truly static / immutable resources: acceptable by design

The developer journal's stated philosophy is clear: *"immutable values can be accessed through a singleton."* The following singletons hold data that is genuinely read-only and loaded once from classpath or a fixed file — they are correctly implemented as singletons.

| Singleton                 | Why it is acceptable                                                                                                                                                                                                                                                               |
| ------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `ManuelDeCodage`          | Explicitly approved in the journal: *"it does only deal with the basic Gardiner List. We can continue to use a singleton here."* The data is the Gardiner code table — it never changes at runtime.                                                                                |
| `ResourcesManager`        | Provides access to embedded classpath resources: transliteration fonts, icon definitions, ligature data, the user prefs directory. All read-only after construction.                                                                                                               |
| `SpecialSymbolDrawer`     | Self-documented: *"stateless, and costly to create, so we use a singleton."* Correctly designed.                                                                                                                                                                                   |
| `TkseshLigatureCatalogue` | Reads a fixed ligature file once; used as a lookup table. No mutable state.                                                                                                                                                                                                        |
| `WildcardFont`            | Loads a classpath font resource for the search field; purely read-only.                                                                                                                                                                                                            |
| `BundleHelper`            | The i18n singleton for JHotDraw action labels, called in every Action constructor. Even ignoring the JHotDraw coupling, this is the standard pattern for a resource bundle — passing it as a constructor parameter to ~30 action classes would add noise with no gain. Acceptable. |

**Action required: none.** Document this explicitly so future contributors don't mistake these for legacy accidents.

---

## Axis 3 — Font singletons: migration already underway

The following singletons are in the process of being replaced by the `JSeshFontKit` / `HieroglyphShapeRepository.getStandardShapeRepository()` pattern that the developer journal documents as the new canonical approach.

| Singleton                                         | Status                                                                                                                                               |
| ------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------- |
| `GnutraceHieroglyphShapeRepository.getInstance()` | Used as a building block inside `StandardFontShapeRepository`. The public API is moving to `HieroglyphShapeRepository.getStandardShapeRepository()`. |
| `StandardFontShapeRepository.getInstance()`       | The backing implementation of `getStandardShapeRepository()`. Will eventually be hidden behind the interface.                                        |

The journal's `HieroglyphShapeRepository` / `SimpleFontKit` / `JSeshUserSignLibraryConfiguration` documentation describes the intended end state. Both of these will become implementation details once the refactoring is complete.

**Action required: continue the existing plan.** No new work needed here; just finish what has been started.

---

## Axis 4 — Problematic singletons: two cases worth addressing

### `ImageIconFactory` — mutable singleton

This is the most technically awkward singleton in the codebase. It is accessed as a singleton but has a setter that modifies its state: `ImageIconFactory.getInstance().setCadratHeight(data.iconHeight())` is called in `Main.java` at startup. It is also called from deep inside Swing widget code (`MdCTableCellRenderer`, `HieroglyphicMenu`, and multiple action classes in both `jsesh` and `jseshAppli`).

The developer journal notes: *"The `ImageIconFactory` is not a factory, it's a repository/cache. It should probably use `HieroglyphPictureBuilder` and not contain its own icon building code."*

The practical problem with its current singleton form is that the cadrat height is set once globally at startup and then cannot change per-editor or per-context. If a user ever wanted different zoom levels in different windows, this would silently use the wrong icon size.

**Suggested fix:** Once the `HieroglyphPictureBuilder` refactoring is done (a TODO you already have), construct `ImageIconFactory` with a `cadratHeight` argument and pass it through the `JSeshApplicationCore` or `JSeshFontKit` context, which already holds display preferences. This is medium effort but low urgency — until multi-window zoom is a real requirement, the current behaviour is not wrong, just fragile.

### `GlossaryManager` — not a GoF singleton, but uses `ResourcesManager` as a global

`GlossaryManager` is not technically a singleton — it has no `getInstance()` method. But it calls `ResourcesManager.getInstance()` to locate the user glossary file, creating an implicit dependency on the global prefs directory path. The journal specifically flags this: *"separate constructor call for the glossary manager and reading user glossary from file, mainly to simplify testing and debugging."*

**Suggested fix:** Add a static factory method or named constructor that takes a `File` as the glossary path, keeping the zero-arg constructor as a convenience shim. This makes it testable without touching `ResourcesManager`.

---

## Axis 5 — Dead code: delete immediately

### `MDCEditorKit` — confirmed unused

A full-text search of the entire codebase finds exactly **one** reference to `MDCEditorKit` outside its own file:

```java
// jseshTests/src/main/java/jsesh/demo/GroupEditorDemo.java:71
//topItems.asList(),MDCEditorKit.getBasicMDCEditorKit()
```

It is **commented out**. The journal's 2026/04/01 entry also raises the question *"Use the MDCEditorKit?"* as a hypothetical, which confirms it is not actively used.

The class itself is a singleton wrapper around a `Layout` factory with a `JSeshStyle`. Its purpose has been superseded by the new `JSeshRenderContext` / `JSeshFontKit` / `JSeshStyle` approach documented in the journal.

**Action required: delete `MDCEditorKit.java`.** Check that the build still passes — it should. This is a one-minute task.

---

## Revised Singleton Priority Table

| Singleton                           | Category                              | Action                                            |
| ----------------------------------- | ------------------------------------- | ------------------------------------------------- |
| All `jhotdrawfw` singletons         | Frozen framework                      | Ignore                                            |
| `ManuelDeCodage`                    | Static data, explicitly approved      | Keep as-is, document                              |
| `ResourcesManager`                  | Static classpath resources            | Keep as-is                                        |
| `SpecialSymbolDrawer`               | Stateless, self-documented            | Keep as-is                                        |
| `TkseshLigatureCatalogue`           | Static file, read-only                | Keep as-is                                        |
| `WildcardFont`                      | Classpath font, read-only             | Keep as-is                                        |
| `BundleHelper`                      | i18n bundle, JHotDraw pattern         | Keep as-is                                        |
| `GnutraceHieroglyphShapeRepository` | Font singleton, migration in progress | Finish existing refactoring                       |
| `StandardFontShapeRepository`       | Font singleton, migration in progress | Finish existing refactoring                       |
| `GlossaryManager` (implicit)        | Needs testable constructor            | Add named constructor with path arg               |
| `ImageIconFactory`                  | Mutable singleton — medium risk       | Migrate after `HieroglyphPictureBuilder` refactor |
| **`MDCEditorKit`**                  | **Dead code**                         | **Delete now**                                    |

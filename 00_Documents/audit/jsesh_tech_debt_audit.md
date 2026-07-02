# JSesh — Technical Debt Audit
**Date:** 2026-05-13  
**Codebase:** `jsesh-development` (multi-module Maven, Java 21, version 8.0-SNAPSHOT)  
**Scope:** 13 modules, ~167k total LOC (excluding generated sources)

---

## Executive Summary

JSesh is a mature, specialised desktop application with a well-structured module layout and a clear domain model. The core debt is concentrated in three areas: **near-zero automated test coverage** (the most dangerous item given planned refactoring), **pervasive singleton coupling** (already acknowledged in the TODO file), and **several large vendored codebases** that cannot be independently maintained. The good news is that the codebase's own authors have correctly identified the most important structural issues — the work here is to quantify, prioritise, and plan them.

---

## Module Overview

| Module                 | Files | ~LOC   | Notes                               |
| ---------------------- | ----- | ------ | ----------------------------------- |
| `jsesh`                | 458   | 57,387 | Core library                        |
| `jhotdrawfw`           | 300   | 64,053 | Forked/vendored GUI framework       |
| `cupAndlex`            | 42    | 19,234 | Vendored parser-generator tools     |
| `jseshAppli`           | 76    | 8,627  | Application entry point             |
| `qenherkhopeshefUtils` | 114   | 8,609  | Custom Swing/utility framework      |
| `signInfoAppli`        | 27    | 4,227  | Sign information editor             |
| `jseshSearch`          | 39    | 3,012  | Search subsystem                    |
| `cupruntime`           | 4     | 1,513  | **Duplicate** of cupAndlex runtime  |
| `jseshTests`           | 10    | 669    | Demo programs (not automated tests) |

---

## Debt Inventory

### Scoring formula
`Priority = (Impact + Risk) × (6 − Effort)` — higher is more urgent.

---

### 1. Test Debt — CRITICAL

**Finding:** The entire project has 19 test files. Of these, approximately half are demo programs with a `main()` method rather than automated `@Test` methods (e.g., `CartoucheDemo`, `ForDebuggingPlacementA/B`, `ScaledAbsolutePlacementTest`). The `jseshTests` module — whose name implies a test suite — contains zero `@Test` classes; all its files live under `src/main/java` and are manual UI exercises. Modules with zero automated tests: `jseshAppli`, `jseshLabels`, `jhotdrawfw`, `qenherkhopeshefUtils`, `signInfoAppli`, `jseshTests`.

JUnit 4 (4.13.1) is declared in the root POM. JUnit 5 has been the community standard since 2017 and adds parameterised tests, extension points, and better IDE integration.

**Why it matters:** The TODO file documents at least a dozen planned refactorings (singleton removal, Layout flattening, LigatureComparator clean-up). Executing any of them without a safety net means regressions ship silently.

| Metric                                             | Score                |
| -------------------------------------------------- | -------------------- |
| Impact (slow-down / risk of refactoring)           | 5                    |
| Risk (regressions ship undetected)                 | 5                    |
| Effort to fix (gradual; start with critical paths) | 3                    |
| **Priority score**                                 | **(5+5)×(6−3) = 30** |

**Recommended fix:**
1. Upgrade to JUnit 5 (`junit-jupiter`) in the root POM and remove JUnit 4.
2. Add Surefire 3.x configuration explicitly so tests are discovered reliably.
3. Write characterisation tests (golden-output tests) for the MdC parser and the drawing pipeline — these can be added without changing production code and immediately prevent regressions.
4. Rename `jseshTests` to `jseshDemos` or move its content to `src/test/java` with proper annotations.

---

### 2. Architecture Debt — Singleton Proliferation — HIGH

**Finding:** 116 source files call `getInstance()`. The main singletons are `ManuelDeCodage`, `ResourcesManager`, `GnutraceHieroglyphShapeRepository`, `StandardFontShapeRepository`, `MDCEditorKit`, and `ImageIconFactory`. The TODO file explicitly flags this: *"Removing Singleton for preferences"*, *"Removing most singletons for MDCEditorKit"*.

Singletons make unit testing impossible (you cannot inject a test double), create hidden coupling between otherwise unrelated modules, and prevent running multiple editor instances in the same JVM.

| Metric                                                 | Score                |
| ------------------------------------------------------ | -------------------- |
| Impact                                                 | 4                    |
| Risk (prevents testability, blocks multi-instance use) | 4                    |
| Effort (must be done incrementally)                    | 4                    |
| **Priority score**                                     | **(4+4)×(6−4) = 16** |

**Recommended fix:**
1. Introduce a simple `JSeshContext` or `ApplicationServices` record that holds the collaborators currently accessed as singletons.
2. Migrate one singleton at a time, starting with `ManuelDeCodage` (most widely used — 6+ direct callers in the core library).
3. Pass the context through constructors in new code; use the singleton only as a transitional shim.
4. The existing `MDCEditorKit.getBasicMDCEditorKit()` static factory is a good candidate for the first migration since the TODO file specifically calls it out.

---

### 3. Architecture Debt — Incomplete Rewrite in Production Code — HIGH

**Finding:** The package `jsesh.newEdit` (with a `temp` sub-package) is an in-progress architectural experiment that has been left in the production source tree. Its own `package-info.java` describes it as *"Temporary package to test the use of the 'new' view toolkit library."* It compiles, ships in the JAR, and is invisible to users — but it adds confusion for anyone reading the code and creates a false impression that two editor architectures coexist.

| Metric                                          | Score                |
| ----------------------------------------------- | -------------------- |
| Impact (confusion, dead weight)                 | 3                    |
| Risk (accidental dependency on unfinished code) | 3                    |
| Effort (delete or promote to a separate branch) | 1                    |
| **Priority score**                              | **(3+3)×(6−1) = 30** |

**Recommended fix:** Either delete `jsesh.newEdit` and track the planned rewrite in a dedicated feature branch, or promote it to a documented experiment module (`jsesh-newEdit-experiment`) with a clear README explaining its status. Do not leave experimental packages in the production source tree.

---

### 4. Code Quality Debt — Raw Types — HIGH

**Finding:** 256+ instances of raw collection usage in the `jsesh` module alone: `new ArrayList()`, `new HashMap()`, `new HashSet()` without generic type parameters. Examples include `InnerGroupLister`, `ResourcesHieroglyphicShapeRepository`, `ResourcesManager`, and `RTFReader`. Raw types bypass the compiler's type-checking and produce unchecked cast warnings (52 `@SuppressWarnings` annotations suggest these are being silenced rather than fixed).

A specific high-risk instance flagged in the TODO is the ligature map using `String[]` as a key (`Map<String[], ExplicitPosition[]> ligaturesMap`). Arrays do not implement `equals()`/`hashCode()` based on content, so map lookups silently return `null` instead of the expected ligature data — a latent correctness bug.

| Metric                                                     | Score                |
| ---------------------------------------------------------- | -------------------- |
| Impact (potential correctness bugs, hard to refactor)      | 4                    |
| Risk (silent lookup failures, type cast errors at runtime) | 4                    |
| Effort (mostly mechanical but many files)                  | 3                    |
| **Priority score**                                         | **(4+4)×(6−3) = 24** |

**Recommended fix:**
1. Fix the `String[]`-keyed map immediately — replace the key type with `List<String>` or a dedicated `LigatureKey` value class. This is a bug, not just a cleanliness issue.
2. Enable `-Xlint:unchecked` in the compiler plugin and work through the warnings module by module.
3. Most raw-type fixes are a one-line change and can be done opportunistically whenever a file is touched.

---

### 5. Architecture Debt — God Classes — MEDIUM-HIGH

**Finding:** Several classes have grown far beyond a single responsibility:

- `JMDCEditorWorkflow` — 1,986 lines, 131 method signatures. The central command handler for the editor.
- `Layout` — 924 lines. Explicitly flagged in TODO: *"flatten the current Layout class/hierarchy. It has only one implementation."*
- `JMDCEditor` — 973 lines. Swing component combining display, input handling, and state.
- `PalettePresenter` — 952 lines.
- `SignInfoPresenter` — 929 lines.

These classes are difficult to test, hard to reason about, and frequently the site of merge conflicts in collaborative projects.

| Metric                                                            | Score                |
| ----------------------------------------------------------------- | -------------------- |
| Impact (slow to understand, change, and test)                     | 4                    |
| Risk (bugs hide in large methods; changes have wide blast radius) | 3                    |
| Effort (must be done in careful steps)                            | 4                    |
| **Priority score**                                                | **(4+3)×(6−4) = 14** |

**Recommended fix:**
1. Start with `Layout` — the TODO already calls for flattening it. Since there is only one implementation, the interface and implementation can be merged and the class split by drawing concern (text layout vs. sign layout vs. cartouche layout).
2. Extract command handlers from `JMDCEditorWorkflow` into dedicated `Action` or `Command` classes. The `jsesh/editor/actions/` package structure already exists — use it fully.
3. Address `JMDCEditor` last, as it is the most entangled.

---

### 6. Dependency Debt — Vendored / Forked Libraries — MEDIUM

**Finding:** Three substantial external codebases are vendored directly into the project:

- **`jhotdrawfw`** (300 files, 64k LOC): A heavily modified fork of JHotDraw 7.6, an old version of a Swing drawing framework. The original JHotDraw project has evolved significantly; this fork diverges with custom changes (package `org.qenherkhopeshef.jhotdrawChanges`) and cannot receive upstream security patches or bug fixes.
- **`cupAndlex`** (42 files, 19k LOC) and **`cupruntime`** (4 files, 1.5k LOC): Vendored copies of the CUP parser generator and JLex lexer. The `cupruntime` module is an **exact duplicate** of the runtime classes already present in `cupAndlex` — confirmed by identical directory listing. This is dead weight.
- **`nanoxml`** (24 files, embedded inside `jhotdrawfw`): An unmaintained XML parser from the early 2000s. Java's built-in XML APIs (`javax.xml`) are a straightforward replacement.

| Metric                                                                   | Score                |
| ------------------------------------------------------------------------ | -------------------- |
| Impact (can't patch vulnerabilities; dead duplication wastes build time) | 3                    |
| Risk (security patches cannot reach vendored code)                       | 4                    |
| Effort (jhotdrawfw is large; cupruntime deletion is trivial)             | 4                    |
| **Priority score**                                                       | **(3+4)×(6−4) = 14** |

**Recommended fix:**
1. Delete `cupruntime` immediately — it is fully covered by `cupAndlex`. Update any module that depends on it to depend on `cupAndlex` instead.
2. Replace nanoxml usage in `jhotdrawfw` with `javax.xml` or a current library (Jackson, JAXB). The 24 nanoxml files can be removed once callers are migrated.
3. Track `jhotdrawfw` as a long-term extraction: the custom changes should be isolated in a clearly-named package, making it easier to eventually publish the fork as a standalone artifact or replace it with a modern framework.

---

### 7. Infrastructure / Build Debt — MEDIUM

**Finding:**
- The root POM declares the Maven repository at `http://www.qenherkhopeshef.org/maven` (plain HTTP). Since Maven 3.8.1 this blocks by default and is flagged as a security risk. Dependency resolution over HTTP is vulnerable to MITM attacks.
- Maven deployment uses `wagon-ssh 1.0-beta-7`, a version from approximately 2009. The current stable release is 3.x.
- No CI/CD pipeline configuration file (GitHub Actions, GitLab CI, Jenkins, etc.) is present in the repository.

| Metric                                                           | Score                |
| ---------------------------------------------------------------- | -------------------- |
| Impact (builds break on modern Maven; no automated quality gate) | 3                    |
| Risk (supply-chain attack surface via HTTP repository)           | 4                    |
| Effort (configuration changes, low code impact)                  | 2                    |
| **Priority score**                                               | **(3+4)×(6−2) = 28** |

**Recommended fix:**
1. Switch the Maven repository URL to HTTPS immediately.
2. Upgrade `wagon-ssh` to `3.5.3` (current stable).
3. Add a minimal CI pipeline (GitHub Actions is zero-infrastructure): run `mvn verify` on push. This alone will prevent broken commits from going undetected.

---

### 8. Code Quality Debt — Debug Logging in Production — MEDIUM

**Finding:** 80 instances of `System.out.println` and `e.printStackTrace()` in production code within the `jsesh` module. Notable examples: `MDCLex.java` prints every token during lexing; `DocumentPreferences.java` has a test `main()` method and two bare `System.out.println` calls in non-test code; multiple catch blocks call `e.printStackTrace()` and silently continue.

Bare `e.printStackTrace()` without a logging framework means exceptions are invisible in deployed applications and impossible to suppress in tests.

| Metric                                                       | Score                |
| ------------------------------------------------------------ | -------------------- |
| Impact (noise in output; exceptions swallowed silently)      | 3                    |
| Risk (silent failures in production; untestable error paths) | 3                    |
| Effort (mostly mechanical find-and-replace)                  | 2                    |
| **Priority score**                                           | **(3+3)×(6−2) = 24** |

**Recommended fix:** Introduce SLF4J with a Logback backend. Replace all `System.out.println` with appropriate log levels (`log.debug`, `log.warn`, `log.error`). Replace `e.printStackTrace()` with `log.error("message", e)`. This is a safe, mechanical change.

---

### 9. Documentation Debt — LOW

**Finding:** 227 TODO/FIXME comments in source code. The `TODO` file at the project root is self-described as *"Franglais file, mainly for personal use"* — it is not suitable as team-facing documentation. `NEW.md` contains the placeholder `VERSION NUMBER HERE`. Many auto-generated Javadoc stubs contain empty `// TODO Auto-generated method stub` bodies in demo code.

| Metric                                             | Score                |
| -------------------------------------------------- | -------------------- |
| Impact (onboarding friction; version notes lost)   | 2                    |
| Risk (low — documentation debt rarely causes bugs) | 1                    |
| Effort (low)                                       | 2                    |
| **Priority score**                                 | **(2+1)×(6−2) = 12** |

**Recommended fix:** Replace the personal TODO file with a CONTRIBUTING.md and a structured issue tracker. In-code TODOs that reference specific refactorings should become tracked issues so they don't accumulate indefinitely.

---

## Priority Summary

| #   | Debt Item                                          | Category       | Priority Score | Effort   |
| --- | -------------------------------------------------- | -------------- | -------------- | -------- |
| 1   | Near-zero automated test coverage                  | Test           | 30             | Medium   |
| 2   | Incomplete rewrite in production (`jsesh.newEdit`) | Architecture   | 30             | Low      |
| 3   | HTTP Maven repository (security)                   | Infrastructure | 28             | Low      |
| 4   | Raw types (incl. String[] map key bug)             | Code quality   | 24             | Medium   |
| 5   | Debug logging / printStackTrace in production      | Code quality   | 24             | Low      |
| 6   | Singleton proliferation                            | Architecture   | 16             | High     |
| 7   | God classes (Layout, JMDCEditorWorkflow…)          | Code quality   | 14             | High     |
| 8   | Vendored libraries / duplicate cupruntime          | Dependencies   | 14             | Low→High |
| 9   | Documentation debt                                 | Documentation  | 12             | Low      |

---

## Phased Remediation Plan

All three phases are designed to run **alongside feature work**, not as dedicated debt sprints.

### Phase 1 — Foundation (1–3 months)
These are low-effort, high-impact changes that unblock everything else.

- Fix HTTP → HTTPS Maven repository URL.
- Upgrade `wagon-ssh` to 3.5.3.
- Delete `cupruntime` module (duplicate of cupAndlex runtime).
- Delete or branch-isolate `jsesh.newEdit`.
- Fix the `String[]`-keyed ligature map (correctness bug).
- Upgrade to JUnit 5 in the root POM; add explicit Surefire configuration.
- Add a minimal CI pipeline that runs `mvn verify` on push.

### Phase 2 — Safety Net (3–6 months)
Build the test coverage needed to refactor safely.

- Write characterisation tests for the MdC parser (input MdC string → expected model). This covers the most critical correctness path.
- Write golden-output tests for the drawing pipeline (input model → rendered bitmap checksum). Catches visual regressions.
- Replace all `e.printStackTrace()` with SLF4J logging.
- Introduce SLF4J + Logback; remove all bare `System.out.println` from production code.
- Work through raw-type warnings module by module using `-Xlint:unchecked`.

### Phase 3 — Structural Improvements (6–18 months)
Proceed only after Phase 2 test coverage is in place.

- Flatten `Layout` as described in the TODO — split by drawing concern, remove the single-implementation interface.
- Begin singleton extraction, one at a time, starting with `ManuelDeCodage`. Introduce a `JSeshContext` holder and pass it via constructors.
- Extract command handlers from `JMDCEditorWorkflow` into the existing `jsesh/editor/actions/` package structure.
- Replace nanoxml inside `jhotdrawfw` with standard Java XML APIs.
- Isolate all custom JHotDraw modifications into a single clearly-named package as a stepping stone toward eventually publishing the fork or replacing it.

---

## Appendix: Key Files to Watch

| File                                                             | LOC   | Why it matters                                      |
| ---------------------------------------------------------------- | ----- | --------------------------------------------------- |
| `jsesh/editor/JMDCEditorWorkflow.java`                           | 1,986 | Central command handler; highest refactoring ROI    |
| `jsesh/mdcDisplayer/layout/Layout.java`                          | 924   | Explicitly flagged in TODO; single implementation   |
| `jsesh/editor/JMDCEditor.java`                                   | 973   | Main Swing component; entangled state               |
| `jsesh/hieroglyphs/data/coreMdC/ManuelDeCodage.java`             | —     | Most-used singleton; start singleton migration here |
| `jsesh/hieroglyphs/fonts/GnutraceHieroglyphShapeRepository.java` | 1,288 | Large font data class with singleton access         |
| `cupruntime/`                                                    | 1,513 | Entire module is a duplicate — delete it            |
| `jsesh/newEdit/`                                                 | ~200  | Experimental package — remove from production tree  |

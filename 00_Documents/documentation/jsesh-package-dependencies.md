# Package dependencies inside the `jsesh` module

All three diagrams below are generated from the actual `import` statements found
in `jsesh/src/main/java` (2026-07-21). Edge labels = number of import
statements.

- The **first** diagram is the detailed one: `jsesh.ui` is broken down into its
  six sub-packages.
- The **second** is the bird's-eye view: only top-level `jsesh.*` packages,
  with the whole UI collapsed into a single `jsesh.ui` node.
- The **third** is the second one with all transitive edges removed — the
  architecture's skeleton, useful for seeing the layering at a glance rather
  than for looking up who imports what.

Conventions: solid arrows are dependencies going *downwards* (the intended
direction); the dotted red arrow goes *upwards* — a lower layer depending on a
higher one.

## 1. Detailed view

```mermaid
%%{init: {"flowchart": {"htmlLabels": true}} }%%
flowchart TD
    %% State as of 2026-09-24, after:
    %%   - jsesh.parser -> jsesh.model removed: MDCParserModelGenerator and
    %%     AstModelBuilder moved to the new jsesh.mdcreader package, and the
    %%     AST now stores the lexer's own enums instead of model constants.
    %%     Edges touching parser/mdcreader recounted on 2026-09-24; the others
    %%     are still the 2026-07-21 counts.
    %%   - jsesh.glyphs -> jsesh.ui.widgets removed: the dead `import` that
    %%     outlived its {@link} in UserSignWriter is gone. jsesh.glyphs now has
    %%     three outgoing edges, all pointing down, and nothing below the UI layer
    %%     imports the UI any more.
    %%   - jsesh.signcodes -> jsesh.glyphs removed: basicGardinerCodes.txt moved
    %%     to src/main/resources/jsesh/signcodes/, and ManuelDeCodage now loads it
    %%     itself instead of going through EmbeddedGlyphsPathResources.
    %%     jsesh.signcodes is now a pure leaf: zero outgoing edges.
    %%   - (earlier) jsesh.model -> jsesh.io removed: the model no longer reaches
    %%     up into serialisation.
    %%   - (earlier) the sign-code core extracted into jsesh.signcodes
    %%     (GardinerCode, ManuelDeCodage, CanonicalCode, HieroglyphCodesSource):
    %%     jsesh.model and jsesh.glyphs used to be a mutual pair; now they both
    %%     depend *down* on jsesh.signcodes and no longer on each other.
    %%   - jsesh.graphics.glyphs folded into jsesh.glyphs (the bzr fonts moved
    %%     in), so the old glyphs <-> graphics.glyphs pair is gone.
    %%   - (earlier) the Swing/UI reorganisation:
    %%       jsesh.swing           -> jsesh.ui.widgets
    %%       jsesh.editor          -> jsesh.ui.editor
    %%       jsesh.clipboard       -> jsesh.ui.clipboard
    %%       jsesh.graphics.export -> jsesh.ui.export
    %%       jsesh.glossary        -> split into jsesh.glossary (model side)
    %%                                 and jsesh.ui.glossary (editor UI)
    %%       jsesh.ui.palette      -> new

    classDef base fill:#EEF6FF,stroke:#666,color:#000;
    classDef core fill:#E9F7E9,stroke:#666,color:#000;
    classDef docLayer fill:#E4F1E4,stroke:#666,color:#000;
    classDef mid fill:#FFF6E0,stroke:#666,color:#000;
    classDef conf fill:#FFF0F8,stroke:#666,color:#000;
    classDef uiStyle fill:#FDECEC,stroke:#666,color:#000;
    classDef ext fill:#F0F0F0,stroke:#666,color:#000,stroke-dasharray: 3 3;

    subgraph Base[base]
        utils[jsesh.utils]
        platform[jsesh.platform]
    end

    subgraph Core[core]
        signcodes[jsesh.signcodes]
        model[jsesh.model]
        parser[jsesh.parser]
        glyphs[jsesh.glyphs]
    end

    subgraph Reader[reader]
        mdc[jsesh.mdcreader]
    end

    subgraph Document[document]
        doc[jsesh.document]
    end

    subgraph Middle[middle]
        io[jsesh.io]
        render[jsesh.render]
    end

    subgraph Config[config]
        gloss[jsesh.glossary]
        defaults[jsesh.defaults]
    end

    subgraph UILayer[UI]
        widgets[jsesh.ui.widgets]
        palette[jsesh.ui.palette]
        clip[jsesh.ui.clipboard]
        export[jsesh.ui.export]
        editor[jsesh.ui.editor]
        uigloss[jsesh.ui.glossary]
    end

    res[jsesh.resources]

    utils -->|1| res
    model -->|6| signcodes
    model -->|3| utils
    parser -->|2| signcodes
    mdc -->|36| parser
    mdc -->|35| model
    glyphs -->|16| signcodes
    glyphs -->|7| utils
    glyphs -->|1| platform
    doc -->|26| model
    doc -->|2| parser
    doc -->|1| mdc
    render -->|143| model
    render -->|14| glyphs
    render -->|5| utils
    render -->|2| parser
    render -->|1| mdc
    render -->|3| signcodes
    render -->|2| doc
    render -->|2| platform
    io -->|44| model
    io -->|7| doc
    io -->|6| utils
    io -->|4| parser
    io -->|2| mdc
    io -->|1| signcodes
    gloss -->|4| glyphs
    gloss -->|3| model
    gloss -->|2| utils
    gloss -->|1| parser
    gloss -->|1| mdc
    gloss -->|1| signcodes
    defaults -->|17| glyphs
    defaults -->|5| utils
    defaults -->|2| gloss
    defaults -->|1| signcodes
    widgets -->|14| render
    widgets -->|14| glyphs
    widgets -->|7| model
    widgets -->|6| utils
    widgets -->|4| signcodes
    widgets -->|2| platform
    palette -->|14| glyphs
    palette -->|5| signcodes
    palette -->|3| utils
    palette -->|1| editor
    clip -->|7| render
    clip -->|4| export
    clip -->|3| model
    clip -->|1| io
    export -->|47| render
    export -->|35| model
    export -->|15| utils
    export -->|7| res
    export -->|4| io
    export -->|3| doc
    export -->|3| widgets
    export -->|1| glyphs
    editor -->|67| model
    editor -->|27| render
    editor -->|14| doc
    editor -->|7| glyphs
    editor -->|6| clip
    editor -->|5| utils
    editor -->|4| defaults
    editor -->|2| parser
    editor -->|2| io
    editor -->|1| widgets
    editor -->|1| export
    editor -->|1| res
    uigloss -->|3| gloss
    uigloss -->|3| editor
    uigloss -->|2| glyphs
    uigloss -->|2| render
    uigloss -->|2| res
    uigloss -->|2| utils
    uigloss -->|1| defaults
    uigloss -->|1| widgets
    res -.- resNote["Lives in the jseshLabels<br/>module, not in jsesh"]

    class utils,platform base;
    class signcodes,model,parser,glyphs core;
    class mdc docLayer;
    class doc docLayer;
    class io,render mid;
    class gloss,defaults conf;
    class widgets,palette,clip,export,editor,uigloss uiStyle;
    class res,resNote ext;

    linkStyle 0 stroke:#cc0000,stroke-width:2px;
```

**Legend**

| layer | packages |
|---|---|
| base | utils, platform |
| core | signcodes, model, parser, glyphs |
| reader | mdcreader |
| document | document |
| middle | io, render |
| config | glossary, defaults |
| UI | ui.widgets, ui.palette, ui.clipboard, ui.export, ui.editor, ui.glossary |

**No** red edge is left, and `model` and `parser` no longer depend on each
other in either direction (see history below): the parser only builds a
literal AST, and `jsesh.mdcreader` is the one package that turns that AST
into the model.
`jsesh.signcodes` and `jsesh.platform` are both clean: no outgoing edge.
`jsesh.glyphs` is clean too: it only reaches down to signcodes, utils and
platform.
`jsesh.model` no longer serialises itself (`model -> io` is gone) and no
longer knows the sign database (`model <-> glyphs` is gone); both now go
through `jsesh.signcodes`.
The whole UI lives under `jsesh.ui.*`, and nothing below it imports it.

## 2. Top-level view

```mermaid
flowchart TD
    %% Same data, but only top-level jsesh.* packages: the six jsesh.ui.*
    %% sub-packages are collapsed into a single jsesh.ui node.

    classDef base fill:#EEF6FF,stroke:#666,color:#000;
    classDef core fill:#E9F7E9,stroke:#666,color:#000;
    classDef docLayer fill:#E4F1E4,stroke:#666,color:#000;
    classDef mid fill:#FFF6E0,stroke:#666,color:#000;
    classDef conf fill:#FFF0F8,stroke:#666,color:#000;
    classDef uiStyle fill:#FDECEC,stroke:#666,color:#000;
    classDef ext fill:#F0F0F0,stroke:#666,color:#000,stroke-dasharray: 3 3;

    subgraph Base[base]
        utils[jsesh.utils]
        platform[jsesh.platform]
    end

    subgraph Core[core]
        signcodes[jsesh.signcodes]
        model[jsesh.model]
        parser[jsesh.parser]
        glyphs[jsesh.glyphs]
    end

    subgraph Reader[reader]
        mdc[jsesh.mdcreader]
    end

    subgraph Document[document]
        doc[jsesh.document]
    end

    subgraph Middle[middle]
        io[jsesh.io]
        render[jsesh.render]
    end

    subgraph Config[config]
        gloss[jsesh.glossary]
        defaults[jsesh.defaults]
    end

    ui[jsesh.ui]:::uiStyle
    res[jsesh.resources]

    utils -->|1| res
    model -->|6| signcodes
    model -->|3| utils
    parser -->|2| signcodes
    mdc -->|36| parser
    mdc -->|35| model
    glyphs -->|16| signcodes
    glyphs -->|7| utils
    glyphs -->|1| platform
    doc -->|26| model
    doc -->|2| parser
    doc -->|1| mdc
    render -->|143| model
    render -->|14| glyphs
    render -->|5| utils
    render -->|2| parser
    render -->|1| mdc
    render -->|3| signcodes
    render -->|2| doc
    render -->|2| platform
    io -->|44| model
    io -->|7| doc
    io -->|6| utils
    io -->|4| parser
    io -->|2| mdc
    io -->|1| signcodes
    gloss -->|4| glyphs
    gloss -->|3| model
    gloss -->|2| utils
    gloss -->|1| parser
    gloss -->|1| mdc
    gloss -->|1| signcodes
    defaults -->|17| glyphs
    defaults -->|5| utils
    defaults -->|2| gloss
    defaults -->|1| signcodes
    ui -->|112| model
    ui -->|97| render
    ui -->|38| glyphs
    ui -->|31| utils
    ui -->|17| doc
    ui -->|10| res
    ui -->|9| signcodes
    ui -->|7| io
    ui -->|5| defaults
    ui -->|3| gloss
    ui -->|2| parser
    ui -->|2| platform
    res -.- resNote["Lives in the jseshLabels<br/>module, not in jsesh"]

    class utils,platform base;
    class signcodes,model,parser,glyphs core;
    class mdc docLayer;
    class doc docLayer;
    class io,render mid;
    class gloss,defaults conf;
    class res,resNote ext;

    linkStyle 0 stroke:#cc0000,stroke-width:2px;
```

**Legend**

At this granularity the graph is a **DAG**.
`jsesh.ui` is again a pure consumer: 12 outgoing edges, **zero incoming**.
`jsesh.platform` (2 incoming, zero outgoing) and `jsesh.signcodes`
(6 incoming, zero outgoing) are pure leaves.
There is no mutual pair and no upward edge left in the whole module.
(`model <-> parser` in both directions, `model <-> glyphs`, `model -> io`, `signcodes <-> glyphs`, `glyphs -> ui` and
`glyphs <-> graphics.glyphs` are all gone.)

## 3. Top-level view, transitively reduced

Same data as §2, with every **transitive** edge removed: if `a -> b` and
`b -> c` are present, the shortcut `a -> c` is dropped, since it tells us
nothing the two others didn't already. What is left is the *skeleton* of the
architecture — 16 edges instead of 42.

Two remarks on method:

- `model` and `parser` are independent: neither imports the other. Every
  package that needs both goes through `jsesh.mdcreader`, so `document`,
  `glossary` and the rest reach `model` and `parser` via that single edge.
- Edge labels are still the raw import counts, so a reduced edge carries the
  same number as in §2. Note how *large* some of the dropped shortcuts are:
  `render -> model` is 143 imports and `ui -> model` 112, yet both are
  redundant — `render` and `ui` already reach `model` through `document`.
  Transitivity says nothing about how heavily a package is used, only that a
  path already exists.

```mermaid
flowchart TD
    %% Transitive reduction of the top-level graph (state 2026-09-24).
    %% Dropped because a path already existed:
    %%   ui -> {model, parser, glyphs, utils, document, resources, signcodes,
    %%          glossary, platform}
    %%   render -> {model, parser, mdcreader, signcodes, utils, platform}
    %%   io -> {model, parser, mdcreader, signcodes, utils}
    %%   document -> {model, parser}
    %%   defaults -> {glyphs, signcodes, utils}
    %%   glossary -> {model, parser, signcodes, utils}

    classDef base fill:#EEF6FF,stroke:#666,color:#000;
    classDef core fill:#E9F7E9,stroke:#666,color:#000;
    classDef docLayer fill:#E4F1E4,stroke:#666,color:#000;
    classDef mid fill:#FFF6E0,stroke:#666,color:#000;
    classDef conf fill:#FFF0F8,stroke:#666,color:#000;
    classDef uiStyle fill:#FDECEC,stroke:#666,color:#000;
    classDef ext fill:#F0F0F0,stroke:#666,color:#000,stroke-dasharray: 3 3;

    ui[jsesh.ui]:::uiStyle

    subgraph MidRow[" "]
        io[jsesh.io]
        render[jsesh.render]
        defaults[jsesh.defaults]
    end

    gloss[jsesh.glossary]
    doc[jsesh.document]
    mdc[jsesh.mdcreader]

    subgraph CoreRow[" "]
        model[jsesh.model]
        parser[jsesh.parser]
    end

    glyphs[jsesh.glyphs]

    subgraph BaseRow[" "]
        signcodes[jsesh.signcodes]
        utils[jsesh.utils]
        platform[jsesh.platform]
    end

    res[jsesh.resources]

    ui -->|97| render
    ui -->|7| io
    ui -->|5| defaults
    io -->|7| doc
    render -->|14| glyphs
    render -->|2| doc
    defaults -->|2| gloss
    gloss -->|4| glyphs
    gloss -->|1| mdc
    doc -->|1| mdc
    mdc -->|35| model
    mdc -->|36| parser
    parser -->|2| signcodes
    model -->|6| signcodes
    model -->|3| utils
    glyphs -->|16| signcodes
    glyphs -->|7| utils
    glyphs -->|1| platform
    utils -->|1| res
    res -.- resNote["Lives in the jseshLabels<br/>module, not in jsesh"]

    class io,render mid;
    class defaults conf;
    class gloss conf;
    class doc docLayer;
    class mdc docLayer;
    class model,parser core;
    class glyphs core;
    class signcodes,utils base;
    class platform base;
    class res,resNote ext;

    linkStyle 0 stroke:#cc0000,stroke-width:2px;
```

**Legend**

42 edges reduce to 16. The skeleton has a single source and three sinks:
`jsesh.ui` needs only **three** direct dependencies — `render`, `io`,
`defaults` — and reaches everything else through them;
`signcodes`, `platform` and `resources` are the sinks.
The interesting shape is that it is **not one chain but two branches**
meeting at the bottom: the *document* branch
(ui → io/render → document → mdcreader → model, parser) and the *sign* branch
(ui → render/defaults → glossary → glyphs). Nothing in `model`, `parser`
or `document` imports `glyphs`, and nothing in `glyphs` imports the model —
they are independent, and only meet again at `signcodes` and `utils`.
That separation is exactly what extracting `jsesh.signcodes` bought.
There is no red edge left, and `model` and `parser` are now two independent
core leaves joined only by `jsesh.mdcreader`.

## History of the clean-up

**Renamings / moves**

| before | after |
|---|---|
| `jsesh.swing` | `jsesh.ui.widgets` |
| `jsesh.editor` | `jsesh.ui.editor` |
| `jsesh.clipboard` | `jsesh.ui.clipboard` |
| `jsesh.graphics.export` | `jsesh.ui.export` |
| `jsesh.glossary` (mixed) | `jsesh.glossary` (model) + `jsesh.ui.glossary` (UI) |
| `jsesh.defaults.PredefinedFonts` | `jsesh.glyphs.fonts.PredefinedFonts` |
| Gardiner-code core (was under `jsesh.glyphs`) | `jsesh.signcodes` (GardinerCode, ManuelDeCodage, CanonicalCode, HieroglyphCodesSource) |
| `jsesh.graphics.glyphs` | merged into `jsesh.glyphs` (bzr fonts included) |
| `resources/jsesh/glyphs/resources/basicGardinerCodes.txt` | `resources/jsesh/signcodes/basicGardinerCodes.txt` |
| — | `jsesh.ui.palette` (new) |
| `jsesh.parser.MDCParserModelGenerator`, `jsesh.parser.AstModelBuilder` | `jsesh.mdcreader` (new) |

**Layering violations fixed** (edges that no longer exist)

- `parser -> model` (was 4; removed 2026-09-24). This one was not an upward
  edge, but it tied the syntax layer to the model. It had two causes.
  `MDCParserModelGenerator` and `AstModelBuilder`, which interpret the AST
  into the model, moved to the new `jsesh.mdcreader` package. The AST stored
  model constants (`SymbolCodes` ints, `WordEndingCode`, the model's
  `ToggleType`); it now stores the lexer's own enums (`SignSubType`,
  `PhilologyKind`, `jsesh.parser.lexer.ToggleType`, and a new
  `jsesh.parser.ast.WordEnding`), and the mapping to model constants moved
  from `MDCParser` to `AstModelBuilder`. Moving the bridge to `jsesh.io`
  was ruled out, because `jsesh.document` (`HieroglyphicTextModel`) needs it
  and `io -> document` already exists, which would create a cycle.
  Some AST javadoc still `{@link}`s model classes for comparison; those are
  documentation cross-references, not imports.

- `model -> parser` (was 2, red; removed 2026-09-24) — two causes. The
  `jsesh.model.api` marker interfaces (`CadratInterface`, `HBoxInterface`…)
  were implemented by both the model classes and the `jsesh.parser.ast`
  nodes, but carried no methods and were never used as types, so the
  package was deleted outright. `jsesh.model.tools.MDCCodeExtractor`, which
  parses an MdC string and walks the AST, moved to `jsesh.parser`, where it
  belongs. `jsesh.model` no longer imports `jsesh.parser` in any form; the
  edge counts in the diagrams above predate this change and were not
  regenerated.

- `glyphs -> ui.widgets` (was 1, red) — a javadoc-only `{@link}` in
  `UserSignWriter`, and then the orphaned `import` that survived the `{@link}`'s
  removal. Both are gone; `jsesh.glyphs` no longer references the UI in any
  form, and `jsesh.ui` is once more a pure consumer.
- `signcodes -> glyphs` (was 1, red) — `ManuelDeCodage` read its Gardiner-code
  list through `jsesh.glyphs.resources.EmbeddedGlyphsPathResources`, which was
  the only thing keeping `signcodes` from being a leaf. The resource file moved
  to `src/main/resources/jsesh/signcodes/`, `ManuelDeCodage` opens it itself
  with a private `getBasicGardinerCodes()`, and the now-unused accessor was
  dropped from `EmbeddedGlyphsPathResources`. Note the resources caveat: the
  stream is resolved relative to the class's package, so the `.txt` had to
  move together with its reader.
- `model -> io` (was 3, red) — the document model no longer reaches up into
  serialisation. This was one of the two red edges left in the previous
  revision.
- `model <-> glyphs` (was `model -> glyphs` 6 and `glyphs -> model` 2) — the
  Gardiner-code identity classes moved out of `jsesh.glyphs` into the new
  `jsesh.signcodes` package, which both `model` and `glyphs` now depend on
  *downwards*. The two used to be a mutual pair; they no longer import each
  other at all.
- `glyphs <-> graphics.glyphs` — `jsesh.graphics.glyphs` was folded into
  `jsesh.glyphs`, so both directions of that pair vanished.
- `render -> editor`, `swing -> editor`, `swing -> defaults`,
  `defaults -> editor`, `export -> editor`, `utils -> platform`.
- `io -> ui.export` — `PDFExportConstants` moved to `jsesh.io.constants`, so
  the PDF exporter and the PDF importer both import it downwards.
- `glyphs -> render`.
- `glyphs -> defaults` — `ExternalSignImporterModel` used exactly one method
  of `UserFontDirectoryManager`, so that method became the one-method
  interface `jsesh.glyphs.signsource.UserSignWriter`, which
  `UserFontDirectoryManager` implements. `ui.widgets -> defaults` went away
  with it, and the application modules were untouched: they still pass a
  `UserFontDirectoryManager`, which simply satisfies the interface.
  This deliberately keeps the app-scoped, preference-touching class *out* of
  `jsesh.glyphs`: no file under `jsesh/glyphs/` references `java.util.prefs`,
  and embedders can supply their own writer.
- `render -> defaults` — `PredefinedFonts` was not app-scoped at all (it only
  wrapped four `jsesh.glyphs.fonts` classes), so it moved *down* into
  `jsesh.glyphs.fonts` rather than its caller moving up. `jsesh.defaults` is
  left holding only genuine app-scoped assembly.

**Watch out for javadoc-only imports.** Importing a class purely to shorten a
`{@link}` creates a real edge in this graph even though no code depends on it.
It happened twice. `PredefinedFonts` briefly reintroduced `glyphs -> defaults`
that way, and `jsesh.glyphs.signsource.UserSignWriter` then imported
`jsesh.ui.widgets.signimportdialog.ExternalSignImporterModel` solely for a
`{@link}` — reintroducing `glyphs -> ui.widgets`, the very edge the
`UserSignWriter` interface had been extracted to remove. There is a second
half to the lesson: deleting the `{@link}` is not enough, since the `import`
outlives it and keeps the edge alive. Both are now fully cleaned up.

**Layering violations remaining**: none.

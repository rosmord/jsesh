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
direction); red arrows go *upwards* — a lower layer depending on a higher one.

## 1. Detailed view

```plantuml
@startuml jsesh-package-dependencies
'
' Dependencies between the packages of the `jsesh` module, with `jsesh.ui`
' expanded.
'
' State as of 2026-07-21, after:
'   - `jsesh.glyphs -> jsesh.ui.widgets` removed: the dead `import` that
'     outlived its {@link} in UserSignWriter is gone. `jsesh.glyphs` now has
'     three outgoing edges, all pointing down, and nothing below the UI layer
'     imports the UI any more.
'   - `jsesh.signcodes -> jsesh.glyphs` removed: `basicGardinerCodes.txt` moved
'     to `src/main/resources/jsesh/signcodes/`, and ManuelDeCodage now loads it
'     itself instead of going through EmbeddedGlyphsPathResources.
'     `jsesh.signcodes` is now a pure leaf: zero outgoing edges.
'   - (earlier) `jsesh.model -> jsesh.io` removed: the model no longer reaches
'     up into serialisation.
'   - (earlier) the sign-code core extracted into `jsesh.signcodes`
'     (GardinerCode, ManuelDeCodage, CanonicalCode, HieroglyphCodesSource):
'     `jsesh.model` and `jsesh.glyphs` used to be a mutual pair; now they both
'     depend *down* on `jsesh.signcodes` and no longer on each other.
'   - `jsesh.graphics.glyphs` folded into `jsesh.glyphs` (the bzr fonts moved
'     in), so the old `glyphs <-> graphics.glyphs` pair is gone.
'   - (earlier) the Swing/UI reorganisation:
'       jsesh.swing           -> jsesh.ui.widgets
'       jsesh.editor          -> jsesh.ui.editor
'       jsesh.clipboard       -> jsesh.ui.clipboard
'       jsesh.graphics.export -> jsesh.ui.export
'       jsesh.glossary        -> split into jsesh.glossary (model side)
'                                 and jsesh.ui.glossary (editor UI)
'       jsesh.ui.palette      -> new
'
title JSesh — package dependencies inside the `jsesh` module (detailed)

skinparam packageStyle rectangle
skinparam shadowing false
skinparam linetype ortho
skinparam package {
  BackgroundColor<<base>>   #EEF6FF
  BackgroundColor<<core>>   #E9F7E9
  BackgroundColor<<doc>>    #E4F1E4
  BackgroundColor<<mid>>    #FFF6E0
  BackgroundColor<<conf>>   #FFF0F8
  BackgroundColor<<ui>>     #FDECEC
  BackgroundColor<<ext>>    #F0F0F0
  BorderColor #666666
}

together {
package "jsesh.utils"           as utils     <<base>>
package "jsesh.platform"        as platform  <<base>>
}

together {
package "jsesh.signcodes"       as signcodes <<core>>
package "jsesh.model"           as model     <<core>>
package "jsesh.parser"          as parser    <<core>>
package "jsesh.glyphs"          as glyphs    <<core>>
}

together {
package "jsesh.document"        as doc       <<doc>>
}

together {
package "jsesh.io"              as io        <<mid>>
package "jsesh.render"          as render    <<mid>>
}

together {
package "jsesh.glossary"        as gloss     <<conf>>
package "jsesh.defaults"        as defaults  <<conf>>
}

together {
package "jsesh.ui.widgets"      as widgets   <<ui>>
package "jsesh.ui.palette"      as palette   <<ui>>
package "jsesh.ui.clipboard"    as clip      <<ui>>
package "jsesh.ui.export"       as export    <<ui>>
package "jsesh.ui.editor"       as editor    <<ui>>
package "jsesh.ui.glossary"     as uigloss   <<ui>>
}

package "jsesh.resources" as res <<ext>>
note right of res
  Lives in the **jseshLabels**
  module, not in `jsesh`.
end note

' ---------------------------------------------------------------- base layer
' `jsesh.platform` has no outgoing dependency at all: it is a pure leaf.
utils  -->        res                 : 1

' ---------------------------------------------------------------- core layer
' `jsesh.signcodes` is the bottom of the core: the Gardiner-code identity
' classes that both the model and the sign database build on. It now has *no*
' outgoing edge at all — it carries its own `basicGardinerCodes.txt` resource
' instead of borrowing `glyphs`'s loader.
model  -->        signcodes           : 6
model  -->        utils               : 3
model  -[#red]->  parser              : 2

parser -->        model               : 4

glyphs -->        signcodes           : 16
glyphs -->        utils               : 7
' `jsesh.glyphs` is clean: three outgoing edges, all downwards.
glyphs -->        platform            : 1

' ------------------------------------------------------------ document layer
' `jsesh.document` holds MDCDocument, DocumentPreferences,
' HieroglyphicTextModel and the undo machinery. It is shared by `io`
' (which serialises it) and `ui.editor` (which edits it), and depends on
' nothing but the model and the parser.
doc    -->        model               : 26
doc    -->        parser              : 3

' ---------------------------------------------------------------- middle layer
render -->        model               : 143
render -->        glyphs              : 14
render -->        utils               : 5
render -->        parser              : 4
render -->        signcodes           : 3
render -->        doc                 : 2
render -->        platform            : 2

io     -->        model               : 44
io     -->        doc                 : 7
io     -->        utils               : 6
io     -->        parser              : 6
io     -->        signcodes           : 1

' ------------------------------------------------------- configuration layer
' `jsesh.glossary` is Swing-free: the table model and the editor dialog
' live in `jsesh.ui.glossary`.
' `jsesh.defaults` is now purely app-scoped assembly:
' HieroglyphResourcesBuilder, HieroglyphResources, UserFontDirectoryManager.
gloss    --> glyphs                   : 4
gloss    --> model                    : 3
gloss    --> utils                    : 2
gloss    --> parser                   : 2
gloss    --> signcodes                : 1

defaults --> glyphs                   : 17
defaults --> utils                    : 5
defaults --> gloss                    : 2
defaults --> signcodes                : 1

' ---------------------------------------------------------------- UI layer
widgets --> render                    : 14
widgets --> glyphs                    : 14
widgets --> model                     : 7
widgets --> utils                     : 6
widgets --> signcodes                 : 4
widgets --> platform                  : 2

palette --> glyphs                    : 14
palette --> signcodes                 : 5
palette --> utils                     : 3
palette --> editor                    : 1

clip    --> render                    : 7
clip    --> export                    : 4
clip    --> model                     : 3
clip    --> io                        : 1

export  --> render                    : 47
export  --> model                     : 35
export  --> utils                     : 15
export  --> res                       : 7
export  --> io                        : 4
export  --> doc                       : 3
export  --> widgets                   : 3
export  --> glyphs                    : 1

editor  --> model                     : 67
editor  --> render                    : 27
editor  --> doc                       : 14
editor  --> glyphs                    : 7
editor  --> clip                      : 6
editor  --> utils                     : 5
editor  --> defaults                  : 4
editor  --> parser                    : 2
editor  --> io                        : 2
editor  --> widgets                   : 1
editor  --> export                    : 1
editor  --> res                       : 1

uigloss --> gloss                     : 3
uigloss --> editor                    : 3
uigloss --> glyphs                    : 2
uigloss --> render                    : 2
uigloss --> res                       : 2
uigloss --> utils                     : 2
uigloss --> defaults                  : 1
uigloss --> widgets                   : 1

legend bottom
  |= layer |= packages |
  | base     | utils, platform |
  | core     | signcodes, model, parser, glyphs |
  | document | document |
  | middle   | io, render |
  | config   | glossary, defaults |
  | UI       | ui.widgets, ui.palette, ui.clipboard, ui.export, ui.editor, ui.glossary |
  **One** red edge is left: `model -> parser` (2), the back-edge of the
  `model <-> parser` pair — the last mutual pair in the module.
  `jsesh.signcodes` and `jsesh.platform` are both clean: no outgoing edge.
  `jsesh.glyphs` is clean too: it only reaches down to signcodes, utils and
  platform.
  `jsesh.model` no longer serialises itself (`model -> io` is gone) and no
  longer knows the sign database (`model <-> glyphs` is gone); both now go
  through `jsesh.signcodes`.
  The whole UI lives under `jsesh.ui.*`, and nothing below it imports it.
endlegend
@enduml
```

## 2. Top-level view

```plantuml
@startuml jsesh-toplevel-package-dependencies
'
' Same data, but only top-level `jsesh.*` packages: the six `jsesh.ui.*`
' sub-packages are collapsed into a single `jsesh.ui` node.
'
title JSesh — top-level package dependencies inside the `jsesh` module

skinparam packageStyle rectangle
skinparam shadowing false
skinparam linetype ortho
skinparam package {
  BackgroundColor<<base>> #EEF6FF
  BackgroundColor<<core>> #E9F7E9
  BackgroundColor<<doc>>  #E4F1E4
  BackgroundColor<<mid>>  #FFF6E0
  BackgroundColor<<conf>> #FFF0F8
  BackgroundColor<<ui>>   #FDECEC
  BackgroundColor<<ext>>  #F0F0F0
  BorderColor #666666
}

together {
package "jsesh.utils"     as utils     <<base>>
package "jsesh.platform"  as platform  <<base>>
}

together {
package "jsesh.signcodes" as signcodes <<core>>
package "jsesh.model"     as model     <<core>>
package "jsesh.parser"    as parser    <<core>>
package "jsesh.glyphs"    as glyphs    <<core>>
}

together {
package "jsesh.document"  as doc       <<doc>>
}

together {
package "jsesh.io"        as io        <<mid>>
package "jsesh.render"    as render    <<mid>>
}

together {
package "jsesh.glossary"  as gloss     <<conf>>
package "jsesh.defaults"  as defaults  <<conf>>
}

together {
package "jsesh.ui"        as ui        <<ui>>
}

package "jsesh.resources" as res       <<ext>>
note right of res
  Lives in the **jseshLabels**
  module, not in `jsesh`.
end note

utils     -->       res        : 1

' `jsesh.signcodes` has no outgoing edge: it is a second pure leaf.
model     -->       signcodes  : 6
model     -->       utils      : 3
model     -[#red]-> parser     : 2

parser    -->       model      : 4

glyphs    -->       signcodes  : 16
glyphs    -->       utils      : 7
glyphs    -->       platform   : 1

doc       -->       model      : 26
doc       -->       parser     : 3

render    -->       model      : 143
render    -->       glyphs     : 14
render    -->       utils      : 5
render    -->       parser     : 4
render    -->       signcodes  : 3
render    -->       doc        : 2
render    -->       platform   : 2

io        -->       model      : 44
io        -->       doc        : 7
io        -->       utils      : 6
io        -->       parser     : 6
io        -->       signcodes  : 1

gloss     -->       glyphs     : 4
gloss     -->       model      : 3
gloss     -->       utils      : 2
gloss     -->       parser     : 2
gloss     -->       signcodes  : 1

defaults  -->       glyphs     : 17
defaults  -->       utils      : 5
defaults  -->       gloss      : 2
defaults  -->       signcodes  : 1

ui        -->       model      : 112
ui        -->       render     : 97
ui        -->       glyphs     : 38
ui        -->       utils      : 31
ui        -->       doc        : 17
ui        -->       res        : 10
ui        -->       signcodes  : 9
ui        -->       io         : 7
ui        -->       defaults   : 5
ui        -->       gloss      : 3
ui        -->       parser     : 2
ui        -->       platform   : 2

legend bottom
  At this granularity the graph is a **DAG except for one edge**.
  `jsesh.ui` is again a pure consumer: 12 outgoing edges, **zero incoming**.
  `jsesh.platform` (2 incoming, zero outgoing) and `jsesh.signcodes`
  (6 incoming, zero outgoing) are pure leaves.
  `model <-> parser` is the only mutual pair left in the whole module, and
  `model -> parser` (2) the only upward edge anywhere. (`model <-> glyphs`,
  `model -> io`, `signcodes <-> glyphs`, `glyphs -> ui` and
  `glyphs <-> graphics.glyphs` are all gone.)
endlegend
@enduml
```

## 3. Top-level view, transitively reduced

Same data as §2, with every **transitive** edge removed: if `a -> b` and
`b -> c` are present, the shortcut `a -> c` is dropped, since it tells us
nothing the two others didn't already. What is left is the *skeleton* of the
architecture — 16 edges instead of 36.

Two remarks on method:

- `model` and `parser` import each other, so they are not orderable; they form
  a strongly connected component. The reduction is computed on the graph where
  that pair is treated as a **single node**, then expanded again for drawing.
  This is why both `document -> model` and `document -> parser` survive: the
  reduction can only say "`document` depends on the model/parser knot", not on
  which half.
- Edge labels are still the raw import counts, so a reduced edge carries the
  same number as in §2. Note how *large* some of the dropped shortcuts are:
  `render -> model` is 143 imports and `ui -> model` 112, yet both are
  redundant — `render` and `ui` already reach `model` through `document`.
  Transitivity says nothing about how heavily a package is used, only that a
  path already exists.

```plantuml
@startuml jsesh-toplevel-reduced
'
' Transitive reduction of the top-level graph (state 2026-07-21).
' Dropped because a path already existed:
'   ui -> {model, parser, glyphs, utils, document, resources, signcodes,
'          glossary, platform}
'   render -> {model, parser, signcodes, utils, platform}
'   io -> {model, parser, signcodes, utils}
'   defaults -> {glyphs, signcodes, utils}
'   glossary -> {signcodes, utils}
'
title JSesh — top-level dependencies, transitively reduced

skinparam packageStyle rectangle
skinparam shadowing false
skinparam linetype ortho
skinparam package {
  BackgroundColor<<base>> #EEF6FF
  BackgroundColor<<core>> #E9F7E9
  BackgroundColor<<doc>>  #E4F1E4
  BackgroundColor<<mid>>  #FFF6E0
  BackgroundColor<<conf>> #FFF0F8
  BackgroundColor<<ui>>   #FDECEC
  BackgroundColor<<ext>>  #F0F0F0
  BorderColor #666666
}

package "jsesh.ui"        as ui        <<ui>>

together {
package "jsesh.io"        as io        <<mid>>
package "jsesh.render"    as render    <<mid>>
package "jsesh.defaults"  as defaults  <<conf>>
}

package "jsesh.glossary"  as gloss     <<conf>>
package "jsesh.document"  as doc       <<doc>>

together {
package "jsesh.model"     as model     <<core>>
package "jsesh.parser"    as parser    <<core>>
}

package "jsesh.glyphs"    as glyphs    <<core>>

together {
package "jsesh.signcodes" as signcodes <<core>>
package "jsesh.utils"     as utils     <<base>>
package "jsesh.platform"  as platform  <<base>>
}

package "jsesh.resources" as res       <<ext>>
note right of res
  Lives in the **jseshLabels**
  module, not in `jsesh`.
end note

ui       -->       render     : 97
ui       -->       io         : 7
ui       -->       defaults   : 5

io       -->       doc        : 7

render   -->       glyphs     : 14
render   -->       doc        : 2

defaults -->       gloss      : 2

gloss    -->       glyphs     : 4
gloss    -->       model      : 3
gloss    -->       parser     : 2

doc      -->       model      : 26
doc      -->       parser     : 3

' The one irreducible knot: a genuine cycle, so neither side can be dropped.
model    -[#red]-> parser     : 2
parser   -->       model      : 4

model    -->       signcodes  : 6
model    -->       utils      : 3

glyphs   -->       signcodes  : 16
glyphs   -->       utils      : 7
glyphs   -->       platform   : 1

utils    -->       res        : 1

legend bottom
  36 edges reduce to 16. The skeleton has a single source and three sinks:
  `jsesh.ui` needs only **three** direct dependencies — `render`, `io`,
  `defaults` — and reaches everything else through them;
  `signcodes`, `platform` and `resources` are the sinks.
  The interesting shape is that it is **not one chain but two branches**
  meeting at the bottom: the *document* branch
  (ui → io/render → document → model↔parser) and the *sign* branch
  (ui → render/defaults → glossary → glyphs). Nothing in `model`, `parser`
  or `document` imports `glyphs`, and nothing in `glyphs` imports the model —
  they are independent, and only meet again at `signcodes` and `utils`.
  That separation is exactly what extracting `jsesh.signcodes` bought.
  The single red edge is the `model <-> parser` cycle, which no reduction
  can remove.
endlegend
@enduml
```

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

**Layering violations fixed** (edges that no longer exist)

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

**Layering violations remaining** (1 red edge)

- `model -> parser` (2) — the core model still reaches up into parsing
  (`jsesh.model.tools.MDCCodeExtractor` runs `MDCParserFacade`). This is the
  one knot left, and the only mutual pair still in the module. Everything else
  in `jsesh` now points strictly downwards.

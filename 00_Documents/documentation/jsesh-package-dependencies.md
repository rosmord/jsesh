# Package dependencies inside the `jsesh` module

Both diagrams below are generated from the actual `import` statements found in
`jsesh/src/main/java` (2026-07-19). Edge labels = number of import statements.

- The **first** diagram is the detailed one: `jsesh.ui` is broken down into its
  six sub-packages.
- The **second** is the bird's-eye view: only top-level `jsesh.*` packages,
  with the whole UI collapsed into a single `jsesh.ui` node.

Conventions: solid arrows are dependencies going *downwards* (the intended
direction); red arrows go *upwards* — a lower layer depending on a higher one.

## 1. Detailed view

```plantuml
@startuml jsesh-package-dependencies
'
' Dependencies between the packages of the `jsesh` module, with `jsesh.ui`
' expanded.
'
' State as of 2026-07-19, after:
'   - the Swing/UI reorganisation:
'       jsesh.swing           -> jsesh.ui.widgets
'       jsesh.editor          -> jsesh.ui.editor
'       jsesh.clipboard       -> jsesh.ui.clipboard
'       jsesh.graphics.export -> jsesh.ui.export
'       jsesh.glossary        -> split into jsesh.glossary (model side)
'                                 and jsesh.ui.glossary (editor UI)
'       jsesh.ui.palette      -> new
'   - PDFExportConstants moved to `jsesh.io.constants`
'   - the UserSignWriter interface extracted into `jsesh.glyphs.signsource`
'   - PredefinedFonts moved down from `jsesh.defaults` to `jsesh.glyphs.fonts`
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

package "jsesh.utils"           as utils    <<base>>
package "jsesh.platform"        as platform <<base>>

package "jsesh.model"           as model    <<core>>
package "jsesh.parser"          as parser   <<core>>
package "jsesh.glyphs"          as glyphs   <<core>>
package "jsesh.graphics.glyphs" as ggly     <<core>>

package "jsesh.document"        as doc      <<doc>>

package "jsesh.io"              as io       <<mid>>
package "jsesh.render"          as render   <<mid>>

package "jsesh.glossary"        as gloss    <<conf>>
package "jsesh.defaults"        as defaults <<conf>>

package "jsesh.ui.widgets"      as widgets  <<ui>>
package "jsesh.ui.palette"      as palette  <<ui>>
package "jsesh.ui.clipboard"    as clip     <<ui>>
package "jsesh.ui.export"       as export   <<ui>>
package "jsesh.ui.editor"       as editor   <<ui>>
package "jsesh.ui.glossary"     as uigloss  <<ui>>

package "jsesh.resources" as res <<ext>>
note right of res
  Lives in the **jseshLabels**
  module, not in `jsesh`.
end note

' ---------------------------------------------------------------- base layer
' `jsesh.platform` has no outgoing dependency at all: it is a pure leaf.
utils  -->        res                 : 1

' ---------------------------------------------------------------- core layer
model  -->        glyphs              : 6
model  -->        utils               : 3
model  -[#red]->  parser              : 4
model  -[#red]->  io                  : 3

parser -->        model               : 4

' `jsesh.glyphs` is now clean: every edge below points down or sideways
' inside the core. The sign importer takes a
' `jsesh.glyphs.signsource.UserSignWriter`, implemented up in
' `jsesh.defaults.UserFontDirectoryManager`; and `PredefinedFonts` moved
' down here, where it belongs (it only wraps `jsesh.glyphs.fonts` classes).
glyphs -->        ggly                : 8
glyphs -->        utils               : 7
glyphs -->        model               : 2
glyphs -->        parser              : 2
glyphs -->        platform            : 1

ggly   -->        glyphs              : 2

' ------------------------------------------------------------ document layer
' `jsesh.document` holds MDCDocument, DocumentPreferences,
' HieroglyphicTextModel and the undo machinery. It is shared by `io`
' (which serialises it) and `ui.editor` (which edits it), and depends on
' nothing but the model and the parser.
doc    -->        model               : 26
doc    -->        parser              : 3

' ---------------------------------------------------------------- middle layer
render -->        model               : 143
render -->        glyphs              : 17
render -->        utils               : 5
render -->        parser              : 4
render -->        doc                 : 2
render -->        platform            : 2

io     -->        model               : 42
io     -->        doc                 : 7
io     -->        utils               : 6
io     -->        parser              : 4
io     -->        glyphs              : 1

' ------------------------------------------------------- configuration layer
' `jsesh.glossary` is Swing-free: the table model and the editor dialog
' live in `jsesh.ui.glossary`.
' `jsesh.defaults` is now purely app-scoped assembly:
' HieroglyphResourcesBuilder, HieroglyphResources, UserFontDirectoryManager.
gloss    --> glyphs                   : 5
gloss    --> model                    : 4
gloss    --> parser                   : 2
gloss    --> utils                    : 1

defaults --> glyphs                   : 12
defaults --> utils                    : 4
defaults --> gloss                    : 2

' ---------------------------------------------------------------- UI layer
widgets --> render                    : 14
widgets --> glyphs                    : 10
widgets --> model                     : 7
widgets --> utils                     : 6
widgets --> platform                  : 2

palette --> glyphs                    : 19
palette --> utils                     : 3
palette --> editor                    : 1

clip    --> render                    : 7
clip    --> export                    : 4
clip    --> model                     : 3

export  --> render                    : 47
export  --> model                     : 35
export  --> utils                     : 15
export  --> res                       : 7
export  --> io                        : 3
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
editor  --> io                        : 1
editor  --> widgets                   : 1
editor  --> export                    : 1
editor  --> res                       : 1

uigloss --> gloss                     : 3
uigloss --> editor                    : 3
uigloss --> render                    : 2
uigloss --> glyphs                    : 2
uigloss --> res                       : 2
uigloss --> utils                     : 2
uigloss --> defaults                  : 1
uigloss --> widgets                   : 1

legend bottom
  |= layer |= packages |
  | base     | utils, platform |
  | core     | model, parser, glyphs, graphics.glyphs |
  | document | document |
  | middle   | io, render |
  | config   | glossary, defaults |
  | UI       | ui.widgets, ui.palette, ui.clipboard, ui.export, ui.editor, ui.glossary |
  Only **2** red edges are left, down from 10, and both belong to the same
  knot: `model -> parser` and `model -> io`.
  `jsesh.platform` is clean: no outgoing edge.
  `jsesh.document` is clean: it only reaches down to model and parser.
  `jsesh.glyphs` is clean: it no longer looks up at render or defaults.
  The whole UI lives under `jsesh.ui.*`, and nothing below it imports it.
endlegend
@enduml
```

## 2. Top-level view

```plantuml
@startuml jsesh-toplevel-package-dependencies
'
' Same data, but only top-level `jsesh.*` packages: the six `jsesh.ui.*`
' sub-packages are collapsed into a single `jsesh.ui` node, and
' `jsesh.graphics.glyphs` into `jsesh.graphics`.
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

package "jsesh.utils"     as utils    <<base>>
package "jsesh.platform"  as platform <<base>>

package "jsesh.model"     as model    <<core>>
package "jsesh.parser"    as parser   <<core>>
package "jsesh.glyphs"    as glyphs   <<core>>
package "jsesh.graphics"  as graphics <<core>>

package "jsesh.document"  as doc      <<doc>>

package "jsesh.io"        as io       <<mid>>
package "jsesh.render"    as render   <<mid>>

package "jsesh.glossary"  as gloss    <<conf>>
package "jsesh.defaults"  as defaults <<conf>>

package "jsesh.ui"        as ui       <<ui>>

package "jsesh.resources" as res      <<ext>>
note right of res
  Lives in the **jseshLabels**
  module, not in `jsesh`.
end note

utils    -->       res        : 1

model    -->       glyphs     : 6
model    -->       utils      : 3
model    -[#red]-> parser     : 4
model    -[#red]-> io         : 3

parser   -->       model      : 4

glyphs   -->       graphics   : 8
glyphs   -->       utils      : 7
glyphs   -->       model      : 2
glyphs   -->       parser     : 2
glyphs   -->       platform   : 1

graphics -->       glyphs     : 2

doc      -->       model      : 26
doc      -->       parser     : 3

render   -->       model      : 143
render   -->       glyphs     : 17
render   -->       utils      : 5
render   -->       parser     : 4
render   -->       doc        : 2
render   -->       platform   : 2

io       -->       model      : 42
io       -->       doc        : 7
io       -->       utils      : 6
io       -->       parser     : 4
io       -->       glyphs     : 1

gloss    -->       glyphs     : 5
gloss    -->       model      : 4
gloss    -->       parser     : 2
gloss    -->       utils      : 1

defaults -->       glyphs     : 12
defaults -->       utils      : 4
defaults -->       gloss      : 2

ui       -->       model      : 112
ui       -->       render     : 97
ui       -->       glyphs     : 39
ui       -->       utils      : 31
ui       -->       doc        : 17
ui       -->       res        : 10
ui       -->       defaults   : 5
ui       -->       io         : 4
ui       -->       gloss      : 3
ui       -->       parser     : 2
ui       -->       platform   : 2

legend bottom
  At this granularity the graph is **almost a DAG**.
  `jsesh.ui` is a pure consumer: 11 outgoing edges, **zero incoming**.
  `jsesh.platform` is a pure leaf: 2 incoming, zero outgoing.
  The only upward edges left are `model -> parser` and `model -> io`.
  (`model <-> glyphs`, `model <-> parser` and `glyphs <-> graphics` are
  mutual pairs; only the `model` side is coloured, following the
  convention that `model` is meant to sit at the bottom of the core.)
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
| — | `jsesh.ui.palette` (new) |

**Layering violations fixed** (edges that no longer exist)

- `render -> editor`, `swing -> editor`, `swing -> defaults`,
  `defaults -> editor`, `export -> editor`, `utils -> platform`.
- `io -> ui.export` — `PDFExportConstants` moved to `jsesh.io.constants`, so
  the PDF exporter and the PDF importer both import it downwards. This was
  the last UI reference below the UI layer.
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
`PredefinedFonts` briefly reintroduced `glyphs -> defaults` that way; the link
is now written out in full instead.

**Layering violations remaining** (2 red edges)

- `model -> parser` (4) and `model -> io` (3) — the core model still reaches
  up into parsing and serialisation. This is the one substantial knot left.


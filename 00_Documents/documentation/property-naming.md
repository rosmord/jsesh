# Naming property keys

How to name a new i18n property key in JSesh, or fix one you're touching.

## Where the file goes

New labels go in `jseshLabels/src/main/resources/jsesh/resources/labels.properties`
(and its per-locale siblings, `labels_xx.properties`). Don't add new labels to
`jhotdrawfw`'s vendored JHotDraw bundles (`org/jhotdraw_7_6/app/*.properties`)
— those are upstream JHotDraw's own generic-action files, only useful if you're adding a new **locale**, not a new **label**.

`mdi8n.properties`, `demoI8n.properties` (in `qenherkhopeshefUtils`) and
`definitionsI8n.properties` (in `signInfoAppli`) follow the same scheme below; they just live next to the module that owns them instead of in `jseshLabels`.

## Key shape

```text
<namespace>.<elementId>.<role>
```

```properties
edit.copyAsBitmap.text=Copy as Low-resolution picture
edit.copyAsBitmap.toolTipText=Copy the current selection as a bitmap image
edit.copyAsBitmap.accelerator=${accelerator.meta.[$os]} shift C
```

- **`namespace`** — the menu/feature/dialog the string belongs to, in
  **lowerCamelCase**: `edit`, `file`, `text`, `sign`, `format`,
  `fontPreferences`, `drawingPrefs`, `exportPrefs`, `search`, `glossary`,
  `errorDialog`...
  Namespace by *what the string is part of in the UI*, never by the Java
  class that happens to read it. So a new HTML-export label goes under
  `export.html.*`, not `HTMLExporter.*`.
- **`elementId`** — the specific action or widget inside that namespace, in
  **lowerCamelCase**: `copy`, `copyAsBitmap`, `browseFonts`,
  `quadratHeightLarge`. Keep a multi-word id as one lowerCamelCase segment —
  don't dot it further.
- **`role`** — which piece of the widget/action this string fills. Use the
  fixed vocabulary below.

If a `namespace.elementId` only ever needs a single plain string (no
text/tooltip/accelerator split), you can skip the role segment entirely —
e.g. `fontPreferences.smallFontBodyLimit=...` is fine as-is.

## The role vocabulary

| Role        | Suffix                   | Meaning                                                                             |
| ----------- | ------------------------ | ----------------------------------------------------------------------------------- |
| text        | `.text`                  | Menu/button/dialog label text                                                       |
| tooltip     | `.toolTipText`           | Tooltip                                                                             |
| accelerator | `.accelerator`           | Keyboard shortcut                                                                   |
| mnemonic    | `.mnemonic`              | Mnemonic letter                                                                     |
| icon        | `.icon`                  | Icon path                                                                           |
| title       | `.title`                 | Dialog/window title                                                                 |
| error       | `.error` / `.errorTitle` | Error message / error dialog title                                                  |
| comment     | `.comment`               | Explanatory sub-text                                                                |
| iconMdC     | `.iconMdC`               | *Manuel de Codage* code used tp create a glyph icon (not the same thing as `.icon`) |

Everything here is **lowerCamelCase**, including for `javax.swing.Action`-bound entries filled through `org.qenherkhopeshef.guiFramework.BundledActionFiller` (module `qenherkhopeshefUtils`). You do not need to know or care whether an id is routed through `BundledActionFiller` or through JHotDraw's `ResourceBundleUtil`/`BundleHelper` — both read this same vocabulary.

You may still encounter the legacy PascalCase suffixes (`.Name`, `.ShortDescription`, `.AcceleratorKey`, `.MnemonicKey`, `.SmallIcon`) and the `guiFramework`-only extension suffixes (`.ProxyMethod`, `.Preconditions`, `.BooleanProperty`, `.GroupProperty`, `.Argument`, `.TearOff`, `.NumberOfColumns`, `.IsLabelled`) in older entries.
They still work — `BundledActionFiller.NEW_SUFFIX_FOR_LEGACY` maps each to
its lowerCamelCase equivalent and prefers the new spelling when both are present — but don't author new entries with them. When you edit an existing entry that still uses a legacy suffix, rename it to the current spelling (lowercase the extension suffixes: `.proxyMethod`, `.preconditions`, `.booleanProperty`, `.groupProperty`, `.argument`, `.tearOff`, `.numberOfColumns`, `.isLabelled`).

`.MenuPlatform` is dead (no code reads it) — don't use it, and don't rename it to `.menuPlatform` if you happen to see it; leave it as a marker that is unimplemented.

## Casing rule

Namespace, element id, and role suffix are all **lowerCamelCase**. Never:

- a Java class name as namespace (`HTMLExporter.*` — use `export.html.*`
  instead),
- `snake_case` anywhere (`files_searched` — use `filesSearched`).

## Reuse: don't retype shared strings

- For generic, context-free vocabulary (Cancel, Browse, OK, Folder, File,
  Query, Search), reuse the existing `generic.*` bucket via `${...}`
  interpolation instead of retyping the literal:

  ```properties
  myDialog.browse.text=${generic.browse.text}
  ```
  
  Add to `generic.*` rather than duplicating a string that's already there.
- `${key}` interpolation also supports OS-variant lookup via `${key.[$os]}`,
  which is how the accelerator modifier key is shared across every
  platform-specific shortcut (`accelerator.meta.default`/`accelerator.meta.mac`,
  referenced as `${accelerator.meta.[$os]}`). Use the same mechanism for any
  other OS-dependent string instead of duplicating per-platform lines.
- One string, one key, one place: don't create a new per-file/per-module bundle to hold a string that conceptually belongs in an existing one.

## Accelerator keys on macOS

`BundledActionFiller` resolves accelerators through `.accelerator`
(falling back to legacy `.AcceleratorKey`), with a mac-specific override via
a `[mac]`-suffixed sibling key:

```properties
edit.redo.accelerator=control Y
edit.redo.accelerator[mac]=shortcut shift Z
```

If you rename an id's accelerator suffix, rename its `[mac]` sibling too —
they must match, or the mac-specific override silently stops applying. A
value starting with `shortcut` is expanded to the platform's menu shortcut
mask (⌘ on macOS, Ctrl elsewhere) automatically.

## Worked example

Adding a new "Export as GIF" menu action under the `export` feature:

```properties
export.gif.text=Export as GIF...
export.gif.toolTipText=Export the current document as a GIF image
export.gif.accelerator=${accelerator.meta.[$os]} G
export.gif.mnemonic=G
```

Not `ExportGifAction.Name=...` (class-name namespace, legacy suffix) and not `export.exportAsGif.Name=...` (redundant `elementId`, legacy suffix).

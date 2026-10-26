# JSeshJs customisation guide

The [user guide](user-guide.md) shows how to display and edit hieroglyphs with the ready-made parts. This guide is for when they are not enough: you want other colours or proportions, another source of signs, your own editor or palette (in a framework, say), or to take the pipeline apart and use a piece of it.

You should have read the user guide first. For how the code is organised inside, see the [maintainer guide](maintainer-guide.md).

JSeshJs is built in layers, so that you can leave the ready-made parts at the level you need:

| You want to... | Use | Section |
|---|---|---|
| change colours, line widths, fonts, spacing, direction | the style (`StyleChanges`) | [Changing the look](#changing-the-look) |
| draw with other signs, or load the data differently | a `GlyphProvider` | [Using your own signs, or your own data source](#using-your-own-signs-or-your-own-data-source) |
| render in Node, or measure text differently | a `TextMeasurer` | [Measuring text](#measuring-text) |
| keep the picture, but process it yourself (hit testing, overlays...) | `layOutText`, then `drawLaidOutText` | [Taking the pipeline apart](#taking-the-pipeline-apart) |
| change the text programmatically | the model | [Working with the model](#working-with-the-model) |
| an editor with your own markup or inside a framework | `EditorSession`, `EditorView` | [Building your own editor](#building-your-own-editor) |
| a palette with your own look | `PaletteController` | [Building your own palette](#building-your-own-palette) |
| Vue or React components on the headless parts | `subscribe` / `state` | [Headless parts in Vue and React](#headless-parts-in-vue-and-react) |

---

## Changing the look

### The style

Everything about the appearance of a text is in a `JSeshStyle` (from `@jsesh/core`), made of four parts:

| Part | Content |
|---|---|
| `geometry` | Sizes and spaces, in **points**. `standardSignHeight` is the height of the sign A1 (18 by default); `lineSkip`, `columnSkip`, `smallSkip` are the spaces between lines, columns and small things; `maxCadratHeight` and `maxCadratWidth` limit the size of a quadrat; `leftMargin` and `topMargin` surround the text; `cartoucheLineWidth`, `cartoucheLoopLength`, `cartoucheMargin`, `hwtSquareSize`, `bastionDepth`... shape the enclosures; `fineLineWidth` and `wideLineWidth` are for `{l...}` and `{L...}` lines; `textWidth` is the length of horizontal rules. |
| `painting` | Colours (any CSS colour): `blackColor`, `redColor`, `grayColor` (shading), `backgroundColor`, `cursorColor`; `shadingStyle` (`"GRAY_SHADING"` or `"LINE_HATCHING"`); `colorMap` (see below). |
| `fonts` | The fonts of alphabetic text: `plainFont`, `boldFont`, `italicFont`, `superScriptFont`, `transliterationFont` (`{ family, size, style?, weight? }`), and the transliteration options (`translitUnicode`, `yodChoice`, `gardinerQofUsed`). |
| `options` | `textDirection` (`"LEFT_TO_RIGHT"` or `"RIGHT_TO_LEFT"`), `textOrientation` (`"HORIZONTAL"` or `"VERTICAL"`), `smallSignCentered`, `justified`, `paged`. |

`DEFAULT_STYLE` is JSesh's own. You almost never write a whole style: you give **changes**, an object with the same four parts, each one optional and itself partial (`StyleChanges`). `withStyle(style, changes)` returns a new style.

```js
import { DEFAULT_STYLE, withStyle } from "@jsesh/core";

const style = withStyle(DEFAULT_STYLE, {
  painting: { blackColor: "#223366", redColor: "#aa0000" },
  geometry: { lineSkip: 10, leftMargin: 4 },
  options: { textDirection: "RIGHT_TO_LEFT" },
});
```

Lengths are in points, and **the scale is separate**: a picture's size in CSS pixels is set by `cadratHeight` (the height of a quadrat, that is of A1, in pixels). The scale applied to the points is `cadratHeight / geometry.standardSignHeight`. So changing `standardSignHeight` alone changes the *proportions* of the spaces with respect to the signs, not the size of the picture.

### Where to put the changes

| Where | Applies to |
|---|---|
| `configureJSesh({ style })` | All the texts of the page. |
| `jseshStyle` property of `<jsesh-text>` or `<jsesh-editor>`, `jseshStyle` prop of the Vue components | One element. |
| the `style` option of `renderMdc`, `layOutInPage`, `layOutText`, `renderToSvg` | One call. |

In a page, they pile up in this order: JSesh's default style, the small margins JSeshJs uses in web pages (`leftMargin` and `topMargin` of 1 point), the `configureJSesh` changes, then the element's own changes and attributes (`direction`, `orientation`, `centered`, `justified`).

When `layOutText` or `renderToSvg` is given a **string** which is a whole JSesh document (with a header), the preferences of the header are applied to the default style first, and a *partial* `style` then comes on top. If you pass a *complete* `JSeshStyle` (all four parts), it replaces everything, header preferences included.

### Colouring signs by tag

`painting.colorMap` maps the name of a sign's modifier (a "tag", as in `A1\det`) to a colour. It lets a page colour, say, the determinatives:

```js
configureJSesh({ style: { painting: { colorMap: { det: "#0a7a4a" } } } });
```

### Fonts

Alphabetic text (transliteration, Latin, Hebrew...) is drawn with the fonts of the style, as SVG `<text>`. The transliteration font is `CharisSIL`, which JSeshJs declares itself (a `FontFace` pointing to `fonts/CharisSIL-R.woff` in the glyph data) and waits for before measuring. To use another font, change its `family` in the style; the browser must be able to load it before the page is laid out, because the width of text is measured with a canvas (see [Measuring text](#measuring-text)). Declare it with `@font-face` in your CSS: JSeshJs waits for the fonts of the style (`document.fonts.load`) before it measures anything.

Hieroglyphs themselves are not text: they are paths, from the glyph data.

### The SVG

`drawLaidOutText(laidOut, options)` and `renderToSvg` take the options of `SvgOptions`:

| Option | Role |
|---|---|
| `scale` or `cadratHeight` | Size of the picture. |
| `title` | Accessible description (a `<title>`); the picture is `aria-hidden` without it. |
| `idPrefix` | Prefix of the ids defined in the picture. **Two pictures in one page need different prefixes**; the elements and `renderMdc` take care of it. |
| `background` | Fill the background with `painting.backgroundColor` (transparent by default). |
| `attributes` | Extra attributes for the `<svg>` element, **already escaped**. |
| `showKerningMarks` | Draw `@hkern` and `@vkern` as marks, in `painting.cursorColor` (false by default; meant for editors). |

The SVG is a plain string. To style it from CSS, give `attributes: 'class="my-text"'`, or wrap it. Colours are set in the style rather than in CSS, since they are written into the paths.

---

## Using your own signs, or your own data source

The layout and the drawing never read files: they ask a **`GlyphProvider`** (an interface of `@jsesh/core`). `@jsesh/glyphs` provides one, `GlyphIndexProvider`, which reads JSesh's data, but you can supply another.

```ts
interface GlyphProvider {
  canonicalCode(code: string): string;                            // "nfr" -> "F35"
  size(code: string): { width: number; height: number } | undefined;
  ligatureZone(code: string, i: number): LigatureZone | undefined; // i: 0 before, 1 after, 2 below
  ligaturePositions(codes: readonly string[]): readonly ExplicitPosition[] | undefined;
  shape(code: string): SignShape | undefined;                      // { d: "<SVG path>", evenOdd? }
  loadShapes(codes: Iterable<string>): Promise<void>;
}
```

Sizes, zones and ligatures must be available at once (they are all the layout needs); only the shapes may arrive later, and `loadShapes` is called before drawing with the codes of the text. Units are the font's (A1 is about 18 high in JSesh's font); the drawing rescales them. `canonicalCode` may simply return its argument, but A1 must exist: it is the unit of size.

### Loading the same data differently

`GlyphIndexProvider(index, loadShape)` takes the content of `glyph-index.json` and a function which loads the shape stored in a file. Replace that function to read shapes from anywhere (a database, a different URL scheme, a bundled object...):

```js
import { GlyphIndexProvider, fileNameForCode } from "@jsesh/glyphs";

const provider = new GlyphIndexProvider(index, async (fileName) => {
  const response = await fetch(`https://cdn.example.org/shapes/${fileName}.json`, { credentials: "include" });
  return response.json(); // { d: "M..." }
});
```

`fileNameForCode(code)` gives the name of the file of a sign (case is encoded, because file systems are not case-sensitive everywhere).

### Telling the page to use it

```js
configureJSesh({ glyphs: provider }); // instead of glyphsUrl
```

Every `<jsesh-text>`, `<jsesh-editor>` and `renderMdc` then uses this provider (unless an element has its own `glyphs-url`). Note that the palette needs the sign *database*, a different thing: pass one to it with its `database` property.

### A provider of your own

A complete implementation is short. This one draws a few signs of your own, as paths:

```js
const signs = {
  A1: { width: 16, height: 18, d: "M0 0H16V18H0Z" },
  // ...
};

const provider = {
  canonicalCode: (code) => code,
  size: (code) => signs[code],
  ligatureZone: () => undefined,
  ligaturePositions: () => undefined,
  shape: (code) => (signs[code] ? { d: signs[code].d } : undefined),
  loadShapes: async () => {},
};
configureJSesh({ glyphs: provider });
```

The path of a sign has its bounding box starting at 0,0 and its coordinates grow downwards, as in SVG. Without ligature zones, a ligature (`&`) cannot fit one sign into another.

### The format of JSesh's data

If you produce data in the format of `@jsesh/glyphs` (to add a font, for instance), this is what `GlyphIndexProvider` reads:

- `glyph-index.json`: `{ format, canonical, ligatures, glyphs }`. `glyphs` maps a canonical code to `[width, height]` or `[width, height, [zone, zone, zone]]`, a zone being `null` or `[x, y, width, height, gravity]` with `gravity` made of a horizontal letter (`s`tart, `c`enter, `e`nd) and a vertical one (`t`op, `c`enter, `b`ottom). `canonical` maps the other codes (phonetic values) to canonical codes. `ligatures` lists the predefined ligatures: `[[codes...], [[x, y, scale]...]]` (positions in thousandths of the height of A1, scale in percent).
- `glyphs/<file>.json`: `{ "d": "<SVG path>" }`, one per sign.
- `sign-info.json`, for the palette (families, values, tags, variants), read by `SignDatabase`; see `SignInfoData` in `@jsesh/glyphs`.

`glyph-index.json` and `sign-info.json` carry a `format` number, which is incremented when the layout of the data changes.

---

## Measuring text

Alphabetic text (and the small texts above or beside signs) has a width that only a font can tell. The layout therefore asks a **`TextMeasurer`**, and the result goes into the positions of everything else. Three implementations exist:

- `CanvasTextMeasurer`: uses a canvas (`OffscreenCanvas`, or a `<canvas>`). It is the one used in browsers, and it requires the fonts to be loaded;
- `ApproximateTextMeasurer`: guesses (about half the font size per character). This is what runs in Node when no canvas is available. Hieroglyphs do not depend on it;
- your own, for instance on top of `node-canvas` or a font library, to get exact widths on a server:

```js
import { renderToSvg } from "@jsesh/core";

const measurer = {
  measureRun(fonts, script, pieces, rightToLeft) { /* -> { offsets, widths, width } */ },
  characterMetrics(fonts, script, text) { /* -> { advance, ascent, descent } */ },
  smallTextDimensions(fonts, text) { /* -> { width, height, ascent? } */ },
};
const svg = await renderToSvg(mdc, { glyphs, measurer });
```

`measureRun` measures a whole run of characters (kerning matters), cut into `pieces`, and returns for each piece its offset from the left of the run and its width; see `measureRunWith` in `measurers.ts` for the trick that gets this from a function giving the width of a string.

---

## Taking the pipeline apart

`renderToSvg` and `renderMdc` do three things: parse, lay out, draw. You can stop after any of them.

```js
import { drawLaidOutText, layOutText, parseMdc } from "@jsesh/core";

const text = parseMdc("nfr-f:r-A1");                                  // 1. parse: the model
const laidOut = await layOutText(text, { glyphs, cadratHeight: 40 });  // 2. lay out: the view tree
const svg = drawLaidOutText(laidOut, { cadratHeight: 40 });            // 3. draw
```

`layOutText` accepts a string or a model, and loads the shapes it needs (`layOutTextSync` does the same if they are loaded already). In a page, `layOutInPage` (from `@jsesh/elements`) is the same with the page's configuration (glyph data, style, fonts, measurer).

`laidOut` (a `LaidOutText`) holds the `text` and the `style`, and `view`, the root of a tree of `MDCView`s: one per element of the model (`view.model`), each with a position and a size, in points, **in its parent's coordinates**, y pointing down. The tree is the whole layout; nothing in it is related to the DOM or to SVG.

When the layout is given a **model** (not a string), the subviews of the root view are the views of the top items, in order. So you can find where anything is drawn:

```js
const root = laidOut.view;
const scale = 40 / laidOut.style.geometry.standardSignHeight; // points to pixels
root.subviews.forEach((_, i) => {
  const box = root.getSubViewBounds(i); // in points, in the root's inner system
  console.log(i, box.x * root.xScale * scale, box.y * root.yScale * scale, box.width * root.xScale * scale, box.height * root.yScale * scale);
});
```

This is how `@jsesh/editor` knows where to put the cursor (`itemAreas` does exactly this), and what you need for hit testing, tooltips, highlighting a word, or drawing something over the picture. `MDCView` has many more members (`width`, `height`, `position`, `xScale`, `angle`, `parent`, `next`, `previous`...); they follow the Java class, see `render/view.ts`.

A drawing procedure other than SVG would start from the same view tree: `drawSvg` (`render/svg.ts`) is a visitor over views, and is a good model to read.

---

## Working with the model

### Modifying a text, and displaying the result

Because `layOutInPage`, `layOutText` and `drawLaidOutText` accept a model as well as a string, you can edit the model and display it without going back through MdC:

```js
import { drawLaidOutText, parseMdc } from "@jsesh/core";
import { layOutInPage } from "@jsesh/elements";

const text = parseMdc("nfr-f:r");
text.removeItems(0, 1);                      // remove the first quadrat
const laidOut = await layOutInPage(text);
target.innerHTML = drawLaidOutText(laidOut, { cadratHeight: 40 });
```

To build new quadrats, the simplest is to parse a small piece of MdC and add its `topItems`: `text.addAll(parseMdc("A1").topItems.slice())`. That is also how `EditorSession` builds groups: it writes items as MdC and reads them back. Building elements by hand (`new Cadrat(...)`, `new Hieroglyph(...)`) is possible, but the constructors follow the Java ones and are less convenient than writing MdC.

### Walking and observing

Every element has `children`, `parent`, `accept(visitor)` and `deepCopy()`. To process a tree, write a visitor: extend `ModelElementAdapter` (all its methods call `visitDefault`) and override those you care about, as the layout, the drawing and the writer do.

```js
import { ModelElementAdapter, parseMdc } from "@jsesh/core";

class SignCounter extends ModelElementAdapter {
  count = 0;
  visitHieroglyph() { this.count++; }
  visitDefault(element) { element.children.forEach((c) => c.accept(this)); }
}
```

A `HieroglyphicText` is **observable**: `text.addObserver((operation) => ...)` is told of each insertion, deletion, replacement or modification (`operation.kind`, and the element and indices concerned), however deep in the tree; `deleteObserver` stops. This is what lets a view redraw only what changed; `EditorSession` does not need it (it redraws after each command), but your own tools might.

### The literal syntax tree

Between the string and the model there is a third representation, the literal **AST** (`parseMdcAst`, `MdcParser`, types in `Ast`). It records what was *written*, without interpreting it. You will rarely need it, unless you are building a tool which must treat the MdC source itself (a syntax checker, a converter...). The model is what you want for everything else.

---

## Building your own editor

`<jsesh-editor>` is one `EditorSession` plus one `EditorView`, wrapped in a Web Component. You can use the two directly.

### `EditorSession`: the editing state machine

`EditorSession` has no DOM. It holds the text (a `HieroglyphicText`), the cursor, the selection, the code being typed, and the undo and redo stacks. It follows the behaviour of JSesh's editor, at the level of quadrats: the cursor stands between top items.

Its commands are those of the element (`insertSign`, `insertMdc`, `group`, `cartouche`, `toggleRed`, `undo`, `redo`, `document`), plus:

| Member | Role |
|---|---|
| `handleKey(event)` | Give it a `KeyboardEvent`, or anything with `key`, `ctrlKey`, `metaKey`, `shiftKey`, `altKey`: it returns `true` if the key was used. Everything which types is here, including the shortcuts. |
| `typeCharacter(c)`, `acceptSeparator(s)`, `backspace()`, `delete()`, `escape()`, `selectAll()` | The pieces `handleKey` is made of, for your own keys or buttons. |
| `moveCaret(position, extend)`, `moveKey(key, extend)` | Move the cursor (and extend the selection). |
| `setOptions({ textDirection, textOrientation })` | The direction in which the text flows (the cursor needs to know). |
| `setMdc(mdc)`, `setText(model)` | Replace the whole text (undoable unless `{ undoable: false }`). `setMdc` returns `false` if the MdC can't be read. |
| `possibilities` | A function from the typed code to the list of MdC texts it may stand for, the preferred first. By default, the code itself. |
| `subscribe(listener)`, `state` | Observing (see below). |

You can use a session in Node, for tests.

**Sign completion.** In JSesh, typing `i` then a space gives the first sign with the value `i`, and another space the next, and so on. `<jsesh-editor>` sets this up. With a session of your own, give it the sign database:

```js
import { loadSignDatabase } from "@jsesh/glyphs";

const database = await loadSignDatabase("/jsesh-glyphs/");
session.possibilities = (code) => database.getPossibilityListFor(code);
```

**Observing.** `session.subscribe(listener)` calls the listener after each change and returns a function which stops it; the listener receives `{ layout }`, which is `false` when only the cursor, the selection or the typed code changed (so there is no need to lay out again). `session.state` is a snapshot, **the same object until the next change**, which is exactly what the "external store" interfaces of frameworks expect:

```js
const stop = session.subscribe(() => console.log(session.state.mdc, session.state.caret));
```

`state` holds `mdc`, `code` (being typed), `separator`, `caret`, `length`, `selection`, `selectionStart`, `selectionEnd`, `error`, `canUndo` and `canRedo`.

### `EditorView`: showing a session

`EditorView` shows a session in an element of your page (focusable: `tabindex="0"`), draws the cursor and selection over the picture, and sends the keys and the clicks to the session:

```js
import { EditorSession, EditorView } from "@jsesh/editor";

const session = new EditorSession();
const view = new EditorView(session, document.getElementById("surface"), { cadratHeight: 40 });
session.setMdc("nfr-f:r");
```

Options: `cadratHeight`, `style` (changes to the page's style), `glyphsUrl`, `onError`. `view.destroy()` stops listening. The element's content is replaced by the picture, so give the view its own element.

The surface needs a little CSS for the cursor and the selection, which the view adds as `div`s of class `caret` and `selection-box` (`EDITOR_VIEW_CSS` is the CSS used by `<jsesh-editor>`):

```css
#surface { position: relative; }
.caret, .selection-box { position: absolute; pointer-events: none; }
.caret { background: #c00; }
.selection-box { background: rgb(74 127 212 / 25%); }
```

This is what you use to build the editor into your own components: the page `custom/index.html` of the zip shows it, and [Headless parts in Vue and React](#headless-parts-in-vue-and-react) has the recipes.

### Writing the view yourself

If even `EditorView` does not suit you (a canvas, a different cursor, an overlay of your own...), the one thing to understand is how the picture is linked to the model, described in [Taking the pipeline apart](#taking-the-pipeline-apart):

- The model is the session's `text`. After each change that has `layout: true`, call `layOutInPage(session.text)` (from `@jsesh/elements`; `layOutText` in `@jsesh/core`). Given a model, it returns views **which refer to the model's elements**: the subviews of the root view are the views of the top items (quadrats, line breaks...), in the same order.
- `itemAreas(laidOut, cadratHeight)` gives the rectangle where each top item is drawn, in pixels.
- `caretArea(areas, caret, flow, cadratHeight, margin)` is where to draw the cursor, and `positionAt(areas, x, y, flow)` the cursor position closest to a click (`flow` is `{ vertical, rightToLeft }`, that is `session.isVertical` and `session.isRightToLeft`). Draw the selection from `state.selectionStart` and `state.selectionEnd`.

`EditorView` is about 100 lines on top of these functions: read it (`packages/editor/src/view.ts`) as a model.

### Keeping the element, changing its look

If you only want another appearance for `<jsesh-editor>`, you don't need any of this: it exposes CSS custom properties and `::part(surface)` (see the [user guide](user-guide.md)), and its `session` property gives access to everything else, for instance to build a toolbar around it:

```js
const editor = document.querySelector("jsesh-editor");
editor.session.setOptions({ textDirection: "RIGHT_TO_LEFT" });
button.addEventListener("click", () => editor.session.toggleRed());
```

---

## Building your own palette

`PaletteController` has all the palette's behaviour and no DOM. You give it a `SignDatabase`, show its `state`, and call its methods; `<jsesh-palette>` is just one way to draw it.

```js
import { loadSignDatabase } from "@jsesh/glyphs";
import { PaletteController } from "@jsesh/palette";

const palette = new PaletteController(await loadSignDatabase("/jsesh-glyphs/"), {
  initialFamily: "G",       // "A" by default
  // storage: ..., labels: { ... }
});

palette.subscribe(() => draw(palette.state));  // after each change
palette.onSignChosen((code) => console.log(code));
```

| `state` | |
|---|---|
| `displayed` | The codes to show (a new array whenever the list changes: easy to compare). |
| `selected`, `info` | The selected sign, and what is known of it: `values`, `tags`, `variants` (`{ code, type, typeLabel }`), `parts`, `description` (MdC), `inUserPalette`. |
| `family`, `showAllVariants` | The family selected (`""` when the signs come from a search), and whether variants are included. `palette.familyOptions` lists the families, as `{ value, label }`. |
| `tags`, `tag`, `secondaryTags`, `secondaryTag` | The tag lists, and the selection in each. |
| `containingOptions` | The parts of the displayed signs, for the "containing…" list. |
| `canGoBack` | Whether `back()` can do something. |

| Method | |
|---|---|
| `setFamily(code)`, `setShowAllVariants(bool)`, `setTag(tag)`, `setSecondaryTag(tag)` | Choose what to display. |
| `search(text)` | Signs with a value (`nfr`) or a code (`A1`, or the start of one). |
| `showVariants()`, `showContaining()` | Variants of the selected sign, or the signs containing it; each call goes one level further. |
| `filterContaining(code)` | Keep, among the displayed signs, those which contain `code`. |
| `select(code)`, `move(offset)`, `reveal(code)`, `back()` | The selection. `reveal` shows a sign which is not in the list; `move(±1, ±columns)` is for arrow keys. |
| `setInUserPalette(bool)`, `choose(code?)` | Put the sign in the user palette; choose it (calls the `onSignChosen` listeners). |

The `subscribe` / `state` pair is the interface which React's `useSyncExternalStore` expects, and is easily made into a Vue `ref`: see the next section. The page `custom/index.html` of the zip shows a palette built on the controller with a few lines of plain JavaScript.

To draw a sign in your cells, use `renderMdc(code, { cadratHeight: 24 })`, or `<jsesh-text>` with the code as content. Drawing thousands of cells at once is slow: render a cell when it scrolls into view (`<jsesh-palette>` does this with an `IntersectionObserver`).

Where the user's choices are kept is a `PaletteStorage` (`load(key)` returning an array of codes, and `save(key, codes)`); the default keeps them in `localStorage`. `labels` translates the texts, as for the element (`PaletteLabels`, `DEFAULT_PALETTE_LABELS`).

---

## Headless parts in Vue and React

`EditorSession` and `PaletteController` both have `subscribe(listener)`, which returns a function to stop listening, and a `state` which is the same object until the next change. That is all a framework needs to follow them. The recipes below are adapted from code which was run with Vue 3.5 and React 19. (For Vue, `@jsesh/vue` already contains `useExternalState` below, and ready-made components over the *elements*; see [its README](../jsesh-vuejs/README.md).)

### Vue

A small composable makes either a `ref`:

```js
// useExternalState.js
import { onScopeDispose, shallowRef } from "vue";

export function useExternalState(source) {
  const state = shallowRef(source.state);
  onScopeDispose(source.subscribe(() => (state.value = source.state)));
  return state;
}
```

The editor is an `EditorView` on a `div` of your template, created once it is mounted:

```vue
<script setup>
import { onBeforeUnmount, onMounted, ref } from "vue";
import { EditorSession, EditorView } from "@jsesh/editor";
import { useExternalState } from "./useExternalState.js";

const surface = ref(null);
const session = new EditorSession();
const state = useExternalState(session);
let view;
onMounted(() => (view = new EditorView(session, surface.value, { cadratHeight: 36 })));
onBeforeUnmount(() => view.destroy());
</script>

<template>
  <div ref="surface" tabindex="0" style="position: relative; min-height: 4rem; border: 1px solid #888; line-height: 0" />
  <button :disabled="!state.canUndo" @click="session.undo()">Undo</button>
  <code>{{ state.mdc }}</code>
</template>
```

(Add the CSS for `.caret` and `.selection-box` given [above](#editorview-showing-a-session).) The palette is a list of buttons, and you decide what it looks like:

```vue
<script setup>
import { loadSignDatabase } from "@jsesh/glyphs";
import { PaletteController } from "@jsesh/palette";
import { useExternalState } from "./useExternalState.js";

const emit = defineEmits(["chosen"]);
const palette = new PaletteController(await loadSignDatabase("/jsesh-glyphs/"), { initialFamily: "G" });
const state = useExternalState(palette);
palette.onSignChosen((code) => emit("chosen", code));
</script>

<template>
  <select :value="state.family" @change="palette.setFamily($event.target.value)">
    <option v-for="o in palette.familyOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
  </select>
  <button
    v-for="code in state.displayed.slice(0, 100)"
    :key="code"
    :class="{ selected: code === state.selected }"
    @click="palette.select(code)"
    @dblclick="palette.choose(code)"
  >
    <jsesh-text cadrat-height="24">{{ code }}</jsesh-text>
  </button>
</template>
```

(`await` at the top level of `<script setup>` makes the component async: wrap it in `<Suspense>`.)

### React

`useSyncExternalStore` is made for this:

```jsx
import { useEffect, useRef, useState, useSyncExternalStore } from "react";
import { EditorSession, EditorView } from "@jsesh/editor";
import { PaletteController } from "@jsesh/palette";

function useExternalState(source) {
  return useSyncExternalStore(
    (callback) => source.subscribe(callback),
    () => source.state,
  );
}

function Editor() {
  const surface = useRef(null);
  const [session] = useState(() => new EditorSession());
  const state = useExternalState(session);
  useEffect(() => {
    const view = new EditorView(session, surface.current, { cadratHeight: 36 });
    return () => view.destroy();
  }, [session]);
  return (
    <>
      <div ref={surface} tabIndex={0} style={{ position: "relative", minHeight: "4rem", border: "1px solid #888", lineHeight: 0 }} />
      <button disabled={!state.canUndo} onClick={() => session.undo()}>Undo</button>
      <code>{state.mdc}</code>
    </>
  );
}

function Palette({ database, onChosen }) {
  const [palette] = useState(() => new PaletteController(database, { initialFamily: "G" }));
  const state = useExternalState(palette);
  useEffect(() => palette.onSignChosen(onChosen), [palette, onChosen]);
  return (
    <>
      <select value={state.family} onChange={(e) => palette.setFamily(e.target.value)}>
        {palette.familyOptions.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
      </select>
      {state.displayed.slice(0, 100).map((code) => (
        <button key={code} className={code === state.selected ? "selected" : ""} onClick={() => palette.select(code)} onDoubleClick={() => palette.choose(code)}>
          <jsesh-text cadrat-height="24">{code}</jsesh-text>
        </button>
      ))}
    </>
  );
}
```

(React's strict mode runs effects twice in development: `EditorView.destroy()` and the function returned by `onSignChosen` make that harmless.)

To keep the editor's text in your own state, subscribe to the session and copy `session.state.mdc`; to insert a chosen sign, call `session.insertSign(code)` (keep the session in your state or in a context, shared by the editor and the palette).

### Other frameworks, and Web Components of your own

The same two calls work anywhere: `subscribe` to be told, read `state` to draw. For a Web Component (Lit, Svelte, plain), create the `EditorSession`/`PaletteController` in the element, subscribe in `connectedCallback`, stop in `disconnectedCallback`. The source of `<jsesh-editor>` and `<jsesh-palette>` is the reference: they are small, and written this way.

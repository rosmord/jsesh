# JSeshJs Programmer documentation

This is a simple documentation to help you use and integrate JSeshJs in your own project.

JSeshJs is the web version of [JSesh](https://jsesh.qenherkhopeshef.org): it displays (and lets you edit) hieroglyphic texts in your web pages, drawn exactly as JSesh draws them. It reads texts written in the *Manuel de Codage* (MdC).

## Before you start: the Manuel de Codage in one minute

You don't need to know much of MdC to follow this documentation. A text is a string where each hieroglyph is written with a **code**, and punctuation says how the signs are arranged:

| You write | You get | Meaning |
|---|---|---|
| `nfr` | the sign F35 | A sign can be written by its Gardiner code (`F35`) or by one of its phonetic values (`nfr`). |
| `i-w-r` | three signs in a row | `-` separates *quadrats* (the little squares in which signs are packed). |
| `p:t` | `p` above `t` | `:` stacks two signs vertically in one quadrat. |
| `f*t` | `f` next to `t` | `*` puts two signs side by side in one quadrat. |
| `a&b` | `a` and `b` as a ligature | `&` makes a ligature. |
| `!` | new line | |

So `i-w-r:a-C1-m-p*t:pt` is a whole phrase. The rest of the documentation simply passes such strings around: you give them to JSeshJs, and it draws them.

## What is in the box

JSeshJs is made of three small packages:

| Package | What it does | Needs a browser? |
|---|---|---|
| `@jsesh/core` | Reads and writes MdC, holds the document model, computes the layout and draws it as SVG. | No: runs in browsers *and* in Node. |
| `@jsesh/glyphs` | The sign shapes, the font, and the sign database (families, values, tags...). | No (but it loads files, see below). |
| `@jsesh/elements` | The Web Components: `<jsesh-text>`, and `autoRender()` for plain pages. | Yes. |

And what exists today:

| | Status |
|---|---|
| Displaying MdC (`<jsesh-text>`, `autoRender()`, `renderToSvg()`) | **Available** |
| Parsing and writing MdC, the document model, layout, sign database | **Available** |
| A hieroglyphic **editor** (cursor, selection, typing signs) | **Example code only** (`examples/editor/editor.js`, also in the zip): it works, but you copy it into your site and adapt it. |
| A sign **palette** | **Example code only** (`examples/palette/palette.js`), same remark. |

---

## Installation

### What do you need?

A web page showing hieroglyphs needs two things:

1. **The code**: small (about 130 kB for the whole library in one file).
2. **The glyph data**: big (38 MB in about 7,000 files), but a visitor's browser only downloads what it needs: an index of about 150 kB, the font, and the shapes of the signs which are actually displayed.

The data is kept separate from the code on purpose: it can live on another server, shared by several sites (see [Sharing the glyph data](#sharing-the-glyph-data-between-sites)).

### The two zip files

We start with one of the two zip files available:

- `jsesh-web-<NUMBER>.zip`: a full version of JSeshJs, including the font files;
- `jsesh-web-<NUMBER>-code-only.zip`: a version of JSeshJs without the font files. If you have multiple projects, it can allow you to save some space on your server by using `jsesh-web-<NUMBER>.zip` to provide the font files, and `jsesh-web-<NUMBER>-code-only.zip` for the other projects.

Both unzip into a `jsesh-web/` folder:

```text
jsesh-web/
├── index.html          the simplest possible page: view its source!
├── examples.css        styles for the examples only
├── README.md           a short guide for the recipient of the zip
├── jsesh/
│   ├── jsesh.js        the library, in ONE file
│   └── data/           the glyph data (full zip only)
├── editor/             a hieroglyphic editor (example, to copy and adapt)
└── palette/            a sign palette (example, to copy and adapt)
```

#### Testing it

- If you have put the files on a web server, simply open the folder containing `index.html` in a browser should work;
- on your own computer, as browsers refuse to load data from the local file system, double-clicking `index.html` won't work. You need a small server. If you have python, you can use`cd jsesh-web && python3 -m http.server`, then open <http://localhost:8000/>. With php, `cd jsesh-web && php -S localhost:8000` works too, and in Node, `cd jsesh-web && npx serve` (install `serve` first with `npm install -g serve`).

### Configuring JSeshJs from scratch

In this part, we suppose you include your javascript code the old-fashioned way, with `<script>` tags. For modern framework, see the next section.

**Step 1: put the files on your site.** Copy the `jsesh/` folder (library and data) to your server, for instance so that it is available as `/jsesh/jsesh.js` and `/jsesh/data/`.

**Step 2: load the library and tell where the data is.** `jsesh.js` is a JavaScript *module*, hence you need to add`type="module"` to your `<script>` tag:

```html
<script type="module">
  import { configureJSesh } from "/jsesh/jsesh.js"; // also defines <jsesh-text>
  configureJSesh({ glyphsUrl: "/jsesh/data/" });     // once, before the first text is displayed
</script>
```

**Step 3: write hieroglyphs.**

```html
<p>The word <jsesh-text>nfr-f:r</jsesh-text> means "good".</p>
```

That is all. A complete page:

```html
<!doctype html>
<html lang="en">
  <head><meta charset="utf-8" /><title>My first hieroglyphs</title></head>
  <body>
    <p>The word <jsesh-text>nfr-f:r</jsesh-text> means "good".</p>

    <script type="module">
      import { configureJSesh } from "/jsesh/jsesh.js";
      configureJSesh({ glyphsUrl: "/jsesh/data/" });
    </script>
  </body>
</html>
```

A few remarks:

- **Modules and "classic" scripts.** A plain `<script>` (without `type="module"`) cannot use `import`. If you must stay with classic scripts, load the library with a dynamic import: `const jsesh = await import("/jsesh/jsesh.js")` (inside an `async` function, or in a module).
- **Relative URLs** work too (`"./jsesh/data/"`), relative to the page.
- **Bare names.** Examples elsewhere in this documentation write `from "@jsesh/elements"`. A browser cannot resolve such a *bare name* by itself: it only understands URLs. In a page without a bundler, replace it with `"/jsesh/jsesh.js"` (which exports everything from the three packages), or declare an [import map](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script/type/importmap) before your scripts, as the `editor/` page of the zip does:

  ```html
  <script type="importmap">
    { "imports": {
        "@jsesh/core": "/jsesh/jsesh.js",
        "@jsesh/glyphs": "/jsesh/jsesh.js",
        "@jsesh/elements": "/jsesh/jsesh.js"
    } }
  </script>
  ```

## Using JSeshJs to render hieroglyphic text

### The `<jsesh-text>` element

Once you have set up JSeshJs, for your page with the javascript `<script>` from **step 3** above, you can write hieroglyphs in your page with `<jsesh-text>`:

```html
<p>The word <jsesh-text>nfr-f:r</jsesh-text> means "good".</p>
<jsesh-text cadrat-height="40" direction="rtl">i-w-r:a-C1-m-p*t:pt</jsesh-text>
```

The element is `inline-block`, so it sits in a line of text like a word, and can also be styled like any other element. Its text comes from its `mdc` attribute (or property) or, if there is none, from its content. If you change either one, the picture is redrawn.

The text can even be a whole JSesh document (the content of a `.gly` file): the preferences stored in its header (direction, justification...) are then used.

#### Attributes

| Attribute | Meaning |
|---|---|
| `mdc` | The text. Overrides the element's content. |
| `cadrat-height` | Height of a quadrat, in pixels. By default, 1.5 times the font size of the surrounding text, so hieroglyphs scale with your text. |
| `direction` | `ltr` or `rtl`. |
| `orientation` | `horizontal` or `vertical` (columns). |
| `centered` | (no value) center small signs in their quadrat. |
| `justified` | (no value) justify lines. |
| `glyphs-url` | Glyph data for this text only (see [Sharing the glyph data](#sharing-the-glyph-data-between-sites)). |

#### Changing the style: `jseshStyle`

For everything else (colours, line widths, fonts...), set the `jseshStyle` property to an object containing only the values you want to change. It has four optional parts: `geometry`, `painting`, `fonts`, `options` (see `JSeshStyle` in `@jsesh/core`):

```js
const el = document.querySelector("jsesh-text");
el.jseshStyle = { painting: { blackColor: "#223366", redColor: "#aa0000" } };
```

To change the style of **all** the texts of the page, pass `style` to `configureJSesh`:

```js
configureJSesh({ glyphsUrl: "/jsesh/data/", style: { painting: { blackColor: "#223366" } } });
```

#### Events

| Event | When |
|---|---|
| `jsesh-render` | The text has been drawn. |
| `jsesh-error` | It could not be drawn (invalid MdC, data not found...). The error is in `event.detail`. |

On error, the element does not throw: it shows its own content as plain text instead. This is what makes live previews possible (see [Editing](#using-jseshjs-to-edit-hieroglyphic-text)).
You can use an *even listener* to catch the events if you want. You will find examples below.

### Without custom elements: `autoRender()`

`<jsesh-text>` is not standard html, and it can confuse some softwares. If you don't want to use it, Manuel de codage text can be put in standard `html` elements.
You can use `autoRender()` to turn any element into a hieroglyphic text. It looks for elements of **class** `jsesh` and replaces their content with the picture:

```html
<span class="mdc">i-w-r:a-C1-m-p*t:pt</span>
<script type="module">
  import { autoRender } from "/jsesh/jsesh.js";
  autoRender({ observe: true }); // observe: also process elements added later
</script>
```

Options: `className` (another class: `autoRender({ className: "hiero" })`), `selector` (any CSS selector) and `observe`.

### Getting an SVG string: `renderMdc()`

For full control, `renderMdc` produces a string representation of the text as a `<svg>` element:

```js
import { renderMdc } from "/jsesh/jsesh.js";

const svg = await renderMdc("nfr-f:r", { cadratHeight: 40 });
document.getElementById("target").innerHTML = svg;
```

`renderMdc` takes the same `style` and `glyphsUrl` options as the element. It is `async` because it may have to load sign shapes.

### On a server, or in a script

The same rendering works in Node, with no browser: use `@jsesh/core` and `@jsesh/glyphs`, and read the data from the disk.

```ts
import { readFileSync } from "node:fs";
import { ApproximateTextMeasurer, renderToSvg } from "@jsesh/core";
import { GlyphIndexProvider } from "@jsesh/glyphs";

const data = "node_modules/@jsesh/glyphs/data";
const glyphs = new GlyphIndexProvider(JSON.parse(readFileSync(`${data}/glyph-index.json`, "utf-8")), async (file) =>
  JSON.parse(readFileSync(`${data}/glyphs/${file}.json`, "utf-8")),
);
const svg = await renderToSvg("i-w-r:a-C1-m-p*t:pt", { glyphs, measurer: new ApproximateTextMeasurer(), cadratHeight: 30 });
```

Without a canvas, Latin (alphabetic) text is measured approximately; hieroglyphs are not affected.

(`jsesh.js`, the one-file bundle, is meant for browsers, since it defines a custom element. In Node, use the packages.)

### How it works (a mental model)

It helps to know the three steps behind every picture:

```mermaid
flowchart LR
    A("MdC string") -->|parse| B("model<br/>(HieroglyphicText)")
    B -->|layout| C("view tree")
    C -->|draw| D("SVG")

    classDef input fill:#fde8c8,stroke:#d9923b,stroke-width:2px,color:#5a3a0a
    classDef model fill:#d6e8f7,stroke:#4a90c8,stroke-width:2px,color:#12385c
    classDef view fill:#e0f0d8,stroke:#6aa84f,stroke-width:2px,color:#244a14
    classDef output fill:#eadcf5,stroke:#8e63b8,stroke-width:2px,color:#3a1f57

    class A input
    class B model
    class C view
    class D output
```

1. **Parse**: the string becomes a *model*, a tree of quadrats and signs (see [Manipulating Manuel de Codage](#manipulating-manuel-de-codage-with-jseshjs)).
2. **Layout**: JSesh's algorithm decides where each sign goes, using the sizes found in `glyph-index.json`. The result is a tree of *views*, with positions, in points.
3. **Draw**: the views are turned into SVG, loading the shape of each sign.

`<jsesh-text>` and `renderMdc` do the three steps for you. If you need a step on its own, the functions are `parseMdc`, `layOutInPage` / `layOutText`, and `drawLaidOutText`.

---

## Using JSeshJs to edit hieroglyphic text

"Editing" can mean two quite different things. Pick the one you need.

### Option 1: an ordinary text field with a live preview

The user types MdC as plain text (`nfr-f:r`), and the field's value *is* the MdC. This is the simplest solution, and perfectly enough for a search box or a comment form. Since `<jsesh-text>` does not throw on invalid text (a partial one, while typing), it can be updated at each keystroke:

```html
<input id="query" size="40" placeholder="MdC, for instance nfr-f:r" />
<jsesh-text id="preview" cadrat-height="40"></jsesh-text>

<script type="module">
  import { configureJSesh } from "/jsesh/jsesh.js";
  configureJSesh({ glyphsUrl: "/jsesh/data/" });

  const query = document.getElementById("query");
  const preview = document.getElementById("preview");
  preview.addEventListener("jsesh-error", () => preview.classList.add("invalid"));
  preview.addEventListener("jsesh-render", () => preview.classList.remove("invalid"));
  query.addEventListener("input", () => (preview.mdc = query.value)); // query.value is the MdC to use
</script>
```

Add a `.invalid` rule in your CSS (a red outline, say) to tell the user that the text is not valid yet.

### Option 2: a real hieroglyphic editor

Here the user clicks in a box showing the hieroglyphs, with a blinking cursor, and types sign codes; there are selection, undo and redo. This exists as `examples/editor/editor.js` (also in the zip, in `editor/`), a class `HieroglyphicEditor`, but it is **not part of the npm packages**: copy the file into your site (and `palette/palette.js` if you want a sign palette). It only uses the public API of `@jsesh/core` and `@jsesh/elements`, so it works there unchanged. Read `editor/index.html` in the zip for a complete page.

Below is an (almost) minimal example with an editor. I will probably provide something more convenient a bit later:

```html
<!doctype html>
<html>
  <head>
<style>
  #jsedit {
    position: relative; /* needed for caret positioning*/
    min-height: 6rem;
    overflow: auto;
    outline: 1px solid #aaa;
  }
  #jsedit:focus { outline-color: #4a7fd4; }
  #jsedit svg { display: block; }

  /* caret and selection display */
  .caret, .selection-box { position: absolute; pointer-events: none; }
  .caret { background: #c00; }
  .selection-box { background: rgb(74 127 212 / 25%); }

  /* optional: caret blinking */
  #jsedit:focus .caret { animation: blink 1s step-end infinite; }
  #jsedit:not(:focus) .caret { background: #999; }
  @keyframes blink { 50% { opacity: 0; } }
</style>
  </head>
  <body>
    <script type="importmap">
      /*
        Needed as editor.js uses modern js imports.
        Tells where the libraries are. 
      */
      { "imports": { "@jsesh/core": "./jsesh/jsesh.js",
                     "@jsesh/glyphs": "./jsesh/jsesh.js",
                     "@jsesh/elements": "./jsesh/jsesh.js" } }
    </script>

    <div>The editor itself, in a minimal configuration.</div>


    <div tabindex="0" id="jsedit"></div>

    <div>
      <button id="linebreak" title="New line (!)">New line</button>
    </div>

    <script type="module">
      /* 'module' is necessary because editor.js uses import.  */
      import { configureJSesh } from "./jsesh/jsesh.js";
      import { HieroglyphicEditor } from "./editor/editor.js";

      configureJSesh({ glyphsUrl: "./jsesh/data/" });
      const editor = new HieroglyphicEditor(document.getElementById("jsedit"));
      editor.setMdc("nfr-f:r-!nfr-f:r-!nfr-f:r-!");

      // Insert a line break (-!) at the caret, then give the focus back to the editor.
      document.getElementById("linebreak").addEventListener("click", () => {
        editor.insertMdc("!");
        editor.focus();
      });
    </script>
  </body>
</html>
```

The editor is materialized in an element of the page, here in a `<div>`with `id="jsedit"` (you can obviously choose the id you want). A very important point is that it needs to receive keyboard input. This can be done by making the element focusable, using `tabindex="0"`. You can call `editor.focus()` when you want it to receive the keyboard.

#### Typing, as in JSesh

The editor types like JSesh does: you type a **code**, then a **separator** which says how it connects to the next sign.

| Key | Effect |
|---|---|
| a code, then <kbd>-</kbd>, <kbd>Space</kbd> or <kbd>Enter</kbd> | The sign goes into a new quadrat. |
| a code, then <kbd>:</kbd>, <kbd>*</kbd> or <kbd>&</kbd> | The sign is grouped with the next one (stacked, side by side, ligature). So <kbd>p</kbd> <kbd>:</kbd> <kbd>t</kbd> <kbd>Space</kbd> gives `p` above `t`. |
| <kbd>!</kbd> | New line. |
| <kbd>Backspace</kbd>, <kbd>Delete</kbd> | Erase. |
| <kbd>Esc</kbd> | Forget the code being typed. |
| arrows, <kbd>Home</kbd>, <kbd>End</kbd> | Move the cursor; with <kbd>Shift</kbd> (or shift-click), select. |
| <kbd>Ctrl</kbd>/<kbd>⌘</kbd>+<kbd>Z</kbd> | Undo; with <kbd>Shift</kbd>, redo. <kbd>Ctrl</kbd>/<kbd>⌘</kbd>+<kbd>A</kbd>: select all. |

#### What you can call

| Member | Role |
|---|---|
| `new HieroglyphicEditor(element, { onChange })` | Create the editor on a focusable element. |
| `setMdc(mdc)` | Replace the whole text. Returns `false` if the MdC can't be read. |
| `state` | `{ mdc, code, separator, caret, length, selection, error }`: what to show around the editor. |
| `insertSign(code)` | Insert a sign at the cursor, as if typed. |
| `insertMdc(mdc)` | Insert MdC text at the cursor (replacing the selection). |
| `group(separator)`, `cartouche()`, `toggleRed()` | Operate on the selection (or on what is before the cursor). |
| `undo()`, `redo()` | |
| `document()` | The text as a JSesh document (the content of a `.gly` file, with header). |
| `cadratHeight`, `setOptions({ textDirection, textOrientation })` | Display settings. |
| `focus()` | Give the keyboard to the editor. |

#### Adapting it, or writing your own

The example is deliberately short and plain JavaScript, so that you can change it. If you want to write your own editing component, the one thing to understand is how the picture is linked to the model:

- You keep a `HieroglyphicText` (the model) and change it.
- After each change, call `layOutInPage(model)` (from `@jsesh/elements`; `layOutText` in `@jsesh/core`). Given a model instead of a string, it returns views **which refer to the model's elements**: the subviews of the root view are the views of the top items (quadrats, line breaks...), in the same order.
- `view.getSubViewBounds(i)` gives the rectangle where top item *i* is drawn, in points. Multiply by the scale (`cadratHeight` divided by `style.geometry.standardSignHeight`) to get pixels.
- That is enough to place a cursor between two items, to draw a selection, and to find which item a click hit. `editor.js` does exactly this (`drawCaret`, `positionAt`).

### Example: finding a typed text

JSesh's search (`jseshSearch`) is not ported. Still, MdC is easy to compare once it is *normalized*: `a`, `nfr` or `F35` all denote the same sign, and the same sign can be spelled in several ways in the texts. The idea: parse the query and the texts, reduce them to the lists of their canonical sign codes, and look for the former in the latter.

```js
import { Hieroglyph, parseMdc } from "@jsesh/core";
import { loadGlyphProvider } from "@jsesh/glyphs";

const glyphs = await loadGlyphProvider("/jsesh-glyphs/"); // same URL as for configureJSesh

/// The canonical (Gardiner) codes of the signs in an MdC text, in reading order.
function signs(mdc) {
  const result = [];
  const visit = (element) => {
    if (element instanceof Hieroglyph) result.push(glyphs.canonicalCode(element.code));
    else element.children.forEach(visit);
  };
  visit(parseMdc(mdc)); // throws if the MdC is invalid
  return result;
}

/// Is `needle` a run of consecutive signs of `haystack`?
function contains(haystack, needle) {
  if (needle.length === 0) return false;
  for (let i = 0; i + needle.length <= haystack.length; i++) {
    if (needle.every((code, j) => haystack[i + j] === code)) return true;
  }
  return false;
}

const corpus = [{ id: 1, mdc: "i-w-r:a-C1-m-p*t:pt" }, { id: 2, mdc: "nfr-f:r-A1" }];
const entries = corpus.map(({ id, mdc }) => ({ id, mdc, signs: signs(mdc) })); // do it once

query.addEventListener("input", () => {
  try {
    const wanted = signs(query.value);
    const found = entries.filter((entry) => contains(entry.signs, wanted));
    // show `found`, each entry with <jsesh-text>
  } catch {
    // the query is not valid MdC (yet)
  }
});
```

Typing `nfr` finds entry 2 (`nfr` is `F35`). Grouping (`:`, `*`) is ignored: this searches for signs in sequence, wherever they are in the quadrats. If quadrats should matter, compare the normalized MdC instead (next section).

---

## Using the Hieroglyphic Palette to Select and Navigate Signs

### Why a palette?

With about 7,000 signs, nobody knows every code by heart. A user who wants a sign usually knows *something* about it, but not always its code:

- its **pronunciation** (`nfr`, `m`);
- its **family** (birds, gods, buildings...);
- its **shape**: "it looks like A1, but with something added";
- or only that it is **a variant** of another sign.

A plain text field handles only the first case (and only if you already know the value). Egyptologists typing hieroglyphs on the web have often had to leave the page to look signs up in a book or another program. JSesh's own **palette** solves this inside the application, and JSeshJs offers the same tool for the web: you look at the signs, narrow down, and pick.

> Like the editor, the palette is **example code** for now (`examples/palette/palette.js`, also in the zip, in
> `palette/`): you copy it into your site and adapt it. The planned `<jsesh-palette>` element does not exist yet.

### What the user can do

The palette shows a grid of signs, each drawn with the JSesh font, and a panel describing the selected sign.

#### Finding signs

| Control | What it does |
|---|---|
| **Family** list | The standard Gardiner families (`A. Man and his occupations`, `G. Birds`... as in Gardiner's list), plus *Latest signs* (those you chose recently), *User palette* (those you put in "my palette"), *All signs*, and three shape-based lists: *Tall narrow*, *Low broad* and *Low narrow* signs. |
| **Show all variants** | By default, a family shows its main signs; tick this box to also see the variants. |
| **Tag** and **secondary tag** lists | Narrow a family down. Signs carry tags describing them (A1, for instance, is tagged `seated` and `holding nothing`); the first list shows the tags of the family, the second the other tags of the signs which remain. |
| **Search** box | Type a *value* (`nfr`, `m`) or a *code* (`A1`, `G17`, or the start of a code) and press Enter. |
| **Containing…** list | Among the signs displayed, keep those which contain a given sign as a part. |

#### Navigating from one sign to another

This is what makes the palette more than a list: it follows the *relations* between signs.

- Click a sign: the panel shows its **values**, **tags**, **description**, its **variants** and the signs it **contains**. Each variant and each part is itself a small button: click it to jump there.
- **Variants** button: shows the variants of the selected sign. Click again: the variants *of those variants*, and so on. (This is how you find a sign which is only "similar" to the one you know.)
- **Containing** button: shows the signs which contain the selected sign (ligatures, composed signs...). Click again to go one level further.
- **Back** button: returns to the previous sign (the last 6 are remembered).
- **In my palette** box: puts the sign in the user palette, for signs you use all the time.

#### Choosing

Double-click a sign, press Enter (or Space) on it, or click **Choose**. The palette then sends a `sign-chosen` event; the code of the sign is in `event.detail`. The sign also goes into *Latest signs*.

**Keyboard.** The grid can be used without a mouse: Tab enters the grid, the arrows move from sign to sign, Enter chooses.

### Using it in a page

The palette needs the sign database (`sign-info.json`, see [The sign database](#the-sign-database)), which is loaded separately from the shapes because only palettes need it:

```html
<div id="palette"></div>

<script type="module">
  import { configureJSesh } from "@jsesh/elements";
  import { loadSignDatabase } from "@jsesh/glyphs";
  import { HieroglyphPalette } from "./palette.js"; // your copy of the example
  // Ready-made styles; the zip has them as palette/palette.css:
  //   <link rel="stylesheet" href="./palette.css" />

  configureJSesh({ glyphsUrl: "/jsesh-glyphs/" });
  const palette = new HieroglyphPalette(
    document.getElementById("palette"),     // its content is replaced by the palette
    await loadSignDatabase("/jsesh-glyphs/"), // same folder as for configureJSesh
  );

  palette.addEventListener("sign-chosen", (e) => {
    console.log("The user chose", e.detail); // for instance "A1"
  });
</script>
```

That is the whole interface: *create it with an element and a database; listen to `sign-chosen`*. What you do with the code is up to your page.

### Connecting the palette to an editor, or to a text field

**With the example editor**, a chosen sign is inserted at the cursor, exactly as if the user had typed its code followed by a space:

```js
palette.addEventListener("sign-chosen", (e) => {
  editor.insertSign(e.detail);
  editor.focus(); // give the keyboard back to the editor, so that typing goes on
});
```

Because `insertSign` follows JSesh's typing rules, this combines well with the keyboard. If the user types `p` then `:`, and *then* picks `t` in the palette, the two signs are stacked: the palette obeys the separator which was typed last. The page `editor/index.html` in the zip shows the palette in a side panel which can be hidden.

**With a text field** (the first editing option above), append the code to the field and refresh the preview:

```js
palette.addEventListener("sign-chosen", (e) => {
  query.value += (query.value === "" ? "" : "-") + e.detail; // "-": a new quadrat
  query.dispatchEvent(new Event("input"));                    // redraw the preview
});
```

`palette/index.html` is a minimal page of the same kind: chosen signs are joined with `-` and shown with `<jsesh-text>`.

### What it costs, and how it stays fast

Showing "All signs" means thousands of cells, but the palette only draws what is on screen: a sign is rendered (with `renderMdc`) when its cell scrolls into view, using an `IntersectionObserver`. Shapes are fetched by the usual rule, only when needed, so the palette downloads little more than `sign-info.json` (130 kB compressed) and the shapes visitors actually look at.

### Remembering the user's choices

The *Latest signs* and *User palette* lists are kept in the browser's `localStorage` (keys `jsesh-palette-last` and `jsesh-palette-user`). They belong to one browser on one site: they are not shared between devices. If you want them saved in your own database, listen to `sign-chosen` and keep your own list. If `localStorage` is not available (private mode...), the palette still works but forgets.

### Customizing

- **Appearance.** All the palette's markup carries `data-palette="..."` attributes, and `palette.css` only styles those and a few classes (`.sign`, `.selected`, `.code-link`...). Copy `palette.css` and edit it; the example editor does this to turn the palette into a narrow side panel.
- **Layout and controls.** The controls are described by one HTML string (`PALETTE_HTML`) at the top of `palette.js`. Remove the controls you don't want (the tag lists, say), or translate the labels.
- **Behaviour.** Each action is a small method (`selectVariants`, `selectContaining`, `selectFromTransliterationOrCode`...) built on the `SignDatabase` (`getFamilies`, `getCodesForFamily`, `getVariants`, `getSignsContaining`, `getPossibilityFor`...). You can reuse these queries to build a palette that looks nothing like this one, for instance a simple "find a sign by value" box in a form.

### Where the knowledge comes from

The families, values, tags, variants and descriptions are those of JSesh's own sign database, exported from the Java application, so the web palette and the desktop one agree. Everything is read-only here: the palette does not edit the database (JSesh's separate *sign information editor* does that).

---

## Manipulating Manuel de Codage with JSeshJs

Everything in this section is in `@jsesh/core`, so it can run both in a browser and in Node.

### Reading and writing

```js
import { parseMdc, toMdc } from "@jsesh/core";

const text = parseMdc("nfr-f:r");   // a HieroglyphicText
toMdc(text);                         // "nfr-f:r" again
```

`parseMdc` returns the **model**; `toMdc` writes a model back as MdC, in JSesh's own dialect (so the output may be spelled slightly differently from the input, but means the same).

### Invalid text

`parseMdc` throws a `MdcSyntaxError` when the text is not valid. The error tells you where:

```js
import { MdcSyntaxError, parseMdc } from "@jsesh/core";

try {
  parseMdc("nfr-(");
} catch (e) {
  if (e instanceof MdcSyntaxError) console.log(e.message, e.line, e.charPos, e.token); // line is 0-based
}
```

### Whole documents (`.gly` files)

A JSesh file is MdC text preceded by an optional header holding the preferences (direction, orientation, fonts...). Two functions handle both parts at once:

```js
import { readMdcDocument, writeMdcDocument } from "@jsesh/core";

const doc = readMdcDocument(fileContent); // { preferences, text }
doc.preferences;                          // what the header said (or the defaults)
writeMdcDocument(doc);                    // header + MdC, ready to be saved as a .gly file
```

By default, `readMdcDocument` is *forgiving*, like JSesh: lines which cannot be read are replaced by an error message followed by the source line. Use `{ failFast: true }` to get the exception instead.

### The document model

The model is a tree. You walk it with `children` (and `parent`):

```
HieroglyphicText                ← the whole text
 └─ top items:  Cadrat (a quadrat), LineBreak, PageBreak, Cartouche...
      └─ Cadrat → HBox(es) → Hieroglyph (one sign), Ligature, ...
```

The most useful pieces:

| | |
|---|---|
| `HieroglyphicText` | `topItems`, `getTopItemAt(i)`, `addTopItem()`, `insertItemsAt()`, `removeItems(start, end)`, `deepCopy()`, `getCodes()`. |
| `Hieroglyph` | `code` (as written: `"nfr"`, `"A1"`...) and its modifiers. |
| any element | `children`, `numberOfChildren`, `parent`, `accept(visitor)`, `deepCopy()`. |

For instance, to list the sign codes of a text:

```js
import { parseMdc } from "@jsesh/core";

parseMdc("i-w-r:a-C1-m-p*t:pt").getCodes(); // the codes, in order of first appearance
```

or to walk the whole tree yourself (as the search example above does): `if (element instanceof Hieroglyph) ... else element.children.forEach(visit)`.

The model is **observable**: `text.addObserver((operation) => ...)` is told of each insertion, deletion or modification. This is what lets an editor redraw only what is needed.

### Modifying a text, and displaying the result

Because `layOutInPage` and `drawLaidOutText` accept a model as well as a string, you can edit the model and display it without going back through MdC:

```js
import { Hieroglyph, Cadrat, parseMdc, drawLaidOutText } from "@jsesh/core";
import { layOutInPage } from "@jsesh/elements";

const text = parseMdc("nfr-f:r");
text.removeItems(0, 1);                      // remove the first quadrat
const laidOut = await layOutInPage(text);
target.innerHTML = drawLaidOutText(laidOut, { cadratHeight: 40 });
```

(To build new quadrats, the simplest is to parse a small piece of MdC and add its `topItems`: `text.addAll(parseMdc("A1").topItems.slice())`. That is also how the example editor builds groups: it writes items as MdC and reads them back.)

### Normalizing: one spelling for one sign

The same sign has several names: the Gardiner code `F35`, the phonetic value `nfr`. `toMdc` can replace each by a canonical form, using the sign database:

```js
import { loadGlyphProvider } from "@jsesh/glyphs";

const glyphs = await loadGlyphProvider("/jsesh-glyphs/");
toMdc(parseMdc("nfr-f:r"), { normalize: (code) => glyphs.canonicalCode(code) });
```

The result is one canonical string per text: ideal to index in a database, or to compare with `===` or `includes`. `canonicalCode` only needs `glyph-index.json`, not the shapes, so it also works cheaply in Node.

Another option of `toMdc` is to write a part of the text only: `toMdc(text, { start: 2, end: 5 })` writes the top items 2 to 4.

### The sign database

Palettes and sign searches need more than shapes: families, phonetic values, tags, variants. `sign-info.json` holds all this, and `loadSignDatabase(url)` reads it as a `SignDatabase`, whose methods are those of JSesh's `HieroglyphDatabase`:

```js
import { loadSignDatabase } from "@jsesh/glyphs";

const db = await loadSignDatabase("/jsesh-glyphs/"); // the data folder, as for configureJSesh
db.getFamilies();
db.getValuesFor("F35");           // phonetic values
db.getTagsForSign("F35");
db.getVariants("F35");
db.getSignsContaining("F35");
db.getDescriptionFor("F35");
db.getPossibilityFor("nfr");      // signs which can be written with this value
```

`examples/palette/palette.js` is a complete use of this database.

### Lower level: the literal syntax tree

Between the string and the model there is a third representation, the literal **AST** (`parseMdcAst`, `MdcParser`, types in `Ast`). It records what was *written*, without interpreting it. You will rarely need it, unless you are building a tool which must treat the MdC source itself (a syntax checker, a converter...). The model is what you want for everything else.

---

## The glyph data

`@jsesh/glyphs` reads a folder containing:

| File | Size | Role |
|---|---|---|
| `glyph-index.json` | 540 kB (150 kB compressed) | The size and ligature zones of each of the ~7,000 signs, the phonetic codes and the predefined ligatures. Loaded once: with it alone, texts can be laid out. |
| `glyphs/*.json` | one file per sign | The shape of a sign, loaded only when it is displayed. |
| `fonts/CharisSIL-R.woff` | | The transliteration font (Charis SIL). |
| `sign-info.json` | 700 kB (130 kB compressed) | The sign database, for palettes and searches only. |

You can serve it yourself, share it between sites (`glyphsUrl`), or use the copy inside the package. To use another font, implement `GlyphProvider` (in `@jsesh/core`).


## Advanced points

### Sharing the glyph data between sites

Note that the simplest way to use JSeshJs is to use the full zip, which contains the font files. It requires less setup and fewer headaches. However, the fonts are large, and if you run several sites, you may want to host the font files once and point all your sites to the same data.

`glyphsUrl` can be an absolute URL. If you run several sites, host `jsesh/data/` once (on any static server or CDN), install the **code-only** zip on each site, and point them all to the same data:

```js
configureJSesh({ glyphsUrl: "https://static.example.org/jsesh-glyphs/0.1.0/" });
```

The server hosting the data must be set up properly:

- **CORS.** The data is read with `fetch` and the font is loaded as a web font, and browsers apply CORS to both. The host must answer with `Access-Control-Allow-Origin: *` (or the list of your sites). Without it, nothing is displayed; the browser console shows the blocked request. *This is the most common problem.*
- **Content types and compression**: `.json` as `application/json`, `.woff` as `font/woff`; the JSON files should be served with gzip or brotli (the `.woff` font is already compressed).
- **Caching**: put the data in a versioned folder (`.../0.1.0/`) with a long `Cache-Control`, so that returning visitors download nothing.

A single `<jsesh-text>` can also read its own data with its `glyphs-url` attribute.

#### Example: an `.htaccess` file for Apache

If the data is hosted on an Apache server (the most common case on shared hosting), the three requirements above can be met with a single small file, called `.htaccess`.

**Where does it go?** In the folder which holds the data, that is the one containing `glyph-index.json`, `sign-info.json`, `fonts/` and `glyphs/`. A `.htaccess` file applies to its own folder and to all the folders below it, so one file at the top covers every file of the data. For the example URL above, with the data served from `https://static.example.org/jsesh-glyphs/0.1.0/`:

```text
(web root of static.example.org)
└── jsesh-glyphs/
    └── 0.1.0/
        ├── .htaccess          ← here
        ├── glyph-index.json
        ├── sign-info.json
        ├── fonts/
        └── glyphs/
```

Note the leading dot in the name: `.htaccess` is a "hidden" file, which some FTP clients and file managers do not show by default, and which `unzip` or a drag-and-drop may leave behind. Check that it was really uploaded.

**What does it contain?**

```apache
# --- 1. CORS: let pages of other sites read these files -----------------------
# REQUIRED if pages on another site use these files; useless if same site.
<IfModule mod_headers.c>
  Header set Access-Control-Allow-Origin "*"
</IfModule>

# --- 2. Content types ----------------------------------------------------------
# Optional safety net: Apache usually knows these types already.
<IfModule mod_mime.c>
  AddType application/json .json
  AddType font/woff .woff
</IfModule>

# --- 3. Compression (the JSON files are text: they shrink a lot) --------------
# Optional, for performance only.
<IfModule mod_deflate.c>
  AddOutputFilterByType DEFLATE application/json
</IfModule>

# --- 4. Caching: the folder is versioned, so its files never change -----------
# Optional, for performance only. Remove if you overwrite files under the same URL.
<IfModule mod_headers.c>
  Header set Cache-Control "public, max-age=31536000, immutable"
</IfModule>
```

Block by block:

1. **CORS.** `Access-Control-Allow-Origin: *` tells browsers that a page from *any* site may read these files. The data is public and read-only, so this is harmless. If you prefer to name your sites, replace the first block with:

   ```apache
   <IfModule mod_headers.c>
     SetEnvIf Origin "^https://(www\.)?(site-one\.org|site-two\.org)$" ALLOWED_ORIGIN=$0
     Header set Access-Control-Allow-Origin "%{ALLOWED_ORIGIN}e" env=ALLOWED_ORIGIN
     Header append Vary Origin
   </IfModule>
   ```

   (`Vary: Origin` stops caches from giving one site's answer to another.)
2. **Content types.** Many servers already know `.json`, but older ones do not know `.woff`. Declaring both is harmless.
3. **Compression.** The JSON files are large text files, which compress well: `glyph-index.json` goes from 540 kB to about 150 kB. Do not compress the `.woff` font, it already is. If your server has `mod_brotli`, you can use it instead (same line, with `BROTLI` in place of `DEFLATE`).
4. **Caching.** Visitors keep the files for a year and do not even ask the server again. This is safe *only if you change the folder name for each version* (`0.1.0/`, then `0.2.0/`...), because browsers will never look at a file they consider fresh. Never overwrite the data of a published version.

The `<IfModule ...>` wrappers make the file harmless when a module is missing: the corresponding block is simply ignored instead of causing an error. The flip side is that a missing module fails *silently*, so test (below).

**It does not seem to work?**

- *Test it from a terminal*, simulating a page of another site:

  ```bash
  curl -sI -H "Origin: https://example.com" -H "Accept-Encoding: gzip" \
    https://static.example.org/jsesh-glyphs/0.1.0/glyph-index.json
  ```

  In the answer you want `Access-Control-Allow-Origin: *`, `Content-Type: application/json`, `Content-Encoding: gzip` and the `Cache-Control` line. Whichever is missing tells you which block does not apply.
- *"500 Internal Server Error" on every file*: the server refuses a directive of the file. With a main configuration which does not allow `.htaccess` to set headers, types or expiry, Apache answers 500. Ask your host to enable `AllowOverride FileInfo` for this folder, or, if you manage the server, put the same directives in the virtual host: inside a `<Directory "/path/to/jsesh-glyphs">` block, in which case no `.htaccess` is needed.
- *Headers missing, but no error*: `mod_headers` (CORS and caching), `mod_deflate` (compression) may not be enabled on the server (`a2enmod headers deflate` on Debian/Ubuntu, then reload Apache).
- *A CDN or proxy in front of the server* may add or strip headers: test the public URL, not the origin server.
- *Nginx or another server*: the same four things are needed (CORS header, content types, compression, caching header), but in that server's own configuration language; `.htaccess` is an Apache feature only.



### Configuring JSeshJs in a modern framework

We will suppose you use `npm` and `vite` here. The same ideas apply to other bundlers.

> The packages are **not published on npm yet**. Until then, you install them from tarballs built from the sources.

**Step 1: build the tarballs.** In the `jsesh-js` folder of the JSesh sources:

```bash
npm install && npm run build
for p in core glyphs elements; do (cd packages/$p && npm pack --pack-destination /somewhere); done
```

You get `jsesh-core-0.1.0.tgz`, `jsesh-glyphs-0.1.0.tgz` (10 MB: it contains the glyph data) and `jsesh-elements-0.1.0.tgz`.

**Step 2: install them in your project**, all three in the same command (they depend on each other):

```bash
npm install /somewhere/jsesh-core-0.1.0.tgz /somewhere/jsesh-glyphs-0.1.0.tgz /somewhere/jsesh-elements-0.1.0.tgz
```

**Step 3: make the glyph data available.** The data is not JavaScript, so it should not go through the bundler: it must be *served as files*. With Vite, copy it into the `public/` folder, which is served as is:

```bash
cp -r node_modules/@jsesh/glyphs/data public/jsesh-glyphs
```

(Or host it elsewhere, as in the previous section.) Add this copy to your build scripts if you want it to follow upgrades of the package. Remember that `public/` is copied into your final `dist/`: 38 MB of data.

**Step 4: configure and use.** In your entry file (`src/main.js`):

```js
import { configureJSesh } from "@jsesh/elements"; // also defines <jsesh-text>

// Always set glyphsUrl with a bundler: the default location is the `data` folder next to the
// package's own files, which doesn't exist once the code is bundled.
configureJSesh({ glyphsUrl: "/jsesh-glyphs/" });
```

and in your HTML, `<jsesh-text>nfr-f:r</jsesh-text>` works.

*Alternative, without tarballs:* take `jsesh.js` from the zip, put it in your sources (say `src/vendor/jsesh.js`), and `import { configureJSesh } from "./vendor/jsesh.js"`. It is the same code, in one file.

### Vue and React

`<jsesh-text>` is a standard custom element, so frameworks can use it directly once `@jsesh/elements` has been imported (do the import and the `configureJSesh` call once, in your entry file).

- **Vue**: tell the template compiler that `jsesh-*` tags are not Vue components, otherwise it warns about an unknown component: `compilerOptions.isCustomElement: (tag) => tag.startsWith("jsesh-")` in the Vue plugin options. Then: `<jsesh-text :mdc="text" cadrat-height="30" />`.
- **React 19**: `<jsesh-text mdc={text} cadrat-height="30" />`.

### Building from the sources

The data and the test fixtures are generated from the Java version (`../jseshJsExport`):

```bash
../gradlew -p .. :jseshJsExport:exportAll   # glyph data, fixtures, and JSesh's pictures of the test texts
npm install
npm test            # compares parser, writer and layout with JSesh
npm run dev         # examples: /vanilla/, /editor/, /palette/ and /compare/ (JSesh's pictures next to the port's)
npm run build       # builds the three packages
npm run zip         # builds the bundle and the two zip files, in web-dist/
```

The code follows the Java classes closely, keeping their names, so that both can be compared.

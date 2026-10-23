# JSesh for the web

A TypeScript port of [JSesh](https://jsesh.qenherkhopeshef.org): hieroglyphic texts written in the
*Manuel de Codage* (MdC), as JSesh writes it, displayed in web pages exactly as JSesh displays them.

| Package           | Content                 |
|-------------------|------------             |
| `@jsesh/core`     | MdC parser, document model, MdC writer, layout and SVG drawing. No DOM needed: it runs in browsers and in Node. |
| `@jsesh/glyphs`   | JSesh's sign font (sizes, ligature zones, shapes) and sign database (families, values, tags, variants), exported from the Java version, and their loaders. |
| `@jsesh/elements` | Web Components: `<jsesh-text>`, and `autoRender()` for plain pages. |

## What exists, and what does not

| | Status |
|---|---|
| Displaying MdC (`<jsesh-text>`, `autoRender()`, `renderToSvg()`) | **Available**, in the packages above. |
| Parsing, writing MdC, the document model, layout, sign database | **Available** (`@jsesh/core`, `@jsesh/glyphs`). |
| A hieroglyphic **editor** (cursor, selection, typing signs) | **Example code only**: `examples/editor/editor.js`. It works, but it is not in any package: to use it you copy the file into your site. |
| A sign **palette** | **Example code only**: `examples/palette/palette.js`, same remark. |
| `<jsesh-editor>`, `<jsesh-field>`, `<jsesh-palette>` (ready-to-use Web Components) | **Not written yet.** These names are only planned; nothing in the packages uses them. |

So today, to let someone type MdC you either use an ordinary text field (see [Letting the user type MdC](#letting-the-user-type-mdc-and-getting-it-back)),
or copy and adapt the example editor (see [Development](#development) to run it).

## Installing on a web site

A site needs two things: the **code** (`@jsesh/elements`, which brings `@jsesh/core` and `@jsesh/glyphs`), and the
**glyph data** (the `data` folder of `@jsesh/glyphs`, 38 MB in 6,963 files, see [The glyph data](#the-glyph-data)).
The code is small; the data is not, and is not bundled with the code, so it can live elsewhere.

The packages are not published on npm yet. There are two ways to use them.

- **Plain pages, no build tool: use the bundle** (`npm run zip`, see below). `jsesh.js` is one self-contained file,
  imported by its URL: `import { configureJSesh } from "/jsesh/jsesh.js"`.
- **With a bundler** (Vite, webpack...): build the packages (`npm install && npm run build` here), `npm pack` each
  package in `packages/`, and install the tarballs in your project. Then `import ... from "@jsesh/elements"` works.

A browser cannot resolve a *bare* name such as `"@jsesh/elements"` by itself: it only understands URLs. Outside a
bundler, either import the bundle by URL, as above, or declare an
[import map](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script/type/importmap) before your scripts (the
`editor/` page of the zip does this). So the `from "@jsesh/..."` in the snippets below is for code
going through a bundler, or Node; in a plain page, replace it with the URL of `jsesh.js`.

### Own data on the same site

1. Copy `packages/glyphs/data` to your site, for instance as `/jsesh-glyphs/`.
2. Tell the library where it is, once, before the first text is displayed:

```html
<script type="module">
  import { configureJSesh } from "/jsesh/jsesh.js"; // the bundle; also defines <jsesh-text>
  configureJSesh({ glyphsUrl: "/jsesh-glyphs/" });
</script>
```

With a bundler, always set `glyphsUrl`: the default location is the `data` folder next to the package's own files,
which only exists when the package is served as is.

Visitors only download what they need: `glyph-index.json` (150 kB compressed) and `fonts/EgyptoSerif.ttf` once, and the
shape of each sign actually displayed. Serve the `.json` files compressed.

### Data shared by several sites

`glyphsUrl` can be an absolute URL, so several sites can use one copy of the data, hosted on any static server
or CDN:

```js
configureJSesh({ glyphsUrl: "https://static.example.org/jsesh-glyphs/0.1.0/" });
```

Only the data needs to be shared: each site keeps its own copy of the (small) code, or loads it from the same place.
Requirements on the data host:

- **CORS.** The data is read with `fetch`, and the font is loaded as a web font, and browsers apply CORS to both. The
  host must answer with `Access-Control-Allow-Origin: *` (or the list of your sites). Without it, nothing is displayed
  (the browser console shows the blocked request).
- **Content types and compression**: `.json` as `application/json` and `.ttf` as `font/ttf`, with gzip or brotli.
- **Caching**: put the data in a versioned folder (`.../0.1.0/`) and serve it with a long `Cache-Control`, so that
  returning visitors download nothing.

A single `<jsesh-text>` can also read other data with its `glyphs-url` attribute.

### A zip to give to people

```bash
npm run zip     # needs the glyph data (see Development) and the `zip` command
```

writes two files in `web-dist/`:

- `jsesh-web-<version>.zip`: `jsesh/jsesh.js` (the three packages in one self-contained module), `jsesh/data/` (the glyph
  data), a demo `index.html`, the `editor/` and `palette/` examples, and a `README.md` for the recipient;
- `jsesh-web-<version>-code-only.zip`: the same without `jsesh/data/`, for sites which use a shared copy of the data.

Unzipped on a web server, the demo works as is. Its sources are `bundle/` (the bundle), `web-template/` (demo and README) and
`scripts/make-web-zip.mjs`; the editor and palette pages are the ones from `examples/`, adapted by the script.

## Displaying texts

```html
<script type="module">
  import { configureJSesh } from "@jsesh/elements";
  // Where the glyph data is served (the `data` folder of @jsesh/glyphs).
  configureJSesh({ glyphsUrl: "/jsesh-glyphs/" });
</script>

<p>The word <jsesh-text>nfr-f:r</jsesh-text> means "good".</p>
<jsesh-text cadrat-height="40" direction="rtl">i-w-r:a-C1-m-p*t:pt</jsesh-text>
```

`<jsesh-text>` takes its text from its `mdc` attribute, or else from its content. The text can be a whole JSesh
document (`.gly` file): the preferences in its header are then used.

Attributes:

- `cadrat-height`: height of a quadrat, in pixels (by default, 1.5 times the font size);
- `direction`: `ltr` or `rtl`;
- `orientation`: `horizontal` or `vertical` (columns);
- `centered`: center small signs in their quadrat;
- `justified`: justify lines;
- `glyphs-url`: glyph data for this text only.

The `jseshStyle` property takes any change to the style (see `JSeshStyle` in `@jsesh/core`): colours, line widths,
fonts... The element sends a `jsesh-render` event once the text is displayed.

### Without custom elements

`autoRender()` replaces the content of the elements of class `mdc` with pictures (the MdC text is
kept in their `data-mdc` attribute). Use the `className` option (`autoRender({ className: "hiero" })`) for another
class, or `selector` for any CSS selector:

```html
<span class="mdc">i-w-r:a-C1-m-p*t:pt</span>
<script type="module">
  import { autoRender } from "@jsesh/elements";
  autoRender({ observe: true }); // observe: also process elements added later
</script>
```

### With Vue or React

Both use custom elements directly once `@jsesh/elements` is imported.

- Vue: tell the template compiler that `jsesh-*` tags are custom elements
  (`compilerOptions.isCustomElement: (tag) => tag.startsWith("jsesh-")` in the Vue plugin options), then
  `<jsesh-text :mdc="text" cadrat-height="30" />`.
- React 19: `<jsesh-text mdc={text} cadrat-height="30" />`.

### On a server, or in a script

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

Without a canvas, Latin text is measured approximately; hieroglyphs are not affected.

### Editing

`layOutInPage` (in `@jsesh/elements`) and `layOutText` (in `@jsesh/core`) also take a model (`HieroglyphicText`).
The views then refer to its elements: the subviews of the root view are the views of the top items, in order, and
`view.getSubViewBounds(i)` gives where each one is drawn (in points; multiply by the scale, `cadratHeight` divided by
`style.geometry.standardSignHeight`, for pixels). `examples/editor/editor.js` uses this to place a cursor and a
selection, and to find where a click falls; it types as JSesh does (codes completed by `-`, `:`, `*` or `&`).

## Letting the user type MdC, and getting it back

There are two ways to do it today (a packaged `<jsesh-editor>` does not exist; see [What exists](#what-exists-and-what-does-not)).

**1. An ordinary text field, with a live preview.** The user types MdC as text (`nfr-f:r`), and the field's value is
the MdC. This is the simplest, and enough for a search box. `<jsesh-text>` does not throw on an invalid text (a partial one, while
typing): it sends a `jsesh-error` event, so it can be updated at each keystroke:

```html
<input id="query" size="40" placeholder="MdC, for instance nfr-f:r" />
<jsesh-text id="preview" cadrat-height="40"></jsesh-text>
<script type="module">
  import { configureJSesh } from "@jsesh/elements";
  configureJSesh({ glyphsUrl: "/jsesh-glyphs/" });

  const query = document.getElementById("query");
  const preview = document.getElementById("preview");
  preview.addEventListener("jsesh-error", () => preview.classList.add("invalid"));
  preview.addEventListener("jsesh-render", () => preview.classList.remove("invalid"));
  query.addEventListener("input", () => (preview.mdc = query.value)); // query.value is the MdC to use
</script>
```

**2. A real hieroglyphic editor, copied from the examples.** The user clicks in a box showing the hieroglyphs, with a
cursor, and types sign codes (completed by `-`, `:`, `*`, `&`), with selection and undo. This exists as
`examples/editor/editor.js`, a class `HieroglyphicEditor`, but it is *not part of the npm packages*: copy the file
(and `examples/palette/palette.js` for a sign palette) into your site. It only uses the public API of `@jsesh/core`
and `@jsesh/elements`, so it works there unchanged. Use it like this (see `examples/editor/index.html` for a complete page):

```js
import { configureJSesh } from "@jsesh/elements";
import { HieroglyphicEditor } from "./editor.js"; // your copy

configureJSesh({ glyphsUrl: "/jsesh-glyphs/" });
const editor = new HieroglyphicEditor(document.getElementById("surface"), { // a <div tabindex="0">
  onChange: (state) => (document.getElementById("mdc").value = state.mdc), // the MdC, after each change
});
editor.setMdc("nfr-f:r"); // put a text in the editor
editor.insertSign("A1");  // insert a sign at the cursor
editor.state.mdc;         // the MdC currently in the editor
```

What is planned is to turn this example into packaged elements (`<jsesh-editor>`, and a palette and single-line field
`<jsesh-palette>`, `<jsesh-field>`), so that no copying is needed. Until then, the example is the editor.

### Example: finding a typed text

JSesh's search (`jseshSearch`) is not ported. Still, MdC is easy to compare once it is *normalized*: `a`,
`nfr` or `F35` all denote the same sign, and the same sign can be spelled in several ways in the texts. Parse
the query and the texts, reduce them to the lists of their canonical sign codes, and look for the former in the latter:

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

Here, typing `nfr` finds entry 2 (`nfr` is `F35`). Grouping (`:`, `*`) is ignored: this searches for signs in
sequence, wherever they are in the quadrats. If you want quadrats to matter, compare the normalized MdC instead:
`toMdc(parseMdc(mdc), { normalize: (code) => glyphs.canonicalCode(code) })` (`toMdc` is in `@jsesh/core`) gives one
canonical string per text, which can be indexed in a database or compared with `===` or `includes`. The same
functions run in Node, on the server, with `GlyphIndexProvider` as shown in [On a server](#on-a-server-or-in-a-script)
(`canonicalCode` only needs `glyph-index.json`, not the shapes).

## The glyph data

`@jsesh/glyphs` reads a folder with:

- `glyph-index.json` (540 kB, 150 kB compressed): the size and ligature zones of each of the 6,963 signs, the
  phonetic codes and the predefined ligatures. It is loaded once, and lays out texts without other requests;
- `glyphs/*.json`: one file per sign shape, loaded when the sign is displayed;
- `fonts/EgyptoSerif.ttf`: the transliteration font;
- `sign-info.json` (700 kB, 130 kB compressed): the sign database, for palettes only. `loadSignDatabase(url)` reads
  it as a `SignDatabase`, whose methods are those of JSesh's `HieroglyphDatabase` (`getFamilies()`,
  `getCodesForFamily(family, includeVariants)`, `getValuesFor(code)`, `getTagsForSign(code)`, `getVariants(code)`,
  `getSignsContaining(code)`, `getDescriptionFor(code)`, `getPossibilityFor(value)`...).

A site can serve it itself or from a shared host (`glyphsUrl`, see [Installing on a web site](#installing-on-a-web-site)),
or use the copy in the package. Another font can be used by implementing
`GlyphProvider` (in `@jsesh/core`).

## Development

The data and the test fixtures are generated from the Java version (`../jseshJsExport`):

```bash
../gradlew -p .. :jseshJsExport:exportAll   # glyph data, fixtures, and JSesh's pictures of the test texts
npm install
npm test            # compares parser, writer and layout with JSesh
npm run dev         # examples: /vanilla/, /editor/, /palette/ and /compare/ (JSesh's pictures next to the port's)
npm run build
```

The code follows the Java classes closely, keeping their names, so that both can be compared.

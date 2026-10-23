# JSesh for your web site

Hieroglyphic texts written in the *Manuel de Codage*, displayed as JSesh displays them. Nothing to install: copy
files, and open them from a web server (opening the `.html` files directly from the disk does not work, because
browsers refuse to load data from `file://`. For a quick test, run `python3 -m http.server` in this folder and open
<http://localhost:8000/>).

## Contents

| | |
|---|---|
| `jsesh/jsesh.js` | The library, in one file (parser, writer, layout, drawing, `<jsesh-text>`). |
| `jsesh/data/` | The glyph data: signs, sign database and the transliteration font (only in the full package). |
| `index.html` | The simplest use: copy what you need from it. |
| `editor/`, `palette/` | A hieroglyphic editor and a sign palette. Plain JavaScript, written to be copied and adapted. |
| `examples.css` | Only styles the examples. |

## On your site

1. Copy `jsesh/` (the library, and the data) to your site, for instance as `/jsesh/`.
2. In your pages:

```html
<script type="module">
  import { configureJSesh } from "/jsesh/jsesh.js"; // also defines <jsesh-text>
  configureJSesh({ glyphsUrl: "/jsesh/data/" });
</script>

<p>The word <jsesh-text>nfr-f:r</jsesh-text> means "good".</p>
```

`<jsesh-text>` takes its text from its content or from its `mdc` attribute (which can be changed by a script).
Other attributes: `cadrat-height` (height of a quadrat in pixels), `direction` (`ltr` or `rtl`),
`orientation` (`horizontal` or `vertical`), `centered`, `justified`.

### One copy of the data for several sites

The data is large (38 MB, but a page only loads the signs it shows). If you have several sites, host the
`jsesh/data` folder once, and give its address to all of them:

```js
configureJSesh({ glyphsUrl: "https://static.example.org/jsesh-glyphs/" });
```

Each site then only needs `jsesh/jsesh.js` (the `-code-only` package has just that). The server hosting the data must
send the header `Access-Control-Allow-Origin: *` (or list your sites): without it, the browser blocks the data and the
font, and nothing is shown. Serve the `.json` files compressed.

### Getting the text the user typed

The text is plain MdC. For a text field, the field's `value` is the MdC; see the "Typing MdC" part of `index.html`.
For the editor (`editor/`), `editor.state.mdc` is the text being edited, and the `onChange` function given to
`HieroglyphicEditor` is called after each change.

### Using the library in your own scripts

`jsesh/jsesh.js` exports all of `@jsesh/core` (`parseMdc`, `toMdc`, `renderToSvg`...), `@jsesh/glyphs`
(`loadGlyphProvider`, `loadSignDatabase`...) and `@jsesh/elements` (`configureJSesh`, `renderMdc`, `autoRender`...):

```js
import { renderMdc, toMdc, parseMdc } from "/jsesh/jsesh.js";

const svg = await renderMdc("nfr-f:r", { cadratHeight: 40 }); // a string with an <svg> picture
const normalized = toMdc(parseMdc("nfr-f:r"));
```

`jsesh.js` is meant for browsers (it defines a custom element). For Node, use the `@jsesh/core` and `@jsesh/glyphs`
packages instead.

The pages in `editor/` and `palette/` have an `importmap` which says that `@jsesh/core`, `@jsesh/glyphs` and
`@jsesh/elements` all mean `jsesh/jsesh.js`; this is what lets their scripts `import` them. Reuse it if you copy those scripts.

## Licence

CeCILL-C. JSesh: <https://jsesh.qenherkhopeshef.org>.

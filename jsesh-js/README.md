# JSesh for the web

A TypeScript port of [JSesh](https://jsesh.qenherkhopeshef.org): hieroglyphic texts written in the
*Manuel de Codage* (MdC), as JSesh writes it, displayed in web pages exactly as JSesh displays them.

| Package | Content |
|---|---|
| `@jsesh/core` | MdC parser, document model, MdC writer, layout and SVG drawing. No DOM needed: it runs in browsers and in Node. |
| `@jsesh/glyphs` | JSesh's sign font (sizes, ligature zones, shapes) and sign database (families, values, tags, variants), exported from the Java version, and their loaders. |
| `@jsesh/elements` | Web Components: `<jsesh-text>`, and `autoRender()` for plain pages. |

The sign palette and the editors (`<jsesh-palette>`, `<jsesh-editor>`, `<jsesh-field>`) are to come. Meanwhile,
`examples/editor/` and `examples/palette/` show how an editor and a palette can be built on the current API (see [Development](#development)).

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

`autoRender()` replaces the content of the elements of class `hieroglyphic-text` with pictures (the MdC text is
kept in their `data-mdc` attribute):

```html
<span class="hieroglyphic-text">i-w-r:a-C1-m-p*t:pt</span>
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

A site can serve it itself (`glyphsUrl`), or use the copy in the package. Another font can be used by implementing
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

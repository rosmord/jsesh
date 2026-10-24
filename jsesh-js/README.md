# JavaScript port of JSesh

A TypeScript port of [JSesh](https://jsesh.qenherkhopeshef.org): it displays, and lets users edit, hieroglyphic texts written in the *Manuel de Codage* (MdC), in web pages and in Node, drawn exactly as JSesh draws them.

```html
<p>Use <jsesh-text>D&&&Y3</jsesh-text> !</p>

<script type="module">
  import { configureJSesh } from "/jsesh/jsesh.js"; // defines <jsesh-text>
  configureJSesh({ glyphsUrl: "/jsesh/data/" });    // where the glyph data is served
</script>
```

## What is in it

| Package | Content |
| --- | --- |
| `@jsesh/core` | MdC parser and writer, document model, layout and SVG drawing. No DOM: runs in browsers and in Node. |
| `@jsesh/glyphs` | JSesh's hieroglyphic font data and sign database (exported from the Java version), and their loaders. |
| `@jsesh/elements` | The Web Component `<jsesh-text>`, and `autoRender()` for plain pages. |
| `@jsesh/editor` | The hieroglyphic editor: `<jsesh-editor>`, and the headless `EditorSession` under it. |
| `@jsesh/palette` | The sign palette: `<jsesh-palette>`, and the headless `PaletteController` under it. |
| `@jsesh/vue` (in [jsesh-vuejs/](jsesh-vuejs/README.md)) | Vue 3 components over the elements, and a sample application. |

Not ported (yet): JSesh's search, the PDF and RTF exports, and `<jsesh-field>`, a single-line editor.

## Documentation

- **[User guide](documentation/user-guide.md)**: installing JSeshJs on a web site, displaying and editing texts, the palette, reading and writing MdC, sharing the glyph data between sites. Start here.
- **[Customisation guide](documentation/customisation.md)**: changing the style, using your own signs, rendering on a server, taking the pipeline apart, building your own editor or palette (Vue, React...).
- **[Maintainer guide](documentation/maintainer-guide.md)**: how the code is organised and how it relates to the Java version, testing, releases.

## Building

The packages are not published on npm yet: you build them from the sources, or take the zip file described in the user guide.

You need Node 20.19 or later (22.12 or later), and a JDK for the first step.

**1. Generate the glyph data and the test fixtures**, from the Java version. They are not stored in git, so a fresh clone does not have them. From the root of the repository:

```bash
./gradlew :jseshJsExport:exportAll
```

This writes `packages/glyphs/data/` (about 38 MB: the sign shapes, the sign database, the transliteration font) and `fixtures/generated/` (what the tests compare the port with).

**2. Install, test and build**, in this folder (`jsesh-js/`):

```bash
npm install
npm test            # compares the parser, writer and layout with what JSesh produces
npm run build       # builds the packages (and runs the type check)
```

**3. Try it**:

```bash
npm run dev         # examples: a page of texts, the editor, the palette, custom components, and a comparison with JSesh's pictures
```

**4. Make the files to give to others** (needs the `zip` command and the glyph data):

```bash
npm run zip         # web-dist/jsesh-web-<version>.zip, and a -code-only.zip without the glyph data
```

The zip files contain the library in one file (`jsesh.js`), the glyph data, a demo page and examples: unzip them on a web server and they work as is.

All the packages share one version number, set with `npm run version:set -- X.Y.Z` (see the [maintainer guide](documentation/maintainer-guide.md#version-numbers)).

## Licence

CeCILL-C. JSesh: <https://jsesh.qenherkhopeshef.org>.

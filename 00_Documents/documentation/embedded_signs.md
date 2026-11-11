# Appendix: embedded signs in JSesh documents

A JSesh document can carry signs drawn by its user, which no font contains. They are stored in the header of the `.gly` file and used in the text as `sign1`, `sign2`, and so on. This appendix explains how to read them. It is for people who write a program that reads JSesh files, and for people who work on the JSesh code that reads and writes these signs.

It completes [manuel-de-codage.md](manuel-de-codage.md), §12 *Files*, which describes the header in general. As there, when this document and the code disagree, the code is right, and this document should be fixed.

| Subject | Java | TypeScript port |
|---|---|---|
| The signs of a document | `jsesh.document.embeddedsigns.EmbeddedSignList`, `EmbeddedSign` | `core/src/document/embedded-signs.ts` |
| Header format | `jsesh.io.document.EmbeddedSignsHeaderCodec` | `embeddedSignsFromMap`, `embeddedSignsToMap` |
| Reading the SVG | `jsesh.glyphs.signsource.SVGSignSource` | (not ported) |
| The rendering data | `EmbeddedSignShapes.renderingData` | `core/src/render/embedded-glyphs.ts` |

## Contents <!-- omit from toc -->

- [1. Overview](#1-overview)
- [2. In the text](#2-in-the-text)
- [3. In the header](#3-in-the-header)
- [4. Decoding a sign, step by step](#4-decoding-a-sign-step-by-step)
- [5. The rendering data: drawing a sign](#5-the-rendering-data-drawing-a-sign)
- [6. The SVG file: reading it as JSesh does](#6-the-svg-file-reading-it-as-jsesh-does)
- [7. Writing embedded signs](#7-writing-embedded-signs)
- [8. Example code](#8-example-code)

## 1. Overview

Each embedded sign has:

- a **code**, `sign` followed by a number (`sign1`, `sign12`);
- the **SVG file** the user gave, byte for byte. This is the reference;
- the **rendering data**: the sign's size, outline and ligature zones, as JSesh reads them from the SVG. JSesh computes them for programs which can't read arbitrary SVG files, such as its own JavaScript port;
- optionally, the **Gardiner code** it is a variant of. JSesh then proposes the sign when the user types that code. Only Gardiner codes are accepted (`A1`, `Aa15`, `US1A1`...), in any case, and stored with the Manuel de Codage capitalisation (`a1` → `A1`); phonetic codes such as `mn` are refused;
- a **description**, possibly empty.

Older versions of JSesh ignore these header lines. They show `sign1` as an unknown code.

## 2. In the text

An embedded sign is used like any other sign. Its code is an ordinary sign code for the lexer (`[@a-zA-Z0-9]+`, see the grammar in [manuel-de-codage.md](manuel-de-codage.md)). It can be grouped, put in a cartouche or in a ligature, have modifiers, and so on:

```
sign1-A1:sign2-sign1*sign2-w&&&sign2
```

A code is an embedded sign code if it matches `sign[1-9][0-9]*` exactly. Case matters: `SIGN1` is not one. A `sign` code which has no definition in the header is an unknown code. JSesh draws such a code as text.

**Searching.** A sign which declares a Gardiner equivalent ("variant of") is found under both codes. If `sign1` is a variant of `A5`, searching `i-A5` finds `i-sign1`, whatever the variant level of the search; searching `sign1` finds only `sign1`. The search which respects the layout compares each quadrat as it is, and once its embedded signs are replaced by their equivalents: `A5:Z1` finds `sign1:Z1`, but a query which mixes the two (`sign1:A5`) only finds itself. A program which searches JSesh texts should do the same.

## 3. In the header

Each sign is one header line. One more line gives the highest sign number used so far:

```
++JSesh_max_embedded_sign_id 2 +s
++JSesh_embedded_sign_sign1 A1 v0 Rml4dHVyZSBzaWdu H4sIAAAAAAAA/7MpLktX… H4sIAAAAAAAA/6tWKley… +s
++JSesh_embedded_sign_sign2 - v0 - H4sIAAAAAAAA/7MpLktX… H4sIAAAAAAAA/6tWKlex… +s
```

(the long values are cut here.)

As for any header line, the value is the text between the first space and the final ` +s` (see §12 of [manuel-de-codage.md](manuel-de-codage.md)). The property name is `JSesh_embedded_sign_` followed by the sign code. The value has **five fields separated by single spaces**. No field ever contains a space:

| # | Field | Content | Empty value |
|---|---|---|---|
| 1 | variant of | A Gardiner code (`A1`, `Y5`), written as is. A reader ignores (and keeps the sign) any other value | `-` |
| 2 | format version | The version of the storage format of the sign: `v0` | (never empty) |
| 3 | description | The description in UTF-8, base64-encoded | `-` |
| 4 | SVG | The user's SVG file, base64-encoded | (never empty) |
| 5 | rendering data | A JSON object in UTF-8 (§5), base64-encoded | (never empty) |

**Format version.** The fields after the version depend on it. This document describes version `v0`, the only one so far. A later version may change, add or remove the following fields; it will keep the first two. A reader must check the version and skip a sign whose version it doesn't know. JSesh does so, with a warning.

`JSesh_max_embedded_sign_id` is written as soon as the document has had one sign. Numbers are never reused: if `sign2` is removed, the next sign is `sign3`. That way, a copy of the text made before the removal never shows another drawing for `sign2`. So the highest number is not always the number of the last sign.

The lines come in the alphabetical order of their property names, after the document preferences. A reader should not depend on this order.

**Encodings.** Base64 is the standard alphabet (`A–Z a–z 0–9 + /`), with `=` padding and without line breaks (RFC 4648, §4). The data is not compressed: decoding the base64 gives the SVG file or the JSON text directly (on the command line, `base64 -d`). The values may contain `+`: this is no problem, since only the final ` +s`, with its space, ends the line.

## 4. Decoding a sign, step by step

1. Read the header lines (`++JSesh_… +s`) and keep the properties whose name starts with `JSesh_embedded_sign_`.
2. Take the code after this prefix, and check that it matches `sign[1-9][0-9]*`.
3. Split the value on spaces. Field 2 must be `v0`; if it is not, skip the sign (§3). There must then be exactly five fields.
4. Field 1: `-` means "no variant", anything else is a Gardiner code.
5. Field 3: `-` means an empty description. Otherwise, decode it from base64 and read the bytes as UTF-8.
6. Field 4: decode it from base64. The result is the bytes of the SVG file. Keep them as **bytes**: the file may declare another encoding than UTF-8 in its XML declaration (`<?xml … encoding='ISO-8859-1'?>`), and an XML parser handles that.
7. Field 5: decode it from base64, read it as UTF-8, and parse it as JSON (§5).
8. Read `JSesh_max_embedded_sign_id` as an integer. If it is missing or smaller than the highest sign number, use that number.

JSesh skips a malformed line (wrong code, unknown format version, wrong number of fields, bad base64 data) and logs a warning. The other signs are still read.

JSesh itself doesn't read field 5. It recomputes the rendering data from the SVG each time it writes a document.

## 5. The rendering data: drawing a sign

This is the simplest way to draw a sign. The JSON object has four members:

```json
{"w":16,"h":18,"d":"M 8 0  L 0 18  L 16 18  L 8 0  Z","zones":[[-3.5,9,4,4.5,"ec"],[12.6875,0,8,7.2,"st"],null]}
```

| Member | Content |
|---|---|
| `w`, `h` | Width and height of the sign: its bounding box. |
| `d` | The outline, as an SVG path (commands `M`, `L`, `Q`, `C`, `Z`, absolute coordinates, separated by spaces). The bounding box starts at 0,0. |
| `zones` | Three ligature zones, or `null` for a missing zone. |

Numbers have at most four decimals.

**Coordinates.** The units are those of JSesh's fonts. The sign A1 is about 18 units high, so a sign of normal height has `h` close to 18. The y axis points **down**, as in SVG. To draw the sign at a given height, scale it by `height / 18` (more exactly, divide by the height of A1 in the font you use with it).

**Filling.** Fill the outline in black, with the **nonzero** fill rule. The outline already has its holes: JSesh has merged the parts of the drawing (see §6).

**Ligature zones.** A zone is `[x, y, width, height, gravity]`, in the sign's coordinates. A zone may extend beyond the sign; `x` can be negative. Zone 0 receives the signs placed **before** the sign in a ligature (`A&&B`), zone 1 those placed **after** it, and zone 2 those placed **below** it. `gravity` has two letters. The first is the horizontal position of the ligatured group in the zone: `s` (start), `c` (centre) or `e` (end). The second is its vertical position: `t` (top), `c` (centre) or `b` (bottom). If the SVG file defines no zone, these are the zones JSesh computes for the sign (`LigatureZoneBuilder`). This is the same format as the glyph data of the JavaScript port (`glyph-index.json`, see `jsesh-js/documentation/maintainer-guide.md`).

## 6. The SVG file: reading it as JSesh does

To get the drawing JSesh draws without using the rendering data, read the SVG as `SVGSignSource` does. This is **not** a general SVG renderer:

- **Drawn elements.** Only `path` (all commands, arcs included) and `polygon`. `rect`, `circle`, `ellipse`, `line`, `polyline`, `use`, text and images are not drawn.
- **Ignored structures.** Transforms (`transform` attributes, on elements or groups) are ignored. So is everything inside a `pattern` or a `clipPath`.
- **Combining the parts.** The parts are combined as areas, in document order:
  - a part filled in black (no fill colour, `black`, `#000000`, `rgb(0,0,0…)` or `currentColor`) is added;
  - a part filled with another colour (white, for instance) is **subtracted**: this is how holes are drawn;
  - `fill:none` means no filling;
  - a stroke (`stroke` other than `none`) adds the outline of the part, with its `stroke-width`.

  Styles come from the `style` attribute and from the presentation attributes. They are inherited from the enclosing elements. The fill rule of each part is nonzero; `fill-rule` is ignored.
- **Scale.** If the drawing is more than 60 units high, it is divided by 100. Signs are drawn at 1800 units for an 18-unit A1, and older files were drawn at 18.
- **Position.** The drawing is then moved so that its bounding box starts at 0,0. The ligature zones are moved with it.
- **Ligature zones.** A zone is defined in one of two ways:
  - a `rect` whose `id` starts with `zone` (`zone1`, `zone2`);
  - a `rect` inside an `a` element whose `title` attribute is the zone's name.

  The gravity comes from the label (`label`, or `inkscape:label`): `gravity:` followed by `s`/`e` and `t`/`b` (`gravity:st`). Without gravity, the zone is centred. **Current behaviour:** only `zone1` and `zone2` are recognized, as zones 0 and 1. A rect with any other zone name, `zone3` included, is read as zone 0. So a file with zones can't define the "below" zone. When the file defines no zone, JSesh computes the three zones (§5).
- **Metadata.** `dc:creator` and `dc:description` (Dublin Core) are read, but not used for embedded signs: their description is the one in the header.

The JSesh reference for all this is `SVGSignSource.SVGReader`. The user's sign folder is read the same way, so an embedded sign looks the same as it would in the user's fonts.

## 7. Writing embedded signs

A program which writes JSesh documents should:

- write the five fields as in §3, with the version `v0`, without line breaks;
- give a new sign the number `max + 1`, where `max` is `JSesh_max_embedded_sign_id`, and update that property;
- never reuse the number of a removed sign;
- keep the SVG bytes as they are;
- keep `JSesh_max_embedded_sign_id` even if no sign is left;
- if it changes the SVG of a sign, recompute the rendering data, or at least make it consistent. JSesh recomputes it when it saves the document, but other readers trust it.

JSesh saves a document with embedded signs in the `.gly` format, whatever its other options are, because only that format has a header.

**Moving text to another document.** Codes only make sense in their own document: `sign1` in one document and `sign1` in another are usually different drawings. When JSesh pastes text copied from another document, it brings the signs the text uses with it:

- a sign whose SVG is the same, byte for byte, as a sign of the target document takes that sign's code. The target's "variant of" and description are kept;
- the other signs are added to the target with new numbers (`max + 1`, `max + 2`…), keeping their "variant of" and description;
- the codes of the pasted text are renamed to match. The renaming is simultaneous: if `sign1` becomes `sign2` and `sign2` becomes `sign1`, the two codes are swapped.

Copying and pasting inside one document therefore changes no code. A program which merges JSesh texts should follow the same rules.

## 8. Example code

**Python**, reading the signs of a `.gly` file:

```python
import base64, json, re

def embedded_signs(gly_text):
    signs, max_id = {}, 0
    for line in gly_text.splitlines():
        if not line.startswith("++JSesh_"):
            break                                   # end of the header
        name, _, rest = line[2:].partition(" ")
        value = rest[:-3] if rest.endswith(" +s") else rest
        if name == "JSesh_max_embedded_sign_id":
            max_id = max(max_id, int(value))
        elif name.startswith("JSesh_embedded_sign_"):
            code = name[len("JSesh_embedded_sign_"):]
            fields = value.split(" ")
            if not re.fullmatch(r"sign[1-9][0-9]*", code) or len(fields) < 2 or fields[1] != "v0":
                continue                            # malformed or unknown version: skipped, as JSesh does
            if len(fields) != 5:
                continue
            variant, _version, description, svg, rendering = fields
            signs[code] = {
                "variantOf": None if variant == "-" else variant,
                "description": "" if description == "-" else base64.b64decode(description).decode("utf-8"),
                "svg": base64.b64decode(svg),                          # bytes
                "rendering": json.loads(base64.b64decode(rendering)),
            }
            max_id = max(max_id, int(code[4:]))
    return signs, max_id
```

**JavaScript** (browser or Node 16+), decoding one field:

```js
function base64ToBytes(text) {
  return Uint8Array.from(atob(text), (c) => c.charCodeAt(0));
}

const rendering = JSON.parse(new TextDecoder().decode(base64ToBytes(fields[4])));
// <path d={rendering.d} fill="black"/>, in a box of rendering.w × rendering.h
```

With the JavaScript port, `readMdcDocument` reads the signs (`document.embeddedSigns`), and `renderToSvg`, `layOutText` and `layOutTextSync` draw them. The data is decoded only when the signs are drawn (`embeddedSignSvg` and `embeddedSignRendering` give the decoded fields).

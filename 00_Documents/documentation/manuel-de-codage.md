# The Manuel de Codage in JSesh

This document describes the Manuel de Codage (MdC) as JSesh reads and writes it in version 9 (`9.0.0-SNAPSHOT`). It is for people who write MdC by hand, who write tools which read or produce it, or who work on the JSesh parser.

It describes what the code does, not what the original Manuel de Codage or any other program specifies. JSesh's MdC is a dialect: it has constructs of its own (`%zone`, `%insert`, `%hkern`...), and it keeps constructs from older software.

The document has three separate parts:

- **[Part I](#part-i-the-language-in-current-use)** describes the language in current use. Use it to write new texts.
- **[Part II](#part-ii-legacy-constructs)** describes the constructs kept for compatibility with tksesh, Winglyph, MacScribe and older versions of JSesh. Avoid them in new texts.
- **[Part III](#part-iii-annotated-grammar)** is the annotated grammar: lexical rules, syntax and interpretation.

When this document and the code disagree, the code is right, and this document should be fixed. The reference sources are:

| Subject | Source |
|---|---|
| Lexical rules | `jsesh.parser.lexer.MdcLexicon`, `MdcLexer`, `MdcTokenType` |
| Syntax | `jsesh.parser.MDCParser` |
| Interpretation into the model | `jsesh.mdcreader.AstModelBuilder`, `jsesh.model.ModifiersList` |
| Writing MdC back | `jsesh.io.mdc.MdCModelWriter` |
| File reading and dialect detection | `jsesh.io.document.MDCDocumentReader` |

The TypeScript port has its own lexer and parser in `jsesh-js/packages/core/src/parser/`. A change to the language must be made there too.

## Contents <!-- omit from toc -->

- [Part I. The language in current use](#part-i-the-language-in-current-use)
  - [1. A text at a glance](#1-a-text-at-a-glance)
  - [2. Signs](#2-signs)
  - [3. Quadrats](#3-quadrats)
  - [4. Sign modifiers and sign properties](#4-sign-modifiers-and-sign-properties)
  - [5. Combining signs](#5-combining-signs)
  - [6. Cartouches and other enclosures](#6-cartouches-and-other-enclosures)
  - [7. Colour and shading](#7-colour-and-shading)
  - [8. Philological marks](#8-philological-marks)
  - [9. Alphabetic text, superscripts, comments](#9-alphabetic-text-superscripts-comments)
  - [10. Lines, pages and zones](#10-lines-pages-and-zones)
  - [11. Forms accepted without effect](#11-forms-accepted-without-effect)
  - [12. Files](#12-files)
  - [13. The form JSesh writes](#13-the-form-jsesh-writes)
- [Part II. Legacy constructs](#part-ii-legacy-constructs)
  - [14. How a legacy file is recognised](#14-how-a-legacy-file-is-recognised)
  - [15. Legacy forms still read](#15-legacy-forms-still-read)
  - [16. MacScribe forms that are not supported](#16-macscribe-forms-that-are-not-supported)
- [Part III. Annotated grammar](#part-iii-annotated-grammar)
  - [17. Notation and overview](#17-notation-and-overview)
  - [18. Lexical level](#18-lexical-level)
  - [19. Syntax](#19-syntax)
  - [20. Rules outside the grammar](#20-rules-outside-the-grammar)
  - [21. Traps](#21-traps)

---

# Part I. The language in current use

## 1. A text at a glance

An MdC text is a plain string in which each hieroglyph is written as a code, and punctuation says how the signs are arranged:

```
i-w-r:a-C1-m-p*t:pt
```

This text has six quadrats (`i`, `w`, `r:a`, `C1`, `m`, `p*t:pt`). A *quadrat* is the ideal square of hieroglyphic writing (the model class is named `Cadrat`). The main levels of a text are:

| Level | Written | Contains |
|---|---|---|
| Text | whole string | top-level items, separated by `-` |
| Top-level item | | quadrat, line or page break, zone, alphabetic text, superscript |
| Quadrat | `a:b` | one or more lines, stacked with `:` |
| Line of a quadrat | `a*b` | one or more elements, side by side with `*` |
| Element | | sign, group, ligature, insertion, cartouche... |

A text with a syntax error is rejected as a whole by the parser. There is no error recovery (see [§14](#14-how-a-legacy-file-is-recognised) for what happens when reading a file).

## 2. Signs

### 2.1. Sign codes

| Kind | Examples | Notes |
|---|---|---|
| Gardiner code | `A1`, `G17`, `Aa1`, `Ff1` | Family (one uppercase letter, `Aa` or `Ff`), then a number. |
| Variant | `A1A`, `A23h`, `A1VARA`, `D2EXTA` | Gardiner code followed by a variant part. |
| Phonetic code | `n`, `nfr`, `xpr`, `A`, `nTrw` | Short name for a sign; case matters (`A` is G1, `a` is D36). `nn` (M22B) and `nTrw` (R8A) are JSesh additions to the 1988 list. |
| Number | `2`, `20`, `300` | Numerals. |
| Nome sign | `NL5`, `NU12` | Families `NL` and `NU`. |
| User sign | `US1A1`, `US22A1VARA` | `US`, a user number, then a Gardiner-like code. |

The grammar does not check that a code exists: any run of letters and digits is a sign code. A code unknown to the sign database is kept, and its text is drawn in red in place of the sign. `A1B1`, without a separator, is one unknown code, not two signs.

When writing MdC, `MdCModelWriter` can replace phonetic codes by their Gardiner equivalents (the `normalized` option, based on `ManuelDeCodage.getCanonicalCode`).

### 2.2. Special signs

These codes are signs like the others: they can be placed in groups and take modifiers.

| Code | Sign | Size |
|---|---|---|
| `.` | half space | half a quadrat wide and high |
| `..` | full space | full quadrat |
| `o` | red point | |
| `O` | black point | |
| `//` | full shade | full quadrat |
| `/` | quarter shade | half a quadrat wide and high |
| `h/` | horizontal shade | full width, half height |
| `v/` | vertical shade | half width, full height |
| `"text"` | small text used as a sign | size of the text |

In small text, `]` and `"` must be preceded by a backslash. The backslash stays in the code and is displayed.

## 3. Quadrats

| Operator | Meaning | Example |
|---|---|---|
| `-` | separates quadrats | `i-w` |
| `:` | stacks lines in a quadrat | `r:a` |
| `*` | places elements side by side | `p*t` |
| `( )` | groups | `(p*t):n`, `n*(t:r)` |

`*` binds more tightly than `:`, so `p*t:pt` means "`p*t` above `pt`". Parentheses nest. They may hold several quadrats (`(A1-B1)`), and those are then treated as one element.

The `-` can often be omitted (see [§18.5](#185-spaces-and-separators)), but JSesh always writes it, and new texts should too.

## 4. Sign modifiers and sign properties

### 4.1. Modifiers

Modifiers come right after the sign code, each one starting with `\`.

| Modifier | Effect | Example |
|---|---|---|
| `\` | mirrors the sign | `A1\` |
| `\N` | scale, in percent | `A1\50` |
| `\RN` | rotation in degrees, clockwise; may be negative; normalised to 0-359 | `A1\R90`, `A1\R-90` |
| `\l` | wide sign: a line of a quadrat containing only this sign does not force the quadrat to be scaled down to the maximum width | `N17\l` |
| `\red` | draws the sign in red | `A1\red` |
| `\i` | draws the sign in gray | `A1\i` |
| `\shadingN` | shades part of the sign; `N` is a list of digits, as in [§7.2](#72-shading) | `A1\shading12` |
| `\?` | marks a doubtful reading; stored, but not drawn | `A1\?` |
| `\name`, `\nameN` | any other name is stored and written back; if the name is in the colour map of the style, the sign is drawn in that colour | `A1\col50` |

If a modifier is repeated, the last one wins (`A1\50\200` has scale 200). `\name` and `\name3` are different modifiers. `\R` and `\r` need a value: without one they have no effect.

### 4.2. Explicit position

`{{x,y,scale}}` follows the modifiers:

```
A1**B1{{120,300,50}}
```

`x` and `y` are in thousandths of the height of A1, and `scale` is in percent. All three are required, and they cannot be negative. The layout only uses `x` and `y` in `**` groups ([§5.5](#55-absolute-groups)). Elsewhere only the scale has an effect. `{{0,0,100}}` is the same as no position at all. `{{x,y,scale}}` replaces a scale given with `\N`.

### 4.3. Word ends and grammatical signs

| Form | Meaning | Example |
|---|---|---|
| `_` after a sign | end of word | `n_` |
| `__` after a sign | end of sentence | `n__` |
| one white space after a sign | end of word | `n w` |
| two white spaces after a sign | end of sentence | `n  w` |
| `=` before a sign | grammatical sign | `=n` |

These properties are stored and written back, and the editor can set them. They do not change the drawing. They come in this order around a sign: `=`, code, modifiers, position, word end.

White space after a sign is significant, line breaks included ([§18.5](#185-spaces-and-separators)).

## 5. Combining signs

### 5.1. Ligatures

`&` joins two or more signs:

```
p&t
stp&n&ra
```

JSesh first looks for a predefined ligature for these signs. Otherwise, for two signs, it places the smaller one in a *ligature zone* of the larger one, and for three signs it places the first and third around the second, if the zones exist. Otherwise the signs are not arranged.

Only signs can be joined with `&`, not groups.

### 5.2. Ligatures with a group

`^^^` and `&&&` place a whole group in the ligature zones of a sign:

```
t^^^w           group before the sign
w&&&t           group after the sign
t^^^w&&&n       both
(a:b)^^^w&&&(c:d)
```

The middle element must be a sign. The group after `&&&` goes in the bottom zone of the sign if there is one, otherwise in its "after" zone. If the sign has no zone, the operator has the same effect as `*`.

### 5.3. Insertions

`%insert` places a group in the free space of a sign, computed from the actual shapes:

```
%insert[dir=bs,dw=20](D36,(n:t))
```

| Option | Value | Meaning |
|---|---|---|
| `dir` | required | where the inserted element goes, relative to the target |
| `target` | `1` (default) or `2` | which argument is the target; the other one is inserted |
| `dw` | integer, 0 by default | enlarges the target's frame horizontally, in percent of the inserted element's width |
| `dh` | integer, 0 by default | enlarges the target's frame vertically, in percent of the inserted element's height |

| `dir` | Position |
|---|---|
| `s`, `e` | start, end |
| `t`, `b` | top, bottom |
| `ts`, `te` | top-start, top-end |
| `bs`, `be` | bottom-start, bottom-end |
| `c` | centre |

"Start" and "end" follow the writing direction: start is left in left-to-right text and right in right-to-left text.

Arguments are in reading order. `%insert[dir=bs](A,B)` and `%insert[dir=bs,target=2](B,A)` insert `B` into `A`, but `B` is read first in the second one. Both arguments accept any content a group accepts, so insertions can nest:

```
%insert[dir=te](%insert[dir=bs](t,w),t)
```

### 5.4. Kerning

`%hkern` and `%vkern` bring the groups before and after them closer, so that they are about one inter-sign space apart where their signs are closest.

```
p*%hkern*q      in a line of a quadrat
p:%vkern:q      between the lines of a quadrat
p-%hkern-q      between quadrats
```

`%hkern` only acts between elements of a line, or between quadrats in text written in lines. `%vkern` only acts between lines of a quadrat, or between quadrats in text written in columns. Elsewhere the operator is ignored. Options are accepted and saved (`%hkern[a=1]`), but none is used yet.

### 5.5. Absolute groups

`**` builds a group where each sign has an explicit position:

```
A1**B1{{120,300,50}}**C1{{0,500,100}}
```

The signs are placed from their `{{x,y,scale}}`. The group needs at least two signs. Shading signs are moved to the front of the group, so that they are drawn first.

### 5.6. Overwriting

`##` draws two signs on top of each other, both centred:

```
a##n
```

Both operands must be signs.

## 6. Cartouches and other enclosures

| Start | End | Enclosure |
|---|---|---|
| `<` | `>` | cartouche |
| `<s` | `s>` | serekh |
| `<h` | `h>` | Hwt-sign |
| `<f` | `f>` | castle |
| `<g` | `g>` | circular enclosure |

JSesh's normal spelling of a complete enclosure uses an uppercase letter on the left and a plain `>` on the right. Always separate the content with `-`:

```
<-ra-mn:n-xpr->
<S-Hr-nfr->
```

The content is a sequence of quadrats. Alphabetic text and toggles are allowed inside. Line breaks, zones and superscripts are not. An enclosure is an element of a quadrat line: it can be followed by `*` or `:`, but it cannot be used in a ligature.

A digit after the letter says how each end is drawn, which allows for a cartouche split over several lines or columns:

```
<1-ra-mn-0>    start only
<0-ra-mn-2>    end only
<s1-Hr-s0>     serekh, start only
```

| Digit | Cartouche, serekh, castle, enclosure | Hwt-sign |
|---|---|---|
| `0` | nothing drawn | unused |
| `1` | normal start | no square |
| `2` | ending part (for a cartouche, the knot) | square in the lower part (text in lines) |
| `3` | | square in the upper part |

The default is `1` at the start and `2` at the end. Digit `3` needs a type letter (`<h3`).

The type written at the end has no effect: only the start token gives the type. A trailing modifier (`<s-A1-s>\R90`) is accepted and ignored.

## 7. Colour and shading

### 7.1. Colour and shading toggles

| Toggle | Effect |
|---|---|
| `$r` | following items are red |
| `$b` | following items are black |
| `#b` | following items are shaded |
| `#e` | following items are no longer shaded |

```
n-$r-Hr-nfr-$b-w
```

A toggle changes the state of the following top-level items, across line breaks and zone limits. `$r-A1-%zone(A2)` makes A2 red.

The state applies to a whole top-level item. A toggle inside a group or a cartouche changes the state of the whole enclosing quadrat: `(A1-$r-B1)` is entirely red, and `<-$r-A1-$b->` has no effect on it. To colour one sign in a group, use `\red`.

### 7.2. Shading

`#` and a list of digits after a quadrat shade parts of it:

```
A1#12        top half
A1:B1#34     bottom half
A1#1234      whole quadrat
```

| Digit | Part |
|---|---|
| `1` | top, start side |
| `2` | top, end side |
| `3` | bottom, start side |
| `4` | bottom, end side |

Digits must be in increasing order. The shading applies to the whole quadrat and comes at its very end: `A1#12*B1` is an error. Quadrat shading, `\shadingN` and the shade signs of [§2.2](#22-special-signs) are three independent mechanisms.

## 8. Philological marks

| Marks | Meaning | Drawn as |
|---|---|---|
| `[[` `]]` | erased signs | `[ ]` |
| `[&` `&]` | editor addition | `< >` |
| `[{` `}]` | editor superfluous | `{ }` |
| `["` `"]` | previously readable | `[\| \|]` |
| `['` `']` | scribe addition | `' '` |
| `[(` `)]` | minor addition | `( )` |
| `[?` `?]` | dubious | half brackets |

In current JSesh, each mark is **a sign**, not a pair of parentheses:

```
n-[[-w-r-]]-a
```

Marks can therefore be left open, overlap, or be used in a quadrat (`]]*B1`). This is deliberate: real texts sometimes open a mark once and close it twice. The reading as parentheses, used by tksesh, is described in [§15.5](#155-philological-marks-as-parentheses-tksesh).

## 9. Alphabetic text, superscripts, comments

### 9.1. Alphabetic text

`+` and a script letter start a text block, and `+s` returns to hieroglyphs:

```
+lThe word +ibook+s-G1-+tjw+s
```

| Letter | Script |
|---|---|
| `l` | latin |
| `b` | bold |
| `i` | italic |
| `t` | transliteration |
| `c` | coptic |
| `g` | greek |
| `h` | hebrew (laid out right to left) |
| `r` | cyrillic |
| any other lowercase letter | kept and written back; drawn with the latin font |

A block ends at a `+` followed by a lowercase letter or by another `+`, or at the end of the input. A new block can therefore follow directly, as `+i` does above. Any other `+` is part of the text. `\+` is a literal `+` and `\\` a literal backslash. Line breaks and tabulations in a block become spaces.

In transliteration, letters are the MdC letters, and `^` makes the next letter uppercase: `+t^nfr+s`.

### 9.2. Superscripts

`|` starts a superscript, used for instance for line numbers. It ends at the next `-`:

```
|12-A1-B1
```

A backslash escapes the character after it: `\-` is a literal hyphen and `\\` a literal backslash. Hence `|a\\-` is the superscript `a\`: the backslash is doubled so that it does not protect the closing hyphen. Superscripts are only allowed at top level, not in a group or a cartouche.

### 9.3. Comments

`++` starts a comment, which ends like a text block:

```
++ checked against the original +s-A1
```

Comments are not displayed, but they are saved back. Comments at the very beginning of a file may be document metadata ([§12](#12-files)).

## 10. Lines, pages and zones

### 10.1. Lines and pages

| Code | Meaning |
|---|---|
| `!` | end of line (end of column in vertical text) |
| `!=150%` | end of line, with the skip to the next line scaled to 150 % |
| `!!` | end of page |

### 10.2. Zones

A zone is an area of the text with its own layout. It sits in the line or column of its parent like a large quadrat:

```
%zone[layout=columns,dir=rl,height=300,frame](A1-A2-!-B1-B2)
```

A zone holds anything a text holds, including line breaks and other zones. Zones are top-level items: they cannot appear in a quadrat, a group or a cartouche.

| Option | Value | Meaning |
|---|---|---|
| `layout` | `lines` or `columns` | orientation of the zone's text |
| `dir` | `lr` or `rl` | writing direction |
| `width` | length | fixed width; for a zone in lines, also the width at which lines wrap |
| `height` | length | fixed height |
| `lineskip` | length | space between lines |
| `colskip` | length | space between columns |
| `separator` | length | thickness of the rules between lines or columns; no rule if unset |
| `quadratwidth` | length | maximum width of quadrats |
| `quadratheight` | length | maximum height of quadrats |
| `frame` | no value | draws a frame around the zone |

Lengths are integers, in percent of the height of a quadrat: `width=1200` is twelve quadrats wide. An unset option is inherited from the enclosing zone, or from the document preferences for an outermost zone. There are three exceptions: without `width` or `height` the zone takes its natural size, and without `separator` there is no rule. Unknown options are ignored and are not saved.

## 11. Forms accepted without effect

These forms are not legacy, but they do nothing yet.

| Form | Status |
|---|---|
| `%quadrat[options](a:b)` | Same as `a:b`. The options are parsed and thrown away, and the `%quadrat` wrapper is not written back. |
| Options of `%hkern` and `%vkern` | Kept as written and saved, but unused. |
| `\?` and unknown modifiers | Kept and saved, but not drawn. |
| Unknown options of `%zone` and `%insert` | Ignored and lost on saving. |

## 12. Files

A JSesh file has the `.gly` extension and is encoded in UTF-8. It starts with a header of comment lines, followed by the MdC text:

```
++JSesh_Info 1.0 +s
++JSesh_page_direction RIGHT_TO_LEFT +s
++JSesh_small_sign_centered false +s
A1-B1-!
```

Each header line is `++`, a property name starting with `JSesh_`, a value, and ` +s`. Property names are in `jsesh.model.constants.JSeshInfoConstants`. They hold the document preferences: page direction and orientation, maximum quadrat size, line and column skip, and so on.

`00_Documents/formats/jseshHeader.txt` describes a JSON header starting with `++JSESH_FILE`. That format was never implemented.

## 13. The form JSesh writes

`MdCModelWriter` produces one spelling for each construct. Texts generated by other tools should use the same one.

| Construct | Written as |
|---|---|
| Separator | always `-`; `:` and `*` without spaces |
| Sign | `=`, code, `\` (mirror), `\N` (scale), `\RN` (rotation), other modifiers, `{{x,y,s}}`, `_` or `__` |
| Rotation | always `\RN` with 0 ≤ N < 360 |
| Scale with a position | in `{{x,y,s}}`, and only if `x` or `y` is not 0; otherwise `\N` |
| Absolute group | `**` |
| Overwrite | `##` |
| Ligature with a group | `^^^` and `&&&` |
| Complete enclosure | `<-...->`, `<S-...->`, `<H-...->`, `<F-...->`, `<G-...->` |
| Partial enclosure | `<1-...-0>`, `<s1-...-s0>` |
| Toggles | `$r`, `$b`, `#b`, `#e` only, and never crossing each other |
| Philological marks | as signs, separated with `-` |
| Text | `+l...+s`, with `\` and `+` escaped, one block per script |
| Superscript | `\|...-`, with `\` and `-` escaped |
| End of line, of page | `!` or `!=N%`, or `!!`, followed by a line break |
| Zone | `%zone[...](...)`, with only the options which are set |

Toggles are closed before each line break, page break and zone, and at the end of the text, then reopened if needed. This comes from HieroTeX compatibility. The closing toggle at the end of a text follows the last item directly, as in `$r-A1$b-`.

---

# Part II. Legacy constructs

Everything in this part is still read, so old texts keep working. Do not use these forms in new texts.

Where the "Origin" column says "not recorded", the code does not say where the form comes from.

## 14. How a legacy file is recognised

`MDCDocumentReader` chooses a dialect (`jsesh.model.constants.Dialect`) from the file name and its first bytes:

| Test, in this order | Dialect | Encoding | Header handling |
|---|---|---|---|
| name ends in `.hie` | `TKSESH` | iso-8859-1 | none |
| starts with `@` | `WINGLYPH` | windows-1252 | first line skipped |
| starts with `++Created by: MacScribe` | `MACSCRIBE` | MacRoman | leading `++` and `+O` lines skipped |
| starts with `++JSesh_Info` | `JSESH1` | UTF-8 | read as document preferences |
| anything else | `JSESH` | system default | none |

A UTF-8 byte order mark is skipped, and sets the encoding to UTF-8. An encoding given to the reader by the caller takes precedence over this table.

Despite its name, `JSESH1` is the format JSesh writes today. `JSESH` means "a file without a header". The dialects `INSCRIBE`, `MDC88`, `MDC2007` and `OTHER` are defined, but no code gives them a behaviour of their own.

Only two things depend on the dialect once the file is read:

- in `TKSESH`, philological marks are parentheses ([§15.5](#155-philological-marks-as-parentheses-tksesh));
- in `MACSCRIBE`, `\r` means `\R` ([§15.2](#152-modifiers)).

If a file does not parse, it is read again line by line. Lines with an error become plain text preceded by an error message, so that nothing is lost and they can be corrected. In that second reading, philological marks are always signs, even for a `.hie` file.

## 15. Legacy forms still read

### 15.1. Sign codes

| Form | Read as | Written back as | Origin |
|---|---|---|---|
| `J1`, `J12` (any code starting with `J`) | `Aa1`, `Aa12` | `Aa1`, `Aa12` | not recorded |
| `Ff2` | `V49A` | `V49A` | not recorded |
| `` ` `` | `Ff1` | `Ff1` | not recorded |
| `A23H`, `A23V` | variants `A23h`, `A23v` when normalised | unchanged, unless normalised | not recorded |
| `UG1M2N3` | tksesh user sign | unchanged | tksesh |

### 15.2. Modifiers

| Form | Read as | Written back as | Origin |
|---|---|---|---|
| `\r1`, `\r2`, `\r3` | rotation by quarter turns: `\R270`, `\R180`, `\R90` | `\R` form | original Manuel |
| `\t1`, `\t2`, `\t3` | the same rotation, mirrored | `\` and the `\R` form | original Manuel |
| `\s1`, `\s2`... | scale divided by √2 at each step (`\s1` is 70 %, `\s2` is 49 %) | `\N` | not recorded |
| `\rN` in a MacScribe file | `\RN`, so `\r90` is 90 degrees | `\RN` | MacScribe |

### 15.3. Operators

| Form | Current form | Notes | Origin |
|---|---|---|---|
| `a&&b` | `a**b` | Same absolute group. | not recorded |
| `t^^w` | `t^^^w` | Same ligature. | not recorded |
| `a#n` | `a##n` | One `#` as overwrite only works right after a sign, and not before signs starting with `b`, `e` or `1`-`4` ([§18.4](#184-context-dependent-tokens)). | MacScribe |
| `$` | `$r`, `$b` | Toggles between red and black. | not recorded |
| `#` before a sign, `-#`, `-#-`, `#-` | `#b`, `#e` | Toggles shading. | not recorded |
| `-#b`, `-#e` | `#b`, `#e` with a separator | The `-` is part of the token. | not recorded |
| `A1#-`, `A1#` followed by spaces | `A1#1234` | After a sign only. | not recorded |

### 15.4. Cartouches

The lexer calls the uppercase forms "old cartouches" and attributes them to MacScribe. `<S`, `<H`, `<F` and `<G`, without a part letter, are also what JSesh writes for a complete enclosure ([§13](#13-the-form-jsesh-writes)), so these four are not obsolete.

| Form | Meaning | Current form |
|---|---|---|
| `<Sb`, `<Fb`, `<Hb` | start only | `<s1-...-s0>` |
| `<Sm`, `<Fm`, `<Hm` | middle: no extremity | `<s0-...-s0>` |
| `<Se`, `<Fe`, `<He` | end only | `<s0-...-s2>` |
| `<b`, `<m`, `<e` | same, for a plain cartouche | `<1-...-0>`, `<0-...-0>`, `<0-...-2>` |

There is no part form for `G`: `<Gb` reads as `<G` followed by the sign `b`. A cartouche opened with one of these forms can be closed with any end token, and cannot be followed by a modifier.

### 15.5. Philological marks as parentheses (tksesh)

In `.hie` files (`philologyAsSigns = false` on the parser), philological marks are parentheses and build a `Philology` element containing a list of quadrats. They must then be balanced, and they count as elements of a quadrat line: `[[A1:B1]]:C1` is one quadrat.

Only the opening mark gives the kind: `[[-A1-"]` is accepted as an erased group.

JSesh writes these documents back as `.hie` in iso-8859-1, with the marks as parentheses. For another output dialect, `MdCModelWriter` surrounds each group with `( )`.

**Known bug:** `LexicalSymbolsUtils.getOpenCodeForPhilology` and `getCloseCodeForPhilology` have no entry for minor additions and dubious readings. In this mode, `[(A1)]` and `[?-A1-?]` are written as `null-A1-null`, which damages the text.

### 15.6. Tab stops and horizontal rules

| Form | Meaning | Origin |
|---|---|---|
| `?N` | the next item is placed at absolute position `N`, in tab units (`tabUnitWidth`) | Winglyph |
| `{lA,B}` | thin horizontal rule from position `A` to `B`, in tab units | not recorded |
| `{LA,B}` | wide horizontal rule | not recorded |

JSesh lays out and saves these items, but no editor command creates them. They are top-level items.

### 15.7. Toggles which are dropped

| Form | Name in the code | Effect |
|---|---|---|
| `?` | `LACUNA` | none |
| `??` | `LINE_LACUNA` | none |
| `^` | `OMIT` | none |

These tokens are accepted, but they do not reach the model. **They are lost when the text is saved.**

### 15.8. Transliteration encoding

In `+t` blocks, the "Utrecht font" encoding is always converted to MdC, whatever the dialect:

| Input | `!` | `@` | `#` | `£` | `%` | `&` | `*`, `ß` | `"` | `c` |
|---|---|---|---|---|---|---|---|---|---|
| Result | `^h` | `^H` | `^x` | `^X` | `^s` | `^t` | `^T` | `^d` | `s` |

So these characters cannot be used literally in transliteration.

## 16. MacScribe forms that are not supported

`00_Documents/notes/compat_macscribe.txt` lists forms found in MacScribe files which JSesh has no equivalent for. They are not implemented. Some are errors, but several are read silently as something else, so a MacScribe file can parse and still be wrong.

| MacScribe form | Meaning in MacScribe, from the notes | What JSesh does |
|---|---|---|
| `.f` | elastic space | half space, then sign `f` |
| `.v`, `.h` | vertical and horizontal half quadrat | half space, then sign `v` or `h` |
| `!/` | justified end of line | line break, then a quarter shade |
| `\u`, `\d` on `"text"` | text above or below, taking no space | kept as an unknown modifier, no effect |
| `A##` alone, `X1##^^D39` | hatched sign | syntax error |
| `#` between signs | normal superposition | read as overwrite, with the limits of [§15.3](#153-operators) |
| `%` | tabulation | syntax error |
| `^b`, `^e`, `^w`, `/^` | arrows | `^` dropped, then sign `b`, `e` or `w`; or `/`, then `^` dropped |
| `+u`, `+d`, `+n` | text above, below, as a note | read as text in an unknown script |
| `+L`, `+O` in the body | | syntax error |
| `k\200\150` | two successive scales | the last value replaces the first |

---

# Part III. Annotated grammar

## 17. Notation and overview

The grammar has two levels. The lexer turns characters into symbols (`MdcSymbolCode`), and the parser builds a literal AST (`jsesh.parser.ast`) from these symbols. `AstModelBuilder` then interprets the AST into the model.

Notation for both levels:

| Notation | Meaning |
|---|---|
| `"x"` | literal text |
| `[x]` in a syntax rule | optional |
| `{x}` in a syntax rule | zero or more |
| `x?`, `x*`, `x+` in a lexical rule | optional, zero or more, one or more |
| `[a-z]` in a lexical rule | character class |
| `UPPER_CASE` | symbol from the lexer |
| `lowerCase` | syntax rule |

## 18. Lexical level

### 18.1. Scanning rules

The lexer has two states:

- **Initial**: the default. White space is not skipped: it produces tokens ([§18.5](#185-spaces-and-separators)).
- **PROPERTIES**: entered by `[` and `{{`, left by `]` and `}}`. White space (space, tab, newline, CR) is skipped.

At each position the lexer takes the **longest match**. If two rules match the same length, the rule declared first in `MdcTokenType` wins. This matters for a few cases only:

- `#` alone: `HASH_ALONE` wins over `SHADING`;
- `o` and `O`: the point signs win over `SIGN_CODE`;
- `\R`: `MODIFIER_R` wins over `MODIFIER_GENERIC`;
- `,`: `COMMA` wins over `UNKNOWN`.

Longest match explains most surprises: `s>` is one token, not a sign then `>`. See [§21](#21-traps).

### 18.2. Character classes

```
DIGIT     = [0-9]
INTEGER   = DIGIT+
UPPER     = [A-Z]
LOWER     = [a-z]
TRUESPACE = [ \t\n\r_]
ESPSO     = (TRUESPACE | "-")*
```

`_` is a kind of space. `ESPSO` lets a line break swallow the following spaces and separators.

### 18.3. Tokens

Initial state, by function. The name on the right is the symbol given to the parser.

```
-- Structure
"!!" ESPSO                                 PAGE_END
"!" ("=" INTEGER "%")? ESPSO               LINE_END     value: the skip, default 100
"-" (TRUESPACE | "-")*                     SEPARATOR
":"                                        COLON
"*"                                        STAR
"("                                        LPAREN
")"                                        RPAREN
","                                        COMMA
"["                                        OPEN_BRACE   enters PROPERTIES
"{{"                                       DOUBLE_LEFT_CURLY   enters PROPERTIES

-- Keywords
"%zone"  "%quadrat"  "%insert"             ZONE  QUADRAT  INSERT
"%hkern" "%vkern"                          HKERN  VKERN

-- Sign operators
"&"                                        AMP
"&&" | "**"                                DOUBLE_AMP
"&&&"                                      LIG_AFTER
"^^" | "^^^"                               LIG_BEFORE
"##"                                       OVERWRITE
"="                                        GRAMMAR

-- Signs (all give the symbol HIEROGLYPH)
SIGN_CODE     = ([A-Z] | "Aa") INTEGER "A"* | [@a-zA-Z0-9]+ | "@" any
SIGN_BACKTICK = "`"
SIGN_SMALL_TEXT = '"' ( [^"\\\]] | "\\" any )* '"'
"o"  "O"  "."  ".."  "//"  "/"  "h/"  "v/"

-- Modifiers (all give the symbol MODIFIER)
"\?"
"\R" "-"? DIGIT*
"\" (UPPER | LOWER)* DIGIT*

-- Toggles (all give the symbol TOGGLE)
"$r"  "$b"  "$"
"?"  "??"  "^"
"-"? "#b"     "-"? "#e"
"-#"  "-#-"

-- Shading: see 18.4
"#" "1"? "2"? "3"? "4"?                    SHADING
"#"     "#-"     "#" " "+

-- Cartouches
"<"                                        BEGIN_OLD_CARTOUCHE
"<" [SFHG]                                 BEGIN_OLD_CARTOUCHE
"<" [SFH] [bme]                            BEGIN_OLD_CARTOUCHE
"<" [bme]                                  BEGIN_OLD_CARTOUCHE
"<" [sfhg] [0123]?                         BEGIN_CARTOUCHE
"<" [012]                                  BEGIN_CARTOUCHE
">"                                        END_CARTOUCHE
[sfhg] [0123]? ">"                         END_CARTOUCHE
[012] ">"                                  END_CARTOUCHE

-- Philological marks: see 18.4
"[[" "[{" '["' "['" "[&" "[(" "[?"         begin
"]]" "}]" '"]' "']" "&]" ")]" "?]"         end

-- Text
"+s"                                       START_HIEROGLYPHS
"+" [a-rt-z+] ( "\+" | [^+] | "+" [^a-z+] )*       TEXT
"|" ( [^-] | "\-" )*                       TEXT_SUPER

-- Legacy
"?" INTEGER                                TAB_STOP
"{l" INTEGER "," INTEGER "}"               HRULE
"{L" INTEGER "," INTEGER "}"               HRULE

-- Space: see 18.5
TRUESPACE                                  WORD_END, or nothing
(TRUESPACE TRUESPACE)+                     SENTENCE_END, or nothing

-- Everything else
any single character                       UNKNOWN (always a syntax error)
```

The closing `+x` of a text block is not part of `TEXT`, and the closing `-` of a superscript is not part of `TEXT_SUPER`: they are read as the next token.

PROPERTIES state:

```
"}}"                                       DOUBLE_RIGHT_CURLY   returns to the initial state
"]"                                        CLOSE_BRACE          returns to the initial state
","                                        COMMA
"="                                        EQUAL
INTEGER                                    INTEGER
[a-zA-Z_] [a-zA-Z0-9_]*                    IDENTIFIER
any single character                       UNKNOWN
```

There is no negative integer in this state, so `{{-10,0,100}}` and `width=-3` are errors.

### 18.4. Context-dependent tokens

**`#`.** The symbol depends on whether the previous symbol is a `HIEROGLYPH`. A modifier or a `{{...}}` in between counts as "not after a sign".

| Input | Right after a sign code | Elsewhere |
|---|---|---|
| `#` | `OVERWRITE` | `TOGGLE` (shading) |
| `#-`, `#` and spaces | `SHADING` `#1234` | `TOGGLE` (shading) |
| `#1`...`#1234` | `SHADING` | `SHADING` (a syntax error at the start of an item) |
| `##` | `OVERWRITE` | `OVERWRITE` |
| `#b`, `#e`, `-#b`, `-#e` | `TOGGLE` | `TOGGLE` |
| `-#`, `-#-` | `TOGGLE` | `TOGGLE` |

**Philological marks.** With `philologyAsSigns = true` (the default), they are `HIEROGLYPH` symbols. With `false` (tksesh), they are `BEGIN_PHIL` and `END_PHIL`. The syntax rule `philology` below can only match in the second case.

**Cartouche values.** `<s2` gives the type `s` and the part `2`. Defaults are type `c` and part `1` at the start, and type `c` and part `2` at the end. `<S`, `<b`... give a type and a part letter instead.

**Modifier values.** The name is the letters after `\` and the value the integer after them, if any. `\` gives the name "" with no value, and `\50` gives the name "" with the value 50.

### 18.5. Spaces and separators

A white-space token gives a symbol only right after `HIEROGLYPH`, `MODIFIER` or `DOUBLE_RIGHT_CURLY`. Everywhere else it is discarded.

- One `TRUESPACE` gives `WORD_END`, and two or more give `SENTENCE_END`.
- `_` and `__` are therefore just the visible spelling of the same thing.
- A newline counts: `A1`, newline, `B1` gives a word end. CR LF counts as two characters, so **each line end after a sign in a Windows file is a sentence end**.
- `SEPARATOR` absorbs a whole run of `-` and spaces, but a space *before* it is a separate token: `A1 - B1` marks a word end.

The parser never requires a `SEPARATOR` between items: `A1 B1`, `A1!B1` and `$rA1` are all valid. This is why `.f` reads as two quadrats without an error.

## 19. Syntax

The grammar below is equivalent to `MDCParser`, a recursive-descent parser with one symbol of lookahead. It has no error recovery: the first error stops the parse with an `MDCSyntaxError`. Option lists are read by a general rule, and [§20](#20-rules-outside-the-grammar) gives the meaning of the options.

### 19.1. Text and top-level items

```ebnf
mdcfile  ::= topitems EOF ;

topitems ::= { [SEPARATOR] topitem } [SEPARATOR] ;

topitem  ::= shadedCadrat
           | zone
           | TEXT | TEXT_SUPER | START_HIEROGLYPHS
           | TOGGLE
           | LINE_END | PAGE_END
           | TAB_STOP | HRULE ;
```

- All elements are optional: an empty string is a valid text.
- `START_HIEROGLYPHS` (`+s`) builds nothing. It only closes a text block.
- A `TEXT` with the script `+` is a comment. Other `TEXT` blocks become one model element per character.

### 19.2. Zones and options

```ebnf
zone       ::= ZONE [optionList] LPAREN topitems RPAREN ;

optionList ::= OPEN_BRACE option { COMMA option } CLOSE_BRACE ;

option     ::= IDENTIFIER [ EQUAL ( IDENTIFIER | INTEGER ) ] ;
```

- A zone's content is `topitems`, which is why zones nest and accept line breaks.
- An option list cannot be empty: `%zone[](A1)` is an error.
- An option without a value is a flag, such as `frame`.

### 19.3. Quadrats

```ebnf
shadedCadrat   ::= cadrat [SHADING] ;

cadrat         ::= verticalStack
                 | QUADRAT [optionList] LPAREN verticalStack RPAREN ;

verticalStack  ::= horizontalList { COLON horizontalList } ;

horizontalList ::= horizontalElement { STAR horizontalElement } ;

horizontalElement ::= cartouche
                    | complexLigature
                    | innerGroup ;
```

- `SHADING` comes after the whole quadrat, so it cannot appear before `*` or `:`.
- The precedence of `-`, `:` and `*` comes from this nesting.
- `%quadrat` contains a single stack, not a list of quadrats. `%quadrat(A1-B1)` is an error.

### 19.4. Groups

```ebnf
complexLigature ::= hieroglyph LIG_AFTER innerGroup
                  | innerGroup LIG_BEFORE hieroglyph [ LIG_AFTER innerGroup ] ;

innerGroup    ::= hieroglyph
                | ligature
                | absoluteGroup
                | overwrite
                | subgroup
                | insert
                | kerning
                | philology ;

ligature      ::= hieroglyph AMP hieroglyph { AMP hieroglyph } ;

absoluteGroup ::= hieroglyph DOUBLE_AMP hieroglyph { DOUBLE_AMP hieroglyph } ;

overwrite     ::= hieroglyph OVERWRITE hieroglyph ;

subgroup      ::= LPAREN basicitems RPAREN ;

insert        ::= INSERT optionList LPAREN basicitems COMMA basicitems RPAREN ;

kerning       ::= ( HKERN | VKERN ) [optionList] ;

philology     ::= BEGIN_PHIL basicitems END_PHIL ;
```

- The parser reads a leading sign first, then looks at the next symbol to choose the rule. The rules for `ligature`, `absoluteGroup`, `overwrite` and `complexLigature` all start that way.
- The middle of a complex ligature must be a sign. `t^^%insert[dir=c](A1,B1)` is an error.
- `LIG_AFTER` alone needs a sign before it: `(A1)&&&w` is an error, but `(A1)^^w` is valid.
- `innerGroup` is not recursive through the operators, so operators do not chain or mix: `A1&B1**C1`, `a^^b^^c` and `a#n#m` are errors. Use parentheses where the operands allow it.
- The operands of `&`, `**` and `##` are signs: `A1&(B1)` is an error.
- `kerning` is a group, which is why it may stand alone in a line or a quadrat.
- `subgroup` and the arguments of `insert` may be empty: `()` and `%insert[dir=c](,A1)` are accepted.

### 19.5. Cartouches and quadrat lists

```ebnf
cartouche  ::= BEGIN_CARTOUCHE basicitems END_CARTOUCHE modifiers
             | BEGIN_OLD_CARTOUCHE basicitems END_CARTOUCHE ;

basicitems ::= { [SEPARATOR] basicitem } [SEPARATOR] ;

basicitem  ::= shadedCadrat
             | TEXT | START_HIEROGLYPHS
             | TOGGLE ;
```

- `basicitems` is `topitems` without what needs a page: no line break, zone, superscript, tab stop or rule. It is used everywhere a group holds quadrats.
- Cartouches nest through `basicitems`, and may be empty.
- `cartouche` is not an `innerGroup`, so it cannot enter a ligature: `<A1>^^w` is an error.
- The modifiers after a cartouche are parsed and thrown away.
- The plain `<` is a `BEGIN_OLD_CARTOUCHE`, so `<A1>\R90` is an error, while `<s-A1-s>\R90` is accepted.

### 19.6. Signs

```ebnf
hieroglyph ::= [GRAMMAR] HIEROGLYPH modifiers [position] [ WORD_END | SENTENCE_END ] ;

modifiers  ::= { MODIFIER } ;

position   ::= DOUBLE_LEFT_CURLY INTEGER COMMA INTEGER COMMA INTEGER DOUBLE_RIGHT_CURLY ;
```

- The order is fixed: `A1{{1,2,3}}\R90` and `A1_\R90` are errors.
- `GRAMMAR` belongs to the sign, so `=(A1)` is an error.
- `WORD_END` belongs to the sign too, so a space before `*` or `:` is valid and sets a word end on the sign: `A1 *B1` is written `A1_*B1`.

## 20. Rules outside the grammar

**`%insert`.** The option list is required. `dir` is required and must be one of the nine codes of [§5.3](#53-insertions). `target` must be `1` or `2`. These checks are in the parser and give an `MDCSyntaxError`. `dw` and `dh` are read by `AstModelBuilder`. Other options are ignored.

**`%zone`.** Options are read by `AstModelBuilder`. Unknown options and unknown values of `layout` and `dir` are ignored. A size, quadrat size or separator of 0 is unset.

**Cartouches.** The type comes from the start token alone. The end token only gives the end part, and plain `>` gives 2. For old-style starts, the end token gives nothing at all: the part letter sets both ends.

**Modifiers.** Interpretation is in `ModifiersList.includeModifier`:

| Name | Value | Interpretation |
|---|---|---|
| empty | none | mirror |
| empty | N | scale |
| `R` | N | angle = N modulo 360 |
| `r` | N | angle = (360 − 90 × N) modulo 360 |
| `t` | N | same as `r`, and mirror |
| `s` | N | scale = 100 / (√2)^N, rounded down |
| other | any | stored; one entry per name and arity |

**Philology, tksesh mode.** The kinds of the opening and closing marks are not compared. Only the opening kind is kept.

**Toggles.** `AstModelBuilder` keeps a current state while reading the items in order, and gives it to each top-level item once the item has been read. This is why a toggle inside a group affects the whole enclosing quadrat and everything after it. `LACUNA`, `LINE_LACUNA` and `OMIT` change nothing.

**Superscripts.** The escapes `\\` and `\-` are resolved in the model builder. The lexer only knows about `\-`, so a superscript ending in a backslash, written `\\-`, does not stop at that `-`.

## 21. Traps

All of these were checked against the parser.

| Input | Actual reading | Write instead |
|---|---|---|
| `<-n-s>` | Cartouche containing only `n`: `s>` is an end token, and the sign `s` is lost. The same goes for `f>`, `h>`, `g>`, `1>`, `2>`. | `<-n-s->` |
| `<s-t->` | Serekh containing `t`, not a cartouche starting with `s`. The same goes for `<f`, `<h`, `<g`, `<b`, `<m`, `<e`, `<0`, `<1`, `<2`. | `<-s-t->` |
| `<Gb-A1->` | `G` enclosure containing `b` and `A1`. | `<g1-A1-g0>` |
| `a#b` | Sign `a`, then the toggle `#b`: the sign `b` disappears, and shading is switched on. | `a##b` |
| `A1#21` | `A1#2`, then the sign `1`. | `A1#12` |
| `A1-#12` | Toggle `-#`, then the sign `12`. | `A1#12` |
| `A1#` at the end of the input | Syntax error: this is an overwrite with nothing after it. | `A1#1234` |
| `A1\R90#-B1` | Toggle, not shading: the previous symbol is a modifier. | `A1\R90#1234-B1` |
| `v/A1`, `h/` | `v/` and `h/` are shade signs, even if `v` or `h` was meant as a sign. | `v-/`, `h-/` |
| `??10` | Toggle `??`, then the sign `10`. `?10` is a tab stop. | |
| `$c` | Toggle `$`, then the sign `c`. | `$r-c` |
| `A1!=150` | Line break, then the sign `150` marked as grammatical: the `%` is required. | `A1-!=150%` |
| `+Lfoo+s` | Syntax error: the script letter must be lowercase. | `+lfoo+s` |
| `A1   B1` | Sentence end: any run of two or more spaces counts as one. | |
| `(\|sup)`, `(A1-!-B1)`, `(A1-%zone(B1))` | Syntax errors: these items are top-level only. | |
| `<s-A1-s>\R90 B1` | Syntax error: the space after the modifier gives a `WORD_END` outside a sign. | `<s-A1-s>\R90-B1` |
| `"a]b"` | Syntax error: `]` must be escaped. | `"a\]b"` |

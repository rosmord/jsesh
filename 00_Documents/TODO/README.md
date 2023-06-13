# TODO

## Current

- [ ] Introduce/Rename a specific class to represent the ManuelDeCodageCore : Gardiner (slightly extended) and translitteration signs, plus the possibility to normalize. This object **can be a singleton**.
- [ ] [Remove singletons](./toto20230607-001.md)
- [ ] Improve architecture of `jsesh.hieroglyphs` ; add `jsesh.hieroglyphs.common` ;
- [ ] outsource the creation of `PossibilitiesList` to something more linked with the widgets workflow ; **note that those list depend on the available signs, i.e. on the glyph configuration**.
- [ ] rename `JSimplePalette` (which is not a simple palette) ; use MigLayout for `JSimplePalette` instead of Matisse ;

## Planned

### Simple

- [ ] [Remove dependency on com.jgoodies.forms](./todo20200622-001.md)
- [ ]  move the palette to its own module ;
- [ ]  move the glossary **editor** to its own module too


### Complex
- [ ] [Simplify view architecture](./todo20200622-002.md)
- [ ] [Simplifies the use of MDCPosition](./todo20200622-003.md)
- [ ] [Harmonize the I18n system and use consistent naming](./todo20200622-004.md)

## Done

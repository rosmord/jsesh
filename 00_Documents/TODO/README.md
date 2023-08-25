# TODO

## Current
- [ ] Check what to do of `isCanonicalCode` in `GardinerCode`.
- [ ] [Remove singletons](./toto20230607-001.md)
- [ ] Improve architecture of `jsesh.hieroglyphs` ; add `jsesh.hieroglyphs.common` ;
- [ ] outsource the creation of `PossibilitiesList` to something more linked with the widgets workflow ; **note that those list depend on the available signs, i.e. on the glyph configuration**.
- [ ] rename `JSimplePalette` (which is not a simple palette) ; use MigLayout for `JSimplePalette` instead of Matisse ;
- [ ] remove `depth` in `Layout` ; hide `Layout` if possible.
- [ ] Check RTF export when update is done...
- [ ] hide most implementations of listeners/observers in inner classes ; use `PropertyChangeSupport` when possible.
- [ ] consider what to do with `JMDCEditorWorkflow::buildAbsoluteGroup` ; could we make something more generic, like `transformSelection(Function<List<TopItem> l1,List<TopItem> l1>)` ?
## Planned

### Simple

- [ ] [Remove dependency on com.jgoodies.forms](./todo20200622-001.md)
- [ ] move the palette to its own module ;
- [ ] move the glossary **editor** to its own module too
- [ ] Use the GardinerCode class instead of String in every place where it's meaningful (basically simple, but tedious)
- [ ] Move `LargeFontImporter` out of the main JSesh package. It's an utility application.


### Complex
- [ ] [Simplify view architecture](./todo20200622-002.md)
- [ ] [Simplifies the use of MDCPosition](./todo20200622-003.md)
- [ ] [Harmonize the I18n system and use consistent naming](./todo20200622-004.md)

## Done

- [x] Decided that after all, the `ManuelDeCodage` class would represent the core ManuelDeCodage : Gardiner (slightly extended) and translitteration signs, plus the possibility to normalize. This object **can be a singleton**.


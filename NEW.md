# What's new in JSesh (VERSION NUMBER HERE)


## Software modifications

- when text is selected, the "explode group" command will act on selection instead of acting on the text before caret ;
- the hieroglyphic palette is no longer kept on top of other windows ;

## Fonts and encoding

- Now, R8A and nTrw use the exact same shape
- MdC "nn" has now a "Gardiner-like" code : M22B ; it will be normalized as such.
- nTrw and M22B will now be "normalized" as R8A and M22B

**normalized** means that exports which try to give a canonical rendering of MdC text (and which remove phonetical codes and use Gardiner codes instead) will use the Gardiner code version.


### Planned for this version

- A41 and A42 have now straight beards and not divine beards ;
- B7 has an Uraeus according to Gardiner shape ;
- S44 has now the shape in Gardiner grammar ;
- hieroglyphica shape for S44 is now S44VARA 
- some library improvement.
- (other font cleanup)

### Code cleaning

Normally without interest for users. Might break some code if linked on latest JSesh libraries.

- class renaming :
- removing the dependency on `jsesh.hieroglyphs.graphics.DefaultHieroglyphicFontManager` as a **singleton** in most places ; this makes the code a bit more complex to use, but cleaner.


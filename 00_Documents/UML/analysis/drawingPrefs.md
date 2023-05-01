# Improving Drawing Preferences and related classes

Currently, JSesh uses way too many singletons. Parts of it should also be simpler. 
But what we want to do right now is creating a simpler system for Drawing Preferences.

First, we should classify them and see how we can get a better
structure.

We currently use interfaces (which might be a good idea).

The following properties are used :
## Content of DrawingPreferences

The interface defines *getters* **and** *setters* for the following
properties :

- tabUnitWidth
- standardSignHeight
- maxCadratHeight
- maxCadratWidth
- fineStroke
- wideStroke
- fineLineWidth
- wideLineWidth
- philologyWidth
- smallSignCentered
- largeSignSizeRatio
- smallSignSizeRatio
- lineSkip
- smallSkip
- columnSkip
- pageLayout
- textDirection
- textOrientation
- blackColor
- cursorColor
- grayColor
- redColor
- backgroundColor
- tagColor(String tag)
- cartoucheKnotLength
- cartoucheLineWidth
- cartoucheLoopLength
- cartoucheMargin
- enclosureBastionDepth
- enclosureBastionLenght
- HwtSmallMargin
- HwtSquareSize
- serekhDoorSize
- cartoucheLineWidth
- getFont(code) (non hieroglyphic fonts)
- getSuperScriptFont()
- translitUnicode
- yodChoice
- smallBodyScaleLimit
- graphicDeviceScale (see doc)
- isPaged
- shadingStyle
- justified

## DrawingSpecification

DrawingSpecification mainly adds computed methods, and, most importantly, the HieroglyphsDrawer.

### Computed methods
- copy()
- signScale()
- buildCartoucheStroke()
- fontRenderContext
- superScriptDimensions(String)
- textDimensions(code, text)
- extractDocumentPreferences
- applyDocumentPreferences
- getTransliterationEncoding()
- getColorForProperty

### HieroglyphsDrawer
- hieroglyphsDrawer

### Out-of-place 
- gardinerQofUsed (out of place, should be in DrawingPreferences)


## Improvements

One is almost easy : separate getters and setter...

Also, organise properties by theme :

### Layout properties


#### Internal Cadrant information
- standardSignHeight
- maxCadratHeight
- maxCadratWidth
- smallSignCentered
- smallSkip

#### Sign oriented information
- largeSignSizeRatio
- smallSignSizeRatio
- smallBodyScaleLimit
- graphicDeviceScale (see doc) : only used to compute A1 theoretical height, and decide if we should use *small body* hieroglyphic fonts or normal ones. Perhaps not relevant and out of place. It might be a good idea to separate such properties
  (i.e. description of output device) from actual preferences.

#### Skips (outside cadrats)
- lineSkip
- columnSkip
- tabUnitWidth

#### Pagination
- pageLayout
- isPaged
- justified (to be deprecated)
- textDirection should be an area attribute.
- textOrientation

### Line drawing properties

- fineStroke
- wideStroke
- fineLineWidth
- wideLineWidth

### Colors

- blackColor
- cursorColor
- grayColor
- redColor
- backgroundColor
- tagColor(String tag)
- shadingStyle

### Ecdotic and Cartouches symbols
 
#### Ecdotic
- philologyWidth

#### Cartouches
- cartoucheKnotLength
- cartoucheLineWidth
- cartoucheLoopLength
- cartoucheMargin
- cartoucheLineWidth

#### Enclosures

- enclosureBastionDepth
- enclosureBastionLenght

#### Hwt signs
- HwtSmallMargin
- HwtSquareSize

#### Serekh

- serekhDoorSize

### Fonts and latin script

- getFont(code) (non hieroglyphic fonts)
- getSuperScriptFont()
- translitUnicode
- yodChoice
- gardinerQofUsed (moved from drawingSpecs)

## PageLayout

PageFormat (A4, etc...)

textWidth; // In fact, text width ?
textHeight; // In fact, text height.
leftMargin;
topMargin;

## About HieroglyphsDrawer

Note : we use preferences there, but what is actually in the preferences might be the **folder** containing the signs,
not the whole object.

`HieroglyphsDrawer` is currently used :

- To change smallBodyUsed: should not be done at this level !!!!
  - either the drawer itself chooses sign body, or it's an attribute passed to the method.

- to get the current height of A1

- retrieved directly from VisitComplexLigatures from SimpleLayout and visitHieroglyph (quite logical)!

- to draw (normal!)

- to check that a sign is drawable

- to get the Area occupied by a sign (for group editor clicks)

- to get the BBox of a sign

- to get the ligatureZone (messed up!)

- to get the "groupUnitLength" (i.e. 1/1000 of A1 size)

## TO DO:

1. create a class which represent a HieroglyphDrawing (or something similar) :
  - it should have a BBox ;
  - ligature zones
  - area

2. A1 size should probably be extracted... from A1 ? or should be 18 ?

3. smallBodyUsed should be passed as an argument (DrawingOptions ?), not kept in memory.   

## Problems with the `GroupEditor`

Currently, if (and apparently only if) the size of A1
is not 18, the Group Editor layout doesn't work correctly.

Only the **Move** function is broken.

Anyway, the various dimensions used by the document layout system
should be redesigned.

This being said :

- we can have a "standard height" for signs in the text ;
- a max cadrant height, which might be different from the previous one ;
  For texts in columns, for instance, it can be used to allow cadrants largers than
  « normal » sign height.

See :

- GroupEditorDrawingPreferences (with a suspicious looking scale variable) ;

## Coordinate systems

It might be a good idea to use Points2D as coordinates in Graphics2D,
and another type to designate points in model Space. 

## Some problems

### Relatively illogical code

In group editor, we have :

~~~java
public void setDrawingSpecification(DrawingSpecification drawingSpecifications) {
    // Ensure drawing specs are left to right.
    // A better system for drawingSpecs (with immutable and DrawingSpecificationProperty) would be nice.
    DrawingSpecification specs = drawingSpecifications.copy();
    specs.setTextDirection(TextDirection.LEFT_TO_RIGHT.LEFT_TO_RIGHT);
    groupEditorDrawingPreferences.setDrawingSpecifications(specs);
    repaint();
    revalidate();
}
~~~

1. we would like the whole repaint/revalidate stuff to be somehow automated ; an **event** might convey the notion of having some effect on the layout or not (or possibly, we consider any change can alter the layout)
2. the whole stuff on textdirection is weird.


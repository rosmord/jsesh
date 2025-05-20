# Improving Drawing Preferences and related classes


## State of JSesh as of version 7.x

Currently, JSesh uses way too many singletons. Parts of it should also be simpler. 
But what we want to do right now is creating a simpler system for Drawing Preferences.

First, we should classify them and see how we can get a better
structure.

We currently use interfaces (which might be a good idea).

The following properties are used :
### Content of DrawingPreferences

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

### DrawingSpecification

DrawingSpecification mainly adds computed methods, and, most importantly, the HieroglyphsDrawer.

#### Computed methods
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

#### HieroglyphsDrawer
- hieroglyphsDrawer

#### Out-of-place 
- gardinerQofUsed (out of place, should be in DrawingPreferences)


## Analysis

One is almost easy : separate getters and setter...

Also, organise properties by theme :
### Logical grouping of properties
#### Layout properties


##### Internal Cadrant information
- standardSignHeight
- maxCadratHeight
- maxCadratWidth
- smallSignCentered
- smallSkip

##### Sign oriented information
- largeSignSizeRatio
- smallSignSizeRatio
- smallBodyScaleLimit
- graphicDeviceScale (see doc) : only used to compute A1 theoretical height, and decide if we should use *small body* hieroglyphic fonts or normal ones. Perhaps not relevant and out of place. It might be a good idea to separate such properties
  (i.e. description of output device) from actual preferences.

##### Skips (outside cadrats)
- lineSkip
- columnSkip
- tabUnitWidth

##### Pagination
- pageLayout
- isPaged
- justified (to be deprecated)
- textDirection should be an area attribute.
- textOrientation

#### Line drawing properties

- fineStroke
- wideStroke
- fineLineWidth
- wideLineWidth

#### Colors

- blackColor
- cursorColor
- grayColor
- redColor
- backgroundColor
- tagColor(String tag)
- shadingStyle

#### Ecdotic and Cartouches symbols
 
##### Ecdotic
- philologyWidth

##### Cartouches
- cartoucheKnotLength
- cartoucheLineWidth
- cartoucheLoopLength
- cartoucheMargin
- cartoucheLineWidth

##### Enclosures

- enclosureBastionDepth
- enclosureBastionLenght

##### Hwt signs
- HwtSmallMargin
- HwtSquareSize

##### Serekh

- serekhDoorSize

#### Fonts and latin script

- getFont(code) (non hieroglyphic fonts)
- getSuperScriptFont()
- translitUnicode
- yodChoice
- gardinerQofUsed (moved from drawingSpecs)

#### PageLayout

- PageFormat (A4, etc...)
- textWidth; // In fact, text width ?
- textHeight; // In fact, text height.
- leftMargin;
- topMargin;


### UML View for the analysis

**Note : here, a red square doesn't mean *private* but *problematic***
~~~plantuml
@startuml
skin rose
hide empty members

class GroupLayoutSpecifications {
  standardSignHeight
  maxCadratHeight
  maxCadratWidth
  smallSignCentered
  smallSkip  
}

class OutOfCadratSkipsSpecifications {
  lineSkip
  columnSkip
  tabUnitWidth
}

class PaginationSpecification {
  pageLayout
  isPaged
  justified
  textDirection
  textOrientation
}


class PageLayout {
  PageFormat
  textWidth
  textHeight
  leftMargin
  topMargin
}



class EcdoticSpecifications {
  philologyWidth  
}

class CartoucheSpecifications {
    cartoucheKnotLength
    cartoucheLineWidth
    cartoucheLoopLength
    cartoucheMargin    
}

class EnclosureSpecifications {
    enclosureBastionDepth
    enclosureBastionLenght
}

class HwtSignSpecifications {
    HwtSmallMargin
    HwtSquareSize
}

class SerekhSpecifications {
  serekhDoorSize
}

class NonHieroglyphicFontSpecifications {
    - getFont(code) (non hieroglyphic fonts)
    -getSuperScriptFont()
    translitUnicode
    yodChoice
    gardinerQofUsed
}

class LineDrawingProperties {
  fineStroke
  wideStroke
  fineLineWidth
  wideLineWidth
}

class ColorSpecifications {
    blackColor
    cursorColor
    grayColor
    redColor
    backgroundColor    
    shadingStyle
    -tagColor(String tag)
}

class SignSpecifications {
  largeSignSizeRatio
  smallSignSizeRatio
  smallBodyScaleLimit
  -graphicDeviceScale
}
@enduml
~~~

### Current uses of drawing specifications

#### GroupEditor

- computation of glyphArea 
- building views
- drawing views
- **getGroupUnitLength()**

#### ImageIconFactory

#### mdcview

- page layout
- 


## Description of various problems


### About HieroglyphsDrawer

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

#### TO DO:

1. create a class which represent a HieroglyphDrawing (or something similar) :
  - it should have a BBox ;
  - ligature zones
  - area

2. A1 size should probably be extracted... from A1 ? or should be 18 ?

3. smallBodyUsed should be passed as an argument (DrawingOptions ?), not kept in memory.   


### Problems with the `GroupEditor`

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

### Coordinate systems

It might be a good idea to use Points2D as coordinates in Graphics2D,
and another type to designate points in model Space. 

### Grain of DrawingSpecifications

`DrawingSpecifications` are used both :

- to describe specifications for a given document ;
- and (maybe, we need to check) to describe micro-local arrangement of signs. When we add a system for mixing columnar and linear layout, in any case, we will meet the problem.

**It seems that we need to differentiate global setting and local one.**

## Conception of the new system

If we decide to use *records* for most specifications, changing them requires us to keep them in small groups, for we will need to copy each field when creating a new record.

- for Preference editors, we only need to isolate the data which is likely to be edited from the data which is not. That way, we will minimize work. But usually, in preference editions, **all values are copied back,** so we don't really have a problem here

- for other cases, we need to see what's up.


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



### UML representation and TODO

possibly problematic uses :
- The Editor should send back TextDirection and Orientation directly (obviously taking them from the data)
- Editor Drawer uses drawer.getPositionForPoint() which expects a drawing specification
- paintComponent uses 

~~~plantuml
@startuml
title Work in progress 
skin rose
hide empty members

class RenderingSpecifications {
		smallSignsCentered : boolean		
}

class PageSpecifications {
  PageFormat
  textWidth
  textHeight
  leftMargin
  topMargin
}

note left of PageSpecifications::PageFormat
We should probably use something 
not based on `java.awt`
end note

class LayoutSpecifications {
  isPaged
  justified
  textDirection
  textOrientation
}


class GroupsLayoutSpecifications {
  standardSignHeight
  maxCadratHeight
  maxCadratWidth
  smallSignCentered
  smallSkip  
}

class StrokeSpecifications {
		fineLineWidth
		fineLigneHeight
		shadingStyle : ShadingStyle
}


class OutOfCadratSkipsSpecifications {
  lineSkip
  columnSkip
  tabUnitWidth
}


class EcdoticSpecifications {
  philologyWidth  
}

class CartoucheSpecifications {
    cartoucheKnotLength
    cartoucheLineWidth
    cartoucheLoopLength
    cartoucheMargin
    cartoucheLineWidth
}

class EnclosureSpecifications {
    enclosureBastionDepth
    enclosureBastionLenght
}

class HwtSignSpecifications {
    HwtSmallMargin
    HwtSquareSize
}

class SerekhSpecifications {
  serekhDoorSize
}

class FontsSpecifications {
		translitUnicode : boolean
		yodChoice
    gardinerQofUsed,
		plainFont, 
		boldFont,
		italicFont,
		translitterationFont
}


class ColorSpecifications {
    blackColor
    cursorColor
    grayColor
    redColor
    backgroundColor    
    colorMap
}

class SignDrawingSpecifications {
  largeSignSizeRatio
  smallSignSizeRatio
  smallBodyScaleLimit
}

class DeviceSpecifications {
  graphicDeviceScale
}

RenderingSpecifications --> GroupsLayoutSpecifications
RenderingSpecifications --> ElementDrawingSpecifications
RenderingSpecifications --> PageSpecifications 
RenderingSpecifications --> StrokeSpecifications 


ElementDrawingSpecifications --> CartoucheSpecifications
ElementDrawingSpecifications --> EcdoticSpecifications
ElementDrawingSpecifications --> EnclosureSpecifications
ElementDrawingSpecifications --> HwtSignSpecifications
ElementDrawingSpecifications --> SerekhSpecifications

@enduml
~~~

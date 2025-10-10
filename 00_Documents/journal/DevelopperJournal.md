# JSesh developer journal

This journal should only be edited and modified in the Development branch.

**TODO**

When the software compiles, replace all variable named "drawingSpecifications" by jseshStyle.

## 2025/10/10

- (cleaned up master branch), probably not the good time to do this; merged both.
- how to proceed now:
  - start with `mdcDisplayer`
    - fix `layout`, `draw` and `drawingElements`
      - LineLayout and ColumnLayout
      - QuadratLayout
      - 
    - then move to components and clipboard
  - then fix the rest of jsesh module
  - then the rest of the world.

## 2025/10/08


The current organisation of drawingspecification was not really helpful. It's not obvious to find a given information.
We have made larger groups.

- we keep drawingSpecifications as a name until everything compiles, then it will be jseshStyle.
- we need to replace the lines:

  ~~~java
  drawingSpecifications.getHieroglyphsDrawer().getGroupUnitLength();
  ~~~

  Basically, HieroglyphsDrawer should not be stored in drawingSpecifications (or should they)?


- we have to solve the problem of FontRenderContext. Originaly, it's stored in drawingspecification:

~~~java
FontRenderContext fontRenderContext = drawingSpecifications.getFontRenderContext();
~~~

Now :

- it's initialized with :

~~~java
fontRenderContext = new FontRenderContext(null, true, true);
~~~

So all FontRenderContexts are currently the same.

We **might** want to change it, but it's not probably not correctly placed in drawingSpecifications.

Generally, it's requested from the `Graphics2D` object.


## 2025/10/07

Problem to solve: should the drawing specifications depend on Swing? or should they be stand alone?

- in the first case, some things are easier to write and probably more efficient.
- in the second case, we can more easily move to non-swing rendering if needed later.
- we keep the newEdit package for now. It will probably move to a branch of its own once we have finished this update.
- our new architecture for specification doesn't play very well with DocumentPreferences. It might be interesting to try to fit the two. Currently, it's a blatant violation of the Demeter principle.

## 2025/10/06

- We freeze the structure of the display preferences (`RenderingParameters`) ;
- we will then proceed to write the code ;
- and then, we will refactor them if needed.
- we have removed computed data from the drawingspecifications. They will be provided by helper functions when needed.
-  In the current version, the defaults for pagespec are computed from the other specifications. The whole specification system should be rethought to use independent values.
-  DocumentPreferences are using an outdated architecture. We should move to a record based one.

## 2023/06/07

## 2023/06/06

Analysis and classification of current problem. Planning. See TODO about removing singletons.


## 2023/05/19

We might introduce the notion of BasicMdC codes for codes :

- which appear in Gardiner's grammar ;
- or which have an official phonetic code.

## 2023/05/15

Back to JSesh. Considering :

- simplifying HieroglyphFontManager (which might be a simple code to shape repository), to remove its possible dependency on HieroglyphDatabase ?
- think of introducing a Facade to the whole hieroglyph system in this case.

## 2023/04/25

Well, the mess of singleton is also linked to the question of preferences (not preference files, but in-memory preferences) in the software. 

- investigate variants of Observable and Reactive patterns outside of Swing
- look how it's done in other softwares...

The current question is the granularity of preferences. Should it be large (say, setPreferences/getPreferences, with an immutable 
preferenceValue object, and possibly a preferenceProperty which could be shared), or fine-grained (with a mutable preference object).


**anyway, the first step will be to make everything explicit at constructor level**. We will only use `new` when absolutely mandatory.


## 2023/04/20

Working on cleaning up the mess of Singletons. 

### Hieroglyphic fonts and database

Currently, we have the following system :

~~~plantuml
@startuml
skin rose
interface HieroglyphDatabaseInterface {
    getCanonicalCode(code)
    getCodesForFamily(family, includeVariants)            
    getDescriptionFor(String code)
    getFamilies()
    getPossibilityFor(phoneticValue, level)
    getSignsContaining(code)
    getSignsIn(code)
    getSignsWithTagInFamily(currentTag, familyName)
    getSignsWithoutTagInFamily(familyName);
    getTagsForFamily(familyName)
    getTagsForSign(gardinerCode)
    getValuesFor(gardinerCode)
    getVariants(String code)
    getVariants(String code, variantTypeForSearches)
    getTransitiveVariants(String code, variantTypeForSearches)
    isAlwaysDisplayed(code)
    getCodesStartingWith(code)
    getSuitableSignsForCode(code)
}

class ManuelDeCodage <<singleton>> {
    getTallNarrowSigns()
    getLowBroadSigns()
    getLowNarrowSigns()
    getCanonicalCode(code)
    isKnownCode(code)
    getBasicGardinerCodesForFamily(familyCode)

}

class SimpleHieroglyphDatabase extends HieroglyphDatabaseInterface {
  SimpleHieroglyphDatabase(HieroglyphicFontManager, ManuelDeCodage)
}

HieroglyphicFontManager <- SimpleHieroglyphDatabase
SimpleHieroglyphDatabase -> ManuelDeCodage


interface HieroglyphicFontManager {
	get(code) : ShapeChar
  getSmallBody(code)  : ShapeChar
  getCodes()	
	hasNewSigns()
} 

HieroglyphicFontManager <|-- DefaultHieroglyphicFontManager 
HieroglyphicFontManager <|-- DirectoryHieroglyphicFontManager
HieroglyphicFontManager <|-- CompositeHieroglyphicFontManager
HieroglyphicFontManager <|-- ResourcesHieroglyphicFontManager 


class DefaultHieroglyphicFontManager <<singleton>> {}
@enduml
~~~

Possible changes :

- replace `SimpleHieroglyphicDatabase`, or hide it behind a factory method  ;
- it seems that `SimpleHieroglyphicDatabase` only uses `HieroglyphicFontManager` for its list of codes ; create an interface `CodeRepository` and implement it ?

Make code a class :

~~~plantuml
@startuml
skin rose

abstract class Code {
  {static} Code buildCode(string)
  {abstract} accept(visitor)
}

class GardinerCode extends Code {}
class PhoneticCode extends Code {}
class NumericCode extends Code {}
class OtherCode extends Code {}

@enduml
~~~

## 2020/02/14

- Note for the future : the current package organization of JSesh is illogical.   
   For future versions, we could have :
  - a package for the model (mdc and sundries)
  - a "component" package, with all components
  - each component having its own package.
  - functions which communicate with jhotdraw should do so through a neutral interface
  - "heavy" functions, like "advanced searches", should take advantage of 
  
## 2019/02/22

The git archive is a bit of a mess, as I performed changes on an old version.

- remove dead or unused branches (production and development)
- do more granular changes
- save often (possibly in local archives)
- I'm trying to understand what changes I have done in the fork. I seem 
  to have set the scale to 1.0 systematically. I need to investigate it.
  Some renaming was also involved, but with a very wrong timing. Don't 
  rename outside of the master. I think it had something to do with scaling not working properly.
  
  The temporary branch has many (relatively logical) renames :
  - BaseGraphics2DFactory -> Graphics2DFactoryIF as it is indeed an interface. 
  - TopItemSimpleDrawer -> TopItemDrawer


  

## 2018/10/17

Classes dealt with in our modifications :

### Originaly 
- SelectionExporter : uses ExportData and Graphics2DFactoryIF to
export a selection. 

- TopItemDrawer: used in RTFDrawer, for RTF export

- MdCDrawerTemplate (and subclasses): generic copy class, very similar (if not equals to) 
   TopItemDrawer. Has a slightly better name though.
   

   

## 2018/10/16

I'm trying to clean the graphical export system.
Many things (like quadrat sizes) are computed 
in various places, and it hurts. 

Now, I was looking at the classes in RTFSimpleExporter
(which I'm probably going to rename RTFExporter. Simple does not give any information here.)

A class like EMFSimpleDrawer seems to be a good candidate to encapsulate decisions.
However, it's currently linked to RTF, and with good reasons:
when we output the EMF content in the RTF stream, the way
of encoding it will definitly be different if 
it's EMF and if it's, say, WMF.

- we need an "embedded picture" system
- in which we want to extract the non-RTF part for reuse.

After some reading, the current architecture is not
so bad. The names of classes need to be updated
to fit their actual use. Also, the scaling/sizing
system is flawed. We have two things called CadratHeight
which behave differently : an "inner" height, which
is a question of proportion between signs and quadrat height,
and the actual height in the resulting file.

While renaming, I guess I should rename setShadeAfter, which 
is difficult to understand, as, setDrawShadeOnTop() or 
something like that. Not done at the moment. 

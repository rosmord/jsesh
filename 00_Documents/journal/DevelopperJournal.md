# JSesh developer journal

This journal should only be edited and modified in the Development branch.

🍰
: easy task

❗️
: important task

## Important decisions


- When a method **semantically** creates a new object (vs. a methods which gives lazy access to a field which is built on demand), it should be named `createXXX`. The only exception should be when using the **builder** pattern, where the method should be named `build()` (usually just `build()`.

### How to build resources

Regarding the available resources and preferences, here is a documentation about how we can access them.

- *Note that a revision of this system could be useful, in order to make is more cohesive*.
- *names are liable to change*.


The current philosophy is:

- immutable values can be accessed through a singleton ;
- other values can be created, but should normally be shared. The best way would be either to use *dependency injection* or to create a single instance of them at the *top level* of the application. Component should not need to decide whence they take their data.

#### Defaults (without access to user preferences)

`JSeshStyle`
: A default value is available as a static field in the class, `JSeshStyle.DEFAULT`. Note that the class is immutable.

`HieroglyphShapeRepository`
: the interface has a static method called  `getStandardShapeRepository` which returns a singleton instance of a font based on old Gardiner-like tksesh font and the modern JSesh font (if its jar is available). Fine-grained access is available through the class `PredefinedFonts`, with static methods to create instances of the various fonts. The `HieroglyphShapeRepository.getStandardShapeRepository()` method is more convenient.

`HieroglyphDatabaseInterface`
: a call to `HieroglyphDatabaseFactory.buildPlainDefault(...)` will give you a database with only the JSesh embedded data.

  The following code would do:
  ~~~java
  var shapeRepository = HieroglyphShapeRepository.getStandardShapeRepository()
  var database = HieroglyphDatabaseFactory.buildPlainDefault(shapeRepository);
  ~~~

`JseshFontKit`
: an interface which represents a <strong>coordinated</strong> n-uplet of  `PossibilityRepository`,  `HieroglyphShapeRepository` and `HieroglyphDatabaseInterface`. The class `SimpleFontKit` provides a singleton `embeddedOnlyInstance` and convenient named constructor for more versatile instances.

`JSeshRenderContext`
: used as argument of **drawing operations.**. Can be built on the fly. It's a couple of `HieroglyphShapeRepository` and `JSeshStyle`. It can be built with its very simple constructor.

#### With user preferences

The simplest way to access font resources is to use `JSeshUserSignLibraryConfiguration`.

`JSeshUserSignLibraryConfiguration`
: An instance of this class, created by calling its default constructor, is a `JSeshFontKit`. It also gives access to `JSeshFullHieroglyphShapeRepository`, `GlossaryManager`, and `HieroglyphDatabaseInterface`. In most cases, only one instance of this class should be created. That is, it should be a singleton in the **Spring** meaning of the term, even if it's not one in the GoF sense.

`HieroglyphShapeRepository`
: has an implementation which provides access to the user folder, `JSeshFullHieroglyphShapeRepository`. It can be instanciated using its constructor; 

`JseshFontKit`
: use the various named constructors of `SimpleFontKit` to create an instance with the user preferences. The simplest way is to use `JSeshUserSignLibraryConfiguration`.

`Glossary`
: available through the `GlossaryManager`, which is a singleton. It will automatically load the user glossary.


User sign database source
: The XML file which may contain user-defined sign properties is found at `HieroglyphDatabaseFactory.getUserSignDefinitionFile()`.

## TODO


- [ ] Plan the application of those **TODO** just after the main refactoring is done (and the code runs).
- [ ] **Architectural decision** in the app, the `JHotdraw` linked part should be as thin as possible, and code should move to the **Core** classes.

### Plan

Follow the following steps:


- [ ] use standard maven conventions for plugins ; rename the folders accordingly if needed. Clean, recompile, check.
- when everything runs, check what code can be moved to `Core` classes and move it ; check.
- rename JSeshFontKit using `Compendium`
- check and debug the SignInfo editor.

### Long Term TODO

- [ ] ❗️systematicaly use MVC for **preferences** and **fonts** ; ensure that there is no memory leak.
- [ ] 🍰 **TODO** the organisation of the various preferences in JSesh Appli is not optimal. They are difficult to sort and understand. Improve this (when the software runs!) (relatively simple). 
- [ ] ❗️ try to use `doubles` instead of `floats` to avoid rounding errors.
- [ ] design a coherent naming system for interfaces and implementations.
- [ ] ensure we use only **one** system for defaults; we don't want to go back to the use of random singletons. **DOCUMENT IT** (as a way to ensure it's coherent).
- [ ] make the hieroglyphic font observable, so that **all** components which display hieroglyphs can be notified when they are modified - take care of possible memory leaks.
- [ ] When the software compiles, replace all variable named "drawingSpecifications" by jseshStyle.
- [ ] consider removing `depth` in layout;
- [ ] when the new version is functional, think about the lifecycle of Layout objects ; it might be interesting to simplify it. They should probably be short-lived objects.
- [ ] rename `HieroglyphicFontManager` into **ShapeCatalog** ;
- [ ] refactor the whole business around hieroglyphs to make it more logical.
- [ ] separate JSeshStyle into two parts: one with the features which are likely to be shared, and one with features which are probably specific to a particular document. I'm not sure it's that useful, this being said.
- [ ] **We should perhaps move some of the responsabilities of `HieroglyphsDrawer` to `JSeshStyle`.**
- [ ] In the JHotdraw linked part, move **down** (to JSeshViewModel) what can be moved down, possibly keeping `JSeshView` as a facade.

- Note about singletons

  - `ManuelDeCodage` is a singleton. It *could* be annoying if we had different versions of the *Manuel*, but in fact, it does only deal with the basic Gardiner List. We can continue to use a singleton here.

- review the following problem in `JSeshView`:

  ~~~java
  public void setSmallSignsCentered(boolean selected) {
        // Rather bad design: the info is kept both in drawingspecs
        // and in the document.
        /*
         * TODO CLEANUP THIS MESS (well we do have this mess since we introduced
         * this capability in JSesh and we still have it now, including the
         * "bad design" comment...
         * 
         * There should be some kind of "document event" system there... (better
         * still, have a look at Buoy, and propose something on the lines of...
         * document.addEventLink(FormatEvent.class, menuManager,
         * updateMenuItems); )
         */
        PaintingSpecifications specs = getDrawingSpecifications().copy();
        specs.setSmallSignsCentered(selected);
        viewModel.setJseshStyle(specs);
        // getEditor().setSmallSignsCentered(selected);
        /*
         * getMdcDocument().setDocumentPreferences(
         * getMdcDocument().getDocumentPreferences()
         * .withSmallSignCentered(selected)); getEditor().invalidateView();
         */
        firePropertyChange(DOCUMENT_INFO_PROPERTY, false, true);
    }
  ~~~

### Test TODO

- [ ] Check that all actual changes to a document mark it as modified.
- [ ] `JMDCEditor` 
  - [ ] when everything works, ensure that scaling works ;
  - [ ] TODO : check that when the style is modified, the editor is notified and repainted.

    in the old code, we had:
    ~~~java
    /**
     * @param drawingSpecifications The drawingSpecifications to set.
     */
    public void setDrawingSpecifications(
            PaintingSpecifications drawingSpecifications) {
        this.drawingSpecifications = drawingSpecifications;
        drawingSpecifications.setGraphicDeviceScale(scale);
        // TODO : remove me after... (after what ???)
        PageLayout p = drawingSpecifications.getPageLayout();
        p.setPageFormat(new PageFormat()); // what for ???
        drawingSpecifications.setPageLayout(p);

        invalidateView();
    }
    ~~~
- [ ] `JGlossaryEditor` 
- [ ] checks that copy/paste correctly uses the preferences we have.

### Low priority TODO

- [ ] improve the mechanism for margins of components, which is not well defined.
- [ ] use i18n for texts in the JSesh Palette
- [ ] improve `getPreferredSize` for `JMDCField` ?
- [ ] find a better organisation for text size. The following code:
  ~~~java
  public void export(ExportData data) {
        try {
            HieroglyphDrawer drawer = new HieroglyphDrawer(data.getRenderContext().hieroglyphShapeRepository());
            double length = drawer.getHeightOfA1();
            data.setScale(this.cadratHeight / length);
            if (multiFile) {
                exportAll(data);
            } else {
                exportSelection(data);
            }
  ~~~

  is somehow cumbersome.

- [ ] ❗️rename `HieroglyphDatabaseInterface` into `HieroglyphDatabase`, and name implementations instead.
- [ ] ❗️rename `SimpleHieroglyphDatabase` into `DefaultHieroglyphDatabase`.
- [ ] rename mojos using the standard maven scheme.
- [ ] move `showCorpusSearchHit` out of `JSeshApplicationModel`.

### TODO / MANDATORY

Regarding **standard** codes:

- [ ] ❗️add a test checking that nTrw is mapped to `R8A`;
- [ ] ❗️add a test checking that nn is mapped to `M22B`.
- [ ] write a documentation about using JSesh as a library with and without user-defined signs.

### Simple TODO

- [ ] rename `HieroglyphPictureBuilder` into `HieroglyphIconBuilder`, because it's its function.
- [ ] when the code is ok, check the sign editor to be sure all the constructors are needed. There are an awful lot of them, for instance for `SignInfoProperty`;
- [ ] Improve the structure of `QuickPDFExportAction`
- [ ] Choose a consistent Logging scheme.
- [ ] **Find where I used the "modern" accessors without get**, and move back to standard getters for code consistency - except when it's not really a **property**.
- [ ] use **folder** instead of directory (probably less unix-centric).
- [ ] consider grouping `styleRef` and `fontKit` in a single element (e.g. JSeshComponentConfigSource) to use when creating secondary windows and dialogs. See what needs to be passed and when.
- [ ] ❗️rename `JSeshFontKit` into `HieroglyphCompendium`, and `HieroglyphDatabase` into `HieroglyphSignLexicon` (to emphasize it's not about shapes).
- [ ] ❗️❗️Modify `MDCEditorKeyManager` to make its use more transparent. Add an `attach` method, instead of doing everything in the constructor. We don't do it immediately to avoid having one more refactoring to do. We will wait until the present refactoring is complete, and works. 
- [ ] Document what is the scale in `JSeshTechRenderContext`.
- [ ] parametrize each ModelElement class with the type of its possible children.
- [ ] merge `SimpleHieroglyphDatabase` and its interface `HieroglyphDatabase`, as it's the only existing implementation ;
- [ ] remove the `ViewDrawer` from `JMDCEditor`; it shouldn't be an instance variable.
- [ ] find what to do with `HieroglyphDatabaseFactory`. It builds the database, but also reads sign descriptions from XML files. Most of the code it contains could move to `SimpleHieroglyphDatabase` as *named constructors*.
- [ ] consider if `HieroglyphDrawer` could be moved to local variables instead of being an instance variable. The “true”  instance variable is the `HieroglyphShapeRepository`.
- [ ] ❗️❗️For the default glyph source, we should probably propose a system with two defaults sources : with or without user-defined signs.
- [ ] separate constructor call for the glossary manager and reading user glossary from file, mainly to simplify testing and debugging. 
- [ ] reorganise the packages of `jseshAppli`, which have really been designed on the fly.
- [ ] manage  `USE_J` in `YodChoice`


### Cleanup

List of classes which need some cleanup:

- `QuickPDFExportAction`
- `ExportAsRTFAction`

## Daily log

### 2026/04/27

- In the sign info property, we use `XMLInfoProperty` and `SignInfoProperty`, which is a subclass of the first, with a `sign` attribute. The problem is that the code we wrote initialy is old and sometimes ignored the actual class of the elements (collections were pre-java 1.5).

- Rethink `JSeshFontKit`. In particular :

  USes of `JSeshUserSignLibraryConfiguration` and of some implementations of `JSeshFontKit` uses intersect. In fact, we could consider having `JSeshUserSignLibraryConfiguration` as an implementation of `JSeshFontKit`.

- **Once everything compiles,** review uses of `SimpleFontKit` methods and see what constructors are actually used, and remove the others.

**historical reminder** : for a long time, Mac OS X used to provide its own version of Java. In theory, it was nice, but those version tended to lag behind the official java version. And old mac did not get updates if they didn't upgrade to newer Mac OS X version. A consequence of this was that JSesh could not use java 1.5 generic collection until a very late date.

#### About XMLInfoProperty and SignInfoProperty

`XMLInfoProperty`
: a class which represent data which will be serialized as an XML element, with a simple string value, and various attributes. It's used as such to describe  tag lables (see `TagEditorPresenter`) and tag categories (`<tagCategory>`), see `SignInfoModel`.

`SignInfoProperty`
: a subclass of `XMLInfoProperty` which has a `sign` attribute, which is the sign code of the sign described by the property.


Currently, `SignPropertyTableModel` has methods which refer to `XMLInfoProperty`, but in fact, all implementations use `SignInfoProperty`. In the worst case, the type of the element could be a generic parameter.




### 2026/04/25

- [ ] **TODO** working on the sign info editor. I notice that there are two classes used for producing sign icons:
  - `ImageIconFactory`
  - `HieroglyphPictureBuilder`
  
  sort this out and find if both are needed. Well, the `ImageIconFactory` is not a factory, it's a repository/cache. It should probably use `HieroglyphPictureBuilder` and not contain its own icon building code.


### 2026/04/24

- **TODO** : urgent and simple. The `GlossaryManager` instance will always load the user glossary. If we want an empty glossary, we need either to create other implementations, or to remove the call to `read()` from the constructor. 
- moving back to java 21. Currently, the m2e plugin in vscode uses java 21, so maven plugin compiled with java 25 don't work. The `jseshGlyphs` modules depends on java code which knows how to normalize the sign codes. This code is domain code, and part of the application. We could move it to a specific module, but it's a weird solution to solve a technical problem. We might solve this with Gradle, or simply by waiting until m2e uses java 25. Meanwhile, we move the code back to java 21. We had used *flexible constructors*, we need to revert to standard ones. We had also used the anonymous variable `_`.
- working of `jsesh.jhotdraw.actions`
- important point about the JHotdraw framework. The JHotdraw views handle the `uri` properties of the documents. If we want to move code to JSeshViewCore, we need either to propose a backward link with the JHotdraw view, which kinds of defeats the purpose of the refactoring, or to explicitly pass the URI to the core when needed.

  hence most methods linked with file names and file saving should stay in `JSeshView`;
- the class `GenericExportAction` has a call to `getURI` which is a bit suspect. We can probably do better, see the other exporters. We have added a `getURI` method to `AbstractCoreViewAction` just for that case, but we should remove it if we fix it.

### 2026/04/23

- making ViewCore visible can be problematic in some cases. For instance:

  ~~~java
  @Override
  public void setEnabled(boolean enabled) {
        viewCore.setEnabled(enabled);
        super.setEnabled(enabled);
  }
  ~~~

  is supposed to can `setEnabled` both on viewCore and on the JHotDraw view component. Making viewCore visible makes the code more difficult to read, as we might forget to call the method on the JHotDraw component.

- problem with the "core" model. In some cases, we need access to the **active view**. This is managed by the application.
  - so, for the moment, we move the search panel back into the application model.
- instead of delegating methods and having a heavy interface in `JSeshApplicationModel`, we could decide to forward what should be forwarded to the **core** components. It would transform the action into:

~~~java
public class ApplySavedStyleAction extends AbstractViewAction {

	public static final String ID="file.applyModel";

	public ApplySavedStyleAction(Application app, View view) {
		super(app, view);
		BundleHelper.getInstance().configure(this);
	}

        @Override
	public void actionPerformed(ActionEvent e) {
    // Standard JHotdraw stuff
		JSeshView view= (JSeshView) getActiveView();
		JSeshApplicationModel app = (JSeshApplicationModel) getApplication().getModel();
    // out stuff
		app.core().applyNewDocumentStyleTo(view.core());	
	}
	
}
~~~

Nice suggestion from whatever LLM vscode is using right now:

Replace:

~~~java
JSeshView view= (JSeshView) getActiveView();
JSeshApplicationModel app = (JSeshApplicationModel) getApplication().getModel();
view.setJseshStyle(app.getNewDocumentStyle());
~~~

by:

~~~java
JSeshView view= (JSeshView) getActiveView();
JSeshApplicationModel app = (JSeshApplicationModel) getApplication().getModel();
app.applyNewDocumentStyleTo(view);
~~~

Which is more **responsability** focused.

- [x] renamed `JSeshApplicationBase` into `JSeshApplicationCore`, because `Base` in java suggests inheritance;
- [x] renamed `JSeshViewModel` into `JSeshViewCore`, to clarify the structure of the application.
- [ ] in `PdfExportPreferences`, the `drawingSpecifications` where... not used!
- [ ] We have added a chain of `getJseshStyle` methods in the view part of the application in `MyTransferableBroker`. But is it needed? The information needed might be available in the `JSeshApplicationModel`. 
- [ ] we have removed the method `setClipboardPreferences` from `MDCModelTransferable` : clipboardPreferences were **not** used at that point!
- [ ] **TODO**  `JSeshView` should not have a `setMDCModelTransferableBroker` method. the broker comes from the application. It should either be passed in the constructor or set at init time. No need to have a **public** setter for it!
- [ ] as a matter of fact, the method:
  ~~~java
  @Override
      public void initView(Application a, View v) {
          super.initView(a, v);
          JSeshView jSeshView = (JSeshView) v;
          jSeshView.initWithResources(jseshApplicationBase);
          jSeshView.setMDCModelTransferableBroker(transferableBroker); // Might be performed by initWithResources
          jSeshView.setFontInfo(getFontInfo());
      }
  ~~~

  should probably be:

  ~~~java
  @Override
      public void initView(Application a, View v) {
          super.initView(a, v);
          JSeshView jSeshView = (JSeshView) v;
          jSeshView.initWithResources(jseshApplicationBase);          
      }
  ~~~

  with both the `transferableBroker` and the `fontInfo` being held by `jseshApplicationBase`. Actually, fontInfo is already there.

### 2026/04/22

Work on  `JSeshAppli`.

#### Code structure
A bit of structure. In the following diagram, the stereotype `<<jhotdraw>>` is used to mark classes which instantiate elements of the `jhotdraw` framework.

```plantuml
@startuml
skin rose
hide empty members

class Main {
    main(String[] args)
}

note left of Main
Starts the application.
Loads resources and instanciate the main panel elements.
implements AppStartup from 
org.qenherkhopeshef.guiFramework
end note

Main --> JSeshApplicationModel
Main .> JSeshApplicationStartingData

note bottom of JSeshApplicationStartingData
Data passed from the main thread to the EDT thread.
Purely technical and should be hidden.
Which means we should probably have a Main class the programmmer can see, and a non public one which implements AppStartup and is called by the true main method.
end note

together {
class JSeshApplicationModel <<jhotdraw>> {
    
}

class JSeshApplicationCore  {
    jseshStyle JSeshStyle    
}


JSeshApplicationModel --> JSeshApplicationCore

class JSeshMenuBuilder <<jhotdraw>> {
    
}

JSeshApplicationModel --> JSeshMenuBuilder
}

package documentview {

  class JSeshView <<jhotdraw>> {    
  }
  
  class JSeshViewComponent {}

  class JSeshViewController {
      
  }
}

note top of JSeshViewController
Presenter/controller 
for the "JSeshViewComponent"
end note

note right of JSeshViewComponent
gui JPanel for the JSeshView
  end note


JSeshView --> JSeshViewController
JSeshViewController --> JSeshViewComponent

JSeshViewComponent --> JMDCEditor
JSeshApplicationModel ..> JSeshView

note bottom of JSeshApplicationCore
Everything which is 
# application-level
# and non specific to JHotdraw

is delegated to JSeshApplicationCore.

It knows about all defaults, and currently
holds the whole hieroglyphic font system.
end note
@enduml
```

Currently, the class `JSeshStyleHelper` is in the `jsesh.utils` package. Logically, it's part of `jsesh.jhotdraw` (the main application), but it's relatively likely that some library users will want to use the JSesh **application** preferences for some of their own softwares.

#### Work log

- consider adding logging to the application, to improve debugging;
- we have added the boolean property `useEmbeddedTransliterationFont` to `JSeshApplicationBase`. It looks like a hack, and we should try to improve it.

### 2026/04/21

- Problem with JSeshFontKit and SimpleFontKit. We want to have only **one** glossary, **one** possibilityRepository, etc. The constructors of SimpleFontKit may lead to the creation of multiple instances of those objects.
- [ ] **TODO** improve naming. The `JSeshApplicationBase` uses both `RTFExportPreferences` which are used by the actual graphical layer, and `exportType` and `ExportPreferences` which come from preference dialogs. `RTFExportPreferences` is built using both `exportType` and `ExportPreferences`, but the naming scheme is not very good.

- [ ] **TODO** find why `getCurrentDirectory` is synchronized. Choose between the names `getCurrentDirectory` and `getWorkingDirectory`.
`
- [ ] **TODO** (very soon) rename `selectCopyPasteConfiguration` into `setCopyPasteConfiguration` or something like that. 

- [x] **TODO** introduce the following packages :

  ```plantuml
  @startsalt
  {
  {T
   +jsesh
   ++ jhotdraw
   +++ preferences
   ++++ application
   ++++ document
  }
  }
  @endsalt
  ```

- working on `JSeshAppli`
  - look at the way the glossary is initialized in the old system.
  - it's the GlossaryManager, which is originaly a singleton. It's relatively standalone, being referenced then by `GlossaryTableModel` and `PossibilityRepository`.
  - we need to decide how to manage `FontInfo` **and** `JSeshFullHieroglyphShapeRepository` (BTW, we might add an interface to differentiate using the font and changing it). 
    `FontInfo` is in a way a glorified **DTO**, a model for the font preferences dialog. We should probably keep it, but use another class to manage the fonts themselves (actually, we have such a class in JSeshStyle).

- [ ] have a closer look at the problem of keeping both fontKit (even with a new name) and an instance of `JSeshFullHieroglyphShapeRepository`. It's somehow redundant, but `JSeshFullHieroglyphShapeRepository` has methods to add news signs, wereas you can't do it through fontKit. 

- [x] the `jseshSearch` module compiles. It still needs to pass the tests;
- [x] tests of `jseshSearch` ok.
- [x] `pom.xml/vscode` problem: fixed by running `mvn install` can be a good idea.


### 2026/04/20

- removed `codeDumper`, and moved it to the [jseshUtils](https://github.com/rosmord/jseshUtils) project.
- [x] ensure the tests of the `jsesh` module pass;
  - but we removed two tests about `nTrw` and `nn`. See the **Mandatory TODO** section for replacing them.
- rename module `prepareJSeshRelease` to comply with maven modules naming conventions.
- Working on `jseshSearch`.
  - we have a problem here. We need to pass a `JseshFontKit` to the `JMDCField` constructor, because the font repository is expanded with specific glyphs which render the search language operators. However, we need to pass the standard possibilityRepository (those glyphs should not be accessible for completion) and `hieroglyphDatabase`.
  - ok, now we pass everything. See suggestions for naming in **Simple TODO**.


### 2026/04/18

- [x] remove codeDumper. It should go in another project;

### 2026/04/17

- [x] work on `JMDCField`.
  - add constructors. See their Javadoc for the decisions which were taken.
  - why do I call setScale() in the constructor of `JMDCField` ? 
- [x] work on actions (goright, etc.)
- the whole `editor` package compiles!
- `JGlossaryEditor` : removed some dead code.
- the whole `jsesh` module compiles!

### 2026/04/15

- [x] work on JMDCEditor.
  - About style : currently, style contains data which is not very compatible with the idea of sharing style. For instance, it contains textOrientation.
  - ... some boring stuff
  - mostly replacing drawingSpecifications by `getStyle()` ;
  - [x] TODO : rename `getPointerRectangle` to `getCursorRectangle` or something like that.
  
### 2026/04/14

 
### The method `scaleFromFontToStyle` (FIXED)

In JSesh 7, we have things like:

~~~java
case SymbolCodes.REDPOINT: {
    Color col = g.getColor();
    g.setColor(drawingSpecifications.getRedColor());
    g.scale(drawingSpecifications.getSignScale(), drawingSpecifications
            .getSignScale());
    drawingSpecifications.getHieroglyphsDrawer().draw(g, h.getCode(),
            0, currentView);
    g.setColor(col);
}
~~~

Which are replaced in the current code by:

~~~java
case SymbolCodes.REDPOINT: {
Graphics2D tempG = (Graphics2D) g.create();
float scale = hieroglyphsDrawer.scaleFromFontToStyle(jseshStyle);
tempG.setColor(jseshStyle.painting().redColor());
tempG.scale(scale, scale);
hieroglyphsDrawer.draw(tempG, h.getCode(), 0, currentView, HieroglyphBodySize.STANDARD);
tempG.dispose();
}
~~~

So, the method replaces `drawingSpecifications.getSignScale()`, whose original code was:

~~~java
public float getSignScale() {
  return (float) (getStandardSignHeight() / getHieroglyphsDrawer()
          .getHeightOfA1());
}
~~~


### 2026/04/10

**TODO**: 

- [ ] ensure that `ViewDrawer` is created only when we need it - or make it stateless. In particular, it's created in the constructor of `JMDCEditor`.
- [x] fix the problem of `float baseSignScale = hieroglyphsDrawer.scaleFromFontToStyle(jseshStyle);`.

  





**Note**: in JSesh 7.x,  `HieroglyphsDrawer` is simply created by calling its default constructor, which will automatically use the user preferences. 

The name of the class `HieroglyphsDrawer` is not very good. 

- [x] we want to hide most of the content of the `jsesh.mdcDisplayer.drawingElements` package. We are currently annoyed by `symbolDrawers` being visible. After considering moving everything to the parent package, we resort as a dirty trick to renaming. The package will be  moved `jsesh.mdcDisplayer.drawingElements.internal`. When (if!) we introduce **modules**, a clean solution will be available.
- [x] currently, some methods of `HieroglyphsDrawer` have only a meaning for hieroglyphs. The symbol font does not handle them. We could split the interface.

### 2026/04/09

**Decision** : don't pass `HieroglyphsDrawer`, pass `HieroglyphicFontManager` instead. 

The problem we are going to face is the following. A `JMDCEDitor` draws the data it needs from a variety of sources.

- `HieroglyphsDrawer` for hieroglyphic fonts (as far as geometry is concerned) ;
- `JSeshStyle` for the way we want to draw things (e.g. size of quadrats, etc.) ;
- `PossibilityRepository` which links translitteration and codes.

The question are : who creates those objects, are they shared, and what are good default values for them? Concerning `JMDCEditor`, it would be nice to have a default constructor with reasonnable default values. It would also be nice to have a **Factory** for creating editors with more advanced values.

Logically, the default constructor should build an instance:

- which uses the standard JSesh font if possible (not the old limited tksesh font) ;
- should be independant of the preferences of the user for JSesh itself.

Specific factories should allow one to create editors using either the user's preference, or to specify what they want.

The current view of the system is available in to file [jsesh.hieroglyphs-V1.md](../UML/jsesh/jsesh.hieroglyphs-V1.md). 

Basically:

- `HieroglyphicFontManager` is the source for glyph shapes; the `DefaultHieroglyphicFontManager` currently uses all possible sources.
- `HieroglyphsDrawer` is in fact used to 
  1. draw signs from a `HieroglyphicFontManager` ;
  2. draw ecdotic symbols ;
  3. choose the size of signs.
   
In the case of HieroglyphsDrawer we have a composite, actual implementation. The only real parameter is the `HieroglyphicFontManager` it uses. Hence, the parameter to pass in the constructor should be a `HieroglyphicFontManager`.

The `JSeshRenderContext` should also hold a `HieroglyphicFontManager`, not a `HieroglyphsDrawer`.



- in `MDCViewUpdater` : should we use a scale which is not $1.0$ for the TechRenderContext? 

### 2026/04/08

### TODO

- TODO: introduce a class for Combobox and lists elements which would allow:
  - stand-in, label only elements (for the first entry)
  - active elements, holding a “true” object. 

### 2026/04/01

- Renamed package `jsesh.hieroglyphs.graphics` into `jsesh.hieroglyphs.fonts`, which is much adequate. 


The changes in the `jsesh` module are almost done. A few new problem appears. We had foreseen them at some point:

~~~java
    // FIXME : choose a reasonable method to share drawing specifications.    
    // Remark : once the drawing specifications are set here, we copy them so only us change them.
    // Think about that.
~~~

The `FIXME` remark was already there in 2012, when we moved the archive from sourceforge to git!

What should we do?

- Introduce a JSeshPreferenceHolder?
- use the MDCEditorKit?

A real problem is that we might want to **share** preferences in some cases, and not in others.

After thinking about this:

- sharing should probably be done mostly when the editors are **created**. Having a default set of preferences, which can be individualy modified afterwards;
- for hieroglyphic fonts, it's a bit more complicated.

We would like:

- in some cases, to have a generic, all-encompassing change.
- for instance, when a user-defined sign is **modified** or **added**, we would like to hear about it;
- in other cases, we could be happy to have two different font sets.

### How are font changes handled in the current system?

Currently, when the preferences dialog is modified, the following code is executed:

~~~java
 /**
     * Sets the application preferences.
     * @param app
     */
    public void updatePreferences(JSeshApplicationModel app) {
        clipboardFormatSelector.updatePreferences(app);
        exportPreferences.updatePreferences(app);
        app.setFontInfo(fontPreferences.getFontInfo());
        otherPreferences.savePreferences();
    }
~~~

Then, `app.setFontInfo` will do :

~~~java
/**
 * Change the fonts used by JSesh.
 *
 * @param fontInfo
 */
public void setFontInfo(FontInfo fontInfo) {
    jseshApplicationBase.setFontInfo(fontInfo);
    for (View v : application.views()) {
        JSeshView view = (JSeshView) v;
        view.setFontInfo(fontInfo);
    }
}
~~~

It will tell each view that its font has been modified.

Finally,  each `JSeshView` (a class in the `jseshAppli`) module, will call:

~~~java
public void setDrawingSpecifications(
        PaintingSpecifications drawingSpecifications) {
    getEditor().setJseshStyle(drawingSpecifications);
    getMdcDocument().setDocumentPreferences(drawingSpecifications.extractDocumentPreferences());
}
~~~

It could be done in an MVC way.

### Current system for characters updates in `DirectoryHieroglyphicFontManager`

Currently, nothing is really done. Modern Java/IO would allow us to watch for file changes.








### 2026/03/24

Regarding the editor part: continue the great hunt for singletons, and pass the objects we need as parameters. 

I consider doing some renaming **(probably after everything compiles), not now**

- The current `JSeshRenderContext` could be renamed to `JSeshProperties` or something like that - in a way, `JSeshStyle` would be a good name ;
- `JSeshRenderContext` would then contain both those information and the `JSeshTechRenderContext`.

The most problematic part is `HieroglyphsDrawer`, because this class is not immutable.

In some cases (for short-lived objects), it's not a problem. For others, notably the editor, we do need to keep track of changes.

### 2026/03/19

- reread what we do with graphicsDeviceScale from TechRenderContext. It might be important when we draw on 4K screens for instance.

Regarding `JSeshRenderContext` and `JSeshTechRenderContext`. Using `JSeshRenderContext`  tends to violate the Demeter principle quite a lot. It's convenient when we need to modify something, but for **reading**, it's somehow cumbersome.

### 2026/03/18

We should avoid letting the code alone for too long. We forgot what we were doing.

updated `ViewDrawer` with `JTechRenderContext`. Some further refactoring would be nice :

- either create records to group most arguments of `drawViewAndCursor` (all of them, in a `DrawRequest` record, simpler groupings.)
- it could also be a good idea to avoid using instance variables of `ViewDrawer` to store what is really function arguments. Grouping them in a record would allow one to pass the easily, and make everything much more explicit.


**What** is `getPointForPosition` doing in `ViewDrawer`? It should somehow be a method of `View`? **Edit**: it's the case in the current system, but in fact, it's brittle, as we depend on position and subviews to be aligned.

- We start working on `GroupEditor`;
- why is `JSeshTechRenderContext` outside of `JSeshRenderContext`? 

### 2025/12/03


I definitly don't like the current structure of `JSeshRenderContext`. For instance, the component for `GroupEditor` needs it. But it's not logical at all. Technically, it can be done, by passing mock or default values for `fontRenderContext` and `graphicDeviceScale`, but it's not very nice.

We could consider splitting `JSeshRenderContext` into two parts:
- `JSeshConfiguration`, containing `jseshStyle` and `hieroglyphDrawer`;
- `JSeshRenderContext`, containing `fontRenderContext` and `graphicDeviceScale`.

- regarding the `GroupEditor` : a new `GroupEditor` is created each time a user edits a group. It's fine, but on the architectural level, it's a bit dangerous, as programmers might think that the `GroupEditor` can be long-lived object. We could change its interface to allow reuse. 

**Trying to split JSeshRenderContext**

- in a number of places, we know the `Graphics` object (for instance, in the drawer). Hence, the `FontRenderContext` should be available without too much trouble. Currently, we pass it. We will not change this immediately, but we must remember to perform an update if possible later. 

### 2025/11/28

Currently, the class JSeshRenderContext has the following structure:

```plantuml
@startuml
class JSeshRenderContext {
    fontRenderContext
    graphicDeviceScale
    jseshStyle
    hieroglyphDrawer
}
@enduml
```

- fontRenderContext: depends on the rendering context, Swing specific, but needed **even for layout,** as text measurement depends on it;
- graphicDeviceScale: depends on the ultimate output;
- jseshStyle: options and the like;
- hieroglyphDrawer: hieroglyphic font source.

In the **external** API, the first two make little sense. It's quite likely that the system will be able to provide them. 

Should we modify the API of `EmbeddableMacPictSimpleDrawer` for instance ?

- Working on `MDCModelTransferable`: lots of parameters if we avoid creating stuff with `new`. We need to figure a cleaner way to use them ;
- `AbtractExportDrawer` : `MDCModelTransferable`, which is in `mdcDisplayer`, depends on it (for copy/paste). In turn, `AbtractExportDrawer` depends on `mdcDisplayer` (because of views). We should do something here (maybe at the level of packages)

- added `setRenderContext` to `AbstractRTFEmbeddableDrawer`, because we need to recompute it at some point. It's not very nice. We should probably have a shorter-lived object for this.
- still about `EmbeddableWMFSimpleDrawer` : the drawing workflow is somehow weird. We should probably polish it to make it stateless ;
- same problem with `RTFExporter` (in this case, the modified rendercontext should be passed to the visitor)
- move `ensureCMYKColorSpace` to JSeshStyle.colors ???
### 2025/11/06

Done : replaced `LigatureManager` by code in `SVGFontHieroglyphicDrawer`. Simplified the system by using JSesh-based coordinates, not the old Tksesh system (which had a different reference system altogether)

Actual use of `LigatureManager` :

only in `Layout`; used in `visitLigature`; calls only one method, which is `ligatureManager.getPositions(codes)`;

- the `LigatureManager` needs the `HieroglyphicFontManager`;
- the `Layout` knows reasonably well the `HieroglyphsDrawer`.

### Inheritance hierarchies of HieroglyphsDrawer and HieroglyphicFontManager

```plantuml
@startuml
skin rose
hide empty member
title Inheritance hierarchies
interface HieroglyphsDrawer {
    draw(Graphics2D g,...)
    getBBox(String code, int angle, boolean fixed)
    getShape(String code)
    getSignArea(...)
    isKnown(String code)
    getLigatureZone(...)
    getHeightOfA1()
    signScale(...)
    scaleFromFontToStyle(...)
    getGroupUnitLength()
}

note left of HieroglyphsDrawer
This class is responsible for everything
which concerns the graphical
appearance of symbols 
(hieroglyphs, editorial markup...)
end note

interface HieroglyphicFontManager {
  	hasCode(String code)
	  getCodes()
    get(String code): ShapeChar
    getSmallBody(String code): ShapeChar 
  	hasNewSigns()
}

note right of HieroglyphicFontManager
HieroglyphicFontManager associates
glyphs with codes.
end note


interface HieroglyphsDrawer{}
HieroglyphsDrawer <|.. HieroglyphicDrawerDispatcher
HieroglyphsDrawer <|.. SVGFontHieroglyphicDrawer
HieroglyphsDrawer <|.. SpecialSymbolDrawer
HieroglyphicDrawerDispatcher -> SVGFontHieroglyphicDrawer
SpecialSymbolDrawer <.. HieroglyphicDrawerDispatcher 

interface HieroglyphCodesSource {
	hasCode(String code): boolean 	
	getCodes() : Set<String> 
}

interface HieroglyphicFontManager extends HieroglyphCodesSource {}

HieroglyphicFontManager <|.. CompositeHieroglyphicFontManager
HieroglyphicFontManager <|.. DefaultHieroglyphicFontManager
HieroglyphicFontManager <|.. DirectoryHieroglyphicFontManager
HieroglyphicFontManager <|.. MemoryHieroglyphicFontManager
HieroglyphicFontManager <|.. ResourcesHieroglyphicFontManager

note bottom of DefaultHieroglyphicFontManager
Could perhaps be a Composite manager.
knows of the three sources used by JSesh:
- user-defined signs
- jsesh sign library
- old tksesh Gardiner-like signs
end note

@enduml
```

The class `SVGFontHieroglyphicDrawer` draws its signs from a `HieroglyphicFontManager`.


In fact:

- `HieroglyphicFontManager` could be called **ShapeCatalog** ;
- `HieroglyphsDrawer` has a reasonable name.


A note about bounding box returned by `HieroglyphsDrawer`: the `getBBox` methods can handle both fixed a stretchable signs. The current interface is:

~~~java
getBBox(String code, int angle, boolean fixed);
~~~

But it would be more logical if the possible quality of the bbox would be stored in the return value itself.

### 2025/11/05


### Done

- written some tests;

### to consider

- simplify HieroglyphsManager, HieroglyphsDrawer, etc.

### Notes

- bug with ligatures as ligatureManager is null in layout. **It was originally a singleton**
- Consider removing "depth" in layout ?

Concerning the **LigatureManager**: its only use is currently to deal with the **old** tksesh-based ligature (i.e. predefined ligatures like `stp&n&ra`).
It reads a specific file, and is more a dinosaur than anything else. Can it be a singleton? It uses the `HieroglyphicFontManager` to know the sign's bounding boxes. Hence it's linked to the font manager. 



### 2025/11/04

- renamed `RenderContext` as `JSeshRenderContext` to avoid importing wrong class;
- it seems that we **always** need `RenderContext` (there are fonts), `HieroglyphsDrawer` and `JSeshStyle` after all. The simplest solution is to move all of them in `JSeshRenderContext`.

### Notes

- `superScriptDimensions` and `textDimensions` returns respectively a `Rectangle2D` and `Dimensions2D`. This is **not** logical. Think about it.
- I'm not fully convinced of the interest of `PhilologyHelper`.

We have to fit `graphicDeviceScale` somewhere. In the original `DrawingPreferences`, we described it as:

~~~java
 /**
     * Returns the scale of the graphic device, in graphic units per
     * typographical point. This is the scale used by the device if
     * g.getXScale() returns 1.0, not the current scale. Note that we could be
     * lying. In the case of a screen zoom, for instance, we will still provide
     * the original scale.
     * @return 
     */
~~~

Basically, it's the size of a typographical point (`pt`) unit in a *Graphics2D* space whose base scale is 1.0f.

Its **only** use is to decide if we use the **small font** shapes or the **normal shape**, as it depends on the actual rendered size. Its logical place is in the root of the `RenderContext` Object, along with the `FontRenderContext`.

### 2025/11/03

- TODO : look more closely at the uses of `groupUnitLength`, and consider expressing it in the model space, not the inner font space.

- renamed `ColorSpecifications` to `PaintingSpecifications` (see next entry);
- moved `shadingStyle` to `PaintingSpecifications` (more logical);
- in `HieroglyphsDrawer`, `smallBodyUsed` makes the class stateful. It can be removed safely if we add an argument to `HieroglyphsDrawer.draw`.

- **TODO** (cleanup): remove `depth` in Layout. It's not used.
- pass `HieroglyphsDrawer` **and** `RenderingContext` ?
- the `reset()` method in layout is used to set up the JSeshStyle to use. It seems the style could be set elsewhere.

- **check**: what is the best place to apply tag colors ?
- **check if the following code is correct** : 
  
  ~~~java
  if (subView.getHeight() > jseshStyle.geometry().largeSignSizeRatio()
						* hieroglyphsDrawer.getHeightOfA1()) {
  ~~~

  I think we should use `jseshStyle.geometry().standardSignHeight()` instead of `hieroglyphsDrawer.getHeightOfA1`

Note:

- `getGroupUnitLength` in `HieroglyphsDrawer` is not very logical, as signs are scaled to fit the Geometry;



### 2025/10/13

- When updating `QuadratLayout`, we have a problem as `HieroglyphsDrawer` is no longer part of the specifications.
  we need to solve this.

- If we introduce `RenderingContext`, we will need to decide how we pass it, and what it contains.
- I don't see the hieroglyphic fonts being a part of rendering context.


In `Layout`, for small text, we had:

~~~java
	Dimension2D r = drawingSpecification.getSuperScriptDimensions(smallText);
~~~

Now, I'm not sure that the new JSeshStyle class is the right place to have this method, whose old implementation was:

~~~java
public Dimension2D getSuperScriptDimensions(String text) {
    Rectangle2D r = superScriptFont.getStringBounds(text,
            fontRenderContext);
    return new DoubleDimensions(r.getWidth(), r.getHeight());
}
~~~

Technically, it might be done, if we pass the `FontRenderContext` as parameter. But `records` are mostly *pure data holders,* and adding behaviour in records is not a very good idea.


~~~java
record FontSpecification .... {
  public Dimension2D getSuperScriptDimensions(FontRenderContext fontRender, String text) {
    Rectangle2D r = superScriptFont.getStringBounds(text,
            fontRender);
    return new DoubleDimensions(r.getWidth(), r.getHeight());
  }
}
~~~

### Current problems

When fixing the classes in the `layout` package, we find the following problems:

- hieroglyphic font related problem: the method `getHeightOfA1` returns the actual size of the `A1` sign in the current font; this is not supported by `JSeshStyle`;
- we need the `FontRenderContext`;
- we need to compile text size (e.g. getSuperScriptDimensions)
- we need to compile `signScale()`, which depends on the current font **and** on the target size we want in `JSeshStyle`;
- we need the `HieroglyphsDrawer` to find `ligatureZones`, `heightOfA1`;
- we need to compute the bounding boxes of hieroglyphs: `getBBox`;
- we need to compute the width of ecdotic symbols: `getPhilologyWidth`;
- we have `getGroupUnitLength()` which is 1/1000f of the size of `A1`. Currently, it's a bit weird, as it's in `HieroglyphicDrawer`, but if it appears in the `MdC` code, it should probably be computed from `JSeshStyle` and be independant of the font.






### 2025/10/10

TODO : add a RenderingContext parameter, which should **not** be part of JSeshStyle.

  - `JSeshStyle` : the way you want the text to be drawn;
  - `RenderingContext` : the technical constraints you meet.

- (cleaned up master branch), probably not the good time to do this; merged both.
- how to proceed now:
  - start with `mdcDisplayer`
    - fix `layout`, `draw` and `drawingElements`
      - LineLayout and ColumnLayout
      - QuadratLayout
      - anxnb
      - 
    - then move to components and clipboard
  - then fix the rest of jsesh module
  - then the rest of the world.

### 2025/10/08


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


### 2025/10/07

Problem to solve: should the drawing specifications depend on Swing? or should they be stand alone?

- in the first case, some things are easier to write and probably more efficient.
- in the second case, we can more easily move to non-swing rendering if needed later.
- we keep the newEdit package for now. It will probably move to a branch of its own once we have finished this update.
- our new architecture for specification doesn't play very well with DocumentPreferences. It might be interesting to try to fit the two. Currently, it's a blatant violation of the Demeter principle.

### 2025/10/06

- We freeze the structure of the display preferences (`RenderingParameters`) ;
- we will then proceed to write the code ;
- and then, we will refactor them if needed.
- we have removed computed data from the drawingspecifications. They will be provided by helper functions when needed.
-  In the current version, the defaults for pagespec are computed from the other specifications. The whole specification system should be rethought to use independent values.
-  DocumentPreferences are using an outdated architecture. We should move to a record based one.

### 2023/06/07

### 2023/06/06

Analysis and classification of current problem. Planning. See TODO about removing singletons.


### 2023/05/19

We might introduce the notion of BasicMdC codes for codes :

- which appear in Gardiner's grammar ;
- or which have an official phonetic code.

### 2023/05/15

Back to JSesh. Considering :

- simplifying HieroglyphFontManager (which might be a simple code to shape repository), to remove its possible dependency on HieroglyphDatabase ?
- think of introducing a Facade to the whole hieroglyph system in this case.

### 2023/04/25

Well, the mess of singleton is also linked to the question of preferences (not preference files, but in-memory preferences) in the software. 

- investigate variants of Observable and Reactive patterns outside of Swing
- look how it's done in other softwares...

The current question is the granularity of preferences. Should it be large (say, setPreferences/getPreferences, with an immutable 
preferenceValue object, and possibly a preferenceProperty which could be shared), or fine-grained (with a mutable preference object).


**anyway, the first step will be to make everything explicit at constructor level**. We will only use `new` when absolutely mandatory.


### 2023/04/20

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

### 2020/02/14

- Note for the future : the current package organization of JSesh is illogical.   
   For future versions, we could have :
  - a package for the model (mdc and sundries)
  - a "component" package, with all components
  - each component having its own package.
  - functions which communicate with jhotdraw should do so through a neutral interface
  - "heavy" functions, like "advanced searches", should take advantage of 
  
### 2019/02/22

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


  

### 2018/10/17

Classes dealt with in our modifications :

### Originaly 
- SelectionExporter : uses ExportData and Graphics2DFactoryIF to
export a selection. 

- TopItemDrawer: used in RTFDrawer, for RTF export

- MdCDrawerTemplate (and subclasses): generic copy class, very similar (if not equals to) 
   TopItemDrawer. Has a slightly better name though.
   

   

### 2018/10/16

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

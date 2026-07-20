# JSesh developer journal

This journal should only be edited and modified in the Development branch.

🍰
: easy task

❗️
: important task


## Next pending step


## Important decisions


- Use the handy list of abbreviations in https://tla.digital/listings/bibliography/ for the SignInfo editor.




- When a method **semantically** creates a new object (vs. a methods which gives lazy access to a field which is built on demand), it should be named `createXXX`. The only exception should be when using the **builder** pattern, where the method should be named `build()` (usually just `build()`.

### Format of the jseshGlyph.jar file

This file, which contains the fonts, is currently a jar file, containing the svg files for the font, and a file called `list.txt`. In older versions, it used to contain two columns, one with the code of the glyph, and the other with the name of the file. We have now simplified this, and we list only the name of the file. for instance, the group ḥnꜥ would not be rendered as V28*(N35:D36) but more like V28\130*(N35:D36) (in fact, "short" versions of ayin and n would be used).


## TODO



### TODO / MANDATORY

Regarding **standard** codes:

- [ ] write a documentation about using JSesh as a library with and without user-defined signs.

### Long Term TODO

- [ ] Change the **column** layout algorithm. Currently, quadrats are processed the same way in columns and lines. But, in column, vertical sign tend not to be scaled. 
- [ ] Consider all occurrences of
   ~~~java
   String canonicalCode = ManuelDeCodage.getInstance().getCanonicalCode(code).code();
   ~~~

  and decide whether the surrounding code should be changed to use `CanonicalCode` instead of `String`.

- [ ] improve the structure of ElementDrawer:
  - the separation between ElementDrawer and DefaultElementDrawer doesn't serve any useful purpose, and is a bad attribution of responsabilities;
  - however, we should separate routing from actual drawing,
  - and we should do away with the mutable fields passed in the drawing process;
  - the actual drawing of each element could be performed by a dedicated and focused class.
- [ ] cleanup the parsing system, and remove the dependency of `TopItemList` to the builder;
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
- [ ] test the sign info editor, both in expert and user mode.

### Low priority TODO

- [ ] use the type system to differenciate between the various updates of the view (e.g replace `updateView()` by `updateDocumentView()`, `updateSelectionView()`, etc. Make `Selection` a type, an not a particular case of `TopItemList`.
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

- [x] ❗️rename `HieroglyphDatabaseInterface` into `HieroglyphDatabase`, and name implementations instead.
- [x] ❗️rename `SimpleHieroglyphDatabase` into `DefaultHieroglyphDatabase`.
- ~~rename mojos using the standard maven scheme.~~

### Simple TODO

- [ ] improve i18n for `JSignInfo`
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


### 2026-07-20


- [ ] remove cycles (except those related with the parser)
  - Introduce sub-packages for glyphs:
    - shape
    - fonts
    - signdata        
  - [x] remove dependency on `TopItemList` from `Possibility` (cleans up dependencies a lot)
  - [ ] move `HieroglyphDatabaseFactory` to `jsesh.default` ;
  - [ ] introduce a top-level `signcode` package (or `coremdc`)
  - [ ] glyphs should have sub-packages shape, fonts, signdata
- [ ] use i18n for texts in the JSesh Palette

glyphs has currently three internal cycles:

| cycle              | cause                                                                     |
| ------------------ | ------------------------------------------------------------------------- |
| data ↔ io          | only HieroglyphDatabaseFactory imports io                                 |
| data ↔ fonts       | HieroglyphShapeRepository needs the small HieroglyphCodesSource interface |
| fonts ↔ signsource | ExternalSignImporterModel needs HieroglyphShapeRepository to size A1      |


### 2026-07-19

- still trying to clean up packages dependencies in module jsesh. Lots of semi-automatic reorganisation. Will now:
  - [x] move back jsesh.ui.defaults to jsesh.defaults : it's not ui ;
  - [x] move glossary package
  - [ ] perhaps introduce an interface for `PossibilityRepository` 

### 2026-07-18

LLM-assisted reorganisation of packages in the `jsesh` module, documented in [[package_refactoring]].

### 2026-07-15

### Arabic localization

**almost done**

I have integrated the localisation proposed by our colleague Hany ZARIF. However, some part of it don't work very well with the Swing library, especially changing the whole application layout from left-to-right to right-to-left.

Plan:

1. [x] allow changes in the JSesh local from **user choice** (e.g. in preferences); we might survive if it requires a restart of the application;
2. [x] integrate arabic localization in the application
   1. [x] add arabic localization files from the localization branch;
   2. [x] change the occurrences of text which did not render correctly in right-to-left orientation (e.g. in the search window);


- [x] For correct arabic localization, we need rtol support for components layout;
  - look at https://www.javacodegeeks.com/2025/05/swing-on-steroids-modernizing-java-desktop-apps-with-flatlaf-and-jreleaser.html

- I don't remember when I introduced the following: the `jseshLabels` module now has a `update-labels` target which will automatically update the localized files by adding missing entries (it won't translate them, of course, but it will copy the english text, plus a warning comment asking for translation).

### 2026-07-13

- possible syntax error problem in MdC. Some texts produced by the ramses corpus contains strings such as `S38\\R`

### 2026-07-12

- [x] fixed bug in method `HieroglyphResourcesBuilder.buildFull(...)`
- [x] fix memory leak due to listeners when building the fonts for the search engine (the problem lies in `WildcardFont.addToFont(...)`, and somehow in the logic of `CompositeHieroglyphShapeRepository`, which has no method to release the listeners it holds.
- [x] TODO : while testing the search function, it occurred to us that some searched texts had "syntax error" (MdC syntax errors, not Egyptian Grammar ones!). We should write a small utility to find them easily and see whence the problem comes.

### 2026-07-09

- decide how we save the preferences about external hieroglyphic font source. It used to be done **twice**, which was not a good idea. (done).
- This being said, if we don't use JSesh defaults, but need the user folder, separate code for saving/loading it from preferences should be written.
- [x] Tests
  - [x] check that changing the hieroglyph font source works
  - [x] check that sign import works
  - [x] check that search works (with the extra codes and signs) **BUG/FIXED** (the `list.txt` was using the old format).
  - [x] ensure that the sofware can run if the user fonts points to a non-existing folder or to a file.

### 2026-07-09


- modified sign import and its relationship with `HieroglyphShapeRepository`. Now, `DirectoryHieroglyphShapeRepository` uses an observable reference to a folder, called `DirectoryHolder`. Thus, one can modify the `DirectoryHieroglyphShapeRepository` without touching it directly, just by acting on the `DirectoryHolder`. It separates nicely the responsabilities. We don't need specific modifications of `HieroglyphShapeRepository` anymore. 
- the modification of the default user defined `DirectoryHieroglyphShapeRepository` is managed by `UserFontDirectoryManager`.

- **TODO** : rename `Constants` to `HieroglyphFontConstants` ;
- still pending: where to use `CanonicalCode`;
- work on resources factories: how can be get the `HieroglyphShapeRepository` and other resources in a **simple** way, while keeping the ability to add new sources of glyphs if we want to.

### 2026-07-03


- [x] temporary fix: canonical Gardiner codes with a "H" ending should be normalized with a "h" ending. e.g. A23H should be normalized A23h. **Wrote test for this.**

- [x] add use of `jpackage` (through the `org.beryx.runtime` plugin) to produce the JSesh distribution. As a side note, it will be difficult to incorporate the text library in all distributions. We might consider providing a way to **download it from JSesh** itself.

- [ ] consider whether or not we replace strings by `CanonicalCode` in search engine.
- [ ] decide if `HieroglyphCodesSource.getCodes()`returns strings or canonical codes.
- [ ] todo: check that _BOLD signs can be used.
- [ ] look closely at the responsabilities of `ManuelDeCodage` and `GardinerCode`.
- [x] checked jpackage on windows (we still need to add a few things though)

- [x] solve the problem of signs canonization.
  - instead of Strings as codes, the `HieroglyphShapeRepository` we use a specific class, `CanonicalCode`. It's in fact an interface hiding a record.


### 2026-07-02

- lost lots of time because of [this bug](https://bugs.openjdk.org/browse/JDK-8372753). 
  - file associations don't seem to work on Mac (with my current environment)
    - I had to provide my own `Info.plist`
    - and to add code to copy the needed resources in the JSesh.app folder.
- work on jpackage
  - [x] check that the binary is able to handle very large files ;
  - [x] add auto-open for mac 
- [ ] maybe try to package more than one laucher.
- [ ] do the same for SignInfo editor.

### 2026-07-01

- [x] first generation of binary through jpackage for JSesh.

 
### 2026-06-27

- [x] **Renamed `HieroglyphDatabaseInterface` → `HieroglyphDatabase`** and `SimpleHieroglyphDatabase` → `DefaultHieroglyphDatabase` 

### 2026-06-24

A bit of cleanup: removed the old `utils/copyData.xml` **Ant script** and replaced it by the gradle `prepareResources` task.


### 2026-06-23

####  Problem with gradle build

- finished the handling of observable fonts; improved the architecture of the system (LLM are quite useful here) ;
- added tests for module `qenherkhopeshefUtils` (in the simple cases). Another LLM-assisted refactoring;
- The *cup* and *lex* tasks were running randomly when we ran `./gradlew build`, even when nothing had changed.
In fact, it was due to alternating between command line build and IDE build. The system recognized changes in the environment, and re-ran the `buildSrc` construction.

### 2026-06-22

- started working on the problem of observable fonts.

- In Swing components, prefer the use of `addNotify()` and `removeNotify()` to register and unregister listeners, rather than doing it in the constructor and in a `dispose()` method. 

- hand made test :
  - when changing the hieroglyphic font:
    - the editor is correctly updated;
    - the JMDCFields are correctly updated;
    - so is the hieroglyphic palette;
    - the "Gardiner" menu is not updated (it's made of icons, which are not recomputed) ;
    - the texts in the glossary are built **once**, using the current font, and then kept as icons.
- problem: the lex and cup tasks run every time we run `./gradlew build`. Needs fixing.

- **todo** : remove the frame in `JGlossaryEditor`. It should not be there.
  
### 2026/06/20

- created a branch (not pushed) to use an AST (Abstract Syntax Tree) for MDC parsing, separated from the editing model, and do away with the convoluted (and useless) use of interfaces for the builder.

- small cleanup.



### 2026/06/19

- [ ] **TODO** ? move back to gradle/groovy. Gradle Kotlin is a pain without IDE support - and I don't want to depend on IntelliJ.
- [x] ensure `nn` and `nTrw` are correctly processed as phonetic codes for `M22B` and `R8A` respectively. 
- [x] checked it's the case for the legacy fonts.
- [x] moved to Junit 5 for tests.
- [x] check import in various IDE
  - [x] VSCode (**ok**, it's the one I use).
  - [x] Netbeans 30: **ok**
    - be sure gradle is activated in Netbeans (simply open the netbeans preferences, look for java and then gradle; il will activate its support)
    - needs to run gradle build once first.
  - [x] Eclipse
    - first run `./gradlew build` and `./gradlew eclipse` to generate the eclipse project files.
    - then, in eclipse, use “import gradle project”
    - [x] problem with the file `version.properties` which is produced by gradle, and which is not found by eclipse.
  - [x] IntelliJ **ok**

- [ ] move `FileButtonMapper` to static method style.
- [ ] make `HieroglyphShapeRepositories` observable.
Here's the summary of what I found and recommended:


- [ ] FileButtonMapper refactor (~30 min): Convert to static methods — a self-contained cleanup with no architectural ripple.

- [ ] Start the observable HieroglyphShapeRepository (the top-priority item from the journal header): Add a listener interface, wire it into JSeshFullHieroglyphShapeRepository and JSeshApplicationModel.setFontInfo. This directly fixes the long-standing issue where changing the font folder requires clicking in windows to refresh.



### 2026/06/18

- added descriptions to gradle files
- simplified the `jseshGlyphs` module
- removed `nn` and `nTrw` as special cases. The codes are now processed just like any other phonetic code.
- removed the need for `prepareJSeshRelease`.
  
### 2026/06/17

Added file [[gradle-migration]] about the gradle migration built by **kyaneticblue**.

- [ ] deal with deprecation in `BundledActionFiller` (later)
- moved to gradle (done)
- ~~~still need to integrate the replacement of `prepareJSeshRelease` by gradle code~~~
- [x] try to remove **nanoXML** which is probably not used (removed everything linked to xml in this jhotdraw fork).
- [x] 🍰 added generic type parameters to all raw collections in `cupAndlex/src/main/java/JLex/Main.java`:
  - `CSpec`: `Hashtable<String,Integer> m_states`, `Hashtable<String,String> m_macros`, `Vector<CNfa> m_nfa_states`, `Vector<CNfa>[] m_state_rules`, `Vector<CDfa> m_dfa_states`, `Hashtable<SparseBitSet,CDfa> m_dfa_sets`, `Vector<CAccept> m_accept_vector`, `Vector<CDTrans> m_dtrans_vector`
  - `CBunch`, `CDfa`: `Vector<CNfa> m_nfa_set`
  - `CMinimize`: `Vector<Vector<CDTrans>> m_group`
  - `CLexGen`: `Hashtable<Character,Integer> m_tokens`
  - all local `Vector`, `Stack`, `Enumeration`, `Hashtable` variables and method parameters parameterized accordingly
  - all resulting redundant casts on collection element access removed
  - `SparseBitSet.elements()` now returns `Enumeration<Integer>`
  - two `(Vector<CNfa>)` casts on `.clone()` calls kept (unavoidable — `clone()` returns `Object`)

### 2026/06/12

- [x] **regarding lists**: introduce a generic adapter class, `ListItem<G>` or something like that, which could hold either :
  - a particular object of class `G`
  - or a label, typically something like "choose a town", etc... which would be used mainly for the first item.
  - applied to the palette and sign families.∏
  - done, but not yet applied.

- [x] fix the hyphen in transliteration problem.
- [x] renamed `JSeshGlossary` which was misleading (could suggest it was a Swing component) into `Glossary`.
- [x] enlarge the hieroglyphic search field
- [x] enlarge the hieroglyphic field in the glossary editor
- [x] see how the glyph rendering in the basic Gardiner list menu is performed, and possibly unify this (leave as is for now)
- [x] improve the handling of padding in the `HieroglyphPictureBuilder`. We tend to reduce the size of the icon according to the padding, but we also pass the padding as argument to the drawing method. It's not logical.
- [x] first draft of icon generation fix. Now we pass a dimension (w x h) to the icon builder, and not an icon height;
  - sign display is correct in the sign info editor main label
  - [x] **FIXED** but it's truncated in the palette (it was simply a copy/paste induced bug problem, where I used the height instead of the width for the dimension).
- [x] FIXME : the menu entry for the documentation has an empty text
- [x] added tests for R8A and M22B.

### 2026/06/09

- [x] fix icon building ;
- [x] unify icon production (sort of);


Note: it might be useful to design a `WithFolder` interface for some kinds of `HieroglyhShapeRepositories`, in order to have a consistent way to run and test our code.


### 2026/06/08

Plan for the next days:

- [x] check that a code is added when defining a variant or a "part of".
- [x] fix bug in sign import (and added a test);
- [x] fix bug when starting signinfo ;
- [x] menu-based SVG sign import result in a black square instead of the sign. 


### 2026/06/04

- **note** we have multiple classes for creating pictures: `HieroglyphPictureBuilder`, `MDCIconFactory` and `MDCDrawingFacade`. We should either document this or rationalise it.

  we should certainly move them to the same package.
 
  - `jsesh.hieroglyphs.utils.HieroglyphPictureBuilder`;
  - `jsesh.swing.utils.MDCIconFactory`;
  - `jsesh.mdcDisplayer.draw.MDCDrawingFacade`.
  
  Note that `HieroglyphPictureBuilder` does **not** analyse MDC. It draws signs directly from the font. 

- [x] **TODO** replace the size argument of `IconRenderOptions` by a dimension object (or something like that, read-only).

- some warnings in starting mvn:

  ~~~
  $./mvnw package
  
  WARNING: A restricted method in java.lang.System has been called
  WARNING: java.lang.System::load has been called by org.fusesource.jansi.internal.JansiLoader in an unnamed module (file:/Users/rosmord/.m2/wrapper/dists/apache-maven-3.9.9/3477a4f1/lib/jansi-2.4.1.jar)
  WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module
  WARNING: Restricted methods will be blocked in a future release unless native access is enabled
  
  WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
  WARNING: sun.misc.Unsafe::objectFieldOffset has been called by com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper (file:/Users/rosmord/.m2/wrapper/dists/apache-maven-3.9.9/3477a4f1/lib/guava-33.2.1-jre.jar)
  WARNING: Please consider reporting this to the maintainers of class com.google.common.util.concurrent.AbstractFuture$UnsafeAtomicHelper
  WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
  [INFO] Scanning for projects...
  ~~~

  Linked to modules and maven code (not JSesh code). It's supposed to be fixed in maven 4. But we might also move to gradle at some point...

- [x] check the SignInfo app.
- [x]❗️ icons content should either be downscaled if it doesn't fit in the icon, or the icon should be resized.
- [x] fix the sign palette display.
- [x] backport the support of hyphens in transliteration which was used in the former master branch, but deleted when merging with the development branch. 

### 2026/06/03

- SimplePalette : 
  - (modified to use MigLayout)
    - the starting height of the palette is too large ;
    - the area for displaying the current sign is too small
- [x] Merge with master branch
  - removal of font-related utilities (moved elsewhere). Removed file `MdC2Unicode-table.txt`. We need to keep track of this if we want to reuse the file (but more sophisticated Unicode integration is planned).


- [x] changing the hieroglyphic font source doesn't update the display of the windows, until we click in them. It should not be the case.

  ok. The problem is that we process the font information change in two steps.

  ~~~java
  public class JSeshApplicationModel ... {

   public void setFontInfo(FontInfo fontInfo) {
        jseshApplicationCore.setFontInfo(fontInfo); // a) change globally
        for (View v : application.views()) {
            JSeshView view = (JSeshView) v;
            view.setFontInfo(fontInfo); // change locally
        }
    }
  ~~~

  But the local change ends up modifying the `jseshStyle`, which doesn't know about the hieroglyphic font at all. In fact, formally, the hieroglyphic font is the same; **one** of its fields has been modified.

  The correct solution would be to make `HieroglyphShapeRepository` an observable. Each object using a `HieroglyphShapeRepository` could listen to it, and be notified when changes occur.


- [x] file encoding problem for labels in the search window.  **Encoding is now UTF-8 by default.**

#### Todo at the end of the merge:

- Classes to check in both branches:
  - `EventSupport`
  - `GlossaryEntry`
  - `JSeshGlossary`
  - `TranslitterationUtilities`
  - `TranslitterationUtilitiesTest`

- fix back so that transliterated values with hyphens (and dots...) are searchable (neglecting the hyphens/dots...)
- fix "translitteration" -> "transliteration"


### 2026/06/02

- TODO: by default, **if the standard JSesh font is available**, it should be used. Programmers will be disapointed to get the embedded font instead.


### 2026/05/29

- fixed the problem of document preferences. We need to check, however, that if we share jseshStyleReferences between document, document preferences are correctly handled.
- wrote an ADR about the previous problem.
- pass style and font kit to the glossary editor;
- finding one's way between `HieroglyphToolkit` and similar classes is painful. 
  - TODO:
    - make a list of those classes and of their uses;
    - rationalise their use and simplify the architecture.
    - document.
- In order to generate icons, we may use the icon generation system, or generate icons on the fly. In any case, we need to be able to configure the fonts for the generation system.
- renamed `ImageIconFactory` into `MDCIconFactory`



### 2026/05/28

- [x] when changing document properties, the change is visible in rendering, but is not saved in the document.
- [x] the glossary uses the basic font, not the actual one.
- [ ] currently, changing the hieroglyphic font folder changes a mere *field* of `JSeshFullHieroglyphShapeRepository`, and `JSeshFullHieroglyphShapeRepository` is not observable. Hence the lack of updates when it's changed.


  ~~~java
   /**
     * Apply the hieroglyphic font information to a shape repository.
     * @param shapeRepository
     */
    public void applyToShapeRepository(JSeshFullHieroglyphShapeRepository shapeRepository) {
        if (hieroglyphsFolder != null) {
            shapeRepository.setDirectory(hieroglyphsFolder);
        }
    }
  ~~~

  - possible solutions : make it observable, being careful about memory leaks;
  - add ad-hoc methods to force re-computation.

- [x] checked changing JSesh settings: the fonts changes are visible in all windows, and are correctly saved ;
- [x] copy/paste and copy/paste preferences seems ok.

### 2026/05/26

Checking the software:


- [x] when closing document properties, one gets 
  ~~~
  java.lang.ClassCastException: class java.lang.Float cannot be cast to class java.lang.Double (java.lang.Float and java.lang.Double are in module java.base of loader 'bootstrap')
    at jsesh.jhotdraw.preferences.document.ui.DrawingSpecificationsPresenter.updatePreferences(DrawingSpecificationsPresenter.java:119)
  ~~~

  **TODO** : either try to use double everywhere (which will be the best choice in the long run), or fix to use floats there. As the short-term goal is to get the software up and running, it's probably the best choice.

  fixed by replacing `Double` by `Float` in cast.

- [x] added call to `loadPreferences()` in `JSeshApplicationCore` constructor.

- [x] `JSeshApplicationCore.loadPreferences` is not called, hence the `this.exportPreferences` is null exception. It was called in the class constructor.

- [x] “gray” shading is not transparent; (fix: add transparency back)

- [x] when quitting JSesh, one gets:
  ~~~
  java.lang.NullPointerException: Cannot invoke "jsesh.jhotdraw.preferences.application.model.ExportPreferences.saveToPrefs(java.util.prefs.Preferences)" because "this.exportPreferences" is null
    at jsesh.jhotdraw.JSeshApplicationCore.savePreferences(JSeshApplicationCore.java:223)
    at jsesh.jhotdraw.JSeshApplicationModel.destroyApplication(JSeshApplicationModel.java:540)
  ~~~

- [x] when opening "preferences" one gets the exception:

  ~~~
  java.lang.NullPointerException: Cannot invoke "jsesh.jhotdraw.preferences.application.model.ExportPreferences.getGranularity()" because "exportPreferences" is null
  	at jsesh.jhotdraw.preferences.application.ui.JExportPreferences.setExportPreferences(JExportPreferences.java:110)
  ~~~

  (fixed by calling `loadPreferences()` in the constructor of `JSeshApplicationCore`)

- [ ] on the mac (at least), iconified window are not deiconified easily. After using **command-M** (menu Window/reduce), the window is iconified, but clicking on the app icon on the deck doesn't do anything. One needs to select the iconified window in the Window list, or to right-click on it on the JSesh icon. 



### 2026/05/18

- [x] Problem with actionMap creation. We have moved `v.setActionMap(createViewActionMap(v));` *after* `model.initView(this, v);`, because we wanted to have a fully initialized view when creating the action map. But if the view itself creates its own actions, this will erase them. The solutions would be either to add a view-specific step *after* `v.setActionMap(createViewActionMap(v));` (which is in this case badly named), to replace creation with **update**, or to revert to the original organization. Currently, the only problematic entry is `EditorAction` which needs an editor as argument. It's placed in the application model mainly because we probably thought it had nothing to do in the view itself, and was too "application-level", as it opened a dialog. **Fixed by reverting to the original organization and making *edit group action* an AbstractViewAction**

- Note that the previous problem would not exist if we had a factory method for creating the view instead of using introspection. 


- `GnutraceHieroglyphShapeRepository` worked with biliteral signs and not with uniliteral signs: at some point, I started removing phonetic codes from the map, but I stopped before it was complete. Hence, some signs had phonetic codes built-in the `GnutraceHieroglyphShapeRepository` and other did not.

- When running the `MDCEditorDemo`, using only the old font, unilitary signs are not correctly displayed. In the old version, we had the following code:

  ~~~java
  public class DefaultHieroglyphicFontManager implements HieroglyphicFontManager {}
  
    ...
  
  	@Override
  	public ShapeChar get(String code) {
  		String newCode = code;
  		// TODO Awful patch for now. This should move to another class. The
  		// font manager should
  		// associate glyphs codes to drawings ;
  		// a code manager should associate mdc codes to glyphs codes.
  		if (!GardinerCode.isCanonicalCode(code))
  			newCode = HieroglyphDatabaseRepository.getHieroglyphDatabase()
  					.getCanonicalCode(code);
  		return composite.get(newCode);
  	}
  }
  ~~~
  
  The corresponding code is now in `JSeshFullHieroglyphShapeRepository`:
  
  ~~~java
  @Override
  	public ShapeChar get(String code) {
  		String canonicalCode = ManuelDeCodage.getInstance().getCanonicalCode(code);
  		return composite.get(canonicalCode);
  	}
  ~~~
  
  But the new system does not install this repository as the default one, hence the problem.
  
  In all cases, there is a logical problem here, as the function of `HieroglyphicFontManager` is ill defined. Some of its implementation expect canonical codes, other can perform the canonicalization themselves. 
  
### 2026/05/16

- [x] fixed NPE when creating a `JMDCField` and `JMDCEditor`. It was basically a problem with preferredSize not being set.

Identified a problem with the current approach in `JHotDraw`.

Currently, we have:

~~~java
class AbstractApplication ... {
  public final View createView() {
    View v = basicCreateView();
    v.setActionMap(createViewActionMap(v));
    return v;
  }

  protected View basicCreateView() {
    return model.createView();
  }
    
  public void add(View v) {
      if (v.getApplication() != this) {
          int oldCount = views.size();
          views.add(v);
          v.setApplication(this);
          v.init();
          model.initView(this, v);
          firePropertyChange(VIEW_COUNT_PROPERTY, oldCount, views.size());
      }
  }

~~~

And a typical example of view creation is:

~~~java
p = app.createView();
app.add(p);
app.show(p);
~~~

Now, the existence of both `v.init()` and `model.initView(this, v)` suggest that the view is not complete when `createView` returns. In this respect, calling `v.setActionMap(createViewActionMap(v));` might be problematic. We can move it *after* calling `model.initView(this, v)`, but it remains that the view creation sequence is fragile and left to the programmer to manage.

More recent versions of JHotdraw improve the system, using a factory instead of introspection, but the ordering problem remains. Our current solution is somehow a patch, but not perfect. The right thing to do would be to improve the life cycle and simplify it for the programmer. Basically, a creation method which would take a the application and the view as arguments would be reasonnable. Another point is that `Application` and `View` could be generic, with a parameter which would be their respective models.

### 2026/05/15

- The whole software compiles.
- I have had a discussion with Claude about the design of some parts, which might lead to some changes.
- running the software is a bit more complex than anticipated. The singletons did hide some initialization problems, and I have a few NullPointerExceptions to solve.
- [ ] I have tried to create a `JMDCEditor` and interact with it, but it seems to have problems passed the first steps. codes like `A1`, `mn` or `nfr` work, but not unilitary values like `n`.
- [ ] `MDCFieldDemo` creates a `JMDCField` and displays it. But it throws a NullPointerException.

### 2026/04/27

TODO next: see how I handle the opening of sign info file in normal and expert mode in the original app, and get sure the behaviour is the same.

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

####‡ TODO

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


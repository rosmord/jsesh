# TODO

Franglish file, mainly for personnal use.


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
- [x] ensure we use only **one** system for defaults; we don't want to go back to the use of random singletons. **DOCUMENT IT** (as a way to ensure it's coherent).
- [x] make the hieroglyphic font observable, so that **all** components which display hieroglyphs can be notified when they are modified - take care of possible memory leaks.
- [x] When the software compiles, replace all variable named "drawingSpecifications" by jseshStyle.
- [ ] consider removing `depth` in layout;
- [ ] when the new version is functional, think about the lifecycle of Layout objects ; it might be interesting to simplify it. They should probably be short-lived objects.
- ~~rename `HieroglyphicFontManager` into ShapeCatalog~~
- [ ] refactor the whole business around hieroglyphs to make it more logical.
- [ ] separate JSeshStyle into two parts: one with the features which are likely to be shared, and one with features which are probably specific to a particular document. I'm not sure it's that useful, this being said.
- [x] In the JHotdraw linked part, move **down** (to JSeshViewModel) what can be moved down, possibly keeping `JSeshView` as a facade.

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

## Bug fixes

## In work


- flatten the current *Layout* class/hierarchy. It has only one implementation, and we should probably simplify it, which would allow us to clarify its relationship with drawing preferences.

## Next version
- Add export to XML/JSON for MdC documents

## In work

## High priority

* full regular expression language.
* Define document dimensions
* Redefine MDCPosition to point to anywhere in the text, not just quadrat limits.

## Easy to do

* Horizontal and vertical grouping : do the same as ":" or "*" addition
* add circular enclosure.

## Strategic projets

* mise en page (taille du texte, taille de page)
* Système de tabulation:
    * Horizontal et/ou vertical
    * contrôle éventuellement sur la totalité de la page (ou d'une zone quand ça fonctionnera)
    * contrôle du système d'alignement entre deux tabulations : espace à gauche, à droite ou justification
    * On conserve le ? de Winglyph. (à voir)
    * Sinon:
    ~~~~~~~~ 
    Code: %[label=1,justification=fill,orientation=h]
    ~~~~~~~~
    Joli rouge CMYK : 0 52 62 7
* par défaut, l'orientation est contextuelle. 
* le label est obligatoire
* On décidera ensuite de la portée des justifications.
* un changement de page EXPLICITE les efface certainement
* %[clear] aussi.

## FUTURE Project.
* zones
* ligatures "à la RES"
* annulation de la calibration (d'une ligne, d'un cadrat), à la fois horizontalement et verticalement.
* Modification complète du système de dessin, en explicitant les éléments graphiques.
  Le dessin serait alors complètement mécanique. *Toute* la mise en page serait faite dans layout.
  Le lien entre éléments graphiques et éléments du modèle serait gérée par une map.
  
  Intérêt : on a actuellement, pour certains éléments, de la logique dans le layout et dans le dessin
  (en particulier pour les éléments à taille variable), ou pour les éléments graphiques non trouvés.
* mode "export", avec les signes spécifiques utilisés
* A propos de la mise en page: idée... on trouve les éléments de modèle dont la mise en page est à refaire,
et on empile les ordres de mise en page (ou, alternativement, on remet en page directement).
* Un même ordre peut peut-être être déclenché plusieurs fois (cas d'un élément déformé ? aligné ?)
* export PDF (presque) mis en page
* REVOIR STRUCTURE DU PROGRAMME. EN PARTICULIER NOTION DE DOCUMENT...
* SIMPLIFIER LES DIFFERENTS "EXPERTS" (Layout/Dessin, etc...). Dans ce programme,
  on n'a probablement pas besoin d'autant de possibilités de paramétrage (quel intérêt
  à pouvoir changer à la volée le système de dessin ???????)
* REVOIR TopItemList/HieroglyphicTextModel/MDCDocument
* PROBABLEMENT DEPLACER LA CONNAISSANCE DE L'ORIENTATION DU DOCUMENT DANS TOPITEMLIST !!!
* réduire le corps ?
* paramétrage raccourcis claviers (fait sous mac)
* putClientProperty() sur mac (cf. choix des fontes)
* édition de l'intérieur des structures (pb. d'ergonomie)
* About horizontal/vertical text : I realise that I should probably use specific settings,
 as interline space is something very different in vertical and horizontal context.
 I should probably have a set of settings for vertical documents, and another for horizontal ones.
 Another problem is that the good settings are very document dependant in vertical texts. 
 The current default for JSesh favours relatively wide columns, for instance, 
 which don't look nice at all if their content doesn't fill them.
* (message from N.S.) OK, I've tried it and I see how to do it. 
You might want to find more accurate descriptive terms for it, since "line spacing"
makes no sense in English for vertical text. The effect of changing current "Line Spacing"
is actually, in vertical texts, reducing the space between quadrats, and can thus be confused 
with "Space between quadrats".
* ALSO: the undo command seems not to apply to Document Properties.
* Suggestion: any chance of an "Apply" button in the Document Properties box so 
  that one doesn't have to keep closing and opening it to see the effects of the changes?
  This is a nice feature that e.g. lots of Adobe applications have.
* Add margins to EPS files
* Fonction de mise en page intelligente : génère n:A1\70 pour n:A1 - ou mieux, n[noscale]:A1 ?
* Redéfinir les différentes représentations du texte édité et leurs relations.
  Il serait en particulier important de dire quelles informations sont conservées:
    * dans le texte lui même
    * dans la partie qui représente le document (avec nom de fichier, codage, etc.)
* Système de préférence générique (pb interne)

## Varia IFAO
* choix des couleurs pentones ?
* tabulations
* rouge = souligné
* trucs et astuces dans la doc
* considérer tout signe avec pos non 0,0 comme un groupe potentiel ?

## Probably not (except if someone else does it)

* possibilité de paramétrer les raccourcis

## Annoying bug

The app framework uses setHasUnsavedChanges to change its state each time a change occurs.

Now, this is a bit annoying : we should have a computed, dynamic, method for this, not a boolean value. It makes keeping track of
modifications difficult.


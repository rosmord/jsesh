# TODO

Franglish file, mainly for personnal use.

## Bug fixes

## High priority
* définir les dimensions des documents
* Redéfinir MDCPosition
* Automate installer building again.

## Easy to do
* Modif rapide: la modification des signes doit agir sur la sélection quand il y en a une.
* PLUS OU MOINS FAITE, À VÉRIFIER SYSTÉMATIQUEMENT : Modif rapide (plus ou moins) :  quand les commandes 
  ont besoin d'une sélection et qu'il n'y en a pas, faire
  quelque chose de raisonnable (e.g. dernier ou deux derniers cadrats, etc...).
* finir enceinte crénellée
* ajouter enceintes circulaires

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

FUTUR
* zones
* ligatures "à la RES"
* annulation de la calibration (d'une ligne, d'un cadrat), à la fois horizontalement et verticalement.
* Modification complète du système de dessin, en explicitant les éléments graphiques.
  Le dessin serait alors complètement mécanique. *Toute* la mise en page serait faite dans layout.
  Le lien entre éléments graphiques et éléments du modèle serait gérée par une map.
  
  Intérêt : on a actuellement, pour certains éléments, de la logique dans le layout et dans le dessin
  (en particulier pour les éléments à taille variable), ou pour les éléments graphiques non trouvés.


## Would be nice
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


## Unsorted

* About horizontal/vertical text : I realise that I should probably use specific settings,
 as interline space is something very different in vertical and horizontal context.
 I should probably have a set of settings for vertical documents, and another for horizontal ones.
 Another problem is that the good settings are very document dependant in vertical texts. 
 The current default for JSesh favours relatively wide columns, for instance, 
 which don't look nice at all if their content is don't fill them.

* (message from N.S.) OK, I've tried it and I see how to do it. 
You might want to find more accurate descriptive terms for it, since "line spacing"
makes no sense in English for vertical text. The effect of changing current "Line Spacing"
is actually, in vertical texts, reducing the space between quadrants, and can thus be confused 
with "Space between quadrants".


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

Varia IFAO:
* choix des couleurs pentones ?
* tabulations
* rouge = souligné
* Dessin de bastion
* trucs et astuces dans la doc
* considérer tout signe avec pos non 0,0 comme un groupe potentiel ?

## Probably not (except if someone else does it)

* possibilité de paramétrer les raccourcis

## Recently Done

### version 7.1

* Searches (DONE)
* Include text names in texts. (DONE)
* fix Neith sign C303 (DONE)
* Upon a suggestion by N. Strudwick, remove the error cases when exporting.
    * the initial suggestion was to export all the document 
      if no selection is available
    * implementing it would delay the release of this version of JSesh. I prefer
      proposing a reasonable solution: exporting the current page.
    * bitmap and RTF are already ok.
    * EMF, WMF, MACPICT, EPS, SVG : TODO use Bitmap logic DONE
    * PDF : ok, except encapsulated pdf. Export current page. DONE
    * quick pdf export : export current line if no selection. DONE
* unify exports : use the standard file export chooser everywhere. DONE
* (N.S.) Another question: the open and save dialogue boxes use the standard Mac sidebar etc. 
  But the Export as dialogue uses something custom that does not work in the same way. 
  I suspect this is breaking the user interface guidelines...

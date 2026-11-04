Vers une rationalisation des propriétés de mise en page

# Propriétés du document:

- taille de A1 (unité)
- espace interligne
- ligne de base du texte alphabétique
- espace inter-quadrats 
- espace inter-colonnes
- hauteur maximale d'un quadrat
- largeur maximale d'un quadrat
- taille pour "petit corps" (unité absolue)
- largeur de la ligne d'un cartouche

On pourrait ajouter:

espace inter-quadrats (vertical) et espace inter-quadrats (horizontal)

# Propriété des zones:

**tout est exprimé en hauteur de quadrat**

largeur de la zone
hauteur de la zone
inter ligne ou colonne
inter quadrats
filet (épaisseur)
largeur des cadrats
hauteur des cadrats
cadre

Ajouter:

au minimum, un élément qui règle la taille de référence des **signes** (hauteur de A1 ?)

Modifier éventuellement:

- largeur de colonne : largeur max d'une colonne individuelle - en fait, actuellement largeur de cadrat ?
- ~~pour les textes en ligne, doit-on avoir une propriété "largeur max de cadrat?"~~

**Modification suggérée**:

Pour le **Document** :

- notion de **taille de référence** (hauteur de A1) utilisée à la fois pour la hauteur de A1 et pour la hauteur du quadrat en **ligne**

- pour les zones : hauteur de A1 (en % de la hauteur déclarée pour le document dans son ensemble)
- utilisée comme hauteur de quadrat pour le texte en ligne des zones (cette information disparaît donc)

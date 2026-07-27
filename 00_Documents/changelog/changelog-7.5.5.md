*Released 2020/11/11*

JSesh 7.5.5 is out !

I have uploaded a [Youtube video about it](https://youtu.be/675QNSflmnw).

(If you have problems installing it, please refer to [this post](https://jsesh.qenherkhopeshef.org/fr/news/news20200316)).

JSesh 7.5.5 contains a number of improved features :

- the search system has been improved, and is much faster now
- a system to add new line numbers efficiently ;
- export in Unicode format of the hieroglyphic text (when possible)
- support for the new U+A7BD support of yod (see the page on [transliteration](/varia/transliteration))
- at least, "enclosure" signs are drawn correctly
- moved to Java9, as Java8 support is going away...

#### Improved Search system

In JSesh 7.4.2, you had an option to limit the length of the matched text. Obviously, it was only
useful when the search included a "*" character.

Now, this search was slow. I have replaced it with a possibility to limit the length of the text skipped by "*".
It's probably more straightforward, and way faster.

As an example, the following search 

Will look for sequences of signs which start with i-A2, contain either r or rw, and end with A1. The "*" represents a number of arbitrary glyphs. Their number can be limited with "max skip length" (here, 3).
The \[...\] allows to search for a number of alternative glyphs (here either  r or rw).

### Bug fixes

- new files, when saved, will be correctly understood as ending with a ".gly" suffix.
- fixes to the group editor : rotation and scaling were very difficult to use.

### Data and texts

- As always, information about a few signs has been added/updated
- working on Horus and Seth (P. CB I), I have found and fixed a few typos.

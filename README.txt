Maven-oriented JSesh distributions. 
Contains the following folders and modules:

comments: various text files I keep about JSesh...
cupAndlex : a bundle with CUP and LEX. Probably usable for other projects as a maven MOJO.
cupruntime : the CUP runtime, used by JSesh
jhotdrawfw : the "application framework" part of JHOTDRAW 7.
jsesh : the main jsesh library (we will remove the application part from it)
jsesh-installer : the IZ pack installer
jseshAppli : a module containing the new JSesh application (using JHotdraw)
jseshDoc : the module for JSesh documentation
jseshGlyphs : the main hieroglyphic font
jseshMac : the mac JSesh application (will be replaced by jseshAppli)
jseshOthers : non-mac application jar (will be replaced by jseshAppli)
jvect : vectorial copy/paste library.
prepareJSeshRelease: a maven mojo for preparing the release (mostly indexing the fonts)
utils : utilities to update some data files. nothing of interest.


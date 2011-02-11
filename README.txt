Maven-oriented JSesh distributions. 
Contains the following folders and modules:

First, modules and files for the current JSesh version :

comments: various text files I keep about JSesh, and some unused code (unformal tests and proof-of-concept)
cupAndlex : a bundle with CUP and LEX. Probably usable for other projects as a maven MOJO.
cupruntime : the CUP runtime, used by JSesh
jsesh : the main jsesh library (we will remove the application part from it)
jsesh-installer : the IZ pack installer
jseshDoc : the module for JSesh documentation
jseshGlyphs : the main hieroglyphic font
jvect : vectorial copy/paste library.
prepareJSeshRelease: a maven mojo for preparing the release (mostly indexing the fonts)
utils : utilities to update some data files. nothing of interest.

Among those, the following modules and files are quite likely to disapear in the next versions :
jseshMac : the mac JSesh application (will be replaced by jseshAppli)
jseshOthers : non-mac application jar (will be replaced by jseshAppli)


Then, modules and files for the NEXT JSesh version :
jhotdrawfw : the "application framework" part of JHOTDRAW 7.
jseshAppli : a module containing the new JSesh application (using JHotdraw)



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
jhotdrawfw : the "application framework" part of JHOTDRAW 7.
jseshAppli : a module containing the new JSesh application (using JHotdraw)

Among those, the following modules are obsolete :
jseshMac : the mac JSesh application (will be replaced by jseshAppli)
jseshOthers : non-mac application jar (will be replaced by jseshAppli)

Working with eclipse:
	there seems to be a problem with the eclipse plugin for maven regarding generated sources:
	the "jsesh" maven module will report missing classes in eclipse.
	A quick fix:
	* import the JSesh-all project in eclipse (import maven project)
	* open the jsesh module, and, on the pom.xml file, select "run as/maven package".
		This will create the missing files
	* then, you need to add the corresponding folders to the jsesh eclipse project as "source folders".
		In the jsesh module, open target/generated sources
		Then right click on target/generated sources/cup, and select the menu entry "build path/use as source folder".
		do the same for the target/generated sources/lex folder.
That's it.

Mac OS X distribution:
    Bundled JRE for mac os X distribution need :
    a) to patch flavormap.properties:
        add the line PDF: application/pdf
        (allows copy/paste of PDF)

    P.S. see if we can handle this as EMF ?

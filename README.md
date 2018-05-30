#Maven-oriented JSesh distributions. 
Contains the following folders and modules:

### Maven modules for JSesh

#### Libraries
* cupAndlex : a bundle with CUP and LEX. Probably usable for other projects as a maven MOJO.
* cupruntime : the CUP runtime, used by JSesh
* jsesh : the main jsesh library (we will remove the application part from it)
* jseshDoc : the module for JSesh documentation
* jseshGlyphs : the main hieroglyphic font
* jvect : vectorial copy/paste library.
* prepareJSeshRelease: a maven mojo for preparing the release (mostly indexing the fonts)
* utils : utilities to update some data files. nothing of interest.
* jhotdrawfw : the "application framework" part of JHOTDRAW 7.

Among those, the following modules are obsolete :
* jseshMac : the mac JSesh application (will be replaced by jseshAppli)
* jseshOthers : non-mac application jar (will be replaced by jseshAppli)

#### Softwares

* jseshAppli : a module containing
  the JSesh application (using JHotdraw)
* jsesh-installer : everthing related to installing JSesh
* codeDumper : a small software to create a dump of JSesh files, 
  as simple lists of codes.


## Files and Folders not part of the general maven architecture

* README.md : this file
* TODO.md : ok, a TODO file
* comments: various text files I keep about JSesh, 
    and some unused code (unformal tests and 
    proof-of-concept)

## Working with eclipse:
there seems to be a problem with the eclipse plugin for maven regarding generated sources:
the "jsesh" maven module will report missing classes in eclipse.

A quick fix:

* import the JSesh-all project in eclipse (import maven project)
* open the jsesh module, and, on the pom.xml file, select "run as/maven package".
    This will create the missing files
* then, you need to add the corresponding folders to the jsesh eclipse project as "source folders".

    * In the jsesh module, open target/generated sources
	* Then right click on target/generated sources/cup, and select the menu entry "build path/use as source folder".
		do the same for the target/generated sources/lex folder.
		
That's it.

## Building distributions

Due to changes in Java distribution and on Windows and Mac OS X as platform 
(with a strong bias against softwares not distributed through their respective
stores), the previous java-only distribution system has changed.

JSesh will now embed its own version of Java - at the present time, it will
be java 8.


The files here are used in the last, non-automated phase of building a
JSesh distribution. 

I will probably try to automate everything at some point, this being said.

------------------------------------
### Building a Mac Distribution:

build the whole project: "mvn install".

1. all files are in jsesh-installer/target/mac. cd there.

5. Ensure main.sh is executable in both apps (JSesh.app and SignInfo.app)

3. place jre 8 in the Contents folder of JSesh.app, with "jre" as folder name.

4. (maybe not needed anymore ?) Bundled JRE for mac os X distribution need to patch `flavormap.properties`:
        add the line PDF: application/pdf (allows copy/paste of PDF)
        P.S. see if we can handle this as EMF ? (check if still needed
        anyway)


6. Check if JSesh and SignInfo are functional (they should start if you double click on them).

7. make a package (.pkg) with "Packages" by Stéphane Sudre.
    See JSesh-dist.pkgproj for a config file for the Packages software for Mac.

------------------------------------
### Windows distribution

1. copy the files from target/windows into a Windows machine
2. copy a 32 bit JRE in the JSesh folder on Windows. Ensure it's named "jre".
3. start lauch4J and use the jsesh-bundler.xml file. It should create JSesh.exe in the JSesh folder.
4. same for the file signInfo-bundler.xml
5. run InnoDB on jsesh-inno.iss 
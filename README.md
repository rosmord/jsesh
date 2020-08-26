# JSesh sources
Welcome to JSesh sources! 

**JAVA 9+ (actually Java 12) version: **


JSesh is a Java hieroglyphic editor 
developed by Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)

JSesh developpers :
- Serge J.-P. Thomas : most of the fonts
- S. Rosmorduc : almost all the code, a few glyphs in the fonts
- Wayne Collins (CVS, ant improvement)


Licences for the various libraries used by JSesh can be found in resources/licenses



## Source Content 
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

JSesh will now embed its own version of Java


The files here are used in the last, non-automated phase of building a
JSesh distribution. 

I will probably try to automate everything at some point, this being said.

------------------------------------
### Building a Mac Distribution:

build the whole project: "mvn install".

1. all files are in jsesh-installer/target/mac. cd there.

5. Ensure main.sh is executable in both apps (JSesh.app and SignInfo.app)
~~~
$ find . -name main.sh -exec chmod a+x {} \;
~~~

3. build a jre for JSesh (check if your path is correct before).

~~~
$ cd JSesh.app/Contents
$ MODULES=java.base,java.desktop,java.naming,java.prefs,java.sql
$ jlink -G -c --no-header-files --no-man-pages --add-modules  $MODULES --output jre
~~~
  (we should identify why on earth java.sql is needed. This being said, it's very small,
   so no harm done.)


6. Check if JSesh and SignInfo are functional (they should start if you double click on them).

7. make a package (.pkg) using the application "Packages" by Stéphane Sudre.
    A config file is provided : JSesh-dist.pkgproj.


------------------------------------
### Windows distribution

1. copy the files from target/windows into a Windows machine
2. copy a 32 bit JRE in the JSesh folder on Windows. Ensure it's named "jre".
3. start lauch4J and use the jsesh-bundler.xml file. It should create JSesh.exe in the JSesh folder.
4. same for the file signInfo-bundler.xml
5. run Inno Setup on jsesh-inno.iss. Generate a new ID for the build before building.

*Note : we will probably use jlink as above to generate the JRE*


## Note about github distribution (for personnal use mainly)

To get the number of downloads for version 7.2.0 :

~~~~~~~~~~~~~
curl -i https://api.github.com/repos/rosmord/jsesh/releases/11259307
~~~~~~~~~~~~~

Remove the last number for all releases.

# JSesh sources

See [the JSesh web site for information about the use of the software](https://jsesh.qenherkhopeshef.org/).



JSesh is a Java hieroglyphic editor mostly developed by Serge Rosmorduc (serge.rosmorduc@qenherkhopeshef.org)

JSesh developpers :
- Serge J.-P. Thomas : most of the fonts
- Serge Rosmorduc : almost all the code, a few glyphs in the fonts
- Wayne Collins (CVS, ant improvement)


Licenses for the various libraries used by JSesh can be found in resources/licenses

## Building JSesh

JSesh sources can be loaded in an IDE (Eclipse, Netbeans, IntelliJ, VSCode), or built from the command line using Gradle.

### Using Gradle

Building the sources
: `./gradlew build`

Running JSesh directly (from the root folder of the project)
: `./gradlew jseshAppli:run`

Running the sign info editor
: `./gradlew signInfoAppli:run`

Building the distribution for the current platform
: `./gradlew jpackage`

You will find the distribution in `jsesh-installer/build/jpackage`.


## Source Content 

The sources contain the following folders and modules:


#### Libraries

* cupAndlex: bundle with CUP and LEX. Probably usable for other projects as a maven MOJO; those are used to parse Manuel de Codage files;
* cupruntime: the runtime for CUP;
* jhotdrawfw: the "application framework" part of JHOTDRAW 7, adapted from [JHotDraw 7 by Walter Randelshofer](https://www.randelshofer.ch/oop/jhotdraw/);
* jsesh: the main jsesh library;
* jsesh-installer: the system for building JSesh distribution;
* jseshGlyphs: the main hieroglyphic font;
* jseshLabels: the labels for menus, buttons, etc. in Jsesh all in one place to ease translation of the software;
* jseshSearch: the search module;
* jseshTests: various small softwares used to check JSesh runs correctly. Only interesting if you develop JSesh;
* qenherkhopeshefUtils: sundry utilities to help writing JSesh and Swing softwares. Some are outdated (the guiFramework has been replaced by jHotdraw);
* signInfoAppli: the editor for sign information.


## Things removed from JSesh

- All demonstrations for programmers have moved to the project [jseshDemos](https://github.com/rosmord/jseshDemos);
- utilities softwares belongs to the project [jseshUtils](https://github.com/rosmord/jseshUtils);
- the texts are available in [MDC-texts](https://github.com/rosmord/MDC-texts) (but there is a copy of them in JSesh).


#### Weird compilation behaviour

I have just lost a few hours because JSesh wasn't compiling correctly anymore. It seems that the problem was
due to some IDE project files with a wrong path (in particular, with a wrong path regarding to the generated files.

If you have the following behaviour :

- initial compile is ok, and build files from the CUP and JFlex folder ;
- further builds complain that `MDCParse` doesn't exist

it might be worthwhile deleting `.classpath` and `.project` files in your JSesh folder, and trying again.


## Files and Folders not part of the general Gradle architecture

* README.md : this file
* TODO.md : ok, a TODO file
* comments: various text files I keep about JSesh, 
    and some unused code (unformal tests and 
    proof-of-concept)

## Working with IDEs

### Working with VSCode

With the gradle plugin, JSesh can be edited with VSCode. I did the following to ensure the code is up to date when running the software:


**.vscode/launch.json** file:

~~~json
{
    // Use IntelliSense to learn about possible attributes.
    // Hover to view descriptions of existing attributes.
    // For more information, visit: https://go.microsoft.com/fwlink/?linkid=830387
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Current File",
            "request": "launch",
            "mainClass": "${file}"
        },
        {
            "type": "java",
            "name": "JSesh application",
            "request": "launch",
            "mainClass": "jsesh.jhotdraw.Main",
            "projectName": "jsesh-all-jseshAppli",
            "preLaunchTask": "Build JSesh application classes"
        },       
        {
            "type": "java",
            "name": "Sign Info Application",
            "request": "launch",
            "mainClass": "jsesh.utilitysoftwares.signinfoeditor.Main",
            "projectName": "jsesh-all-signInfoAppli",
            "preLaunchTask": "Build SignInfo application classes"
        }
    ]
}
~~~

**.vscode/tasks.json** file:

~~~json
{
    "version": "2.0.0",
    "tasks": [
        {
            "label": "Build JSesh application classes",
            "type": "shell",
            "command": "./gradlew",
            "args": [
                ":jseshAppli:classes"
            ],
            "options": {
                "cwd": "${workspaceFolder}"
            },
            "group": "build",
            "problemMatcher": []
        },
        {
            "label": "Build SignInfo application classes",
            "type": "shell",
            "command": "./gradlew",
            "args": [
                ":signInfoAppli:classes"
            ],
            "options": {
                "cwd": "${workspaceFolder}"
            },
            "group": "build",
            "problemMatcher": []
        }
    ]
}
~~~

### Working with eclipse:

There seems to be a problem with the eclipse plugin for maven regarding generated sources:
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

## Working with VSCode

Interaction between VSCode and Maven is not that easy (again, because of generated files).

I have had problems with java versions. I tend to have **multiple** java version on my machine, and vscode plus maven don't use the same one if the **default** java version is not the one I need. It's pretty annoying.

Also, vscode doesn't understand it needs to run `mvn install` for the plugins. So you must do it yourself.

Either run `./mvnw install` in the root of the project on the command line, or use the vscode maven interface, selecting *maven/JSesh Complete distribution/Lifecycle/install*.

The use the vscode command **Java: Clean Java Language Server Workspace** to get vscode to understand the generated files.

## Updating data

This is only done if you are in charge of updating the JSesh text base or the JSesh sign base (that is, if I did stop maintaining JSesh, and you decide to take over).

**That is, you most probably don't need to do it.**

Beware if you do it, it can delete files you need in JSesh (and you can use git to restore them).


To update the JSesh external data, run:

~~~bash
./gradlew prepareResources
~~~

It uses path from my own computers to find the original data (you can find the said path in `gradle.properties`)


## How to collaborate on JSesh


There are many ways you can help on JSesh:

- you can report bugs and make suggestions, either by sending me an email, or by using the [github issue tracker](https://github.com/rosmord/jsesh/issues);
- you can help with the [translation](#helping-with-translation) of the software;
- you can help with the [documentation](#helping-with-documentation) of the software;
- you can send texts for the JSesh text base;
- you can design hieroglyphs in SVG;
- you can [help with the code](#coding-jsesh) and fix bugs or add new features.

### Helping with Translation

To help with the translation of JSesh, you need to get the source code (you are at the right place). If you know how to use git, it's simpler. If not, you can download the [sources as a zip file](https://github.com/rosmord/jsesh/archive/refs/heads/master.zip).

The files you need to edit are placed in the folder `jseshLabels/src/main/resources/jsesh/resources`. There is a file for each supported language. Each file name is of the form `labels_XX.properties`, where `XX` is the language code (for instance, `fr` for French, `en` for English, etc. The list of those **two letter** codes corresponds to the [Iso 639 Standard](https://en.wikipedia.org/wiki/List_of_ISO_639_language_codes)). The default language is English, and the corresponding file is `labels.properties` (without any language suffix).

If you want to add a new language, simply copy one of them (probably the english one), rename it, and edit it. **Only change the right side of entries**, and leave the lines with “AcceleratorKey” or “shortcut” alone.


Most lines are relatively easy to translate, such as

~~~properties
edit.insertNextLineNumber.text=Next Line Number
~~~

which gives in the French translation:

~~~properties
edit.insertNextLineNumber.text=numéro de ligne suivant
~~~


Other contain specific placeholders, such as `{0}` or `{1}`. They will be replaced by text or numbers by the software. Suppose they are a word in your sentence and place them logically.

For instance:

~~~properties
file.quickPDFExportFolder.ok=Selected {0} for PDF Output
~~~~

gives in the French translation:

~~properties
file.quickPDFExportFolder.ok=Les exportations pdf seront dans le dossier {0}
~~~

Once you have translated the file, you can send it to me by mail (or use a pull request if you know how to use git). Please consider that JSesh is not a founded project, that we do it in our free time, and try to avoid unnecessary work for us. So keep the order of the lines as is.

### Helping with Documentation

### Coding JSesh

If you want to help with the code, the best way is to **fork** the project on github.

Get in touch with me (either through issues or by mail) to discuss what you want to do. Of course, you can also propose a genuine fork if you want to explore something completely different. But if you want to contribute to the main project, please discuss it with me first.

Note that I take care of having a clean codebase, and try to follow the same coding style. I can probably help.

Once your modified code runs, you can send me a **pull request**. I will then check it, and merge it if it is ok.

The folder [[00_Documents]] contains documentation about the code and its architecture.

## Note about github distribution (for personnal use mainly)

To get the number of downloads for version 7.2.0 :

~~~~~~~~~~~~~
curl -i https://api.github.com/repos/rosmord/jsesh/releases/11259307
~~~~~~~~~~~~~

Remove the last number for all releases.

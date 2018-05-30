#!/bin/bash

# A shell script to start JSesh sign info.

# remove when script is ok.
LOG=/tmp/$$.log
exec > $LOG 2>&1
set -x
env

# JRE is not known by java_home...
# find it here:

#Check which of those properties are really needed...
PROPERTIES="-Dapple.awt.textantialiasing=true -Dapple.laf.useScreenMenuBar=true"
PROPERTIES="$PROPERTIES -Dapple.awt.antialiasing=true -Dapple.awt.showGrowBox=true -Dapple.awt.graphics.UseQuartz=true"

# Allows drag and drop to application icon:
export CFProcessPath="$0"
# find relevant folders
BINDIR=$(dirname "$0")
CONTENTS="$BINDIR/.."
LIB="$CONTENTS/../../JSesh.app/Contents/lib"
JRE="$CONTENTS/../../JSesh.app/Contents/jre/bin/java"
# Starts java
# removed -Djava.library.path="$LIB" because not needed anymore...
 APPCLASS=jsesh.utilitySoftwares.signInfoEditor.ui.Main
"$JRE" -Xmx512m  -cp "$LIB/*" -Xdock:name="Sign Info" $APPCLASS

#!/bin/bash

# A shell script to start JSesh.

# remove when script is ok.
#LOG=/tmp/$$.log
#exec > $LOG 2>&1
#set -x

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
LIB="$CONTENTS/lib"
JRE="$CONTENTS/jre/bin/java"
# Starts java
"$JRE" -Xmx512m  -cp "$LIB/*" -Xdock:icon="$CONTENTS/Resources/hibou.icns" -Xdock:name="JSesh" jsesh.jhotdraw.JSeshMain

